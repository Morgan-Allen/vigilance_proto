

package proto.game.person;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.util.*;



public class PersonHistory {
  
  
  final Person person;
  String alias   = null;
  String summary = null;
  
  static class Bond { Element other; float value; }
  Table <Element, Bond> relations = new Table();
  
  List <Clue> memories = new List();
  
  
  PersonHistory(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    alias   = s.loadString();
    summary = s.loadString();
    for (int n = s.loadInt(); n-- > 0;) {
      Bond r = new Bond();
      r.other = (Element) s.loadObject();
      r.value = s.loadFloat();
      relations.put(r.other, r);
    }
    s.loadObjects(memories);
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveString(alias  );
    s.saveString(summary);
    s.saveInt(relations.size());
    for (Bond r : relations.values()) {
      s.saveObject(r.other);
      s.saveFloat (r.value);
    }
    s.saveObjects(memories);
  }
  
  
  
  public float valueFor(Element other) {
    Bond r = relations.get(other);
    return r == null ? 0 : r.value;
  }
  
  
  public void setBond(Element other, float value) {
    Bond r = relations.get(other);
    if (r == null) relations.put(other, r = new Bond());
    r.other = other;
    r.value = value;
  }
  
  
  public void incBond(Element other, float inc) {
    setBond(other, valueFor(other) + inc);
  }
  
  
  public Series <Element> sortedBonds() {
    final List <Element> all = new List <Element> () {
      protected float queuePriority(Element p) {
        return Nums.abs(valueFor(p));
      }
    };
    for (Element p : relations.keySet()) all.add(p);
    all.queueSort();
    return all;
  }
  

  
  /**  Rendering, debug and interface methods-
    */
  public void setSummary(String s) {
    summary = s;
  }
  
  
  public void setAlias(String a) {
    alias = a;
  }
  
  
  public String summary() {
    return summary;
  }
  
  
  public String alias() {
    return alias;
  }
}







