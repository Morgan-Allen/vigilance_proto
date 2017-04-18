

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
    Plot plot, Place crimeScene
  ) {
    Pick <Place > pickH = new Pick();
    for (Place b : venuesNearby(plot, crimeScene, 1)) {
      if (b.isBase() || b == crimeScene) continue;
      pickH.compare(b, Rand.num());
    }
    return pickH.result();
  }
  
  
  public static void fillExpertRole(
    Plot plot, Trait trait, Series <Person> candidates, Plot.Role role
  ) {
    fillExpertRole(plot, trait, candidates, role, Plot.ROLE_HIDEOUT);
  }
  
  
  public static void fillExpertRole(
    Plot plot, Trait trait, Series <Person> candidates,
    Plot.Role role, Plot.Role placing
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
    Plot.Role role, Plot.Role placing
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : target.residents()) {
      if (p.isCaptive() || ! p.health.conscious()) continue;
      if (plot.entryFor(p, null) != null) continue;
      pick.compare(p, 0 - p.history.bondWith(target.owner()));
    }
    if (pick.empty()) return;
    plot.assignRole(pick.result(), role, placing);
  }
  
  
  public static void fillItemRole(
    Plot plot, ItemType type, World world,
    Plot.Role role, Plot.Role placing
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
  
  
  public static Series <Place> venuesNearby(
    Plot plot, Place target, int maxDist
  ) {
    final Batch <Place> venues = new Batch();
    for (Region r : plot.world.regionsInRange(target.region(), maxDist)) {
      for (Place p : r.buildSlots()) if (p != null) venues.include(p);
    }
    return venues;
  }
  
  
  
  /**  Helper methods for dealing with perps before and after a scene:
    */
  public static Scene generateHideoutScene(
    Plot plot, Step step, Element focus, Task lead
  ) {
    //  TODO:  This should change later!
    return generateHeistScene(plot, step, focus, lead);
  }
  
  
  public static Scene generateSideScene(
    Plot plot, Step step, Element focus, Task lead
  ) {
    //  TODO:  This should change later!
    return generateHeistScene(plot, step, focus, lead);
  }
  
  
  public static Scene generateHeistScene(
    Plot plot, Step step, Element focus, Task lead
  ) {
    Base  base  = plot.base();
    World world = base.world();
    Place place = focus.place();
    Series <Element> involved = plot.involved(step);
    
    if (! involved.includes(focus)) {
      I.say("Step: "+step+" does not involve: "+focus);
      return null;
    }
    
    final float dangerLevel = 0.5f;
    final PersonType GOONS[] = base.goonTypes().toArray(PersonType.class);
    float forceLimit = dangerLevel * 10, forceSum = 0;
    
    final List <Person> forces = new List();
    for (Element e : involved) {
      if (e == null || e.place() != place || ! e.isPerson()) continue;
      Person perp = (Person) e;
      forces.add(perp);
      if (perp.isCriminal()) forceSum += perp.stats.powerLevel();
    }
    if (forces.empty()) return null;
    
    while (forceSum < forceLimit && forces.size() < (forceLimit * 2)) {
      PersonType ofGoon = (PersonType) Rand.pickFrom(GOONS);
      Person goon = Person.randomOfKind(ofGoon, base.world());
      goon.setBase(plot.base());
      float power = goon.stats.powerLevel();
      forceSum += power;
      forces.add(goon);
    }
    
    Scene scene = place.kind().sceneType().generateScene(world);
    scene.entry.provideInProgressEntry(forces);
    scene.entry.provideBorderEntry(lead.assigned());
    scene.assignMissionParameters(place, lead, plot);
    
    return scene;
  }
  
  
  public static EventEffects generateSceneEffects(
    Scene scene, Plot plot, Lead lead
  ) {
    World world = scene.world();
    Base player = world.playerBase();
    Batch <Person  > captives = new Batch();
    Batch <CaseFile> evidence = new Batch();
    int time = world.timing.totalHours();
    
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
        CaseFile  file = player.leads.caseFor(plot);
        Plot.Role role = plot.roleFor(p);
        Place     site = scene.site();
        Clue redHanded = Clue.confirmSuspect(plot, role, p, site);
        file.recordClue(redHanded, lead, time, site, false);
        captives.add(p);
        evidence.add(file);
      }
    }
    
    world.council.scheduleTrial(plot, captives, evidence);
    
    EventEffects effects = new EventEffects();
    effects.composeFromScene(scene);
    return effects;
  }
  
}


