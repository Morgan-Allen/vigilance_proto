

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.base.*;
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
    MEDIUM_ASSAULT  =  4,
    MEDIUM_QUESTION =  5,
    MEDIUM_COVER    =  6,
    MEDIUM_ANY      = -1,
    PHYSICAL_MEDIA[] = { 1, 3, 4, 5, 6 },
    SOCIAL_MEDIA  [] = { 1, 5, 6 },
    WIRED_MEDIA   [] = { 2 },
    FORENSIC_MEDIA[] = { 1, 2, 4, 5 },
    //  Whether this lead can pick up on past/present/future contacts-
    TENSE_NONE      = -2,
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
    //  Time taken to gather information-
    TIME_NONE            = 0,
    TIME_SHORT           = World.HOURS_PER_DAY / 8,
    TIME_MEDIUM          = World.HOURS_PER_DAY / 2,
    TIME_LONG            = World.HOURS_PER_DAY * 2,
    CLUE_EXPIRATION_TIME = World.HOURS_PER_DAY * World.DAYS_PER_WEEK;
  
  final public static String
    MEDIUM_DESC[] = {null,
      "Meet", "Wire", "Surveil", "Heist", "Question"
    },
    TENSE_DESC[] = {
      "Before", "During", "After"
    },
    PROFILE_DESC[] = {
      "Nil", "Low", "Moderate", "High", "BLATANT"
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
    
    final public String name, info, tenseVerbs[];
    final public Image icon;
    
    final public int ID;
    final public int minHours;
    final public int medium, focus, tense, profile;
    final public float confidence;
    final public int cluesMedia[];
    
    Type(
      String name, int ID, String info, String iconPath, String tenseVerbs[],
      int minHours, int medium, int focus, int tense, int profile,
      float confidence, int... cluesMedia
    ) {
      this.name = name;
      this.info = info;
      this.icon = Kind.loadImage(iconPath);
      this.ID   = ID  ;
      this.tenseVerbs = tenseVerbs;
      this.minHours   = minHours  ;
      this.medium     = medium    ;
      this.focus      = focus     ;
      this.tense      = tense     ;
      this.profile    = profile   ;
      this.confidence = confidence;
      this.cluesMedia = cluesMedia;
      TYPE_B.add(this);
    }
    
    public String toString() {
      return name;
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
      TIME_SHORT,
      MEDIUM_SURVEIL, FOCUS_PERSON, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, PHYSICAL_MEDIA
    ),
    LEAD_SURVEIL_BUILDING = new Type(
      "Surveillance", 1,
      "Stake out a building to see who visits and who might be holed up.",
      ICON_DIR+"icon_surveil.png",
      new String[] { "Surveiled", "Surveilling", "Will Surveil" },
      TIME_SHORT,
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, PHYSICAL_MEDIA
    ),
    LEAD_QUESTION = new Type(
      "Questioning", 2,
      "Question a suspect for information on past dealings or future plans.",
      ICON_DIR+"icon_question.png",
      new String[] { "Questioned", "Questioning", "Will Question" },
      TIME_SHORT,
      MEDIUM_QUESTION, FOCUS_PERSON, TENSE_AFTER, PROFILE_HIGH,
      CONFIDENCE_MODERATE, MEDIUM_ANY
    ),
    LEAD_WIRETAP = new Type(
      "Wiretap", 3,
      "Intercept suspicious communications to or from a structure.",
      ICON_DIR+"icon_wiretap.png",
      new String[] { "Wiretapped", "Wiretapping", "Will Wiretap" },
      TIME_MEDIUM,
      MEDIUM_WIRE, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, WIRED_MEDIA
    ),
    LEAD_PATROL = new Type(
      "Patrol", 4,
      "Patrol an area while keeping an eye out for suspicious activity.",
      ICON_DIR+"icon_surveil.png",
      new String[] { "Patrolled", "Patrolling", "Will Patrol" },
      TIME_MEDIUM,
      MEDIUM_SURVEIL, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, PHYSICAL_MEDIA
    ),
    LEAD_SCAN = new Type(
      "Frequency Scan", 5,
      "Scan wireless frequencies in an area for fragments of information.",
      ICON_DIR+"icon_scan.png",
      new String[] { "Scanned", "Scanning", "Will Scan" },
      TIME_MEDIUM,
      MEDIUM_WIRE, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, WIRED_MEDIA
    ),
    LEAD_CANVASS = new Type(
      "Canvass", 6,
      "Ask civilians or friendly contacts in an area for leads.",
      ICON_DIR+"icon_question.png",
      new String[] { "Canvassed", "Canvassing", "Will Canvass" },
      TIME_LONG,
      MEDIUM_QUESTION, FOCUS_REGION, TENSE_ANY, PROFILE_SUSPICIOUS,
      CONFIDENCE_LOW, MEDIUM_ANY
    ),
    LEAD_SEARCH = new Type(
      "Search", 7,
      "Search a building for logs, records or forensic evidence.",
      ICON_DIR+"icon_search.png",
      new String[] { "Searched", "Searching", "Will Search" },
      TIME_SHORT,
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_AFTER, PROFILE_SUSPICIOUS,
      CONFIDENCE_MODERATE, FORENSIC_MEDIA
    ),
    LEAD_TIPOFF = new Type(
      "Tipoff", 8,
      "_",
      ICON_DIR+"icon_wiretap.png",
      new String[] { "Tipped Off", "Tipping Off", "Will Tip Off" },
      TIME_NONE,
      MEDIUM_WIRE, FOCUS_ANY, TENSE_ANY, PROFILE_HIDDEN,
      CONFIDENCE_LOW
    ),
    LEAD_REPORT = new Type(
      "Report", 9,
      "_",
      ICON_DIR+"icon_database.png",
      new String[] { "Reported", "Reporting", "Will Report" },
      TIME_NONE,
      MEDIUM_WIRE, FOCUS_ANY, TENSE_ANY, PROFILE_OBVIOUS,
      CONFIDENCE_HIGH
    ),
    LEAD_GUARD = new Type(
      "Guard", 10,
      "Guard this suspect against criminal activity.",
      ICON_DIR+"icon_guard_lead.png",
      new String[] { "Guarded", "Guarding", "Will Guard" },
      TIME_SHORT,
      MEDIUM_ASSAULT, FOCUS_ANY, TENSE_DURING, PROFILE_OBVIOUS,
      CONFIDENCE_HIGH, PHYSICAL_MEDIA
    ),
    LEAD_BUST = new Type(
      "Bust", 11,
      "Bust down the doors and unleash hell.",
      ICON_DIR+"icon_guard_lead.png",
      new String[] { "Busted", "Busting", "Will Bust" },
      TIME_SHORT,
      MEDIUM_ASSAULT, FOCUS_ANY, TENSE_DURING, PROFILE_OBVIOUS,
      CONFIDENCE_HIGH, MEDIUM_ANY
    ),
    LEAD_TYPES[] = TYPE_B.toArray(Type.class);
  
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Type type;
  final public Element focus;
  
  private String lastContactID = "___";
  private int contactTime = -1;
  private List <Person> onceActive = new List();
  
  public float leadRating = 0;
  public float setResult = -1;
  public boolean noScene = false;
  
  
  
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
    type          = LEAD_TYPES[s.loadInt()];
    focus         = (Element) s.loadObject();
    lastContactID = s.loadString();
    contactTime   = s.loadInt();
    s.loadObjects(onceActive);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveInt   (type.ID      );
    s.saveObject(focus        );
    s.saveString(lastContactID);
    s.saveInt   (contactTime  );
    s.saveObjects(onceActive);
  }
  
  
  
  /**  Task-associated methods-
    */
  public Element targetElement(Person p) {
    return focus;
  }
  
  
  public void setAssigned(Person p, boolean is) {
    super.setAssigned(p, is);
    onceActive.include(p);
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_LEAD;
  }
  
  
  public Series <Person> onceActive() {
    return onceActive;
  }
  
  
  
  /**  Extraction and screening of clues related to the case:
    */
  public boolean canDetect(
    Step step, int tense, Plot plot, int time
  ) {
    //
    //  First check the tense-
    if (tense != TENSE_ANY && type.tense != TENSE_ANY && tense != type.tense) {
      return false;
    }
    if (tense == TENSE_AFTER) {
      int start = plot.startTime(step);
      if (start < 0) return false;
      int timeFromEnd = time - (start + step.hoursTaken);
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
    for (Element contacts : plot.involved(step)) {
      if (focus.isPerson()) {
        if (contacts != focus) continue;
      }
      if (focus.isPlace()) {
        if (contacts != focus) continue;
      }
      if (focus.isRegion()) {
        if (! contacts.isPlace()) continue;
        if (contacts.region() != focus) continue;
      }
      matchFocus = true;
    }
    if (! matchFocus) return false;
    //
    //  Then return true-
    return true;
  }
  
  
  protected float recognition(int attemptResult, Place scene, Element e) {
    //
    //  We assign a higher probability of recognition if the suspect is
    //  present on-site, if the skill-test went well, and based on a random
    //  roll.
    float recognition;
    if (setResult > 0) {
      recognition = setResult;
    }
    else {
      recognition = e == null ? 0 : (e.place() == scene ? 0.5f : -0.5f);
      recognition = (recognition + attemptResult + Rand.num()) / 3;
    }
    return recognition;
  }
  
  
  protected float attemptFollow(
    Step step, int tense, Plot plot, Series <Person> follow, int time
  ) {
    //
    //  First, check to see whether anything has actually changed here (i.e,
    //  avoid granting cumulative 'random' info over time.)  If it hasn't,
    //  just return.
    String contactID = plot.eventID+"_"+step.uniqueID()+"_"+tense;
    if (contactTime == -1 && ! contactID.equals(lastContactID)) {
      lastContactID = contactID;
      contactTime   = time;
    }
    if (contactTime == -1 || (time - contactTime) < type.minHours) {
      return RESULT_NONE;
    }
    //
    //  Then perform the actual skill-test needed to ensure success:
    attempt = configAttempt(follow);
    CaseFile file = base.leads.caseFor(plot);
    Place scene = focus.place();
    int outcome = setResult > 0 ? 1 : attempt.performAttempt(1);
    //
    //  And iterate over over all the elements involved to generate suitable
    //  clues:
    for (Element e : plot.involved(step)) {
      float recognition = recognition(outcome, scene, e);
      Role role = plot.roleFor(e);
      //
      //  If recognition is strong, we get an exact confirmation of the role of
      //  the suspect and their current location.  If it's weaker, we get a
      //  partial clue, and if it's weaker still, we get no clue at all.
      if (recognition > 0.66f) {
        Clue confirms = Clue.confirmSuspect(plot, role, e, e.place());
        file.recordClue(confirms, this, time, scene);
      }
      else if (recognition > 0.33f) {
        Series <Clue> possible = step.possibleClues(plot, e, this);
        Clue gained = (Clue) Rand.pickFrom(possible);
        if (gained != null) file.recordClue(gained, this, time, scene);
      }
      else {
        continue;
      }
    }
    //
    //  We check separately for any element whose role is mentioned-
    float recogMention = recognition(outcome, scene, null);
    if (step.mentions != null && recogMention > 0.5f) {
      Element match = plot.filling(step.mentions);
      Clue confirms = Clue.confirmSuspect(plot, step.mentions, match);
      file.recordClue(confirms, this, time, scene);
    }
    //
    //  And for the overall objective of the plot-
    float recogAim = recognition(outcome, scene, null);
    if (step.canLeakAim() && recogAim > 0.5f) {
      Clue confirms = Clue.confirmAim(plot);
      file.recordClue(confirms, this, time, scene);
    }
    //
    //  Either way, you have to take the risk of tipping off the perps
    //  themselves:
    if (setResult == -1) plot.takeSpooking(type.profile);
    //
    //  Reset for the next contact and return your result.
    contactTime = -1;
    return outcome;
  }
  
  
  protected Attempt configAttempt(Series <Person> attempting) {
    Trait skill = null;
    int range = 5, obstacle = 0;
    Person perp = focus.isPerson() ? ((Person) focus) : null;
    Place  site = focus.isPlace () ? ((Place ) focus) : null;
    Region area = focus.isRegion() ? ((Region) focus) : null;
    
    if (type.medium == MEDIUM_SURVEIL) {
      skill = SIGHT_RANGE;
      range = 10;
      
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
    
    if (perp != null) {
      obstacle *= (perp.base().organisationRank(perp) + 2) / 4f;
    }
    if (site != null) {
      obstacle *= (site.owner().organisationRank(site) + 2) / 4f;
    }
    if (area != null) {
      obstacle *= 1f;
    }
    
    Attempt attempt = new Attempt(this);
    attempt.addTest(skill, range, obstacle);
    attempt.setAssigned(attempting);
    return attempt;
  }
  
  
  protected Scene enteredScene(Step step, int tense, Plot plot, int time) {
    if (noScene || tense != TENSE_DURING) {
      return null;
    }
    if (step.medium != MEDIUM_ASSAULT && type.medium != MEDIUM_ASSAULT) {
      return null;
    }
    return plot.generateScene(step, focus, this);
  }
  
  
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    Series <Person> active = active();
    World world  = base.world();
    int   time   = world.timing.totalHours();
    float result = RESULT_NONE;
    //
    //  Continuously monitor for events of interest connected to the current
    //  focus of your investigation, and see if any new information pops up...
    for (Event event : world.events.active()) if (event.isPlot()) {
      Plot plot = (Plot) event;
      for (Step step : plot.allSteps()) {
        int tense = plot.tense(step);
        if (canDetect(step, tense, plot, time)) {
          Scene scene = enteredScene(step, tense, plot, time);
          if (scene != null) {
            MessageUtils.presentBustMessage(world.view(), scene, this, plot);
            base.world().enterScene(scene);
          }
          else {
            result = attemptFollow(step, tense, plot, active, time);
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
          result = attemptFollow(step, TENSE_AFTER, plot, active, time);
        }
      }
    }
    //
    //  Remember to close the lead once one or more clues have been extracted.
    if (result != RESULT_NONE) setCompleted(result > RESULT_COLD);
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
    s.append("\n  Evidence Level:  "+confDesc   );
    return s.toString();
  }
  
  
  public String helpInfo() {
    return type.info;
  }
  
  
  public Image icon() {
    return type.icon;
  }
  
  
  public String choiceInfo(Person p) {
    return type.name+" ("+focus+")";
  }
}



