

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
  final public static int
    BLOCK_NONE    = 0,
    BLOCK_PARTIAL = 1,
    BLOCK_FULL    = 2;
  
  String name;
  String defaultInfo;
  Image sprite;
  String firstNames[], lastNames[];
  
  int type, subtype;
  int wide, high;
  boolean blockSight;
  int blockLevel;
  
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
    k.blockSight = false;
    k.blockLevel = BLOCK_PARTIAL;
    initStatsFor(k, initStats);
    
    k.sprite = loadImage(spritePath);
    if (personNames == null) personNames = new String[2][0];
    k.firstNames = personNames[0];
    k.lastNames  = personNames[1];
    
    return k;
  }
  
  
  public static Kind ofProp(
    String name, String ID, String spritePath,
    int wide, int high, int blockLevel, boolean blockSight,
    Object... initStats
  ) {
    Kind k = new Kind(name, ID, "", TYPE_PROP);
    k.wide = wide;
    k.high = high;
    k.blockLevel = blockLevel;
    k.blockSight = blockSight;
    initStatsFor(k, initStats);
    
    k.sprite = loadImage(spritePath);
    return k;
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









