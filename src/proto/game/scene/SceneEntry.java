

package proto.game.scene;
import proto.game.person.*;
import proto.util.*;



public class SceneEntry implements TileConstants {
  
  
  final Scene scene;
  
  
  SceneEntry(Scene scene) {
    this.scene = scene;
  }
  
  

  //  TODO:  You also need to populate a scene with civilian passerbys and/or
  //  hostages and/or resident workers...
  
  
  public void provideBorderEntry(Series <Person> forces) {
    int across = (scene.size() - (forces.size())) / 2;
    for (Person p : forces) {
      p.addAssignment(scene);
      scene.enterScene(p, across++, 0);
    }
  }
  
  
  public void provideInProgressEntry(Series <Person> forces) {
    
    //  TODO:  It would be nice to have multiple 'pods' of enemies going on
    //  patrol around areas of the scene.
    
    int range = scene.size() / 4;
    int nX = scene.size() / 2, nY = scene.size() / 2;
    nX += 5 - Rand.index(range);
    nY += 5 - Rand.index(range);
    
    for (Person p : forces) {
      Tile entry = findEntryPoint(nX, nY, p);
      if (entry == null) continue;
      p.addAssignment(scene);
      scene.enterScene(p, entry.x, entry.y);
    }
  }
  
  
  
  public Tile findEntryPoint(int x, int y, Person enters) {
    Tile under = scene.tileAt(x, y);
    int dir = T_INDEX[Rand.index(T_INDEX.length)], size = scene.size();
    
    while (under != null) {
      if (x != Nums.clamp(x, size) || y != Nums.clamp(y, size)) break;
      if (! under.blocked()) return under;
      x += T_X[dir];
      y += T_Y[dir];
      under = scene.tileAt(x, y);
    }
    return under;
  }
  
  
  public boolean isExitPoint(Object point, Person exits) {
    Tile under = scene.tileUnder(point);
    int size = scene.size();
    if (under == null || exits == null || ! exits.mind.retreating()) {
      return false;
    }
    if (under.x == 0 || under.x == size - 1) return true;
    if (under.y == 0 || under.y == size - 1) return true;
    return false;
  }
  
  
  
}





