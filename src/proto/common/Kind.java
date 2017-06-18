

package proto.common;
import proto.game.person.*;
import proto.util.*;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.lang.reflect.*;



public class Kind extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Kind> INDEX = new Index <Kind> ();
  
  final public static int
    TYPE_INIT   = -1,
    TYPE_WORLD  =  0,
    TYPE_REGION =  1,
    TYPE_PLACE  =  2,
    TYPE_PERSON =  3,
    TYPE_ITEM   =  5,
    TYPE_PROP   =  6,
    TYPE_CLUE   =  7
  ;
  
  //  TODO:  These should be handled as traits or properties for a given Kind.
  //         Take 'em out!
  final public static int
    SUBTYPE_CIVILIAN = 1,
    SUBTYPE_MOOK     = 2,
    SUBTYPE_HERO     = 3,
    SUBTYPE_BOSS     = 4,
    
    SUBTYPE_WALLING = 0,
    SUBTYPE_FURNISH = 1,
    SUBTYPE_DETAIL  = 2,
    SUBTYPE_EFFECT  = 3,
    
    SUBTYPE_HEAVY_GUN   = 0,
    SUBTYPE_PRECISE_GUN = 1,
    SUBTYPE_WING_BLADE  = 2,
    SUBTYPE_BOW         = 3,
    SUBTYPE_BLADE       = 4,
    SUBTYPE_BLUNT       = 5,
    SUBTYPE_GRENADE     = 6,
    SUBTYPE_GADGET      = 7,
    SUBTYPE_ARMOUR      = 8,
    SUBTYPE_TRACE       = 9
  ;
  final public static int
    BLOCK_NONE    = 0,
    BLOCK_PARTIAL = 1,
    BLOCK_FULL    = 2;
  
  String name;
  String defaultInfo;
  Image sprite;
  int renderPriority;
  
  int type, subtype;
  int wide, high;
  boolean blockSight;
  int blockLevel;
  
  Trait kindTrait;
  Table <Object, Integer> traitLevels = new Table();
  Trait    baseTraits  [] = new Trait   [0];
  ItemType baseEquipped[] = new ItemType[0];
  ItemType customItems [] = new ItemType[0];
  Kind     childTypes  [] = new Kind    [0];
  
  
  protected Kind(String name, String uniqueID, String info, int type) {
    super(INDEX, uniqueID);
    this.name        = name;
    this.defaultInfo = info;
    this.type        = type;
    this.kindTrait = new Trait(name, "trait_"+uniqueID, null, info);
  }
  
  
  protected Kind(
    String name, String ID, String spritePath, String defaultInfo,
    int wide, int high,
    int blockLevel, boolean blockSight,
    int type, int subtype,
    Object... initStats
  ) {
    this(name, ID, defaultInfo, TYPE_PROP);
    this.wide = wide;
    this.high = high;
    this.blockLevel = blockLevel;
    this.blockSight = blockSight;
    this.renderPriority = blockLevel + (blockSight ? 1 : 0);
    this.type    = type   ;
    this.subtype = subtype;
    initStatsFor(this, initStats);
    sprite = loadImage(spritePath);
  }
  
  
  public static Kind loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  protected static void initStatsFor(Kind k, Object... initStats) {
    Batch <Object> allKeys = new Batch();
    
    for (int i = 0; i < initStats.length; i++) {
      Object arg = initStats[i];
      Integer pref = numberAt(i, initStats), post = numberAt(i + 1, initStats);
      if (pref == null && post != null) {
        allKeys.add(arg);
        k.traitLevels.put(arg, post);
      }
      else if (pref == null) {
        allKeys.add(arg);
        k.traitLevels.put(arg, 1);
      }
    }

    Batch <Trait   > allT = new Batch();
    Batch <ItemType> allE = new Batch();
    Batch <ItemType> allC = new Batch();
    Batch <Kind    > allK = new Batch();
    
    for (Object o : allKeys) {
      if (o instanceof Trait) allT.add((Trait) o);
      if (o instanceof Kind ) allK.add((Kind ) o);
      if (o instanceof ItemType) {
        ItemType e = (ItemType) o;
        allE.add(e);
        if (e.isCustom()) allC.add(e);
      }
    }
    
    k.baseTraits   = allT.toArray(Trait   .class);
    k.baseEquipped = allE.toArray(ItemType.class);
    k.customItems  = allC.toArray(ItemType.class);
    k.childTypes   = allK.toArray(Kind    .class);
  }
  
  
  
  /**  Generic type-data access methods-
    */
  protected static Integer numberAt(int index, Object args[]) {
    if (index < 0 || index >= args.length) return null;
    Object arg = args[index];
    if (arg instanceof Integer) return (Integer) arg;
    if (arg instanceof Float) return (int) (float) (Float) arg;
    return null;
  }
  
  
  public int baseLevel(Object t) {
    final Integer l = traitLevels.get(t);
    return l == null ? 0 : l;
  }
  
  
  public Trait   [] baseTraits  () { return baseTraits  ; }
  public ItemType[] baseEquipped() { return baseEquipped; }
  public ItemType[] customItems () { return customItems ; }
  public Kind    [] childTypes  () { return childTypes  ; }
  
  public int type() { return type; }
  public int wide() { return wide; }
  public int high() { return high; }
  public int subtype() { return subtype; }
  public boolean blockSight() { return blockSight; }
  public int blockLevel() { return blockLevel; }
  
  public String name  () { return name  ; }
  public Image  sprite() { return sprite; }
  public int renderPriority() { return renderPriority; }
  public String defaultInfo() { return defaultInfo; }
  public String toString() { return name; }
  
  
  
  /*  Other setup-related helper methods-
   */
  public static Image loadImage(String imgPath) {
    if (imgPath == null) return null;
    try { return ImageIO.read(new File(imgPath)); }
    catch (Exception e) {
      if (GameSettings.reportMediaMiss) I.say("Could not load: "+imgPath);
      return null;
    }
  }
  
  
  public static Image[] loadImages(String basePath, String... imgNames) {
    final Image images[] = new Image[imgNames.length];
    for (int n = 0; n < imgNames.length; n++) {
      images[n] = loadImage(basePath+imgNames[n]);
    }
    return images;
  }
  
  
  public static int loadField(String label, Class from) {
    try { return from.getField(label).getInt(null); }
    catch (Exception e) {}
    try { return Integer.parseInt(label); }
    catch (Exception e) {}
    return -1;
  }
  
  
  public static Object loadObject(String label, Class from) {
    try { return from.getField(label).get(null); }
    catch (Exception e) {}
    return null;
  }
  
  
  public static int loadField(String label) {
    return loadField(label, Kind.class);
  }
  
  
  public static String fieldLabel(int value, Class from) {
    try {
      for (Field f : from.getDeclaredFields()) {
        if (f.getInt(null) == value) return f.getName();
      }
    }
    catch (Exception e) {}
    return null;
  }
  
  
  public static Kind kindWithID(String ID) {
    for (Kind k : INDEX.allEntries(Kind.class)) {
      if (k.entryKey().equals(ID)) return k;
    }
    return null;
  }
  
}




