

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.base.MessageUtils;

import static proto.game.event.Lead.*;
import static proto.game.event.LeadType.*;



public abstract class Plot extends Event implements Assignment {
  
  
  /**  Role-definitions-
    */
  final public static String
    ASPECT = "Aspect",
    PERP   = "Perp"  ,
    ITEM   = "Item"  ,
    SCENE  = "Scene" ,
    VICTIM = "Victim";
  
  public static Role role(
    String ID, String name, String category, String descTemplate
  ) {
    return new Role(ID, name, category, descTemplate);
  }
  
  final public static Role
    ROLE_OBJECTIVE = role(
      "role_objective", "Objective", ASPECT,
      "<faction> are planning <aim>."
    ),
    ROLE_TIME = role(
      "role_time", "Time", ASPECT,
      "<plot> is scheduled for <time>."
    ),
    ROLE_MASTERMIND = role(
      "role_mastermind", "Mastermind", PERP,
      "<suspect> is the mastermind for <plot>."
    ),
    ROLE_HQ = role(
      "role_hq", "HQ", SCENE,
      "<suspect> is the headquarters for <faction>."
    ),
    ROLE_ORGANISER = role(
      "role_organiser", "Organiser", PERP,
      "<suspect> is the organiser for <plot>."
    ),
    ROLE_HIDEOUT = role(
      "role_hideout", "Hideout", SCENE,
      "<suspect> is the hideout for a crew involved in <plot>."
    ),
    ROLE_TARGET = role(
      "role_target", "Target", VICTIM,
      "<suspect> is the target of <plot>."
    ),
    ROLE_SCENE = role(
      "role_scene", "Scene", SCENE,
      "<suspect> is the scene for <plot>."
    ),
    KNOWS_AIM [] = { ROLE_MASTERMIND, ROLE_ORGANISER },
    NEVER_SHOW[] = { ROLE_OBJECTIVE, ROLE_TIME },
    NEVER_TIP [] = { ROLE_MASTERMIND, ROLE_HQ, ROLE_TARGET, ROLE_SCENE };
  
  final public static int
    STATE_INIT    = -1,
    STATE_ACTIVE  =  0,
    STATE_SPOOKED =  2,
    STATE_SUCCESS =  3,
    STATE_FAILED  =  4;
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  List <RoleEntry> entries = new List();
  List <Person> involved = new List();
  
  private class RoleEntry {
    
    Role role, location;
    Element element;
    Plot supplies;
    
    public String toString() {
      return element+" ("+role+")";
    }
  }
  
  int state      = STATE_INIT;
  int duration   = -1;
  int tipsCount  = 0;
  int spookLevel = 0;
  
  
  protected Plot(PlotType type, Base base) {
    super(type, base.world());
    this.base = base;
  }
  
  
  public Plot(Session s) throws Exception {
    super(s);
    base = (Base) s.loadObject();
    
    for (int n = s.loadInt(); n-- > 0;) {
      RoleEntry entry = new RoleEntry();
      entry.role     = (Role   ) s.loadObject();
      entry.location = (Role   ) s.loadObject();
      entry.element  = (Element) s.loadObject();
      entry.supplies = (Plot   ) s.loadObject();
      entries.add(entry);
    }
    s.loadObjects(involved);
    
    state      = s.loadInt();
    duration   = s.loadInt();
    tipsCount  = s.loadInt();
    spookLevel = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(base);
    
    s.saveInt(entries.size());
    for (RoleEntry entry : entries) {
      s.saveObject(entry.role    );
      s.saveObject(entry.location);
      s.saveObject(entry.element );
      s.saveObject(entry.supplies);
    }
    s.saveObjects(involved);
    
    s.saveInt(state     );
    s.saveInt(duration  );
    s.saveInt(tipsCount );
    s.saveInt(spookLevel);
  }
  
  
  
