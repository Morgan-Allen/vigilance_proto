

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;



public class PlotUtils {
  
  
  /**  Helper methods for filling Roles:
    */
  public static void fillHideoutRole(
    Plot plot, Place crimeScene
  ) {
    Pick <Place > pickH = new Pick();
    for (Place b : venuesNearby(plot, crimeScene, 1)) {
      if (b.isBase() || b == crimeScene) continue;
      pickH.compare(b, Rand.num());
    }
    if (pickH.empty()) return;
    Place hideout = pickH.result();
    plot.assignRole(hideout, Plot.ROLE_HIDEOUT);
  }
  
  
  public static void fillExpertRole(
    Plot plot, Trait trait, Series <Person> candidates, Plot.Role role
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : candidates) {
      if (p.isCaptive() || ! p.health.conscious()) continue;
      if (plot.entryFor(p, null) != null) continue;
      pick.compare(p, p.stats.levelFor(trait));
    }
    if (pick.empty()) return;
    plot.assignRole(pick.result(), role);
  }
  
  
  public static void fillInsideRole(
    Plot plot, Place target, Plot.Role role
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : target.residents()) {
      if (p.isCaptive() || ! p.health.conscious()) continue;
      if (plot.entryFor(p, null) != null) continue;
      pick.compare(p, 0 - p.history.bondWith(target.owner()));
    }
    if (pick.empty()) return;
    plot.assignRole(pick.result(), role);
  }
  
  
  public static void fillItemRole(
    Plot plot, ItemType type, World world, Plot.Role role
  ) {
    plot.assignRole(new Item(type, world), role);
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
  
  
  
  /**  Helper methods for queueing steps:
    */
  
  
  
  
  /**  Helper methods for dealing with perps before and after a scene:
    */
  public static Scene generateHideoutScene(
    Plot plot, Step step, Element focus, Task lead
  ) {
    //  TODO:  This should change later!
    return generateHeistScene(plot, step, focus, lead);
  }
  
  
  public static Scene generateHeistScene(
    Plot plot, Step step, Element focus, Task lead
  ) {
    if (! Visit.arrayIncludes(step.involved(), focus)) {
      I.say("Step: "+step+" does not involve: "+focus);
      return null;
    }
    
    Base  base  = plot.base();
    World world = base.world();
    Place place = focus.place();
    Scene scene = place.kind().sceneType().generateScene(world);
    
    final float dangerLevel = 0.5f;
    final PersonType GOONS[] = base.goonTypes().toArray(PersonType.class);
    float forceLimit = dangerLevel * 10, forceSum = 0;
    
    final List <Person> forces = new List();
    for (Element e : step.involved()) if (e != null && e.isPerson()) {
      Person perp = (Person) e;
      forces.add(perp);
      forceSum += perp.stats.powerLevel();
    }
    
    while (forceSum < forceLimit) {
      PersonType ofGoon = (PersonType) Rand.pickFrom(GOONS);
      Person goon = Person.randomOfKind(ofGoon, base.world());
      forceSum += goon.stats.powerLevel();
      goon.setBase(plot.base());
      forces.add(goon);
    }
    
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
        
        CaseFile file = player.leads.caseFor(p);
        Plot.Role role = plot.roleFor(p);
        if (role == null) role = Plot.ROLE_GOON;
        Clue redHanded = new Clue(plot, role);
        redHanded.confirmMatch(p, lead, time, scene.site());
        file.recordClue(redHanded, null, false);
        
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


