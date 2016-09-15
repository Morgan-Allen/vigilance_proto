

package proto.game.plans;
import proto.util.*;



public class TypeHeist extends StepType {
  
  
  //  TODO:  Replace with 'mole', 'explosives', 'getaway', 'lookout' and
  //  a generic 'muscle' role- plus the venue itself.
  
  final static Role
    VENUE     = new Role("Venue"    , 0),
    EXPLOSIVE = new Role("Explosive", 1),
    MOLE      = new Role("Mole"     , 2),
    DRIVER    = new Role("Driver"   , 3),
    LOOKOUT   = new Role("Lookout"  , 4),
    MUSCLE    = new Role("Muscle"   , 5)
  ;
  
  
  TypeHeist() { super(
    "Heist",
    rolesFor("plan", "travel", "scene", "getaway"),
    new Role[] { VENUE, EXPLOSIVE, MOLE, DRIVER, LOOKOUT, MUSCLE },
    rolesFor("loot")
  ); }
  
  

  Series <Thing> availableTargets(Role role, Thing world) {
    if (role == EXPLOSIVE) {
      Thing bomb = new Thing(Thing.TYPE_ITEM, "bomb");
      bomb.setValue(Thing.STAT_MAKE_DC, 5);
      bomb.setValue(Thing.PROP_BOMB, true);
      return new Batch(bomb);
    }
    return super.availableTargets(role, world);
  }
  
  
  float calcSuitability(Thing used, Role role, PlanStep step) {
    if (role == VENUE) {
      if (used.type != Thing.TYPE_PLACE) return 0;
      return 1;
    }
    if (role == EXPLOSIVE) {
      if (used.type != Thing.TYPE_ITEM     ) return 0;
      if (! used.propValue(Thing.PROP_BOMB)) return 0;
      return 1;
    }
    if (role == MOLE) {
      if (used.type != Thing.TYPE_PERSON) return 0;
      Thing venue = step.needs[VENUE.ID];
      if (venue == null || used.owner != venue) return 0;
      return 1;
    }
    if (role == DRIVER) {
      if (used.type != Thing.TYPE_PERSON) return 0;
      return used.statValue(Thing.STAT_DRIVING) / 10;
    }
    if (role == LOOKOUT) {
      if (used.type != Thing.TYPE_PERSON) return 0;
      return used.statValue(Thing.STAT_SMARTS) / 10;
    }
    if (role == MUSCLE) {
      if (used.type != Thing.TYPE_PERSON) return 0;
      return used.statValue(Thing.STAT_BRAWL) / 10;
    }
    return 0;
  }
}

