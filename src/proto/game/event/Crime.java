

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;



public abstract class Crime extends Event {
  
  
  /**  Role-definitions-
    */
  final static Index <Role> ROLES_INDEX = new Index <Role> ();
  
  static class Role extends Index.Entry implements Session.Saveable {
    
    protected Role(String ID) {
      super(ROLES_INDEX, ID);
    }
    
    public static Role loadConstant(Session s) throws Exception {
      return ROLES_INDEX.loadEntry(s.input());
    }
    
    public void saveState(Session s) throws Exception {
      ROLES_INDEX.saveEntry(this, s.output());
    }
  }
  
  final public static Role
    ROLE_BASE      = new Role("role_base"     ),
    ROLE_HIDEOUT   = new Role("role_hideout"  ),
    ROLE_ORGANISER = new Role("role_organiser"),
    ROLE_BACKUP    = new Role("role_backup"   ),
    ROLE_TARGET    = new Role("role_target"   ),
    ROLE_SCENE     = new Role("role_scene"    )
  ;
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  
  protected class RoleEntry {
    
    Role role;
    Element element;
    Crime supplies;
    
    public String toString() {
      return role.entryKey()+" ("+element+")";
    }
  }
  
  protected class Contact {
    
    int medium;
    int timeTaken;
    Role between[];
    
    int timeStart;
    boolean spooked;
  }
  
  List <RoleEntry> entries = new List();
  List <Contact> contacts = new List();
  int spookLevel;
  
  
  
  protected Crime(CrimeType type, Base base) {
    super(type, base.world());
    this.base = base;
    assignRole(base, ROLE_BASE);
  }
  
  
  public Crime(Session s) throws Exception {
    super(s);
    base = (Base) s.loadObject();
    
    for (int n = s.loadInt(); n-- > 0;) {
      RoleEntry entry = new RoleEntry();
      entry.role     = (Crime.Role) s.loadObject();
      entry.element  = (Element   ) s.loadObject();
      entry.supplies = (Crime     ) s.loadObject();
      entries.add(entry);
    }
    for (int n = s.loadInt(); n-- > 0;) {
      Contact contact = new Contact();
      contact.between   = (Role[]) s.loadObjectArray(Role.class);
      contact.medium    = s.loadInt();
      contact.timeTaken = s.loadInt();
      contact.timeStart = s.loadInt();
      contact.spooked   = s.loadBool();
      contacts.add(contact);
    }
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(base);
    
    s.saveInt(entries.size());
    for (RoleEntry entry : entries) {
      s.saveObject(entry.role    );
      s.saveObject(entry.element );
      s.saveObject(entry.supplies);
    }
    s.saveInt(contacts.size());
    for (Contact contact : contacts) {
      s.saveObjectArray(contact.between);
      s.saveInt (contact.medium   );
      s.saveInt (contact.timeTaken);
      s.saveInt (contact.timeStart);
      s.saveBool(contact.spooked  );
    }
  }
  
  
  
  /**  Queueing and executing sub-events and generating clues for
    *  investigation-
    */
  public void queueContact(int medium, int timeTaken, Role... between) {
    Contact c = new Contact();
    c.between   = between;
    c.medium    = medium;
    c.timeTaken = timeTaken;
    contacts.add(c);
  }
  
  
  public void queueContacts(int medium, int timeTaken, Role from, Role... to) {
    for (Role r : to) queueContact(medium, timeTaken, from, r);
  }
  
  
  
  
  /**  Utility methods for assigning roles, fulfilling needs and evaluating
    *  possible targets-
    */
  public void assignRole(Element element, Role role) {
    RoleEntry match = roleFor(element, role);
    if (match == null) entries.add(match = new RoleEntry());
    match.role    = role   ;
    match.element = element;
  }
  
  
  public void assignTarget(Element target, Place scene) {
    assignRole(target, ROLE_TARGET);
    assignRole(scene , ROLE_SCENE );
  }
  
  
  public void assignOrganiser(Person organiser, Place hideout) {
    assignRole(organiser, ROLE_ORGANISER);
    assignRole(hideout  , ROLE_HIDEOUT  );
  }
  
  
  public Element target() {
    return elementWithRole(ROLE_TARGET);
  }
  
  
  public Element targetAt() {
    return elementWithRole(ROLE_SCENE);
  }
  
  
  public Element hideout() {
    return elementWithRole(ROLE_HIDEOUT);
  }
  
  
  public Element elementWithRole(Role role) {
    return roleFor(null, role).element;
  }
  
  
  public Batch <Element> elementsWithRole(Role role) {
    Batch <Element> matches = new Batch();
    for (RoleEntry entry : entries) {
      if (entry.role == role) matches.add(entry.element);
    }
    return matches;
  }
  
  
  protected RoleEntry roleFor(Element element, Role role) {
    for (RoleEntry entry : entries) {
      if (element != null && element != entry.element) continue;
      if (role    != null && role    != entry.role   ) continue;
      return entry;
    }
    return null;
  }
  
  
  protected void fillExpertRole(
    Trait trait, Series <Person> candidates, Role role
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : candidates) {
      if (roleFor(p, null) != null) continue;
      pick.compare(p, p.stats.levelFor(trait));
    }
    if (pick.empty()) return;
    assignRole(pick.result(), role);
  }
  
  
  protected void fillInsideRole(
    Place target, Crime.Role role
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : target.residents()) {
      if (roleFor(p, null) != null) continue;
      pick.compare(p, 0 - p.history.valueFor(target.owner()));
    }
    if (pick.empty()) return;
    assignRole(pick.result(), role);
  }
  
  
  protected void fillItemRole(
    ItemType type, World world, Role role
  ) {
    assignRole(new Item(type, world), role);
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







