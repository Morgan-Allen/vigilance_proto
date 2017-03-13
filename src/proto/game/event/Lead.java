

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import java.awt.Image;


//
//  TODO:  You could sharpen this up a little, with separate success-chance
//  for each party involved in a step?
//  TODO:  You might consider only giving information on the rough
//  location of a meet or wiretap trace?
//  ...Yeah.  Determine location and traits in a separate step.


public class Lead extends Task {
  
  
  /**  Some preliminaries needed for built-in type-definitions:
    */
  final public static int
    //  The method by which a lead can be followed (and which contacts it can
    //  pick up on)-
    MEDIUM_MEET     =  1,
    MEDIUM_WIRE     =  2,
    MEDIUM_SURVEIL  =  3,
    MEDIUM_QUESTION =  4,
    MEDIUM_COVER    =  5,
    MEDIUM_HEIST    =  6,
    MEDIUM_ANY      = -1,
    PHYSICAL_MEDIA[] = { 1, 3, 4, 5, 6 },
    SOCIAL_MEDIA  [] = { 1, 4 },
    WIRED_MEDIA   [] = { 2 },
    //  Whether this lead can pick up on past/present/future contacts-
    TENSE_BEFORE    =  0,
    TENSE_DURING    =  1,
    TENSE_AFTER     =  2,
    TENSE_ANY       = -1,
    //  The type of focus this lead is intended for-
    FOCUS_PERSON    =  0,
    FOCUS_BUILDING  =  1,
    FOCUS_REGION    =  2,
    //  How likely following a lead is to spook the perpetrator-
    PROFILE_HIDDEN     = 0,
    PROFILE_LOW        = 1,
    PROFILE_SUSPICIOUS = 2,
    PROFILE_HIGH       = 3,
    PROFILE_OBVIOUS    = 4;
  final public static float
    //  Degrees of success in investigation-
    RESULT_NONE    = -1,
    RESULT_COLD    =  0,
    RESULT_PARTIAL =  1,
    RESULT_HOT     =  2,
    //  How much a successful lead counts for, assuming perfect success-
    CONFIDENCE_LOW      = 0.33f,
    CONFIDENCE_MODERATE = 0.66f,
    CONFIDENCE_HIGH     = 1.00f;
  
  
  public static boolean isPhysical(int medium) {
    for (int m : PHYSICAL_MEDIA) if (m == medium) return true;
    return false;
  }
  
  public static boolean isSocial(int medium) {
    for (int m : SOCIAL_MEDIA) if (m == medium) return true;
    return false;
  }
  
  public static boolean isWired(int medium) {
    for (int m : WIRED_MEDIA) if (m == medium) return true;
    return false;
  }
  
  
  public static class Type {
    
    String name;
    int ID;
    int medium, focus, tense, profile;
    float confidence;
    int cluesMedia[];
    
    Type(
      String name, int ID,
      int medium, int focus, int tense, int profile, float confidence,
      int... cluesMedia
    ) {
      this.name = name;
      this.ID   = ID  ;
      this.medium  = medium ;
      this.focus   = focus  ;
      this.tense   = tense  ;
      this.profile = profile;
      this.confidence = confidence;
      this.cluesMedia = cluesMedia;
      TYPE_B.add(this);
    }
  }

