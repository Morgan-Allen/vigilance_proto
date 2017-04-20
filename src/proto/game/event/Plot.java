

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;



public abstract class Plot extends Event implements Assignment {
  
  
  /**  Role-definitions-
    */
  final static Index <Role> ROLES_INDEX = new Index <Role> ();
  
  final public static String
    ASPECT = "Aspect",
    PERP   = "Perp"  ,
    ITEM   = "Item"  ,
    SCENE  = "Scene" ,
    VICTIM = "Victim";
  
  public static class Role extends Index.Entry implements Session.Saveable {
    
    final public String name;
    final public String category;
    
    Role(String ID, String name, String category) {
      super(ROLES_INDEX, ID);
      this.name = name;
      this.category = category;
    }
    
    public static Role loadConstant(Session s) throws Exception {
      return ROLES_INDEX.loadEntry(s.input());
    }
    
    public void saveState(Session s) throws Exception {
      ROLES_INDEX.saveEntry(this, s.output());
    }
    
    public String toString() {
      return name;
    }
    
    public boolean isAspect() {
      return category == ASPECT;
    }
    
    public boolean isPerp() {
      return category == PERP;
    }
    
    public boolean isItem() {
      return category == ITEM;
    }
    
    public boolean isScene() {
      return category == SCENE;
    }
    
    public boolean isVictim() {
      return category == VICTIM;
    }
  }
  
  public static Role role(String ID, String name, String category) {
    return new Role(ID, name, category);
  }
  
  final public static Role
    ROLE_OBJECTIVE  = new Role("role_objective" , "Objective" , ASPECT),
    ROLE_TIME       = new Role("role_time"      , "Time"      , ASPECT),
    ROLE_MASTERMIND = new Role("role_mastermind", "Mastermind", PERP  ),
    ROLE_HQ         = new Role("role_hq"        , "HQ"        , SCENE ),
    ROLE_ORGANISER  = new Role("role_organiser" , "Organiser" , PERP  ),
    ROLE_HIDEOUT    = new Role("role_hideout"   , "Hideout"   , SCENE ),
    ROLE_TARGET     = new Role("role_target"    , "Target"    , VICTIM),
    ROLE_SCENE      = new Role("role_scene"     , "Scene"     , SCENE ),
    
    MAIN_ROLES[] = { ROLE_MASTERMIND, ROLE_ORGANISER, ROLE_TARGET };
  
  
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
  
