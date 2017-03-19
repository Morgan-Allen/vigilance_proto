

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
  
  public static class Role extends Index.Entry implements Session.Saveable {
    
    final String name;
    
    public Role(String ID, String name) {
      super(ROLES_INDEX, ID);
      this.name = name;
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
    ROLE_TIME      = new Role("role_time"     , "Time"     ),
    ROLE_OBJECTIVE = new Role("role_objective", "Objective"),
    ROLE_BASE      = new Role("role_base"     , "Base"     ),
    ROLE_HIDEOUT   = new Role("role_hideout"  , "Hideout"  ),
    ROLE_ORGANISER = new Role("role_organiser", "Organiser"),
    ROLE_ENFORCER  = new Role("role_enforcer" , "Enforcer" ),
    ROLE_TARGET    = new Role("role_target"   , "Target"   ),
    ROLE_SCENE     = new Role("role_scene"    , "Scene"    )
  ;
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  int spookLevel = 0, nextContactID = 0;
  List <RoleEntry> entries = new List();
  List <Step     > steps   = new List();
  
  String caseLabel;
  
  
  
  protected Plot(PlotType type, Base base) {
    super(type, base.world());
    this.base = base;
    assignRole(base, ROLE_BASE);
  }
  
  
  public Plot(Session s) throws Exception {
    super(s);
    base = (Base) s.loadObject();
    spookLevel    = s.loadInt();
    nextContactID = s.loadInt();
    
    for (int n = s.loadInt(); n-- > 0;) {
      RoleEntry entry = new RoleEntry();
      entry.role     = (Plot.Role) s.loadObject();
      entry.element  = (Element  ) s.loadObject();
      entry.supplies = (Plot     ) s.loadObject();
      entries.add(entry);
    }
    for (int n = s.loadInt(); n-- > 0;) {
      steps.add(Step.loadStep(s));
    }
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(base);
    s.saveInt(spookLevel   );
    s.saveInt(nextContactID);
    
    s.saveInt(entries.size());
    for (RoleEntry entry : entries) {
      s.saveObject(entry.role    );
      s.saveObject(entry.element );
      s.saveObject(entry.supplies);
    }
    s.saveInt(steps.size());
    for (Step step : steps) {
      step.saveStep(s);
    }
  }
  
  
  
  /**  Queueing and executing sub-events and generating clues for
    *  investigation-
    */
  protected Step queueStep(int medium, int timeTaken, Role... involves) {
    Step s = new Step();
    s.involved  = involves ;
    s.medium    = medium   ;
    s.timeTaken = timeTaken;
    s.ID        = nextContactID++;
    if (Lead.isPhysical(medium)) s.setMeetsAt(involves[0]);
    steps.add(s);
    return s;
  }
  
  
  protected void queueSteps(int medium, int timeTaken, Role from, Role... to) {
    for (Role r : to) queueStep(medium, timeTaken, from, r);
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
  
  
  public Series <Step> allSteps() {
    return steps;
  }
  
  
  public void takeSpooking(int spookAmount) {
    spookLevel += spookAmount;
    float abortFactor = spookLevel * 1f / Lead.PROFILE_SUSPICIOUS;
    abortFactor = (abortFactor / entries.size()) - 1;
    //
    //  If the perps get too spooked, the plot may be cancelled, and any
    //  further investigation will be wasting it's time...
    if (Rand.num() < abortFactor) {
      completeEvent();
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
    RoleEntry match = roleFor(element, role);
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
    return roleFor(null, role).element;
  }
  
  
  public Batch <Element> elementsWithRole(Role role) {
    Batch <Element> matches = new Batch();
    for (RoleEntry entry : entries) {
      if (entry.role == role) matches.add(entry.element);
    }
    return matches;
  }
  
  
  protected RoleEntry roleFor(Element element, Role role) {
    for (RoleEntry entry : entries) {
      if (element != null && element != entry.element) continue;
      if (role    != null && role    != entry.role   ) continue;
      return entry;
    }
    return null;
  }
  

  
  /**  Life cycle and execution:
    */
  public void fillAndExpand() {
    fillRoles();
  }
  
  
  protected abstract boolean fillRoles();
  protected abstract void onCompletion(Step step);
  
  
  public boolean possible() {
    for (RoleEntry entry : entries) {
      if (! rolePossible(entry.role, entry.element, entry.supplies)) {
        return false;
      }
    }
    return true;
  }
  
  
  public void updateEvent() {
    for (RoleEntry entry : entries) if (entry.supplies != null) {
      if (! entry.supplies.hasBegun()) {
        world.events.scheduleEvent(entry.supplies);
      }
    }
    //  TODO:  Re-satisfy needs as and when required.
    if (! possible()) return;
    
    int time = world.timing.totalHours();
    Step current = null, next = null;
    for (Step c : steps) {
      if (next    == null && ! stepBegun(c)) next    = c;
      if (current == null &&   stepBegun(c)) current = c;
    }
    
    boolean currentEnds = current == null || stepComplete(current);
    if (currentEnds && current != null) {
      checkForTipoffs(current, false, true);
    }
    if (currentEnds && next != null) {
      next.timeStart = time;
      checkForTipoffs(next, true, false);
    }
    if (currentEnds && current == steps.last()) {
      //  TODO:  Execute the actual crime.
      completeEvent();
    }
  }
  
  
  protected boolean rolePossible(Role role, Element element, Plot supplies) {
    if (supplies != null) {
      if (! supplies.possible()) return false;
      if (! supplies.complete()) return false;
    }
    return true;
  }
  
  
  public Element targetElement(Person p) {
    return target();
  }
  
  
  public void completeEvent() {
    super.completeEvent();
  }
  
  
  
  /**  Generating reports and tipoffs:
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
      EventReport report = generateReport(step);
      Clue clue = new Clue(this, ROLE_TARGET);
      clue.confirmTipoff(target(), Lead.LEAD_REPORT, time, site);
      CaseFile file = player.leads.caseFor(target());
      file.recordClue(clue, report);
    }
  }
  
  
  protected EventReport generateReport(Step step) {
    EventReport report = new EventReport();
    report.composeFromEvent(this, 0, 1);
    return report;
  }
  
  
  
  /**  Dealing with scene-population:
    */
  public Scene generateScene(Step step, Element focus, Lead player) {
    Series <Element> involved = involved(step);
    
    if (! involved.includes(focus)) {
      I.complain("Step: "+step+" does not involve: "+focus);
      return null;
    }
    
    World world = base.world();
    Place place = focus.place();
    Scene scene = place.kind().sceneType().generateScene(world);
    
    final List <Person> forces = new List();
    for (Element e : involved) {
      if (e == null || e.type != Kind.TYPE_PERSON) continue;
      forces.add((Person) e);
    }
    
    final float dangerLevel = 0.5f;
    final PersonType GOONS[] = base.goonTypes().toArray(PersonType.class);
    float forceLimit = dangerLevel * 10;
    float forceSum   = 0;
    
    while (forceSum < forceLimit) {
      PersonType ofGoon = (PersonType) Rand.pickFrom(GOONS);
      Person goon = Person.randomOfKind(ofGoon, base.world());
      forceSum += goon.stats.powerLevel();
      forces.add(goon);
    }
    
    scene.entry.provideInProgressEntry(forces);
    scene.entry.provideBorderEntry(player.assigned());
    
    return scene;
  }
  
  
  public void completeAfterScene(Scene scene, EventReport report) {
    super.completeAfterScene(scene, report);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public void printRoles() {
    I.say("\n\nRoles are: ");
    for (Plot.RoleEntry entry : entries) {
      I.say("  "+entry);
    }
    I.say("\nSteps are: ");
    for (Step step : steps) {
      I.say("  "+step);
    }
  }
  
  
  public String name() {
    return type.name+": "+target();
  }
  
  
  public String nameForCase(Base base) {
    CaseFile file = base.leads.caseFor(this);
    CaseFile forTarget = base.leads.caseFor(target());
    boolean targetKnown = false, typeKnown = false;
    
    for (Clue clue : file.clues) if (clue.heistType == this.type) {
      typeKnown = true;
    }
    for (Clue clue : forTarget.clues) if (clue.confirmed) {
      targetKnown = true;
    }
    
    String name = caseLabel;
    if (targetKnown && typeKnown) name += type.name+": "+target();
    else if (targetKnown) name += " (target: "+target()+")";
    else if (typeKnown  ) name += " ("+type.name+")";
    return name;
  }
  
  
}




