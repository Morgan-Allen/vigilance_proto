

package proto.game.scene;
import proto.common.*;
import proto.util.*;

import java.util.StringTokenizer;



public class SceneFromXML implements TileConstants {
  
  
  public static SceneTypeFixed fixedSceneFrom(
    String filePath, String fileName
  ) {
    XML file = XML.load(filePath+""+fileName);
    //
    //  Scoop up the prop-definitions first, then load up the basic stats for
    //  the scene-type:
    Batch <PropType> allTypes = new Batch();
    for (XML child : file.allChildrenMatching("prop")) {
      PropType type = propFrom(child, filePath);
      if (type != null) allTypes.add(type);
    }
    XML sceneNode = file.child("scene");
    String name     = sceneNode.value ("name"  );
    String ID       = sceneNode.value ("ID"    );
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
      floor  = propWithID(floorID , allTypes, filePath),
      wall   = propWithID(wallID  , allTypes, filePath),
      door   = propWithID(doorID  , allTypes, filePath),
      window = propWithID(windowID, allTypes, filePath);
    for (int i = types.length; i-- > 0;) {
      int index = Integer.parseInt(gridArgs[i]) - 1;
      types[index] = propWithID(gridXML.value(gridArgs[i]), allTypes, filePath);
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
      int portI = matchIndex(token, '/', '.');
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
    //  And finallly, return the initialised type:
    return sceneType;
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
  
  
  private static PropType propWithID(
    String ID, Series <PropType> from, String basePath
  ) {
    String key = basePath+"_"+ID;
    for (PropType p : from) if (p.entryKey().equals(key)) return p;
    return null;
  }
  
  
  public static PropType propFrom(String xmlPath) {
    return propFrom(XML.load(xmlPath), xmlPath);
  }
  
  
  public static PropType propFrom(XML node, String basePath) {
    try { return new PropType(
      node.value("name"),
      basePath.toLowerCase()+"_"+node.value("ID"),
      basePath+node.value("sprite"),
      Kind.loadField(node.value("subtype")),
      node.getInt("wide"),
      node.getInt("high"),
      Kind.loadField(node.value("blockLevel")),
      node.getBool("blockSight")
    ); }
    catch (Exception e) { I.report(e); return null; }
  }
}



