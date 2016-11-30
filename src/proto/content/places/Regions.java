

package proto.content.places;
import static proto.content.places.Facilities.*;

import proto.game.world.*;



public class Regions {
  
  
  final public static RegionType
    PORT_ADAMS     = new RegionType("Port Adams"    , "region_pa"),
    BLACKGATE      = new RegionType("Blackgate"     , "region_bg"),
    GOTHAM_CENTRAL = new RegionType("Gotham Central", "region_gc"),
    OLD_GOTHAM     = new RegionType("Old Gotham"    , "region_og"),
    THE_TRICORNE   = new RegionType("The Tricorne"  , "region_tt"),
    
    DINISBURG      = new RegionType("Dinisburg"     , "region_db"),
    MILLER_BAY     = new RegionType("Miller Bay"    , "region_mb"),
    BUSINESS_DIST  = new RegionType("Business Dist.", "region_bd"),
    NOLAN_HOOK     = new RegionType("Nolan Hook"    , "region_nh"),
    THE_LEESIDE    = new RegionType("The Leeside"   , "region_tl"),
    
    ARKHAM         = new RegionType("Arkham"        , "region_ak"),
    CRIME_ALLEY    = new RegionType("Crime Alley"   , "region_ca"),
    CAPE_FINGER    = new RegionType("Cape Finger"   , "region_cf"),
    AMUSEMENT_MILE = new RegionType("Amusement Mile", "region_am"),
    NEW_GOTHAM     = new RegionType("New Gotham"    , "region_ng"),
    
    ALL_REGIONS[] = {
      PORT_ADAMS, BLACKGATE  , GOTHAM_CENTRAL, OLD_GOTHAM    , THE_TRICORNE,
      DINISBURG , MILLER_BAY , BUSINESS_DIST , NOLAN_HOOK    , THE_LEESIDE ,
      ARKHAM    , CRIME_ALLEY, CAPE_FINGER   , AMUSEMENT_MILE, NEW_GOTHAM  ,
    };
    
  
  static {
    final String IMG_DIR = "media assets/scene backgrounds/";
    
    //  Downtown-
    final RegionType PA = PORT_ADAMS;
    PA.view.attachColourKey(-14894156, "PA");
    PA.view.attachPortrait(IMG_DIR+"district_industrial.png");
    PA.attachDefaultFacilities(STEEL_MILL, UNION_OFFICE);
    
    final RegionType BG = BLACKGATE;
    BG.view.attachPortrait(IMG_DIR+"district_industrial.png");
    BG.view.attachColourKey(-12284725, "BG");
    
    final RegionType GC = GOTHAM_CENTRAL;
    GC.view.attachPortrait(IMG_DIR+"district_business.png");
    GC.view.attachColourKey(-2712    , "GC");
    GC.attachDefaultFacilities(SOUP_KITCHEN);
    
    final RegionType OG = OLD_GOTHAM;
    OG.view.attachPortrait(IMG_DIR+"district_business.png");
    OG.view.attachColourKey(-553386  , "WH");
    GC.attachDefaultFacilities(COMMUNITY_COLLEGE);
    
    final RegionType TT = THE_TRICORNE;
    TT.view.attachPortrait(IMG_DIR+"district_business.png");
    TT.view.attachColourKey(-3874917 , "TT");
    
    //  Midtown-
    final RegionType DB = DINISBURG;
    DB.view.attachPortrait(IMG_DIR+"district_park.png");
    DB.view.attachColourKey(-8205668 , "DB");
    DB.attachDefaultFacilities(CITY_PARK, ROBINS_CAMP);
    
    final RegionType MB = MILLER_BAY;
    MB.view.attachPortrait(IMG_DIR+"district_amusement.png");
    MB.view.attachColourKey(-8600202 , "MB");
    
    final RegionType BD = BUSINESS_DIST;
    BD.view.attachPortrait(IMG_DIR+"district_business.png");
    BD.view.attachColourKey(-746814  , "BD");
    BD.attachDefaultFacilities(BUSINESS_PARK);
    
    final RegionType NH = NOLAN_HOOK;
    NH.view.attachPortrait(IMG_DIR+"district_industrial.png");
    NH.view.attachColourKey(-9580297 , "NH");
    NH.attachDefaultFacilities(STEEL_MILL);
    
    final RegionType TL = THE_LEESIDE;
    TL.view.attachPortrait(IMG_DIR+"district_park.png");
    TL.view.attachColourKey(-8154166 , "TL");
    TL.attachDefaultFacilities(CITY_PARK);
    
    //  Uptown-
    final RegionType AK = ARKHAM;
    AK.view.attachPortrait(IMG_DIR+"district_industrial.png");
    AK.view.attachColourKey(-11111239, "AK");
    AK.attachDefaultFacilities(CHEMICAL_PLANT);
    
    final RegionType CA = CRIME_ALLEY;
    CA.view.attachPortrait(IMG_DIR+"district_industrial.png");
    CA.view.attachColourKey(-486371  , "CA");
    
    final RegionType CF = CAPE_FINGER;
    CF.view.attachPortrait(IMG_DIR+"district_park.png");
    CF.view.attachColourKey(-1172444 , "CF");
    CF.attachDefaultFacilities(SOUP_KITCHEN);
    
    final RegionType AM = AMUSEMENT_MILE;
    AM.view.attachPortrait(IMG_DIR+"district_amusement.png");
    AM.view.attachColourKey(-12995254, "AM");
    
    final RegionType NG = NEW_GOTHAM;
    NG.view.attachPortrait(IMG_DIR+"district_business.png");
    NG.view.attachColourKey(-3584    , "NG");
    NG.attachDefaultFacilities(TECH_STARTUP, CHEMICAL_PLANT);
  }
}
