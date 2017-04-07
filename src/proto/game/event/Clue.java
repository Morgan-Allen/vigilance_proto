

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
  
  Lead.Type leadType;
  Lead source;
  float confidence;
  Place placeFound;
  int timeFound;
  
  //
  //  TODO:  Split off these various information types into separate classes of
  //  Clue?
  Element match;
  boolean confirmed;
  
  Trait trait;
  
  Element location;
  int nearRange;
  
  int heistTime;
  PlotType heistType;
  
  
  
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
    location   = (Element) s.loadObject();
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
    s.saveObject(location   );
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
    Lead source, int time, Place place
  ) {
    confirmDetails(source.type, time, place);
    this.match  = match ;
    this.trait  = trait ;
    this.source = source;
  }
  
  
  public void assignNearbyRegion(
    Element match, Element near, int range,
    Lead source, int time, Place place
  ) {
    confirmDetails(source.type, time, place);
    this.match     = match ;
    this.location  = near  ;
    this.nearRange = range ;
    this.source    = source;
  }
  
  
  public void confirmMatch(
    Element match, Lead source, int time, Place place
  ) {
    confirmDetails(source.type, time, place);
    this.match      = match ;
    this.confirmed  = true  ;
    this.source     = source;
    this.confidence = 1.0f  ;
  }
  
  
  public void confirmHeistDetails(
    PlotType heistType, int heistTime, Lead source, int time, Place place
  ) {
    confirmDetails(source.type, time, place);
    this.heistType = heistType;
    this.heistTime = heistTime;
    this.source    = source   ;
  }
  
  
  public void confirmTipoff(
    Element match, Lead.Type type, int time, Place place
  ) {
    confirmDetails(type, time, place);
    this.match      = match;
    this.confirmed  = true ;
  }
  
  
  private void confirmDetails(Lead.Type type, int time, Place place) {
    this.leadType   = type ;
    this.timeFound  = time ;
    this.placeFound = place;
    this.confidence = type.confidence;
  }
  
  
  public boolean makesRedundant(Clue other) {
    if (plot != other.plot) return false;
    if (role != other.role) return false;
    
    if (isConfirmation()) {
      return true;
    }
    
    if (isTraitClue() && other.isTraitClue()) {
      if (trait != other.trait) return false;
      return true;
    }
    
    if (isLocationClue() && other.isLocationClue()) {
      if (location != other.location) return false;
      if (nearRange > other.nearRange) return false;
      return true;
    }
    
    if (isObjectiveClue() && other.isObjectiveClue()) {
      if (heistType != other.heistType) return false;
      if (heistTime != other.heistTime) return false;
      return true;
    }
    
    return false;
  }
  
  
  
  /**  Other basic access methods-
    */
  public Lead.Type leadType() {
    return leadType;
  }
  
  
  public boolean isConfirmation() {
    return match != null && confirmed;
  }
  
  
  public boolean isTraitClue() {
    return trait != null;
  }
  
  
  public boolean isLocationClue() {
    return location != null;
  }
  
  
  public boolean isObjectiveClue() {
    return heistType != null;
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
    if (location != null) {
      Region near = location.region();
      float dist = e.world().distanceBetween(near, e.region());
      
      if (location.isPlace() && nearRange == 0) {
        if (e.place() != location) return false;
      }
      else if (dist > nearRange) {
        return false;
      }
    }
    return true;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return traitDescription();
  }
  
  
  public String longDescription(Base base) {
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
    desc.append(" indicates that "+plot.nameForCase(base)+"'s "+role);
    
    if (match != null && confirmed        ) desc.append(" is: "+match);
    if (trait != null                     ) desc.append(" has trait: "+trait);
    if (location != null && nearRange == 0) desc.append(" inisde:  "+location);
    if (location != null && nearRange > 0 ) desc.append(" is near: "+location);
    
    return desc.toString();
  }
  
  
  public String traitDescription() {
    if (trait != null) {
      return ""+trait;
    }
    if (location != null) {
      if (nearRange == 0) return "in "+location;
      else                return "near "+location;
    }
    return "";
  }
  
  
  public Image icon() {
    if (source == null) return null;
    return source.icon();
  }
  
}



