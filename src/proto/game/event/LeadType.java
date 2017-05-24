
package proto.game.event;
import proto.common.*;
import proto.util.*;
import static proto.game.event.Lead.*;

import java.awt.Image;





public class LeadType extends Index.Entry implements Session.Saveable {
  
  
  final static Index <LeadType> INDEX = new Index();

  final static String ICON_DIR = "media assets/ability icons/";
  
  final public static LeadType
    SURVEIL_PERSON = new LeadType(
      "Surveillance", "lead_surveil_person",
      "Surveil a suspect for clues to their activities and who they meet with.",
      ICON_DIR+"icon_surveil.png",
      new String[] { "Surveiled", "Surveilling", "Will Surveil" },
      TIME_SHORT,
      MEDIUM_SURVEIL, FOCUS_PERSON, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, PHYSICAL_MEDIA
    ),
    SURVEIL_BUILDING = new LeadType(
      "Surveillance", "lead_surveil_building",
      "Stake out a building to see who visits and who might be holed up.",
      ICON_DIR+"icon_surveil.png",
      new String[] { "Surveiled", "Surveilling", "Will Surveil" },
      TIME_SHORT,
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, PHYSICAL_MEDIA
    ),
    QUESTION = new LeadType(
      "Questioning", "lead_question",
      "Question a suspect for information on past dealings or future plans.",
      ICON_DIR+"icon_question.png",
      new String[] { "Questioned", "Questioning", "Will Question" },
      TIME_SHORT,
      MEDIUM_QUESTION, FOCUS_PERSON, TENSE_AFTER, PROFILE_HIGH,
      CONFIDENCE_MODERATE, MEDIUM_ANY
    ),
    WIRETAP = new LeadType(
      "Wiretap", "lead_wiretap",
      "Intercept suspicious communications to or from a structure.",
      ICON_DIR+"icon_wiretap.png",
      new String[] { "Wiretapped", "Wiretapping", "Will Wiretap" },
      TIME_MEDIUM,
      MEDIUM_WIRE, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, WIRED_MEDIA
    ),
    PATROL = new LeadType(
      "Patrol", "lead_patrol",
      "Patrol an area while keeping an eye out for suspicious activity.",
      ICON_DIR+"icon_surveil.png",
      new String[] { "Patrolled", "Patrolling", "Will Patrol" },
      TIME_MEDIUM,
      MEDIUM_SURVEIL, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, PHYSICAL_MEDIA
    ),
    SCAN = new LeadType(
      "Frequency Scan", "lead_scan",
      "Scan wireless frequencies in an area for fragments of information.",
      ICON_DIR+"icon_scan.png",
      new String[] { "Scanned", "Scanning", "Will Scan" },
      TIME_MEDIUM,
      MEDIUM_WIRE, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, WIRED_MEDIA
    ),
    CANVASS = new LeadType(
      "Canvass", "lead_canvass",
      "Ask civilians or friendly contacts in an area for leads.",
      ICON_DIR+"icon_question.png",
      new String[] { "Canvassed", "Canvassing", "Will Canvass" },
      TIME_LONG,
      MEDIUM_QUESTION, FOCUS_REGION, TENSE_ANY, PROFILE_SUSPICIOUS,
      CONFIDENCE_LOW, MEDIUM_ANY
    ),
    SEARCH = new LeadType(
      "Search", "lead_search",
      "Search a building for logs, records or forensic evidence.",
      ICON_DIR+"icon_search.png",
      new String[] { "Searched", "Searching", "Will Search" },
      TIME_SHORT,
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_AFTER, PROFILE_SUSPICIOUS,
      CONFIDENCE_MODERATE, FORENSIC_MEDIA
    ),
    TIPOFF = new LeadType(
      "Tipoff", "lead_tipoff",
      "_",
      ICON_DIR+"icon_wiretap.png",
      new String[] { "Tipped Off", "Tipping Off", "Will Tip Off" },
      TIME_NONE,
      MEDIUM_WIRE, FOCUS_ANY, TENSE_ANY, PROFILE_HIDDEN,
      CONFIDENCE_LOW
    ),
    REPORT = new LeadType(
      "Report", "lead_report",
      "_",
      ICON_DIR+"icon_database.png",
      new String[] { "Reported", "Reporting", "Will Report" },
      TIME_NONE,
      MEDIUM_WIRE, FOCUS_ANY, TENSE_ANY, PROFILE_OBVIOUS,
      CONFIDENCE_HIGH
    ),
    GUARD = new LeadType(
      "Guard", "lead_guard",
      "Guard this suspect against criminal activity.",
      ICON_DIR+"icon_guard_lead.png",
      new String[] { "Guarded", "Guarding", "Will Guard" },
      TIME_SHORT,
      MEDIUM_ASSAULT, FOCUS_ANY, TENSE_DURING, PROFILE_OBVIOUS,
      CONFIDENCE_HIGH, PHYSICAL_MEDIA
    ),
    BUST = new LeadType(
      "Bust", "lead_bust",
      "Bust down the doors and unleash hell.",
      ICON_DIR+"icon_guard_lead.png",
      new String[] { "Busted", "Busting", "Will Bust" },
      TIME_SHORT,
      MEDIUM_ASSAULT, FOCUS_ANY, TENSE_DURING, PROFILE_OBVIOUS,
      CONFIDENCE_HIGH, MEDIUM_ANY
    );
  
  
  final public String name, info, tenseVerbs[];
  final public Image icon;
  
  final public int minHours;
  final public int medium, focus, tense, profile;
  final public float confidence;
  final public int cluesMedia[];
  
  
  LeadType(
    String name, String ID,
    String info, String iconPath, String tenseVerbs[],
    int minHours, int medium, int focus, int tense, int profile,
    float confidence, int... cluesMedia
  ) {
    super(INDEX, ID);
    this.name = name;
    this.info = info;
    this.icon = Kind.loadImage(iconPath);
    this.tenseVerbs = tenseVerbs;
    this.minHours   = minHours  ;
    this.medium     = medium    ;
    this.focus      = focus     ;
    this.tense      = tense     ;
    this.profile    = profile   ;
    this.confidence = confidence;
    this.cluesMedia = cluesMedia;
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public static LeadType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public String toString() {
    return name;
  }
}



