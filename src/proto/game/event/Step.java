
package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class Step implements Session.Saveable {
  
  /**  Data fields and save/load methods-
    */
  Plot plot;
  String label;
  int ID;
  
  Element involved[];
  Element acting  ;
  Element subject ;
  Element mentions;
  Element brings  ;
  Place from;
  Place goes;
  int medium;
  int hoursTaken;
  int timeStart = -1;
  
  
  private Step() {
    return;
  }
  
  
  public Step(Session s) throws Exception {
    s.cacheInstance(this);
    plot       = (Plot) s.loadObject();
    label      = s.loadString();
    ID         = s.loadInt();

    involved   = (Element[]) s.loadObjectArray(Element.class);
    acting     = (Element) s.loadObject();
    subject    = (Element) s.loadObject();
    mentions   = (Element) s.loadObject();
    brings     = (Element) s.loadObject();
    from       = (Place  ) s.loadObject();
    goes       = (Place  ) s.loadObject();
    medium     = s.loadInt();
    hoursTaken = s.loadInt();
    timeStart  = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(plot );
    s.saveString(label);
    s.saveInt   (ID   );
    
    s.saveObjectArray(involved);
    s.saveObject(acting    );
    s.saveObject(subject   );
    s.saveObject(mentions  );
    s.saveObject(brings    );
    s.saveObject(from      );
    s.saveObject(goes      );
    s.saveInt   (medium    );
    s.saveInt   (hoursTaken);
    s.saveInt   (timeStart );
  }
  
  
  
  /**  Factory methods for convenience:
    */
  public static Step queueStep(
    String label, Plot plot,
    Plot.Role acting, Plot.Role from, Plot.Role subject, Plot.Role goes,
    Plot.Role mentions, int medium, int hoursTaken, Plot.Role... others
  ) {
    Step s = new Step();
    s.label      = label;
    s.plot       = plot;
    s.ID         = plot.steps.size();
    s.medium     = medium;
    s.hoursTaken = hoursTaken;
    
    Batch <Element> involved = new Batch();
    involved.include(s.acting =       plot.filling(acting ));
    involved.include(s.from = (Place) plot.filling(from   ));
    involved.include(s.subject =      plot.filling(subject));
    involved.include(s.goes = (Place) plot.filling(goes   ));
    for (Plot.Role role : others) involved.include(plot.filling(role));
    
    s.involved = involved.toArray(Element.class);
    s.mentions = plot.filling(mentions);
    plot.steps.add(s);
    return s;
  }
  
  
  
  /**  Assorted no-brainer access methods-
    */
  public boolean isAssault() {
    return medium == Lead.MEDIUM_ASSAULT;
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
  
  
  public Element acting() {
    return acting;
  }
  
  
  public Element subject() {
    return subject;
  }
  
  
  public Place from() {
    return from;
  }
  
  
  public Place goes() {
    return goes;
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
    Element involved, Lead lead, Batch <Clue> possible
  ) {
    //
    //  Wiretaps and mentions can't reliably reveal any descriptive features
    //  of the suspects involved.
    Plot.Role role = plot.roleFor(involved);
    if (role == null || medium == Lead.MEDIUM_WIRE) {
      return possible;
    }
    
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
      Clue match = Clue.confirmSuspect(plot, role, p, p.place());
      possible.add(match);
    }
    
    return possible;
  }
  
  
  protected Batch <Clue> addLocationClues(
    Element involved, Lead lead, Batch <Clue> possible
  ) {
    Plot.Role role = plot.roleFor(involved);
    if (role == null || ! involved.isPlace()) return possible;
    
    World world = plot.base.world();
    Region at = involved.region();
    Series <Region> around = world.regionsInRange(at, 1);
    
    for (Region near : around) {
      int range = (int) world.distanceBetween(at, near);
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
  
  
  
  public Series <Clue> possibleClues(
    Element focus, Lead lead
  ) {
    Batch <Clue> possible = new Batch();
    addTraitClues   (focus, lead, possible);
    addLocationClues(focus, lead, possible);
    addIntentClues  (focus, lead, possible);
    return possible;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String label() {
    return label;
  }
  
  
  public String toString() {
    StringBuffer s = new StringBuffer();
    s.append(label);
    if (timeStart == -1) s.append(" [T=?]");
    else s.append(" [T="+timeStart+"-"+(timeStart + hoursTaken)+"]");
    s.append(" ["+Lead.MEDIUM_DESC[medium]+"]");
    s.append(" ["+from+" -> "+goes+"] [");
    for (Element e : involved) {
      if (e.isPlace() || e.isRegion()) continue;
      s.append("\n    "+e);
    }
    s.append("\n  ]");
    
    return s.toString();
  }
}




