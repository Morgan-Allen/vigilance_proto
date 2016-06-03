

package proto.content.techs;
import proto.game.world.*;
import static proto.game.world.District.*;



public class Facilities {
  
  
  final public static Facility STEEL_MILL = new Facility(
    "Steel Mill", "facility_steel_mill",
    "media assets/tech icons/icon_steel_mill.png",
    JOBS_AND_SERVICES, 3,
    HEALTH_AND_ENVIRONMENT, -2,
    INCOME, 2
  );  //  TODO:  Reduce cost of workshop projects!
  
  final public static Facility CHEMICAL_PLANT = new Facility(
    "Chemical Plant", "facility_chemical_plant",
    "media assets/tech icons/icon_chemical_plant.png",
    JOBS_AND_SERVICES, 3,
    HEALTH_AND_ENVIRONMENT, -2,
    INCOME, 2
  );  //  TODO:  Reduce cost of laboratory projects!
  
  final public static Facility UNION_OFFICE = new Facility(
    "Union Office", "facility_union_office",
    "media assets/tech icons/icon_union_office.png",
    JOBS_AND_SERVICES, 2,
    EDUCATION_AND_CULTURE, -1,
    DETERRENCE, 1,
    INCOME, -1
  );
  
  final public static Facility BUSINESS_PARK = new Facility(
    "Business Park", "facility_business_park",
    "media assets/tech icons/icon_business_park.png",
    JOBS_AND_SERVICES, 1,
    INCOME, 2
  );
  
  final public static Facility TECH_STARTUP = new Facility(
    "Tech Startup", "facility_tech_startup",
    "media assets/tech icons/icon_steel_mill.png",
    JOBS_AND_SERVICES, 1,
    EDUCATION_AND_CULTURE, 1,
    INCOME, -1
  );
  
  final public static Facility CITY_PARK = new Facility(
    "City Park", "facility_city_park",
    "media assets/tech icons/icon_city_park.png",
    HEALTH_AND_ENVIRONMENT, 1,
    ENTERTAINMENT, 1
  );
  
  final public static Facility ROBINS_CAMP = new Facility(
    "Robins Camp", "facility_robins_camp",
    "media assets/tech icons/icon_robins_camp.png",
    TRUST, 1,
    DETERRENCE, 1
  );
  
  final public static Facility SOUP_KITCHEN = new Facility(
    "Soup Kitchen", "facility_soup_kitchen",
    "media assets/tech icons/icon_soup_kitchen.png",
    TRUST, 1,
    VIOLENCE, -1,
    INCOME, -1
  );
  
  final public static Facility COMMUNITY_COLLEGE = new Facility(
    "Community College", "facility_community_college",
    "media assets/tech icons/icon_community_college.png",
    EDUCATION_AND_CULTURE, 2,
    TRUST, 1,
    INCOME, -1
  );
  
  
  //  TODO:  I'm not supporting all these at the moment.  For testing, just a 
  //  couple.  Expand later.
  
  
  
  
}












