

package proto.content.events;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import proto.content.agents.*;
import static proto.content.agents.Crooks.*;



public class Kidnapping extends Event {
  
  final static int
    LEAD_INSPECT = 0,
    LEAD_FIBRES  = 1,
    LEAD_RESCUE  = 2,
    LEAD_RAID    = 3;
  
  Person boss;
  Person missing;
  Scene home;
  Scene taken;
  Clue fibres;
  
  
  public Kidnapping(Session s) throws Exception {
    super(s);
    boss    = (Person) s.loadObject();
    missing = (Person) s.loadObject();
    home    = (Scene ) s.loadObject();
    taken   = (Scene ) s.loadObject();
    fibres  = (Clue  ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObjects(boss, missing, home, taken, fibres);
  }
  
  
  
  public Kidnapping(Person boss, Person missing, Region region, World world) {
    super(
      "Kidnapping of "+missing.name(),
      missing.name()+" disappeared from their home in "+region+" recently. "+
      "Find them before it's too late.",
      world, region
    );
    
    this.boss    = boss;
    this.missing = missing;
    this.home    = new Scene("home"  , region);
    this.taken   = new Scene("taken" , region);
    this.fibres  = new Clue("fibres");
    
    this.setKnown(home);
    
    this.assignLeads(new Lead(
      "Inspect the scene",
      "Inspect the victim's last known location for clues.",
      this, LEAD_INSPECT, home, fibres, Task.TIME_SHORT,
      SURVEILLANCE, 4
    ));
    this.assignLeads(new Lead(
      "Fibres",
      "It looks like the kidnapper snagged some fibres when they broke the "+
      "window.  Microscopic or chemical analysis might yield some information.",
      this, LEAD_FIBRES, fibres, taken, Task.TIME_MEDIUM,
      PHARMACY, 6
    ));
    this.assignLeads(new Lead(
      "Escape with "+missing,
      "You've found where "+missing+" is being kept.  You could try to free "+
      "the victim and escape without alerting their captors.",
      this, LEAD_RESCUE, taken, missing, Task.TIME_SHORT,
      STEALTH, 7, GYMNASTICS, 4
    ));
    this.assignLeads(new Lead(
      "Raid on "+taken,
      "You've found where "+missing+" is being kept.  You could try a direct "+
      "raid to subdue- or at least distract- their captors.",
      this, LEAD_RAID, taken, missing, Task.TIME_SHORT,
      CLOSE_COMBAT, 6, MARKSMAN, 3
    ));
  }
  
  
  protected boolean checkFollowed(Lead lead, boolean success) {
    final District nation = world().nationFor(region());
    final Events events = world().events();
    
    if (lead.ID == LEAD_RESCUE) {
      if (success) {
        setComplete(true);
        events.log(missing+" escapes unharmed from their captors.");
        nation.incLevel(District.TRUST, 5);
      }
      else {
        Lead raid = leadWithID(LEAD_RAID);
        raid.setModifier(STRENGTH, -2);
        raid.attemptTask();
      }
    }
    
    if (lead.ID == LEAD_RAID) {
      if (success) {
        setComplete(true);
        events.log("The captors keeping "+missing+" hostage were subdued.");
        nation.incLevel(District.DETERRENCE, 5);
      }
      else {
        setComplete(false);
        if (Rand.yes()) {
          for (Person p : lead.assigned()) {
            p.health.receiveInjury(2);
          }
        }
        else {
          missing.health.receiveInjury(
            missing.health.maxHealth() * (Rand.num() + 0.5f)
          );
          nation.incLevel(District.TRUST, -10);
        }
        nation.incLevel(District.DETERRENCE, -10);
      }
    }
    
    return super.checkFollowed(lead, success);
  }
  
  
  
  /**  And, last but not least, a generator/type for the event-
    */
  final public static EventType TYPE = new EventType(
    "Kidnapping", "event_kidnapping"
  ) {
    public Event createRandomEvent(World world) {
      
      Person boss    = Crooks.randomMobster (world);
      Person missing = Crooks.randomCivilian(world);
      District nation  = (District) Rand.pickFrom(world.nations());
      
      Event s = new Kidnapping(boss, missing, nation.region, world);
      float time = world.timeDays() + Rand.index(5);
      s.assignDates(time + 2, time + 7);
      return s;
    }
    
    public float eventChance(Event event) {
      return 1;
    }
  };
}