  /**  Queueing and executing sub-events and generating clues for
    *  investigation-
    */
  public Element targetElement(Person p) {
    return location(roleFor(p));
  }
  
  
  public int tense() {
    int
      time   = base.world().timing.totalHours(),
      begins = timeBegins(),
      ends   = timeComplete();
    if (begins == -1 || begins < time) return LeadType.TENSE_FUTURE;
    if (ends   != -1 && ends   < time) return LeadType.TENSE_PAST  ;
    return LeadType.TENSE_PRESENT;
  }
  
  
  
  /**  General update cycle and associated methods-
    */
  public void updateEvent() {
    for (RoleEntry entry : entries) if (entry.supplies != null) {
      if (! entry.supplies.hasBegun()) {
        world.events.scheduleEvent(entry.supplies);
      }
    }
    if (! possible()) return;
    
    if (this.state != STATE_ACTIVE) {
      this.state = STATE_ACTIVE;
      checkForTipoffs(true, false, base.world().playerBase());
    }
    
    int time = base.world().timing.totalHours();
    if (time >= timeBegins() + duration) {
      completeEvent();
    }
  }
  
  
  public void takeSpooking(int spookAmount, Element spooked) {
    boolean report = GameSettings.eventsVerbose;
    //
    //  Increment the spook factor:
    spookLevel += spookAmount;
    float abortFactor = spookLevel * 1f / PROFILE_SUSPICIOUS;
    abortFactor = (abortFactor / entries.size()) - 1;
    float roll = Rand.num();
    if (report) {
      I.say("\nIncrementing spook level for "+this);
      I.say("  Spook amount: "+spookAmount);
      I.say("  Spook Level:  "+spookLevel );
      I.say("  Roll vs. Abort factor was: "+roll+" vs. "+abortFactor);
    }
    //
    //  If the perps get too spooked, the plot may be cancelled, and any
    //  further investigation will be wasting it's time.
    if (roll < abortFactor) {
      if (report) {
        I.say("  Participants were too spooked!  Cancelling plot: "+this);
      }
      this.state = STATE_SPOOKED;
      completeEvent();
    }
  }
  
  
  public void completeEvent() {
    if (state == STATE_ACTIVE) {
      checkForTipoffs(false, true, base.world().playerBase());
      this.state = STATE_SUCCESS;
    }
    for (Person perp : involved) perp.removeAssignment(this);
    involved.clear();
    super.completeEvent();
  }
  
  
  public void completeAfterScene(Scene scene, EventEffects report) {
    if (report.playerWon()) {
      this.completeEvent();
      this.state = STATE_FAILED;
    }
    else {
      this.state = STATE_SUCCESS;
    }
    report.applyEffects(scene.site());
  }
  
  
  public boolean checkExpired(boolean complete) {
    if (! complete) return false;
    int timeExpires = timeComplete() + LeadType.CLUE_EXPIRATION_TIME;
    boolean expired = world.timing.totalHours() > timeExpires;
    
    if (expired) {
      MessageUtils.presentColdCaseMessage(world.view(), this, state);
    }
    return expired;
  }
  
  
  
