

package proto.game.plans;
import proto.util.*;



public class Thing {

  
  /**  Data fields, constructors and save/load methods-
    */
  final static Object
    TYPE_PERSON = "Person",
    TYPE_ITEM   = "Item"  ,
    TYPE_PLACE  = "Place" ,
    TYPE_INFO   = "Info"  ,
    TYPE_LIST   = "List"  ,
    TYPE_FACT   = "Fact"
  ;
  final static String
    STAT_GUNS    = "Guns"   ,
    STAT_BRAWL   = "Brawl"  ,
    STAT_DRIVING = "Driving",
    STAT_WIRING  = "Wiring" ,
    STAT_CHARM   = "Charm"  ,
    STAT_SMARTS  = "Smarts" ,
    PERSON_STATS[] = {
      STAT_GUNS, STAT_BRAWL, STAT_DRIVING,
      STAT_WIRING, STAT_CHARM, STAT_SMARTS
    },
    
    STAT_MAKE_DC  = "Make DC" ,
    STAT_BUY_COST = "Buy Cost",
    PROP_SAFE     = "Has Safe",
    PROP_BOMB     = "Is Bomb"
  ;
  
  static int nextID = 0;
  
  Object type;
  String name;
  int uniqueID = nextID++;
  
  Thing owner;
  Thing contains;
  List <Thing> inside = new List();
  
  Table <String, Object> props = new Table <String, Object> ();
  
  
  Thing(Object type, String name) {
    this.type = type;
    this.name = name;
  }
  
  
  void setOwner(Thing owner) {
    this.owner = owner;
  }
  
  
  void beInside(Thing contains, boolean is) {
    if (is) {
      contains.inside.add(this);
      this.contains = contains;
    }
    else {
      contains.inside.remove(this);
      this.contains = null;
    }
  }
  
  
  
  /**  Setting and accessing properties/values-
    */
  void setValue(String label, Object val) {
    props.put(label, val);
  }
  
  
  Object valueFor(String label) {
    return props.get(label);
  }
  
  
  float statValue(String label) {
    Object val = props.get(label);
    if (val instanceof Integer) return (Integer) val;
    if (val instanceof Float  ) return (Float  ) val;
    return 0;
  }
  
  
  boolean propValue(String label) {
    Object val = props.get(label);
    return val != null && val.equals(true);
  }
  
  
  boolean propEquals(String label, Object val) {
    Object current = props.get(label);
    if (val == null && current == null) return true ;
    if (val == null || current == null) return false;
    return val.equals(current);
  }
  
  
  
  /**  Debugging and interface methods-
    */
  public String toString() {
    return name+" (#"+uniqueID+")";
  }
  
  
  public String longDescription() {
    final StringBuffer s = new StringBuffer();
    s.append(this);
    s.append(", contents:");
    for (Thing agent : inside) {
      s.append("\n  "+agent);
      if (agent.owner != null) s.append(" ("+agent.owner+")");
    }
    return s.toString();
  }
}




