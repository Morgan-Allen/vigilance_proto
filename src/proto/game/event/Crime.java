

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
    
    final String name;
    
    protected Role(String ID, String name) {
      super(ROLES_INDEX, ID);
      this.name = name;
    }
    
    public static Role loadConstant(Session s) throws Exception {
      return ROLES_INDEX.loadEntry(s.input());
    }
    
    public void saveState(Session s) throws Exception {
      ROLES_INDEX.saveEntry(this, s.output());
    }
    
    public String toString() {
      return name;
    }
  }
  
  final public static Role
    ROLE_BASE      = new Role("role_base"     , "Base"     ),
    ROLE_HIDEOUT   = new Role("role_hideout"  , "Hideout"  ),
    ROLE_ORGANISER = new Role("role_organiser", "Organiser"),
    ROLE_BACKUP    = new Role("role_backup"   , "Backup"   ),
    ROLE_TARGET    = new Role("role_target"   , "Target"   ),
    ROLE_SCENE     = new Role("role_scene"    , "Scene"    )
  ;
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Base base;
  int spookLevel = 0, nextContactID = 0;
  List <RoleEntry> entries  = new List();
  List <Contact  > contacts = new List();
  
  
  
  protected Crime(CrimeType type, Base base) {
    super(type, base.world());
    this.base = base;
    assignRole(base, ROLE_BASE);
  }
  
  
  public Crime(Session s) throws Exception {
    super(s);
    base = (Base) s.loadObject();
    spookLevel    = s.loadInt();
    nextContactID = s.loadInt();
    
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
      contact.ID        = s.loadInt();
      contact.timeStart = s.loadInt();
      contact.spooked   = s.loadBool();
      contacts.add(contact);
    }
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(base);
    s.saveInt(spookLevel   );
    s.saveInt(nextContactID);
    
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
      s.saveInt (contact.ID       );
      s.saveInt (contact.timeStart);
      s.saveBool(contact.spooked  );
    }
  }
  
  
  
  /**  Queueing and executing sub-events and generating clues for
    *  investigation-
    */
  //  TODO:  Have this extend Task.
  protected class Contact {
    
    int medium;
    int timeTaken;
    Role between[];
    int ID;
    
    int timeStart = -1;
    boolean spooked = false;
    
    public String toString() {
      return "Contact between "+I.list(between);
    }
  }
  
  
  public void queueContact(int medium, int timeTaken, Role... between) {
    Contact c = new Contact();
    c.between   = between;
    c.medium    = medium;
    c.timeTaken = timeTaken;
    c.ID        = nextContactID++;
    contacts.add(c);
  }
  
  
  public void queueContacts(int medium, int timeTaken, Role from, Role... to) {
    for (Role r : to) queueContact(medium, timeTaken, from, r);
  }
  
  
  public boolean contactBegun(Contact contact) {
    return contact != null && contact.timeStart >= 0;
  }
  
  
  public boolean contactComplete(Contact contact) {
    if (! contactBegun(contact)) return false;
    int time = base.world().timing.totalHours();
    return time >= contact.timeStart + contact.timeTaken;
  }
  
  
  public int contactTense(Contact contact) {
    int time = base.world().timing.totalHours();
    int start = contact.timeStart, tense = Lead.TENSE_BEFORE;
    boolean begun = start >= 0;
    boolean done = time > (contact.timeStart + contact.timeTaken);
    if (begun) tense = done ? Lead.TENSE_AFTER : Lead.TENSE_DURING;
    return tense;
  }
  
  
  public Series <Contact> allContacts() {
    return contacts;
  }
  
  
  public void takeSpooking(int spookAmount) {
    spookLevel += spookAmount;
    //  TODO:  Abort the heist if you're too spooked!
  }
  
  
  
  /**  Utility methods for assigning roles, fulfilling needs and evaluating
    *  possible targets-
    */
  protected class RoleEntry {
    
    Role role;
    Element element;
    Crime supplies;
    
    public String toString() {
      return role.name+" ("+element+")";
    }
  }
  
  
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
  
  
  public Person organiser() {
    return (Person) elementWithRole(ROLE_ORGANISER);
  }
  
  
  public Place hideout() {
    return (Place) elementWithRole(ROLE_HIDEOUT);
  }
  
  
  public Person target() {
    return (Person) elementWithRole(ROLE_TARGET);
  }
  
  
  public Place scene() {
    return (Place) elementWithRole(ROLE_SCENE);
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
  

  
  /**  Life cycle and execution:
    */
  protected boolean fillRoles() {
    //  TODO:  Make abstract and implement per-subclass.
    return false;
  }
  
  
  protected void onCompletion(Contact contact) {
    //  TODO:  Make abstract and implement per-subclass.
    return;
  }
  
  
  public boolean possible() {
    for (RoleEntry entry : entries) {
      if (! rolePossible(entry.role, entry.element, entry.supplies)) {
        return false;
      }
    }
    return true;
  }
  
  
  public void updateEvent() {
    for (RoleEntry entry : entries) if (entry.supplies != null) {
      if (! entry.supplies.hasBegun()) {
        world.events.scheduleEvent(entry.supplies);
      }
    }
    
    //  TODO:  Re-satisfy needs as and when required.
    if (! possible()) return;
    
    int time = world.timing.totalHours();
    Contact current = null, next = null;
    
    for (Contact c : contacts) {
      if (next    == null && ! contactBegun(c)) next    = c;
      if (current == null &&   contactBegun(c)) current = c;
    }
    if ((current == null || contactComplete(current)) && next != null) {
      next.timeStart = time;
    }
    
    if (current == contacts.last() && contactComplete(current)) {
      //  TODO:  Execute the actual crime.
      completeEvent();
    }
  }
  
  
  protected boolean rolePossible(Role role, Element element, Crime supplies) {
    if (supplies != null) {
      if (! supplies.possible()) return false;
      if (! supplies.complete()) return false;
    }
    return true;
  }
  
  
  public Place targetLocation(Person p) {
    return scene();
  }
  
}



