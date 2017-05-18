
package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class Step extends Index.Entry implements Session.Saveable {
  
  /**  Data fields and save/load methods-
    */
  final static Index <Step> INDEX = new Index();
  
  String label;
  Role
    involved[],
    acting  ,
    subject ,
    mentions,
    from    ,
    goes    ;
  int medium;
  int hoursTaken;
  
  
  private Step(String ID, String label) {
    super(INDEX, ID);
    this.label = label;
  }
  
  
  public static Step loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  /**  Factory methods for convenience:
    */
  public static Step stepWith(
    String ID, String label,
    Role acting, Role from, Role subject, Role goes,
    Role mentions, int medium, int hoursTaken, Role... others
  ) {
    Step s = new Step(ID, label);
    s.medium     = medium;
    s.hoursTaken = hoursTaken;
    
    Batch <Role> involved = new Batch();
    involved.include(s.acting  = acting );
    involved.include(s.from    = from   );
    involved.include(s.subject = subject);
    involved.include(s.goes    = goes   );
    for (Role role : others) involved.include(role);
    
    s.involved = involved.toArray(Role.class);
    s.mentions = mentions;
    
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
  
  
  
  /**  Generating potential Clues-
    */
  //*
  protected Series <Clue> addTraitClues(
    Plot plot, Element involved, Base follows, Batch <Clue> possible
  ) {
    //
    //  Wiretaps and mentions can't reliably reveal any descriptive features
    //  of the suspects involved.
    Role role = plot.roleFor(involved);
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
    Plot plot, Element involved, Base follows, Batch <Clue> possible
  ) {
    Role role = plot.roleFor(involved);
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
  
  
  protected boolean canLeakAim() {
    for (Role main : Plot.MAIN_ROLES) {
      if (main == mentions) return true;
      if (Visit.arrayIncludes(involved, main)) return true;
    }
    return false;
  }
  
  
  public Series <Clue> possibleClues(
    Plot plot, Element focus, Base follows
  ) {
    Batch <Clue> possible = new Batch();
    Batch <Clue> screened = new Batch();
    
    addTraitClues   (plot, focus, follows, possible);
    addLocationClues(plot, focus, follows, possible);
    
    CaseFile file = follows.leads.caseFor(plot);
    for (Clue clue : possible) if (! file.isRedundant(clue)) {
      screened.add(clue);
    }
    
    return screened;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String label() {
    return label;
  }
  
  
  public String toString() {
    StringBuffer s = new StringBuffer();
    s.append(label);
    s.append(" ["+Lead.MEDIUM_DESC[medium]+"]");
    s.append(" ["+from+" -> "+goes+"] [");
    for (Role e : involved) {
      s.append("\n    "+e);
    }
    s.append("\n  ]");
    
    return s.toString();
  }
}




