

package proto.common;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;
import proto.content.agents.*;
import proto.content.items.*;
import proto.content.places.*;



public class DebugSceneXML extends RunGame {
  
  final static SceneType FILE_TEST_SCENE;
  final static XML SCENE_XML;
  static {
    XML testXML = XML.load("media assets/testing/testing_parameters.xml");
    SCENE_XML = testXML.child("sceneTest");
    FILE_TEST_SCENE = SceneFromXML.sceneWithID(
      SCENE_XML.value("sceneID" ),
      SCENE_XML.value("fileName"),
      SCENE_XML.value("basePath")
    );
  }
  
  
  public static void main(String args[]) {
    GameSettings.debugScene = true;
    GameSettings.pauseScene = true;
    runGame(new DebugSceneXML(), "saves/debug_scene_xml");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    
    int     sceneSize = SCENE_XML.getInt("size");
    Scene   scene     = FILE_TEST_SCENE.generateScene(world, sceneSize, true);
    Faction owns      = Heroes.JANUS_INDUSTRIES;
    Base    player    = new Base(Facilities.MANOR, world, owns);
    world.addBase(player, true);
    
    Tile.printWallsMask(scene);
    
    Person toSelect = null;
    
    for (XML agentXML : SCENE_XML.allChildrenMatching("agent")) {
      String typeID = agentXML.value("ID");
      int    startX = agentXML.getInt("startX");
      int    startY = agentXML.getInt("startY");
      Kind kind = Kind.kindWithID(typeID);
      if (kind.type() != Kind.TYPE_PERSON) continue;
      
      Person agent = new Person(kind, world, kind.name);
      agent.addAssignment(scene);
      scene.enterScene(agent, startX, startY);
      toSelect = agent;
    }
    
    world.enterScene(scene);
    
    if (toSelect == null) {
      scene.view().setZoomPoint(scene.tileAt(8, 8));
    }
    else {
      scene.view().setSelection(toSelect, false);
      scene.view().setZoomPoint(toSelect.currentTile());
    }
    
    return world;
  }
  
}