  List <Step> steps = new List();
  Step current = null;
  int stepTimes[];
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
      entry.role     = (Plot.Role) s.loadObject();
      entry.location = (Plot.Role) s.loadObject();
      entry.element  = (Element  ) s.loadObject();
      entry.supplies = (Plot     ) s.loadObject();
      entries.add(entry);
    }
    s.loadObjects(involved);
    
    s.loadObjects(steps);
    current = (Step) s.loadObject();
    stepTimes = new int[steps.size()];
    for (int i = 0; i < steps.size(); i++) stepTimes[i] = s.loadInt();
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
    
    s.saveObjects(steps);
    s.saveObject(current);
    for (int t : stepTimes) s.saveInt(t);
    s.saveInt(spookLevel);
  }
  
  
  
  /**  Queueing and executing sub-events and generating clues for
    *  investigation-
    */
  protected void queueSteps(Step... steps) {
    this.steps     = new List();
    this.stepTimes = new int[steps.length];
    this.current   = null;
    
    check: for (Step s : steps) if (s != null) {
      for (Role r : s.involved) if (filling(r) == null) {
        I.say("  Could not fill role!");
        continue check;
      }
      this.steps.add(s);
    }
    for (int i = steps.length; i-- > 0;) stepTimes[i] = -1;
  }
  
  
  public Step currentStep() {
    return current;
  }
  
  
  public Series <Step> allSteps() {
    return steps;
  }
  
  
  public Step mainHeist() {
    for (Step s : steps) {
      if (s.subject != ROLE_TARGET        ) continue;
      if (s.medium  != Lead.MEDIUM_ASSAULT) continue;
      return s;
    }
    return null;
  }
  
  
  public int startTime(Step step) {
    return stepTimes[steps.indexOf(step)];
  }
  

  public boolean begun(Step step) {
    return startTime(step) >= 0;
  }
  
  
  public boolean complete(Step step) {
    if (! begun(step)) return false;
    int time = base.world().timing.totalHours();
    return time >= startTime(step) + step.hoursTaken;
  }
  
  
  public int timeScheduled(Step step) {
    if (current == null) return -1;
    int time = -1;
    for (Step s : steps) {
      if (time == -1) time = startTime(step);
      time += s.hoursTaken;
      if (s == step) break;
    }
    return time;
  }
  
  
  public int tense(Step step) {
    int start = startTime(step);
    if (start == -1) return Lead.TENSE_NONE;
    int time = base.world().timing.totalHours();
    boolean begun = start >= 0;
    boolean done = time >= (start + step.hoursTaken);
    if (begun) return done ? Lead.TENSE_AFTER : Lead.TENSE_DURING;
    return Lead.TENSE_BEFORE;
  }
  
  
  public Series <Element> involved(Step step) {
    Batch <Element> all = new Batch();
    for (Role r : step.involved) all.add(filling(r));
    return all;
  }
  
  
  public Place from(Step step) {
    return location(step.from);
  }
  
  
  public Place goes(Step step) {
    return location(step.goes);
  }
  
  
  public Place goes(Person p, Step step) {
    Role role = roleFor(p);
    if (Visit.arrayIncludes(step.involved, role)) {
      if (step.isWired()) {
        return (role == step.subject) ? goes(step) : from(step);
      }
      else {
        return goes(step);
      }
    }
    return location(role);
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
    
    boolean verbose = GameSettings.eventsVerbose;
    
    if (current == null || complete(current)) {
      int time = world.timing.totalHours();
      if (verbose) {
        I.say("\n\n\nUpdating plot: "+this);
      }
      
      if (current != null) {
        if (verbose) {
          I.say("  Ended step: "+current.label());
        }
        checkForTipoffs(current, false, true);
        boolean success = checkSuccess(current);
        onCompletion(current, success);
      }
      if (current != steps.last()) {
        int nextIndex = current == null ? 0 : (steps.indexOf(current) + 1);
        current = steps.atIndex(nextIndex);
        stepTimes[steps.indexOf(current)] = time;
        checkForTipoffs(current, true, false);
        if (verbose) {
          I.say("  Began step: "+current.label());
        }
      }
      else {
        if (verbose) {
          I.say("  Plot completed.");
        }
        completeEvent();
      }
      
      for (Element e : involved(current)) if (e.isPerson()) {
        ((Person) e).addAssignment(this);
      }
      for (Person p : assigned()) {
        Place goes = goes(p, current);
        if (goes != null) goes.setAttached(p, true);
      }
      
      if (verbose) {
        I.say("  Current Time: "+time);
        I.say("Current Step: "+current);
        printLocations();
      }
    }
  }
  
  
  public void advanceToStep(Step step) {
    current = step;
    stepTimes[steps.indexOf(step)] = world.timing.totalHours();
    printSteps();
  }
  
  
  public void takeSpooking(int spookAmount) {
    spookLevel += spookAmount;
    float abortFactor = spookLevel * 1f / Lead.PROFILE_SUSPICIOUS;
    abortFactor = (abortFactor / entries.size()) - 1;
    float roll = Rand.num();
    //
    //  If the perps get too spooked, the plot may be cancelled, and any
    //  further investigation will be wasting it's time...
    if (roll < abortFactor) {
      if (GameSettings.eventsVerbose) {
        I.say("\nParticipants were too spooked!  Cancelling plot: "+this);
        I.say("  Spook Level: "+spookLevel);
        I.say("  Roll vs. Abort factor was: "+roll+" vs. "+abortFactor);
      }
      completeEvent();
    }
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
  
  
  public Element targetElement(Person p) {
    if (current == null) return location(roleFor(p));
    return goes(p, current);
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
  
  
  public Batch <Element> allInvolved() {
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
  protected abstract boolean checkSuccess(Step step);
  protected abstract void onCompletion(Step step, boolean success);
  

  
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
  protected void checkForTipoffs(Step step, boolean begins, boolean ends) {
    
    Role    focusRole = (Role) Rand.pickFrom(step.involved);
    Element focus     = filling(focusRole);
    Place   at        = focus.place();
    float   trust     = at.region().currentValue(Region.TRUST);
    float   tipChance = trust / (10f * (1 + base.organisationRank(focus)));
    Base    player    = world.playerBase();
    int     time      = world.timing.totalHours();
    
    if (GameSettings.freeTipoffs) tipChance = 1;
    if (GameSettings.noTipoffs  ) tipChance = 0;
    
    if (begins && Rand.num() < tipChance) {
      Clue tipoff = Clue.confirmSuspect(this, focusRole, focus, at);
      CaseFile file = player.leads.caseFor(this);
      file.recordClue(tipoff, Lead.LEAD_TIPOFF, time, at);
    }
    
    if (ends && step == mainHeist()) {
      EventEffects effects = generateEffects(step);
      Element  target = target();
      Place    scene  = target.place();
      Clue     report = Clue.confirmSuspect(this, ROLE_TARGET, target, scene);
      Clue     aim    = Clue.confirmAim(this);
      CaseFile file   = player.leads.caseFor(this);
      file.recordClue(aim   , Lead.LEAD_REPORT, time, scene, false);
      file.recordClue(report, Lead.LEAD_REPORT, time, scene       );
      effects.applyEffects(target.place());
      recordEffects(effects);
    }
  }
  
  
  public EventEffects generateEffects(Step step) {
    EventEffects effects = new EventEffects();
    float roughs = Rand.num() / 2, losses = Rand.num() / 2;
    effects.composeFromEvent(this, 0 + roughs, 1 - losses);
    return effects;
  }
  
  
  public void completeEvent() {
    for (Person perp : involved) perp.removeAssignment(this);
    involved.clear();
    super.completeEvent();
  }
  
  
  public Scene generateScene(Step step, Element focus, Lead lead) {
    if (step.isAssault()) {
      return PlotUtils.generateHeistScene(this, step, focus, lead);
    }
    if (focus.place() == hideout() || focus.place() == HQ()) {
      return PlotUtils.generateHideoutScene(this, step, focus, lead);
    }
    return PlotUtils.generateSideScene(this, step, focus, lead);
  }
  
  
  public EventEffects generateEffects(Scene scene) {
    Lead lead = (Lead) scene.playerTask();
    return PlotUtils.generateSceneEffects(scene, this, lead);
  }
  
  
  public void completeAfterScene(Scene scene, EventEffects report) {
    super.completeAfterScene(scene, report);
    report.applyEffects(scene.site());
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public void printRoles() {
    I.say("\nRoles are: ");
    for (Plot.RoleEntry entry : entries) {
      I.say("  "+entry);
    }
  }
  
  
  public void printSteps() {
    I.say("\nSteps are:");
    for (Step c : steps) {
      I.say("  "+c);
    }
  }
  
  
  public void printLocations() {
    I.say("\nLocations: ");
    for (Plot.RoleEntry entry : entries) {
      I.say("  "+entry.element+" ("+entry.role+") -> "+entry.element.place());
    }
  }
  
  
  public String name() {
    return type.name+" of "+target();
  }
  
}




