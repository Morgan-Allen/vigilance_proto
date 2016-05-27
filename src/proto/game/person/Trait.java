

package proto.game.person;
import proto.common.*;
import proto.util.*;

import java.awt.Image;



public class Trait extends Index.Entry implements Session.Saveable  {
  
  
  final static Index <Trait> INDEX = new Index <Trait> ();
  
  final public String name;
  final public String description;
  final public Image icon;
  
  
  protected Trait(String name, String ID, String imgPath, String description) {
    super(INDEX, ID);
    this.name        = name;
    this.description = description;
    this.icon        = Kind.loadImage(imgPath);
  }
  
  
  public static Trait loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public String toString() {
    return name;
  }
  
  
  public Image icon() {
    return icon;
  }
}





