

package proto.common;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

import proto.game.person.Ability;
import proto.game.person.Equipped;
import proto.util.*;



public class Kind extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Kind> INDEX = new Index <Kind> ();

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
  
  Ability  baseAbilities[] = new Ability [0];
  Integer  baseLevels   [] = new Integer [0];
  Equipped baseEquipped [] = new Equipped[0];
  
  
  Kind(String uniqueID) {
    super(INDEX, uniqueID);
  }
  
  
  public static Kind loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public int type() { return type; }
  public int wide() { return wide; }
  public int high() { return high; }
  public boolean blockSight() { return blockSight; }
  public boolean blockPath () { return blockPath ; }
  
  public Ability [] baseAbilities() { return baseAbilities; }
  public Integer [] baseLevels   () { return baseLevels   ; }
  public Equipped[] baseEquipped() { return baseEquipped ; }
  
  public String name  () { return name  ; }
  public Image  sprite() { return sprite; }
  
  
  
  
  
  public static Kind ofPerson(
    String name, String ID, String spritePath,
    int type, Object... initStats
  ) {
    Kind k = new Kind(ID);
    k.type = type;
    k.wide = k.high = 1;
    k.blockPath = k.blockSight = false;
    
    Batch <Ability > allA = new Batch();
    Batch <Integer > allL = new Batch();
    Batch <Equipped> allE = new Batch();
    Ability readA = null;
    Integer readL = null;
    for (Object o : initStats) {
      if (o instanceof Ability) {
        readA = (Ability) o;
      }
      if (o instanceof Integer) {
        readL = (Integer) o;
      }
      if (o instanceof Equipped) {
        allE.add((Equipped) o);
      }
      if (readA != null && readL != null) {
        allA.add(readA);
        allL.add(readL);
        readA = null;
        readL = null;
      }
    }
    k.baseAbilities = allA.toArray(Ability .class);
    k.baseLevels    = allL.toArray(Integer .class);
    k.baseEquipped  = allE.toArray(Equipped.class);
    
    k.name = name;
    k.sprite = loadImage(spritePath);
    return k;
  }
  
  
  public static Kind ofProp(
    String name, String ID, String spritePath,
    int wide, int high, boolean blockPath, boolean blockSight
  ) {
    Kind k = new Kind(ID);
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









