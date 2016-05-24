

package proto.game.person;
import proto.common.*;
import proto.util.*;



public class PersonBonds {
  
  
  final Person person;
  
  static class Bond { Person other; float value; }
  Table <Person, Bond> relations = new Table();
  
  
  PersonBonds(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    for (int n = s.loadInt(); n-- > 0;) {
      Bond r = new Bond();
      r.other = (Person) s.loadObject();
      r.value = s.loadFloat();
      relations.put(r.other, r);
    }
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt(relations.size());
    for (Bond r : relations.values()) {
      s.saveObject(r.other);
      s.saveFloat (r.value);
    }
  }
  
  
  
  public float valueFor(Person other) {
    Bond r = relations.get(other);
    return r == null ? 0 : r.value;
  }
  
  
  public void setBond(Person other, float value) {
    Bond r = relations.get(other);
    if (r == null) relations.put(other, r = new Bond());
    r.other = other;
    r.value = value;
  }
  
  
  public void incBond(Person other, float inc) {
    setBond(other, valueFor(other) + inc);
  }
  
  
  public Series <Person> sortedBonds() {
    final List <Person> all = new List <Person> () {
      protected float queuePriority(Person p) {
        return Nums.abs(valueFor(p));
      }
    };
    for (Person p : relations.keySet()) all.add(p);
    all.queueSort();
    return all;
  }
}







