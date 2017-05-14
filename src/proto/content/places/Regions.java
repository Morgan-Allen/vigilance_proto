

package proto.content.places;
import static proto.content.places.Facilities.*;
import proto.content.agents.*;
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
    //  TODO:  It won't work to have Bases as a subtype of Place.  Villains
    //  need to be able to switch hideouts, for a start.
    
    Faction heroes  = Heroes.JANUS_INDUSTRIES;
    Faction city    = Civilians.THE_CITY_COUNCIL;
    Faction crooks1 = Crooks.THE_MADE_MEN;
    
    
    final RegionType r1 = SECTOR01;
    r1.view.attachColourKey(-16734721, "01");
    r1.attachFacilities(heroes, STEEL_MILL);
    r1.attachMapCoordinates(0, 0);

    final RegionType r2 = SECTOR02;
    r2.view.attachColourKey(-3584, "02");
    r2.attachFacilities(heroes, UNION_OFFICE);
    r2.attachMapCoordinates(1, 0);
    
    final RegionType r3 = SECTOR03;
    r3.view.attachColourKey(-65354, "03");
    r3.attachFacilities(heroes, SOUP_KITCHEN, MANOR);
    r3.attachMapCoordinates(2, 0);
    
    
    final RegionType r4 = SECTOR04;
    r4.view.attachColourKey(-14812949, "04");
    r4.attachFacilities(city, COMMUNITY_COLLEGE);
    r4.attachFacilities(crooks1, LOUNGE);
    r4.attachMapCoordinates(0, 1);
    
    final RegionType r5 = SECTOR05;
    r5.view.attachColourKey(-486371, "05");
    r5.attachFacilities(city, CITY_PARK, Civilians.CITY_HALL);
    r5.attachMapCoordinates(1, 1);
    
    final RegionType r6 = SECTOR06;
    r6.view.attachColourKey(-2286088, "06");
    r6.attachFacilities(heroes, BUSINESS_PARK);
    r6.attachMapCoordinates(2, 1);
    
    
    final RegionType r7 = SECTOR07;
    r7.view.attachColourKey(-4628394, "07");
    r7.attachFacilities(city, Civilians.PENTHOUSE);
    r7.attachMapCoordinates(0, 2);
    
    final RegionType r8 = SECTOR08;
    r8.view.attachColourKey(-11111239, "08");
    r8.attachFacilities(heroes, TECH_STARTUP);
    r8.attachMapCoordinates(1, 2);
    
    final RegionType r9 = SECTOR09;
    r9.view.attachColourKey(-11093671, "09");
    r9.attachFacilities(heroes, CHEMICAL_PLANT);
    r9.attachFacilities(city, Civilians.CITY_JAIL);
    r9.attachMapCoordinates(2, 2);
    
  }
}



