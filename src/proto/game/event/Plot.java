

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.base.MessageUtils;
import proto.view.common.*;



public abstract class Plot extends Event {
  
  
  /**  Role-definitions-
    */
  final static Index <Role> ROLES_INDEX = new Index <Role> ();
  
  final public static String
    ASPECT = "Aspect",
    PERP   = "Perp"  ,
    VICTIM = "Victim",
    STEP   = "Step"  ;
  
  public static class Role extends Index.Entry implements Session.Saveable {
    
    final public String name;
    final public String category;
    
    public Role(String ID, String name, String category) {
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
  }
  
  final public static Role
    ROLE_TIME      = new Role("role_time"     , "Time"     , ASPECT),
    ROLE_OBJECTIVE = new Role("role_objective", "Objective", ASPECT),
    ROLE_BASE      = new Role("role_base"     , "Base"     , PERP  ),
    ROLE_HIDEOUT   = new Role("role_hideout"  , "Hideout"  , PERP  ),
    ROLE_ORGANISER = new Role("role_organiser", "Organiser", PERP  ),
    ROLE_ENFORCER  = new Role("role_enforcer" , "Enforcer" , PERP  ),
    ROLE_GOON      = new Role("role_goon"     , "Goon"     , PERP  ),
    ROLE_TARGET    = new Role("role_target"   , "Target"   , VICTIM),
    ROLE_SCENE     = new Role("role_scene"    , "Scene"    , VICTIM)
  ;
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  int caseID = -1;
  
  int spookLevel = 0;
  List <RoleEntry> entries = new List();
  List <Step     > steps   = new List();
  Step current = null;
  
  
  protected Plot(PlotType type, Base base) {
    super(type, base.world());
    this.base = base;
    assignRole(base, ROLE_BASE);
  }
  
  
  public Plot(Session s) throws Exception {
    super(s);
    base       = (Base) s.loadObject();
    caseID     = s.loadInt();
    spookLevel = s.loadInt();
    
    for (int n = s.loadInt(); n-- > 0;) {
      RoleEntry entry = new RoleEntry();
      entry.role     = (Plot.Role) s.loadObject();
      entry.element  = (Element  ) s.loadObject();
      entry.supplies = (Plot     ) s.loadObject();
      entries.add(entry);
    }
    s.loadObjects(steps);
    current = (Step) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(base);
    s.saveInt(caseID    );
    s.saveInt(spookLevel);
    
    s.saveInt(entries.size());
    for (RoleEntry entry : entries) {
      s.saveObject(entry.role    );
      s.saveObject(entry.element );
      s.saveObject(entry.supplies);
    }
    s.saveObjects(steps);
    s.saveObject(current);
  }
  
  
  
