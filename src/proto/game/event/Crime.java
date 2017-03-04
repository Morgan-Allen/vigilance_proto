

package proto.game.event;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public abstract class Crime {
  
  
  protected class Role {
    Object roleType;
    Element element;
  }
  
  List <Role> roles = new List();
  
  
  Role roleFor(Element element) {
    for (Role role : roles) {
      if (role.element == element) return role;
    }
    return null;
  }
  
  
  
  /**  
    */
  void assignRole(Element element, Object roleType) {
    Role match = roleFor(element);
    if (match == null) roles.add(match = new Role());
    match.roleType = roleType;
    match.element  = element ;
  }
  
  
}


class Lead2 {
  
  
}


class CaseFiles {
  
  List <Clue> clues = new List();
  
}






