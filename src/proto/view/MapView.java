

package proto.view;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.util.*;

import java.awt.*;
import java.awt.image.*;



public class MapView {
  
  
  /**  Data fields, construction, setup and attachment-
    */
  final WorldView parent;
  final Box2D viewBounds;
  
  BufferedImage mapImage;
  BufferedImage keyImage;
  RegionView attached[];
  
  Nation selectedNation;
  
  
  MapView(WorldView parent, Box2D viewBounds) {
    this.parent     = parent    ;
    this.viewBounds = viewBounds;
  }
  
  
  void loadMapImages(String mapImageName, String keyImageName) {
    mapImage = (BufferedImage) Kind.loadImage(mapImageName);
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
      for (RegionView r : attached) {
        if (r.colourKey != pixVal || r.outline == null) continue;
        r.outline.setRGB(c.x - r.outlineX, c.y - r.outlineY, fills);
      }
    }
  }
  
  
  
  /**  Actual rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    Nation nations[] = parent.world.nations();
    attachOutlinesFor(nations);
    //
    //  Draw the background image first-
    final Box2D b = this.viewBounds;
    g.drawImage(
      mapImage,
      (int) b.xpos(), (int) b.ypos(),
      (int) b.xdim(), (int) b.ydim(),
      null
    );
    Nation nationHovered = null;
    //
    //  Then draw the nations of the world on the satellite map.
    int viewWide = (int) b.xdim(), viewHigh = (int) b.ydim();
    int imgWide = mapImage.getWidth(), imgHigh = mapImage.getHeight();
    float mapWRatio = 1, mapHRatio = 1;
    mapWRatio *= (imgWide * 1f) / viewWide;
    mapHRatio *= (imgHigh * 1f) / viewHigh;
    
    int mX = surface.mouseX, mY = surface.mouseY;
    mX = (int) ((mX - b.xpos()) * mapWRatio);
    mY = (int) ((mY - b.ypos()) * mapHRatio);
    int pixVal = 0;
    if (mX >= 0 && mX < imgWide && mY >= 0 && mY < imgHigh) {
      pixVal = ((BufferedImage) keyImage).getRGB(mX, mY);
    }
    
    ///if (I.used60Frames) I.say("Pixel value is: "+pixVal);
    
    for (Nation n : nations) if (n.region.view.colourKey == pixVal) {
      nationHovered = n;
    }
    if (nationHovered != null && surface.mouseClicked) {
      parent.setSelection(selectedNation = nationHovered);
    }
    
    renderOutline(selectedNation, surface, g, mapWRatio, mapHRatio);
    renderOutline(nationHovered , surface, g, mapWRatio, mapHRatio);
    
    for (Investigation event : parent.world.events().active()) {
      for (Nation n : nations) if (event.leadsFrom(n.region).size() > 0) {
        int x = (int) ((n.region.view.centerX / mapWRatio) + b.xpos());
        int y = (int) ((n.region.view.centerY / mapHRatio) + b.ypos());
        g.drawImage(parent.alertMarker, x - 25, y - 25, 50, 50, null);
        //renderAssigned(crisis.playerTeam(), x - 25, y + 15, surface, g);
      }
    }
  }
  
  
  void renderOutline(
    Nation n, Surface surface, Graphics2D g, float mapWRatio, float mapHRatio
  ) {
    if (n == null || n.region.view.outline == null) return;
    RegionView r = n.region.view;
    final Box2D b = this.viewBounds;
    int
      x = (int) (b.xpos() + (r.outlineX / mapWRatio) + 0.5f),
      y = (int) (b.ypos() + (r.outlineY / mapHRatio) + 0.5f),
      w = (int) (r.outline.getWidth (null) / mapWRatio),
      h = (int) (r.outline.getHeight(null) / mapHRatio);
    g.drawImage(r.outline, x, y, w, h, null);
  }
}








