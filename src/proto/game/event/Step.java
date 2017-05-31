
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
  String descTemplate;
  Role
    involved[],
    acting  ,
    subject ,
    mentions,
    from    ,
    goes    ;
  int medium;
  int hoursTaken;
  
  
  private Step(String ID) {
    super(INDEX, ID);
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
    String ID, String label, String descTemplate,
    Role acting, Role from, Role subject, Role goes,
    Role mentions, int medium, int hoursTaken, Role... others
  ) {
    Step s = new Step(ID);
    
    s.label        = label       ;
    s.descTemplate = descTemplate;
    
    s.medium     = medium    ;
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
    return medium == LeadType.MEDIUM_ASSAULT;
  }
  
  
  public boolean isMeeting() {
    return medium == LeadType.MEDIUM_MEETING;
  }
  
  
  public boolean isPhysical() {
    return medium != LeadType.MEDIUM_WIRE;
  }
  
  
  public boolean isWired() {
    return medium == LeadType.MEDIUM_WIRE;
  }
  
  
  public boolean hasLabel(String label) {
    return this.label.equals(label);
  }
  
  
  public boolean involves(Role role) {
    return Visit.arrayIncludes(involved, role);
  }
  
  
  
  /**  Generating potential Clues-
    */
  protected Series <Clue> addTraitClues(
    Plot plot, Element involved, Step step,
    Base follows, Batch <Clue> possible
  ) {
    //
    //  Wiretaps and mentions can't reliably reveal any descriptive features
    //  of the suspects involved, except as tipoffs.
    Role role = plot.roleFor(involved);
    if (role == null) return possible;
    
    if (involved.isPerson()) {
      Person p = (Person) involved;
      for (Trait t : Common.PERSON_TRAITS) {
        if (p.stats.levelFor(t) <= 0) continue;
        Clue forTrait = Clue.traitClue(plot, role, step, t);
        possible.add(forTrait);
      }
    }
    
    if (involved.isPlace()) {
      Place p = (Place) involved;
      for (Trait t : Common.VENUE_TRAITS) {
        if (! p.hasProperty(t)) continue;
        Clue forTrait = Clue.traitClue(plot, role, step, t);
        possible.add(forTrait);
      }
    }
    
    if (involved.isItem()) {
      Item p = (Item) involved;
      Clue match = Clue.confirmSuspect(plot, role, step, p, p.place());
      possible.add(match);
    }
    
    return possible;
  }
  
  
  protected Batch <Clue> addLocationClues(
    Plot plot, Element involved, Step step,
    Base follows, Batch <Clue> possible
  ) {
    Role role = plot.roleFor(involved);
    if (role == null || ! involved.isPlace()) return possible;
    
    World world = plot.base.world();
    Region at = involved.region();
    Series <Region> around = world.regionsInRange(at, 1);
    
    for (Region near : around) {
      int range = (int) world.distanceBetween(at, near);
      Clue clue = Clue.locationClue(plot, role, step, near, range);
      clue.setGetChance(1f / ((1 + range) * (1 + range)));
      possible.add(clue);
    }
    
    return possible;
  }
  
  
  //  TODO:  Not sure I like this.  Think about it again.
  //*
  protected boolean canLeakAim() {
    for (Role main : Plot.KNOWS_AIM) {
      if (main == mentions) return true;
      if (Visit.arrayIncludes(involved, main)) return true;
    }
    return false;
  }
  //*/
  
  
  public Series <Clue> possibleClues(
    Plot plot, Element involved, Element focus, Step step,
    Base follows, LeadType leadType
  ) {
    Batch <Clue> possible = new Batch();
    Batch <Clue> screened = new Batch();
    
    addTraitClues   (plot, involved, step, follows, possible);
    addLocationClues(plot, involved, step, follows, possible);
    
    CaseFile file = follows.leads.caseFor(plot);
    for (Clue clue : possible) {
      if (! leadType.canProvide(clue, involved, focus)) continue;
      if (file.isRedundant(clue)) continue;
      screened.add(clue);
    }
    
    return screened;
  }
  
  
  public Clue pickFrom(Series <Clue> possible) {
    Clue band[] = possible.toArray(Clue.class);
    float weights[] = new float[band.length];
    for (int i = band.length; i-- > 0;) weights[i] = band[i].getChance();
    return (Clue) Rand.pickFrom(band, weights);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String label() {
    return label;
  }
  
  
  public String descTemplate() {
    return descTemplate;
  }
  
  
  public String toString() {
    return label;
  }
}







