

package proto.view;
import proto.common.*;
import proto.game.world.*;
import proto.game.scene.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;



public class MapView extends UINode {
  
  
  /**  Data fields, construction, setup and attachment-
    */
  BufferedImage mapImage;
  BufferedImage keyImage;
  RegionAssets attached[];
  
  
  MapView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  
  
  void loadMapImages(String mapImageName, String keyImageName) {
    mapImage = (BufferedImage) Kind.loadImage(mapImageName);
    keyImage = (BufferedImage) Kind.loadImage(keyImageName);
  }
  
  
  void attachOutlinesFor(Nation... nations) {
    if (this.attached != null) return;
    
    attached = new RegionAssets[nations.length];
    for (int i = attached.length; i-- > 0;) {
      attached[i] = nations[i].region.view;
    }
    
    int imgWide = keyImage.getWidth(), imgHigh = keyImage.getHeight();
    int pixVals[] = keyImage.getRGB(0, 0, imgWide, imgHigh, null, 0, imgWide);
    int fills = new Color(1, 1, 1, 0.5f).getRGB();
    
    for (Coord c : Visit.grid(0, 0, imgWide, imgHigh, 1)) {
      int pixVal = pixVals[(c.y * imgWide) + c.x];
      for (RegionAssets r : attached) if (r.colourKey == pixVal) {
        if (r.bounds == null) { r.bounds = new Box2D(c.x, c.y, 0, 0); }
        else r.bounds.include(c.x, c.y, 0);
        break;
      }
    }
    
    for (RegionAssets r : attached) if (r.bounds != null && r.outline == null) {
      int w = (int) (r.bounds.xdim() + 1), h = (int) (r.bounds.ydim() + 1);
      r.outline  = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      r.outlineX = (int) r.bounds.xpos();
      r.outlineY = (int) r.bounds.ypos();
      r.centerX  = r.outlineX + (w / 2);
      r.centerY  = r.outlineY + (h / 2);
    }
    for (Coord c : Visit.grid(0, 0, imgWide, imgHigh, 1)) {
      int pixVal = pixVals[(c.y * imgWide) + c.x];
      for (RegionAssets r : attached) {
        if (r.colourKey != pixVal || r.outline == null) continue;
        r.outline.setRGB(c.x - r.outlineX, c.y - r.outlineY, fills);
      }
    }
  }
  
  
  
  /**  Actual rendering methods-
    */
  void renderTo(Surface surface, Graphics2D g) {
    Nation nations[] = mainView.world.nations();
    attachOutlinesFor(nations);
    //
    //  Draw the background image first-
    g.drawImage(mapImage, vx, vy, vw, vh, null);
    Nation nationHovered = null;
    //
    //  Then draw the nations of the world on the satellite map.
    int imgWide = mapImage.getWidth(), imgHigh = mapImage.getHeight();
    float mapWRatio = 1, mapHRatio = 1;
    mapWRatio *= (imgWide * 1f) / vw;
    mapHRatio *= (imgHigh * 1f) / vh;
    
    boolean mouseInMap = surface.tryHover(vx, vy, vw, vh, this);
    int mX = surface.mouseX(), mY = surface.mouseY();
    mX = (int) ((mX - vx) * mapWRatio);
    mY = (int) ((mY - vy) * mapHRatio);
    int pixVal = 0;
    if (mouseInMap && mX >= 0 && mX < imgWide && mY >= 0 && mY < imgHigh) {
      pixVal = ((BufferedImage) keyImage).getRGB(mX, mY);
    }
    
    ///if (I.used60Frames) I.say("Pixel value is: "+pixVal);
    Nation selectedArea = mainView.baseView.selectedNation();
    for (Nation n : nations) if (n.region.view.colourKey == pixVal) {
      nationHovered = n;
    }
    if (nationHovered != null && surface.mouseClicked()) {
      mainView.baseView.setSelection(nationHovered);
    }
    
    renderOutline(selectedArea , surface, g, mapWRatio, mapHRatio);
    renderOutline(nationHovered, surface, g, mapWRatio, mapHRatio);
    
    for (Nation n : nations) {
      int x = (int) ((n.region.view.centerX / mapWRatio) + vx);
      int y = (int) ((n.region.view.centerY / mapHRatio) + vy);
      
      g.setColor(Color.LIGHT_GRAY);
      g.drawString(n.region.name, x - 25, y + 25 + 15);
      
      float crimeLevel = n.crimeLevel(), trustLevel = n.trustLevel();
      ViewUtils.renderStatBar(
        x - 25, y + 25 + 15 + 5, 25, 5,
        Color.RED, Color.BLACK, crimeLevel, false, g
      );
      ViewUtils.renderStatBar(
        x - 0, y + 25 + 15 + 5, 25, 5,
        Color.BLUE, Color.BLACK, trustLevel, false, g
      );
      
      for (Event event : mainView.world.events().active()) {
        if (event.openLeadsFrom(n.region).size() > 0) {
          g.drawImage(mainView.alertMarker, x - 25, y - 25, 50, 50, null);
        }
      }
      ViewUtils.renderAssigned(visitors(n), x + 25, y + 25, surface, g);
    }
  }
  
  
  private Series <Person> visitors(Nation located) {
    final Batch <Person> visitors = new Batch();
    for (Person p : mainView.world.base().roster()) if (p.assignment() != null) {
      if (p.assignment().targetLocation() == located.region) {
        visitors.include(p);
      }
    }
    return visitors;
  }
  
  
  private void renderOutline(
    Nation n, Surface surface, Graphics2D g, float mapWRatio, float mapHRatio
  ) {
    if (n == null || n.region.view.outline == null) return;
    RegionAssets r = n.region.view;
    int
      x = (int) (vx + (r.outlineX / mapWRatio) + 0.5f),
      y = (int) (vy + (r.outlineY / mapHRatio) + 0.5f),
      w = (int) (r.outline.getWidth (null) / mapWRatio),
      h = (int) (r.outline.getHeight(null) / mapHRatio);
    g.drawImage(r.outline, x, y, w, h, null);
  }
}








