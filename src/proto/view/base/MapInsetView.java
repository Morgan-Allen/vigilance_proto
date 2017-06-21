

package proto.view.base;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;



public abstract class MapInsetView extends UINode {
  
  
  /**  Data fields, construction, setup and attachment-
    */
  BufferedImage mapImage;
  BufferedImage keyImage;
  BufferedImage outlines;
  RegionAssets attached[];
  
  private static RegionType lastRegionType;
  
  
  public MapInsetView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  
  
  public void loadMapImages(
    String mapImageName, String keyImageName, String outlinesName
  ) {
    mapImage = (BufferedImage) Kind.loadImage(mapImageName);
    keyImage = (BufferedImage) Kind.loadImage(keyImageName);
    outlines = (BufferedImage) Kind.loadImage(outlinesName);
  }
  
  
  public void resizeToFitAspectRatio() {
    float
      mapW  = mapImage.getWidth (),
      mapH  = mapImage.getHeight(),
      selfW = relBounds.xdim(),
      selfH = relBounds.ydim()
    ;
    
    if (selfW < selfH) {
      float shrink = selfH - selfW;
      relBounds.incHigh(0 - shrink);
      relBounds.incY   (shrink / 2);
    }
    
    if (selfH < selfW) {
      float shrink = selfW - selfH;
      relBounds.incWide(0 - shrink);
      relBounds.incX   (shrink / 2);
    }
  }
  
  
  void attachOutlinesFor(Region... nations) {
    if (this.attached != null) return;
    
    attached = new RegionAssets[nations.length];
    for (int i = attached.length; i-- > 0;) {
      attached[i] = nations[i].kind().view;
    }
    
    int imgWide    = keyImage.getWidth(), imgHigh = keyImage.getHeight();
    int pixVals[]  = keyImage.getRGB(0, 0, imgWide, imgHigh, null, 0, imgWide);
    int lineVals[] = outlines.getRGB(0, 0, imgWide, imgHigh, null, 0, imgWide);
    
    for (Coord c : Visit.grid(0, 0, imgWide, imgHigh, 1)) {
      int pixVal = pixVals[(c.y * imgWide) + c.x];
      for (RegionAssets r : attached) if (r.colourKey == pixVal) {
        if (r.bounds == null) { r.bounds = new Box2D(c.x, c.y, 0, 0); }
        else r.bounds.include(c.x, c.y, 0);
        r.sumX += c.x;
        r.sumY += c.y;
        r.numPix += 1;
        break;
      }
    }
    
    for (RegionAssets r : attached) if (r.bounds != null && r.outline == null) {
      int w = (int) (r.bounds.xdim() + 1), h = (int) (r.bounds.ydim() + 1);
      r.outline  = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      r.outlineX = (int) r.bounds.xpos();
      r.outlineY = (int) r.bounds.ypos();
      r.centerX  = r.sumX / r.numPix;
      r.centerY  = r.sumY / r.numPix;
    }
    for (Coord c : Visit.grid(0, 0, imgWide, imgHigh, 1)) {
      int pixVal = pixVals [(c.y * imgWide) + c.x];
      int fills  = lineVals[(c.y * imgWide) + c.x];
      for (RegionAssets r : attached) {
        if (r.colourKey != pixVal || r.outline == null) continue;
        r.outline.setRGB(c.x - r.outlineX, c.y - r.outlineY, fills);
      }
    }
  }
  
  
  
  /**  Actual rendering methods-
    */
  protected boolean renderTo(Surface surface, Graphics2D g) {
    Region  regions[] = mainView.world().regions();
    Base    played    = mainView.world().playerBase();
    
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
      setSelectedRegion(regionHovered);
      onRegionSelect(regionHovered);
    }
    
    renderOutline(selectedRegion(), surface, g, mapWRatio, mapHRatio);
    renderOutline(regionHovered   , surface, g, mapWRatio, mapHRatio);
    renderBorderConnections(regionHovered, surface, g, mapWRatio, mapHRatio);
    
