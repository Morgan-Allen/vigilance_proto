

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import java.awt.Image;



public class Event implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public static int
    EVENT_MINOR  = 0,
    EVENT_NORMAL = 1,
    EVENT_MAJOR  = 2
  ;
  final static Image
    LEAD_IMAGES[] = Kind.loadImages(
      "media assets/scene backgrounds/",
      "crime_generic_1.png",
      "crime_generic_2.png",
      "crime_generic_3.png",
      "crime_generic_4.png"
    );
  
  
  final EventType type;
  PlanStep step;
  Place place;
  float timeBegins, timeEnds;
  boolean complete;
  
  
  protected Event(EventType type) {
    this.type = type;
  }
  
  
  public Event(Session s) throws Exception {
    s.cacheInstance(this);
    type       = (EventType) s.loadObject();
    step       = (PlanStep ) s.loadObject();
    place      = (Place    ) s.loadObject();
    timeBegins = s.loadFloat();
    timeEnds   = s.loadFloat();
    complete   = s.loadBool ();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(type      );
    s.saveObject(step      );
    s.saveObject(place     );
    s.saveFloat (timeBegins);
    s.saveFloat (timeEnds  );
    s.saveBool  (complete  );
  }
  
  
  
  /**  Supplemental setup/progression methods-
    */
  public void assignParameters(
    PlanStep step, Place place, float begins, float ends
  ) {
    this.step       = step  ;
    this.place      = place ;
    this.timeBegins = begins;
    this.timeEnds   = ends  ;
  }
  
  
  public World world() {
    return place.world();
  }
  
  
  public Place place() {
    return place;
  }
  
  
  public float timeBegins() {
    return timeBegins;
  }
  
  
  public float timeEnds() {
    return timeEnds;
  }
  

  public Series <Lead> knownOpenLeads() {
    final Batch <Lead> matches = new Batch();
    /*
    for (Lead l : leads) {
      if (l.open()) matches.add(l);
    }
    //*/
    return matches;
  }
  
  
  public void updateEvent() {
    /*
    for (Lead lead : leads) {
      lead.updateAssignment();
    }
    //*/
  }
  
  
  public boolean complete() {
    return complete;
  }
  
  
  /*
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
    if (success) for (Object r : lead.goes) known.include(r);
    if (complete()) world.events().closeEvent(this);
    return true;
  }
  
  
  protected void setComplete(boolean solved) {
    this.closed = true;
    this.solved = solved;
  }
  
  
  
  public Series <Lead> knownOpenLeads() {
    final Batch <Lead> matches = new Batch();
    for (Lead l : leads) {
      if (l.open()) matches.add(l);
    }
    return matches;
  }
  
  
  public Series <Object> knownObjects() {
    return known;
  }
  
  
  public Series <Lead> openLeadsFrom(Lead lead) {
    final Batch <Lead> from = new Batch();
    for (Lead l : leads) {
      boolean match = false;
      for (Object o : lead.goes) if (o == l.origin) match = true;
      if (match) from.add(l);
    }
    return from;
  }
  //*/
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String name() {
    return type.nameFor(this);
  }
  
  
  public String info() {
    return type.infoFor(this);
  }
  
  
  public Image imageFor(Lead lead) {
    return LEAD_IMAGES[lead.ID % LEAD_IMAGES.length];
  }
  
  
  public String toString() {
    return name();
  }
}