  private static Batch <Type> TYPE_B = new Batch();
  final public static Type
    LEAD_SURVEIL_PERSON = new Type(
      "Surveillance", 0,
      MEDIUM_SURVEIL, FOCUS_PERSON, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, MEDIUM_MEET
    ),
    LEAD_SURVEIL_BUILDING = new Type(
      "Surveillance", 1,
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_MEET
    ),
    LEAD_QUESTION = new Type(
      "Questioning", 2,
      MEDIUM_QUESTION, FOCUS_PERSON, TENSE_AFTER, PROFILE_HIGH,
      CONFIDENCE_MODERATE, MEDIUM_ANY
    ),
    LEAD_WIRETAP = new Type(
      "Wiretap", 3,
      MEDIUM_WIRE, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, MEDIUM_WIRE
    ),
    LEAD_PATROL = new Type(
      "Patrolling", 4,
      MEDIUM_SURVEIL, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_MEET, MEDIUM_SURVEIL
    ),
    LEAD_SCAN = new Type(
      "Scanning", 5,
      MEDIUM_WIRE, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_WIRE
    ),
    LEAD_CANVAS = new Type(
      "Canvassing", 6,
      MEDIUM_QUESTION, FOCUS_REGION, TENSE_AFTER, PROFILE_SUSPICIOUS,
      CONFIDENCE_LOW, MEDIUM_ANY
    ),
    LEAD_SEARCH = new Type(
      "Search", 7,
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_AFTER, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_WIRE, MEDIUM_MEET
    ),
    LEAD_TYPES[] = TYPE_B.toArray(Type.class);
  
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Type type;
  final Plot plot;
  final Element focus;
  private String lastContactID;
  
  
  Lead(Base base, Type type, Plot plot, Element focus) {
    super(base, Task.TIME_INDEF);
    
    this.type  = type ;
    this.plot  = plot ;
    this.focus = focus;
    
    boolean badFocus = false;
    if (type.focus == FOCUS_REGION) {
      if (! focus.isRegion()) badFocus = true;
    }
    if (type.focus == FOCUS_PERSON) {
      if (! focus.isPerson()) badFocus = true;
    }
    if (type.focus == FOCUS_BUILDING) {
      if (! focus.isPlace()) badFocus = true;
    }
    if (badFocus) I.complain("Incorrect focus type for "+type+": "+focus);
  }
  
  
  public Lead(Session s) throws Exception {
    super(s);
    type  = LEAD_TYPES[s.loadInt()];
    plot  = (Plot   ) s.loadObject();
    focus = (Element) s.loadObject();
    lastContactID = s.loadString();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveInt(type.ID);
    s.saveObject(plot);
    s.saveObject(focus);
    s.saveString(lastContactID);
  }
  
  
  
  /**  Generation and screening of clues related to the case:
    */
  protected boolean canDetect(
    Step step, int tense, Plot plot
  ) {
    //  TODO:  Consider splitting this off into separate sub-methods for
    //  override by subclasses.
    //
    //  First check the tense and crime-
    if (plot != this.plot) {
      return false;
    }
    if (tense != TENSE_ANY && type.tense != TENSE_ANY && tense != type.tense) {
      return false;
    }
    //
    //  Then, check the medium-
    boolean matchMedium = false;
    for (int medium : type.cluesMedia) {
      if (medium == MEDIUM_ANY || medium == step.medium) {
        matchMedium = true;
        break;
      }
    }
    if (! matchMedium) return false;
    //
    //  Then check the focus-
    boolean matchFocus = false;
    for (Plot.Role role : step.between) {
      Element contacts = plot.filling(role);
      
      if (type.focus == FOCUS_REGION) {
        if (contacts.region() != focus) continue;
      }
      else {
        if (contacts != focus) continue;
      }
      matchFocus = true;
    }
    if (! matchFocus) return false;
    //
    //  Then return true-
    return true;
  }
  
  
  protected Series <Clue> traitClues(
    Step step, int tense, Plot plot, float resultHeat
  ) {
    Batch <Clue> possible = new Batch();
    int time = plot.base.world().timing.totalHours();
    
    for (Plot.Role role : step.between) {
      Element involved = plot.filling(role);
      if (involved == focus) continue;
      
      if (involved.isPerson()) {
        Person p = (Person) involved;
        for (Trait t : Common.PERSON_TRAITS) {
          if (p.stats.levelFor(t) <= 0) continue;
          Clue clue = new Clue(plot, role);
          clue.assignEvidence(p, t, type, type.confidence, time);
          possible.add(clue);
        }
      }
      
      if (involved.isPlace()) {
        Place p = (Place) involved;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue clue = new Clue(plot, role);
          clue.assignEvidence(p, t, type, type.confidence, time);
          possible.add(clue);
        }
      }
      
      if (involved.isItem()) {
        Item p = (Item) involved;
        Clue clue = new Clue(plot, role);
        clue.confirmMatch(p, type, time);
        possible.add(clue);
      }
    }
    
