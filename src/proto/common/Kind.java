

package proto.common;
import proto.game.person.*;
import proto.util.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;



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
  final public static int
    SUBTYPE_CIVILIAN = 1,
    SUBTYPE_MOOK     = 2,
    SUBTYPE_HERO     = 3,
    SUBTYPE_BOSS     = 4
  ;
  
  String name;
  String defaultInfo;
  Image sprite;
  String firstNames[], lastNames[];
  
  int type, subtype;
  int wide, high;
  boolean blockSight;
  boolean blockPath;
  
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
  }
  
  
  public static Kind loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public static Kind ofPerson(
    String name, String ID, String spritePath, String defaultInfo,
    String personNames[][],
    int subtype, Object... initStats
  ) {
    Kind k = new Kind(name, ID, defaultInfo, TYPE_PERSON);
    k.subtype = subtype;
    k.wide = k.high = 1;
    k.blockPath = k.blockSight = false;
    initStatsFor(k, initStats);
    
    k.sprite = loadImage(spritePath);
    if (personNames == null) personNames = new String[2][0];
    k.firstNames = personNames[0];
    k.lastNames  = personNames[1];
    
    return k;
  }
  
  
  public static Kind ofProp(
    String name, String ID, String spritePath,
    int wide, int high, boolean blockPath, boolean blockSight,
    Object... initStats
  ) {
    Kind k = new Kind(name, ID, "", TYPE_PROP);
    k.wide = wide;
    k.high = high;
    k.blockPath  = blockPath ;
    k.blockSight = blockSight;
    initStatsFor(k, initStats);
    
    k.sprite = loadImage(spritePath);
    return k;
  }
  
  
  protected static void initStatsFor(Kind k, Object... initStats) {
    Batch <Object> allKeys = new Batch();
    Object  readK = null;
    Integer readL = null;
    
    for (Object o : initStats) {
      if (o instanceof Integer) {
        readL = (Integer) o;
      }
      else if (o instanceof Float) {
        readL = (int) (float) (Float) o;
      }
      else {
        if (readL == null && readK != null) {
          allKeys.add(readK);
          k.traitLevels.put(readK, 1);
        }
        readK = o;
      }
      if (readL != null && readL != null) {
        allKeys.add(readK);
        k.traitLevels.put(readK, readL);
        readK = null;
        readL = null;
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
  public boolean blockPath () { return blockPath ; }
  
  public String name  () { return name  ; }
  public Image  sprite() { return sprite; }
  public String defaultInfo() { return defaultInfo; }
  public String[] firstNames() { return firstNames; }
  public String[] lastNames () { return lastNames ; }
  public String toString() { return name; }
  
  
  public static Image loadImage(String imgPath) {
    if (imgPath == null) return null;
    try { return ImageIO.read(new File(imgPath)); }
    catch (Exception e) { I.say("Could not load: "+imgPath); return null; }
  }
  
  
  public static Image[] loadImages(String basePath, String... imgNames) {
    final Image images[] = new Image[imgNames.length];
    for (int n = 0; n < imgNames.length; n++) {
      images[n] = loadImage(basePath+imgNames[n]);
    }
    return images;
  }
  
}









