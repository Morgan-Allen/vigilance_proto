

package proto.game.person;
import proto.common.*;
import proto.util.*;



public class Trait extends Index.Entry implements Session.Saveable  {
  
  
  final static Index <Trait> INDEX = new Index <Trait> ();
  
  final public String name;
  final public String description;
  

  protected Trait(String name, String ID, String description) {
    super(INDEX, ID);
    this.name = name;
    this.description = description;
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
}
