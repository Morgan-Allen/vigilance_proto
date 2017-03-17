
package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;



public class Base extends Place {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public static int
    MAX_FACILITIES = 12,
    SLOTS_WIDE     = 3 ;
  
  
  Faction faction = null;
  Person leader = null;
  List <PersonType> goonTypes = new List();
  
  final public BaseFinance  finance  = new BaseFinance (this);
  final public BaseStocks   stocks   = new BaseStocks  (this);
  final public BaseTraining training = new BaseTraining(this);
  final public BaseLeads    leads    = new BaseLeads   (this);
  final public BasePlots    plots    = new BasePlots   (this);
  
  Place rooms[] = new Place[MAX_FACILITIES];
  List <Object> knownTech = new List();
  
  
  public Base(PlaceType kind, World world, Faction faction) {
    super(kind, 0, world);
    this.faction = faction;
  }
  
  
  public Base(Session s) throws Exception {
    super(s);
    
    faction = (Faction) s.loadObject();
    leader  = (Person) s.loadObject();
    s.loadObjects(goonTypes);
    
    finance .loadState(s);
    stocks  .loadState(s);
    training.loadState(s);
    leads   .loadState(s);
    plots   .loadState(s);
    
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      rooms[n] = (Place) s.loadObject();
    }
    s.loadObjects(knownTech);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    
    s.saveObject(faction);
    s.saveObject(leader );
    s.saveObjects(goonTypes);
    
    finance .saveState(s);
    stocks  .saveState(s);
    training.saveState(s);
    leads   .saveState(s);
    plots   .saveState(s);
    
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      s.saveObject(rooms[n]);
    }
    s.saveObjects(knownTech);
  }
  
  
  
  /**  General stat queries-
    */
  public Faction faction() { return faction; }
  public World world() { return world; }
  
  
  /**  Regular updates and life-cycle methods:
    */
  void updateBase() {
    for (Person p : residents()) {
      p.updateOnBase();
    }
    for (Place r : rooms) if (r != null) {
      r.updatePlace();
    }
    finance.updateFinance();
    stocks.updateCrafting();
    training.updateTraining();
    leads.updateLeads();
    plots.updatePlanning();
  }
  
  
  
  /**  Roster modification-
    */
  public void addToRoster(Person person) {
    Place.setResident(person, this, true);
    person.setBase(this);
  }
  
  
  public void setLeader(Person leader) {
    this.leader = leader;
  }
  
  
  public Series <Person> roster() {
    return residents();
  }
  
  
  public Person leader() {
    return leader;
  }
  
  
  public Person firstOfKind(Kind kind) {
    for (Person p : residents()) if (p.kind() == kind) return p;
    return null;
  }
  
  
  public Series <PersonType> goonTypes() {
    return goonTypes;
  }
  
  
  public void setGoonTypes(PersonType... types) {
    for (PersonType type: types) goonTypes.add(type);
  }
  
  
  public Access accessLevel(Base base) {
    return base == this ? Access.GRANTED : Access.SECRET;
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
  
  
  
  /**  Construction and salvage-
    */
  public boolean canConstruct(PlaceType print, int slot) {
    if (rooms[slot] != null) return false;
    if (print.buildCost > finance.publicFunds()) return false;
    return true;
  }
  
  
  public float buildRate(PlaceType print) {
    float rate = 1f, numBuilding = 0;
    for (Place r : rooms) if (r.buildProgress() < 1) numBuilding++;
    if (numBuilding == 0) return 1;
    return rate / (numBuilding * print.buildTime);
  }
  
  
  public int buildETA(int slot) {
    Place room = rooms[slot];
    if (room == null) return -1;
    return Nums.ceil((1 - room.buildProgress()) / buildRate(room.kind()));
  }
  
  
  public void addFacility(PlaceType print, int slot, float progress) {
    if (print == null) { rooms[slot] = null; return; }
    Place room = rooms[slot];
    if (room == null) room = rooms[slot] = print.createRoom(this, slot);
    room.setBuildProgress(progress);
  }
  
  
  public void beginConstruction(PlaceType print, int slot) {
    finance.incPublicFunds(0 - print.buildCost);
    addFacility(print, slot, 0);
  }
  
  
  public void beginSalvage(int slot) {
    Place room = rooms[slot];
    if (room == null) return;
    finance.incPublicFunds(room.kind().buildCost / 2);
    addFacility(null, slot, 1);
  }
  
  
  public Place[] rooms() {
    return rooms;
  }
  
  
  
  /**  Rendering and interface methods-
    */
  public int rosterIndex(Person p) {
    return residents().indexOf(p);
  }
  
  
  public Person atRosterIndex(int i) {
    return residents().atIndex(i);
  }
  
  
  public String name() {
    return kind().name();
  }
}


















