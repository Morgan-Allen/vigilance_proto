

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.game.world.*;
import proto.util.*;



//  TODO:  Have this extend Task?

public class Step implements Session.Saveable {
  
  
  /**  Data fields and save/load methods-
    */
  Plot plot;
  Element involved[];
  
  String label;
  int ID;
  int medium;
  int hoursTaken;
  
  int timeStart = -1;
  boolean spooked = false;
  
  
  private Step() {
    return;
  }
  
  
  public Step(Session s) throws Exception {
    s.cacheInstance(this);
    plot       = (Plot) s.loadObject();
    label      = s.loadString();
    ID         = s.loadInt();

    involved   = (Element[]) s.loadObjectArray(Element.class);
    medium     = s.loadInt();
    hoursTaken = s.loadInt();
    
    timeStart = s.loadInt();
    spooked   = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(plot);
    s.saveString(label);
    s.saveInt(ID);
    
    s.saveObjectArray(involved);
    s.saveInt(medium    );
    s.saveInt(hoursTaken);
    
    s.saveInt (timeStart);
    s.saveBool(spooked  );
  }
  
  
  
  /**  Factory methods for convenience:
    */
  public static Step queueStep(
    String label, Plot plot,
    Plot.Role acting, Plot.Role subject,
    int medium, int hoursTaken, Plot.Role... others
  ) {
    Step s = new Step();
    s.label      = label;
    s.plot       = plot;
    s.ID         = plot.steps.size();
    s.medium     = medium;
    s.hoursTaken = hoursTaken;
    
    Batch <Element> involved = new Batch();
    involved.include(plot.filling (acting ));
    involved.include(plot.location(acting ));
    involved.include(plot.filling (subject));
    involved.include(plot.location(subject));
    for (Plot.Role role : others) involved.include(plot.filling(role));
    s.involved = involved.toArray(Element.class);
    
    plot.steps.add(s);
    return s;
  }
  
  
  
  /**  Assorted no-brainer access methods-
    */
  public boolean isHeist() {
    return medium == Lead.MEDIUM_HEIST;
  }
  
  
  public boolean isPhysical() {
    return Lead.isPhysical(medium);
  }
  
  
  public boolean isMeeting() {
    return Lead.isSocial(medium);
  }
  
  
  public boolean isWired() {
    return Lead.isWired(medium);
  }
  
  
  public boolean hasLabel(String label) {
    return this.label.equals(label);
  }
  
  
  public Element[] involved() {
    return involved;
  }
  
  
  
  /**  Life cycle, timing and updates:
    */
  public boolean begun() {
    return timeStart >= 0;
  }
  
  
  public boolean complete() {
    if (! begun()) return false;
    int time = plot.base.world().timing.totalHours();
    return time >= timeStart + hoursTaken;
  }
  
  
  public int timeScheduled() {
    if (plot.currentStep() == null) return -1;
    int time = -1;
    for (Step s : plot.steps) {
      if (time == -1) time = s.timeStart;
      time += s.hoursTaken;
      if (s == this) break;
    }
    return time;
  }
  
  
  public int tense() {
    if (timeStart == -1) return Lead.TENSE_NONE;
    int time = plot.base.world().timing.totalHours();
    boolean begun = timeStart >= 0;
    boolean done = time >= (timeStart + hoursTaken);
    if (begun) return done ? Lead.TENSE_AFTER : Lead.TENSE_DURING;
    return Lead.TENSE_BEFORE;
  }
  
  
  
  /**  Generating potential Clues-
    */
  //*
  protected Series <Clue> addTraitClues(
    Element focus, Lead lead, Batch <Clue> possible
  ) {
    for (Element involved : this.involved()) {
      Plot.Role role = plot.roleFor(involved);
      if (role == null || involved == focus) continue;
      
      if (involved.isPerson()) {
        Person p = (Person) involved;
        for (Trait t : Common.PERSON_TRAITS) {
          if (p.stats.levelFor(t) <= 0) continue;
          Clue forTrait = Clue.traitClue(plot, role, t);
          possible.add(forTrait);
        }
      }
      
      if (involved.isPlace()) {
        Place p = (Place) involved;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue forTrait = Clue.traitClue(plot, role, t);
          possible.add(forTrait);
        }
      }
      
      if (involved.isItem()) {
        Item p = (Item) involved;
        Clue match = Clue.confirmSuspect(plot, role, p);
        possible.add(match);
      }
    }
    
    return possible;
  }
  
  
  protected Batch <Clue> addLocationClues(
    Element focus, Lead lead, Batch <Clue> possible
  ) {
    for (Element involved : this.involved()) {
      Plot.Role role = plot.roleFor(involved);
      if (involved == focus || role == null) continue;
      
      World world = plot.base.world();
      Region at = involved.region();
      int range = Rand.yes() ? 0 : 1;
      Series <Region> around = world.regionsInRange(at, range);
      
      Region near = (Region) Rand.pickFrom(around);
      Clue clue = Clue.locationClue(plot, role, near, range);
      possible.add(clue);
    }
    return possible;
  }
  
  
  protected Batch <Clue> addIntentClues(
    Element focus, Lead lead, Batch <Clue> possible
  ) {
    //  TODO:  Restore later.
    /*
    World world = plot.base.world();
    int time = world.timing.totalHours();
    Place place = focus.place();
    
    Step heist = plot.mainHeist();
    int heistTime = heist.timeScheduled();
    PlotType heistType = (PlotType) plot.type;
    
    Clue forHeist = new Clue(plot, Plot.ROLE_OBJECTIVE);
    forHeist.confirmHeistDetails(heistType, heistTime, lead, time, place);
    possible.add(forHeist);
    //*/
    return possible;
  }
  
  
  
  protected Series <Clue> possibleClues(
    Element focus, Lead lead
  ) {
    Batch <Clue> possible = new Batch();
    addTraitClues   (focus, lead, possible);
    addLocationClues(focus, lead, possible);
    addIntentClues  (focus, lead, possible);
    return possible;
  }
  
  
  /*
  protected void confirmIdentity(
    Element focus, Lead lead, Element subject
  ) {
    World world = plot.base.world();
    int time = world.timing.totalHours();
    Place place = focus.place();
    
    Plot.Role role = plot.roleFor(subject);
    CaseFile file = lead.base.leads.caseFor(subject);
    Clue clue = new Clue(plot, role);
    clue.confirmMatch(subject, lead, time, place);
    file.recordClue(clue);
    
    if (role == Plot.ROLE_TARGET) {
      CaseFile forPlot = lead.base.leads.caseFor(plot);
      for (Clue intent : intentClues(focus, lead)) forPlot.recordClue(intent);
    }
  }
  //*/
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return label+": "+I.list(involved);
  }
}







