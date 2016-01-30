

package proto.view;
import java.awt.Image;

import proto.common.Kind;



public class RegionView {
  
  
  int colourKey;
  Image outline;
  int outlineX, outlineY, centerX, centerY;

  
  
  public void loadOutline(String imgFile, int colourKey, int offX, int offY) {
    outline = Kind.loadImage(WorldView.IMG_DIR+imgFile);
    this.colourKey = colourKey;
    outlineX = offX;
    outlineY = offY;
    if (outline != null) {
      centerX = offX + (outline.getWidth (null) / 2);
      centerY = offY + (outline.getHeight(null) / 2);
    }
  }
}
