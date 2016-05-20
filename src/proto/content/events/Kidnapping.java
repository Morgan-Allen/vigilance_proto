

package proto.content.events;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import proto.content.agents.*;

import java.awt.Image;
import java.awt.image.BufferedImage;




public class Kidnapping extends Event {
  
  final static int
    LEAD_INSPECT = 0,
    LEAD_FIBRES  = 1,
    LEAD_RESCUE  = 2,
    LEAD_RAID    = 3;
  
  Region region;
  Person boss;
  Person missing;
  Scene home;
  Scene taken;
  Clue fibres;
  
  
  public Kidnapping(Session s) throws Exception {
    super(s);
    region  = (Region) s.loadObject();
    boss    = (Person) s.loadObject();
    missing = (Person) s.loadObject();
    home    = (Scene ) s.loadObject();
    taken   = (Scene ) s.loadObject();
    fibres  = (Clue  ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObjects(region, boss, missing, home, taken, fibres);
  }
  
  
  
  public Kidnapping(Person boss, Person missing, Region region, World world) {
    super(
      "Kidnapping of "+missing.name(),
      missing.name()+" disappeared from their home in "+region+" recently. "+
      "Find them before it's too late.",
      world
    );
    
    this.region  = region;
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
      PERCEPTION, 4
    ));
    this.assignLeads(new Lead(
      "Fibres",
      "It looks like the kidnapper snagged some fibres when they broke the "+
      "window.",
      this, LEAD_FIBRES, fibres, taken, Task.TIME_MEDIUM,
      PERCEPTION, 6
    ));
    this.assignLeads(new Lead(
      "Escape with "+missing,
      "You've found where "+missing+" is being kept.  You could try to free "+
      "the victim and escape without alerting their captors.",
      this, LEAD_RESCUE, taken, missing, Task.TIME_SHORT,
      EVASION, 7
    ));
    this.assignLeads(new Lead(
      "Raid on "+taken,
      "You've found where "+missing+" is being kept.  You could try a direct "+
      "raid to subdue- or at least distract- their captors.",
      this, LEAD_RAID, taken, missing, Task.TIME_SHORT,
      COMBAT, 6
    ));
  }
  
  
  protected boolean checkFollowed(Lead lead, boolean success) {
    final Nation nation = world().nationFor(region);
    
    if (lead.ID == LEAD_RESCUE) {
      if (success) {
        setComplete(true);
        logAction(missing+" escapes unharmed from their captors.");
        nation.incTrust(5);
        logAction("Trust +5: "+region);
      }
      else {
        Lead raid = leadWithID(LEAD_RAID);
        raid.setModifier(COMBAT, -2);
        raid.attemptTask();
      }
    }
    
    if (lead.ID == LEAD_RAID) {
      if (success) {
        setComplete(true);
        logAction("The captors keeping "+missing+" hostage were subdued.");
        nation.incCrime(-5);
        logAction("Crime -5: "+region);
      }
      else {
        setComplete(false);
        if (Rand.yes()) {
          for (Person p : lead.assigned()) {
            p.receiveInjury(2);
            logAction(p.name()+" was injured.");
          }
        }
        else {
          missing.receiveInjury(missing.maxHealth() * (Rand.num() + 0.5f));
          if (missing.alive()) logAction(missing.name()+" was injured.");
          else                 logAction(missing.name()+" was killed!" );
          nation.incTrust(-10);
          logAction("Trust -10: "+region);
        }
        nation.incCrime(10);
        logAction("Crime +10: "+region);
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
      
      Person boss    = new Person(Crooks.MOBSTER , Crooks.randomCommonName());
      Person missing = new Person(Crooks.CIVILIAN, Crooks.randomCommonName());
      Nation nation  = (Nation) Rand.pickFrom(world.nations());
      
      Event s = new Kidnapping(boss, missing, nation.region, world);
      float time = world.timeDays() + Rand.index(5);
      s.assignDates(time + 2, time + 7);
      return s;
    }
    
    public float eventChance(Event event) {
      return 0;
    }
  };
}











