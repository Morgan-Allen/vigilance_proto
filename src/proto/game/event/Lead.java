

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public class Lead implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static int
    //  The method by which a lead can be followed (and which contacts it can
    //  pick up on)-
    MEDIUM_MEET     =  1,
    MEDIUM_WIRE     =  2,
    MEDIUM_SURVEIL  =  3,
    MEDIUM_QUESTION =  4,
    MEDIUM_COVER    =  5,
    MEDIUM_HEIST    =  6,
    MEDIUM_ANY      = -1,
    //  Whether this lead can pick up on past/present/future contacts-
    TENSE_BEFORE =  0,
    TENSE_DURING =  1,
    TENSE_AFTER  =  2,
    TENSE_ANY    = -1,
    //  The type of focus this lead is intended for-
    FOCUS_PERSON   = 0,
    FOCUS_BUILDING = 1,
    FOCUS_REGION   = 2,
    //  How likely following a lead is to spook the perpetrator-
    PROFILE_HIDDEN     = 0,
    PROFILE_LOW        = 1,
    PROFILE_SUSPICIOUS = 2,
    PROFILE_HIGH       = 3,
    PROFILE_OBVIOUS    = 4;
  final static float
    //  Degrees of success in investigation-
    RESULT_NONE    = -1,
    RESULT_COLD    =  0,
    RESULT_PARTIAL =  1,
    RESULT_HOT     =  2,
    //  How much a successful lead counts for, assuming perfect success-
    CONFIDENCE_LOW      = 0.33f,
    CONFIDENCE_MODERATE = 0.66f,
    CONFIDENCE_HIGH     = 1.00f;
  
  
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
    SURVEIL_PERSON = new Type(
      "Surveil", 0,
      MEDIUM_SURVEIL, FOCUS_PERSON, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, MEDIUM_MEET
    ),
    SURVEIL_BUILDING = new Type(
      "Surveil", 1,
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_MEET
    ),
    QUESTION = new Type(
      "Question", 2,
      MEDIUM_QUESTION, FOCUS_PERSON, TENSE_AFTER, PROFILE_HIGH,
      CONFIDENCE_MODERATE, MEDIUM_ANY
    ),
    WIRETAP = new Type(
      "Wiretap", 3,
      MEDIUM_WIRE, FOCUS_BUILDING, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_HIGH, MEDIUM_WIRE
    ),
    PATROL = new Type(
      "Patrol", 4,
      MEDIUM_SURVEIL, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_MEET, MEDIUM_SURVEIL
    ),
    SCAN = new Type(
      "Scan", 5,
      MEDIUM_WIRE, FOCUS_REGION, TENSE_DURING, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_WIRE
    ),
    CANVAS = new Type(
      "Canvas", 6,
      MEDIUM_QUESTION, FOCUS_REGION, TENSE_AFTER, PROFILE_SUSPICIOUS,
      CONFIDENCE_LOW, MEDIUM_ANY
    ),
    SEARCH = new Type(
      "Search", 7,
      MEDIUM_SURVEIL, FOCUS_BUILDING, TENSE_AFTER, PROFILE_LOW,
      CONFIDENCE_MODERATE, MEDIUM_WIRE, MEDIUM_MEET
    ),
    LEAD_TYPES[] = TYPE_B.toArray(Type.class);
  
  
  final Type type;
  final Crime crime;
  final Element focus;
  boolean done;
  
  
  Lead(Type type, Crime crime, Element focus) {
    this.type  = type ;
    this.crime = crime;
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
    s.cacheInstance(this);
    type  = LEAD_TYPES[s.loadInt()];
    crime = (Crime  ) s.loadObject();
    focus = (Element) s.loadObject();
    done = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveInt(type.ID);
    s.saveObject(crime);
    s.saveObject(focus);
    s.saveBool(done);
  }
  
  
  
  /**  Generation and screening of clues related to the case:
    */
  protected boolean canDetect(
    Crime.Contact contact, int tense, Crime crime
  ) {
    //  TODO:  Consider splitting this off into separate sub-methods for
    //  override by subclasses.
    //
    //  First check the tense and crime-
    if (crime != this.crime) {
      return false;
    }
    if (tense != TENSE_ANY && type.tense != TENSE_ANY && tense != type.tense) {
      return false;
    }
    //
    //  Then, check the medium-
    boolean matchMedium = false;
    for (int medium : type.cluesMedia) {
      if (medium == MEDIUM_ANY || medium == contact.medium) {
        matchMedium = true;
        break;
      }
    }
    if (! matchMedium) return false;
    //
    //  Then check the focus-
    boolean matchFocus = false;
    for (Crime.Role role : contact.between) {
      Element contacts = crime.elementWithRole(role);
      
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
  
  
  protected int detectResult() {
    return -1;
  }
  
  
  protected Series <Clue> possibleClues(
    Crime.Contact contact, int tense, Crime crime
  ) {
    Batch <Clue> possible = new Batch();
    int time = crime.base.world().timing.totalHours();
    
    for (Crime.Role role : contact.between) {
      Element contacts = crime.elementWithRole(role);
      if (contacts == focus) continue;
      
      if (contacts.isPerson()) {
        Person p = (Person) contacts;
        for (Trait t : Common.PERSON_TRAITS) {
          if (p.stats.levelFor(t) <= 0) continue;
          Clue clue = new Clue(crime, role);
          clue.assignEvidence(t, type, type.confidence, time);
          possible.add(clue);
        }
      }
      
      if (contacts.isPlace()) {
        Place p = (Place) contacts;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue clue = new Clue(crime, role);
          clue.assignEvidence(t, type, type.confidence, time);
          possible.add(clue);
        }
      }
      
      if (contacts.isItem()) {
        Item p = (Item) contacts;
        Clue clue = new Clue(crime,role);
        clue.confirmMatch(p, type, time);
        possible.add(clue);
      }
    }
    
    return possible;
  }
  
  
  /*
  protected Series <Clue> possibleClues(
    Crime crime, Object match, Crime.Role role
  ) {
    final Batch <Clue> possible = new Batch();
    final int time = crime.base.world().timing.totalHours();
    
    for (Crime.RoleEntry entry : crime.entries) {
      if (match != null && entry.element != match) continue;
      if (role  != null && entry.role    != role ) continue;
      
      if (entry.element.isPerson()) {
        Person p = (Person) entry.element;
        for (Trait t : Common.PERSON_TRAITS) {
          if (p.stats.levelFor(t) <= 0) continue;
          Clue clue = new Clue(crime, entry.role);
          clue.assignEvidence(t, type, type.confidence, time);
          possible.add(clue);
        }
      }
      
      if (entry.element.isPlace()) {
        Place p = (Place) entry.element;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue clue = new Clue(crime, entry.role);
          clue.assignEvidence(t, type, type.confidence, time);
          possible.add(clue);
        }
      }
      
      if (entry.element.isItem()) {
        Item p = (Item) entry.element;
        Clue clue = new Clue(crime, entry.role);
        clue.confirmMatch(p, type, time);
        possible.add(clue);
      }
    }
    
    return possible;
  }
  //*/
  
  
}




