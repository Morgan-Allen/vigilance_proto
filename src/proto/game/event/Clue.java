

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.Image;



//  TODO:  Have this extend Element as originally intended?

public class Clue implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Plot plot;
  final public Plot.Role role;

  Element match;
  boolean confirmed;
  
  Trait trait;
  
  Region near;
  int nearRange;
  
  Lead.Type leadType;
  Lead source;
  float confidence;
  Place placeFound;
  int timeFound;
  
  
  
  public Clue(Plot plot, Plot.Role role) {
    this.plot = plot;
    this.role = role;
  }
  
  
  public Clue(Session s) throws Exception {
    s.cacheInstance(this);
    plot       = (Plot) s.loadObject();
    role       = (Plot.Role) s.loadObject();
    match      = (Element) s.loadObject();
    confirmed  = s.loadBool();
    trait      = (Trait) s.loadObject();
    near       = (Region) s.loadObject();
    nearRange  = s.loadInt();
    leadType   = Lead.LEAD_TYPES[s.loadInt()];
    source     = (Lead) s.loadObject();
    confidence = s.loadFloat();
    placeFound = (Place) s.loadObject();
    timeFound  = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
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
    s.saveObject(placeFound );
    s.saveInt   (timeFound  );
  }
  
  
  
  /**  Assignment of evidence & matches-
    */
  public void assignEvidence(
    Element match, Trait trait,
    Lead source, float confidence, int time, Place place
  ) {
    this.match      = match      ;
    this.trait      = trait      ;
    this.source     = source     ;
    this.leadType   = source.type;
    this.confidence = confidence ;
    this.timeFound  = time       ;
    this.placeFound = place      ;
  }
  
  
  public void assignNearbyRegion(
    Element match, Region near, int range,
    Lead source, float confidence, int time, Place place
  ) {
    this.match      = match      ;
    this.near       = near       ;
    this.nearRange  = range      ;
    this.source     = source     ;
    this.leadType   = source.type;
    this.confidence = confidence ;
    this.timeFound  = time       ;
    this.placeFound = place      ;
  }
  
  
  public void confirmMatch(
    Element match, Lead source, int time, Place place
  ) {
    this.match      = match      ;
    this.confirmed  = true       ;
    this.source     = source     ;
    this.leadType   = source.type;
    this.confidence = 1.0f       ;
    this.timeFound  = time       ;
    this.placeFound = place      ;
  }
  
  
  public void confirmTipoff(
    Element match, Lead.Type type, int time, Place place
  ) {
    this.match      = match;
    this.confirmed  = true ;
    this.leadType   = type ;
    this.timeFound  = time ;
    this.placeFound = place;
    this.confidence = type.confidence;
  }
  
  
  public boolean matchesType(Clue other) {
    if (plot  != other.plot ) return false;
    if (role  != other.role ) return false;
    if (trait != other.trait) return false;
    if (match != other.match) return false;
    return true;
  }
  
  
  
  /**  Other basic access methods-
    */
  public Lead.Type leadType() {
    return leadType;
  }
  
  
  public boolean isReport() {
    return leadType == Lead.LEAD_REPORT;
  }
  
  
  public boolean isTipoff() {
    return leadType == Lead.LEAD_TIPOFF;
  }
  
  
  public boolean isEvidence() {
    return ! (isReport() || isTipoff());
  }
  
  
  public int time() {
    return timeFound;
  }
  
  
  public Place place() {
    return placeFound;
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
  
  
  public Image icon() {
    if (source == null) return null;
    return source.icon();
  }
  
}



