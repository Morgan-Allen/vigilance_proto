


package proto.game.content;
import proto.common.Kind;
import proto.game.scene.*;
import proto.util.*;
import static proto.game.content.Common.*;
import static proto.game.person.Ability.*;
import static proto.game.person.Person.*;
import java.awt.Image;



public class Corona {
  
  
  //  TODO:  Let Corona smash through walls!
  

  final public static Kind
    CORONA   = Kind.ofPerson(
      "Corona", "hero_kind_corona", IMG_DIR+"sprite_superman.png",
      Kind.TYPE_HERO,
      HIT_POINTS, 250,
      ARMOUR, 8  ,
      MUSCLE, 100,
      BRAIN , 16 ,
      SPEED_ACT , 16 ,
      SIGHT , 16 ,
      MOVE, 1, STRIKE, 1, DISARM, 1, GUARD, 1
    );
}


