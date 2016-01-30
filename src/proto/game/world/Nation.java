

package proto.game.world;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

import proto.common.Session;
import proto.common.Session.Saveable;
import proto.game.content.UrbanScene;
import proto.game.scene.Scene;
import proto.util.*;



public class Nation implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public Region region;
  float trust = 0.25f;
  float crime = 0.25f;
  boolean member;
  int funding;
  
  Scene mission;
  
  
  Nation(Region region) {
    this.region  = region;
    this.funding = region.defaultFunding;
    this.trust   = region.defaultTrust;
    this.crime   = region.defaultCrime;
    this.member  = region.defaultMember;
  }
  
  
  public Nation(Session s) throws Exception {
    s.cacheInstance(this);
    region  = (Region) s.loadObject();
    trust   = s.loadFloat();
    crime   = s.loadFloat();
    member  = s.loadBool();
    funding = s.loadInt();
    mission = (Scene) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(region);
    s.saveFloat(trust);
    s.saveFloat(crime);
    s.saveBool(member);
    s.saveInt(funding);
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







