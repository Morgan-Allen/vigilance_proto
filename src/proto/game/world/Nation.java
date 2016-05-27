

package proto.game.world;
import proto.common.*;
import proto.game.scene.Event;
import proto.util.*;



public class Nation implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final World world;
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
  
  
  Nation(Region region, World world) {
    this.world  = world ;
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
    world  = (World ) s.loadObject();
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
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(world );
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
  }
  
  
  /**  General query methods-
    */
  public float   trustLevel() { return trust  ; }
  public float   crimeLevel() { return crime  ; }
  public int     funding   () { return funding; }
  public boolean member    () { return member ; }
  
  
  public void incCrime(int percent) {
    final float oldVal = crime;
    crime = Nums.clamp(crime + (percent / 100f), 0, 1);
    
    if (oldVal != crime) {
      String desc = "Crime";
      if (percent >= 0) desc += "+"+percent;
      else              desc += "-"+(0 - percent);
      world.events.log(desc+": "+region, Event.EVENT_MAJOR);
    }
  }
  
  
  public void incTrust(int percent) {
    final float oldVal = trust;
    trust = Nums.clamp(trust + (percent / 100f), 0, 1);
    
    if (oldVal != trust) {
      String desc = "Trust";
      if (percent >= 0) desc += "+"+percent;
      else              desc += "-"+(0 - percent);
      world.events.log(desc+": "+region, Event.EVENT_MAJOR);
    }
  }
  
  
  
  /**  Rendering and debug methods-
    */
  public String toString() {
    return region.name;
  }
}







