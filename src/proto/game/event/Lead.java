

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public class Lead {
  
  
  Crime crime;
  int leadType;
  float confidence = 1.0f;
  
  
  
  Series <Clue> possibleClues(Crime crime) {
    final Batch <Clue> possible = new Batch();
    
    for (Crime.Role role : crime.roles) {
      
      if (role.element.isPerson()) {
        Person p = (Person) role.element;
        for (Trait t : Common.PERSON_TRAITS) {
          if (p.stats.levelFor(t) <= 0) continue;
          Clue clue = new Clue(p, crime, role.roleID);
          clue.assignEvidence(t, leadType, confidence);
          possible.add(clue);
        }
      }
      if (role.element.isPlace()) {
        Place p = (Place) role.element;
        for (Trait t : Common.VENUE_TRAITS) {
          if (! p.hasProperty(t)) continue;
          Clue clue = new Clue(p, crime, role.roleID);
          clue.assignEvidence(t, leadType, confidence);
          possible.add(clue);
        }
      }
      if (role.element.isItem()) {
        Item p = (Item) role.element;
        Clue clue = new Clue(p, crime, role.roleID);
        clue.assignEvidence(p, leadType, confidence);
        possible.add(clue);
      }
    }
    
    return possible;
  }
  
  
}



