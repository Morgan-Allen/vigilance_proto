

package proto.common;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;

import proto.content.agents.*;
import proto.content.items.*;
import proto.content.places.*;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;
import java.io.*;



public class DebugSceneXML extends RunGame {
  
  static SceneType FILE_TEST_SCENE = null;
  static XML SCENE_XML = null;
  static {
    String fullPath = "", sceneID = "", fileName = "", basePath = "";
    try {
      XML testXML = XML.load("media assets/testing/testing_parameters.xml");
      SCENE_XML = findUserPrefsXML(testXML);
      sceneID  = SCENE_XML.value("sceneID" );
      fileName = SCENE_XML.value("fileName");
      basePath = SCENE_XML.value("basePath");
      fullPath = basePath+fileName+", "+sceneID;
      FILE_TEST_SCENE = SceneFromXML.sceneWithID(sceneID, fileName, basePath);
    }
    catch (Exception e) {
      StringBuffer trace = new StringBuffer();
      trace.append("Attempted to load "+sceneID+" from "+basePath+fileName);
      trace.append("\n\n  Encountered error: "+e.toString()+" at-");
      for (Object o : e.getStackTrace()) {
        trace.append("\n  "+o);
      }
      trace.append(
        "\n\nThe last ID referenced was "+SceneFromXML.lastTriedID+" from "+
        SceneFromXML.lastTriedPath+SceneFromXML.lastTriedFile
      );
      trace.append(
        "\n\nPlease check to ensure that all identifiers are correct and "+
        "fully-qualified, using / to separate directories."
      );
      
      JOptionPane.showMessageDialog(null, trace.toString());
      System.exit(0);
    }
  }
  
  private static XML findUserPrefsXML(XML parent) {
    XML defaultChild = parent.child("does_not_exist");
    try {
      File home = FileSystemView.getFileSystemView().getHomeDirectory();
      String homePath = home.getAbsolutePath();
      
      for (XML child : parent.allChildrenMatching("sceneTest")) {
        String userPath = child.value("userPath");
        if (userPath == null) defaultChild = child;
        if (userPath == null || ! homePath.contains(userPath)) continue;
        return child;
      }
    }
    catch (Exception e) {}
    return defaultChild;
  }
  
  
  public static void main(String args[]) {
    GameSettings.debugScene = true;
    GameSettings.pauseScene = true;
    runGame(new DebugSceneXML(), "saves/debug_scene_xml");
  }
  
  
  protected World setupWorld() {
    World world = new World(this, savePath);
    
    if (FILE_TEST_SCENE == null) {
      Scene scene = new Scene(world, 2, 2);
      scene.setupScene(true);
      world.enterScene(scene);
      return world;
    }
    
    int     pS     = SCENE_XML.getInt("prefSize");
    Scene   scene  = FILE_TEST_SCENE.generateScene(world, pS, pS, true);
    Faction owns   = Heroes.JANUS_INDUSTRIES;
    Base    player = new Base(Facilities.MANOR, world, owns);
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
    
    if (toSelect == null || toSelect.currentTile() == null) {
      int dim = Nums.min(scene.wide() / 2, scene.high() / 2);
      scene.view().setZoomPoint(scene.tileAt(dim, dim));
    }
    else {
      scene.view().setSelection(toSelect, false);
      scene.view().setZoomPoint(toSelect.currentTile());
    }
    
    return world;
  }
  
}







