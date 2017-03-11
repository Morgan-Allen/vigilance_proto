

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



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
  
  
  final Type type;
  final Crime crime;
  final Element focus;
  private boolean done;
  
  
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
          clue.assignEvidence(p, t, type, type.confidence, time);
          possible.add(clue);
        }
      }
      
      if (contacts.isPlace()) {
        Place p = (Place) contacts;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue clue = new Clue(crime, role);
          clue.assignEvidence(p, t, type, type.confidence, time);
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
  
  
  
  
  public float followChance(Person follows) {
    float skill = 0, obstacle = 1;
    
    if (type.medium == MEDIUM_SURVEIL) {
      Person perp = (Person) focus;
      skill    = follows.stats.levelFor(SIGHT_RANGE);
      obstacle = perp   .stats.levelFor(HIDE_RANGE );
    }
    
    if (type.medium == MEDIUM_WIRE) {
      Place site = (Place) focus;
      skill    = follows.stats.levelFor(ENGINEERING);
      obstacle = 5;
    }
    
    if (type.medium == MEDIUM_QUESTION) {
      Person perp = (Person) focus;
      skill    = follows.stats.levelFor(QUESTION);
      obstacle = perp.stats.levelFor(PERSUADE);
    }
    
    if (type.medium == MEDIUM_COVER) {
      Place site = (Place) focus;
      skill    = follows.stats.levelFor(PERSUADE);
      obstacle = 5;
    }
    
    return skill / (skill + obstacle);
  }
  
  
  protected float followResult(Person follows) {
    float chance = followChance(follows);
    chance = 1 - (Nums.sqrt(1 - chance));
    boolean roll1 = Rand.num() < chance, roll2 = Rand.num() < chance;
    
    if (roll1 && roll2) return RESULT_HOT;
    if (roll1 || roll2) return RESULT_PARTIAL;
    return RESULT_COLD;
  }
  
  
  public float followAttempt(
    Person follows, Crime.Contact contact, int tense, Crime crime
  ) {
    float result = followResult(follows);
    int time = crime.base.world().timing.totalHours();
    this.done = true;
    //
    //  TODO:  You could sharpen this up a little, with separate success-chance
    //  for each party involved in a contact?
    
    if (result <= RESULT_COLD) {
      return result;
    }
    else if (result <= RESULT_PARTIAL) {
      //  TODO:  You might consider only giving information on the rough
      //  location of a meet or wiretap trace?
      //  ...Yeah.  Determine location and traits in a separate step.
      
      for (Clue clue : possibleClues(contact, tense, crime)) {
        if (Rand.num() > 0.5f) continue;
        CaseFile file = follows.base().leads.caseFor(clue.match);
        file.recordClue(clue);
      }
    }
    else if (result <= RESULT_HOT) {
      for (Crime.Role role : contact.between) {
        Element subject = crime.elementWithRole(role);
        CaseFile file = follows.base().leads.caseFor(subject);
        
        Clue clue = new Clue(crime,role);
        clue.confirmMatch(subject, type, time);
        file.recordClue(clue);
      }
    }
    return result;
  }
  
  
}




