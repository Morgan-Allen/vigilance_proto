

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;

/*
Person
  Surveil
  Database
  Question
  Guard / Extract

Building
  Search
  Wiretap
  Undercover
  Guard / Bust

Area
  Patrol
  Canvas
  Scan
  Guard / Crackdown
//*/


public class Lead implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public static int
    TYPE_SURVEIL    = 1,
    TYPE_DATABASE   = 2,
    TYPE_QUESTION   = 3,
    TYPE_SEARCH     = 4,
    TYPE_WIRETAP    = 5,
    TYPE_UNDERCOVER = 6,
    TYPE_PATROL     = 7,
    TYPE_CANVAS     = 8,
    TYPE_SCAN       = 9;
  final public static String LEAD_DESC[] = { null,
    "Surveillance", "Database Check", "Questioning",
    "Search", "Wiretap", "Undercover Work",
    "Patrol", "Canvassing", "Scanning Frequencies"
  };
  final public static int
    MEDIUM_MEETING  = 1,
    MEDIUM_WIRE     = 2,
    MEDIUM_SURVEIL  = 3,
    MEDIUM_HEIST    = 4;
  final public static String MEDIUM_DESC[] = { null,
    "Meeting", "Wire", "Surveil", "Heist"
  };
  
  Crime crime;
  Element focus;
  int leadType;
  float confidence = 1.0f;
  
  
  Lead(Crime crime, Element focus, int leadType, float confidence) {
    this.crime      = crime     ;
    this.focus      = focus     ;
    this.leadType   = leadType  ;
    this.confidence = confidence;
  }
  
  
  public Lead(Session s) throws Exception {
    s.cacheInstance(this);
    crime      = (Crime  ) s.loadObject();
    focus      = (Element) s.loadObject();
    leadType   = s.loadInt  ();
    confidence = s.loadFloat();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(crime);
    s.saveObject(focus);
    s.saveInt(leadType);
    s.saveFloat(confidence);
  }
  
  
  
  /**  Generation and screening of clues related to the case:
    */
  Series <Clue> possibleClues(
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
          clue.assignEvidence(t, leadType, confidence, time);
          possible.add(clue);
        }
      }
      
      if (entry.element.isPlace()) {
        Place p = (Place) entry.element;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue clue = new Clue(crime, entry.role);
          clue.assignEvidence(t, leadType, confidence, time);
          possible.add(clue);
        }
      }
      
      if (entry.element.isItem()) {
        Item p = (Item) entry.element;
        Clue clue = new Clue(crime, entry.role);
        clue.confirmMatch(p, leadType, time);
        possible.add(clue);
      }
    }
    
    return possible;
  }
  
  
  //  TODO:  Now you need to establish which Clues you are likely to be able to
  //  derive from tracking a particular element at a particular time.
  
  
}















