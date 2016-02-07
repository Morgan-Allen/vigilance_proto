

package proto.game.world;
import proto.common.*;
import proto.game.content.UrbanScene;
import proto.game.scene.Scene;
import proto.util.*;



public class Nation implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Region region;
  
  List <Object> infrastructure = new List();
  float
    crime      ,
    wealth     ,
    environment,
    education  ,
    equality   ,
    freedom    ;
  
  float   trust  ;
  boolean member ;
  int     funding;
  Scene   mission;
  
  
  Nation(Region region) {
    this.region = region;
    
    this.crime       = region.defaultCrime;
    this.wealth      = region.defaultWealth;
    this.environment = region.defaultEnvironment;
    this.education   = region.defaultEducation;
    this.equality    = region.defaultEquality;
    this.freedom     = region.defaultFreedom;

    this.trust   = region.defaultTrust;
    this.funding = region.defaultFunding;
    this.member  = region.defaultMember;
  }
  
  
  public Nation(Session s) throws Exception {
    s.cacheInstance(this);
    region = (Region) s.loadObject();
    
    s.loadObjects(infrastructure);
    crime       = s.loadFloat();
    wealth      = s.loadFloat();
    environment = s.loadFloat();
    education   = s.loadFloat();
    equality    = s.loadFloat();
    freedom     = s.loadFloat();
    
    trust   = s.loadFloat();
    member  = s.loadBool();
    funding = s.loadInt();
    mission = (Scene) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(region);
    
    s.saveObjects(infrastructure);
    s.saveFloat(crime      );
    s.saveFloat(wealth     );
    s.saveFloat(environment);
    s.saveFloat(education  );
    s.saveFloat(equality   );
    s.saveFloat(freedom    );

    s.saveFloat (trust  );
    s.saveBool  (member );
    s.saveInt   (funding);
    s.saveObject(mission);
  }
  
  
  /**  General query methods-
    */
  public float   trustLevel() { return trust  ; }
  public float   crimeLevel() { return crime  ; }
  public int     funding   () { return funding; }
  public boolean member    () { return member ; }
  
  
  
  /**  Life cycle and update methods-
    */
  Scene generateCrisis(World world) {
    final Scene s = new UrbanScene(world, 100);
    
    int expireTime = world.currentTime + 1 + Rand.index(3);
    float dangerLevel = 1;
    if (Rand.num() < world.base.sensorChance()) {
      dangerLevel = 0.75f * (Rand.avgNums(2) + 1.5f) / 2;
    }
    else {
      dangerLevel = 1.25f * (Rand.avgNums(2) + 1.5f) / 2;
    }
    s.assignMissionParameters(
      "Hostage situation in "+Rand.pickFrom(region.cities),
      this, dangerLevel, expireTime, null
    );
    return s;
  }
  
  
  public Scene currentMission() {
    return mission;
  }
  
  
  public void applyMissionEffects(
    Scene s, boolean success,
    float dangerLevel, float collateral, float getaways
  ) {
    trust += (1 - getaways) * dangerLevel / 10;
    crime += getaways       * dangerLevel / 10;
    trust -= collateral / 10f;
    crime -= collateral / 10f;
    
    if (success) {
      trust += 0.5f / 10;
      crime -= 0.5f / 10;
    }
    else {
      trust -= 0.5f * dangerLevel / 10;
      crime += 0.5f * dangerLevel / 10;
    }
    trust = Nums.clamp(trust, -1, 1);
    crime = Nums.clamp(crime,  0, 2);
  }
  
  
  
  /**  Rendering and debug methods-
    */
  public String toString() {
    return region.name;
  }
}







