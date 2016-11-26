

package proto.common;
import proto.game.person.*;
import proto.util.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;



public class Kind extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Kind> INDEX = new Index <Kind> ();
  final public static int
    TYPE_PROP     = 0,
    TYPE_CIVILIAN = 1,
    TYPE_HERO     = 2,
    TYPE_MOOK     = 3,
    TYPE_BOSS     = 4
  ;
  
  String name;
  String defaultInfo;
  Image sprite;
  
  //  TODO:  Move these out into a dedicated person/prop-kind class.
  int type;
  int wide, high;
  boolean blockSight;
  boolean blockPath;
  
  Table <Trait, Integer> traitLevels = new Table();
  Trait    baseTraits  [] = new Trait   [0];
  Equipped baseEquipped[] = new Equipped[0];
  Equipped customItems [] = new Equipped[0];
  
  
  protected Kind(String name, String uniqueID, String info) {
    super(INDEX, uniqueID);
    this.name        = name;
    this.defaultInfo = info;
  }
  
  
  public static Kind loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  public static Kind ofPerson(
    String name, String ID, String spritePath, String defaultInfo,
    int type, Object... initStats
  ) {
    Kind k = new Kind(name, ID, defaultInfo);
    k.type = type;
    k.wide = k.high = 1;
    k.blockPath = k.blockSight = false;
    
    Batch <Trait   > allT = new Batch();
    Batch <Equipped> allE = new Batch();
    Batch <Equipped> allC = new Batch();
    Trait   readT = null;
    Integer readL = null;
    
    for (Object o : initStats) {
      if (o instanceof Trait) {
        readT = (Trait) o;
      }
      if (o instanceof Integer) {
        readL = (Integer) o;
      }
      if (o instanceof Equipped) {
        Equipped e = (Equipped) o;
        allE.add(e);
        if (e.isCustom()) allC.add(e);
      }
      if (readT != null && readL != null) {
        allT.add(readT);
        k.traitLevels.put(readT, readL);
        readT = null;
        readL = null;
      }
    }
    k.baseTraits   = allT.toArray(Trait   .class);
    k.baseEquipped = allE.toArray(Equipped.class);
    k.customItems  = allC.toArray(Equipped.class);
    
    k.sprite = loadImage(spritePath);
    return k;
  }
  
  
  public static Kind ofProp(
    String name, String ID, String spritePath,
    int wide, int high, boolean blockPath, boolean blockSight
  ) {
    Kind k = new Kind(name, ID, "");
    k.type = TYPE_PROP;
    k.wide = wide;
    k.high = high;
    k.blockPath  = blockPath ;
    k.blockSight = blockSight;
    
    k.sprite = loadImage(spritePath);
    return k;
  }
  
  
  public int baseLevel(Trait t) {
    final Integer l = traitLevels.get(t);
    return l == null ? 0 : l;
  }
  
  
  public Trait[] baseTraits() {
    return baseTraits;
  }
  
  
  public int type() { return type; }
  public int wide() { return wide; }
  public int high() { return high; }
  public boolean blockSight() { return blockSight; }
  public boolean blockPath () { return blockPath ; }
  
  public Equipped[] baseEquipped() { return baseEquipped; }
  public Equipped[] customItems () { return customItems ; }
  
  public String name  () { return name  ; }
  public Image  sprite() { return sprite; }
  
  public String defaultInfo() { return defaultInfo; }
  
  
  public static Image loadImage(String imgPath) {
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









