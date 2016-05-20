

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
  
  
  
  public Kidnapping(Person boss, Person missing, Region region) {
    super(
      "Kidnapping of "+missing.name(),
      missing.name()+" disappeared from their home in "+region+" recently. "+
      "Find them before it's too late."
    );
    
    this.region = region;
    this.home   = new Scene("home"  , region);
    this.taken  = new Scene("taken" , region);
    this.fibres = new Clue("fibres");
    
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
    
    if (lead.ID == LEAD_RESCUE) {
      if (success) {
        setComplete(true);
      }
    }
    
    if (lead.ID == LEAD_RAID) {
      if (success) {
        setComplete(true);
      }
      else {
        setComplete(false);
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
      
      Event s = new Kidnapping(boss, missing, nation.region);
      float time = world.timeDays() + Rand.index(5);
      s.assignDates(time + 2, time + 7);
      return s;
    }
    
    public float eventChance(Event event) {
      return 0;
    }
  };
}











