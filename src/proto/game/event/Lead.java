

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;

import java.awt.Image;



public class Lead extends Task {
  
  
  /**  Some preliminaries needed for built-in type-definitions:
    */
  final public static int
    //  The method by which a lead can be followed (and which contacts it can
    //  pick up on)-
    MEDIUM_MEET     =  1,
    MEDIUM_WIRE     =  2,
    MEDIUM_SURVEIL  =  3,
    MEDIUM_HEIST    =  4,
    MEDIUM_QUESTION =  5,
    MEDIUM_COVER    =  6,
    MEDIUM_ANY      = -1,
    PHYSICAL_MEDIA[] = { 1, 3, 4, 5, 6 },
    SOCIAL_MEDIA  [] = { 1, 5 },
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
    FOCUS_ANY       = -1,
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
  final public static int
    CLUE_EXPIRATION_TIME = World.HOURS_PER_DAY * World.DAYS_PER_WEEK;
  
  final static String
    PROFILE_DESC[] = {
      "Nil", "Low", "Moderate", "High", "100%"
    },
    CONFIDENCE_DESC[] = {
      "Weak", "Fair", "Strong"
    };
  
  
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
    
    String name, info, tenseVerbs[];
    Image icon;
    
    int ID;
    int medium, focus, tense, profile;
    float confidence;
    int cluesMedia[];
    
