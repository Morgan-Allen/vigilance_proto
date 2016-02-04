

package proto.view;
import proto.util.*;
import java.awt.*;
import java.awt.image.*;



public class RegionView {
  
  
  String name;
  int colourKey;
  Box2D bounds = null;
  BufferedImage outline;
  int outlineX, outlineY, centerX, centerY;
  
  
  public void attachColourKey(int key, String name) {
    this.name      = name;
    this.colourKey = key ;
  }
}











