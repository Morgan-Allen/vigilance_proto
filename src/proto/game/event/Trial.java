

package proto.game.event;
import proto.game.world.*;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.base.*;



public class Trial extends Event {
  
  
  /**  Data fields, construction and save/load methods:
    */
  final public static EventType TYPE_TRIAL = new EventType(
    "Trial", "event_trial", null
  ) {};
  
  
  Base conducts;
  Place venue;
  Plot plot;
  Person defends, prosecutes;
  List <Person> accused = new List();
  
  
  public Trial(World world, Base conducts) {
    super(TYPE_TRIAL, world);
    this.conducts = conducts;
  }
  
  
  public Trial(Session s) throws Exception {
    super(s);
    conducts   = (Base  ) s.loadObject();
    venue      = (Place ) s.loadObject();
    plot       = (Plot  ) s.loadObject();
    defends    = (Person) s.loadObject();
    prosecutes = (Person) s.loadObject();
    s.loadObjects(accused);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(conducts  );
    s.saveObject(venue     );
    s.saveObject(plot      );
    s.saveObject(defends   );
    s.saveObject(prosecutes);
    s.saveObjects(accused);
  }
  
  
  
  /**  Initial setup and evidence-related queries:
    */
  public void assignParties(
    Plot plot, Person defends, Person prosecutes, Series <Person> accused
  ) {
    this.plot = plot;
    Visit.appendTo(this.accused, accused);
  }
  
  
  public void addToAccused(Person person) {
    accused.add(person);
  }
  
  
  public float rateEvidence(Person p) {
    float sumEvidence = 0;
    for (Plot plot : conducts.leads.involvedIn(p, true)) {
      sumEvidence += conducts.leads.evidenceAgainst(p, plot, true);
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
  

  public Element targetElement(Person p) {
    return venue;
  }
  
  
  public Plot plot() {
    return plot;
  }
  
  
  public Series <Person> accused() {
    return accused;
  }
  
  
  public Person defends() {
    return defends;
  }
  
  
  public Person prosecutes() {
    return prosecutes;
  }
  
  
  public boolean involves(Plot plot, Person accused) {
    if (plot != null && plot != this.plot) return false;
    if (accused != null && ! this.accused.includes(accused)) return false;
    return true;
  }
  
  
  
  /**  Regular updates and life-cycle:
    */
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
      
      boolean isBoss = p.base().leader() == p;
      if (isBoss) {
        p.history.incBond(prosecutes, -0.5f);
        p.history.incBond(defends   , 0.25f);
      }
    }
    
    MessageUtils.presentSentenceMessage(world.view(), this);
    completeEvent();
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return "Trial for "+plot.organiser();
  }
}















