

package proto.editor;
import proto.common.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.view.common.*;
import proto.view.scene.*;
import proto.util.*;

import java.io.*;



public class Editor extends RunGame {
  
  
  String propsPath = "media assets/testing/";
  String propsFile = "test_props.xml";
  XML    propsXML  = XML.load(propsPath+""+propsFile);
  List <PropType> propTypes = new List();

  
  
  final static SceneType BLANK_SCENE = new SceneType(
    "Blank Scene", "scene_blank"
  ) {
    public Scenery generateScenery(World world, boolean testing) {
      return null;
    }
    public Scenery generateScenery(World world, int prefWide, int prefHigh, boolean testing) {
      return null;
    }
  };
  
  protected World setupWorld() {
    
    for (XML node : propsXML.allChildrenMatching("prop")) {
      String ID = node.value("ID");
      PropType type = SceneFromXML.propFromXML(ID, propsFile, propsPath, propsXML);
      propTypes.add(type);
    }
    
    World world = new World(this, savePath);
    Scene scene = new Scene(BLANK_SCENE, world, 16, 16, true);
    world.enterScene(scene);
    return world;
  }
  
  
  
  void exportToXML(Scene scene, String name, String ID, String outPath) {
    
    final char DIR_CHARS[] = { 'n', 'e', 's', 'w' };
    
    StringBuffer xml = new StringBuffer();
    xml.append(
      "<scene "+
      "  name   = "+quote(name)+
      "  ID     = "+quote(ID  )+
      "  wide   = "+quote(scene.wide())+
      "  high   = "+quote(scene.high())+
      "  floor  = "+
      "  wall   = "+
      "  door   = "+
      "  window = "+
      ">"
    );
    
    xml.append(
      "  <grid"
    );
    int typeID = 1;
    for (PropType type : propTypes) {
      xml.append("    "+typeID+" = "+quote(type.entryKey()));
      typeID += 1;
    }
    xml.append("  >");
    
    for (int y = 0; y < scene.high(); y++) {
      xml.append("\n    ");
      
      for (int x = 0; x < scene.wide(); x++) {
        
        Tile t = scene.tileAt(x, y);
        boolean empty = true;
        
        for (Element e : t.inside()) if (e instanceof Prop) {
          Prop p = (Prop) e;
          int propID = propTypes.indexOf(p.kind());
          int facing = p.facing();
          
          if (p.origin() == t && propID != -1) {
            xml.append(" "+propID);
            if (facing != TileConstants.N) xml.append(DIR_CHARS[facing / 2]);
            
            empty = false;
            //  TODO:  You need to allow for multiple props in a given tile!
            break;
          }
        }
        if (empty) {
          xml.append(" .");
        }
      }
    }
    
    xml.append("  </grid>");
    xml.append("</scene>");
    
    
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter("test.txt"));
      out.write(xml.toString());
      out.close();
    }
    catch(Exception e) {
      
    }
  }
  
  
  private String quote(Object o) {
    return "\""+o+"\"";
  }
  
  
}









