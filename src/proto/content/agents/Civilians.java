

package proto.content.agents;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import static proto.content.places.UrbanScenes.*;
import static proto.game.person.PersonStats.*;
import static proto.game.world.Region.*;



public class Civilians {
  
  
  final static String IMG_DIR = "media assets/character icons/common/";
  
  final public static Faction THE_CITY_COUNCIL = new Faction(
    "The City Council", "faction_the_city_council", false
  );
  
  
  final public static String
    COMMON_FIRST_NAMES[] = {
      "Jerry", "Stan", "Louis", "Abed", "Nico", "Zoe"
    },
    COMMON_LAST_NAMES[] = {
      "Stanfeld", "Turner", "Lewis", "Walker", "Bryant", "Cole"
    },
    COMMON_NAMES[][] = { COMMON_FIRST_NAMES, COMMON_LAST_NAMES };
  
  final public static PersonType
    CIVILIAN = new PersonType(
      "Civilian", "person_kind_civilian", IMG_DIR+"icon_civilian.png",
      "", COMMON_NAMES, null,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 2 ,
      REFLEXES, 2 ,
      WILL    , 2 ,
      MUSCLE  , 2
    ),
    WORKER = new PersonType(
      "Worker", "person_kind_worker", IMG_DIR+"icon_worker.png",
      "", COMMON_NAMES, null,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 2 ,
      REFLEXES, 2 ,
      WILL    , 3 ,
      MUSCLE  , 6 ,
      
      ENGINEERING, 3
    ),
    DOCTOR = new PersonType(
      "Doctor", "person_kind_doctor", IMG_DIR+"icon_doctor.png",
      "", COMMON_NAMES, null,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 7 ,
      REFLEXES, 3 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      MEDICINE, 7,
      QUESTION, 2
    ),
    INVENTOR = new PersonType(
      "Inventor", "person_kind_inventor", IMG_DIR+"icon_inventor.png",
      "", COMMON_NAMES, null,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 7 ,
      REFLEXES, 3 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      ENGINEERING, 7,
      INVESTMENT, 2
    ),
    BROKER = new PersonType(
      "Broker", "person_kind_broker", IMG_DIR+"icon_broker.png",
      "", Civilians.COMMON_NAMES, null,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 6 ,
      REFLEXES, 3 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      PERSUADE, 7,
      INVESTMENT, 4
    ),
    POLICE = new PersonType(
      "Police", "person_kind_police", IMG_DIR+"icon_police.png",
      "", Civilians.COMMON_NAMES, null,
      Kind.SUBTYPE_CIVILIAN,
      BRAINS  , 5 ,
      REFLEXES, 6 ,
      WILL    , 5 ,
      MUSCLE  , 6 ,
      
      PERSUADE, 3,
      QUESTION, 6,
      MEDICINE, 2
    ),
    OFFICIAL = new PersonType(
      "Official", "person_kind_official", IMG_DIR+"icon_official.png",
      "", Civilians.COMMON_NAMES, null,
      Kind.SUBTYPE_CIVILIAN,
      
      BRAINS  , 6 ,
      REFLEXES, 2 ,
      WILL    , 5 ,
      MUSCLE  , 2 ,
      
      PERSUADE  , 5,
      QUESTION  , 5,
      INVESTMENT, 4
    ),
    GENTRY = new PersonType(
      "Gentry", "person_kind_gentry", IMG_DIR+"icon_gentry.png",
      "", Civilians.COMMON_NAMES, null,
      Kind.SUBTYPE_CIVILIAN,
      
      BRAINS  , 5 ,
      REFLEXES, 2 ,
      WILL    , 4 ,
      MUSCLE  , 3 ,
      
      PERSUADE  , 3,
      QUESTION  , 3,
      INVESTMENT, 7
    ),
    CIVIC_TYPES[] = { POLICE, OFFICIAL, GENTRY }
  ;
  
  
  final public static PlaceType WAREHOUSE = new PlaceType(
    "Warehouse", "facility_warehouse",
    "media assets/tech icons/icon_warehouse.png",
    "",
    0, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 0,
    INCOME, 50,
    Civilians.WORKER, 0,
    Common.VENUE_INDUSTRIAL,
    Common.VENUE_SLUM
  );
  
  final public static PlaceType CITY_HALL = new PlaceType(
    "City Hall", "facility_city_hall",
    "media assets/tech icons/icon_city_hall.png",
    "",
    0, BUILD_TIME_LONG, URBAN_SCENE,
    EMPLOYMENT, 2,
    EDUCATION, 1,
    DETERRENCE, 5,
    INCOME, 0,
    Civilians.OFFICIAL, 3,
    Common.VENUE_OFFICE,
    Common.VENUE_CIVIC,
    Common.VENUE_SECURITY,
    Common.VENUE_IS_HQ
  );
  
  final public static PlaceType PENTHOUSE = new PlaceType(
    "Penthouse", "facility_penthouse",
    "media assets/tech icons/icon_penthouse.png",
    "",
    0, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 1,
    DIVERSION, 1,
    INCOME, 100,
    Civilians.GENTRY, 2,
    Common.VENUE_RITZY,
    Common.VENUE_DOMESTIC
  );
  
  final public static PlaceType CITY_JAIL = new PlaceType(
    "City Jail", "facility_city_jail",
    "media assets/tech icons/icon_city_jail.png",
    "",
    0, BUILD_TIME_MEDIUM, URBAN_SCENE,
    EMPLOYMENT, 1,
    INCOME, -100,
    Common.VENUE_SECURITY,
    Common.VENUE_INDUSTRIAL
  );
}


