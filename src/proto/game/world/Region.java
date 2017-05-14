

package proto.game.world;
import proto.common.*;
import proto.util.*;



public class Region extends Element {
  
  
  /**  Data fields, construction and save/load methods-
    */
  public static class Stat {
    
    final int ID;
    final public String name, oppName, description;
    
    
    Stat(String name, int ID, String description) {
      this(name, null, ID, description);
    }
    
    Stat(String name, String oppName, int ID, String description) {
      this.ID          = ID;
      this.name        = name;
      this.oppName     = oppName;
      this.description = description;
    }
    
    public String toString() {
      return name;
    }
  }
  
  final public static int
    BASE_CIVIC_STAT   = 5,
    MAX_CIVIC_STAT    = 10,
    STAT_DRIFT_TIME   = World.DAYS_PER_WEEK * World.WEEKS_PER_YEAR * 10,
    REPUTATION_DECAY  = 1,
    DEFAULT_TRUST     = 25,
    REPUTE_DRIFT_TIME = World.DAYS_PER_WEEK * 2,
    
    BUILD_TIME_SHORT  = 6  * World.DAYS_PER_WEEK * 4,
    BUILD_TIME_MEDIUM = 12 * World.DAYS_PER_WEEK * 4,
    BUILD_TIME_LONG   = 18 * World.DAYS_PER_WEEK * 4,
    BUILD_TIME_NONE   = -1
  ;
  
  final public static Stat
    EMPLOYMENT = new Stat(
      "Employment", "Poverty", 0,
      "Employment puts money in the pockets of the everyday citizen, while "+
      "reducing the incidence of petty theft and crimes of desperation."
    ),
    EDUCATION = new Stat(
      "Education", "Vice", 1,
      "Education improves citizens' long-term career prospects and improves "+
      "the quality of expertise among your doctors and inventors."
    ),
    HEALTH = new Stat(
      "Health", "Squalor", 2,
      "Hospital facilities, sanitation and green space help to maintain "+
      "a city's physical and psychological well-being."
    ),
    DIVERSION = new Stat(
      "Diversion", "Despair", 3,
      "Theatres, museums, stadia and other entertainment venues keep spirits "+
      "high and ideas fresh."
    ),
    CIVIC_STATS[] = { EMPLOYMENT, EDUCATION, HEALTH, DIVERSION },
    
    DETERRENCE = new Stat(
      "Deterrence", 4,
      "Deterrence suppresses violence and corruption in a given sector, but "+
      "decays over time.  Boost it by preventing criminals' escape, ensuring "+
      "conviction, and paying for police and security firms."
    ),
    TRUST = new Stat(
      "Trust", 5,
      "Trust improves the likelihood of tipoffs from local sources, which "+
      "are vital to most investigations.  Boost it by protecting civilians, "+
      "minimising use of force, and building public amenities."
    ),
    CORRUPTION = new Stat(
      "Corruption", 6,
      "Corruption reduces your chance for convictions after arrest, and "+
      "siphons money away from legitimate investors.  Reduce it by shutting "+
      "down criminals' business fronts and neutralising masterminds."
    ),
    VIOLENCE = new Stat(
      "Violence", 7,
      "Violence keeps common citizens in fear of the mob and takes a "+
      "constant toll on lives and property.  Reduce it by providing jobs, "+
      "healthcare, education and diversions."
    ),
    SOCIAL_STATS[] = { DETERRENCE, TRUST, CORRUPTION, VIOLENCE },
    
    INCOME = new Stat(
      "Income", "Expense", 8,
      "Income and expenses are derived from regional facilities you own."
    ),
    FINANCE_STATS[] = { INCOME },
    
    ALL_STATS[] = (Stat[]) Visit.compose(
      Stat.class, CIVIC_STATS, SOCIAL_STATS, FINANCE_STATS
    )
  ;
  
