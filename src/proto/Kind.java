

package proto;
import util.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;



public class Kind {

  final public static int
    TYPE_PROP     = 0,
    TYPE_CIVILIAN = 1,
    TYPE_HERO     = 2,
    TYPE_MOOK     = 3,
    TYPE_BOSS     = 4;
  
  String name;
  Image sprite;
  
  int type;
  
  int wide, high;
  boolean blockSight;
  boolean blockPath;
  
  Ability baseAbilities[] = new Ability[0];
  Integer baseAbilityLevels[] = new Integer[0];
  
  
  static Kind ofPerson(
    String name, String spritePath,
    int type, Object... initStats
  ) {
    Kind k = new Kind();
    k.type = type;
    k.wide = k.high = 1;
    k.blockPath = k.blockSight = false;
    
    Batch <Ability> allA = new Batch();
    Batch <Integer> allL = new Batch();
    Ability readA = null;
    Integer readL = null;
    for (Object o : initStats) {
      if (o instanceof Ability) {
        readA = (Ability) o;
      }
      if (o instanceof Integer) {
        readL = (Integer) o;
      }
      if (readA != null && readL != null) {
        allA.add(readA);
        allL.add(readL);
        readA = null;
        readL = null;
      }
    }
    k.baseAbilities     = allA.toArray(Ability.class);
    k.baseAbilityLevels = allL.toArray(Integer.class);
    
    k.name = name;
    k.sprite = loadImage(spritePath);
    return k;
  }
  
  
  static Kind ofProp(
    String name, String spritePath,
    int wide, int high, boolean blockPath, boolean blockSight
  ) {
    Kind k = new Kind();
    k.type = TYPE_PROP;
    k.wide = wide;
    k.high = high;
    k.blockPath  = blockPath ;
    k.blockSight = blockSight;
    
    k.name = name;
    k.sprite = loadImage(spritePath);
    return k;
  }
  
  
  public static Image loadImage(String spritePath) {
    try { return ImageIO.read(new File(spritePath)); }
    catch (Exception e) { I.say("Could not load: "+spritePath); return null; }
  }
}









