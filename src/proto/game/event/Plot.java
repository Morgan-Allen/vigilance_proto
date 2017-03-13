

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public abstract class Plot extends Event {
  
  
  /**  Role-definitions-
    */
  final static Index <Role> ROLES_INDEX = new Index <Role> ();
  
  static class Role extends Index.Entry implements Session.Saveable {
    
    final String name;
    
    protected Role(String ID, String name) {
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
    ROLE_BASE      = new Role("role_base"     , "Base"     ),
    ROLE_HIDEOUT   = new Role("role_hideout"  , "Hideout"  ),
    ROLE_ORGANISER = new Role("role_organiser", "Organiser"),
    ROLE_BACKUP    = new Role("role_backup"   , "Backup"   ),
    ROLE_TARGET    = new Role("role_target"   , "Target"   ),
    ROLE_SCENE     = new Role("role_scene"    , "Scene"    )
  ;
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  int spookLevel = 0, nextContactID = 0;
  List <RoleEntry> entries = new List();
  List <Step  > steps   = new List();
  
  
  
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
      Step step = new Step();
      step.between   = (Role[]) s.loadObjectArray(Role.class);
      step.medium    = s.loadInt();
      step.timeTaken = s.loadInt();
      step.ID        = s.loadInt();
      step.timeStart = s.loadInt();
      step.spooked   = s.loadBool();
      steps.add(step);
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
      s.saveObjectArray(step.between);
      s.saveInt (step.medium   );
      s.saveInt (step.timeTaken);
      s.saveInt (step.ID       );
      s.saveInt (step.timeStart);
      s.saveBool(step.spooked  );
    }
  }
  
  
  
  /**  Queueing and executing sub-events and generating clues for
    *  investigation-
    */
  //  TODO:  Have this extend Task?
  protected class Step {
    
    int medium;
    int timeTaken;
    Role between[];
    int ID;
    
    int timeStart = -1;
    boolean spooked = false;
    
    public String toString() {
      return "Step involving "+I.list(between);
    }
  }
  
  
  public void queueStep(int medium, int timeTaken, Role... involved) {
    Step s = new Step();
    s.between   = involved;
    s.medium    = medium;
    s.timeTaken = timeTaken;
    s.ID        = nextContactID++;
    steps.add(s);
  }
  
  
  public void queueSteps(int medium, int timeTaken, Role from, Role... to) {
    for (Role r : to) queueStep(medium, timeTaken, from, r);
  }
  
  
  public boolean stepBegun(Step step) {
    return step != null && step.timeStart >= 0;
  }
  
  
  public boolean stepComplete(Step step) {
    if (! stepBegun(step)) return false;
    int time = base.world().timing.totalHours();
    return time >= step.timeStart + step.timeTaken;
  }
  
  
  public int stepTense(Step step) {
    int time = base.world().timing.totalHours();
    int start = step.timeStart, tense = Lead.TENSE_BEFORE;
    boolean begun = start >= 0;
    boolean done = time > (step.timeStart + step.timeTaken);
    if (begun) tense = done ? Lead.TENSE_AFTER : Lead.TENSE_DURING;
    return tense;
  }
  
  
  public Series <Step> allSteps() {
    return steps;
  }
  
  
  public void takeSpooking(int spookAmount) {
    spookLevel += spookAmount;
    //  TODO:  Abort the heist if you're too spooked!
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
  
  
  public Person organiser() {
    return (Person) elementWithRole(ROLE_ORGANISER);
  }
  
  
  public Place hideout() {
    return (Place) elementWithRole(ROLE_HIDEOUT);
  }
  
  
  public Person target() {
    return (Person) elementWithRole(ROLE_TARGET);
  }
  
  
  public Place scene() {
    return (Place) elementWithRole(ROLE_SCENE);
  }
  
  
  public Element elementWithRole(Role role) {
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
  
  
  protected void fillExpertRole(
    Trait trait, Series <Person> candidates, Role role
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : candidates) {
      if (roleFor(p, null) != null) continue;
      pick.compare(p, p.stats.levelFor(trait));
    }
    if (pick.empty()) return;
    assignRole(pick.result(), role);
  }
  
  
  protected void fillInsideRole(
    Place target, Plot.Role role
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : target.residents()) {
      if (roleFor(p, null) != null) continue;
      pick.compare(p, 0 - p.history.valueFor(target.owner()));
    }
    if (pick.empty()) return;
    assignRole(pick.result(), role);
  }
  
  
  protected void fillItemRole(
    ItemType type, World world, Role role
  ) {
    assignRole(new Item(type, world), role);
  }
  
  
  protected Series <Person> goonsOnRoster() {
    Batch <Person> goons = new Batch();
    for (Person p : base.roster()) {
      if (p == base.leader()) continue;
      goons.add(p);
    }
    return goons;
  }
  
  
  protected Series <Person> expertsWith(Trait trait, int minLevel) {
    final Batch <Person> experts = new Batch();
    for (Element e : base.world().inside()) if (e.isPerson()) {
      Person p = (Person) e;
      if (p.stats.levelFor(trait) < minLevel) continue;
      experts.add(p);
    }
    return experts;
  }
  
  
  protected Series <Place> venuesNearby(Place target, int maxDist) {
    final Batch <Place> venues = new Batch();
    return venues;
  }
  

  
  /**  Life cycle and execution:
    */
  protected boolean fillRoles() {
    //  TODO:  Make abstract and implement per-subclass.
    return false;
  }
  
  
  protected void onCompletion(Step step) {
    //  TODO:  Make abstract and implement per-subclass.
    return;
  }
  
  
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
    if ((current == null || stepComplete(current)) && next != null) {
      next.timeStart = time;
    }
    
    if (current == steps.last() && stepComplete(current)) {
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
  
  
  public Place targetLocation(Person p) {
    return scene();
  }
  
}



