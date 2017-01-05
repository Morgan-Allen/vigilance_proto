

package proto.view.base;
import proto.common.*;
import proto.util.*;
import java.awt.*;
import java.awt.image.*;



public class RegionAssets {
  
  
  String name;
  Image portrait;
  
  int colourKey;
  Box2D bounds = null;
  BufferedImage outline;
  int outlineX, outlineY, centerX, centerY;
  
  
  public void attachPortrait(String imgPath) {
    this.portrait = Kind.loadImage(imgPath);
  }
  
  
  public void attachColourKey(int key, String name) {
    this.name      = name;
    this.colourKey = key ;
  }
}











