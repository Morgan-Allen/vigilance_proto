

package proto.common;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.common.DebugSceneGrid.*;
import static proto.game.scene.SceneTypeUnits.*;



public class DebugSceneGen {
  
  
  final public static SceneType GEN_TEST_UNIT = new SceneTypeGrid(
    "gen test unit", "type_gen_test_unit",
    null,
    new PropType[] {
      KIND_COLUMN     ,
      KIND_DOOR       ,
      KIND_WINDOW     ,
    },
    8, 8, new byte[][] {
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 1, 1, 2, 1, 3, 1, 0 },
      { 0, 1, 0, 0, 0, 0, 1, 1 },
      { 0, 2, 0, 0, 0, 0, 0, 0 },
      { 0, 1, 0, 0, 0, 0, 0, 0 },
      { 0, 3, 0, 0, 0, 0, 0, 0 },
      { 0, 1, 1, 0, 0, 0, 0, 0 },
      { 0, 0, 1, 0, 0, 0, 0, 0 },
    }
  ).attachUnitParameters(SceneTypeUnits.CORNER_NORTH, false);
  
  final public static SceneTypeUnits GEN_TEST_SCENE = new SceneTypeUnits(
    "gen test scene", "type_gen_test_scene",
    8,
    2, 4, 2, 4,
    null, null, null, null,
    unit(GEN_TEST_UNIT, -1, -1, -1, PRIORITY_HIGH, 50, -1, -1)
  );
  
  final static SceneType GEN_TEST_XML_SCENE = SceneFromXML.sceneWithID(
    "scene_simple", "slum_bar.xml", "media assets/scene layout/slum scenes/"
  );
  
  
  
  
  public static void main(String args[]) {
    
    World world = new World();
    Scene scene = new Scene(world, 32, 32, true);
    SceneTypeUnits type = (SceneTypeUnits) GEN_TEST_XML_SCENE;
    
    Series <Wing> bounds = new Batch();
    Wing wa = new Wing(), wb = new Wing();
    wa.set( 2,  2, 20, 20);
    wb.set( 2,  2, 28, 12);
    bounds.add(wa);
    bounds.add(wb);
    
    I.say("\nConstructed bounds are:");
    for (Box2D b : bounds) I.say("  "+b);
    
    scene.attachWings(bounds);
    scene.setGridResolution(8);
    printWings(scene, 8);
    
    type.populateWithAreas(world, scene, true);
    Tile.printWallsMask(scene);
  }
  
  
  private static void printWings(Scenery scene, int resolution) {
    
    int dimX = scene.wide() / resolution;
    int dimY = scene.high() / resolution;
    
    I.say("\nPrinting wings...");
    for (int y = 0; y < dimY; y++) {
      I.say("  ");
      for (int x = 0; x < dimX; x++) {
        Wing wing = scene.wingUnder(
          (int) ((x + 0.5f) * resolution),
          (int) ((y + 0.5f) * resolution)
        );
        
        if (wing == null) I.add("__");
        else I.add("[]");
        
        I.add(" ");
      }
    }
  }
}






