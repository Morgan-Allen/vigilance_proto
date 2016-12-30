

package proto.content.places;
import static proto.content.places.Facilities.*;

import proto.game.world.*;



public class Regions {
  
  
  final public static RegionType
    
    SECTOR01 = new RegionType("Sector 01", "region_01"),
    SECTOR02 = new RegionType("Sector 02", "region_02"),
    SECTOR03 = new RegionType("Sector 03", "region_03"),
    SECTOR04 = new RegionType("Sector 04", "region_04"),
    SECTOR05 = new RegionType("Sector 05", "region_05"),
    SECTOR06 = new RegionType("Sector 06", "region_06"),
    SECTOR07 = new RegionType("Sector 07", "region_07"),
    SECTOR08 = new RegionType("Sector 08", "region_08"),
    SECTOR09 = new RegionType("Sector 09", "region_09"),
    /*
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
    //*/
    
    ALL_REGIONS[] = {
      SECTOR01, SECTOR02, SECTOR03,
      SECTOR04, SECTOR05, SECTOR06,
      SECTOR07, SECTOR08, SECTOR09
    };
    
  
  static {
    final String IMG_DIR = "media assets/scene backgrounds/";
    
    final RegionType r1 = SECTOR01;
    r1.view.attachColourKey(-16734721, "01");
    r1.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r1.attachDefaultFacilities(STEEL_MILL);

    final RegionType r2 = SECTOR02;
    r2.view.attachColourKey(-3584, "02");
    r2.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r2.attachDefaultFacilities(UNION_OFFICE);

    final RegionType r3 = SECTOR03;
    r3.view.attachColourKey(-65354, "03");
    r3.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r3.attachDefaultFacilities(SOUP_KITCHEN);

    
    final RegionType r4 = SECTOR04;
    r4.view.attachColourKey(-14812949, "04");
    r4.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r4.attachDefaultFacilities(COMMUNITY_COLLEGE);

    final RegionType r5 = SECTOR05;
    r5.view.attachColourKey(-486371, "05");
    r5.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r5.attachDefaultFacilities(CITY_PARK);

    final RegionType r6 = SECTOR06;
    r6.view.attachColourKey(-2286088, "06");
    r6.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r6.attachDefaultFacilities(BUSINESS_PARK);

    
    final RegionType r7 = SECTOR07;
    r7.view.attachColourKey(-4628394, "07");
    r7.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r7.attachDefaultFacilities();
    
    final RegionType r8 = SECTOR08;
    r8.view.attachColourKey(-11111239, "08");
    r8.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r8.attachDefaultFacilities();

    final RegionType r9 = SECTOR09;
    r9.view.attachColourKey(-11093671, "09");
    r9.view.attachPortrait(IMG_DIR+"district_industrial.png");
    r9.attachDefaultFacilities(TECH_STARTUP, CHEMICAL_PLANT);
    
  }
}



