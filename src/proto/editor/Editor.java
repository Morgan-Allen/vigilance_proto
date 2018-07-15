

package proto.editor;
import proto.common.*;
import proto.game.scene.*;
import proto.game.world.World;
import proto.view.common.*;
import proto.view.scene.*;
import proto.util.*;



public class Editor extends RunGame {
  
  
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
  
  
  String propsPath = "media assets/testing/";
  String propsFile = "test_props.xml";
  XML    propsXML  = XML.load(propsPath+""+propsFile);
  List <PropType> propTypes = new List();
  
  
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
  
  
  void exportToXML(Scene scene, String outPath) {
    
  }
  
  
}










