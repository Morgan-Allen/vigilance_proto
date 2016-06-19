

package proto.content.techs;
import proto.game.event.*;
import proto.game.world.*;

import static proto.game.world.District.*;

import proto.content.rooms.Laboratory;
import proto.content.rooms.Workshop;



public class Facilities {
  
  
  final public static Facility STEEL_MILL = new Facility(
    "Steel Mill", "facility_steel_mill",
    "media assets/tech icons/icon_steel_mill.png",
    "A Steel Mill provides income and steady blue-collar employment, along "+
    "with industrial pollution.\n\n10% bonus to Workshop projects",
    100, BUILD_TIME_MEDIUM,
    EMPLOYMENT, 3,
    HEALTH_AND_ENVIRONMENT, -2,
    INCOME, 2
  ) {
    protected float speedBonus(Task task) {
      if (! (task instanceof Crafting)) return 0;
      final Crafting craft = (Crafting) task;
      if (craft.room().blueprint == Workshop.BLUEPRINT) return 0.1f;
      return 0;
    }
  };
  
  final public static Facility CHEMICAL_PLANT = new Facility(
    "Chemical Plant", "facility_chemical_plant",
    "media assets/tech icons/icon_chemical_plant.png",
    "Chemical plants provide basic laborers with a steady job, but won't "+
    "do their health any favours.\n\n10% bonus to Laboratory projects",
    100, BUILD_TIME_MEDIUM,
    EMPLOYMENT, 3,
    HEALTH_AND_ENVIRONMENT, -2,
    INCOME, 2
  ){
    protected float speedBonus(Task task) {
      if (! (task instanceof Crafting)) return 0;
      final Crafting craft = (Crafting) task;
      if (craft.room().blueprint == Laboratory.BLUEPRINT) return 0.1f;
      return 0;
    }
  };
  
  final public static Facility UNION_OFFICE = new Facility(
    "Union Office", "facility_union_office",
    "media assets/tech icons/icon_union_office.png",
    "The Union Office bolsters relations between workers and management, "+
    "helping to guarantee labour laws.  Luddite tendencies can hold back "+
    "modernisation, however, and union dues are steep.",
    40, BUILD_TIME_MEDIUM,
    EMPLOYMENT, 2,
    EDUCATION_AND_CULTURE, -1,
    DETERRENCE, 1,
    INCOME, -1
  );
  
  final public static Facility BUSINESS_PARK = new Facility(
    "Business Park", "facility_business_park",
    "media assets/tech icons/icon_business_park.png",
    "Shopping malls and small commercial agencies can establish themselves in "+
    "a Business Park.",
    70, BUILD_TIME_MEDIUM,
    EMPLOYMENT, 1,
    INCOME, 2
  );
  
  final public static Facility TECH_STARTUP = new Facility(
    "Tech Startup", "facility_tech_startup",
    "media assets/tech icons/icon_tech_startup.png",
    "Entrepreneurs, hackers and gurus can come together under one roof to "+
    "kickstart great ideas.  If only they could make that seed funding last "+
    "longer...",
    120, BUILD_TIME_SHORT,
    EMPLOYMENT, 1,
    EDUCATION_AND_CULTURE, 1,
    INCOME, -1
  );
  
  final public static Facility CITY_PARK = new Facility(
    "City Park", "facility_city_park",
    "media assets/tech icons/icon_city_park.png",
    "Fresh air, lush lawns and open spaces help to invigorate the body and "+
    "mind.",
    50, BUILD_TIME_LONG,
    HEALTH_AND_ENVIRONMENT, 1,
    ENTERTAINMENT, 1
  );
  
  final public static Facility ROBINS_CAMP = new Facility(
    "Neighbourhood Robins Camp", "facility_robins_camp",
    "media assets/tech icons/icon_robins_camp.png",
    "Helping to give the city's troubled youth a sense of purpose and "+
    "direction, the Neighbourhood Robins teach wilderness survival, a code "+
    "of ethics and various martial arts.  Any rumours of collusion with "+
    "masked vigilantes are... greatly exxaggerated.",
    50, BUILD_TIME_LONG,
    TRUST, 1,
    DETERRENCE, 1
  );
  
  final public static Facility SOUP_KITCHEN = new Facility(
    "Soup Kitchen", "facility_soup_kitchen",
    "media assets/tech icons/icon_soup_kitchen.png",
    "A place of refuge for the city's most desperate, Soup Kitchens afford "+
    "shelter from bad weather, gnawing hunger and cruel intentions.",
    60, BUILD_TIME_SHORT,
    TRUST, 1,
    VIOLENCE, -1,
    INCOME, -1
  );
  
  final public static Facility COMMUNITY_COLLEGE = new Facility(
    "Community College", "facility_community_college",
    "media assets/tech icons/icon_community_college.png",
    "Community Colleges allow for wider enrollment in education, including "+
    "for adults or poorer families.  Left-leaning academics often take up "+
    "tenure.",
    120, BUILD_TIME_LONG,
    EDUCATION_AND_CULTURE, 2,
    TRUST, 1,
    INCOME, -1
  );
  
  //  TODO:  I'm not supporting all these at the moment.  For testing, just a 
  //  couple.  Expand later.
  
  
  
  
}












