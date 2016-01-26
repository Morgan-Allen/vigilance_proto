

package proto;
import util.*;



public class Base implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public static int
    MAX_FACILITIES = 12,
    SLOTS_WIDE     = 3 ;
  
  World world;
  List <Person> roster = new List();
  Room rooms[] = new Room[MAX_FACILITIES];
  
  int currentFunds, income, maintenance;
  int powerUse, maxPower, supportUse, maxSupport;
  
  
  Base(World world) {
    this.world = world;
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      addFacility(Blueprint.NONE, n, 1);
    }
  }
  
  
  public Base(Session s) throws Exception {
    s.cacheInstance(this);
    world = (World) s.loadObject();
    s.loadObjects(roster);
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      rooms[n] = (Room) s.loadObject();
    }
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
  
  
  
  /**  Regular updates and life-cycle methods:
    */
  void updateBase(float numWeeks) {
    
    this.income      = 0;
    this.maintenance = 0;
    this.maxPower    = 0;
    this.powerUse    = 0;
    this.maxSupport  = 0;
    this.supportUse  = 0;
    
    for (Nation n : world.nations) if (n.member) {
      this.income += n.funding;
    }
    for (Person p : roster) {
      if (p.breathing()) supportUse += 1;
      p.updateOnBase(numWeeks);
    }
    for (Room r : rooms) {
      r.blueprint.updateForBase(this, r);
      for (Person p : r.visitors) r.blueprint.affectVisitor(p, this);
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
  
  
  public int engineerForce() {
    return 3;
  }
  
  
  public int researchForce() {
    int force = 3;
    for (Room r : rooms) {
      if (r.buildProgress < 1) continue;
      float skillBonus = 0;
      for (Person p : r.visitors) {
        skillBonus += p.levelFor(Person.BRAIN) * 2;
      }
      skillBonus /= 10;
      force += r.blueprint.studyBonus * (1 + skillBonus);
    }
    return force;
  }
  
  
  public float sensorChance() {
    float chance = 0;
    for (Room r : rooms) {
      if (r.buildProgress < 1) continue;
      float skillBonus = 0;
      for (Person p : r.visitors) {
        skillBonus += p.levelFor(Person.BRAIN);
        skillBonus += p.levelFor(Person.SIGHT);
      }
      skillBonus /= 100;
      chance += r.blueprint.sensorBonus * (1 + skillBonus);
    }
    return Nums.clamp(chance / 100, 0, 1);
  }
  
  
  
  /**  Construction and salvage-
    */
  public boolean canConstruct(Blueprint print, int slot) {
    if (rooms[slot].blueprint != Blueprint.NONE) return false;
    if (print.buildCost > currentFunds) return false;
    return true;
  }
  
  
  public float buildRate(Blueprint print) {
    float rate = 1f, numBuilding = 0;
    for (Room r : rooms) if (r.buildProgress < 1) numBuilding++;
    if (numBuilding == 0) return 1;
    rate *= engineerForce();
    return rate / (numBuilding * print.buildTime);
  }
  
  
  public int buildETA(int slot) {
    Room room = rooms[slot];
    return Nums.ceil((1 - room.buildProgress) / buildRate(room.blueprint));
  }
  
  
  public void addFacility(Blueprint print, int slot, float progress) {
    Room room = rooms[slot];
    if (room == null) room = rooms[slot] = new Room(this, slot);
    room.blueprint     = print;
    room.buildProgress = progress;
  }
  
  
  public void beginConstruction(Blueprint print, int slot) {
    currentFunds -= print.buildCost;
    addFacility(print, slot, 0);
  }
  
  
  public void beginSalvage(int slot) {
    currentFunds += rooms[slot].blueprint.buildCost / 2f;
    addFacility(Blueprint.NONE, slot, 1);
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


















