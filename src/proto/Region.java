

package proto;
import util.*;
import java.awt.Image;



public class Region extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static Index <Region> INDEX = new Index <Region> ();
  
  String name;
  String cities[] = {};
  int colourKey;
  Image outline;
  int outlineX, outlineY, centerX, centerY;
  
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
    NA.name = "North America";
    NA.cities = new String[] { "Toronto", "Metropolis", "Gotham" };
    NA.colourKey = -38656;
    NA.loadOutline("NA_outline.png", 136, 82);
    NA.defaultFunding = 200 ;
    NA.defaultTrust   = 0.5f;
    NA.defaultMember  = true;
    
    SA.name = "South America";
    SA.cities = new String[] { "Brazilia", "Buenos Aires", "Atlantis"};
    SA.colourKey = -11740822;
    SA.loadOutline("SA_outline.png", 463, 337);
    SA.defaultFunding = 150;
    
    AF.name = "Africa";
    AF.cities = new String[] { "Capetown", "Cairo", "Timbuktu" };
    AF.colourKey = -989079;
    AF.loadOutline("AF_outline.png", 665, 243);
    AF.defaultFunding = 120;
    
    EU.name = "Europe";
    EU.cities = new String[] { "London", "Paris", "Themyscira" };
    EU.colourKey = -3956789;
    EU.loadOutline("EU_outline.png", 663, 96);
    EU.defaultFunding = 180;
    
    SB.name = "Soviet Bloc";
    SB.cities = new String[] { "Moscow", "Leningrad", "Berlin" };
    SB.colourKey = -3162470;
    SB.loadOutline("RU_outline.png", 843, 54);
    SB.defaultFunding = 150;
    
    OC.name = "Oceania";
    OC.cities = new String[] { "Perth", "Singapore", "Tokyo" };
    OC.colourKey = -9335063;
    OC.loadOutline("OC_outline.png", 926, 160);
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
  
  
  void loadOutline(String imgFile, int offX, int offY) {
    outline = Kind.loadImage(World.IMG_DIR+imgFile);
    outlineX = offX;
    outlineY = offY;
    if (outline != null) {
      centerX = offX + (outline.getWidth (null) / 2);
      centerY = offY + (outline.getHeight(null) / 2);
    }
  }
  
}








