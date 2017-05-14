
package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;



public class Base implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public static int
    MAX_FACILITIES = 12,
    SLOTS_WIDE     = 3 ;
  
  final World world;
  final Faction faction;
  
  Person leader = null;
  List <Person> roster = new List();
  List <PersonType> goonTypes = new List();
  
  final public BaseFinance  finance  = new BaseFinance (this);
  final public BaseStocks   stocks   = new BaseStocks  (this);
  final public BaseTraining training = new BaseTraining(this);
  final public BaseLeads    leads    = new BaseLeads   (this);
  final public BasePlots    plots    = new BasePlots   (this);
  
  Place HQ = null;
  List <Object> knownTech = new List();
  
  
  public Base(World world, Faction faction) {
    this.world = world;
    this.faction = faction;
  }
  
  
  public Base(Session s) throws Exception {
    s.cacheInstance(this);
    
    world   = (World  ) s.loadObject();
    faction = (Faction) s.loadObject();
    leader  = (Person ) s.loadObject();
    s.loadObjects(roster   );
    s.loadObjects(goonTypes);
    
    finance .loadState(s);
    stocks  .loadState(s);
    training.loadState(s);
    leads   .loadState(s);
    plots   .loadState(s);
    
    HQ = (Place) s.loadObject();
    s.loadObjects(knownTech);
  }
  
  
  public void saveState(Session s) throws Exception {
    
    s.saveObject(world  );
    s.saveObject(faction);
    s.saveObject(leader );
    s.saveObjects(roster   );
    s.saveObjects(goonTypes);
    
    finance .saveState(s);
    stocks  .saveState(s);
    training.saveState(s);
    leads   .saveState(s);
    plots   .saveState(s);
    
    s.saveObject(HQ);
    s.saveObjects(knownTech);
  }
  
  
  
  /**  General stat queries-
    */
  public Faction faction() { return faction; }
  public World world() { return world; }
  
  
  
  /**  Regular updates and life-cycle methods:
    */
  void updateBase() {
    for (Person p : roster) {
      p.updateOnBase();
    }
    finance .updateFinance ();
    stocks  .updateCrafting();
    training.updateTraining();
    leads   .updateLeads   ();
    plots   .updatePlanning();
  }
  
  
  
  /**  Roster modification-
    */
  public void addToRoster(Person person) {
    roster.include(person);
    person.setBase(this);
  }
  
  
  public void assignLeader(Person leader) {
    this.leader = leader;
  }
  
  
  public void assignHQ(Place HQ) {
    this.HQ = HQ;
  }
  
  
  public float organisationRank(Element p) {
    if (p == leader || p == HQ) return 2;
    if (p.isPerson() && roster.includes((Person) p)   ) return 1;
    if (p.isPlace () && ((Place ) p).owner  () == this) return 1;
    return 0;
  }
  
  
  public Series <Person> roster() {
    return roster;
  }
  
  
  public Person leader() {
    return leader;
  }
  
  
  public Place HQ() {
    return HQ;
  }
  
  
  public Person firstOfKind(Kind kind) {
    for (Person p : roster) if (p.kind() == kind) return p;
    return null;
  }
  
  
  public Series <PersonType> goonTypes() {
    return goonTypes;
  }
  
  
  public void setGoonTypes(PersonType... types) {
    for (PersonType type: types) goonTypes.add(type);
  }
  
  
  
  /**  Tech levels and funding-
    */
  public boolean hasTech(Object tech) {
    return knownTech.includes(tech);
  }
  
  
  public boolean addTech(Object tech) {
    if (tech == null || knownTech.includes(tech)) return false;
    knownTech.add(tech);
    return true;
  }
  
  
  public Series <Object> knownTech() {
    return knownTech;
  }
  
  
  
  /**  Ongoing tasks-
    */
  public Series <Task> activeAgentTasks() {
    Batch <Task> tasks = new Batch();
    for (Person p : roster()) {
      for (Assignment a : p.assignments()) if (a instanceof Task) {
        tasks.include((Task) a);
      }
    }
    return tasks;
  }
  
  
  
  /**  Rendering and interface methods-
    */
  public String name() {
    return "Base for "+faction.name;
  }
}


















