

package proto.content.places;
import proto.game.person.*;
import proto.game.event.*;
import proto.game.world.*;
import proto.content.agents.Civilians;
import proto.content.rooms.*;
import static proto.game.world.Region.*;
import static proto.content.places.UrbanScenes.*;



public class Facilities {
  
  
  final public static Trait REINFORCED = new Trait(
    "Reinforced", "trait_reinforced", null, ""
  );
  final public static Trait ALARMED = new Trait(
    "Alarmed", "trait_alarmed", null, ""
  );
  
  
  final public static PlaceType WAYNE_MANOR = new PlaceType(
    "Wayne Manor", "base_type_manor",
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
    100, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 3,
    HEALTH_AND_ENVIRONMENT, -2,
    INCOME, 2,
    Civilians.CIVILIAN, 2
  ) {
    public float speedBonus(Task task) {
      if (task.needsSkill(PersonStats.ENGINEERING)) return 0.1f;
      return 0;
    }
  };
  
  final public static PlaceType CHEMICAL_PLANT = new PlaceType(
    "Chemical Plant", "facility_chemical_plant",
    "media assets/tech icons/icon_chemical_plant.png",
    "Chemical plants provide basic laborers with a steady job, but won't "+
    "do their health any favours.\n\n10% bonus to Medical projects",
    100, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 3,
    HEALTH_AND_ENVIRONMENT, -2,
    INCOME, 2,
    Civilians.CIVILIAN, 2
  ){
    public float speedBonus(Task task) {
      if (task.needsSkill(PersonStats.MEDICINE)) return 0.1f;
      return 0;
    }
  };
  
  final public static PlaceType UNION_OFFICE = new PlaceType(
    "Union Office", "facility_union_office",
    "media assets/tech icons/icon_union_office.png",
    "The Union Office bolsters relations between workers and management, "+
    "helping to guarantee labour laws.  Luddite tendencies can hold back "+
    "modernisation, however, and union dues are steep.",
    40, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 2,
    EDUCATION_AND_CULTURE, -1,
    DETERRENCE, 1,
    INCOME, -1,
    Civilians.CIVILIAN, 1
  );
  
  final public static PlaceType BUSINESS_PARK = new PlaceType(
    "Business Park", "facility_business_park",
    "media assets/tech icons/icon_business_park.png",
    "Shopping malls and small commercial agencies can establish themselves in "+
    "a Business Park.",
    70, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 1,
    INCOME, 2,
    Civilians.BROKER, 1
  );
  
  final public static PlaceType TECH_STARTUP = new PlaceType(
    "Tech Startup", "facility_tech_startup",
    "media assets/tech icons/icon_tech_startup.png",
    "Entrepreneurs, hackers and gurus can come together under one roof to "+
    "kickstart great ideas.  If only they could make that seed funding last "+
    "longer...",
    120, BUILD_TIME_SHORT, URBAN_SCENE,
    EMPLOYMENT, 1,
    EDUCATION_AND_CULTURE, 1,
    INCOME, -1,
    Civilians.INVENTOR, 1
  );
  
  final public static PlaceType CITY_PARK = new PlaceType(
    "City Park", "facility_city_park",
    "media assets/tech icons/icon_city_park.png",
    "Fresh air, lush lawns and open spaces help to invigorate the body and "+
    "mind.",
    50, BUILD_TIME_LONG, URBAN_SCENE,
    HEALTH_AND_ENVIRONMENT, 1,
    ENTERTAINMENT, 1
  );
  
  final public static PlaceType SOUP_KITCHEN = new PlaceType(
    "Soup Kitchen", "facility_soup_kitchen",
    "media assets/tech icons/icon_soup_kitchen.png",
    "A place of refuge for the city's most desperate, Soup Kitchens afford "+
    "shelter from bad weather, gnawing hunger and cruel intentions.",
    60, BUILD_TIME_SHORT, URBAN_SCENE,
    TRUST, 1,
    VIOLENCE, -1,
    INCOME, -1
  );
  
  final public static PlaceType COMMUNITY_COLLEGE = new PlaceType(
    "Community College", "facility_community_college",
    "media assets/tech icons/icon_community_college.png",
    "Community Colleges allow for wider enrollment in education, including "+
    "for adults or poorer families.  Left-leaning academics often take up "+
    "tenure.",
    120, BUILD_TIME_LONG, URBAN_SCENE,
    EDUCATION_AND_CULTURE, 2,
    TRUST, 1,
    INCOME, -1,
    Civilians.DOCTOR, 1
  );
  
  //  TODO:  I'm not supporting all these at the moment.  For testing, just a 
  //  couple.  Expand later.
  
  
  
  
}












