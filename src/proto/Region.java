

package proto;
import util.*;
import view.*;



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
    NA.view.loadOutline("NA_outline.png", -38656, 136, 82);
    NA.defaultFunding = 200 ;
    NA.defaultTrust   = 0.5f;
    NA.defaultMember  = true;
    
    SA.cities = new String[] { "Brazilia", "Buenos Aires", "Atlantis"};
    SA.view.loadOutline("SA_outline.png", -11740822, 463, 337);
    SA.defaultFunding = 150;
    
    AF.cities = new String[] { "Capetown", "Cairo", "Timbuktu" };
    AF.view.loadOutline("AF_outline.png", -989079, 665, 243);
    AF.defaultFunding = 120;
    
    EU.cities = new String[] { "London", "Paris", "Themyscira" };
    EU.view.loadOutline("EU_outline.png", -3956789, 663, 96);
    EU.defaultFunding = 180;
    
    SB.cities = new String[] { "Moscow", "Leningrad", "Berlin" };
    SB.view.loadOutline("RU_outline.png", -3162470, 843, 54);
    SB.defaultFunding = 150;
    
    OC.cities = new String[] { "Perth", "Singapore", "Tokyo" };
    OC.view.loadOutline("OC_outline.png", -9335063, 926, 160);
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








