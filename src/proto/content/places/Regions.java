

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
    
    ALL_REGIONS[] = {
      SECTOR01, SECTOR02, SECTOR03,
      SECTOR04, SECTOR05, SECTOR06,
      SECTOR07, SECTOR08, SECTOR09
    };
    
  
  static {
    final RegionType r1 = SECTOR01;
    r1.view.attachColourKey(-16734721, "01");
    r1.attachDefaultFacilities(STEEL_MILL);

    final RegionType r2 = SECTOR02;
    r2.view.attachColourKey(-3584, "02");
    r2.attachDefaultFacilities(UNION_OFFICE);

    final RegionType r3 = SECTOR03;
    r3.view.attachColourKey(-65354, "03");
    r3.attachDefaultFacilities(SOUP_KITCHEN);

    
    final RegionType r4 = SECTOR04;
    r4.view.attachColourKey(-14812949, "04");
    r4.attachDefaultFacilities(COMMUNITY_COLLEGE);

    final RegionType r5 = SECTOR05;
    r5.view.attachColourKey(-486371, "05");
    r5.attachDefaultFacilities(CITY_PARK);

    final RegionType r6 = SECTOR06;
    r6.view.attachColourKey(-2286088, "06");
    r6.attachDefaultFacilities(BUSINESS_PARK);

    
    final RegionType r7 = SECTOR07;
    r7.view.attachColourKey(-4628394, "07");
    r7.attachDefaultFacilities();
    
    final RegionType r8 = SECTOR08;
    r8.view.attachColourKey(-11111239, "08");
    r8.attachDefaultFacilities();

    final RegionType r9 = SECTOR09;
    r9.view.attachColourKey(-11093671, "09");
    r9.attachDefaultFacilities(TECH_STARTUP, CHEMICAL_PLANT);
    
  }
}



