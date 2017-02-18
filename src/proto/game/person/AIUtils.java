
package proto.game.person;
import proto.common.*;
import proto.game.scene.*;
import proto.util.*;




public class AIUtils implements TileConstants {
  
  
  
  static float rateTileDefence(
    Tile at, Person p, Series <Person> foes, float baseRange, boolean advance
  ) {
    Scene scene = at.scene;
    float rating = 0, sumProximity = 0;
    float baseFear = 0.25f + Nums.clamp(1 - p.mind.confidence(), 0, 0.75f);
    
    for (int dir : T_ADJACENT) {
      float cover = at.coverVal(dir) * 1f / Kind.BLOCK_FULL;
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
      
      Action motion = Common.MOVE.bestMotionToward(t, person, scene);
      if (motion == null || motion.target != t) continue;
      pick.compare(motion, rating);
    }
    
    if (! pick.empty()) {
      I.say("  "+person+" taking cover at: "+pick.result());
      return pick.result();
    }

    for (Person p : foes) {
      Action motion = Common.MOVE.bestMotionToward(p, person, scene);
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
      Nums.clamp(randX, scene.size()),
      Nums.clamp(randY, scene.size())
    );
    
    I.say("  "+person+" picked random tile to approach: "+random);
    return Common.MOVE.bestMotionToward(random, person, scene);
  }
  
  
  static Action pickRetreatAction(Person person, Series <Person> foes) {
    Scene scene = person.currentScene();
    int hS = scene.size() / 2, sD = scene.size() - 1;
    Tile exits[] = {
      scene.tileAt(0 , hS),
      scene.tileAt(hS, 0 ),
      scene.tileAt(sD, hS),
      scene.tileAt(hS, sD)
    };
    
    final Pick <Tile> pick = new Pick();
    final Tile location = person.currentTile();
    for (Tile t : exits) pick.compare(t, 0 - scene.distance(t, location));
    return Common.MOVE.bestMotionToward(pick.result(), person, scene);
  }
  
  
  
  
  
}