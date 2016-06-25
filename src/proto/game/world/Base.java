

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;



public class Base implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public static int
    MAX_FACILITIES = 12,
    SLOTS_WIDE     = 3 ;
  
  World world;
  String name;
  
  List <Person> roster = new List();
  Person leader = null;
  final public BaseStocks stocks = new BaseStocks(this);
  
  Place rooms[] = new Place[MAX_FACILITIES];
  List <Tech> knownTech = new List();
  int currentFunds, incomeFloor, income, maintenance;
  
  
  Base(World world, String name) {
    this.world = world;
    this.name  = name ;
  }
  
  
  public Base(Session s) throws Exception {
    s.cacheInstance(this);
    
    world = (World ) s.loadObject();
    name  = (String) s.loadString();
    
    s.loadObjects(roster);
    leader = (Person) s.loadObject();
    
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      rooms[n] = (Place) s.loadObject();
    }
    s.loadObjects(knownTech);
    stocks.loadState(s);
    
    currentFunds = s.loadInt();
    incomeFloor  = s.loadInt();
    income       = s.loadInt();
    maintenance  = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    
    s.saveObject(world);
    s.saveString(name );
    
    s.saveObjects(roster);
    s.saveObject(leader);
    
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      s.saveObject(rooms[n]);
    }
    s.saveObjects(knownTech);
    stocks.saveState(s);
    
    s.saveInt(currentFunds);
    s.saveInt(incomeFloor );
    s.saveInt(income      );
    s.saveInt(maintenance );
  }
  
  
  
  /**  General stat queries-
    */
  public int currentFunds() { return currentFunds; }
  public int incomeFloor () { return incomeFloor ; }
  public int income      () { return income      ; }
  public int maintenance () { return maintenance ; }
  
  public World world() { return world; }
  
  
  
  /**  Regular updates and life-cycle methods:
    */
  void updateBase(float numWeeks) {
    this.income      = 0;
    this.maintenance = 0;
    
    for (Person p : roster) {
      p.updateOnBase(numWeeks);
    }
    
    for (Place r : rooms) if (r != null) {
      r.updatePlace();
    }
    
    for (District dist : world.districts) {
      this.income      += dist.incomeFor  (this);
      this.maintenance += dist.expensesFor(this);
    }
    
    this.income += incomeFloor;
    this.currentFunds += (income - maintenance) * numWeeks;
  }
  
  
  void updateBaseDaily() {
    
  }
  
  
  
  /**  Roster modification-
    */
  public Person addToRoster(Person hero) {
    roster.add(hero);
    return hero;
  }
  
  
  public Series <Person> roster() {
    return roster;
  }
  
  
  public void setLeader(Person leader) {
    this.leader = leader;
  }
  
  
  public Person leader() {
    return leader;
  }
  
  
  public Person firstOfKind(Kind kind) {
    for (Person p : roster) if (p.kind() == kind) return p;
    return null;
  }
  
  
  
  /**  Tech levels and funding-
    */
  public boolean hasTech(Tech tech) {
    return knownTech.includes(tech);
  }
  
  
  public boolean addTech(Tech tech) {
    if (tech == null || knownTech.includes(tech)) return false;
    knownTech.add(tech);
    return true;
  }
  
  
  public boolean incFunding(int funds) {
    this.currentFunds += funds;
    return true;
  }
  
  
  public boolean setIncomeFloor(int floor) {
    this.incomeFloor = floor;
    return true;
  }
  
  
  
  
  /**  Construction and salvage-
    */
  public boolean canConstruct(Blueprint print, int slot) {
    if (rooms[slot] != null) return false;
    if (print.buildCost > currentFunds) return false;
    return true;
  }
  
  
  public float buildRate(Blueprint print) {
    float rate = 1f, numBuilding = 0;
    for (Place r : rooms) if (r.buildProgress() < 1) numBuilding++;
    if (numBuilding == 0) return 1;
    return rate / (numBuilding * print.buildTime);
  }
  
  
  public int buildETA(int slot) {
    Place room = rooms[slot];
    return Nums.ceil((1 - room.buildProgress()) / buildRate(room.built()));
  }
  
  
  public void addFacility(Blueprint print, int slot, float progress) {
    if (print == null) { rooms[slot] = null; return; }
    Place room = rooms[slot];
    if (room == null) room = rooms[slot] = print.createRoom(this, slot);
    room.setBuildProgress(progress);
  }
  
  
  public void beginConstruction(Blueprint print, int slot) {
    currentFunds -= print.buildCost;
    addFacility(print, slot, 0);
  }
  
  
  public void beginSalvage(int slot) {
    currentFunds += rooms[slot].built().buildCost / 2f;
    addFacility(null, slot, 1);
  }
  
  
  public Place[] rooms() {
    return rooms;
  }
  
  
  
  /**  Rendering and interface methods-
    */
  public int rosterIndex(Person p) {
    return roster.indexOf(p);
  }
  
  
  public Person atRosterIndex(int i) {
    return roster.atIndex(i);
  }
  
  
  public String name() {
    return name;
  }
  
  
  public String toString() {
    return name;
  }
}


















