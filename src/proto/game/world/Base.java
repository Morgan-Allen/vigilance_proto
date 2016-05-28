

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
  
  List <Person> roster = new List();
  final public BaseStocks stocks = new BaseStocks(this);
  
  Room rooms[] = new Room[MAX_FACILITIES];
  List <Tech> knownTech = new List();
  
  int currentFunds, income, maintenance;
  int powerUse, maxPower, supportUse, maxSupport;
  
  
  Base(World world) {
    this.world = world;
  }
  
  
  public Base(Session s) throws Exception {
    s.cacheInstance(this);
    world = (World) s.loadObject();
    s.loadObjects(roster);
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      rooms[n] = (Room) s.loadObject();
    }
    s.loadObjects(knownTech);
    stocks.loadState(s);
    
    currentFunds = s.loadInt();
    income       = s.loadInt();
    maintenance  = s.loadInt();
    powerUse     = s.loadInt();
    maxPower     = s.loadInt();
    supportUse   = s.loadInt();
    maxSupport   = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(world);
    s.saveObjects(roster);
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      s.saveObject(rooms[n]);
    }
    s.saveObjects(knownTech);
    stocks.saveState(s);
    
    s.saveInt(currentFunds);
    s.saveInt(income      );
    s.saveInt(maintenance );
    s.saveInt(powerUse    );
    s.saveInt(maxPower    );
    s.saveInt(supportUse  );
    s.saveInt(maxSupport  );
  }
  
  
  
  /**  General stat queries-
    */
  public int currentFunds() { return currentFunds; }
  public int income() { return income; }
  public int maintenance() { return maintenance; }
  public int powerUse() { return powerUse; }
  public int maxPower() { return maxPower; }
  public int supportUse() { return supportUse; }
  public int maxSupport() { return maxSupport; }
  
  public World world() { return world; }
  
  
  
  /**  Regular updates and life-cycle methods:
    */
  void updateBase(float numWeeks) {
    
    this.income      = 0;
    this.maintenance = 0;
    this.maxPower    = 0;
    this.powerUse    = 0;
    this.maxSupport  = 0;
    this.supportUse  = 0;
    
    //  TODO:  Introduce investment projects for this!
    this.income = 10;
    /*
    for (Nation n : world.nations) if (n.member) {
      this.income += n.funding;
    }
    //*/
    
    for (Person p : roster) {
      if (p.breathing()) supportUse += 1;
      p.updateOnBase(numWeeks);
    }
    for (Room r : rooms) if (r != null) {
      r.updateRoom();
    }
    
    this.currentFunds += (income - maintenance) * numWeeks;
  }
  
  
  
  /**  Roster modification-
    */
  public void addToRoster(Person hero) {
    roster.add(hero);
  }
  
  
  public Series <Person> roster() {
    return roster;
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
  
  
  
  
  /**  Construction and salvage-
    */
  public boolean canConstruct(Blueprint print, int slot) {
    if (rooms[slot] != null) return false;
    if (print.buildCost > currentFunds) return false;
    return true;
  }
  
  
  public float buildRate(Blueprint print) {
    float rate = 1f, numBuilding = 0;
    for (Room r : rooms) if (r.buildProgress < 1) numBuilding++;
    if (numBuilding == 0) return 1;
    return rate / (numBuilding * print.buildTime);
  }
  
  
  public int buildETA(int slot) {
    Room room = rooms[slot];
    return Nums.ceil((1 - room.buildProgress) / buildRate(room.blueprint));
  }
  
  
  public void addFacility(Blueprint print, int slot, float progress) {
    if (print == null) { rooms[slot] = null; return; }
    Room room = rooms[slot];
    if (room == null) room = rooms[slot] = print.createRoom(this, slot);
    room.buildProgress = progress;
  }
  
  
  public void beginConstruction(Blueprint print, int slot) {
    currentFunds -= print.buildCost;
    addFacility(print, slot, 0);
  }
  
  
  public void beginSalvage(int slot) {
    currentFunds += rooms[slot].blueprint.buildCost / 2f;
    addFacility(null, slot, 1);
  }
  
  
  public Room[] rooms() {
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
}


















