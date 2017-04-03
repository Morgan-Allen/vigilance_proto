

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;


//  TODO:  Assign officials to preside over/prosecute/defend during a trial, so
//  they can earn grudges from crime bosses.
//
//  TODO:  Construct a report for trials being scheduled or sentence passed!

public class Council {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public static int
    TRIAL_DELAY_MIN    = World.DAYS_PER_WEEK * 2,
    TRIAL_DELAY_MAX    = World.DAYS_PER_WEEK * 4,
    TRIAL_SENTENCE_AVG = World.DAYS_PER_WEEK * World.WEEKS_PER_YEAR;
  
  final World world;
  Base mainHall;
  List <Place> courts  = new List();
  List <Place> prisons = new List();
  
  
  public static class Sentence {
    Person jailed;
    int timeStarts, timeEnds;
  }
  
  Table <Person, Sentence> sentences = new Table();
  
  
  Council(World world) {
    this.world = world;
  }
  
  
  void loadState(Session s) throws Exception {
    mainHall = (Base) s.loadObject();
    s.loadObjects(courts );
    s.loadObjects(prisons);
    
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
    
    for (Object p : sentences.keySet().toArray()) {
      Sentence s = sentences.get(p);
      if (time >= s.timeEnds) {
        releasePrisoner((Person) p);
      }
    }
  }
  
  
  
  /**  Handling Trials:
    */
  public Trial scheduleTrial(
    Plot plot, Series <Person> captive, Series <CaseFile> evidence
  ) {
    int delay = (int) Rand.range(TRIAL_DELAY_MIN, TRIAL_DELAY_MAX);
    return scheduleTrial(plot, captive, evidence, delay);
  }
  
  
  public Trial scheduleTrial(
    Plot plot, Series <Person> captive, Series <CaseFile> evidence,
    int daysDelay
  ) {
    if (mainHall == null) {
      I.say("NO MAIN HALL ASSIGNED TO COUNCIL, CANNOT SCHEDULE TRIAL!");
      return null;
    }
    
    for (CaseFile f : evidence) {
      CaseFile m = mainHall.leads.caseFor(f.subject);
      m.updateEvidenceFrom(f);
    }
    
    for (Person p : captive) {
      //  TODO:  THIS NEEDS TO BE HANDLED DIFFERENTLY
      Place prison = pickPrison(p);
      Place.setResident(p, prison, true);
    }
    
    Trial t = new Trial(world, mainHall);
    t.assignAccused(plot, captive);
    world.events.scheduleEvent(t, daysDelay * World.HOURS_PER_DAY);
    return t;
  }
  
  
  public Trial nextTrialFor(Person accused) {
    for (Trial t : upcomingTrials()) {
      if (t.involves(null, accused)) return t;
    }
    return null;
  }
  
  
  public Trial nextTrialFor(Plot plot) {
    for (Trial t : upcomingTrials()) {
      if (t.involves(plot, null)) return t;
    }
    return null;
  }
  
  
  public Series <Trial> upcomingTrials() {
    Batch <Trial> matches = new Batch();
    for (Event e : world.events.coming()) {
      if (e.type == Trial.TYPE_TRIAL) matches.add((Trial) e);
    }
    return matches;
  }
  
  
  public void applySentence(Person accused, float durationMult) {
    Sentence s = new Sentence();
    s.jailed = accused;
    s.timeStarts = world.timing.totalHours();
    s.timeEnds = (int) ((Rand.num() + 0.5f) + TRIAL_SENTENCE_AVG);
    s.timeEnds *= World.HOURS_PER_DAY * durationMult;
    s.timeEnds += s.timeStarts;
    sentences.put(accused, s);
  }
  
  
  private Place pickPrison(Person p) {
    Pick <Place> pick = new Pick();
    for (Place prison : prisons) {
      pick.compare(prison, 0 - prison.residents().size());
    }
    if (pick.empty()) return mainHall;
    return pick.result();
  }
  
  
  public void releasePrisoner(Person p) {
    if (p.resides() == null) return;
    Place.setResident(p, p.resides(), false);
    //  TODO:  They'll need to re-enter the workforce now?
    sentences.remove(p);
  }
  
}







