

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public abstract class Crime extends Event {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Base base;
  Place hideout;
  Element target;
  
  protected class Role {
    int roleID;
    Element element;
  }
  
  List <Role> roles = new List();
  
  
  protected Crime(CrimeType type, World world) {
    super(type, world);
  }
  
  
  public Crime(Session s) throws Exception {
    super(s);
    base    = (Base   ) s.loadObject();
    hideout = (Place  ) s.loadObject();
    target  = (Element) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(base   );
    s.saveObject(hideout);
    s.saveObject(target );
  }
  
  
  
  /**  Clue extraction-
    */
  
  
  
  /**  Utility methods for assigning roles, fulfilling needs and evaluating
    *  possible targets-
    */
  public void assignRole(Element element, int roleID) {
    Role match = roleFor(element);
    if (match == null) roles.add(match = new Role());
    match.roleID  = roleID ;
    match.element = element;
  }
  
  
  Role roleFor(Element element) {
    for (Role role : roles) {
      if (role.element == element) return role;
    }
    return null;
  }
  
  
  protected Series <Person> onRoster() {
    return base.roster();
  }
  
  
  protected Series <Person> expertsWith(Trait trait, int minLevel) {
    final Batch <Person> experts = new Batch();
    for (Element e : base.world().inside()) if (e.isPerson()) {
      Person p = (Person) e;
      if (p.stats.levelFor(trait) < minLevel) continue;
      experts.add(p);
    }
    return experts;
  }
  
  
  protected Series <Place> venuesNearby(Place target, int maxDist) {
    final Batch <Place> venues = new Batch();
    return venues;
  }
  
}







