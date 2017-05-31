

package proto.game.world;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;
import java.awt.Image;



public class Place extends Element {
  
  
  /**  Data fields, construction and save/load methods-
    */
  private Base owner;
  private int slotID;
  private float buildProgress;
  private List <Trait> properties = new List();
  private List <Person> residents = new List();
  
  
  public Place(PlaceType kind, int slotID, World world) {
    super(kind, world);
    this.slotID        = slotID;
    this.buildProgress = 1.0f;
    for (Trait t : kind.baseTraits()) setProperty(t, true);
  }
  
  
  public Place(Session s) throws Exception {
    super(s);
    slotID        = s.loadInt();
    owner         = (Base  ) s.loadObject();
    buildProgress = s.loadFloat();
    s.loadObjects(properties);
    s.loadObjects(residents);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveInt   (slotID       );
    s.saveObject(owner        );
    s.saveFloat (buildProgress);
    s.saveObjects(properties);
    s.saveObjects(residents);
  }
  
  
  
  /**  Construction progress-
    */
  public PlaceType kind() {
    return (PlaceType) kind;
  }
  
  
  public float buildProgress() {
    return buildProgress;
  }
  
  
  public float buildDaysRemaining() {
    return kind().buildTime * (1f - buildProgress);
  }
  
  
  public Base base() {
    return owner;
  }
  
  
  public void setBase(Base owns) {
    this.owner = owns;
  }
  
  
  public void setBuildProgress(float progress) {
    this.buildProgress = Nums.clamp(progress, 0, 1);
  }
  
  
  public void setProperty(Trait trait, boolean is) {
    properties.toggleMember(trait, true);
  }
  
  
  public boolean hasProperty(Trait trait) {
    return properties.includes(trait);
  }
  
  
  public Series <Trait> traits() {
    return properties;
  }
  
  
  
  /**  Updates and other life-cycle methods:
    */
  public void updatePlace() {
    for (Task t : possibleTasks()) {
      t.updateAssignment();
    }
  }
  
  
  public void updateResidents() {
    final Kind hired[] = kind().childTypes();
    for (Kind type : hired) if (type.type() == Kind.TYPE_PERSON) {
      final int max = kind.baseLevel(type);
      int count = 0;
      for (Person p : residents) {
        if (p.health.dead()) Place.setResident(p, this, false);
        else if (p.kind() == type) count++;
      }
      while (count++ < max) {
        Person resides = Person.randomOfKind((PersonType) type, world);
        world.setInside(resides, true);
        setAttached(resides, true);
        resides.setBase(base());
        Place.setResident(resides, this, true);
      }
    }
  }
  
  
  
  /**  Toggling visitors and residents-
    */
  public Series <Person> visitors() {
    final Batch <Person> visitors = new Batch();
    for (Task t : possibleTasks()) for (Person p : t.assigned()) {
      visitors.include(p);
    }
    return visitors;
  }
  
  
  public Task[] possibleTasks() {
    return new Task[0];
  }
  
  
  public void setResident(Person person, boolean is) {
    residents.toggleMember(person, is);
  }
  
  
  public Series <Person> residents() {
    return residents;
  }
  
  
  public static void setResident(Person person, Place place, boolean is) {
    //  NOTE:  This is intended to allow residence to be specified from either
    //  the person or place without entering a loop or forgetting to update one
    //  side or another...
    final boolean
      there = person.resides() == place,
      here  = place.residents.includes(person);
    if (is) {
      if (there && here) return;
      if (! here) place.setResident(person, true);
      if (! there) person.setResidence(place);
    }
    else {
      if ((! there) && (! here)) return;
      if (here) place.setResident(person, false);
      if (there) person.setResidence(null);
    }
  }
  
  
  public Access accessLevel(Base base) {
    return Access.GRANTED;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name();
  }
  
  
  public String name() {
    return region().name()+" "+kind().name();
  }
  
  
  public Image icon() {
    return kind().icon();
  }
}



