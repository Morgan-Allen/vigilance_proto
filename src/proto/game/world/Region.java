

package proto.game.world;
import proto.common.Session;
import proto.common.Session.Saveable;
import proto.util.*;
import proto.view.*;



public class Region extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static Index <Region> INDEX = new Index <Region> ();
  
  final public String name;
  final public RegionView view = new RegionView();
  String cities[] = {};
  
  int     defaultFunding = 100  ;
  float   defaultCrime   = 0.25f;
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
    
    SA.cities = new String[] { "Brazilia", "Buenos Aires", "Atlantis"};
    SA.view.attachColourKey(-11740822, "SA");
    SA.defaultFunding = 150;
    
    AF.cities = new String[] { "Capetown", "Cairo", "Timbuktu" };
    AF.view.attachColourKey(-989079, "AF");
    AF.defaultFunding = 120;
    
    EU.cities = new String[] { "London", "Paris", "Themyscira" };
    EU.view.attachColourKey(-3956789, "EU");
    EU.defaultFunding = 180;
    
    SB.cities = new String[] { "Moscow", "Leningrad", "Berlin" };
    SB.view.attachColourKey(-3162470, "SB");
    SB.defaultFunding = 150;
    
    OC.cities = new String[] { "Perth", "Singapore", "Tokyo" };
    OC.view.attachColourKey(-9335063, "OC");
    OC.defaultFunding = 150;
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