  /**  Utility methods for assigning roles, fulfilling needs and evaluating
    *  possible targets-
    */
  public void assignRole(
    Element element, Role role, Role location
  ) {
    RoleEntry match = entryFor(element, role);
    if (match == null) entries.add(match = new RoleEntry());
    
    match.role     = role    ;
    match.location = location;
    match.element  = element ;
  }
  
  
  public void assignRole(Place location, Role role) {
    assignRole(location, role, role);
  }
  
  
  public void assignRole(Person person, Role role) {
    assignRole(person, role, ROLE_HIDEOUT);
  }
  
  
  public void assignTarget(Place target) {
    assignTarget(target, target, ROLE_SCENE);
  }
  
  
  public void assignTarget(Element target, Place scene, Role stays) {
    assignRole(scene , ROLE_SCENE        );
    assignRole(target, ROLE_TARGET, stays);
  }
  
  
  public void assignOrganiser(Person organiser, Place hideout) {
    assignRole(hideout  , ROLE_HIDEOUT                );
    assignRole(organiser, ROLE_ORGANISER, ROLE_HIDEOUT);
  }
  
  
  public void assignMastermind(Person mastermind, Place based) {
    assignRole(based     , ROLE_HQ                 );
    assignRole(mastermind, ROLE_MASTERMIND, ROLE_HQ);
  }
  
  
  public Base base() {
    return base;
  }
  
  
  public Person mastermind() {
    return (Person) filling(ROLE_MASTERMIND);
  }
  
  
  public Place HQ() {
    return (Place) filling(ROLE_HQ);
  }
  
  
  public Person organiser() {
    return (Person) filling(ROLE_ORGANISER);
  }
  
  
  public Place hideout() {
    return (Place) filling(ROLE_HIDEOUT);
  }
  
  
  public Element target() {
    return filling(ROLE_TARGET);
  }
  
  
  public Place scene() {
    return (Place) filling(ROLE_SCENE);
  }
  
  
  public Element filling(Role role) {
    RoleEntry entry = entryFor(null, role);
    return entry == null ? null : entry.element;
  }
  
  
  public Place location(Role role) {
    RoleEntry entry = entryFor(null, role);
    Element location = entry == null ? null : filling(entry.location);
    if (location == null || ! location.isPlace()) return null;
    return (Place) location;
  }
  
  
  public Batch <Element> elementsWithRole(Role role) {
    Batch <Element> matches = new Batch();
    for (RoleEntry entry : entries) {
      if (entry.role == role) matches.add(entry.element);
    }
    return matches;
  }
  
  
  protected RoleEntry entryFor(Element element, Role role) {
    if (role == null && element == null) {
      return null;
    }
    for (RoleEntry entry : entries) {
      if (element != null) {
        if (element != entry.element) continue;
      }
      if (role != null) {
        if (role != entry.role) continue;
      }
      return entry;
    }
    return null;
  }
  
  
  public Series <Role> allRoles() {
    Batch <Role> all = new Batch();
    for (RoleEntry entry : entries) {
      all.include(entry.role);
    }
    return all;
  }
  
  
  public Series <Element> allInvolved() {
    Batch <Element> involved = new Batch();
    for (RoleEntry entry : entries) {
      involved.include(entry.element);
    }
    return involved;
  }
  
  
  public Role roleFor(Element element) {
    RoleEntry entry = entryFor(element, null);
    return entry == null ? null : entry.role;
  }
  
  
  protected abstract boolean fillRoles();
  protected abstract float ratePlotFor(Person mastermind);
  

  
  /**  Implementing the Assignment interface-
    */
  public Series <Person> assigned() {
    return involved;
  }
  
  
  public boolean allowsAssignment(Person p) {
    return true;
  }
  
  
  public void setAssigned(Person p, boolean is) {
    involved.toggleMember(p, is);
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_PLAN_STEP;
  }
  
  
  public Base assigningBase() {
    return base;
  }
  

  
  /**  Supplementary methods for setting up other plots as sub-steps:
    */
  public void fillAndExpand() {
    fillRoles();
  }
  
  
  public boolean possible() {
    for (RoleEntry entry : entries) {
      if (! rolePossible(entry.role, entry.element, entry.supplies)) {
        return false;
      }
    }
    return true;
  }
  
  
  protected boolean rolePossible(Role role, Element element, Plot supplies) {
    if (supplies != null) {
      if (! supplies.possible()) return false;
      if (! supplies.complete()) return false;
    }
    return true;
  }
  
  
  
