

package proto.game.world;
import proto.common.*;
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
  }
  
  
  /**  General query methods-
    */
  public float   trustLevel() { return trust  ; }
  public float   crimeLevel() { return crime  ; }
  public int     funding   () { return funding; }
  public boolean member    () { return member ; }
  
  
  
  /**  Rendering and debug methods-
    */
  public String toString() {
    return region.name;
  }
}







