

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
    LEAD_MOLE   = 1,
    LEAD_BOSS   = 2,
    LEAD_STASH  = 3
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
    
    //  TODO:  Add an 'inspect footage' option here!
    
    this.assignLeads(new Lead(
      "Talk to the manager",
      owner+", the manager of "+busName+" has appealed for help from "+
      "vigilantes.  You could try talking to him.",
      this, LEAD_OWNER, owner, new Object[] { mole, boss }, Task.TIME_SHORT,
      QUESTION, 3
    ));
    
    this.assignLeads(new Lead(
      "Interrogate the inside man",
      owner+" has fingered "+mole+" as responsible for helping the perps "+
      "bypass security.  Try bracing him to see if he cracks.",
      this, LEAD_MOLE, mole, stash, Task.TIME_SHORT,
      INTIMIDATE, 5
    ));
    
    this.assignLeads(new Lead(
      "Surveil the mobster",
      owner+" claims "+boss+" organised the heist in reprisal for his refusal "+
      "to invest in mob businesses.  Follow him and see where it leads.",
      this, LEAD_BOSS, boss, stash, Task.TIME_LONG,
      SURVEILLANCE, 6, STEALTH, 5
    ));
    
    this.assignLeads(new Lead(
      "Raid the stash",
      "You've found the location of the stash where the goods are being "+
      "held- take out the goons and hang them out to dry.",
      this, LEAD_STASH, stash, this, Task.TIME_LONG,
      CLOSE_COMBAT, 5, MARKSMAN, 5
    ));
  }
  
  
  protected boolean checkFollowed(Lead lead, boolean success) {
    final Nation nation = world().nationFor(region());
    final Events events = world().events();
    
    
    if (lead.ID == LEAD_STASH) {
      setComplete(true);
      
      if (success) {
        events.log("The stolen valuables have been recovered.");
        nation.incTrust(2);
      }
      else {
        for (Person p : lead.assigned()) {
          p.receiveInjury(2);
        }
        events.log(
          "Your bust was a failure.  The perps will have scattered and taken "+
          "the cash with them- you'll never find it now."
        );
        nation.incCrime(5);
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
      Nation nation  = (Nation) Rand.pickFrom(world.nations());
      
      Event s = new Robbery(boss, "The Central Bank", nation.region, world);
      float time = world.timeDays() + Rand.index(5);
      s.assignDates(time + 2, time + 7);
      return s;
    }
    
    public float eventChance(Event event) {
      return 0;
    }
  };
}











