

package proto.game.world;
import java.awt.Image;

import proto.common.Kind;
import proto.common.Session;
import proto.common.Session.Saveable;
import proto.game.person.Person;
import proto.util.*;


//  TODO:  Introduce facility types!
//  Infirmary.      (Allows 2 league members to recover injury faster.)
//  Training room.  (Allows 2 league members to gain XP outside missions.)
//  Rec hall.       (Allows 2 league members to recover stress faster.)
//  Sensor array.   (Increases chance of detecting crises & ground intel.)
//  Generator.      (Increases no. of facilities that can be installed.)

//  Cybernetic Lab
//  Biology Lab
//  Psych Lab
//  Library



public class Blueprint extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <Blueprint> INDEX = new Index <Blueprint> ();
  
  final public static String IMG_DIR = "media assets/base view/";
  
  final public String name, description;
  final public Image sprite;
  
  int buildCost   = 0;
  int buildTime   = 0;
  int maintenance = 0;
  int powerCost   = 0;
  int lifeSupport = 0;
  int visitLimit  = 2;
  
  int studyBonus  = 0;
  int sensorBonus = 0;
  
  
  
  final public static Blueprint
    GENERATOR = new Blueprint(
      "Generator", "blueprint_generator",
      "Provides power to the station, allowing you to support more facilities.",
      IMG_DIR+"room_generator.png"
    ) {
      void initStats() {
        this.buildCost   = 1200;
        this.buildTime   = 5;
        this.maintenance = 35;
        this.powerCost   = -20;
        this.visitLimit  = 0;
      }
      
      void affectVisitor(Person visiting, Base base) {
        return;
      }
    },
    SENSOR_STATION = new Blueprint(
      "Sensor Station", "blueprint_sensor_station",
      "Improves the likelihood of detecting crises early and ground intel on "+
      "enemy forces by 20%.",
      IMG_DIR+"room_sensor_station.png"
    ) {
      void initStats() {
        this.buildCost   = 1000;
        this.buildTime   = 5;
        this.maintenance = 30;
        this.powerCost   = 4;
        this.sensorBonus = 20;
      }
      
      void affectVisitor(Person visiting, Base base) {
        return;
      }
    },
    LABORATORY = new Blueprint(
      "Laboratory", "blueprint_laboratory",
      "Allows heroes stationed here to research technology or crack clues "+
      "more quickly.",
      IMG_DIR+"room_laboratory.png"
    ) {
      void initStats() {
        this.buildCost   = 900;
        this.buildTime   = 6;
        this.maintenance = 50;
        this.powerCost   = 6;
        this.studyBonus  = 2;
      }
      
      void affectVisitor(Person visiting, Base base) {
        return;
      }
    },
    INFIRMARY = new Blueprint(
      "Infirmary", "blueprint_infirmary",
      "Allows heroes stationed here to recover from injury twice as fast.",
      IMG_DIR+"room_infirmary.png"
    ) {
      void initStats() {
        this.buildCost   = 800;
        this.buildTime   = 4;
        this.maintenance = 45;
        this.powerCost   = 4;
      }
      
      void affectVisitor(Person visiting, Base base) {
        float regen = visiting.maxHealth() * 1f / Person.FULL_HEAL_WEEKS;
        visiting.liftInjury((int) regen);
        return;
      }
    },
    ARBORETUM = new Blueprint(
      "Arboretum", "blueprint_arboretum",
      "Improves station life-support, allowing extra recruits and providing a "+
      "+4 bonus to stress recovery.",
      IMG_DIR+"room_arboretum.png"
    ) {
      void initStats() {
        this.buildCost   = 600;
        this.buildTime   = 6;
        this.maintenance = 15;
        this.powerCost   = 2;
        this.lifeSupport = 6;
      }
      
      void affectVisitor(Person visiting, Base base) {
        int relief = Person.WEEK_STRESS_DECAY * 2;
        visiting.liftStress(relief);
        return;
      }
    },
    TRAINING_ROOM = new Blueprint(
      "Training Room", "blueprint_training_room",
      "Allows visiting heroes to sharpen their skills between crises.",
      IMG_DIR+"room_training_room.png"
    ) {
      void initStats() {
        this.buildCost   = 750;
        this.buildTime   = 4;
        this.maintenance = 20;
        this.powerCost   = 4;
      }
      
      void affectVisitor(Person visiting, Base base) {
        visiting.stats.gainXP(Person.WEEK_TRAINING_XP);
        return;
      }
    },
    
    ALL_BLUEPRINTS[] = {
      GENERATOR , INFIRMARY    , ARBORETUM     ,
      LABORATORY, TRAINING_ROOM, SENSOR_STATION,
    },
    NONE = new Blueprint(
      "None", "blueprint_none", "", IMG_DIR+"blank_room.png"
    );
  
  
  
  Blueprint(
    String name, String ID, String description, String spritePath
  ) {
    super(INDEX, ID);
    this.name = name;
    this.description = description;
    this.sprite = Kind.loadImage(spritePath);
    this.initStats();
  }
  
  
  void initStats() {
    return;
  }
  
  
  public static Blueprint loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public int buildCost  () { return buildCost  ; }
  public int visitLimit () { return visitLimit ; }
  public int maintenance() { return maintenance; }
  public int lifeSupport() { return lifeSupport; }
  public int powerCost  () { return powerCost  ; }
  
  
  
  /**  Effects on base facilities-
    */
  void updateForBase(Base base, Room room) {
    if (powerCost > 0) base.powerUse += powerCost;
    if (room.buildProgress < 1) {
      float buildRate = base.buildRate(this);
      room.buildProgress = Nums.min(1, room.buildProgress + buildRate);
    }
    else {
      if (powerCost < 0) base.maxPower += 0 - powerCost;
      base.maxSupport  += lifeSupport;
      base.maintenance += maintenance;
    }
  }
  
  
  void affectVisitor(Person visiting, Base base) {
    return;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}











