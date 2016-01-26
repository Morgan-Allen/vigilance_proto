

package proto;
import util.*;
import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;



public class Nation implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Region region;
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
  
  
  
  /**  Life cycle and update methods-
    */
  Scene generateCrisis(World world) {
    final Scene s = new UrbanScene(world, 100);
    s.name = "Hostage situation in "+Rand.pickFrom(region.cities);
    s.expireTime = world.currentTime + 1 + Rand.index(3);
    s.site = this;
    
    if (Rand.num() < world.base.sensorChance()) {
      s.dangerLevel = 0.75f * (Rand.avgNums(2) + 1.5f) / 2;
    }
    else {
      s.dangerLevel = 1.25f * (Rand.avgNums(2) + 1.5f) / 2;
    }
    return s;
  }
  
  
  
  /**  Rendering and debug methods-
    */
  public String toString() {
    return region.name;
  }
}







