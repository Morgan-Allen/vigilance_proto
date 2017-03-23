

package proto.content.places;
import proto.game.person.*;
import proto.game.event.*;
import proto.game.world.*;
import proto.content.agents.*;
import static proto.game.world.Region.*;
import static proto.content.places.UrbanScenes.*;



public class Facilities {
  
  
  final public static PlaceType MANOR = new PlaceType(
    "Manor", "base_type_manor",
    null, "",
    0, BUILD_TIME_NONE, MANSION_SCENE
  );
  
  final public static PlaceType HIDEOUT = new PlaceType(
    "Hideout", "base_type_hideout",
    null, "",
    0, BUILD_TIME_NONE, URBAN_SCENE
  );
  
  
  
  final public static PlaceType STEEL_MILL = new PlaceType(
    "Steel Mill", "facility_steel_mill",
    "media assets/tech icons/icon_steel_mill.png",
    "A Steel Mill provides income and steady blue-collar employment, along "+
    "with industrial pollution.\n\n10% bonus to Engineering projects",
    1000, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 3,
    HEALTH, -2,
    INCOME, 200,
    Civilians.WORKER, 2,
    Common.VENUE_INDUSTRIAL,
    Common.VENUE_SLUM
  ) {
    public float speedBonus(Attempt attempt) {
      if (! (attempt.source() instanceof TaskCraft)) return 0;
      if (attempt.needsSkill(PersonStats.ENGINEERING)) return 0.1f;
      return 0;
    }
  };
  
  final public static PlaceType CHEMICAL_PLANT = new PlaceType(
    "Chemical Plant", "facility_chemical_plant",
    "media assets/tech icons/icon_chemical_plant.png",
    "Chemical plants provide basic laborers with a steady job, but won't "+
    "do their health any favours.\n\n10% bonus to Medical projects",
    1000, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 3,
    HEALTH, -2,
    INCOME, 200,
    Civilians.WORKER, 2,
    Common.VENUE_INDUSTRIAL,
    Common.VENUE_SLUM
  ){
    public float speedBonus(Attempt attempt) {
      if (! (attempt.source() instanceof TaskCraft)) return 0;
      if (attempt.needsSkill(PersonStats.MEDICINE)) return 0.1f;
      return 0;
    }
  };
  
  final public static PlaceType UNION_OFFICE = new PlaceType(
    "Union Office", "facility_union_office",
    "media assets/tech icons/icon_union_office.png",
    "The Union Office bolsters relations between workers and management, "+
    "helping to guarantee labour laws.  Luddite tendencies can hold back "+
    "modernisation, however, and union dues are steep.",
    400, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 2,
    EDUCATION, -1,
    DETERRENCE, 10,
    INCOME, -100,
    Civilians.WORKER, 1,
    Common.VENUE_OFFICE,
    Common.VENUE_INDUSTRIAL
  );
  
  final public static PlaceType BUSINESS_PARK = new PlaceType(
    "Business Park", "facility_business_park",
    "media assets/tech icons/icon_business_park.png",
    "Shopping malls and small commercial agencies can establish themselves in "+
    "a Business Park.",
    750, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 1,
    INCOME, 200,
    Civilians.BROKER, 1,
    Common.VENUE_COMMERCIAL,
    Common.VENUE_INDUSTRIAL
  );
  
  final public static PlaceType TECH_STARTUP = new PlaceType(
    "Tech Startup", "facility_tech_startup",
    "media assets/tech icons/icon_tech_startup.png",
    "Entrepreneurs, hackers and gurus can come together under one roof to "+
    "kickstart great ideas.  If only they could make that seed funding last "+
    "longer...",
    1200, BUILD_TIME_SHORT, URBAN_SCENE,
    EMPLOYMENT, 1,
    EDUCATION, 1,
    INCOME, -100,
    Civilians.INVENTOR, 1,
    Common.VENUE_OFFICE,
    Common.VENUE_SCIENTIFIC
  );
  
  final public static PlaceType CITY_PARK = new PlaceType(
    "City Park", "facility_city_park",
    "media assets/tech icons/icon_city_park.png",
    "Fresh air, lush lawns and open spaces help to invigorate the body and "+
    "mind.",
    500, BUILD_TIME_LONG, URBAN_SCENE,
    HEALTH, 1,
    DIVERSION, 1,
    Common.VENUE_RURAL, Common.VENUE_CIVIC
  );
  
  final public static PlaceType SOUP_KITCHEN = new PlaceType(
    "Soup Kitchen", "facility_soup_kitchen",
    "media assets/tech icons/icon_soup_kitchen.png",
    "A place of refuge for the city's most desperate, Soup Kitchens afford "+
    "shelter from bad weather, gnawing hunger and cruel intentions.",
    600, BUILD_TIME_SHORT, URBAN_SCENE,
    TRUST, 10,
    VIOLENCE, -10,
    INCOME, -100,
    Common.VENUE_DOMESTIC, Common.VENUE_MEDICAL
  );
  
  final public static PlaceType COMMUNITY_COLLEGE = new PlaceType(
    "Community College", "facility_community_college",
    "media assets/tech icons/icon_community_college.png",
    "Community Colleges allow for wider enrollment in education, including "+
    "for adults or poorer families.  Left-leaning academics often take up "+
    "tenure.",
    1200, BUILD_TIME_LONG, URBAN_SCENE,
    EDUCATION, 2,
    TRUST, 10,
    INCOME, -100,
    Civilians.DOCTOR, 1,
    Common.VENUE_ACADEMIC, Common.VENUE_OFFICE
  );
  
  
}