  /**  Queueing and executing sub-events and generating clues for
    *  investigation-
    */
  protected Step queueStep(
    String label, int medium, int timeTaken, Role... involves
  ) {
    Step s = new Step();
    s.label     = label;
    s.ID        = steps.size();
    s.involved  = involves ;
    s.medium    = medium   ;
    s.timeTaken = timeTaken;
    if (Lead.isPhysical(medium)) s.setMeetsAt(involves[0]);
    steps.add(s);
    return s;
  }
  
  
  protected Step queueMeeting(String label, Role... involves) {
    return queueStep(label, Lead.MEDIUM_MEET, World.HOURS_PER_DAY, involves);
  }
  
  
  protected Step queueMessage(String label, Role sends, Role gets, Role info) {
    Step s;
    s = queueStep(label, Lead.MEDIUM_WIRE, World.HOURS_PER_DAY, sends, gets);
    if (info != null) s.setInfoGiven(info);
    return s;
  }
  
  
  protected Step queueHeist(String label, Role... involved) {
    return queueStep(label, Lead.MEDIUM_HEIST, World.HOURS_PER_DAY, involved);
  }
  
  
  protected boolean stepBegun(Step step) {
    return step != null && step.timeStart >= 0;
  }
  
  
  public boolean stepComplete(Step step) {
    if (! stepBegun(step)) return false;
    int time = base.world().timing.totalHours();
    return time >= step.timeStart + step.timeTaken;
  }
  
  
  public Series <Element> involved(Step step) {
    Batch <Element> all = new Batch();
    for (Role role : step.involved) all.include(filling(role));
    return all;
  }
  
  
  public int stepTimeScheduled(Step step) {
    if (currentStep() == null) return -1;
    int time = -1;
    for (Step s : steps) {
      if (time == -1) time = s.timeStart;
      time += s.timeTaken;
      if (s == step) break;
    }
    return time;
  }
  
  
  public int stepTense(Step step) {
    int time = base.world().timing.totalHours();
    int start = step.timeStart, tense = Lead.TENSE_BEFORE;
    boolean begun = start >= 0;
    boolean done = time > (step.timeStart + step.timeTaken);
    if (begun) tense = done ? Lead.TENSE_AFTER : Lead.TENSE_DURING;
    return tense;
  }
  
  
  public Step currentStep() {
    for (Step s : steps) if (stepBegun(s) && ! stepComplete(s)) return s;
    return null;
  }
  
  
  public Step heistStep() {
    for (Step s : steps) if (s.medium == Lead.MEDIUM_HEIST) return s;
    return null;
  }
  
  
  public Step stepWithLabel(String label) {
    for (Step s : steps) if (label.equals(s.label)) return s;
    return null;
  }
  
  
  public Series <Step> allSteps() {
    return steps;
  }
  
  
  public void advanceToStep(Step step) {
    current = step;
    current.timeStart = world.timing.totalHours();
    printSteps();
  }
  
  
  public void takeSpooking(int spookAmount) {
    spookLevel += spookAmount;
    float abortFactor = spookLevel * 1f / Lead.PROFILE_SUSPICIOUS;
    abortFactor = (abortFactor / entries.size()) - 1;
    I.say("Abort factor is: "+abortFactor);
    //
    //  If the perps get too spooked, the plot may be cancelled, and any
    //  further investigation will be wasting it's time...
    if (Rand.num() < abortFactor) {
      I.say("Participants were too spooked!  Cancelling plot: "+this);
      completeEvent();
    }
  }
  
  
  public void updateEvent() {
    for (RoleEntry entry : entries) if (entry.supplies != null) {
      if (! entry.supplies.hasBegun()) {
        world.events.scheduleEvent(entry.supplies);
      }
    }
    if (! possible()) return;
    
    if (current == null || stepComplete(current)) {
      int time = world.timing.totalHours();
      
      if (current != null) {
        I.say("  Ended step: "+current);
        checkForTipoffs(current, false, true);
        boolean success = checkSuccess(current);
        onCompletion(current, success);
      }
      if (current != steps.last()) {
        int nextIndex = current == null ? 0 : (steps.indexOf(current) + 1);
        current = steps.atIndex(nextIndex);
        current.timeStart = time;
        checkForTipoffs(current, true, false);
        I.say("  Began step: "+current);
      }
      else {
        I.say("  Plot completed.");
        completeEvent();
      }
      
      I.say("\nUpdated plot: "+this);
      I.say("  Current Time: "+time);
      I.say("  Current Step: "+current.ID);
      printSteps();
    }
  }
  
  
  
  /**  Utility methods for assigning roles, fulfilling needs and evaluating
    *  possible targets-
    */
  protected class RoleEntry {
    
    Role role;
    Element element;
    Plot supplies;
    
