

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import java.awt.Image;



public class Clue implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public static int
    TYPE_AIM      = 0,
    TYPE_MATCH    = 1,
    TYPE_TRAIT    = 2,
    TYPE_LOCATION = 3;

  int   clueType ;
  Plot  plot     ;
  Role  role     ;
  Step  step     ;
  float getChance;
  
  Element match     = null;
  Trait   trait     = null;
  Element location  = null;
  int     nearRange = -1  ;
  
  Lead.Type leadType  ;
  Lead      source    ;
  Place     placeFound;
  int       timeFound ;
  
  
  
  public Clue(Session s) throws Exception {
    s.cacheInstance(this);
    
    clueType   = s.loadInt();
    plot       = (Plot) s.loadObject();
    role       = (Role) s.loadObject();
    step       = (Step) s.loadObject();
    getChance  = s.loadFloat();
    
    match      = (Element) s.loadObject();
    trait      = (Trait  ) s.loadObject();
    location   = (Element) s.loadObject();
    nearRange  = s.loadInt();
    
    leadType   = Lead.LEAD_TYPES[s.loadInt()];
    source     = (Lead ) s.loadObject();
    placeFound = (Place) s.loadObject();
    timeFound  = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    
    s.saveInt   (clueType   );
    s.saveObject(plot       );
    s.saveObject(role       );
    s.saveObject(step       );
    s.saveFloat (getChance  );
    
    s.saveObject(match      );
    s.saveObject(trait      );
    s.saveObject(location   );
    s.saveInt   (nearRange  );
    
    s.saveInt   (leadType.ID);
    s.saveObject(source     );
    s.saveObject(placeFound );
    s.saveInt   (timeFound  );
  }
  
  
  
  /**  Basic access methods-
    */
  public boolean isAim         () { return clueType == TYPE_AIM        ; }
  public boolean isConfirmation() { return clueType == TYPE_MATCH      ; }
  public boolean isTraitClue   () { return clueType == TYPE_TRAIT      ; }
  public boolean isLocationClue() { return clueType == TYPE_LOCATION   ; }
  public boolean isReport      () { return leadType == Lead.LEAD_REPORT; }
  public boolean isTipoff      () { return leadType == Lead.LEAD_TIPOFF; }
  public boolean isEvidence    () { return ! (isReport() || isTipoff()); }
  
  public Lead.Type leadType () { return leadType ; }
  public Lead      source   () { return source   ; }
  public int       clueType () { return clueType ; }
  public Plot      plot     () { return plot     ; }
  public Role      role     () { return role     ; }
  public Step      step     () { return step     ; }
  public float     getChance() { return getChance; }
  
  public int     time        () { return timeFound ; }
  public Place   found       () { return placeFound; }
  public Element match       () { return match     ; }
  public Trait   trait       () { return trait     ; }
  public Element locationNear() { return location  ; }
  public int     nearRange   () { return nearRange ; }
  
  
  
  /**  Public factory and verification methods for convenience-
    */
  private Clue() {}
  
  
  public static Clue locationClue(
    Plot plot, Role role, Step step,
    Element location, int nearRange
  ) {
    Clue c = new Clue();
    c.plot      = plot;
    c.role      = role;
    c.step      = step;
    c.clueType  = TYPE_LOCATION;
    c.location  = location;
    c.nearRange = nearRange;
    c.getChance = 1;
    return c;
  }
  
  
  public static Clue traitClue(
    Plot plot, Role role, Step step,
    Trait trait
  ) {
    Clue c = new Clue();
    c.plot      = plot;
    c.role      = role;
    c.step      = step;
    c.clueType  = TYPE_TRAIT;
    c.trait     = trait;
    c.getChance = 1;
    return c;
  }
  
  
  public static Clue confirmSuspect(
    Plot plot, Role role, Step step,
    Element match, Place at
  ) {
    Clue c = new Clue();
    c.plot      = plot;
    c.role      = role;
    c.step      = step;
    c.clueType  = TYPE_MATCH;
    c.match     = match;
    c.location  = at;
    c.nearRange = at != null ? 0 : -1;
    c.getChance = 1;
    return c;
  }
  
  
  public static Clue confirmSuspect(
    Plot plot, Role role, Step step, Element match
  ) {
    return confirmSuspect(plot, role, step, match, null);
  }
  
  
  public static Clue confirmAim(Plot plot) {
    Clue c = new Clue();
    c.plot      = plot;
    c.role      = Plot.ROLE_OBJECTIVE;
    c.step      = plot.currentStep();
    c.clueType  = TYPE_AIM;
    c.getChance = 1;
    return c;
  }
  
  
  
  /**  Assigning get-chance, confirming sources and checking redundancy-
    */
  public void setGetChance(float chance) {
    this.getChance = chance;
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
      if (match.isPlace() && other.isLocationClue()) return true;
      if (other.isTraitClue()) return true;
      if (location == other.location && match == other.match) return true;
    }
    
    if (clueType != other.clueType) {
      return false;
    }
    
    if (isAim()) {
      return true;
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
      if (! e.isPlace()) return false;
      
      Region near = location.region();
      float dist = e.world().distanceBetween(near, e.region());
      
      if (location.isPlace() && nearRange == 0) {
        if (e != location) return false;
      }
      else if (dist > nearRange) {
        return false;
      }
    }
    
    return true;
  }
  
  
  protected Place locationGiven() {
    if (location != null && location.isPlace() && nearRange == 0) {
      return (Place) location;
    }
    return null;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    if (isConfirmation()) {
      if (match.isPerson()) return "is "+match+" at "+location;
      else return "is "+match;
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



