

package proto.content.places;
import proto.game.scene.*;



public class UrbanScenes {
  
  
  final public static SceneType URBAN_SCENE = SceneFromXML.sceneWithID(
    "scene_assembled", "slum_bar.xml",
    "media assets/scene layout/slum scenes/"
  );
  
  final public static SceneType MANSION_SCENE = SceneFromXML.sceneWithID(
    "scene_assembled", "civic_bank.xml",
    "media assets/scene layout/civic scenes/"
  );
  
}