

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
  
  
  Person leader = null;
  List <Person> roster = new List();
  List <Kind> goonTypes = new List();
  boolean criminal;
  
  final public BaseFinance  finance  = new BaseFinance (this);
  final public BaseStocks   stocks   = new BaseStocks  (this);
  final public BaseTraining training = new BaseTraining(this);
  final public BaseLeads    leads    = new BaseLeads   (this);
  final public BasePlans    plans    = new BasePlans   (this);
  
  Place rooms[] = new Place[MAX_FACILITIES];
  List <Object> knownTech = new List();
  
  
  public Base(PlaceType kind, World world, boolean criminal) {
    super(kind, 0, world);
    this.criminal = criminal;
  }
  
  
  public Base(Session s) throws Exception {
    super(s);
    
    leader = (Person) s.loadObject();
    s.loadObjects(roster);
    s.loadObjects(goonTypes);
    criminal = s.loadBool();
    
    finance .loadState(s);
    stocks  .loadState(s);
    training.loadState(s);
    leads   .loadState(s);
    plans   .loadState(s);
    
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      rooms[n] = (Place) s.loadObject();
    }
    s.loadObjects(knownTech);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    
    s.saveObject(leader);
    s.saveObjects(roster);
    s.saveObjects(goonTypes);
    s.saveBool(criminal);
    
    finance .saveState(s);
    stocks  .saveState(s);
    training.saveState(s);
    leads   .saveState(s);
    plans   .saveState(s);
    
    for (int n = 0 ; n < MAX_FACILITIES; n++) {
      s.saveObject(rooms[n]);
    }
    s.saveObjects(knownTech);
  }
  
  
  
  /**  General stat queries-
    */
  public World world() { return world; }
  
  public boolean criminal() { return criminal; }
  
  
  
  /**  Regular updates and life-cycle methods:
    */
  void updateBase() {
    for (Person p : roster) {
      p.updateOnBase();
    }
    for (Place r : rooms) if (r != null) {
      r.updatePlace();
    }
    finance.updateFinance();
    stocks.updateCrafting();
    training.updateTraining();
    leads.updateLeads();
    plans.updatePlanning();
  }
  
  
  
  /**  Roster modification-
    */
  public Person addToRoster(Person member) {
    roster.add(member);
    member.setBase(this);
    return member;
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
  
  
  public Series <Kind> goonTypes() {
    return goonTypes;
  }
  
  
  public void setGoonTypes(Kind... types) {
    for (Kind type: types) goonTypes.add(type);
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
    return roster.indexOf(p);
  }
  
  
  public Person atRosterIndex(int i) {
    return roster.atIndex(i);
  }
  
  
  public String toString() {
    return "Base ("+leader+")";
  }
}


