    public String toString() {
      return role.name+" ("+element+")";
    }
  }
  
  
  public void assignRole(Element element, Role role) {
    RoleEntry match = entryFor(element, role);
    if (match == null) entries.add(match = new RoleEntry());
    match.role    = role   ;
    match.element = element;
  }
  
  
  public void assignTarget(Element target, Place scene) {
    assignRole(target, ROLE_TARGET);
    assignRole(scene , ROLE_SCENE );
  }
  
  
  public void assignOrganiser(Person organiser, Place hideout) {
    assignRole(organiser, ROLE_ORGANISER);
    assignRole(hideout  , ROLE_HIDEOUT  );
  }
  
  
  public Base base() {
    return base;
  }
  
  
  public Element targetElement(Person p) {
    return target();
  }
  
  
  public Person organiser() {
    return (Person) filling(ROLE_ORGANISER);
  }
  
  
  public Place hideout() {
    return (Place) filling(ROLE_HIDEOUT);
  }
  
  
  public Person target() {
    return (Person) filling(ROLE_TARGET);
  }
  
  
  public Place scene() {
    return (Place) filling(ROLE_SCENE);
  }
  
  
  public Element filling(Role role) {
    RoleEntry entry = entryFor(null, role);
    return entry == null ? null : entry.element;
  }
  
  
  public Batch <Element> elementsWithRole(Role role) {
    Batch <Element> matches = new Batch();
    for (RoleEntry entry : entries) {
      if (entry.role == role) matches.add(entry.element);
    }
    return matches;
  }
  
  
  protected RoleEntry entryFor(Element element, Role role) {
    for (RoleEntry entry : entries) {
      if (element != null && element != entry.element) continue;
      if (role    != null && role    != entry.role   ) continue;
      return entry;
    }
    return null;
  }
  
  
  public Batch <Element> allInvolved() {
    Batch <Element> involved = new Batch();
    for (RoleEntry entry : entries) involved.include(entry.element);
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
    Place   site      = focus.place();
    float   trust     = site.region().currentValue(Region.TRUST);
    float   tipChance = trust / 10f;
    Base    player    = world.playerBase();
    int     time      = world.timing.totalHours();
    
    if (begins && Rand.num() < tipChance) {
      Clue tipoff = new Clue(this, focusRole);
      tipoff.confirmTipoff(focus, Lead.LEAD_TIPOFF, time, site);
      CaseFile file = player.leads.caseFor(focus);
      file.recordClue(tipoff);
    }
    
    if (ends && step.medium == Lead.MEDIUM_HEIST) {
      EventEffects effects = generateEffects(step);
      Element target = target();
      
      Clue clue = new Clue(this, ROLE_TARGET);
      clue.confirmTipoff(target(), Lead.LEAD_REPORT, time, site);
      CaseFile file = player.leads.caseFor(target);
      file.recordClue(clue, effects, true);
      
      effects.applyEffects(target.place());
    }
  }
  
  
  public EventEffects generateEffects(Step step) {
    EventEffects effects = new EventEffects();
    effects.composeFromEvent(this, 0, 1);
    return effects;
  }
  
  
  public void completeEvent() {
    super.completeEvent();
  }
  
  
  public Scene generateScene(Step step, Element focus, Lead lead) {
    return PlotUtils.generateScene(this, step, focus, lead);
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
    I.say("\n\nRoles are: ");
    for (Plot.RoleEntry entry : entries) {
      I.say("  "+entry);
    }
  }
  
  
  public void printSteps() {
    I.say("\nSteps are:");
    for (Step c : steps) {
      int timeEnd = c.timeStart + c.timeTaken;
      if (c.timeStart == -1) I.say("  "+c+" ["+c.timeStart+"]");
      else I.say("  "+c+" ["+c.timeStart+"-"+timeEnd+"]");
    }
  }
  
  
  public String name() {
    return type.name+": "+target();
  }
  
  
  public String nameForCase(Base base) {
    //return ""+organiser()+" (No. "+caseID+")";
    //*
    CaseFile file = base.leads.caseFor(this);
    CaseFile forTarget = base.leads.caseFor(target());
    boolean targetKnown = false, typeKnown = false;
    
    for (Clue clue : file.clues) if (clue.heistType == this.type) {
      typeKnown = true;
    }
    for (Clue clue : forTarget.clues) if (clue.confirmed) {
      targetKnown = true;
    }
    
    String name = "Case No. "+caseID;
    if (targetKnown && typeKnown) name = type.name+": "+target();
    else if (targetKnown) name += " (target: "+target()+")";
    else if (typeKnown  ) name += " ("+type.name+")";
    return name;
    //*/
  }
  
}




