

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


public class Lead {
  
  
  final public static int
    TYPE_SURVEIL    = 1,
    TYPE_DATABASE   = 2,
    TYPE_QUESTION   = 3,
    TYPE_SEARCH     = 4,
    TYPE_WIRETAP    = 5,
    TYPE_UNDERCOVER = 6,
    TYPE_PATROL     = 7,
    TYPE_CANVAS     = 8,
    TYPE_SCAN       = 9
  ;
  final public static String LEAD_DESC[] = {
    null,
    "Surveillance", "Database Records", "Questioning",
    "Search", "Wiretaps", "Undercover Work",
    "Patrols", "Canvassing", "Scanning Frequencies"
  };
  
  Crime crime;
  int leadType;
  float confidence = 1.0f;
  
  
  Lead(Crime crime, int leadType, float confidence) {
    this.crime      = crime     ;
    this.leadType   = leadType  ;
    this.confidence = confidence;
  }
  
  
  
  Series <Clue> possibleClues(
    Crime crime, Object match, Crime.RoleType roleID
  ) {
    final Batch <Clue> possible = new Batch();
    final int time = crime.base.world().timing.totalHours();
    
    for (Crime.Role role : crime.roles) {
      if (match  != null && role.element != match ) continue;
      if (roleID != null && role.roleID  != roleID) continue;
      
      if (role.element.isPerson()) {
        Person p = (Person) role.element;
        for (Trait t : Common.PERSON_TRAITS) {
          if (p.stats.levelFor(t) <= 0) continue;
          Clue clue = new Clue(crime, role.roleID);
          clue.assignEvidence(t, leadType, confidence, time);
          possible.add(clue);
        }
      }
      
      if (role.element.isPlace()) {
        Place p = (Place) role.element;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue clue = new Clue(crime, role.roleID);
          clue.assignEvidence(t, leadType, confidence, time);
          possible.add(clue);
        }
      }
      
      if (role.element.isItem()) {
        Item p = (Item) role.element;
        Clue clue = new Clue(crime, role.roleID);
        clue.confirmMatch(p, leadType, time);
        possible.add(clue);
      }
    }
    
    return possible;
  }
  
  
}



