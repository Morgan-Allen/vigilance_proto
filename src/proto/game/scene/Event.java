

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import java.awt.Image;



public class Event implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static Image
    //  TODO:  You will need more of these...
    IMG_LEAD = Kind.loadImage(
      "media assets/scene backgrounds/crime_generic_1.png"
    );
  
  
  final String name, info;
  Batch <String> actionLog = new Batch();
  
  final World world;
  float timeBegins, timeEnds;
  List <Lead> leads = new List();
  
  List <Object> known = new List();
  boolean closed, solved;
  
  
  protected Event(String name, String info, World world) {
    this.name  = name ;
    this.info  = info ;
    this.world = world;
  }
  
  
  public Event(Session s) throws Exception {
    s.cacheInstance(this);
    
    name = s.loadString();
    info = s.loadString();
    
    world      = (World) s.loadObject();
    timeBegins = s.loadFloat();
    timeEnds   = s.loadFloat();
    s.loadObjects(leads);
    s.loadObjects(known);
    closed = s.loadBool();
    solved = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    
    s.saveString(name);
    s.saveString(info);
    //  NOTE:  The action log is wiped after use, so we don't save it.
    
    s.saveObject(world);
    s.saveFloat(timeBegins);
    s.saveFloat(timeEnds  );
    s.saveObjects(leads);
    s.saveObjects(known);
    s.saveBool(closed);
    s.saveBool(solved);
  }
  
  
  
  /**  Supplemental setup/progression methods-
    */
  public void assignDates(float begins, float ends) {
    this.timeBegins = begins;
    this.timeEnds   = ends  ;
  }
  
  
  public World world() {
    return world;
  }
  
  
  public float timeBegins() {
    return timeBegins;
  }
  
  
  public float timeEnds() {
    return timeEnds;
  }
  
  
  protected void assignLeads(Lead... leads) {
    for (Lead l : leads) this.leads.add(l);
  }
  
  
  protected void setKnown(Object... known) {
    Visit.appendTo(this.known, known);
  }
  
  
  protected Lead leadWithID(int ID) {
    for (Lead l : leads) if (l.ID == ID) return l;
    return null;
  }
  
  
  protected boolean checkFollowed(Lead lead, boolean success) {
    if (success) known.include(lead.reveals);
    return true;
  }
  
  
  protected void setComplete(boolean solved) {
    this.closed = true;
    this.solved = solved;
  }
  
  
  public void updateEvent() {
    for (Lead lead : leads) lead.updateAssignment();
  }
  
  
  public boolean complete() {
    return closed;
  }
  
  
  
  /**  Lead-compilation for general assessment and UI purposes-
    */
  public Series <Lead> knownLeads() {
    final Batch <Lead> matches = new Batch();
    for (Lead l : leads) if (known.includes(l.origin)) matches.add(l);
    return matches;
  }
  
  
  public Series <Object> knownObjects() {
    return known;
  }
  
  
  public Series <Lead> openLeadsFrom(Object origin) {
    final Batch <Lead> from = new Batch();
    for (Lead l : leads) if (l.origin == origin && l.open()) from.add(l);
    return from;
  }
  
  
  public Series <Lead> openLeadsFrom(Region region) {
    boolean linksToRegion = false;
    final Batch <Lead> from = new Batch();
    
    for (Lead l : leads) if (l.origin instanceof Scene) {
      final Scene s = (Scene) l.origin;
      if (s.region == region && known.includes(s)) linksToRegion = true;
    }
    if (! linksToRegion) return from;
    
    for (Lead l : leads) if (l.open()) {
      from.add(l);
    }
    return from;
  }
  
  
  Region firstLocation() {
    for (Lead l : leads) if (l.origin instanceof Scene) {
      final Scene s = (Scene) l.origin;
      return s.region;
    }
    return null;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void logAction(String action) {
    actionLog.add(action);
  }
  
  
  protected void wipeActionLog() {
    actionLog.clear();
  }
  
  
  public Series <String> actionLog() {
    return actionLog;
  }
  
  
  public String name() {
    return name;
  }
  
  
  public String info() {
    return info;
  }
  
  
  public Image imageFor(Lead lead) {
    return IMG_LEAD;
  }
  
  
  public String toString() {
    return name;
  }
}








