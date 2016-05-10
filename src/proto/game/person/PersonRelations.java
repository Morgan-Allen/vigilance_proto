

package proto.game.person;
import proto.common.*;
import proto.util.*;



public class PersonRelations {
  
  
  final Person person;
  
  static class Relation { Person other; float value; }
  Table <Person, Relation> relations = new Table();
  
  
  PersonRelations(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    for (int n = s.loadInt(); n-- > 0;) {
      Relation r = new Relation();
      r.other = (Person) s.loadObject();
      r.value = s.loadFloat();
      relations.put(r.other, r);
    }
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt(relations.size());
    for (Relation r : relations.values()) {
      s.saveObject(r.other);
      s.saveFloat (r.value);
    }
  }
  
  
  
  public float valueFor(Person other) {
    Relation r = relations.get(other);
    return r == null ? 0 : r.value;
  }
  
  
  public void setRelation(Person other, float value) {
    Relation r = relations.get(other);
    if (r == null) relations.put(other, r = new Relation());
    r.other = other;
    r.value = value;
  }
  
  
  public void incRelation(Person other, float inc) {
    setRelation(other, valueFor(other) + inc);
  }
}







