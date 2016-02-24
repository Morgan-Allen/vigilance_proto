

package proto.game.content;
import proto.common.*;
import proto.game.person.*;

import static proto.game.person.Common.*;
import static proto.game.person.Equipped.*;
import static proto.game.person.Person.*;
import static proto.game.person.PersonStats.*;

import java.awt.Color;



public class Villains {
  
  
  final static String IMG_DIR = "media assets/villain sprites/";
  

  
  final static Equipped
    BASEBALL_BAT = new Equipped(
      "Baseball bat", "item_baseball_bat",
      "A simple, sturdy wooden bat.  +5 damage bonus.",
      null,
      Person.SLOT_WEAPON, 0,
      IS_WEAPON | IS_MELEE | IS_KINETIC, 5
    ),
    TOMMY_GUN = new Equipped(
      "Tommy gun", "item_tommy_gun",
      "A rapid-firing automatic firearm.  +10 damage bonus.",
      Kind.loadImage(IMG_DIR+"sprite_gun_fire.png"),
      Person.SLOT_WEAPON, 0,
      IS_WEAPON | IS_RANGED | IS_KINETIC, 10
    ),
    BOLT_RIFLE = new Equipped(
      "Bolt Rifle", "item_bolt_rifle",
      "Powerful, reverse-engineered alien weaponry.  +15 damage bonus.",
      Color.ORANGE,
      Person.SLOT_WEAPON, 0,
      IS_WEAPON | IS_RANGED | IS_BEAM, 15
    ),
    HI_LASER = new Equipped(
      "Hi Laser", "item_hi_laser",
      "A high-intensity focused microwave cannon.  +20 damage bonus.",
      Color.RED,
      Person.SLOT_WEAPON, 0,
      IS_WEAPON | IS_RANGED | IS_BEAM, 20
    );
  
  
  final static Kind
    KIND_HOSTAGE = Kind.ofPerson(
      "Hostage", "kind_hostage", IMG_DIR+"sprite_hostage.png",
      Kind.TYPE_CIVILIAN,
      MUSCLE, 4  ,
      REFLEX, 8  ,
      BRAIN , 6  ,
      WILL  , 2  ,
      MOVE, 1
    );
  
  final static Kind
    KIND_BRUISER = Kind.ofPerson(
      "Bruiser", "kind_bruiser", IMG_DIR+"sprite_bruiser.png",
      Kind.TYPE_MOOK,
      HIT_POINTS, 20 ,
      ARMOUR, 1  ,
      MUSCLE, 16 ,
      BRAIN , 6  ,
      REFLEX, 8  ,
      WILL  , 2  ,
      SIGHT , 6  ,
      MOVE, 1, STRIKE, 1, BASEBALL_BAT
    ),
    KIND_MOBSTER = Kind.ofPerson(
      "Mobster", "kind_mobster", IMG_DIR+"sprite_mobster.png",
      Kind.TYPE_MOOK,
      HIT_POINTS, 14 ,
      ARMOUR, 0  ,
      MUSCLE, 12 ,
      BRAIN , 10 ,
      REFLEX, 12 ,
      WILL  , 2  ,
      SIGHT , 8  ,
      MOVE, 1, STRIKE, 1, SHOOT, 1, TOMMY_GUN
    ),
    KIND_CRIME_CULTIST = Kind.ofPerson(
      "Crime Cultist", "kind_crime_cultist", IMG_DIR+"sprite_crime_cultist.png",
      Kind.TYPE_MOOK,
      HIT_POINTS, 16 ,
      ARMOUR, 2  ,
      MUSCLE, 14 ,
      BRAIN , 16 ,
      REFLEX, 14 ,
      WILL  , 2  ,
      SIGHT , 12 ,
      MOVE, 1, STRIKE, 1, SHOOT, 1, BOLT_RIFLE
    ),
    KIND_LEXDROID = Kind.ofPerson(
      "Lexdroid", "kind_lexdroid", IMG_DIR+"sprite_lexdroid.png",
      Kind.TYPE_MOOK,
      HIT_POINTS, 30 ,
      ARMOUR, 6  ,
      MUSCLE, 2  ,
      REFLEX, 16 ,
      BRAIN , 4  ,
      WILL  , 0  ,
      SIGHT , 10 ,
      MOVE, 1, SHOOT, 1, HI_LASER
    );
  
  
  final static Kind
    KIND_SLADE = Kind.ofPerson(
      "Slade", "kind_slade", IMG_DIR+"sprite_slade.png",
      Kind.TYPE_BOSS,
      HIT_POINTS, 30 ,
      ARMOUR, 3  ,
      MUSCLE, 20 ,
      REFLEX, 20 ,
      BRAIN , 18 ,
      WILL  , 18 ,
      SIGHT , 18 ,
      MOVE, 1, STRIKE, 1, SHOOT, 1
    ),
    
    KIND_MR_FREEZE = Kind.ofPerson(
      "Mr. Freeze", "kind_mr_freeze", IMG_DIR+"sprite_mr_freeze.png",
      Kind.TYPE_BOSS,
      HIT_POINTS, 50 ,
      ARMOUR, 5  ,
      MUSCLE, 24 ,
      BRAIN , 20 ,
      REFLEX, 10 ,
      WILL  , 18 ,
      SIGHT , 12 ,
      MOVE, 1, STRIKE, 1, SHOOT, 1
    ),
    
    KIND_METALLO = Kind.ofPerson(
      "Metallo", "kind_metallo", IMG_DIR+"sprite_metallo.png",
      Kind.TYPE_BOSS,
      HIT_POINTS, 120,
      ARMOUR, 8  ,
      MUSCLE, 20 ,
      BRAIN , 14 ,
      REFLEX, 16 ,
      WILL  , 18 ,
      SIGHT , 10 ,
      MOVE, 1, SHOOT, 1
    );
}

















