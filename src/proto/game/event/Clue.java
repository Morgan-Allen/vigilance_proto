

package proto.game.event;
import proto.common.*;
import proto.game.person.Person;
import proto.game.world.*;


//  TODO:  Have this extend Element as originally intended...

public class Clue {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Plot plot;
  Plot.Role role;

  Element match;
  boolean confirmed;
  
  Trait trait;
  
  Region near;
  int nearRange;
  
  Lead.Type leadType;
  int resultHeat;
  float confidence;
  int timeFound;
  
  
  Clue() {
    return;
  }
  
  
  public Clue(Plot plot, Plot.Role role) {
    this.plot = plot;
    this.role = role;
  }
  
  
  public static Clue loadClue(Session s) throws Exception {
    Clue c = new Clue();
    c.plot       = (Plot) s.loadObject();
    c.role       = (Plot.Role) s.loadObject();
    c.match      = (Element) s.loadObject();
    c.confirmed  = s.loadBool();
    c.trait      = (Trait) s.loadObject();
    c.near       = (Region) s.loadObject();
    c.nearRange  = s.loadInt();
    c.leadType   = Lead.LEAD_TYPES[s.loadInt()];
    c.resultHeat = s.loadInt();
    c.confidence = s.loadFloat();
    c.timeFound  = s.loadInt();
    return c;
  }
  
  
  public void saveClue(Session s) throws Exception {
    s.saveObject(plot       );
    s.saveObject(role       );
    s.saveObject(match      );
    s.saveBool  (confirmed  );
    s.saveObject(trait      );
    s.saveObject(near       );
    s.saveInt   (nearRange  );
    s.saveInt   (leadType.ID);
    s.saveInt   (resultHeat );
    s.saveFloat (confidence );
    s.saveInt   (timeFound  );
  }
  
  
  
  /**  Assignment of evidence & matches-
    */
  public void assignEvidence(
    Element match, Trait trait,
    Lead.Type leadType, float confidence, int timeFound
  ) {
    this.match      = match     ;
    this.trait      = trait     ;
    this.leadType   = leadType  ;
    this.confidence = confidence;
    this.timeFound  = timeFound ;
  }
  
  
  public void assignNearbyRegion(
    Element match, Region near, int range,
    Lead.Type leadType, float confidence, int timeFound
  ) {
    this.match      = match     ;
    this.near       = near      ;
    this.nearRange  = range     ;
    this.leadType   = leadType  ;
    this.confidence = confidence;
    this.timeFound  = timeFound ;
  }
  
  
  public void confirmMatch(
    Element match, Lead.Type leadType, int timeFound
  ) {
    this.match      = match    ;
    this.confirmed  = true     ;
    this.leadType   = leadType ;
    this.confidence = 1.0f     ;
    this.timeFound  = timeFound;
  }
  
  
  public boolean matchesType(Clue other) {
    if (plot  != other.plot ) return false;
    if (role  != other.role ) return false;
    if (trait != other.trait) return false;
    if (match != other.match) return false;
    return true;
  }
  
  
  
  /**  Evaluation of possible suspects-
    */
  protected boolean matchesSuspect(Element e) {
    if (match != null && confirmed && match != e) {
      return false;
    }
    if (trait != null) {
      if (e.isPerson()) {
        Person p = (Person) e;
        if (p.stats.levelFor(trait) <= 0) return false;
      }
      if (e.isPlace()) {
        Place p = (Place) e;
        if (! p.hasProperty(trait)) return false;
      }
    }
    if (near != null) {
      if (e.world().distanceBetween(near, e.region()) > nearRange) {
        return false;
      }
    }
    return true;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    if (match != null && confirmed) return
      leadType.name+" indicates "+
      role+" is: "+match
    ;
    if (trait != null) return
      leadType.name+" indicates "+
      role+" has trait: "+trait
    ;
    if (near != null && nearRange == 0) return
      leadType.name+" indicates "+
      role+" is within: "+near
    ;
    if (near != null && nearRange > 0) return
      leadType.name+" indicates "+
      role+" is near: "+near
    ;
    return "";
  }
}