    //
    //  Then render any other widgets for each region-
    for (final Region n : regions) {
      int x = (int) ((n.kind().view.centerX / mapWRatio) + vx);
      int y = (int) ((n.kind().view.centerY / mapHRatio) + vy);
      
      g.setColor(Color.WHITE);
      g.drawString(n.kind().name(), x - 25, y);
      
      float crimeLevel = 0;
      crimeLevel += n.currentValue(Region.VIOLENCE  ) / 200f;
      crimeLevel += n.currentValue(Region.CORRUPTION) / 200f;
      ViewUtils.renderStatBar(
        x - 25, y + 5, 50, 5,
        Color.RED, Color.BLACK, crimeLevel, false, g
      );
      
      //
      //  Render any facilities in the region-
      int offF = 5 + ((n.buildSlots().length * 35) / -2);
      int slotID = 0;
      
      for (final Place slot : n.buildSlots()) {
        
        final ImageButton button = new ImageButton(
          slot == null ? RegionView.NOT_BUILT : slot.icon(),
          new Box2D(x + offF - vx, y + vy - 125, 30, 30), this
        ) {
          protected void whenClicked() {
            if (slot == null) return;
            mainView.mapView.setActiveFocus(slot, true);
          }
        };
        button.valid = slot != null;
        button.refers = "slot_"+slotID;
        if (slot != null && slot.buildProgress() < 1) {
          button.attachOverlay(RegionView.IN_PROGRESS);
        }
        
        if (slot != null && played.leads.cluesAssociated(slot).size() > 0) {
          
          boolean urgent = played.leads.suspectIsUrgent(slot);
          Image alertIcon = MapView.CLUE_IMAGE;
          if (urgent) alertIcon = MapView.ALERT_IMAGE;
          
          ImageButton alert = new ImageButton(
            alertIcon, new Box2D(x + offF - vx, y + vy - 150, 30, 30), this
          ) {
            protected void whenClicked() {}
          };
          button.toggled = false;
          alert.renderNow(surface, g);
        }
        
        button.renderNow(surface, g);
        offF += 30 + 5;
      }
      //
      //  Then render any current visitors to the region-
      ViewUtils.renderAssigned(visitors(n), x + 25, y + 35, this, surface, g);
    }
    
    return true;
  }
  
  
  
  /**  Additional helper methods-
    */
  private Series <Person> visitors(Region located) {
    final Batch <Person> visitors = new Batch();
    final Series <Person> roster = mainView.world().playerBase().roster();
    
    for (Person p : roster) {
      final Assignment a = p.topAssignment();
      if (a != null && a.targetElement(p).region() == located) {
        visitors.include(p);
      }
      else if (a == null && p.region() == located) {
        visitors.include(p);
      }
    }
    return visitors;
  }
  
  
  void presentBuildOptions(
    Region d, int slotID
  ) {
    final BuildOptionsView options = new BuildOptionsView(mainView, d, slotID);
    mainView.queueMessage(options);
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
  
  
  private void renderBorderConnections(
    Region n, Surface surface, Graphics2D g, float mapWRatio, float mapHRatio
  ) {
    if (n == null || n.kind().view.outline == null) return;
    if (! GameSettings.viewRegionsNear) return;
    RegionAssets r = n.kind().view;
    g.setColor(Color.WHITE);
    for (RegionType b : n.kind().bordering()) {
      RegionAssets rB = b.view;
      g.drawLine(
        (int) ((r .centerX / mapWRatio) + vx),
        (int) ((r .centerY / mapHRatio) + vy),
        (int) ((rB.centerX / mapWRatio) + vx),
        (int) ((rB.centerY / mapHRatio) + vy)
      );
    }
  }
  
  
  
  /**  Handling region-selection:
    */
  protected abstract void onRegionSelect(Region region);
  
  
  public void setSelectedRegion(Region region) {
    if (region == null) return;
    lastRegionType = region.kind();
  }
  
  
  public Region selectedRegion() {
    return mainView.world().regionFor(lastRegionType);
  }
}