    Type(
      String name, int ID, String info, String iconPath, String tenseVerbs[],
      int medium, int focus, int tense, int profile, float confidence,
      int... cluesMedia
    ) {
      this.name = name;
      this.info = info;
      this.icon = Kind.loadImage(iconPath);
      this.ID   = ID  ;
      this.tenseVerbs = tenseVerbs;
      this.medium  = medium ;
      this.focus   = focus  ;
      this.tense   = tense  ;
      this.profile = profile;
      this.confidence = confidence;
      this.cluesMedia = cluesMedia;
      TYPE_B.add(this);
    }
  }
  
  final static String ICON_DIR = "media assets/ability icons/";
  private static Batch <Type> TYPE_B = new Batch();
  final public static Type
    LEAD_SURVEIL_PERSON = new Type(
      "Surveillance", 0,
      "Surveil a suspect for clues to their activities and who they meet with.",
      ICON_DIR+"icon_surveil.png",
      new String[] { "Surveiled", "Surveilling", "Will Surveil" },
      MEDIUM_SURVEIL, FOCUS_PERSON, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, MEDIUM_MEET, MEDIUM_HEIST
    ),
    LEAD_SURVEIL_BUILDING = new Type(
      "Surveillance", 1,
      "Stake out a building to see who visits and who might be holed up.",
      ICON_DIR+"icon_surveil.png",
      new String[] { "Surveiled", "Surveilling", "Will Surveil" },
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_MEET, MEDIUM_HEIST
    ),
    LEAD_QUESTION = new Type(
      "Questioning", 2,
      "Question a suspect for information on past dealings or future plans.",
      ICON_DIR+"icon_question.png",
      new String[] { "Questioned", "Questioning", "Will Question" },
      MEDIUM_QUESTION, FOCUS_PERSON, TENSE_AFTER, PROFILE_HIGH,
      CONFIDENCE_MODERATE, MEDIUM_ANY
    ),
    LEAD_WIRETAP = new Type(
      "Wiretap", 3,
      "Intercept suspicious communications to or from a structure.",
      ICON_DIR+"icon_wiretap.png",
      new String[] { "Wiretapped", "Wiretapping", "Will Wiretap" },
      MEDIUM_WIRE, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, MEDIUM_WIRE
    ),
    LEAD_PATROL = new Type(
      "Patrol", 4,
      "Patrol an area while keeping an eye out for suspicious activity.",
      ICON_DIR+"icon_surveil.png",
      new String[] { "Patrolled", "Patrolling", "Will Patrol" },
      MEDIUM_SURVEIL, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_MEET, MEDIUM_SURVEIL
    ),
    LEAD_SCAN = new Type(
      "Frequency Scan", 5,
      "Scan wireless frequencies in an area for fragments of information.",
      ICON_DIR+"icon_scan.png",
      new String[] { "Scanned", "Scanning", "Will Scan" },
      MEDIUM_WIRE, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_WIRE
    ),
    LEAD_CANVASS = new Type(
      "Canvass", 6,
      "Ask civilians or friendly contacts in an area for leads.",
      ICON_DIR+"icon_question.png",
      new String[] { "Canvassed", "Canvassing", "Will Canvass" },
      MEDIUM_QUESTION, FOCUS_REGION, TENSE_ANY, PROFILE_SUSPICIOUS,
      CONFIDENCE_LOW, MEDIUM_ANY
    ),
    LEAD_SEARCH = new Type(
      "Search", 7,
      "Search a building for records or forensic evidence.",
      ICON_DIR+"icon_search.png",
      new String[] { "Searched", "Searching", "Will Search" },
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_AFTER, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_WIRE, MEDIUM_MEET, MEDIUM_HEIST
    ),
    LEAD_TIPOFF = new Type(
      "Tipoff", 8,
      "_",
      ICON_DIR+"icon_wiretap.png",
      new String[] { "Tipped Off", "Tipping Off", "Will Tip Off" },
      MEDIUM_WIRE, FOCUS_ANY, TENSE_ANY, PROFILE_HIDDEN,
      CONFIDENCE_LOW
    ),
    LEAD_REPORT = new Type(
      "Report", 9,
      "_",
      ICON_DIR+"icon_database.png",
      new String[] { "Reported", "Reporting", "Will Report" },
      MEDIUM_WIRE, FOCUS_ANY, TENSE_ANY, PROFILE_OBVIOUS,
      CONFIDENCE_HIGH
    ),
    LEAD_GUARD = new Type(
      "Guard", 10,
      "Guard this suspect against criminal activity.",
      ICON_DIR+"icon_guard_lead.png",
      new String[] { "Guarded", "Guarding", "Will Guard" },
      MEDIUM_SURVEIL, FOCUS_ANY, TENSE_DURING, PROFILE_OBVIOUS,
      CONFIDENCE_HIGH, MEDIUM_MEET, MEDIUM_HEIST
    ),
    LEAD_TYPES[] = TYPE_B.toArray(Type.class);
  
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Type type;
  final Element focus;
  private String lastContactID;
  private List <Person> onceAssigned = new List();
  
  
  Lead(Base base, Type type, Element focus) {
    super(base, Task.TIME_INDEF);
    
    this.type  = type ;
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
    focus = (Element) s.loadObject();
    lastContactID = s.loadString();
    s.loadObjects(onceAssigned);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveInt(type.ID);
    s.saveObject(focus);
    s.saveString(lastContactID);
    s.saveObjects(onceAssigned);
  }
  
  
  
  /**  Task-associated methods-
    */
  public Element targetElement(Person p) {
    return focus;
  }
  
  
  public void setAssigned(Person p, boolean is) {
    super.setAssigned(p, is);
    if (is) onceAssigned.include(p);
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_LEAD;
  }
  
  
  public Series <Person> onceAssigned() {
    return onceAssigned;
  }
  
  
  
  /**  Utility methods for generating clues particular to a given subject.
    */
  protected Series <Clue> traitClues(
    Step step, int tense, Plot plot, float resultHeat
  ) {
    Batch <Clue> possible = new Batch();
    int time = plot.base.world().timing.totalHours();
    Place place = focus.place();
    
    for (Plot.Role role : step.involved) {
      Element involved = plot.filling(role);
      if (involved == focus) continue;
      
      if (involved.isPerson()) {
        Person p = (Person) involved;
        for (Trait t : Common.PERSON_TRAITS) {
          if (p.stats.levelFor(t) <= 0) continue;
          Clue clue = new Clue(plot, role);
          clue.assignEvidence(p, t, this, time, place);
          possible.add(clue);
        }
      }
      
      if (involved.isPlace()) {
        Place p = (Place) involved;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue clue = new Clue(plot, role);
          clue.assignEvidence(p, t, this, time, place);
          possible.add(clue);
        }
      }
      
      if (involved.isItem()) {
        Item p = (Item) involved;
        Clue clue = new Clue(plot, role);
        clue.confirmMatch(p, this, time, place);
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
    Place place = focus.place();
    
    for (Plot.Role role : step.involved) {
      Element involved = plot.filling(role);
      if (involved == focus) continue;
      
      Element p = (Element) involved;
      Region at = involved.region();
      int range = Rand.yes() ? 0 : 1;
      Series <Region> around = base.world().regionsInRange(at, range);
      
      Region near = (Region) Rand.pickFrom(around);
      Clue clue = new Clue(plot, role);
      clue.assignNearbyRegion(p, near, range, this, time, place);
      possible.add(clue);
    }
    
    return possible;
  }
  
  
  protected Batch <Clue> intentClues(
    Plot plot
  ) {
    Batch <Clue> possible = new Batch();
    int time = plot.base.world().timing.totalHours();
    Place place = focus.place();
    
    Step heist = plot.heistStep();
    int heistTime = plot.stepTimeScheduled(heist);
    PlotType heistType = (PlotType) plot.type;
    
    Clue forHeist = new Clue(plot, Plot.ROLE_OBJECTIVE);
    forHeist.confirmHeistDetails(heistType, heistTime, this, time, place);
    possible.add(forHeist);
    
    return possible;
  }
  
  
  protected void confirmIdentity(
    Plot plot, Plot.Role role, int time, Place place
  ) {
    Element subject = plot.filling(role);
    CaseFile file = base.leads.caseFor(subject);
    Clue clue = new Clue(plot, role);
    clue.confirmMatch(subject, this, time, place);
    file.recordClue(clue);
    
    if (role == Plot.ROLE_TARGET) {
      CaseFile forPlot = base.leads.caseFor(plot);
      for (Clue intent : intentClues(plot)) forPlot.recordClue(intent);
    }
  }
  
  
  
  /**  Generation and screening of clues related to the case:
    */
  protected boolean canDetect(
    Step step, int tense, Plot plot, int time
  ) {
    //
    //  First check the tense-
    if (tense != TENSE_ANY && type.tense != TENSE_ANY && tense != type.tense) {
      return false;
    }
    if (tense == TENSE_AFTER) {
      if (step.timeStart < 0) return false;
      int timeFromEnd = step.timeStart + step.timeTaken - time;
      if (timeFromEnd >= CLUE_EXPIRATION_TIME) return false;
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
    for (Plot.Role role : step.involved) {
      Element contacts = plot.filling(role);
      
      if (focus.isRegion()) {
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
  
  
  protected float attemptFollow(
    Step step, int tense, Plot plot, Series <Person> follow, int time
  ) {
    //
    //  First, check to see whether anything has actually changed here (i.e,
    //  avoid granting cumulative 'random' info over time.)  If it hasn't,
    //  just return.
    String contactID = plot.caseID+"_"+step.ID+"_"+tense;
    if (contactID.equals(lastContactID)) {
      return RESULT_NONE;
    }
    I.say("\nNew contact: "+contactID);
    this.lastContactID = contactID;
    //
    //  Then perform the actual skill-test needed to ensure success:
    attempt = configAttempt(follow);
    int outcome = attempt.performAttempt(2);
    float result = (outcome == 2) ? RESULT_HOT : RESULT_PARTIAL;
    Place place = focus.place();
    //
    //  If you're on fire at the moment, you can get direct confirmation for
    //  the identity of the participant/s, and any information or payload they
    //  may have relayed to eachother.
    if (result >= RESULT_HOT) {
      for (Plot.Role role : step.involved) {
        confirmIdentity(plot, role, time, place);
      }
      if (step.infoGiven != null) {
        confirmIdentity(plot, step.infoGiven, time, place);
      }
    }
    //
    //  If you're only partly successful, you might still get a glimpse of the
    //  other parties or have a rough idea of where the meeting took place.
    else if (result >= RESULT_PARTIAL) {
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
    //
    //  Either way, you have to take the risk of tipping off the perps
    //  themselves:
    plot.takeSpooking(type.profile);
    return result;
  }
  
  
  protected Attempt configAttempt(Series <Person> attempting) {
    Trait skill = null;
    int range = 5, obstacle = 0;
    Person perp = focus.isPerson() ? ((Person) focus) : null;
    Place  site = focus.isPlace () ? ((Place ) focus) : null;
    Region area = focus.isRegion() ? ((Region) focus) : null;
    
    if (type.medium == MEDIUM_SURVEIL) {
      skill = SIGHT_RANGE;
      range = 5;
      
      if (perp != null) obstacle = perp.stats.levelFor(HIDE_RANGE);
      if (site != null) obstacle = 2;
      if (area != null) obstacle = 4;
    }
    
    if (type.medium == MEDIUM_WIRE) {
      skill = ENGINEERING;
      range = 10;
      
      if (perp != null) obstacle = perp.stats.levelFor(ENGINEERING);
      if (site != null) obstacle = 5;
      if (area != null) obstacle = 10;
    }
    
    if (type.medium == MEDIUM_QUESTION) {
      skill = QUESTION;
      range = 10;
      
      if (perp != null) obstacle = perp.stats.levelFor(PERSUADE);
      if (site != null) obstacle = -1;
      if (area != null) obstacle = 10;
    }
    
    if (type.medium == MEDIUM_COVER) {
      skill = PERSUADE;
      range = 10;
      
      if (perp != null) obstacle = perp.stats.levelFor(SIGHT_RANGE) * 2;
      if (site != null) obstacle = 10;
      if (area != null) obstacle = 10;
    }
    
    Attempt attempt = new Attempt(this);
    attempt.addTest(skill, range, obstacle);
    attempt.setAssigned(attempting);
    return attempt;
  }
  
  
  protected Scene tryInterruptHeist(
    Step step, int tense, Plot plot, int time
  ) {
    if (tense       != TENSE_DURING) return null;
    if (step.medium != MEDIUM_HEIST) return null;
    return plot.generateScene(step, focus, this);
  }
  
  
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    Series <Person> active = active();
    World world = base.world();
    int time = world.timing.totalHours();
    //
    //  Continuously monitor for events of interest connected to the current
    //  focus of your investigation, and see if any new information pops up...
    for (Event event : world.events.active()) if (event.isPlot()) {
      Plot plot = (Plot) event;
      for (Step step : plot.allSteps()) {
        int tense = plot.stepTense(step);
        if (canDetect(step, tense, plot, time)) {
          Scene scene = tryInterruptHeist(step, tense, plot, time);
          if (scene != null) {
            base.world().enterScene(scene);
          }
          else {
            attemptFollow(step, tense, plot, active, time);
          }
        }
      }
    }
    //
    //  Including past events (up to a certain expiration date)-
    for (Event event : world.events.past()) if (event.isPlot()) {
      Plot plot = (Plot) event;
      for (Step step : plot.allSteps()) {
        if (canDetect(step, TENSE_AFTER, plot, time)) {
          attemptFollow(step, TENSE_AFTER, plot, active, time);
        }
      }
    }
    return true;
  }
  
  
  public void onSceneExit(Scene scene, EventEffects report) {
    setCompleted(report.playerWon());
  }
  
  
  protected void onCompletion() {
    super.onCompletion();
    base.leads.closeLead(this);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String activeInfo() {
    return type.tenseVerbs[1]+" "+focus;
  }
  
  
  public String testInfo(Person agent) {
    String info = super.testInfo(agent);
    StringBuffer s = new StringBuffer(type.info+"\n");
    s.append(info);
    
    String profileDesc = PROFILE_DESC[type.profile];
    int confIndex = (int) (type.confidence * 3);
    String confDesc = CONFIDENCE_DESC[Nums.clamp(confIndex, 3)];
    
    s.append("\n  Conspicuousness: "+profileDesc);
    s.append("\n  Evidence Level: "+confDesc);
    return s.toString();
  }
  
  
  public String helpInfo() {
    return type.info;
  }
  
  
  public Image icon() {
    return type.icon;
  }
  
  
  public String choiceInfo(Person p) {
    return "Conduct "+type.name;
  }
}



