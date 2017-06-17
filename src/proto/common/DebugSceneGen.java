

package proto.common;
import proto.game.world.*;
import proto.game.scene.*;
import proto.content.places.*;
import proto.util.*;
import static proto.game.scene.SceneTypeUnits.*;



public class DebugSceneGen {
  
  public static void main(String args[]) {
    /*
    World world = new World();
    Place park = new Place(Facilities.BUSINESS_PARK, 0, world);
    SceneType root = park.kind().sceneType();
    root.generateScene(world, 32, 32, true);
    //*/
    
    World world = new World();
    Scene scene = new Scene(world, 32, 32, true);
    SceneTypeUnits type = (SceneTypeUnits) Facilities.BUSINESS_PARK.sceneType();
    
    /*
    Series <Box2D> bounds = type.constructWingBounds(scene, 8);
    I.say("\nWing bounds are:");
    for (Box2D b : bounds) I.say("  "+b);
    //*/
    Batch <Box2D> bounds = new Batch();
    bounds.add(new Box2D( 2,  2, 20, 20));
    bounds.add(new Box2D( 2,  2, 28, 12));
    
    I.say("\nConstructed bounds are:");
    for (Box2D b : bounds) I.say("  "+b);
    
    Series <Wing> wings = type.constructWings(scene, 8, bounds);
    int dim = 32 / 8;
    
    Object grid[][] = new Object[dim][dim];
    for (Wing wing : wings) grid[wing.x][wing.y] = wing;
    
    I.say("\nPrinting wings...");
    I.add(" (total "+wings.size()+")");
    
    for (int y = dim; y-- > 0;) {
      I.say("  ");
      for (int x = 0; x < dim; x++) {
        Wing wing = (Wing) grid[x][y];
        
        if (wing == null) I.add("___");
        else I.add(""+wing.dir+"_"+wing.unitType);
        
        I.add(" ");
      }
    }
  }
  
}


