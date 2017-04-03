

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.util.*;

//
//  TODO:  Make Trials into a kind of Event themselves, so they can be
//  disrupted by crooks.
//
//  TODO:  Assign officials to preside over/prosecute/defend during a trial, so
//  they can earn grudges from crime bosses.
//
//  TODO:  Construct a report for trials being scheduled or sentence passed!


public class Council {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final World world;
  Base mainHall;
  List <Place> courts  = new List();
  List <Place> prisons = new List();
  
  public static class Trial {
    Plot plot;
    List <Person> accused = new List();
    int timeStarts, timeEnds;
    
    public String toString() {
      return "Trial for "+plot.organiser();
    }
  }
  
  public static class Sentence {
    Person jailed;
    int timeStarts, timeEnds;
  }
  
  List <Trial> trials = new List();
  Table <Person, Sentence> sentences = new Table();
  
  
  Council(World world) {
    this.world = world;
  }
  
  
  void loadState(Session s) throws Exception {
    mainHall = (Base) s.loadObject();
    s.loadObjects(courts );
    s.loadObjects(prisons);
    
    for (int n = s.loadInt(); n-- > 0;) {
      Trial t = new Trial();
      t.plot = (Plot) s.loadObject();
      s.loadObjects(t.accused);
      t.timeStarts = s.loadInt();
      t.timeEnds   = s.loadInt();
      trials.add(t);
    }
    for (int n = s.loadInt(); n-- > 0;) {
      Sentence t = new Sentence();
      t.jailed     = (Person) s.loadObject();
      t.timeStarts = s.loadInt();
      t.timeEnds   = s.loadInt();
      sentences.put(t.jailed, t);
    }
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObject(mainHall);
    s.saveObjects(courts );
    s.saveObjects(prisons);
    
    s.saveInt(trials.size());
    for (Trial t : trials) {
      s.saveObject(t.plot);
      s.saveObjects(t.accused);
      s.saveInt(t.timeStarts);
      s.saveInt(t.timeEnds  );
    }
    s.saveInt(sentences.size());
    for (Sentence t : sentences.values()) {
      s.saveObject(t.jailed);
      s.saveInt(t.timeStarts);
      s.saveInt(t.timeEnds  );
    }
  }
  
  
  
  /**  Regular updates and initial setup-
    */
  public void bindToFaction(Base mainHall) {
    this.mainHall = mainHall;
  }
  
  
  public void assignCourt(Place court) {
    courts.add(court);
  }
  
  
  public void assignPrison(Place prison) {
    prisons.add(prison);
  }
  
  
  public void updateCouncil() {
    if (mainHall == null) return;
    
    int time = world.timing.totalHours();
    for (Trial t : trials) if (time >= t.timeEnds) {
      concludeTrial(t);
      trials.remove(t);
    }
    
    for (Object p : sentences.keySet().toArray()) {
      Sentence s = sentences.get(p);
      if (time >= s.timeEnds) {
        releasePrisoner((Person) p);
      }
    }
  }
  
  
  
  /**  Trials and sentencing-
    */
  final public static int
    TRIAL_DELAY_MIN    = World.DAYS_PER_WEEK * 2,
    TRIAL_DELAY_MAX    = World.DAYS_PER_WEEK * 4,
    TRIAL_DURATION_AVG = World.DAYS_PER_WEEK * 2,
    TRIAL_SENTENCE_AVG = World.DAYS_PER_WEEK * World.WEEKS_PER_YEAR
  ;
  

  public void scheduleTrial(
    Plot plot, Series <Person> captive, Series <CaseFile> evidence
  ) {
    int delay    = (int) Rand.range(TRIAL_DELAY_MIN, TRIAL_DELAY_MAX);
    int duration = (int) ((Rand.num() + 0.5f) * TRIAL_DURATION_AVG);
    scheduleTrial(plot, captive, evidence, delay, duration);
  }
  
  
  public void scheduleTrial(
    Plot plot, Series <Person> captive, Series <CaseFile> evidence,
    int daysDelay, int daysDuration
  ) {
    if (mainHall == null) {
      I.say("NO MAIN HALL ASSIGNED TO COUNCIL, CANNOT SCHEDULE TRIAL!");
      return;
    }
    
    Trial t = new Trial();
    t.plot = plot;
    Visit.appendTo(t.accused, captive);
    
    for (CaseFile f : evidence) {
      CaseFile m = mainHall.leads.caseFor(f.subject);
      m.updateEvidenceFrom(f);
    }
    for (Person p : captive) {
      //  TODO:  THIS NEEDS TO BE HANDLED DIFFERENTLY
      Place prison = pickPrison(p);
      Place.setResident(p, prison, true);
    }
    
    trials.add(t);
    scheduleTrial(t, daysDelay, daysDuration);
  }
  
  
  public void scheduleTrial(Trial t, int daysDelay, int daysDuration) {
    if (! trials.includes(t)) I.complain("Cannot schedule nonexistant trial!");
    
    t.timeStarts =  daysDelay * World.HOURS_PER_DAY;
    t.timeStarts += world.timing.totalHours();
    t.timeEnds   =  daysDuration * World.HOURS_PER_DAY;
    t.timeEnds   += t.timeStarts;
  }
  
  
  public Trial nextTrialFor(Person accused) {
    for (Trial t : trials) if (t.accused.includes(accused)) return t;
    return null;
  }
  
  
  public Trial nextTrialFor(Plot plot) {
    for (Trial t : trials) if (t.plot == plot) return t;
    return null;
  }
  
  
  public Series <Trial> upcomingTrials() {
    return trials;
  }
  
  
  public float rateEvidence(Person p, Trial t) {
    float sumEvidence = 0;
    for (Plot plot : mainHall.leads.involvedIn(p, true)) {
      sumEvidence += mainHall.leads.evidenceForInvolvement(plot, p);
    }
    return 1 - (1f / (1 + sumEvidence));
  }
  
  
  public float rateEvidence(Trial t) {
    float sum = 0;
    for (Person p : t.accused) {
      sum += rateEvidence(p, t);
    }
    return sum / Nums.max(1, t.accused.size());
  }
  
  
  private void concludeTrial(Trial t) {
    for (Person p : t.accused) {
      float evidence = rateEvidence(p, t);
      if (Rand.num() < evidence) {
        Sentence s = new Sentence();
        s.jailed = p;
        s.timeStarts = world.timing.totalHours();
        s.timeEnds = (int) ((Rand.num() + 0.5f) + TRIAL_SENTENCE_AVG);
        s.timeEnds *= World.HOURS_PER_DAY;
        s.timeEnds += s.timeStarts;
        sentences.put(p, s);
      }
      else {
        releasePrisoner(p);
      }
    }
  }
  
  
  private Place pickPrison(Person p) {
    Pick <Place> pick = new Pick();
    for (Place prison : prisons) {
      pick.compare(prison, 0 - prison.residents().size());
    }
    if (pick.empty()) return mainHall;
    return pick.result();
  }
  
  
  private void releasePrisoner(Person p) {
    if (p.resides() == null) return;
    Place.setResident(p, p.resides(), false);
    //  TODO:  They'll need to re-enter the workforce now!
    sentences.remove(p);
  }
  
}






