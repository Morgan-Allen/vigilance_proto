

package proto.game.plans;
import proto.util.*;



public class Thing {

  
  final static Object
    TYPE_PERSON = "Person",
    TYPE_ITEM   = "Item"  ,
    TYPE_PLACE  = "Place" ,
    TYPE_LIST   = "List"  ,
    TYPE_FACT   = "Fact"
  ;
  final static String
    STAT_GUNS    = "Guns",
    STAT_BRAWL   = "Brawl",
    STAT_DRIVING = "Driving",
    STAT_WIRING  = "Wiring",
    STAT_CHARM   = "Charm",
    STAT_SMARTS  = "Smarts",
    ALL_STATS[] = {
      STAT_GUNS, STAT_BRAWL, STAT_DRIVING,
      STAT_WIRING, STAT_CHARM, STAT_SMARTS
    }
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
  
  
  void setInside(Thing contains, boolean is) {
    if (is) {
      contains.inside.add(this);
      this.contains = contains;
    }
    else {
      contains.inside.remove(this);
      this.contains = null;
    }
  }
  
  
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
  
  
  boolean propEquals(String label, Object val) {
    Object current = props.get(label);
    if (val == null && current == null) return true ;
    if (val == null || current == null) return false;
    return val.equals(current);
  }
  
  
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




