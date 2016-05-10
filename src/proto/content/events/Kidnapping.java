

package proto.content.events;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import static proto.game.person.PersonStats.*;

import java.awt.Image;
import java.awt.image.BufferedImage;




public class Kidnapping extends Investigation {
  
  final static int
    LEAD_INSPECT = 0,
    LEAD_FIBRES  = 1,
    LEAD_RESCUE  = 2,
    LEAD_RAID    = 3;
  
  final static Image
    //  TODO:  You will need more of these...
    IMG_LEAD = Kind.loadImage(
      "media assets/scene backgrounds/crime_generic_1.png"
    );
  
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
    super("Kidnapping of "+missing.name());
    
    this.region = region;
    this.home   = new Scene("home"  , region);
    this.taken  = new Scene("taken" , region);
    this.fibres = new Clue("fibres");
    
    this.assignLeads(new Lead(
      "Home of "+missing,
      "Inspect the victim's last known location for clues.",
      this, LEAD_INSPECT, home, fibres,
      PERCEPTION, 4
    ));
    this.assignLeads(new Lead(
      "Fibres",
      "It looks like the kidnapper snagged some fibres when they broke the "+
      "window.",
      this, LEAD_FIBRES, fibres, taken,
      PERCEPTION, 6
    ));
    this.assignLeads(new Lead(
      "Escape with "+missing,
      "You've found where "+missing+" is being kept.  You could try to free "+
      "the victim and escape without alerting their captors.",
      this, LEAD_RESCUE, taken, missing,
      EVASION, 7
    ));
    this.assignLeads(new Lead(
      "Raid on "+taken,
      "You've found where "+missing+" is being kept.  You could try a direct "+
      "raid to subdue- or at least distract- her captors.",
      this, LEAD_RAID, taken, missing,
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
    public Investigation createRandomEvent(World world) {
      return null;
    }
    
    public float eventChance(Investigation event) {
      return 0;
    }
  };
}











