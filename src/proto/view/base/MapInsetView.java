

package proto.view.base;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;



public class MapInsetView extends UINode {
  
  
  /**  Data fields, construction, setup and attachment-
    */
  BufferedImage mapImage;
  BufferedImage keyImage;
  RegionAssets attached[];
  
  private static RegionType lastRegionType;
  
  
  public MapInsetView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  
  
  public void loadMapImages(String mapImageName, String keyImageName) {
    mapImage = (BufferedImage) Kind.loadImage(mapImageName);
    keyImage = (BufferedImage) Kind.loadImage(keyImageName);
  }
  
  
  public void resizeToFitAspectRatio() {
    float
      mapW  = mapImage.getWidth (),
      mapH  = mapImage.getHeight(),
      selfW = relBounds.xdim(),
      selfH = relBounds.ydim(),
      relW  = mapW / selfW,
      relH  = mapH / selfH
    ;
    
    if (relW < relH) {
      float shrink = selfW * ((relH / relW) - 1);
      relBounds.incWide(0 - shrink);
      relBounds.incX   (shrink / 2);
    }
    if (relH < relW) {
      float shrink = selfH * ((relW / relH) - 1);
      relBounds.incHigh(0 - shrink);
      relBounds.incY   (shrink / 2);
    }
  }
  
  
  void attachOutlinesFor(Region... nations) {
    if (this.attached != null) return;
    
    attached = new RegionAssets[nations.length];
    for (int i = attached.length; i-- > 0;) {
      attached[i] = nations[i].kind().view;
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
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Region regions[] = mainView.world().regions();
    Base played = mainView.world().playerBase();
    attachOutlinesFor(regions);
    if (selectedRegion() == null) setSelectedRegion(regions[0]);
    //
    //  Draw the background image first-
    g.drawImage(mapImage, vx, vy, vw, vh, null);
    Region regionHovered = null;
    //
    //  Then draw the nations of the world on the overhead map.
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
    
    for (Region n : regions) if (n.kind().view.colourKey == pixVal) {
      regionHovered = n;
    }
    if (regionHovered != null && surface.mouseClicked()) {
      this.setSelectedRegion(regionHovered);
    }
    
    renderOutline(selectedRegion(), surface, g, mapWRatio, mapHRatio);
    renderOutline(regionHovered   , surface, g, mapWRatio, mapHRatio);
    
    for (Region n : regions) {
      int x = (int) ((n.kind().view.centerX / mapWRatio) + vx);
      int y = (int) ((n.kind().view.centerY / mapHRatio) + vy);
      
      g.setColor(Color.LIGHT_GRAY);
      g.drawString(n.kind().name(), x - 25, y + 25 + 15);
      
      float crimeLevel = n.currentValue(Region.VIOLENCE) / 100f;
      ViewUtils.renderStatBar(
        x - 25, y + 25 + 15 + 5, 50, 5,
        Color.RED, Color.BLACK, crimeLevel, false, g
      );
      
      Series <CaseFile> files = played.leads.casesForRegion(n);
      if (! files.empty()) {
        g.drawImage(mainView.alertMarker, x - 25, y - 25, 50, 50, null);
      }
      
      ViewUtils.renderAssigned(visitors(n), x + 25, y + 25, surface, g);
    }
    
    return true;
  }
  
  
  private Series <Person> visitors(Region located) {
    final Batch <Person> visitors = new Batch();
    final Series <Person> roster = mainView.world().playerBase().roster();
    for (Person p : roster) {
      final Assignment a = p.topAssignment();
      if (a != null && a.targetLocation().region() == located) {
        visitors.include(p);
      }
      else if (p.region() == located) {
        visitors.include(p);
      }
    }
    return visitors;
  }
  
  
  private void renderOutline(
    Region n, Surface surface, Graphics2D g, float mapWRatio, float mapHRatio
  ) {
    if (n == null || n.kind().view.outline == null) return;
    RegionAssets r = n.kind().view;
    int
      x = (int) (vx + (r.outlineX / mapWRatio) + 0.5f),
      y = (int) (vy + (r.outlineY / mapHRatio) + 0.5f),
      w = (int) (r.outline.getWidth (null) / mapWRatio),
      h = (int) (r.outline.getHeight(null) / mapHRatio);
    g.drawImage(r.outline, x, y, w, h, null);
  }
  
  
  
  public void setSelectedRegion(Region region) {
    lastRegionType = region.kind();
  }
  
  
  public Region selectedRegion() {
    return mainView.world().regionFor(lastRegionType);
  }
}




