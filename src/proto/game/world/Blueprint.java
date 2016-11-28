

package proto.game.world;
import proto.common.*;
import proto.game.event.Task;
import proto.game.person.*;
import proto.util.*;
import java.awt.Image;




public class Blueprint extends Kind {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Image icon;
  
  final District.Stat stats[];
  final int statMods[];
  final int buildCost, buildTime;
  
  int security, lighting, cover;
  
  
  
  public Blueprint(String name, String ID, String imgPath, String info) {
    this(name, ID, imgPath, info, 0, -1);
  }
  
  
  public Blueprint(
    String name, String ID, String imgPath, String info,
    int buildCost, int buildTime, Object... args
  ) {
    super(name, ID, info);

    this.icon = Kind.loadImage(imgPath);
    
    this.buildCost = buildCost;
    this.buildTime = buildTime;
    
    final int numS = args.length / 2;
    this.stats = new District.Stat[numS];
    this.statMods = new int[numS];
    
    for (int n = 0; n < numS; n++) {
      stats   [n] = (District.Stat) args[ n * 2     ];
      statMods[n] = (Integer      ) args[(n * 2) + 1];
    }
  }
  
  
  
  /**  Active effects-
    */
  protected void applyStatEffects(District district) {
    for (int i = 0; i < stats.length; i++) {
      district.statLevels[stats[i].ID].bonus += statMods[i];
    }
  }
  
  
  protected int incomeFrom(District district) {
    final int index = Visit.indexOf(District.INCOME, stats);
    return index == -1 ? 0 : statMods[index];
  }
  
  
  protected float speedBonus(Task task) {
    return 0;
  }
  
  
  
  /**  Construction and prereqs-
    */
  public boolean canBuild(Base owner, District district) {
    return owner.currentFunds() >= buildCost;
  }
  
  
  protected Place createRoom(Base base, int slotID) {
    Place place = new Place(this, slotID, base.world);
    place.setOwner(base);
    return place;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() {
    return icon;
  }
  
  
  public String info() {
    final StringBuffer s = new StringBuffer();
    
    s.append(name());
    s.append("\n\n");
    s.append(info());
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











