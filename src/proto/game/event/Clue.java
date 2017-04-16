

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
  final public static int
    TYPE_MATCH    = 0,
    TYPE_TRAIT    = 1,
    TYPE_LOCATION = 2;
  
  Lead.Type leadType;
  Lead source;
  Place placeFound;
  int timeFound;
  
  Plot plot;
  Plot.Role role;
  int clueType;
  
  Element match;
  Trait trait;
  Element location;
  int nearRange;
  
  
  
  private Clue() {}
  
  
  public Clue(Session s) throws Exception {
    s.cacheInstance(this);
    
    leadType   = Lead.LEAD_TYPES[s.loadInt()];
    source     = (Lead) s.loadObject();
    placeFound = (Place) s.loadObject();
    timeFound  = s.loadInt();

    plot       = (Plot     ) s.loadObject();
    role       = (Plot.Role) s.loadObject();
    clueType   = s.loadInt();
    
    match      = (Element) s.loadObject();
    trait      = (Trait  ) s.loadObject();
    location   = (Element) s.loadObject();
    nearRange  = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    
    s.saveInt   (leadType.ID);
    s.saveObject(source     );
    s.saveObject(placeFound );
    s.saveInt   (timeFound  );
    
    s.saveObject(plot       );
    s.saveObject(role       );
    s.saveInt   (clueType   );
    
    s.saveObject(match      );
    s.saveObject(trait      );
    s.saveObject(location   );
    s.saveInt   (nearRange  );
  }
  
  
  
  /**  Assignment of evidence & matches-
    */
  public static Clue traitClue(
    Plot plot, Plot.Role role, Trait trait
  ) {
    Clue c = new Clue();
    c.plot     = plot;
    c.role     = role;
    c.clueType = TYPE_TRAIT;
    c.trait    = trait;
    return c;
  }
  
  
  public static Clue locationClue(
    Plot plot, Plot.Role role, Element location, int nearRange
  ) {
    Clue c = new Clue();
    c.plot      = plot;
    c.role      = role;
    c.clueType  = TYPE_LOCATION;
    c.location  = location;
    c.nearRange = nearRange;
    return c;
  }
  
  
  public static Clue confirmSuspect(
    Plot plot, Plot.Role role, Element match, Place location
  ) {
    Clue c = new Clue();
    c.plot      = plot;
    c.role      = role;
    c.clueType  = TYPE_MATCH;
    c.match     = match;
    c.location  = location;
    c.nearRange = 0;
    return c;
  }
  
  
  public void confirmSource(Lead source, int time, Place place) {
    this.source = source;
    confirmSource(source.type, time, place);
  }
  
  
  public void confirmSource(Lead.Type type, int time, Place place) {
    this.leadType   = type ;
    this.timeFound  = time ;
    this.placeFound = place;
  }
  
  
  public boolean makesRedundant(Clue other) {
    if (plot != other.plot) return false;
    if (role != other.role) return false;
    
    if (isConfirmation()) {
      if (location == other.location && match == other.match) return true;
    }
    
    if (clueType != other.clueType) {
      return false;
    }
    
    if (isTraitClue()) {
      if (trait != other.trait) return false;
      return true;
    }
    
    if (isLocationClue()) {
      if (location != other.location ) return false;
      if (nearRange > other.nearRange) return false;
      return true;
    }
    
    return false;
  }
  
  
  
  /**  Other basic access methods-
    */
  public Lead.Type leadType() {
    return leadType;
  }
  
  
  public int clueType() {
    return clueType;
  }
  
  
  public Plot plot() {
    return plot;
  }
  
  
  public Plot.Role role() {
    return role;
  }
  
  
  public boolean isConfirmation() {
    return clueType == TYPE_MATCH;
  }
  
  
  public boolean isTraitClue() {
    return clueType == TYPE_TRAIT;
  }
  
  
  public boolean isLocationClue() {
    return clueType == TYPE_LOCATION;
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
    
    if (isConfirmation() && match != e) {
      return false;
    }
    
    if (isTraitClue()) {
      if (e.isPerson()) {
        Person p = (Person) e;
        if (p.stats.levelFor(trait) <= 0) return false;
      }
      if (e.isPlace()) {
        Place p = (Place) e;
        if (! p.hasProperty(trait)) return false;
      }
    }
    
    if (isLocationClue()) {
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
  
  
  protected Place locationGiven() {
    return (
      (isLocationClue() || isConfirmation()) &&
      (nearRange == 0 && location.isPlace())
    ) ? (Place) location : null;
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
    
    if (isConfirmation()) {
      desc.append(" is "+match+" at "+location);
    }
    if (isTraitClue()) {
      desc.append(" has "+trait);
    }
    if (isLocationClue()) {
      if (nearRange == 0) desc.append(" is at "+location);
      else                desc.append(" is near "+location);
    }
    
    return desc.toString();
  }
  
  
  public String traitDescription() {
    if (isConfirmation()) {
      return "is "+match+" at "+location;
    }
    if (isTraitClue()) {
      return "has "+trait;
    }
    if (isLocationClue()) {
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



