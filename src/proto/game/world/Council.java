

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.util.*;



public class Council {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final World world;
  Base mainHall;
  List <Place> courts  = new List();
  List <Place> prisons = new List();
  
  //  TODO:  Make Trials into a kind of Event themselves, so they can be
  //  disrupted by crooks.
  static class Trial {
    List <Person> accused = new List();
    int timeStarts, timeEnds;
  }
  
  static class Sentence {
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
    for (Trial t : trials) if (t.timeEnds >= time) {
      concludeTrial(t);
      trials.remove(t);
    }
    
    for (Object p : sentences.keySet().toArray()) {
      Sentence s = sentences.get(p);
      if (s.timeEnds >= time) {
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
    Series <Person> captive, Series <CaseFile> evidence
  ) {
    if (mainHall == null) {
      I.say("NO MAIN HALL ASSIGNED TO COUNCIL, CANNOT SCHEDULE TRIAL!");
      return;
    }
    
    Trial t = new Trial();
    Visit.appendTo(t.accused, captive);
    
    for (CaseFile f : evidence) {
      CaseFile m = mainHall.leads.caseFor(f.subject);
      m.updateEvidenceFrom(f);
    }
    for (Person p : captive) {
      Place prison = pickPrison(p);
      Place.setResident(p, prison, true);
    }
    
    t.timeStarts =  (int) Rand.range(TRIAL_DELAY_MIN, TRIAL_DELAY_MAX);
    t.timeStarts *= World.HOURS_PER_DAY;
    t.timeStarts += world.timing.totalHours();
    t.timeEnds   =  (int) ((Rand.num() + 0.5f) * TRIAL_DURATION_AVG);
    t.timeEnds   *= World.HOURS_PER_DAY;
    t.timeEnds   += t.timeStarts;
    trials.add(t);
  }
  
  
  private Place pickPrison(Person p) {
    Pick <Place> pick = new Pick();
    for (Place prison : prisons) {
      pick.compare(prison, 0 - prison.residents().size());
    }
    if (pick.empty()) return mainHall;
    return pick.result();
  }
  
  
  private void concludeTrial(Trial t) {
    //
    //  TODO:  Construct a report for this!
    
    for (Person p : t.accused) {
      float sumEvidence = 0;
      for (Crime c : mainHall.leads.involvedIn(p)) {
        sumEvidence += mainHall.leads.evidenceForInvolvement(c, p);
      }
      
      float releaseChance = 1f / (1 + sumEvidence);
      if (Rand.num() < releaseChance) {
        releasePrisoner(p);
      }
      else {
        Sentence s = new Sentence();
        s.jailed = p;
        s.timeStarts = world.timing.totalHours();
        s.timeEnds = (int) ((Rand.num() + 0.5f) + TRIAL_SENTENCE_AVG);
        s.timeEnds *= World.HOURS_PER_DAY;
        s.timeEnds += s.timeStarts;
        sentences.put(p, s);
      }
    }
  }
  
  
  private void releasePrisoner(Person p) {
    Place.setResident(p, p.base(), true);
    sentences.remove(p);
  }
  
}





