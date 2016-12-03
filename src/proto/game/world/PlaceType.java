

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.util.*;
import java.awt.Image;




public class PlaceType extends Kind {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Image icon;
  
  final Region.Stat stats[];
  final int statMods[];
  final int buildCost, buildTime;
  
  final SceneType sceneType;
  int security, lighting, cover;
  
  
  
  public PlaceType(
    String name, String ID, String imgPath, String info,
    SceneType sceneType
  ) {
    this(name, ID, imgPath, info, 0, -1, sceneType);
  }
  
  
  public PlaceType(
    String name, String ID, String imgPath, String info,
    int buildCost, int buildTime, SceneType sceneType,
    Object... args
  ) {
    super(name, ID, info);

    this.icon = Kind.loadImage(imgPath);
    
    this.buildCost = buildCost;
    this.buildTime = buildTime;
    
    final int numS = args.length / 2;
    this.stats = new Region.Stat[numS];
    this.statMods = new int[numS];
    
    for (int n = 0; n < numS; n++) {
      stats   [n] = (Region.Stat) args[ n * 2     ];
      statMods[n] = (Integer      ) args[(n * 2) + 1];
    }
    
    this.sceneType = sceneType;
  }
  
  
  public SceneType sceneType() {
    return sceneType;
  }
  
  
  
  /**  Active effects-
    */
  protected void applyStatEffects(Region district) {
    for (int i = 0; i < stats.length; i++) {
      district.statLevels[stats[i].ID].bonus += statMods[i];
    }
  }
  
  
  public int incomeFrom(Region district) {
    final int index = Visit.indexOf(Region.INCOME, stats);
    return index == -1 ? 0 : statMods[index];
  }
  
  
  public float speedBonus(Task task) {
    return 0;
  }
  
  
  
  /**  Construction and prereqs-
    */
  public boolean canBuild(Base owner, Region district) {
    return owner.currentFunds() >= buildCost;
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
    for (int i = 0; i < stats.length; i++) {
      s.append(stats[i].name+" "+I.signNum(statMods[i])+"\n");
    }
    return s.toString();
  }
}











