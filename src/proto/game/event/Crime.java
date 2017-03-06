

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public abstract class Crime extends Event {
  
  
  /**  Role-definitions-
    */
  final static Index <RoleType> ROLES_INDEX = new Index <RoleType> ();
  
  static class RoleType extends Index.Entry implements Session.Saveable {
    
    public RoleType(String ID) {
      super(ROLES_INDEX, ID);
    }
    
    public static RoleType loadConstant(Session s) throws Exception {
      return ROLES_INDEX.loadEntry(s.input());
    }
    
    public void saveState(Session s) throws Exception {
      ROLES_INDEX.saveEntry(this, s.output());
    }
  }
  
  protected class Role {
    
    RoleType roleID;
    Element element;
    Crime supplies;
    
    public String toString() {
      return roleID.entryKey()+" ("+element+")";
    }
  }
  
  //  TODO:  Include default roles for 'base', 'hideout' and 'target'!
  
  
  
  /**  Data fields, construction and save/load methods-
    */
  Base base;
  Place hideout;
  Element target;
  List <Role> roles = new List();
  
  
  protected Crime(CrimeType type, Base base) {
    super(type, base.world());
    this.base = base;
  }
  
  
  public Crime(Session s) throws Exception {
    super(s);
    base    = (Base   ) s.loadObject();
    hideout = (Place  ) s.loadObject();
    target  = (Element) s.loadObject();
    
    for (int n = s.loadInt(); n-- > 0;) {
      Role role = new Role();
      role.roleID   = (Crime.RoleType) s.loadObject();
      role.element  = (Element) s.loadObject();
      role.supplies = (Crime  ) s.loadObject();
    }
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(base   );
    s.saveObject(hideout);
    s.saveObject(target );
    
    s.saveInt(roles.size());
    for (Role role : roles) {
      s.saveObject(role.roleID  );
      s.saveObject(role.element );
      s.saveObject(role.supplies);
    }
  }
  
  
  
  /**  Utility methods for assigning roles, fulfilling needs and evaluating
    *  possible targets-
    */
  public void assignTarget(Element target) {
    this.target = target;
  }
  
  
  public void assignHideout(Place hideout) {
    this.hideout = hideout;
  }
  
  
  public void assignRole(Element element, RoleType roleID) {
    Role match = roleFor(element, roleID);
    if (match == null) roles.add(match = new Role());
    match.roleID  = roleID ;
    match.element = element;
  }
  
  
  protected Role roleFor(Element element, RoleType roleID) {
    for (Role role : roles) {
      if (element != null && element != role.element) continue;
      if (roleID  != null && roleID  != role.roleID ) continue;
      return role;
    }
    return null;
  }
  
  
  protected void fillExpertRole(
    Trait trait, Series <Person> candidates, RoleType roleID
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : candidates) {
      if (roleFor(p, null) != null) continue;
      pick.compare(p, p.stats.levelFor(trait));
    }
    if (pick.empty()) return;
    assignRole(pick.result(), roleID);
  }
  
  
  protected void fillInsideRole(
    Place target, Crime.RoleType roleID
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : target.residents()) {
      if (roleFor(p, null) != null) continue;
      pick.compare(p, 0 - p.history.valueFor(target.owner()));
    }
    if (pick.empty()) return;
    assignRole(pick.result(), roleID);
  }
  
  
  protected void fillItemRole(
    ItemType type, World world, RoleType roleID
  ) {
    assignRole(new Item(type, world), roleID);
  }
  
  
  protected Series <Person> goonsOnRoster() {
    Batch <Person> goons = new Batch();
    for (Person p : base.roster()) {
      if (p == base.leader()) continue;
      goons.add(p);
    }
    return goons;
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







