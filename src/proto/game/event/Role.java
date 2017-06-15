
package proto.game.event;
import proto.common.Session;
import proto.util.Index;



public class Role extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Role> ROLES_INDEX = new Index <Role> ();
  
  final public String name;
  final public String category;
  final public String descTemplate;
  
  
  Role(String ID, String name, String category, String descTemplate) {
    super(ROLES_INDEX, ID);
    this.name = name;
    this.category = category;
    this.descTemplate = descTemplate;
  }
  
  public static Role loadConstant(Session s) throws Exception {
    return ROLES_INDEX.loadEntry(s.input());
  }
  
  public void saveState(Session s) throws Exception {
    ROLES_INDEX.saveEntry(this, s.output());
  }
  
  public String toString() {
    return name;
  }
  
  public boolean isAspect() { return category == Plot.ASPECT; }
  public boolean isPerp  () { return category == Plot.PERP  ; }
  public boolean isItem  () { return category == Plot.ITEM  ; }
  public boolean isScene () { return category == Plot.SCENE ; }
  public boolean isVictim() { return category == Plot.VICTIM; }
}




