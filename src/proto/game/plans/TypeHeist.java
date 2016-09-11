

package proto.game.plans;



public class TypeHeist extends ActionType {
  
  
  final static Role
    DRIVER = new Role("Driver", 0),
    MUSCLE = new Role("Muscle", 1),
    GUNNER = new Role("Gunner", 2),
    BOSS   = new Role("Boss"  , 3)
  ;
  
  
  TypeHeist() { super(
    "Heist",
    rolesFor("plan", "travel", "scene", "getaway"),
    new Role[] { DRIVER, MUSCLE, GUNNER, BOSS },
    rolesFor("loot")
  ); }
  
  
  float calcSuitability(Thing used, Role role, Action action) {
    if (used.type != Thing.TYPE_PERSON) return 0;
    
    if (role == DRIVER) {
      return used.statValue(Thing.STAT_DRIVING) / 10;
    }
    if (role == MUSCLE) {
      return used.statValue(Thing.STAT_BRAWL) / 10;
    }
    if (role == GUNNER) {
      return used.statValue(Thing.STAT_GUNS) / 10;
    }
    if (role == BOSS) {
      return used.statValue(Thing.STAT_SMARTS) / 10;
    }
    return 0;
  }
  
}



