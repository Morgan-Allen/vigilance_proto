

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
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
  
  
  public Image icon() {
    return icon;
  }
}





