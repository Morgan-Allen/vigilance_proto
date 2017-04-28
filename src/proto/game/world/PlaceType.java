

package proto.game.world;
import proto.common.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Image;



public class PlaceType extends Kind {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Image icon;
  final SceneType sceneType;
  final public int buildCost, buildTime;
  
  
  public PlaceType(
    String name, String ID, String imgPath, String info,
    SceneType sceneType
  ) {
    this(name, ID, imgPath, info, 0, -1, sceneType);
  }
  
  
  public PlaceType(
    String name, String ID, String imgPath, String info,
    int buildCost, int buildTime, SceneType sceneType,
    Object... initStats
  ) {
    super(name, ID, info, Kind.TYPE_PLACE);
    
    this.icon = Kind.loadImage(imgPath);
    
    this.buildCost = buildCost;
    this.buildTime = buildTime;
    initStatsFor(this, initStats);
    this.sceneType = sceneType;
  }
  
  
  public SceneType sceneType() {
    return sceneType;
  }
  
  
  public boolean providesItemType(ItemType type) {
    return Visit.arrayIncludes(baseEquipped(), type);
  }
  
  
  
  /**  Active effects-
    */
  public int incomeFrom(Region district) {
    return baseLevel(Region.INCOME);
  }
  
  
  public int bonusFor(Region.Stat stat) {
    return baseLevel(stat);
  }
  
  
  public float traitBonus(Attempt attempt) {
    return 0;
  }
  
  
  public float speedBonus(Attempt attempt) {
    return 0;
  }
  
  
  
  /**  Construction and prereqs-
    */
  public boolean canBuild(Base owner, Region district) {
    return owner.finance.publicFunds() >= buildCost;
  }
  
  
  public Place createRoom(Base base, int slotID) {
    Place place = new Place(this, slotID, base.world);
    place.setOwner(base);
    return place;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return icon;
  }
  
  
  public String defaultInfo() {
    final StringBuffer s = new StringBuffer();
    
    s.append(name());
    s.append("\n\n");
    s.append(super.defaultInfo());
    s.append("\n\n");
    s.append("Build cost: "+buildCost+" ("+(buildTime / 7)+" weeks to build)");
    s.append("\n");
    s.append(statInfo());
    
    return s.toString();
  }
  
  
  public String statInfo() {
    final StringBuffer s = new StringBuffer();
    for (Region.Stat stat : Region.ALL_STATS) {
      int level = baseLevel(stat);
      if (level == 0) continue;
      s.append(stat.name+" "+I.signNum(level)+"\n");
    }
    return s.toString();
  }
}