    return possible;
  }
  
  
  protected Batch <Clue> regionClues(
    Step step, int tense, Plot plot, float resultHeat
  ) {
    Batch <Clue> possible = new Batch();
    int time = plot.base.world().timing.totalHours();
    
    for (Plot.Role role : step.between) {
      Element involved = plot.filling(role);
      if (involved == focus) continue;
      
      Element p = (Element) involved;
      Region at = involved.region();
      int placeRange = Rand.yes() ? 0 : 1;
      Series <Region> around = base.world().regionsInRange(at, placeRange);
      
      Region near = (Region) Rand.pickFrom(around);
      Clue clue = new Clue(plot, role);
      clue.assignNearbyRegion(p, near, placeRange, type, type.confidence, time);
      possible.add(clue);
    }
    
    return possible;
  }
  
  
  
  public float followChance(Series <Person> follow) {
    float skill = 0, obstacle = 1;
    
    if (type.medium == MEDIUM_SURVEIL) {
      Person perp = (Person) focus;
      skill = teamStatBonus(follow, SIGHT_RANGE);
      obstacle = perp   .stats.levelFor(HIDE_RANGE );
    }
    
    if (type.medium == MEDIUM_WIRE) {
      Place site = (Place) focus;
      skill = teamStatBonus(follow, ENGINEERING);
      obstacle = 5;
    }
    
    if (type.medium == MEDIUM_QUESTION) {
      Person perp = (Person) focus;
      skill = teamStatBonus(follow, QUESTION);
      obstacle = perp.stats.levelFor(PERSUADE);
    }
    
    if (type.medium == MEDIUM_COVER) {
      Place site = (Place) focus;
      skill = teamStatBonus(follow, PERSUADE);
      obstacle = 5;
    }
    
    return skill / (skill + obstacle);
  }
  
  
  protected float teamStatBonus(Series <Person> follow, Trait stat) {
    float bestStat = 0, sumStats = 0;
    for (Person p : follow) {
      float level = p.stats.levelFor(stat);
      sumStats += level;
      bestStat = Nums.max(bestStat, level);
    }
    return bestStat + ((sumStats - bestStat) / 2);
  }
  
  
  protected float followResult(Series <Person> follow) {
    float chance = followChance(follow);
    chance = 1 - (Nums.sqrt(1 - chance));
    boolean roll1 = Rand.num() < chance, roll2 = Rand.num() < chance;
    
    if (roll1 && roll2) return RESULT_HOT;
    if (roll1 || roll2) return RESULT_PARTIAL;
    return RESULT_COLD;
  }
  
  
  protected float performFollow(
    Step step, int tense, Plot plot, Series <Person> follow
  ) {
    //
    //  First, check to see whether anything has actually changed here:
    String contactID = step.ID+"_"+tense;
    if (contactID.equals(lastContactID)) {
      return RESULT_NONE;
    }
    
    float result = followResult(follow);
    int time = base.world().timing.totalHours();
    
    if (result <= RESULT_COLD) {
      
    }
    else if (result <= RESULT_PARTIAL) {
      Series <Clue> fromTraits = traitClues (step, tense, plot, result);
      Series <Clue> fromRegion = regionClues(step, tense, plot, result);
      
      Clue gained = null;
      if (Rand.yes()    ) gained = (Clue) Rand.pickFrom(fromTraits);
      if (gained == null) gained = (Clue) Rand.pickFrom(fromRegion);
      
      if (gained != null) {
        CaseFile file = base.leads.caseFor(gained.match);
        file.recordClue(gained);
      }
    }
    else if (result <= RESULT_HOT) {
      for (Plot.Role role : step.between) {
        Element subject = plot.filling(role);
        CaseFile file = base.leads.caseFor(subject);
        
        Clue clue = new Clue(plot,role);
        clue.confirmMatch(subject, type, time);
        file.recordClue(clue);
      }
    }
    
    plot.takeSpooking(type.profile);
    return result;
  }
  
  
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    Series <Person> active = active();
    
    for (Event event : base.world().events.active()) {
      if (! (event instanceof Plot)) continue;
      Plot plot = (Plot) event;
      
      for (Step step : plot.allSteps()) {
        int tense = plot.stepTense(step);
        if (! canDetect(step, tense, plot)) continue;
        performFollow(step, tense, plot, active);
      }
    }
    
    return true;
  }
  
  
  public Place targetLocation(Person p) {
    return focus.place();
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  //  TODO:  Fill these out-
  public String activeInfo() {
    return null;
  }
  
  
  public String helpInfo() {
    return null;
  }
  
  
  public Image icon() {
    return null;
  }
  
  
  public String choiceInfo(Person p) {
    return null;
  }
}



