
package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;




public class AIUtils implements TileConstants {
  
  
  
  static float rateTileDefence(
    Tile at, Person p, Series <Person> foes, float baseRange, boolean advance
  ) {
    Scene scene = (Scene) at.scene;
    float rating = 0, sumProximity = 0;
    float baseFear = 0.25f + Nums.clamp(1 - p.mind.confidence(), 0, 0.75f);
    
    for (int dir : T_ADJACENT) {
      float cover = at.coverLevel(dir) * 1f / Kind.BLOCK_FULL;
      int oppDir = dir + 4 % 8, threatFromDir = 1;
      for (Person f : foes) {
        if (scene.direction(at, f.currentTile()) == oppDir) threatFromDir++;
      }
      rating += cover * threatFromDir * baseFear;
    }
    
    if (advance) for (Person f : foes) {
      float distance = scene.distance(at, f.currentTile());
      sumProximity += baseRange / (distance + baseRange);
    }
    rating *= 1 + (sumProximity / Nums.max(1, foes.size()));
    
    rating += Rand.num() * 0.25f;
    return rating;
  }
  
  
  static Action pickAdvanceAction(Person person, Series <Person> foes) {
    I.say("  Picking advance action for "+person);
    
    Scene scene = person.currentScene();
    Tile orig = person.currentTile();
    int range = 2 + Nums.ceil(person.stats.sightRange() / 2);
    Box2D area = new Box2D(orig.x + 0.5f, orig.y + 0.5f, 0, 0).expandBy(range);
    
    final Pick <Action> pick = new Pick(0);
    
    for (Tile t : scene.tilesInArea(area)) {
      if (t == null || t.blocked() || t.occupied()) continue;
      float rating = rateTileDefence(t, person, foes, range, true);
      if (rating <= 0) continue;
      
      Action motion = bestMotionToward(t, person, scene);
      if (motion == null || motion.target != t) continue;
      pick.compare(motion, rating);
    }
    
    if (! pick.empty()) {
      I.say("  "+person+" taking cover at: "+pick.result());
      return pick.result();
    }

    for (Person p : foes) {
      Action motion = bestMotionToward(p, person, scene);
      float proximity = range / (scene.distance(p, person) + range);
      pick.compare(motion, proximity);
    }
    
    if (! pick.empty()) {
      I.say("  "+person+" moving toward foe at: "+pick.result());
      return pick.result();
    }
    
    int randX = orig.x + ((Rand.index(range) + 1) * (Rand.yes() ? 1 : -1));
    int randY = orig.y + ((Rand.index(range) + 1) * (Rand.yes() ? 1 : -1));
    Tile random = scene.tileAt(
      Nums.clamp(randX, scene.wide()),
      Nums.clamp(randY, scene.high())
    );
    
    I.say("  "+person+" picked random tile to approach: "+random);
    return bestMotionToward(random, person, scene);
  }
  
  
  static Action pickRetreatAction(Person person, Series <Person> foes) {
    Scene scene = person.currentScene();
    
    int wide = scene.wide(), high = scene.high();
    Tile exits[] = {
      scene.tileAt(0       , high / 2),
      scene.tileAt(wide / 2, 0       ),
      scene.tileAt(wide - 1, high / 2),
      scene.tileAt(wide / 2, high - 1)
    };
    
    final Pick <Tile> pick = new Pick();
    final Tile location = person.currentTile();
    for (Tile t : exits) pick.compare(t, 0 - scene.distance(t, location));
    return bestMotionToward(pick.result(), person, scene);
  }
  
  
  static Action bestMotionToward(Object point, Person acting, Scene scene) {
    Tile at = scene.tileUnder(point);
    if (at == null) return null;
    
    if (point instanceof Person && ! acting.actions.checkToNotice(point)) {
      return null;
    }
    MoveSearch search = new MoveSearch(acting, acting.location, at);
    search.doSearch();
    if (! search.success()) return null;
    
    Ability moving = Common.MOVE;
    Tile path[] = search.fullPath(Tile.class);
    
    for (int n = path.length; n-- > 0;) {
      Tile shortPath[] = new Tile[n + 1], t = path[n];
      System.arraycopy(path, 0, shortPath, 0, n + 1);
      Action use = moving.configAction(acting, t, t, scene, shortPath, null);
      if (use != null) return use;
    }
    return null;
  }
  
  
}




