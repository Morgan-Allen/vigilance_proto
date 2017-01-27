

package proto.game.world;
import proto.common.*;
import proto.util.*;



public class Region extends Element {
  
  
  /**  Data fields, construction and save/load methods-
    */
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
  Level statLevels[];
  Place buildSlots[];
  
  
  
  public Region(RegionType kind, World world) {
    super(kind, world);
    initStats();
    buildSlots = new Place[kind.maxFacilities];
  }
  
  
  private void initStats() {
    statLevels = new Level[ALL_STATS.length];
    for (int i = 0; i < ALL_STATS.length; i++) {
      final Level l = this.statLevels[i] = new Level();
      l.stat = ALL_STATS[i];
    }
  }
  
  
  public Region(Session s) throws Exception {
    super(s);
    initStats();
    
    for (Level l : statLevels) {
      l.bonus   = s.loadInt  ();
      l.level   = s.loadInt  ();
      l.current = s.loadFloat();
    }
    buildSlots = (Place[]) s.loadObjectArray(Place.class);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    for (Level l : statLevels) {
      s.saveInt(l.bonus);
      s.saveInt(l.level);
      s.saveFloat(l.current);
    }
    s.saveObjectArray(buildSlots);
  }
  
  
  public RegionType kind() {
    return (RegionType) kind;
  }
  
  
  
  /**  General stat-queries and modifications-
    */
  public void nudgeCurrentStat(Stat stat, float inc) {
    Level l = this.statLevels[stat.ID];
    final float oldL = l.current;
    l.current = Nums.clamp(l.current + inc, 0, 100);
    if (oldL != l.current) recordChange(stat, oldL, l.current);
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
    
    for (Place slot : buildSlots) {
      if (slot == null || slot.owner() != base) continue;
      if (slot.buildProgress() < 1) continue;
      
      final int inc = slot.kind().incomeFrom(this);
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
    return kind().maxFacilities;
  }
  
  
  public Series <PlaceType> facilitiesAvailable(Base base) {
    final Batch <PlaceType> all = new Batch();
    for (Object tech : base.knownTech) {
      if (tech instanceof PlaceType) all.add((PlaceType) tech);
    }
    return all;
  }
  
  
  public Place buildSlot(int slotID) {
    return buildSlots[slotID];
  }
  
  
  public PlaceType slotType(int slotID) {
    Place slot = buildSlots[slotID];
    return slot == null ? null : slot.kind();
  }
  
  
  public Place setupFacility(
    PlaceType print, int slotID, Base owns, boolean complete
  ) {
    final Place oldPlace = buildSlots[slotID];
    if (oldPlace != null) world.setInside(oldPlace, false);
    
    final Place place = new Place(print, slotID, world);
    if (complete || owns == null) {
      place.setOwner(owns);
      place.setBuildProgress(1);
      place.updateResidents();
    }
    else {
      place.setOwner(owns);
      owns.finance.incPublicFunds(0 - place.kind().buildCost);
      place.setBuildProgress(0);
    }
    
    buildSlots[slotID] = place;
    setAttached(place, true);
    world.setInside(place, true);
    return place;
  }
  
  
  
  /**  Updates and life-cycle:
    */
  public void initialiseRegion(Base owns) {
    
    final PlaceType DF[] = kind().defaultFacilities;
    for (int i = 0; i < buildSlots.length; i++) {
      if (DF == null || i >= DF.length) break;
      setupFacility(DF[i], i, owns, true);
    }
    updateRegion();
    
    for (Stat stat : CIVIC_STATS) {
      final Level l = statLevels[stat.ID];
      l.current = l.level + l.bonus;
    }
    updateRegion();
  }
  
  
  public void updateRegion() {
    if (! world.timing.dayIsUp()) return;
    
    //
    //  Reset the bonus for all stats to zero, then iterate across all built
    //  facilities and collect their bonuses (including for income.)
    for (Level l : statLevels) {
      l.bonus = 0;
    }
    int baseIncome = 0, mobIncome = 0, totalIncome = 0;
    
    for (Place slot : buildSlots) if (slot != null) {
      final PlaceType built = slot.kind();
      final Base      owns  = slot.owner();
      final float     prog  = slot.buildProgress();
      
      if (built == null) {
        continue;
      }
      else if (prog < 1) {
        float moreProg = 1f / built.buildTime;
        slot.setBuildProgress(prog + moreProg);
      }
      else {
        final int income = built.incomeFrom(this);
        totalIncome += Nums.max(0, income);
        if      (owns == world.playerBase()) baseIncome += income;
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
    return kind().name();
  }
  
  
  public void recordChange(Stat stat, float oldLevel, float newLevel) {
    float inc = newLevel - oldLevel;
    String desc = "";
    if (inc > 0) desc += stat.name+" +"+inc;
    else if (stat.oppName != null) desc += stat.oppName+" +"+(0 - inc);
    else desc += stat.name+" -"+(0 - inc);
    desc += ": "+kind();
    world.events.log(desc, Events.EVENT_MAJOR);
  }
}









