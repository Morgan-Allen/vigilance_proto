

package proto.editor;
import proto.common.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;



public class Editor extends RunGame {
  
  
  //  TODO:  Load these attributes from an XML definition file themselves, and
  //  refresh whenever the underlying file is changed!
  
  //  TODO:  You need to allow scrolling through longer lists of objects in
  //  the editor view.  And you need to allow for object deletion.
  
  String basePath   = "media assets/testing/";
  String propsFile  = "test_props.xml";
  String outFile    = "test_scene_export.xml";
  String outScene   = "Test Scene";
  String outSceneID = "test_scene";
  
  List <PropType> propTypes = new List();
  
  
  
  public static void main(String args[]) {
    runGame(new Editor(), "");
  }
  
  
  final static SceneType BLANK_SCENE = new SceneType(
    "Blank Scene", "scene_blank"
  ) {
    public Scene generateScene(World world) {
      return new Scene(this, world, 16, 16, true);
    }
    public Scenery generateScenery(World w, boolean t) {
      return null;
    }
    public Scenery generateScenery(World w, int pW, int pH, boolean t) {
      return null;
    }
  };
  
  protected World setupWorld() {
    
    XML propsXML = XML.load(basePath+""+propsFile);
    for (XML node : propsXML.allChildrenMatching("prop")) {
      String ID = node.value("ID");
      PropType type = SceneFromXML.propFromXML(ID, propsFile, basePath, propsXML);
      I.say("Type has full ID: "+type.entryKey());
      propTypes.add(type);
    }
    
    XML loadXML = XML.load(basePath+""+outFile);
    SceneType loaded = SceneFromXML.sceneFromXML(
      outSceneID, outFile, basePath, loadXML
    );
    if (loaded == null) loaded = BLANK_SCENE;
    
    World world = new World(this, savePath);
    Scene scene = loaded.generateScene(world);
    world.enterScene(scene);
    return world;
  }
  
  
  void exportToXML(Scene scene) {
    SceneToXML.exportToXML(scene, this, outScene, outSceneID, basePath, outFile);
  }
  
}









