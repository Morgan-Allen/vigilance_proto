

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;

//  TODO:  Define this externally.
import proto.content.techs.Facilities;



public class District implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public World world;
  final public Region region;
  
  public static class Stat {
    
    final int ID;
    final String name, oppName;
    
    Stat(String name, int ID) {
      this(name, null, ID);
    }
    
    Stat(String name, String oppName, int ID) {
      this.ID      = ID;
      this.name    = name;
      this.oppName = oppName;
    }
    
    public String toString() {
      return name;
    }
  }
  
  final public static int
    BASE_CIVIC_STAT  = 5,
    MAX_CIVIC_STAT   = 10,
    STAT_DRIFT_TIME  = World.DAYS_PER_WEEK * World.WEEKS_PER_YEAR * 10,
    REPUTATION_DECAY = 1,
    
    BUILD_TIME_SHORT  = 6  * World.DAYS_PER_WEEK * 4,
    BUILD_TIME_MEDIUM = 12 * World.DAYS_PER_WEEK * 4,
    BUILD_TIME_LONG   = 18 * World.DAYS_PER_WEEK * 4,
    BUILD_TIME_NONE   = -1
  ;
  
  final public static Stat
    EMPLOYMENT             = new Stat("Employment"          , "Poverty", 0),
    EDUCATION_AND_CULTURE  = new Stat("Education & Culture" , "Vice"   , 1),
    HEALTH_AND_ENVIRONMENT = new Stat("Health & Environment", "Squalor", 2),
    ENTERTAINMENT          = new Stat("Entertainment"       , "Despair", 3),
    
    //  TODO:  Rename as something shorter?
    CIVIC_STATS[] = {
      EMPLOYMENT            , EDUCATION_AND_CULTURE,
      HEALTH_AND_ENVIRONMENT, ENTERTAINMENT        ,
    },
    
    DETERRENCE = new Stat("Deterrence", 4),
    TRUST      = new Stat("Trust"     , 5),
    CORRUPTION = new Stat("Corruption", 6),
    VIOLENCE   = new Stat("Violence"  , 7),
    SOCIAL_STATS[] = { DETERRENCE, TRUST, CORRUPTION, VIOLENCE },
    
    INCOME = new Stat("Income", "Expense", 8),
    FINANCE_STATS[] = { INCOME },
    
    ALL_STATS[] = (Stat[]) Visit.compose(
      Stat.class, CIVIC_STATS, SOCIAL_STATS, FINANCE_STATS
    )
  ;
  
  
  static class Level { Stat stat; int level, bonus; float current; }
  static class Slot { Facility built; Base owns; float progress; }
  
  Level statLevels[];
  Slot buildSlots[];
  
  
  
  District(Region region, World world) {
    this.world  = world ;
    this.region = region;
    initStatsAndSlots();
  }
  
  
  private void initStatsAndSlots() {
    statLevels = new Level[ALL_STATS.length];
    for (int i = 0; i < ALL_STATS.length; i++) {
      final Level l = this.statLevels[i] = new Level();
      l.stat = ALL_STATS[i];
    }
    buildSlots = new Slot[region.maxFacilities];
    for (int i = 0; i < buildSlots.length; i++) {
      final Slot s = this.buildSlots[i] = new Slot();
    }
  }
  
  
  public District(Session s) throws Exception {
    s.cacheInstance(this);
    world  = (World ) s.loadObject();
    region = (Region) s.loadObject();
    initStatsAndSlots();
    
    for (Level l : statLevels) {
      l.bonus   = s.loadInt  ();
      l.level   = s.loadInt  ();
      l.current = s.loadFloat();
    }
    for (Slot slot : buildSlots) {
      slot.built    = (Facility) s.loadObject();
      slot.owns     = (Base    ) s.loadObject();
      slot.progress = s.loadFloat();
    }
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(world );
    s.saveObject(region);
    
    for (Level l : statLevels) {
      s.saveInt(l.bonus);
      s.saveInt(l.level);
      s.saveFloat(l.current);
    }
    for (Slot slot : buildSlots) {
      s.saveObject(slot.built   );
      s.saveObject(slot.owns    );
      s.saveFloat (slot.progress);
    }
  }
  
  
  
  /**  General stat-queries and modifications-
    */
  public void incLevel(Stat stat, int inc) {
    Level l = this.statLevels[stat.ID];
    
    final int oldL = l.level;
    l.level = Nums.clamp(l.level + inc, 100);

    if (oldL != l.level) {
      String desc = "";
      if (inc > 0) desc += stat.name+" +"+inc;
      else if (stat.oppName != null) desc += stat.oppName+" +"+(0 - inc);
      else desc += stat.name+" -"+(0 - inc);
      desc += ": "+region;
      
      world.events.log(desc, Event.EVENT_MAJOR);
    }
  }
  
  
  public void setLevel(Stat stat, int level, boolean asCurrent) {
    Level l = this.statLevels[stat.ID];
    l.level = Nums.clamp(level, 1000);
    if (asCurrent) l.current = l.level + l.bonus;
  }
  
  
  public int longTermValue(Stat stat) {
    Level l = this.statLevels[stat.ID];
    return l.bonus + l.level;
  }
  
  
  public float currentValue(Stat stat) {
    Level l = this.statLevels[stat.ID];
    return l.current;
  }
  
  
  private int incomeFor(Base base, boolean positive) {
    int total = 0;
    for (Slot slot : buildSlots) {
      if (slot.owns != base || slot.built == null) continue;
      if (slot.progress < 1) continue;
      
      final int inc = slot.built.incomeFrom(this);
      if (positive) total += Nums.max(inc, 0      );
      else          total += Nums.max(0  , 0 - inc);
    }
    return total;
  }
  
  
  public int incomeFor(Base base) {
    return incomeFor(base, true);
  }
  
  
  public int expensesFor(Base base) {
    return incomeFor(base, false);
  }
  
  
  
  /**  Construction and ownership-
    */
  public int maxFacilities() {
    return region.maxFacilities;
  }
  
  
  public Series <Facility> facilitiesAvailable() {
    //  TODO:  MOVE THIS SELECTION ELSEWHERE
    final Batch <Facility> all = new Batch();
    Visit.appendTo(all,
      Facilities.BUSINESS_PARK,
      Facilities.CHEMICAL_PLANT,
      Facilities.STEEL_MILL,
      Facilities.UNION_OFFICE,
      Facilities.TECH_STARTUP,
      Facilities.CITY_PARK,
      Facilities.COMMUNITY_COLLEGE,
      Facilities.ROBINS_CAMP,
      Facilities.SOUP_KITCHEN
    );
    return all;
  }
  
  
  public Facility builtInSlot(int slotID) {
    return buildSlots[slotID].built;
  }
  
  
  public Base ownerForSlot(int slotID) {
    return buildSlots[slotID].owns;
  }
  
  
  public float buildProgress(int slotID) {
    return buildSlots[slotID].progress;
  }
  
  
  public void beginConstruction(Facility builds, Base owns, int slotID) {
    final Slot slot = buildSlots[slotID];
    slot.built    = builds;
    slot.owns     = owns  ;
    slot.progress = 0     ;
    
    owns.incFunding(0 - builds.buildCost);
  }
  
  
  
  /**  Updates and life-cycle:
    */
  public void initialiseDistrict() {
    updateDistrict();
    for (Stat stat : CIVIC_STATS) {
      final Level l = statLevels[stat.ID];
      l.current = l.level + l.bonus;
    }
    updateDistrict();
  }
  
  
  public void updateDistrict() {
    //
    //  Reset the bonus for all stats to zero, then iterate across all built
    //  facilities and collect their bonuses (including for income.)
    for (Level l : statLevels) {
      l.bonus = 0;
    }
    int baseIncome = 0, mobIncome = 0, totalIncome = 0;
    for (int i = 0 ; i < buildSlots.length; i++) {
      final Slot     slot  = buildSlots[i];
      final Facility built = slot.built;
      final Base     owns  = slot.owns;
      final float    prog  = slot.progress;
      
      if (built == null) {
        continue;
      }
      else if (prog < 1) {
        float moreProg = 1f / built.buildTime;
        slot.progress = Nums.clamp(slot.progress + moreProg, 0, 1);
      }
      else {
        final int income = built.incomeFrom(this);
        totalIncome += Nums.max(0, income);
        if      (owns == world.base()) baseIncome += income;
        else if (owns != null        ) mobIncome  += income;
        built.applyStatEffects(this);
      }
    }
    //
    //  All civic stats (employment, education, etc.) help to reduce crime, but
    //  will only converge to their 'ultimate' values quite slowly.
    final int MCS = MAX_CIVIC_STAT;
    float crimeFactor = 0;
    
    for (Stat stat : CIVIC_STATS) {
      final Level l = statLevels[stat.ID];
      
      float drift = MCS * 1f / STAT_DRIFT_TIME;
      l.level = BASE_CIVIC_STAT;
      float longTerm = Nums.min(l.level + l.bonus, MCS);
      
      if (l.current >= longTerm) {
        l.current = Nums.clamp(l.current - drift, longTerm, MCS);
      }
      else {
        l.current = Nums.clamp(l.current + drift, 0, longTerm);
      }
      crimeFactor += Nums.clamp(MCS - l.current, 0, MCS);
    }
    crimeFactor *= 100f / (MCS * CIVIC_STATS.length);
    //
    //  Open violence is reduced by deterrence efforts, while corruption is
    //  based on the average of open violence and the percentage of business
    //  income controlled by criminals.  (Businesses you own help reduce this
    //  further.)
    float deterrence = currentValue(DETERRENCE), corruption;
    crimeFactor = Nums.clamp(crimeFactor - deterrence, 0, 100);
    setLevel(VIOLENCE, (int) crimeFactor, true);
    crimeFactor = currentValue(VIOLENCE);
    
    corruption  = mobIncome * 100f / Nums.max(1, totalIncome + baseIncome);
    corruption  = (corruption + crimeFactor) / 2;
    setLevel(DETERRENCE, (int) deterrence - REPUTATION_DECAY, true);
    setLevel(CORRUPTION, (int) corruption , true);
    setLevel(INCOME    , (int) totalIncome, true);
    
    //  TODO:  Rate social stats out of a hundred, so you can see improvement
    //  (or decay) within a week or two.
  }
  
  
  
  
  /**  Rendering and debug methods-
    */
  public String toString() {
    return region.name;
  }
}






