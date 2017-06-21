

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;



public class SceneEntry implements TileConstants {
  
  
  final Scene scene;
  
  static class Pod {
    Coord gridPoint = null;
    Tile tilePoint = null;
    //Tile patrolPoints[] = new Tile[0];
    List <Person> assigned = new List();
  }
  
  List <Pod> pods = new List();
  
  
  
  SceneEntry(Scene scene) {
    this.scene = scene;
  }
  
  
  void loadState(Session s) throws Exception {
    return;
  }
  
  
  void saveState(Session s) throws Exception {
    return;
  }
  
  
  public boolean provideBorderEntry(Series <Person> forces) {
    Pick <Coord> gridPoint = new Pick();
    //
    //  We try and find an entry point that's unobstructed and not too close
    //  to any enemy pods:
    for (Coord c : Visit.perimeter(1, 1, scene.gridW - 2, scene.gridH - 2)) {
      Scenery.Island under = scene.islandUnderGrid(c.x, c.y);
      
      if (under != null && ! under.exterior()) continue;
      float dist = 0;
      for (Pod other : pods) {
        dist += other.gridPoint.axisDistance(c);
      }
      
      gridPoint.compare(new Coord(c), dist * (1 + Rand.num()));
    }
    if (gridPoint.empty()) return false;
    //
    //  Then we insert the team around that point:
    Pod pod = new Pod();
    pod.gridPoint = gridPoint.result();
    pod.tilePoint = scene.gridMidpoint(pod.gridPoint);
    pods.add(pod);
    int sx = pod.tilePoint.x, sy = pod.tilePoint.y;
    
    for (Person p : forces) {
      p.addAssignment(scene);
      p.mind.setDoing(PersonMind.STATE_ACTIVE);
      pod.assigned.add(p);
      Tile t = findEntryPoint(sx, sy, p);
      scene.enterScene(p, t.x, t.y);
    }
    return true;
  }
  
  
  public void provideInProgressEntry(
    Series <Person> forces, int numPods
  ) {
    I.say("Scene is: "+scene.hashCode()+"/"+scene.getClass());
    I.say("Total rooms: "+scene.rooms.size());
    //
    //  We divide up the forces among numPods different pods, and place those
    //  at various points around the map:
    List <Person> forcesLeft = new List();
    Visit.appendTo(forcesLeft, forces);
    int perPod = Nums.ceil(forces.size() * 1f / numPods);
    
    for (int i = numPods; i-- > 0;) {
      Pod pod = new Pod();
      
      for (int n = perPod; n-- > 0 && ! forcesLeft.empty();) {
        Person p = (Person) Rand.pickFrom(forcesLeft);
        forcesLeft.remove(p);
        pod.assigned.add(p);
      }
      //
      //  The points in question should be reasonably clear of obstructions,
      //  and not too close to the edge of the map.
      Pick <Tile> pickEntry = new Pick();
      search: for (Scenery.Room room : scene.rooms) {
        
        Tile open = findOpenArea(room);
        float rating = 1;
        
        for (Pod other : pods) {
          float dist = scene.distance(open, other.tilePoint);
          if (dist < 8) continue search;
          rating += dist / 8f;
        }
        
        if (room.unit.exterior) rating /= 2;
        rating *= 0.5f + Rand.avgNums(2);
        pickEntry.compare(open, rating);
      }
      if (pickEntry.empty()) continue;
      
      pod.tilePoint = pickEntry.result();
      pod.gridPoint  = new Coord(scene.gridPoint(pod.tilePoint));
      //
      //  TODO:  Goons on-base should be unwary by default- not necessarily so
      //  on a heist.
      for (Person p : pod.assigned) {
        Tile entry = findEntryPoint(pod.tilePoint.x, pod.tilePoint.y, p);
        if (entry == null) continue;
        p.addAssignment(scene);
        p.mind.setDoing(PersonMind.STATE_UNAWARE);
        scene.enterScene(p, entry.x, entry.y);
      }
      
      pods.add(pod);
    }
  }
  
  
  private Tile findOpenArea(Scenery.Room room) {
    Tile point = scene.tileAt(
      room.minX + (room.wide / 2),
      room.minY + (room.high / 2)
    );
    Tile temp[] = new Tile[8];
    Pick <Tile> pick = new Pick();
    
    for (int tries = 4; tries-- > 0;) {
      float numOpen = point.blocked() ? 0 : 1;
      for (Tile n : point.tilesAdjacent(temp)) {
        if (n != null && ! n.blocked()) numOpen++;
      }
      pick.compare(point, numOpen);
      
      if (numOpen == 9) break;
      point = scene.tileAt(
        room.minX + Rand.index(room.wide),
        room.minY + Rand.index(room.high)
      );
    }
    
    return pick.result();
  }
  
  
  public void provideCivilianEntry(int numCivilians) {
    //  TODO:  You also need to populate a scene with civilian passers-by,
    //  hostages and/or resident workers.
  }
  
  
  private Tile findEntryPoint(int x, int y, Person enters) {
    Tile under = scene.tileAt(x, y);
    int wide = scene.wide(), high = scene.high();
    int dir = T_INDEX[Rand.index(T_INDEX.length)];
    
    while (under != null) {
      if (x != Nums.clamp(x, wide) || y != Nums.clamp(y, high)) break;
      if (! (under.blocked() || under.occupied())) return under;
      x += T_X[dir];
      y += T_Y[dir];
      under = scene.tileAt(x, y);
    }
    return under;
  }
  
  
  public boolean isExitPoint(Object point, Person exits) {
    Tile under = scene.tileUnder(point);
    int wide = scene.wide(), high = scene.high();
    if (under == null || exits == null || ! exits.mind.retreating()) {
      return false;
    }
    if (under.x == 0 || under.x == wide - 1) return true;
    if (under.y == 0 || under.y == high - 1) return true;
    return false;
  }
  
  
  
}





