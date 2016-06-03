

package proto.game.world;
import proto.common.*;
import proto.util.*;
import proto.view.*;



//  TODO:  Rename this to the 'Geography' class to avoid confusion, and include
//  all the initialisation data in the content package.


public class Region extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static Index <Region> INDEX = new Index <Region> ();
  
  final public String name;
  final public RegionAssets view = new RegionAssets();
  
  int     maxFacilities  = 3    ;
  float   defaultTrust   = 0.00f;
  boolean defaultMember  = false;
  
  
  final static Region
    PORT_ADAMS     = new Region("Port Adams"    , "region_pa"),
    BLACKGATE      = new Region("Blackgate"     , "region_bg"),
    GOTHAM_CENTRAL = new Region("Gotham Central", "region_gc"),
    OLD_GOTHAM     = new Region("Old Gotham"    , "region_og"),
    THE_TRICORNE   = new Region("The Tricorne"  , "region_tt"),
    
    DINISBURG      = new Region("Dinisburg"     , "region_db"),
    MILLER_BAY     = new Region("Miller Bay"    , "region_mb"),
    BUSINESS_DIST  = new Region("Business Dist.", "region_bd"),
    NOLAN_HOOK     = new Region("Nolan Hook"    , "region_nh"),
    THE_LEESIDE    = new Region("The Leeside"   , "region_tl"),
    
    ARKHAM         = new Region("Arkham"        , "region_ak"),
    CRIME_ALLEY    = new Region("Crime Alley"   , "region_ca"),
    CAPE_FINGER    = new Region("Cape Finger"   , "region_cf"),
    AMUSEMENT_MILE = new Region("Amusement Mile", "region_am"),
    NEW_GOTHAM     = new Region("New Gotham"    , "region_ng"),
    
    ALL_REGIONS[] = {
      PORT_ADAMS, BLACKGATE  , GOTHAM_CENTRAL, OLD_GOTHAM    , THE_TRICORNE,
      DINISBURG , MILLER_BAY , BUSINESS_DIST , NOLAN_HOOK    , THE_LEESIDE ,
      ARKHAM    , CRIME_ALLEY, CAPE_FINGER   , AMUSEMENT_MILE, NEW_GOTHAM  ,
    };
    
  
  static {
    final String IMG_DIR = "media assets/scene backgrounds/";
    
    //  Downtown-
    final Region PA = PORT_ADAMS;
    PA.view.attachColourKey(-14894156, "PA");
    PA.view.attachPortrait(IMG_DIR+"district_industrial.png");
    
    final Region BG = BLACKGATE;
    BG.view.attachPortrait(IMG_DIR+"district_industrial.png");
    BG.view.attachColourKey(-12284725, "BG");
    
    final Region GC = GOTHAM_CENTRAL;
    GC.view.attachPortrait(IMG_DIR+"district_business.png");
    GC.view.attachColourKey(-2712    , "GC");
    
    final Region OG = OLD_GOTHAM;
    OG.view.attachPortrait(IMG_DIR+"district_business.png");
    OG.view.attachColourKey(-553386  , "WH");
    
    final Region TT = THE_TRICORNE;
    TT.view.attachPortrait(IMG_DIR+"district_business.png");
    TT.view.attachColourKey(-3874917 , "TT");
    
    //  Midtown-
    final Region DB = DINISBURG;
    DB.view.attachPortrait(IMG_DIR+"district_park.png");
    DB.view.attachColourKey(-8205668 , "DB");
    
    final Region MB = MILLER_BAY;
    MB.view.attachPortrait(IMG_DIR+"district_amusement.png");
    MB.view.attachColourKey(-8600202 , "MB");
    
    final Region BD = BUSINESS_DIST;
    BD.view.attachPortrait(IMG_DIR+"district_business.png");
    BD.view.attachColourKey(-746814  , "BD");
    
    final Region NH = NOLAN_HOOK;
    NH.view.attachPortrait(IMG_DIR+"district_industrial.png");
    NH.view.attachColourKey(-9580297 , "NH");
    
    final Region TL = THE_LEESIDE;
    TL.view.attachPortrait(IMG_DIR+"district_park.png");
    TL.view.attachColourKey(-8154166 , "TL");
    
    //  Uptown-
    final Region AK = ARKHAM;
    AK.view.attachPortrait(IMG_DIR+"district_industrial.png");
    AK.view.attachColourKey(-11111239, "AK");
    
    final Region CA = CRIME_ALLEY;
    CA.view.attachPortrait(IMG_DIR+"district_industrial.png");
    CA.view.attachColourKey(-486371  , "CA");
    
    final Region CF = CAPE_FINGER;
    CF.view.attachPortrait(IMG_DIR+"district_park.png");
    CF.view.attachColourKey(-1172444 , "CF");
    
    final Region AM = AMUSEMENT_MILE;
    AM.view.attachPortrait(IMG_DIR+"district_amusement.png");
    AM.view.attachColourKey(-12995254, "AM");
    
    final Region NG = NEW_GOTHAM;
    NG.view.attachPortrait(IMG_DIR+"district_business.png");
    NG.view.attachColourKey(-3584    , "NG");
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
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}








