

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;



public class PlotUtils {
  
  
  /**  Helper methods for filling Roles:
    */
  public static Place chooseHideout(
    Plot plot, Place crimeScene, Place HQ
  ) {
    Pick <Place > pickH = new Pick();
    
    for (Place b : venuesNearby(crimeScene, 1)) {
      if (b.isHQ() || b == crimeScene) continue;
      pickH.compare(b, Rand.num());
    }
    
    if (pickH.result() == HQ) {
      I.say("Picked wrong hideout!");
      I.say("  HQ is HQ? "+HQ.isHQ());
      I.say("?");
    }
    
    return pickH.result();
  }
  
  
  public static void fillExpertRole(
    Plot plot, Trait trait, Series <Person> candidates, Role role
  ) {
    fillExpertRole(plot, trait, candidates, role, Plot.ROLE_HIDEOUT);
  }
  
  
  public static void fillExpertRole(
    Plot plot, Trait trait, Series <Person> candidates,
    Role role, Role placing
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : candidates) {
      if (p.isCaptive() || ! p.health.conscious()) continue;
      if (plot.entryFor(p, null) != null) continue;
      pick.compare(p, p.stats.levelFor(trait));
    }
    if (pick.empty()) return;
    plot.assignRole(pick.result(), role, placing);
  }
  
  
  public static void fillInsideRole(
    Plot plot, Place target,
    Role role, Role placing
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : target.residents()) {
      if (p.isCaptive() || ! p.health.conscious()) continue;
      if (plot.entryFor(p, null) != null) continue;
      pick.compare(p, 0 - p.history.bondWith(target.base().leader()));
    }
    if (pick.empty()) return;
    plot.assignRole(pick.result(), role, placing);
  }
  
  
  public static void fillItemRole(
    Plot plot, ItemType type, World world,
    Role role, Role placing
  ) {
    plot.assignRole(new Item(type, world), role, placing);
  }
  
  
  public static Series <Person> aidesOnRoster(
    Plot plot
  ) {
    Batch <Person> goons = new Batch();
    for (Person p : plot.base.roster()) {
      if (p.isCaptive() || ! p.health.conscious()) continue;
      if (p == plot.base.leader()) continue;
      goons.add(p);
    }
    return goons;
  }
  
  
  public static Series <Person> expertsWith(
    Trait trait, int minLevel, Plot plot
  ) {
    final Batch <Person> experts = new Batch();
    for (Element e : plot.world().inside()) if (e.isPerson()) {
      Person p = (Person) e;
      if (p.isCaptive() || ! p.health.conscious()) continue;
      if (p.stats.levelFor(trait) < minLevel) continue;
      experts.add(p);
    }
    return experts;
  }
  
  
  
  /**  Additional helper methods-
    */
  public static Series <Place> venuesNearby(
    Place target, int maxDist
  ) {
    final Batch <Place> venues = new Batch();
    for (Region r : target.world().regionsInRange(target.region(), maxDist)) {
      for (Place p : r.buildSlots()) if (p != null) venues.include(p);
    }
    return venues;
  }
  
  
  public static Place chooseTipoffSite(Element source) {
    Series <Place> near = venuesNearby(source.place(), 1);
    return (Place) Rand.pickFrom(near);
  }
  
  
  public static Place selectNewHQFor(Base base) {
    Place oldHQ = base.HQ();
    Pick <Place> pick = new Pick();
    
    for (Place p : base.world().places()) {
      if (p.base() != base || p == oldHQ) continue;
      pick.compare(p, Rand.num());
    }
    
    if (pick.empty()) return null;
    
    Place newHQ = pick.result();
    base.assignHQ(newHQ);
    return newHQ;
  }
  
  
  public static void wipeFactionAssets(Base base) { 
    for (Place p : base.world().places()) {
      if (p.base() != base || p == base.HQ()) continue;
      p.setBase(null);
    }
  }
  
  
  
  /**  Helper methods for dealing with perps before and after a scene:
    */
  public static Scene generateHideoutScene(
    Plot plot, Element focus, Task lead
  ) {
    //  TODO:  This should change later!
    return generateHeistScene(plot, focus, lead);
  }
  
  
  public static Scene generateSideScene(
    Plot plot, Element focus, Task lead
  ) {
    //  TODO:  This should change later!
    return generateHeistScene(plot, focus, lead);
  }
  
  
  public static Scene generateHeistScene(
    Plot plot, Element focus, Task lead
  ) {
    Base  base  = plot.base();
    World world = base.world();
    Place place = focus.place();
    Series <Element> involved = plot.allInvolved();
    List <Person> forces = new List();
    
    final float dangerLevel = 0.5f;
    final PersonType GOONS[] = base.goonTypes().toArray(PersonType.class);
    float forceLimit = dangerLevel * 10, forceSum = 0;
    
    for (Element e : involved) {
      if (e == null || e.place() != place || ! e.isPerson()) continue;
      Person perp = (Person) e;
      forces.add(perp);
      if      (perp.isCriminal()) forceSum += perp.stats.powerLevel();
      else if (perp.isCivilian()) perp.setCaptive(true);
    }
    
    while (forceSum < forceLimit && forces.size() < (forceLimit * 2)) {
      PersonType ofGoon = (PersonType) Rand.pickFrom(GOONS);
      Person goon = Person.randomOfKind(ofGoon, base.world());
      goon.setBase(plot.base());
      float power = goon.stats.powerLevel();
      forceSum += power;
      forces.add(goon);
    }
    
    if (forces.empty()) return null;
    
    Scene scene = place.kind().sceneType().generateScene(world);
    scene.entry.provideInProgressEntry(forces, Nums.ceil(forces.size() / 4f));
    
    Batch <Person> onSite = new Batch();
    for (Person p : lead.base.roster()) {
      if (p.place() == place) onSite.add(p);
    }
    scene.entry.provideBorderEntry(onSite);
    scene.assignMissionParameters(place, lead, plot);
    
    return scene;
  }
  
  
  public static EventEffects generateSceneEffects(
    Scene scene, Plot plot, Lead lead
  ) {
    World    world  = scene.world();
    Base     player = world.playerBase();
    int      time   = world.timing.totalHours();
    CaseFile file   = player.leads.caseFor(plot);
    Place    site   = scene.site();
    Batch <Person  > captives = new Batch();
    Batch <CaseFile> evidence = new Batch();
    
    I.say("Generating scene effects after: "+plot);
    
    for (Person p : scene.didEnter()) {
      float
        damage      = p.health.totalHarm() / p.health.maxHealth(),
        bruiseLevel = PersonHealth.HP_BRUISE_PERCENT / 100f,
        deathLevel  = 100 / 100f;
      
      float critRisk = Nums.clamp(damage - bruiseLevel, 0, 1);
      float diesRisk = Nums.clamp(damage - deathLevel , 0, 1);
      critRisk *= critRisk;
      diesRisk *= diesRisk;
      
      if (Rand.num() < diesRisk) {
        p.health.setState(PersonHealth.State.DECEASED);
      }
      else if (Rand.num() < critRisk) {
        p.health.setState(PersonHealth.State.CRITICAL);
      }
      else if (damage > bruiseLevel) {
        p.health.setState(PersonHealth.State.BRUISED);
      }
      p.health.updateHealth(0);
      
      if (p.isCriminal() && p.currentScene() == scene && scene.wasWon()) {
        //
        //  Anyone caught red-handed at the scene will have evidence recorded
        //  as such-
        Role role = plot.roleFor(p);
        if (role != null) {
          Clue redHanded = Clue.confirmSuspect(plot, role, p, site);
          file.recordClue(redHanded, lead, time, site, false);
          captives.add(p);
          evidence.add(file);
        }
        //
        //  If the mastermind has been deposed, clear the base's HQ and record
        //  the event (with a slight offset to ensure it comes ahead of other
        //  clues.)  Then select a new HQ!
        if (p == plot.base.leader()) {
          Place HQ = p.base().HQ();
          Clue busted = Clue.confirmSuspect(plot, Plot.ROLE_HQ, HQ, site);
          file.recordClue(busted, LeadType.MAJOR_BUST, time + 1, site);
          selectNewHQFor(p.base());
        }
      }
      if (p.isCivilian() && p.isCaptive() && ! p.isCriminal()) {
        p.setCaptive(false);
      }
    }
    
    world.council.scheduleTrial(plot, captives, evidence);
    
    EventEffects effects = new EventEffects(plot, scene.site());
    effects.composeFromScene(scene);
    return effects;
  }
  
  
  
  
}


