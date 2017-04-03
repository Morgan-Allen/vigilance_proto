

package proto.game.event;
import proto.game.world.*;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;



public class Trial extends Event {
  
  
  final public static EventType TYPE_TRIAL = new EventType(
    "Trial", "event_trial", null
  ) {};
  
  
  Base conducts;
  Place venue;
  Plot plot;
  List <Person> accused = new List();
  
  
  public Trial(World world, Base conducts) {
    super(TYPE_TRIAL, world);
    this.conducts = conducts;
  }
  
  
  public Trial(Session s) throws Exception {
    super(s);
    conducts = (Base ) s.loadObject();
    venue    = (Place) s.loadObject();
    plot     = (Plot ) s.loadObject();
    s.loadObjects(accused);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(conducts);
    s.saveObject(venue);
    s.saveObject(plot);
    s.saveObjects(accused);
  }
  
  
  
  
  public void assignAccused(Plot plot, Series <Person> accused) {
    this.plot = plot;
    Visit.appendTo(this.accused, accused);
  }
  
  
  public void beginEvent() {
    super.beginEvent();
    
    for (Person p : accused) {
      float evidence = rateEvidence(p);
      if (Rand.num() < evidence) {
        world.council.applySentence(p, 1.0f);
      }
      else {
        world.council.releasePrisoner(p);
      }
    }
  }
  
  
  
  /**  General query methods-
    */
  public Element targetElement(Person p) {
    return venue;
  }
  
  
  public Plot plot() {
    return plot;
  }
  
  
  public Series <Person> accused() {
    return accused;
  }
  
  
  public float rateEvidence(Person p) {
    float sumEvidence = 0;
    for (Plot plot : conducts.leads.involvedIn(p, true)) {
      sumEvidence += conducts.leads.evidenceForInvolvement(plot, p);
    }
    return 1 - (1f / (1 + sumEvidence));
  }
  
  
  public float rateEvidence() {
    float sum = 0;
    for (Person p : accused) {
      sum += rateEvidence(p);
    }
    return sum / Nums.max(1, accused.size());
  }
  
  
  public boolean involves(Plot plot, Person accused) {
    if (plot != null && plot != this.plot) return false;
    if (accused != null && ! this.accused.includes(accused)) return false;
    return true;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return "Trial for "+plot.organiser();
  }
}















