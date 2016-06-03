

package proto.game.world;
import proto.common.*;
import proto.util.*;

import java.awt.Image;



//  TODO:  Unify this with the Blueprint class, and treat Districts like bases
//         with rooms?  It might simplify things.


public class Facility extends Index.Entry implements Session.Saveable {
  
  
  final static Index <Facility> INDEX = new Index();
  
  final District.Stat stats[];
  final int statMods[];
  
  final String name;
  final Image icon;
  
  
  public Facility(String name, String ID, String imgPath, Object... args) {
    super(INDEX, ID);
    
    this.name = name;
    this.icon = Kind.loadImage(imgPath);
    
    final int numS = args.length / 2;
    this.stats = new District.Stat[numS];
    this.statMods = new int[numS];
    
    for (int n = 0; n < numS; n++) {
      stats   [n] = (District.Stat) args[ n * 2     ];
      statMods[n] = (Integer      ) args[(n * 2) + 1];
    }
  }
  
  
  public static Facility loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  protected void applyStatEffects(District district) {
    for (int i = 0; i < stats.length; i++) {
      district.statLevels[stats[i].ID].bonus += statMods[i];
    }
  }
  
  
  protected int incomeFrom(District district) {
    final int index = Visit.indexOf(District.INCOME, stats);
    return index == -1 ? 0 : statMods[index];
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String name() {
    return name;
  }
  
  
  public Image icon() {
    return icon;
  }
  
  
  public String info() {
    final StringBuffer s = new StringBuffer();
    
    return s.toString();
  }
  
}










