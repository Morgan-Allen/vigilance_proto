

package proto.game.scene;
import proto.*;
import proto.common.Session;
import proto.util.*;



public class PersonStats {
  
  
  final Person person;
  
  int totalXP = 0;
  List <Ability> abilities = new List();
  class Level {
    float level, practice, bonus;
    boolean learned;
  }
  Table <Ability, Level> levels = new Table();
  
  
  PersonStats(Person p) {
    person = p;
  }
  
  
  void initFrom(Kind kind) {
    for (int n = 0; n < kind.baseAbilities.length; n++) {
      Ability a = kind.baseAbilities[n];
      setLevel(a, kind.baseAbilityLevels[n], true);
    }
  }
  
  
  void loadState(Session s) throws Exception {
    totalXP = s.loadInt();
    s.loadObjects(abilities);
    for (int n = s.loadInt(); n-- > 0;) {
      Ability key = (Ability) s.loadObject();
      Level l = new Level();
      l.level    = s.loadFloat();
      l.practice = s.loadFloat();
      l.bonus    = s.loadFloat();
      l.learned  = s.loadBool ();
      levels.put(key, l);
    }
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveInt(totalXP);
    s.saveObjects(abilities);
    s.saveInt(levels.size());
    for (Ability a : levels.keySet()) {
      Level l = levels.get(a);
      s.saveObject(a);
      s.saveFloat(l.level   );
      s.saveFloat(l.practice);
      s.saveFloat(l.bonus   );
      s.saveBool (l.learned );
    }
  }
  
  
  public Series <Ability> learnedAbilities() {
    return abilities;
  }
  
  
  public int levelFor(Ability ability) {
    Level l = levels.get(ability);
    if (l == null) return 0;
    return (int) l.level;
  }
  
  
  public Series <Ability> listAbilities() {
    //  TODO:  I'm not certain, at the moment, if this shouldn't be considered
    //  a UI method.  Check.
    
    Batch <Ability> all = new Batch();
    for (Ability a : abilities) all.add(a);
    for (Equipped e : person.equipSlots) if (e != null) {
      for (Ability a : e.abilities) all.add(a);
    }
    return all;
  }

  
  
  /**  Assigning experience and abilities-
    */
  public void setLevel(Ability a, int level, boolean learned) {
    if (level > 0) {
      Level l = levels.get(a);
      if (l == null) levels.put(a, l = new Level());
      l.level = level;
      l.practice = 0;
      l.learned = learned;
      if (learned) abilities.include(a);
    }
    else {
      levels.remove(a);
      abilities.remove(a);
    }
  }
  
  
  public void gainXP(int XP) {
    totalXP += XP;
  }
  
  
  public void toggleItemAbilities(Equipped item, boolean active) {
    if (item == null) return;
    for (Ability a : item.abilities) {
      if (! a.equipped()) continue;
      setLevel(a, active ? 1 : 0, false);
    }
  }
}