  static class Level {
    Stat stat;
    float level, bonus, current;
  }
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
      l.bonus   = s.loadFloat();
      l.level   = s.loadFloat();
      l.current = s.loadFloat();
    }
    buildSlots = (Place[]) s.loadObjectArray(Place.class);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    for (Level l : statLevels) {
      s.saveFloat(l.bonus  );
      s.saveFloat(l.level  );
      s.saveFloat(l.current);
    }
    s.saveObjectArray(buildSlots);
  }
  
  
  public RegionType kind() {
    return (RegionType) kind;
  }
  
  
  
  /**  General stat-queries and modifications-
    */
  public void incLevel(Stat stat, float inc, boolean asCurrent) {
    Level l = this.statLevels[stat.ID];
    setLevel(stat, l.level + inc, asCurrent);
  }
  
  
  public void setLevel(Stat stat, float level, boolean asCurrent) {
    Level l = this.statLevels[stat.ID];
    l.level = Nums.clamp(level, 0, 100);
    if (asCurrent) l.current = l.level + l.bonus;
  }
  
  
  public float longTermValue(Stat stat) {
    Level l = this.statLevels[stat.ID];
    return l.bonus + l.level;
  }
  
  
  public float currentValue(Stat stat) {
    Level l = this.statLevels[stat.ID];
    return l.current;
  }
  
  
  private int incomeFor(Base base, boolean positive) {
    int total = 0;
    
    if (positive && ! base.faction().criminal) {
      float crime = 0;
      crime += currentValue(VIOLENCE  ) / 100f;
      crime += currentValue(CORRUPTION) / 100f;
      total += kind().baseFunding * (1 - Nums.clamp(crime, 0, 1));
    }
    
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
  
  
  public Place[] buildSlots() {
    return buildSlots;
  }
  
  
  public Place buildSlot(int slotID) {
    return buildSlots[slotID];
  }
  
  
  public PlaceType slotType(int slotID) {
    Place slot = buildSlots[slotID];
    return slot == null ? null : slot.kind();
  }
  
  
  public int slotFor(Place built) {
    return Visit.indexOf(built, buildSlots);
  }
  
  
  public Place replaceFacility(Place newPlace, Place oldPlace) {
    return replaceFacility(newPlace, slotFor(oldPlace));
  }
  
  
  public Place replaceFacility(Place newPlace, int slotID) {
    Place oldPlace = buildSlots[slotID];
    if (oldPlace != null) {
      setAttached(oldPlace, false);
      world.setInside(oldPlace, false);
    }
    
    buildSlots[slotFor(oldPlace)] = newPlace;
    
    if (newPlace != null) {
      setAttached(newPlace, true);
      world.setInside(newPlace, true);
    }
    return newPlace;
  }
  
  
  public Place setupFacility(
    PlaceType print, int slotID, Faction faction, boolean complete
  ) {
    final Place place = new Place(print, slotID, world);
    Base owns = world.baseFor(faction);
    place.setOwner(owns);
    
    if (complete || owns == null) {
      place.setBuildProgress(1);
      place.updateResidents();
    }
    else {
      owns.finance.incPublicFunds(0 - place.kind().buildCost);
      place.setBuildProgress(0);
    }
    return replaceFacility(place, slotID);
  }
  
  
  
  /**  Updates and life-cycle:
    */
  public void initialiseRegion() {
    for (int i : Visit.range(0, buildSlots.length)) {
      PlaceType p = kind().defaultFacilities.atIndex(i);
      Faction   f = kind().defaultOwners    .atIndex(i);
      if (p == null || f == null) continue;
      Place built = setupFacility(p, i, f, true);
      if (p.isHQ()) built.owner().assignHQ(built);
    }
    
    updateStats(0);
    
    incLevel(Region.TRUST     , kind().defaultTrust     , true);
    incLevel(Region.DETERRENCE, kind().defaultDeterrence, true);
    for (Stat stat : CIVIC_STATS) {
      final Level l = statLevels[stat.ID];
      l.current = l.level + l.bonus;
    }
    updateStats(0);
  }
  
  
  void updateStats(float numDays) {
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
        continue;
      }
      
      final int income = built.incomeFrom(this);
      totalIncome += Nums.max(0, income);
      if      (owns == world.playerBase()) baseIncome += income;
      else if (owns != null              ) mobIncome  += income;
      
      for (Region.Stat stat : Region.ALL_STATS) {
        statLevels[stat.ID].bonus += built.bonusFor(stat);
      }
    }
    //
    //  All civic stats (employment, education, etc.) help to reduce crime, but
    //  will only converge to their 'ultimate' values quite slowly.
    float crimeFactor = 0;
    float driftStat = MAX_CIVIC_STAT * numDays / STAT_DRIFT_TIME;
    for (Stat stat : CIVIC_STATS) {
      final Level l = statLevels[stat.ID];
      l.level = BASE_CIVIC_STAT;
      
      float longTerm = Nums.min(l.level + l.bonus, MAX_CIVIC_STAT);
      l.current = driftValue(l.current, longTerm, driftStat);
      crimeFactor += Nums.clamp(MAX_CIVIC_STAT - l.current, 0, MAX_CIVIC_STAT);
    }
    crimeFactor *= 100f / (MAX_CIVIC_STAT * CIVIC_STATS.length);
    //
    //  Open violence is reduced by deterrence efforts, while corruption is
    //  based on the average of open violence and the percentage of business
    //  income controlled by criminals.  (Businesses you own help reduce this
    //  further.)
    float deterrence = statLevels[DETERRENCE.ID].level;
    float trust      = statLevels[TRUST     .ID].level;
    float corruption = 0;
    
    crimeFactor = Nums.clamp(crimeFactor - deterrence, 0, 100);
    setLevel(VIOLENCE, (int) crimeFactor, true);
    crimeFactor = currentValue(VIOLENCE);
    
    corruption = mobIncome * 100f / Nums.max(1, totalIncome + baseIncome);
    corruption = (corruption + crimeFactor) / 2;
    
    float driftRep = numDays / REPUTE_DRIFT_TIME;
    if (! GameSettings.noDeterDecay) {
      deterrence = driftValue(deterrence, 0, driftRep);
    }
    if (! GameSettings.noTrustDecay) {
      trust = driftValue(trust, DEFAULT_TRUST, driftRep);
    }
    
    setLevel(DETERRENCE, (int) deterrence , true);
    setLevel(TRUST     , (int) trust      , true);
    setLevel(CORRUPTION, (int) corruption , true);
    setLevel(INCOME    , (int) totalIncome, true);
    
    //  TODO:  Rate social stats out of a hundred, so you can see improvement
    //  (or decay) within a week or two?
  }
  
  
  float driftValue(float init, float target, float drift) {
    if (init > target) return Nums.max(init - drift, target);
    else               return Nums.min(init + drift, target);
  }
  
  
  public void updateRegion() {
    if (! world.timing.dayIsUp()) return;
    updateStats(1);
  }
  
  
  
  
  /**  Rendering and debug methods-
    */
  public String toString() {
    return kind().name();
  }
}









