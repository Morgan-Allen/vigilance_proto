

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
    String name    = sceneNode.value ("name" );
    String ID      = sceneNode.value ("ID"   );
    int    wide    = sceneNode.getInt("wide" );
    int    high    = sceneNode.getInt("high" );
    String floorID = sceneNode.value ("floor");
    XML    gridXML = sceneNode.child ("grid" );
    //
    //  The arguments for the grid node are assumed to map numbers to
    //  prop-types, so we extract those into an indexed array.
    String gridArgs[] = gridXML.args();
    PropType types[] = new PropType[gridArgs.length];
    PropType floor = propWithID(floorID, allTypes, filePath);
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
    //  Then populate the grid-
    char dU[] = {'N','E','S','W'}, dL[] = {'n','e','s','w'};
    byte typeGrid[][] = new byte[wide][high];
    byte dirsGrid[][] = new byte[wide][high];
    
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) try {
      int direction = N;
      String token = tokens.removeFirst();
      char last = token.charAt(token.length() - 1);
      
      for (int i = 4; i-- > 0;) if (last == dU[i] || last == dL[i]) {
        direction = T_ADJACENT[i];
        token = token.substring(0, token.length() - 1);
        I.say("Direction at "+c+" is "+direction+", token: "+token);
        break;
      }
      
      typeGrid[c.x][c.y] = (byte) Integer.parseInt(token);
      dirsGrid[c.x][c.y] = (byte) direction;
    }
    catch (Exception e) { I.report(e); break; }
    //
    //  And finallly, return the initialised type:
    return new SceneTypeFixed(
      name, ID,
      floor, types, wide, high,
      typeGrid, dirsGrid
    );
  }
  
  
  public static PropType propFrom(String xmlPath) {
    return propFrom(XML.load(xmlPath), xmlPath);
  }
  
  
  private static PropType propWithID(
    String ID, Series <PropType> from, String basePath
  ) {
    String key = basePath+"_"+ID;
    for (PropType p : from) if (p.entryKey().equals(key)) return p;
    return null;
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



