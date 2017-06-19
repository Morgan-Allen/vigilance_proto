

package proto.common;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;
import static proto.common.DebugSceneGrid.*;
import static proto.game.scene.SceneTypeUnits.*;



public class DebugSceneGen {
  
  
  final public static SceneType GEN_TEST_UNIT_A = new SceneTypeGrid(
    "gen test unit A", "type_gen_test_unit_a",
    null,
    new PropType[] {
      KIND_COLUMN,
      KIND_DOOR  ,
      KIND_WINDOW,
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

  final public static SceneType GEN_TEST_UNIT_B = new SceneTypeGrid(
    "gen test unit B", "type_gen_test_unit_b",
    null,
    new PropType[] {
      KIND_COLUMN
    },
    8, 8, new byte[][] {
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 1, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 1, 0, 0, 0, 0, 0, 0 },
      { 0, 1, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
      { 0, 1, 0, 0, 0, 0, 0, 0 },
      { 0, 0, 0, 0, 0, 0, 0, 0 },
    }
  ).attachUnitParameters(SceneTypeUnits.WALL_EAST, true);
  
  final public static SceneTypeUnits GEN_TEST_SCENE = new SceneTypeUnits(
    "gen test scene", "type_gen_test_scene",
    8,
    2, 4, 2, 4,
    null, null, null, null,
    unit(GEN_TEST_UNIT_A, -1, -1, -1, PRIORITY_HIGH, 50, -1, -1),
    unit(GEN_TEST_UNIT_B, -1, -1, -1, PRIORITY_HIGH, 50, -1, -1)
  );
  
  final static SceneType GEN_TEST_XML_SCENE = SceneFromXML.sceneWithID(
    "scene_simple", "slum_bar.xml", "media assets/scene layout/slum scenes/"
  );
  
  
  public static void main(String args[]) {
    World world = new World();
    Scene scene = new Scene(GEN_TEST_SCENE, world, 32, 32, true);
    int resolution = 8;
    //
    //  First, we have a series of reports/tests based on a fixed layout of
    //  building-wings, intended to ensure that corner-fitting is correctly
    //  recognised for potential sub-units.
    Series <Wing> bounds = new Batch();
    Wing wa = new Wing(), wb = new Wing();
    wa.set( 0,  0, 24, 24);
    wb.set( 0,  0, 32, 16);
    bounds.add(wa);
    bounds.add(wb);
    
    I.say("\nConstructed bounds are:");
    for (Box2D b : bounds) I.say("  "+b);
    
    scene.setupWingsGrid(resolution, bounds);
    printWings(scene, resolution);
    
    I.say("\nChecking for border-fitting...");
    Scenery subunitA = GEN_TEST_UNIT_A.generateScenery(world, true);
    Scenery subunitB = GEN_TEST_UNIT_B.generateScenery(world, true);
    
    Object checkFitsArgs[][] = Visit.splitByDivision(new Object[] {
      
      0, 0, N, true , subunitA,
      3, 0, E, true , subunitA,
      3, 1, S, true , subunitA,
      2, 2, S, true , subunitA,
      0, 2, W, true , subunitA,
      
      1, 1, N, false, subunitA,
      1, 2, S, false, subunitA,
      2, 1, N, false, subunitA,
      
      0, 3, W, true , subunitB,
      1, 3, W, true , subunitB,
    }, 10);
    
    boolean allFitChecksMet = true;
    for (Object c[] : checkFitsArgs) {
      int     x    = resolution * (Integer) c[0];
      int     y    = resolution * (Integer) c[1];
      int     f    = (Integer) c[2];
      boolean need = (Boolean) c[3];
      Scenery unit = (Scenery) c[4];
      boolean okay = unit.type().checkBordering(scene, unit, x, y, f, 8);
      I.say("  Okay at "+x+"|"+y+", face: "+f+": "+okay+", should be: "+need);
      
      allFitChecksMet &= need == okay;
    }
    I.say("\nAll border-checks met? "+allFitChecksMet+"\n\n");
    
    //
    //  Then generate a fully random scene and view the walls layout:
    SceneTypeUnits type = (SceneTypeUnits) GEN_TEST_XML_SCENE;
    scene  = new Scene(GEN_TEST_XML_SCENE, world, 32, 32, true);
    bounds = type.generateWings(scene, resolution, 0.66f, 3);
    
    I.say("Generated bounds are:");
    for (Box2D b : bounds) I.say("  "+b);
    
    scene.setupWingsGrid(resolution, bounds);
    printWings(scene, resolution);
    
    type.populateWithAreas(world, scene, true, false);
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






