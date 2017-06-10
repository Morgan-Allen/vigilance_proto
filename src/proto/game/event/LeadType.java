
package proto.game.event;
import proto.common.*;
import proto.game.person.Attempt;
import proto.game.person.Person;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;

import java.awt.Image;





public class LeadType extends Index.Entry implements Session.Saveable {
  
  
  /**  Static constants
    */
  final public static int
    //  The method by which a lead can be followed (and which contacts it can
    //  pick up on)-
    MEDIUM_WIRE     =  1,
    MEDIUM_SURVEIL  =  2,
    MEDIUM_SEARCH   =  3,
    MEDIUM_MEETING  =  4,
    MEDIUM_ASSAULT  =  5,
    MEDIUM_NONE     = -1,
    //  Whether this lead can pick up on past/present/future contacts-
    TENSE_NONE      = -2,
    TENSE_PAST      =  0,
    TENSE_PRESENT   =  1,
    TENSE_FUTURE    =  2,
    TENSE_ANY       = -1,
    //  How likely following a lead is to spook the perpetrator-
    PROFILE_HIDDEN     = 0,
    PROFILE_LOW        = 1,
    PROFILE_SUSPICIOUS = 2,
    PROFILE_HIGH       = 3,
    PROFILE_OBVIOUS    = 4,
    //  Degrees of success in investigation-
    RESULT_NONE    = -1,
    RESULT_COLD    =  0,
    RESULT_PARTIAL =  1,
    RESULT_HOT     =  2;
  final public static float
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
      "Wire", "Surveil", "Search", "Meeting", "Assault", "None"
    },
    TENSE_DESC[] = {
      "Past", "Present", "Future"
    },
    PROFILE_DESC[] = {
      "Nil", "Low", "Moderate", "High", "BLATANT"
    },
    CONFIDENCE_DESC[] = {
      "Weak", "Fair", "Strong"
    };
  
  
  final static Index <LeadType> INDEX = new Index();
  
  final static String ICON_DIR = "media assets/ability icons/";
  
  final public static LeadType
    SURVEIL = new LeadType(
      "Surveillance", "lead_surveil",
      "Stake out the premises to see who visits and who might be holed up.",
      ICON_DIR+"icon_surveil.png",
      TIME_SHORT, MEDIUM_SURVEIL, PROFILE_LOW, CONFIDENCE_MODERATE
    ),
    QUESTIONING = new LeadType(
      "Questioning", "lead_questioning",
      "Question a suspect for information on past dealings or future plans.",
      ICON_DIR+"icon_question.png",
      TIME_SHORT, MEDIUM_MEETING, PROFILE_HIGH, CONFIDENCE_MODERATE
    ),
    WIRETAP = new LeadType(
      "Wiretap", "lead_wiretap",
      "Intercept suspicious communications to or from a structure.",
      ICON_DIR+"icon_wiretap.png",
      TIME_MEDIUM, MEDIUM_WIRE, PROFILE_LOW, CONFIDENCE_HIGH
    ),
    SEARCH = new LeadType(
      "Search", "lead_search",
      "Search a building for logs, records or forensic evidence.",
      ICON_DIR+"icon_search.png",
      TIME_SHORT, MEDIUM_SEARCH, PROFILE_SUSPICIOUS, CONFIDENCE_MODERATE
    ),
    TIPOFF = new LeadType(
      "Tipoff", "lead_tipoff",
      "_",
      ICON_DIR+"icon_wiretap.png",
      TIME_NONE, MEDIUM_NONE, PROFILE_HIDDEN, CONFIDENCE_LOW
    ),
    REPORT = new LeadType(
      "Report", "lead_report",
      "_",
      ICON_DIR+"icon_database.png",
      TIME_NONE, MEDIUM_NONE, PROFILE_OBVIOUS, CONFIDENCE_HIGH
    ),
    GUARD = new LeadType(
      "Guard", "lead_guard",
      "Guard this suspect against criminal activity.",
      ICON_DIR+"icon_guard_lead.png",
      TIME_SHORT, MEDIUM_ASSAULT, PROFILE_OBVIOUS, CONFIDENCE_HIGH
    ),
    BUST = new LeadType(
      "Bust", "lead_bust",
      "Bust down the doors and unleash hell.",
      ICON_DIR+"icon_guard_lead.png",
      TIME_SHORT, MEDIUM_ASSAULT, PROFILE_OBVIOUS, CONFIDENCE_HIGH
    ),
    
    STANDARD_LEADS[] = { SURVEIL, SEARCH, WIRETAP, QUESTIONING },
    OTHER_LEADS   [] = { TIPOFF, REPORT, GUARD, BUST };
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public String name, info;
  final public Image icon;
  
  final public int minHours;
  final public int medium, profile;
  final public float confidence;
  
  
  LeadType(
    String name, String ID,
    String info, String iconPath,
    int minHours, int medium, int profile,
    float confidence
  ) {
    super(INDEX, ID);
    this.name = name;
    this.info = info;
    this.icon = Kind.loadImage(iconPath);
    this.minHours   = minHours  ;
    this.medium     = medium    ;
    this.profile    = profile   ;
    this.confidence = confidence;
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public static LeadType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  
  /**  Tests and configuration:
    */
  //  NOTE:  This is a general summary of the behaviour of the various lead-
  //  types and the information they can disclose.
  //
  //                   Attach   Reveal
  //
  //  Wiretap Person   X        X
  //  Wiretap Place    Yes      Location "IP Tracing", Trait "Acoustics"
  //  Wiretap Thing    X        X
  //  Wiretap Detect   During, After (if advanced), Only Wired
  //
  //  Surveil Person   Yes      Location, Trait
  //  Surveil Place    Yes      Location, Trait
  //  Surveil Thing    Yes      Location, Trait
  //  Surveil Detect   During, All except Wired
  //
  //  Search Person    X        Trait "Chem analysis/DNA/fibres", per item
  //  Search Place     Yes      Trait "Chem analysis/DNA/fibres", per item
  //  Search Thing     Yes      Trait "Chem analysis/DNA/fibres", per item
  //  Search Detect    After, All except Wired
  //
  //  Meeting Person   Yes      Location, Trait
  //  Meeting Place    X        Location, Trait
  //  Meeting Thing    X        Location, Trait
  //  Meeting Detect   Before (if perp), During, After, All actions
  //
  //  TODO:  What about Aims or mentioned elements?  Or motives?
  
  
  public boolean canFollow(
    Element target
  ) {
    return target.isPlace();
    /*
    if (medium == MEDIUM_WIRE) {
      if (target.isPlace()) return true;
    }
    if (medium == MEDIUM_SURVEIL) {
      return true;
    }
    if (medium == MEDIUM_SEARCH) {
      if (target.isPlace()) return true;
      if (target.isItem ()) return true;
    }
    if (medium == MEDIUM_MEETING) {
      if (target.isPerson()) {
        return ((Person) target).isCivilian();
      }
    }
    
    if (medium == MEDIUM_NONE || medium == MEDIUM_ASSAULT) {
      return true;
    }
    return false;
    //*/
  }
  
  
  public boolean canDetect(
    Element involved, Plot plot, Element focus
  ) {
    int     tense  = plot.tense();
    Role    roleE  = plot.roleFor(involved);
    Role    roleF  = plot.roleFor(focus);
    boolean active = roleE != null;
    boolean wired  = true;
    
    if (medium == MEDIUM_WIRE) {
      if ((! wired) || (! active)) return false;
      if (tense == TENSE_PAST   ) return true;
      if (tense == TENSE_PRESENT) return true;
    }
    if (medium == MEDIUM_SURVEIL) {
      if (wired || (! active)) return false;
      if (tense == TENSE_PRESENT) return true;
    }
    if (medium == MEDIUM_SEARCH) {
      boolean isHome = false;
      if (involved.isPerson()) isHome = ((Person) involved).resides() == focus;
      if (wired || ((! active) && (! isHome))) return false;
      if (tense == TENSE_PAST) return true;
    }
    if (medium == MEDIUM_MEETING) {
      //  TODO:  This might have to be turned into something that queries all
      //         residents.
      boolean known = false;// focus.history.bondWith(involved) > 0;
      boolean perp  = active && roleE.isPerp();
      if ((! known) && (! active)) return false;
      if (tense == TENSE_PAST          ) return true;
      if (tense == TENSE_PRESENT       ) return true;
      if (tense == TENSE_FUTURE && perp) return true;
    }
    
    if (medium == MEDIUM_NONE || medium == MEDIUM_ASSAULT) {
      return false;
    }
    
    return false;
  }
  
  
  public boolean canProvide(
    Clue clue, Element involved, Element focus
  ) {
    boolean trait    = clue.isTraitClue   ();
    boolean location = clue.isLocationClue();
    
    if (medium == MEDIUM_WIRE) {
      if (involved.isPlace()) return trait || location;
    }
    if (medium == MEDIUM_SURVEIL) {
      return trait || location;
    }
    if (medium == MEDIUM_SEARCH) {
      return trait;
    }
    if (medium == MEDIUM_MEETING) {
      return trait || location;
    }
    
    if (medium == MEDIUM_NONE || medium == MEDIUM_ASSAULT) {
      return true;
    }
    
    return false;
  }
  
  
  
  /**  Configuring the skill-tests associated with following a lead:
    */
  public Attempt configFollowAttempt(
    Element focus, Lead lead, Series <Person> attempting
  ) {
    Trait skill = null;
    int range = 5, obstacle = 0;
    Person perp = focus.isPerson() ? ((Person) focus) : null;
    Place  site = focus.isPlace () ? ((Place ) focus) : null;
    Region area = focus.isRegion() ? ((Region) focus) : null;
    Base   base = focus.base();
    
    if (medium == MEDIUM_SURVEIL) {
      skill = SIGHT_RANGE;
      range = 10;
      
      if (perp != null) obstacle = perp.stats.levelFor(HIDE_RANGE);
      if (site != null) obstacle = 2;
      if (area != null) obstacle = 4;
    }
    
    if (medium == MEDIUM_SEARCH) {
      skill = SIGHT_RANGE;
      range = 10;
      
      if (perp != null) obstacle = perp.stats.levelFor(HIDE_RANGE);
      if (site != null) obstacle = 2;
      if (area != null) obstacle = 4;
    }
    
    if (medium == MEDIUM_WIRE) {
      skill = ENGINEERING;
      range = 10;
      
      if (perp != null) obstacle = perp.stats.levelFor(ENGINEERING);
      if (site != null) obstacle = 5;
      if (area != null) obstacle = 10;
    }
    
    if (medium == MEDIUM_MEETING) {
      skill = QUESTION;
      range = 10;
      
      if (perp != null) obstacle = perp.stats.levelFor(PERSUADE);
      if (site != null) obstacle = -1;
      if (area != null) obstacle = 10;
    }
    
    if (base != null && (focus == base.HQ() || focus == base.leader())) {
      obstacle *= 2;
    }
    else if (perp != null && perp.isCriminal()) {
      obstacle *= 1.5f;
    }
    
    Attempt attempt = new Attempt(lead);
    attempt.addTest(skill, range, obstacle);
    attempt.setAssigned(attempting);
    return attempt;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
  
  
  public String verbName(Lead lead, Clue clue) {
    boolean location = clue.isLocationClue();
    boolean trait    = clue.isTraitClue   ();
    
    if (medium == MEDIUM_WIRE) {
      if (location) return "Wiretap IP Tracing";
      if (trait   ) return "Wiretap Acoustics" ;
    }
    if (medium == MEDIUM_SURVEIL) {
      return "Surveillance";
    }
    if (medium == MEDIUM_SEARCH) {
      if (trait) {
        String traceDesc  = "Traces";
        String personDesc = Common.PERSON_TRACE_NAMES.get(clue.trait);
        String venueDesc  = Common.VENUE_TRACE_NAMES .get(clue.trait);
        if (personDesc != null) traceDesc = personDesc;
        if (venueDesc  != null) traceDesc = venueDesc ;
        return traceDesc+" found during Search";
      }
    }
    if (medium == MEDIUM_MEETING) {
      return "Questioning";
    }
    
    return name;
  }
}








