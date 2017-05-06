

package proto.game.world;
import proto.common.*;
import proto.util.*;

import java.awt.Image;



//  TODO:  Extend Kind?


public class Tech extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <Tech> INDEX = new Index <Tech> ();
  
  final String name;
  final String info;
  final Image icon;
  
  Object researchArgs[];
  final int researchTime;
  final Object granted[];
  
  
  public Tech(
    String name, String ID, String imgPath, String info,
    Object... granted
  ) {
    super(INDEX, ID);
    this.name = name;
    this.info = info;
    this.icon = Kind.loadImage(imgPath);
    
    this.researchArgs = new Object[] {};
    this.researchTime = World.HOURS_PER_DAY * World.DAYS_PER_WEEK * 2;
    this.granted = granted;
  }
  
  
  public static Tech loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  public Object[] researchArgs() {
    return researchArgs;
  }
  
  
  public int researchTime() {
    return researchTime;
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public Image icon() { return icon; }
  public String helpInfo() { return info; }
}









