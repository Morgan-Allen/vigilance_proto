

package proto.content.events;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import proto.content.agents.*;
import static proto.content.agents.Crooks.*;




public class Murder extends Event {
  
  final static int
    LEAD_MORGUE  = 0,
    LEAD_PARTNER = 1,
    LEAD_RIVAL   = 2,
    LEAD_WEAPON  = 3;
  
  Area  morgue ;
  Person victim ;
  Person partner;
  Person rival  ;
  Area  hides  ;
  Clue   weapon ;
  
  
  public Murder(Session s) throws Exception {
    super(s);
    morgue  = (Area ) s.loadObject();
    victim  = (Person) s.loadObject();
    partner = (Person) s.loadObject();
    rival   = (Person) s.loadObject();
    hides   = (Area ) s.loadObject();
    weapon  = (Clue  ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObjects(morgue, victim, partner, rival, hides, weapon);
  }
  
  
  
  public Murder(Person victim, Region region, World world) {
    super(
      "Murder of "+victim.name(),
      victim.name()+" was found dead in a deserted alleyway late last night "+
      "in "+region+".  Find their killer.",
      world, region
    );
    
    this.morgue  = new Area("The Morgue", region);
    this.victim  = victim;
    this.rival   = Crooks.randomCivilian(world);
    this.partner = Crooks.randomCivilian(world);
    this.hides   = new Area("Hidden Area", region);
    this.weapon  = new Clue("Murder Weapon");
    this.setKnown(morgue, victim, partner, rival);
    
    this.assignLeads(new Lead(
      "Visit the morgue",
      "Visit the morgue and examine the body for clues to how they died.",
      this, LEAD_MORGUE, morgue, weapon, Task.TIME_MEDIUM,
      DISGUISE, 3, ANATOMY, 4
    ));
    this.assignLeads(new Lead(
      "Talk to their partner",
      "Talk to "+partner+", their partner, to get some idea of what "+victim+
      " was involved in.",
      this, LEAD_PARTNER, partner, weapon, Task.TIME_SHORT,
      QUESTION, 2, SUASION, 3
    ));
    this.assignLeads(new Lead(
      "Talk to their rival",
      rival+" was seen involved in a series of heated arguments with "+victim+
      ".  Try leaning on them.",
      this, LEAD_RIVAL, rival, weapon, Task.TIME_SHORT,
      QUESTION, 3, INTIMIDATE, 4
    ));
    this.assignLeads(new Lead(
      "Find the murder weapon",
      "You have reason to believe "+partner+" killed "+victim+" out of "+
      "jealousy and tried to implicate "+rival+".  Find the murder weapon.",
      this, LEAD_WEAPON, hides, weapon, Task.TIME_SHORT,
      SURVEILLANCE, 7
    ));
  }
  
  
  protected boolean checkFollowed(Lead lead, boolean success) {
    final District nation = world().districtFor(region());
    final Events events = world().events();
    
    final boolean
      morgueSolved  = leadWithID(LEAD_MORGUE ).success(),
      partnerSolved = leadWithID(LEAD_PARTNER).success(),
      rivalSolved   = leadWithID(LEAD_RIVAL  ).success()
    ;
    boolean weaponSolved = false;
    
    
    if (success && lead.ID == LEAD_MORGUE) {
      events.log(
        "It looks as though "+victim+" took several small-calibre shots to "+
        "the chest, but close inspection indicates blunt force trauma to "+
        "the head was the true cause of death, preceding the gunshots by "+
        "several hours."
      );
      if (partnerSolved) {
        events.log("What "+partner+" told you doesn't add up.");
        weaponSolved = true;
      }
      if (rivalSolved) {
        events.log("This seems to collaborate what "+rival+" admitted before.");
        weaponSolved = true;
      }
    }
    if (success && lead.ID == LEAD_PARTNER) {
      events.log(
        partner+" claims that "+rival+" and the victim had been involved "+
        "in an affair, and that the latter kept a gun in their desk drawer. "+
        "When "+victim+" broke it off, they must have snapped."
      );
      if (morgueSolved) {
        events.log("This isn't consistent with the forensics from the morgue.");
        weaponSolved = true;
      }
      if (rivalSolved) {
        events.log(rival+" swore they found the victim dead.  Was that a lie?");
        weaponSolved = true;
      }
    }
    if (success && lead.ID == LEAD_RIVAL) {
      events.log(
        rival+" admits to having an affair with "+victim+", but swears that "+
        "they were dead for hours before getting home.  They panicked and fled."
      );
      if (morgueSolved) {
        events.log("The autopsy did suggest an earlier time of death.");
        weaponSolved = true;
      }
      if (partnerSolved) {
        events.log(partner+" is telling a different story.  Who is lying?");
        weaponSolved = true;
      }
    }
    
    if (weaponSolved) {
      setKnown(hides);
    }
    
    if (lead.ID == LEAD_WEAPON) {
      setComplete(true);
      
      if (success) {
        events.log(
          "You're in luck. "+partner+" didn't have time to sterilise the "+
          "scene properly.  You find a bloody paperweight stashed under the "+
          "floorboards that matches the victim's pattern of contusions."
        );
        nation.incLevel(District.TRUST     , 2);
        nation.incLevel(District.DETERRENCE, 5);
      }
      else {
        events.log(
          "This looks like a dead end.  Lacking conclusive evidence, you'll "+
          "have to let this drop..."
        );
        nation.incLevel(District.DETERRENCE, -5);
      }
    }
    
    return super.checkFollowed(lead, success);
  }
  
  
  
  /**  And, last but not least, a generator/type for the event-
    */
  final public static EventType TYPE = new EventType(
    "Murder", "event_murder"
  ) {
    public Event createRandomEvent(World world) {
      
      Person victim = new Person(CIVILIAN, world, randomCommonName());
      District nation  = (District) Rand.pickFrom(world.districts());
      
      Event s = new Murder(victim, nation.region, world);
      float time = world.timeDays() + Rand.index(5);
      s.assignDates(time + 2, time + 4);
      return s;
    }
    
    public float eventChance(Event event) {
      return 1;
    }
  };
}











