

package proto.view;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;

import java.awt.*;
import java.awt.image.*;



public class RegionsMapView {
  
  
  BufferedImage keyImage;
  RegionView attached[];
  
  
  void loadMapImages(String keyImageName) {
    keyImage = (BufferedImage) Kind.loadImage(keyImageName);
  }
  
  
  void attachOutlinesFor(Nation... nations) {
    if (this.attached != null) return;
    
    attached = new RegionView[nations.length];
    for (int i = attached.length; i-- > 0;) {
      attached[i] = nations[i].region.view;
    }
    
    int imgWide = keyImage.getWidth(), imgHigh = keyImage.getHeight();
    int pixVals[] = keyImage.getRGB(0, 0, imgWide, imgHigh, null, 0, imgWide);
    int fills = new Color(1, 1, 1, 0.5f).getRGB();
    
    for (Coord c : Visit.grid(0, 0, imgWide, imgHigh, 1)) {
      int pixVal = pixVals[(c.y * imgWide) + c.x];
      for (RegionView r : attached) if (r.colourKey == pixVal) {
        if (r.bounds == null) { r.bounds = new Box2D(c.x, c.y, 0, 0); }
        else r.bounds.include(c.x, c.y, 0);
        break;
      }
    }
    
    for (RegionView r : attached) if (r.bounds != null && r.outline == null) {
      int w = (int) (r.bounds.xdim() + 1), h = (int) (r.bounds.ydim() + 1);
      r.outline  = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      r.outlineX = (int) r.bounds.xpos();
      r.outlineY = (int) r.bounds.ypos();
      r.centerX  = r.outlineX + (w / 2);
      r.centerY  = r.outlineY + (h / 2);
    }
    for (Coord c : Visit.grid(0, 0, imgWide, imgHigh, 1)) {
      int pixVal = pixVals[(c.y * imgWide) + c.x];
      for (RegionView r : attached) if (r.colourKey == pixVal) {
        r.outline.setRGB(c.x - r.outlineX, c.y - r.outlineY, fills);
      }
    }
  }
}




