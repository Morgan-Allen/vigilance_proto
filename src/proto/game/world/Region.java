

package proto.game.world;
import proto.common.*;
import proto.util.*;
import proto.view.*;



public class Region extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static Index <Region> INDEX = new Index <Region> ();
  
  final public String name;
  final public RegionView view = new RegionView();
  String cities[] = {};
  
  float 
    defaultCrime       = 0.25f,
    defaultWealth      = 0.50f,
    defaultEnvironment = 0.75f,
    defaultEducation   = 0.50f,
    defaultEquality    = 0.50f,
    defaultFreedom     = 0.50f;
  
  int     defaultFunding = 100  ;
  float   defaultTrust   = 0.25f;
  boolean defaultMember  = false;
  
  
  final static Region
    NA = new Region("North America", "region_na"),
    SA = new Region("South America", "region_sa"),
    AF = new Region("Africa"       , "region_af"),
    EU = new Region("Europe"       , "region_eu"),
    SB = new Region("Soviet Bloc"  , "region_sb"),
    OC = new Region("Oceania"      , "region_oc"),
    
    ALL_REGIONS[] = { NA, SA, AF, EU, SB, OC };
  
  static {
    NA.cities = new String[] { "Toronto", "Metropolis", "Gotham" };
    NA.view.attachColourKey(-38656, "NA");
    NA.defaultFunding = 200 ;
    NA.defaultTrust   = 0.5f;
    NA.defaultMember  = true;
    
    NA.defaultCrime       = 0.3f;
    NA.defaultWealth      = 0.8f;
    NA.defaultEnvironment = 0.5f;
    NA.defaultEducation   = 0.6f;
    NA.defaultEquality    = 0.7f;
    NA.defaultFreedom     = 0.8f;
    
    SA.cities = new String[] { "Brazilia", "Buenos Aires", "Atlantis"};
    SA.view.attachColourKey(-11740822, "SA");
    SA.defaultFunding = 150  ;
    SA.defaultTrust   = 0.25f;
    SA.defaultMember  = false;
    
    SA.defaultCrime       = 0.3f;
    SA.defaultWealth      = 0.5f;
    SA.defaultEnvironment = 0.9f;
    SA.defaultEducation   = 0.4f;
    SA.defaultEquality    = 0.5f;
    SA.defaultFreedom     = 0.8f;
    
    AF.cities = new String[] { "Capetown", "Cairo", "Timbuktu" };
    AF.view.attachColourKey(-989079, "AF");
    AF.defaultFunding = 120  ;
    AF.defaultTrust   = 0.25f;
    AF.defaultMember  = false;
    
    AF.defaultCrime       = 0.5f;
    AF.defaultWealth      = 0.4f;
    AF.defaultEnvironment = 0.8f;
    AF.defaultEducation   = 0.3f;
    AF.defaultEquality    = 0.4f;
    AF.defaultFreedom     = 0.6f;
    
    EU.cities = new String[] { "London", "Paris", "Themyscira" };
    EU.view.attachColourKey(-3956789, "EU");
    EU.defaultFunding = 180  ;
    EU.defaultTrust   = 0.25f;
    EU.defaultMember  = false;
    
    EU.defaultCrime       = 0.2f;
    EU.defaultWealth      = 0.7f;
    EU.defaultEnvironment = 0.6f;
    EU.defaultEducation   = 0.7f;
    EU.defaultEquality    = 0.8f;
    EU.defaultFreedom     = 0.7f;
    
    SB.cities = new String[] { "Moscow", "Leningrad", "Berlin" };
    SB.view.attachColourKey(-3162470, "SB");
    SB.defaultFunding = 150  ;
    SB.defaultTrust   = 0.25f;
    SB.defaultMember  = false;
    
    SB.defaultCrime       = 0.1f;
    SB.defaultWealth      = 0.5f;
    SB.defaultEnvironment = 0.4f;
    SB.defaultEducation   = 0.7f;
    SB.defaultEquality    = 0.9f;
    SB.defaultFreedom     = 0.3f;
    
    OC.cities = new String[] { "Perth", "Singapore", "Tokyo" };
    OC.view.attachColourKey(-9335063, "OC");
    OC.defaultFunding = 150  ;
    OC.defaultTrust   = 0.25f;
    OC.defaultMember  = false;
    
    OC.defaultCrime       = 0.4f;
    OC.defaultWealth      = 0.6f;
    OC.defaultEnvironment = 0.5f;
    OC.defaultEducation   = 0.6f;
    OC.defaultEquality    = 0.8f;
    OC.defaultFreedom     = 0.6f;
  }
  
  
  Region(String name, String ID) {
    super(INDEX, ID);
    this.name = name;
  }
  
  
  public static Region loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
}








