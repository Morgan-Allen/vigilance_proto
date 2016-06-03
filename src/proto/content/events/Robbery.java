

package proto.content.events;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import proto.content.agents.*;



public class Robbery extends Event {
  
  final static int
    LEAD_OWNER  = 0,
    LEAD_CAMERA = 1,
    LEAD_MOLE   = 2,
    LEAD_BOSS   = 3,
    LEAD_STASH  = 4
  ;
  
  Scene business;
  Person owner;
  Person mole;
  Person boss;
  Scene stash;
  
  
  public Robbery(Session s) throws Exception {
    super(s);
    
    business = (Scene ) s.loadObject();
    owner    = (Person) s.loadObject();
    mole     = (Person) s.loadObject();
    boss     = (Person) s.loadObject();
    stash    = (Scene ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObjects(business, owner, mole, boss, stash);
  }
  
  
  
  public Robbery(Person boss, String busName, Region region, World world) {
    super(
      "Robbery from "+busName,
      busName+" in "+region+" was cleaned out in a robbery recently.  Find "+
      "the crew which pulled the heist before they can launder the proceeds.",
      world, region
    );
    
    this.business = new Scene(busName, region);
    this.owner    = Crooks.randomCivilian(world);
    this.mole     = Crooks.randomCivilian(world);
    this.boss     = boss;
    this.stash    = new Scene("Stash Safehouse", region);
    
    this.setKnown(business, owner);
    
    this.assignLeads(new Lead(
      "Talk to the manager",
      owner+", the manager of "+busName+" has appealed for help from "+
      "vigilantes.  You could try talking to him.",
      this, LEAD_OWNER, owner, new Object[] { mole, boss }, Task.TIME_SHORT,
      QUESTION, 3
    ));

    this.assignLeads(new Lead(
      "Inspect security footage",
      "The perpetrators were caught on camera.  You could hack the security "+
      "archives and inspect the footage.",
      this, LEAD_CAMERA, business, mole, Task.TIME_SHORT,
      INFORMATICS, 4
    ));
    
    this.assignLeads(new Lead(
      "Interrogate the inside man",
      "You have reason to believe "+mole+" was responsible for helping the "+
      "perps bypass security.  Interrogate them.",
      this, LEAD_MOLE, mole, stash, Task.TIME_SHORT,
      INTIMIDATE, 5
    ));
    
    this.assignLeads(new Lead(
      "Surveil the mobster",
      owner+" claims "+boss+" organised the heist in reprisal for his refusal "+
      "to invest in mob businesses.  Follow him.",
      this, LEAD_BOSS, boss, stash, Task.TIME_MEDIUM,
      SURVEILLANCE, 6, STEALTH, 5
    ));
    
    this.assignLeads(new Lead(
      "Raid the stash",
      "You've found the location of the stash where the goods are being "+
      "held- take out the goons and hang them out to dry.",
      this, LEAD_STASH, stash, this, Task.TIME_SHORT,
      CLOSE_COMBAT, 5, MARKSMAN, 5
    ));
  }
  
  
  protected boolean checkFollowed(Lead lead, boolean success) {
    final District nation = world().nationFor(region());
    final Events events = world().events();
    
    if (lead.ID == LEAD_CAMERA) {
      if (success) {
        events.log(
          "There's a blurry shot of two individuals exiting the building, but "+
          "prior to that it looks like the footage is looped.  According to "+
          "employee records, only "+mole+" had maintenance access."
        );
      }
      else {
        events.log(
          "There's a blurry shot of two individuals exiting the building, but "+
          "the rest of the footage is inaccessible."
        );
      }
    }
    
    if (lead.ID == LEAD_OWNER) {
      if (success) {
        events.log(
          owner+" seems reluctant to talk, but believes that "+mole+" may "+
          "have informed "+boss+" when the vault was being emptied.  The "+
          "latter had been pressuring them to invest in mob businesses."
        );
      }
      else {
        events.log(
          owner+" is clearly frightened.  Someone got to them before you "+
          "could coax them to talk."
        );
      }
    }
    
    if (lead.ID == LEAD_MOLE) {
      if (! success) {
        nation.incLevel(District.TRUST, -1);
      }
    }
    
    if (lead.ID == LEAD_STASH) {
      setComplete(true);
      
      if (success) {
        events.log("The stolen valuables have been recovered.");
        nation.incLevel(District.TRUST     , 2);
        nation.incLevel(District.DETERRENCE, 5);
      }
      else {
        for (Person p : lead.assigned()) {
          p.receiveInjury(2);
        }
        events.log(
          "Your bust was a failure.  The perps will have scattered and taken "+
          "the cash with them- you'll never find it now."
        );
        nation.incLevel(District.DETERRENCE, -10);
      }
    }
    
    return super.checkFollowed(lead, success);
  }
  
  
  
  /**  And, last but not least, a generator/type for the event-
    */
  final public static EventType TYPE = new EventType(
    "Robbery", "event_robbery"
  ) {
    public Event createRandomEvent(World world) {
      Person boss    = Crooks.randomMobster(world);
      District nation  = (District) Rand.pickFrom(world.nations());
      
      Event s = new Robbery(boss, "The Central Bank", nation.region, world);
      float time = world.timeDays() + Rand.index(5);
      s.assignDates(time + 2, time + 7);
      return s;
    }
    
    public float eventChance(Event event) {
      return 1;
    }
  };
}











