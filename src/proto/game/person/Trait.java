

package proto.game.person;
import proto.common.*;
import proto.util.*;
import java.awt.Image;



public class Trait extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, construction, and save/load methods-
    */
  final static Index <Trait> INDEX = new Index <Trait> ();
  
  final public String name;
  final public String description;
  final public Image icon;
  
  final Trait roots[];
  
  
  public Trait(
    String name, String ID, String imgPath, String description,
    Trait... roots
  ) {
    super(INDEX, ID);
    this.name        = name;
    this.description = description;
    this.icon        = Kind.loadImage(imgPath);
    this.roots       = roots;
  }
  
  
  public static Trait loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  /**  Typing and properties-
    */
  public Trait[] roots() {
    return roots;
  }
  
  
  public float xpRequired(int oldLevel) {
    return (oldLevel + 1) * (oldLevel + 1);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  final public static String LEVEL_DESC[] = {
    "None", "Novice", "Practiced", "Mastered"
  };
  
  
  public String levelDesc(int level) {
    return LEVEL_DESC[Nums.clamp(level, 4)];
  }
  
  
  public String toString() {
    return name;
  }
  
  
  public Image icon() {
    return icon;
  }
}





