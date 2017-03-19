

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public class PlotUtils {
  
  
  public static void fillExpertRole(
    Plot plot, Trait trait, Series <Person> candidates, Plot.Role role
  ) {
    Pick <Person> pick = new Pick();
    for (Person p : candidates) {
      if (plot.roleFor(p, null) != null) continue;
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
      if (plot.roleFor(p, null) != null) continue;
      pick.compare(p, 0 - p.history.valueFor(target.owner()));
    }
    if (pick.empty()) return;
    plot.assignRole(pick.result(), role);
  }
  
  
  public static void fillItemRole(
    Plot plot, ItemType type, World world, Plot.Role role
  ) {
    plot.assignRole(new Item(type, world), role);
  }
  
  
  public static Series <Person> goonsOnRoster(
    Plot plot
  ) {
    Batch <Person> goons = new Batch();
    for (Person p : plot.base.roster()) {
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
}




