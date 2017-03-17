

package proto.game.event;
import proto.common.*;
import proto.game.person.Person;
import proto.game.world.*;
import proto.util.*;



//  TODO:  Have this extend Element as originally intended?

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
  Lead source;
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
    c.source     = (Lead) s.loadObject();
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
    s.saveObject(source     );
    s.saveFloat (confidence );
    s.saveInt   (timeFound  );
  }
  
  
  
  /**  Assignment of evidence & matches-
    */
  public void assignEvidence(
    Element match, Trait trait,
    Lead source, float confidence, int timeFound
  ) {
    this.match      = match      ;
    this.trait      = trait      ;
    this.source     = source     ;
    this.leadType   = source.type;
    this.confidence = confidence ;
    this.timeFound  = timeFound  ;
  }
  
  
  public void assignNearbyRegion(
    Element match, Region near, int range,
    Lead source, float confidence, int timeFound
  ) {
    this.match      = match      ;
    this.near       = near       ;
    this.nearRange  = range      ;
    this.source     = source     ;
    this.leadType   = source.type;
    this.confidence = confidence ;
    this.timeFound  = timeFound  ;
  }
  
  
  public void confirmMatch(
    Element match, Lead source, int timeFound
  ) {
    this.match      = match      ;
    this.confirmed  = true       ;
    this.source     = source     ;
    this.leadType   = source.type;
    this.confidence = 1.0f       ;
    this.timeFound  = timeFound  ;
  }
  
  
  public void confirmTipoff(
    Element match, Lead.Type type, float confidence, int timeFound
  ) {
    this.match      = match      ;
    this.confirmed  = true       ;
    this.leadType   = type       ;
    this.confidence = confidence ;
    this.timeFound  = timeFound  ;
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
    return longDescription();
  }
  
  
  public String longDescription() {
    StringBuffer desc = new StringBuffer(leadType.name);
    World world = plot.world();
    
    if (source != null) {
      desc.append(" by ");
      Series <Person> did = source.onceAssigned();
      for (Person p : did) {
        if (p == did.first()) desc.append(""+p);
        else if (p == did.last()) desc.append(" and "+p);
        else desc.append(", "+p);
      }
    }
    
    desc.append(" at "+world.timing.timeString(timeFound));
    desc.append(" indicates the "+role);
    
    if (match != null && confirmed    ) desc.append(" is: "+match);
    if (trait != null                 ) desc.append(" has trait: "+trait);
    if (near != null && nearRange == 0) desc.append(" is within: "+near);
    if (near != null && nearRange > 0 ) desc.append(" is near: "+near);
    
    return desc.toString();
  }
  
  
  public String traitDescription() {
    if (trait != null) return ""+trait;
    if (near != null) {
      if (nearRange == 0) return "in "+near;
      else                return "near "+near;
    }
    return "";
  }
  
}