  /**  Generating reports, tipoffs, actual scenes, and other after-effects:
    */
  protected void checkForTipoffs(
    boolean begins, boolean ends, Base follows
  ) {
    int time = world.timing.totalHours();
    Element involved[] = allInvolved().toArray(Element.class);
    
    if (begins && tipsCount < 2) {
      float tipWeights[] = new float[involved.length], sumWeights = 0;
      
      for (int i = involved.length; i-- > 0;) {
        Role    focusRole = roleFor(involved[i]);
        Element focus     = filling(focusRole);
        Place   at        = focus.place();
        float   trust     = at.region().currentValue(Region.TRUST);
        float   tipChance = trust / 100f;
        
        if (Visit.arrayIncludes(NEVER_TIP, focusRole)) tipChance = 0;
        else if (GameSettings.freeTipoffs) tipChance = 1;
        else if (GameSettings.noTipoffs  ) tipChance = 0;
        else if (focusRole.isPerp  ()    ) tipChance /= 2;
        else if (focusRole.isScene ()    ) tipChance /= 2;
        else if (focusRole.isItem  ()    ) tipChance /= 2;
        else if (focusRole.isVictim()    ) tipChance += 0.25f;
        
        sumWeights += tipWeights[i] = Nums.max(0, tipChance);
      }
      
      Role pickRole = (Role) Rand.pickFrom(involved, tipWeights);
      if (sumWeights == 0) pickRole = null;
      
      if (pickRole != null && (tipsCount == 0 || sumWeights > Rand.num())) {
        Element  pick   = filling(pickRole);
        Region   at     = pick.place().region();
        CaseFile file   = follows.leads.caseFor(this);
        Clue     tipoff = null;
        
        Series <Clue> clues = ClueUtils.possibleClues(
          this, pick, pick, follows, TIPOFF
        );
        tipoff = ClueUtils.pickFrom(clues);
        
        if (tipoff != null) {
          file.recordClue(tipoff, TIPOFF, time, at);
          tipsCount += 1;
        }
      }
    }
    
    if (ends) {
      EventEffects effects = generateEffects();
      Element  target = target();
      Place    scene  = target.place();
      CaseFile file   = follows.leads.caseFor(this);
      Clue 
        report = Clue.confirmSuspect(this, ROLE_TARGET, target, scene),
        aim    = Clue.confirmAim    (this                            ),
        forAt  = Clue.confirmSuspect(this, ROLE_SCENE , scene , scene);
      
      file.recordClue(report, null, REPORT, time, scene, effects, true );
      file.recordClue(aim   ,       REPORT, time, scene         , false);
      file.recordClue(forAt ,       REPORT, time, scene         , false);
      
      effects.applyEffects(target.place());
      recordEffects(effects);
    }
  }
  
  
  public EventEffects generateEffects() {
    EventEffects effects = new EventEffects(this, location(ROLE_TARGET));
    float roughs = Rand.num() / 2, losses = Rand.num() / 2;
    effects.composeFromEvent(this, 0 + roughs, 1 - losses);
    return effects;
  }
  
  
  public Scene generateScene(Element focus, Lead lead) {
    Place scene = focus.place();
    if (scene == location(ROLE_TARGET)) {
      return PlotUtils.generateHeistScene(this, focus, lead);
    }
    if (scene == hideout() || scene == HQ()) {
      return PlotUtils.generateHideoutScene(this, focus, lead);
    }
    return PlotUtils.generateSideScene(this, focus, lead);
  }
  
  
  public EventEffects generateEffects(Scene scene) {
    Lead lead = (Lead) scene.playerTask();
    return PlotUtils.generateSceneEffects(scene, this, lead);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public void printRoles() {
    I.say("\nRoles are: ");
    for (RoleEntry entry : entries) {
      I.say("  "+entry);
    }
  }
  
  
  public void printLocations() {
    I.say("\nLocations: ");
    for (RoleEntry entry : entries) {
      I.say("  "+entry.element+" ("+entry.role+") -> "+entry.element.place());
    }
  }
  
  
  public String name() {
    return type.name+" of "+target();
  }
  
}




