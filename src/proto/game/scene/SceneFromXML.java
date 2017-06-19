
package proto.game.scene;
import proto.common.*;
import proto.util.*;

import java.util.StringTokenizer;




public class SceneFromXML implements TileConstants {
  
  
  //  NOTE:  These are primarily used to assist in debugging...
  public static String
    lastTriedID   = "",
    lastTriedFile = "",
    lastTriedPath = "";
  
  
  public static PropType propWithID(String ID, String file, String basePath) {
    String split[] = splitFilenameFromID(ID);
    if (split != null) { file = split[0]+".xml"; ID = split[1]; }
    lastTriedID   = ID;
    lastTriedFile = file;
    lastTriedPath = basePath;
    return propFromXML(ID, file, basePath, getCachedXML(basePath+file));
  }
  
  
  public static SceneType sceneWithID(String ID, String file, String basePath) {
    String split[] = splitFilenameFromID(ID);
    if (split != null) { file = split[0]+".xml"; ID = split[1]; }
    lastTriedID   = ID;
    lastTriedFile = file;
    lastTriedPath = basePath;
    return sceneFromXML(ID, file, basePath, getCachedXML(basePath+file));
  }
  
  
  private static String[] splitFilenameFromID(String ID) {
    if (ID == null) return null;
    int splitIndex = ID.indexOf(".");
    if (splitIndex == -1) return null;
    return new String[] {
      ID.substring(0             , splitIndex - 0),
      ID.substring(splitIndex + 1, ID.length()   )
    };
  }
  
  
  private static XML getCachedXML(String xmlPath) {
    XML cached = (XML) Assets.getResource(xmlPath);
    if (cached == null) {
      Assets.cacheResource(cached = XML.load(xmlPath), xmlPath);
    }
    return cached;
  }
  
  
  private static String uniqueID(String ID, String file, String basePath) {
    return (basePath+file).toLowerCase()+"\\"+ID;
  }
  
  
  static PropType propFromXML(
    String ID, String file, String basePath, XML fileXML
  ) {
    String   key    = uniqueID(ID, file, basePath);
    PropType cached = (PropType) Assets.getResource(key);
    if (cached != null) return cached;
    
    for (XML node : fileXML.allChildrenMatching("prop")) {
      String nodeID  = node.value("ID");
      String propKey = uniqueID(nodeID, file, basePath);
      if (Assets.getResource(propKey) != null) continue;
      Assets.cacheResource("PROP_HOLDER", propKey);
      
      String name     = node.value("name"  );
      String sprite   = node.value("sprite");
      int    subtype  = Kind.loadField(node.value("subtype"));
      int    wide     = getInt(node, "wide", 1);
      int    high     = getInt(node, "high", 1);
      int    blockage = Kind.loadField(node.value("blockLevel"));
      String opacity  = node.value("blockSight");
      
      if (name     == null) name     = nodeID.replace("_", " ");
      if (sprite   == null) sprite   = nodeID+".png";
      if (subtype  == -1  ) subtype  = Kind.SUBTYPE_FURNISH;
      if (blockage == -1  ) blockage = Kind.BLOCK_FULL;
      if (opacity  == null) opacity  = blockage == Kind.BLOCK_FULL ?
        "true" : "false"
      ;
      
      PropType type = new PropType(
        name, propKey, basePath+sprite,
        subtype, wide, high, blockage, Boolean.parseBoolean(opacity)
      );
      Assets.cacheResource(type, propKey);
    }
    
    Object match = Assets.getResource(key);
    if (match instanceof PropType) return (PropType) match;
    return null;
  }
  
  
  public static SceneType sceneFromXML(
    String ID, String file, String basePath, XML fileXML
  ) {
    String key    = uniqueID(ID, file, basePath);
    Object cached = Assets.getResource(key);
    if (cached instanceof SceneType) return (SceneType) cached;
    
    for (XML node : fileXML.allChildrenMatching("scene")) {
      String nodeID   = node.value("ID");
      String sceneKey = uniqueID(nodeID, file, basePath);
      if (Assets.getResource(sceneKey) != null) continue;
      Assets.cacheResource("SCENE_HOLDER", sceneKey);
      
      if (! node.child("unit").isNull()) {
        SceneType type = unitSceneFrom(file, node, basePath, sceneKey);
        Assets.cacheResource(type, sceneKey);
      }
      else if (! node.child("grid").isNull()) {
        SceneType type = gridSceneFrom(file, node, basePath, sceneKey);
        Assets.cacheResource(type, sceneKey);
      }
    }
    
    cached = Assets.getResource(key);
    if (cached instanceof SceneType) return (SceneType) cached;
    return null;
  }
  
  
  static SceneTypeUnits unitSceneFrom(
    String file, XML sceneNode, String basePath, String ID
  ) {
    String name     = sceneNode.value("name"  );
    String floorID  = sceneNode.value("floor" );
    String wallID   = sceneNode.value("wall"  );
    String doorID   = sceneNode.value("door"  );
    String windowID = sceneNode.value("window");
    
    XML unitXML[] = sceneNode.allChildrenMatching("unit");
    Batch <SceneTypeUnits.Unit> units = new Batch();
    
    for (XML u : unitXML) {
      SceneType type = sceneWithID(u.value("typeID"), file, basePath);
      if (type == null) continue;
      
      Class fieldSource = SceneTypeUnits.class;
      int priority = Kind.loadField (u.value("priority" ), fieldSource);
      
      int exactX   = getInt(u, "x", -1);
      int exactY   = getInt(u, "y", -1);
      int exactDir = Kind.loadField(u.value("dir"), TileConstants.class);
      
      int percent  = getInt(u, "percent" , -1);
      int minCount = getInt(u, "minCount", -1);
      int maxCount = getInt(u, "maxCount", -1);
      
      if (priority == -1) priority = SceneTypeUnits.PRIORITY_MEDIUM;
      if (exactDir == -1) exactDir = N;
      
      SceneTypeUnits.Unit unit = SceneTypeUnits.unit(
        type,
        exactX, exactY, exactDir,
        priority, percent, minCount, maxCount
      );
      if (unit != null) units.add(unit);
    }
    
    int     unitSize = sceneNode.getInt("unitSize");
    int     maxUW    = getInt(sceneNode, "maxUnitsWide",  8);
    int     maxUH    = getInt(sceneNode, "maxUnitsHigh",  8);
    int     minUW    = getInt(sceneNode, "minUnitsWide",  2);
    int     minUH    = getInt(sceneNode, "minUnitsHigh",  2);
    int     unitsH   = getInt(sceneNode, "unitsHigh"   , -1);
    int     unitsW   = getInt(sceneNode, "unitsWide"   , -1);
    String  cornerID = sceneNode.value  ("cornering");
    boolean exterior = sceneNode.getBool("exterior" );
    
    PropType
      floor  = propWithID(floorID , file, basePath),
      wall   = propWithID(wallID  , file, basePath),
      door   = propWithID(doorID  , file, basePath),
      window = propWithID(windowID, file, basePath);
    
    if (unitsW != -1) maxUW = minUW = unitsW;
    if (unitsH != -1) maxUH = minUH = unitsH;
    
    SceneTypeUnits sceneType = new SceneTypeUnits(
      name, ID,
      unitSize, minUW, maxUW, minUH, maxUH,
      wall, door, window, floor,
      units.toArray(SceneTypeUnits.Unit.class)
    );
    
    Object cornering = Kind.loadObject(cornerID, SceneTypeUnits.class);
    if (cornering == null) cornering = SceneTypeUnits.INTERIOR;
    sceneType.attachUnitParameters((Object[]) cornering, exterior);
    
    return sceneType;
  }
  
  
  static SceneTypeGrid gridSceneFrom(
    String file, XML sceneNode, String basePath, String ID
  ) {
    //
    //  First load up the basic stats for this scene-type:
    String  name     = sceneNode.value  ("name"     );
    int     wide     = sceneNode.getInt ("wide"     );
    int     high     = sceneNode.getInt ("high"     );
    String  floorID  = sceneNode.value  ("floor"    );
    String  wallID   = sceneNode.value  ("wall"     );
    String  doorID   = sceneNode.value  ("door"     );
    String  windowID = sceneNode.value  ("window"   );
    String  cornerID = sceneNode.value  ("cornering");
    boolean exterior = sceneNode.getBool("exterior" );
    
    PropType
      floor  = propWithID(floorID , file, basePath),
      wall   = propWithID(wallID  , file, basePath),
      door   = propWithID(doorID  , file, basePath),
      window = propWithID(windowID, file, basePath);
    
    SceneTypeGrid sceneType = new SceneTypeGrid(name, ID, wide, high);
    sceneType.floors  = floor;
    sceneType.borders = wall;
    sceneType.door    = door;
    sceneType.window  = window;
    
    Object cornering = Kind.loadObject(cornerID, SceneTypeUnits.class);
    if (cornering == null) cornering = SceneTypeUnits.INTERIOR;
    sceneType.attachUnitParameters((Object[]) cornering, exterior);
    
    for (XML gridXML : sceneNode.allChildrenMatching("grid")) {
      //
      //  The arguments for the grid node are assumed to map numbers to
      //  prop-types, so we extract those into an indexed array.
      String gridArgs[] = gridXML.args();
      PropType types[] = new PropType[gridArgs.length];
      for (int i = types.length; i-- > 0;) {
        int index = Integer.parseInt(gridArgs[i]) - 1;
        types[index] = propWithID(gridXML.value(gridArgs[i]), file, basePath);
      }
      //
      //  The content of the grid node represent the x/y position of those
      //  props, as represented by index.  First we break down the tokens-
      StringTokenizer t;
      t = new StringTokenizer(gridXML.content(), ", \n\r", false);
      final List <String> tokens = new List();
      while (t.hasMoreTokens()) tokens.add(t.nextToken());
      char wallChars[] = {'-',']','_','['}, dirChars[] = {'n','e','s','w'};
      //
      //  Then populate the scene while iterating over possible grid positions-
      for (Coord c : Visit.grid(0, 0, high, wide, 1)) try {
        //
        //  We skip over any empty positions, and check to see if there's a
        //  numeric type index present, along with markers for doors and
        //  windows:
        if (tokens.empty()) break;
        String token = tokens.removeFirst().toLowerCase();
        int portI     = matchIndex(token, '/', '\'');
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
        //  If a numeric type-index was detected, we check if a particular
        //  facing was specified, and add that to the scene-
        if (typeIndex >= 0) {
          int dir = matchIndex(token, dirChars);
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
    }
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






