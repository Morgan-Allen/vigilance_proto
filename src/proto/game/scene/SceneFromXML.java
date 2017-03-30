

package proto.game.scene;
import proto.common.*;
import proto.util.*;

import java.util.StringTokenizer;



public class SceneFromXML implements TileConstants {
  
  
  public static PropType propWithID(String ID, String file, String basePath) {
    return propWithID(ID, getCachedXML(basePath+file), basePath);
  }
  
  
  public static SceneType sceneWithID(String ID, String file, String basePath) {
    return sceneWithID(ID, getCachedXML(basePath+file), basePath);
  }
  
  
  public static XML getCachedXML(String xmlPath) {
    XML cached = (XML) Assets.getResource(xmlPath);
    if (cached == null) {
      Assets.cacheResource(cached = XML.load(xmlPath), xmlPath);
    }
    return cached;
  }
  
  
  static PropType propWithID(String ID, XML file, String basePath) {
    String key = basePath.toLowerCase()+"_"+ID;
    PropType cached = (PropType) Assets.getResource(key);
    if (cached != null) return cached;

    for (XML node : file.allChildrenMatching("prop")) {
      String propKey = basePath.toLowerCase()+"_"+node.value("ID");
      if (Assets.getResource(propKey) != null) continue;
      Assets.cacheResource("PROP_HOLDER", propKey);
      
      PropType type = new PropType(
        node.value("name"), propKey,
        basePath+node.value("sprite"),
        Kind.loadField(node.value("subtype")),
        node.getInt("wide"),
        node.getInt("high"),
        Kind.loadField(node.value("blockLevel")),
        node.getBool("blockSight")
      );
      Assets.cacheResource(type, propKey);
    }
    
    Object match = Assets.getResource(key);
    if (match instanceof PropType) return (PropType) match;
    
    return null;
  }
  
  
  public static SceneType sceneWithID(String ID, XML file, String basePath) {
    String key = basePath.toLowerCase()+"_"+ID;
    Object cached = Assets.getResource(key);
    if (cached instanceof SceneType) return (SceneType) cached;
    
    for (XML node : file.allChildrenMatching("scene")) {
      String sceneKey = basePath.toLowerCase()+"_"+node.value("ID");
      if (Assets.getResource(sceneKey) != null) continue;
      Assets.cacheResource("SCENE_HOLDER", sceneKey);
      
      if (! node.child("unit").isNull()) {
        SceneType type = gridSceneFrom(node, basePath, sceneKey);
        Assets.cacheResource(type, sceneKey);
      }
      else if (! node.child("grid").isNull()) {
        SceneType type = fixedSceneFrom(node, basePath, sceneKey);
        Assets.cacheResource(type, sceneKey);
      }
    }
    
    cached = Assets.getResource(key);
    if (cached instanceof SceneType) return (SceneType) cached;
    return null;
  }
  
  
  static SceneTypeGrid gridSceneFrom(
    XML sceneNode, String filePath, String ID
  ) {
    XML    file     = sceneNode.parent();
    String name     = sceneNode.value("name"  );
    String floorID  = sceneNode.value("floor" );
    String wallID   = sceneNode.value("wall"  );
    String doorID   = sceneNode.value("door"  );
    String windowID = sceneNode.value("window");
    
    XML unitXML[] = sceneNode.allChildrenMatching("unit");
    Batch <SceneTypeGrid.GridUnit> units = new Batch();
    
    for (XML u : unitXML) {
      SceneType type = sceneWithID(u.value("typeID"), file, filePath);
      int wallType = Kind.loadField(u.value("wall"    ), SceneTypeGrid.class);
      int priority = Kind.loadField(u.value("priority"), SceneTypeGrid.class);
      int percent  = getInt(u, "percent" , -1);
      int minCount = getInt(u, "minCount", -1);
      int maxCount = getInt(u, "maxCount", -1);
      
      SceneTypeGrid.GridUnit unit = SceneTypeGrid.unit(
        (SceneTypeFixed) type, wallType,
        priority, percent, minCount, maxCount
      );
      if (unit != null) units.add(unit);
    }
    
    int unitSize = sceneNode.getInt("unitSize"      );
    int maxUA    = sceneNode.getInt("maxUnitsAcross");
    PropType
      floor  = propWithID(floorID , file, filePath),
      wall   = propWithID(wallID  , file, filePath),
      door   = propWithID(doorID  , file, filePath),
      window = propWithID(windowID, file, filePath);
    
    SceneTypeGrid sceneType = new SceneTypeGrid(
      name, ID, unitSize, maxUA,
      wall, door, window, floor,
      units.toArray(SceneTypeGrid.GridUnit.class)
    );
    return sceneType;
  }
  
  
  static SceneTypeFixed fixedSceneFrom(
    XML sceneNode, String filePath, String ID
  ) {
    //
    //  First load up the basic stats for this scene-type:
    XML    file     = sceneNode.parent();
    String name     = sceneNode.value ("name"  );
    int    wide     = sceneNode.getInt("wide"  );
    int    high     = sceneNode.getInt("high"  );
    String floorID  = sceneNode.value ("floor" );
    String wallID   = sceneNode.value ("wall"  );
    String doorID   = sceneNode.value ("door"  );
    String windowID = sceneNode.value ("window");
    XML    gridXML  = sceneNode.child ("grid"  );
    //
    //  The arguments for the grid node are assumed to map numbers to
    //  prop-types, so we extract those into an indexed array.
    String gridArgs[] = gridXML.args();
    PropType types[] = new PropType[gridArgs.length];
    PropType
      floor  = propWithID(floorID , file, filePath),
      wall   = propWithID(wallID  , file, filePath),
      door   = propWithID(doorID  , file, filePath),
      window = propWithID(windowID, file, filePath);
    for (int i = types.length; i-- > 0;) {
      int index = Integer.parseInt(gridArgs[i]) - 1;
      types[index] = propWithID(gridXML.value(gridArgs[i]), file, filePath);
    }
    //
    //  The content of the grid node represent the x/y position of those props,
    //  as represented by index.  First we break down the tokens-
    StringTokenizer t = new StringTokenizer(gridXML.content(), ", \n", false);
    final List <String> tokens = new List();
    while (t.hasMoreTokens()) tokens.add(t.nextToken());
    //
    //  Then populate the scene while iterating over possible grid positions-
    SceneTypeFixed sceneType = new SceneTypeFixed(name, ID, wide, high);
    sceneType.floors  = floor ;
    sceneType.borders = wall  ;
    sceneType.door    = door  ;
    sceneType.window  = window;
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) try {
      //
      //  We skip over any empty positions, and check to see if there's a
      //  numeric type index present, along with markers for doors and windows:
      if (tokens.empty()) break;
      String token = tokens.removeFirst().toLowerCase();
      int portI = matchIndex(token, '/', '\'');
      char wallChars[] = {'‾',']','_','['};
      int typeIndex = parseIndex(token) - 1;
      PropType placed = null;
      //
      //  For each wall-marker present, we generate a wall-prop with the
      //  correct facing (and as a door or window if appropriate):
      for (int i = 4; i-- > 0;) {
        if (matchIndex(token, wallChars[i]) == -1) continue;
        int dir = T_ADJACENT[i];
        PropType type = wall;
        if (portI == 0) type = door;
        if (portI == 1) type = window;
        sceneType.attachPlacing(type, c.y, c.x, dir);
      }
      //
      //  If a numeric type-index was detected, we check if a particular facing
      //  was specified, and add that to the scene-
      if (typeIndex >= 0) {
        int dir = matchIndex(token, 'n','e','s','w');
        if (dir == -1) dir = N;
        else dir = T_ADJACENT[dir];
        placed = types[typeIndex];
        sceneType.attachPlacing(placed, c.y, c.x, dir);
      }
      //
      //  Finally, include flooring (if you haven't already):
      if (placed == null || placed != floor) {
        sceneType.attachPlacing(floor, c.y, c.x, N);
      }
    }
    catch (Exception e) { I.report(e); break; }
    //
    //  And finally, return the initialised type:
    return sceneType;
  }
  
  
  private static int getInt(XML node, String tag, int defaultVal) {
    String value = node.value(tag);
    if (value != null) try { return Integer.parseInt(value); }
    catch (Exception e) {}
    return defaultVal;
  }
  
  
  private static int matchIndex(String token, char... match) {
    for (int i = token.length(); i-- > 0;) {
      char t = token.charAt(i);
      for (int c = match.length; c-- > 0;) {
        if (match[c] == t) return c;
      }
    }
    return -1;
  }
  
  
  private static int parseIndex(String token) {
    StringBuffer s = new StringBuffer();
    for (char c : token.toCharArray()) {
      if (c < '0' || c > '9') continue;
      s.append(c);
    }
    token = s.toString();
    if (s.length() == 0) return -1;
    return Integer.parseInt(token);
  }
}






