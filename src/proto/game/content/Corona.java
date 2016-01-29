


package proto.game.content;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.scene.Ability.*;
import static proto.game.scene.Person.*;
import static proto.game.content.Common.*;
import java.awt.Image;



public class Corona {
  

  final public static Kind
    CORONA   = Kind.ofPerson(
      "Corona", "hero_kind_corona", IMG_DIR+"sprite_superman.png",
      Kind.TYPE_HERO,
      HEALTH, 250,
      ARMOUR, 8  ,
      MUSCLE, 100,
      BRAIN , 16 ,
      SPEED , 16 ,
      SIGHT , 16 ,
      MOVE, 1, STRIKE, 1, DISARM, 1, GUARD, 1
    );
}
