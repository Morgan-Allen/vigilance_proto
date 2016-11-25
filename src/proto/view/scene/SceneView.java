

package proto.view.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;

import proto.util.*;
import proto.view.common.*;

import java.awt.*;



public class SceneView extends UINode {
  
  final static int
    TILE_SIZE = 64;
  final static String IMG_DIR = "media assets/scene view/";
  
  final ActionsView actionsView;
  
  Tile zoomTile;
  int zoomX, zoomY;
  Person selected;
  
  
  
  public SceneView(UINode parent, Box2D bounds) {
    super(parent, bounds);
    
    actionsView = new ActionsView(this);
    setChild(actionsView, true);
  }
  
  
  public void loadState(Session s) throws Exception {
    zoomTile = (Tile) s.loadObject();
    zoomX    = s.loadInt();
    zoomY    = s.loadInt();
    selected = (Person) s.loadObject();
    actionsView.selectAbility = (Ability) s.loadObject();
    actionsView.selectAction  = (Action ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(zoomTile);
    s.saveInt   (zoomX   );
    s.saveInt   (zoomY   );
    s.saveObject(selected);
    s.saveObject(actionsView.selectAbility);
    s.saveObject(actionsView.selectAction );
  }
  
  
  public void setZoomPoint(Tile t) {
    this.zoomTile = t;
  }
  
  
  public void setSelection(Person acting, boolean keepActiveAbility) {
    final Person old = selected;
    this.selected = acting;
    if (acting != old || ! keepActiveAbility) actionsView.clearSelection();
  }
  
  
  protected void updateAndRender(Surface surface, Graphics2D g) {
    final Box2D b = this.relBounds;
    actionsView.relBounds.set(0, 0, b.xdim() / 4f, b.ydim());
    super.updateAndRender(surface, g);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    final Scene scene = mainView.world().activeScene();
    if (scene == null) return false;
    
    int size = scene.size();
    Action done = scene.currentAction();
    Batch <Person> visible = new Batch <Person> ();
    
    final Color SCALE[] = new Color[10];
    final Color ENEMY[] = new Color[10];
    final Color PALES[] = new Color[10];
    for (int n = 10; n-- > 0;) {
      SCALE[n] = new Color(0, 0, 0, n / 10f);
      ENEMY[n] = new Color(1, 0, 0, n / 50f);
      PALES[n] = new Color(1, 1, 1, n / 10f);
    }
    final Image hoverBox = mainView.selectCircle;
    
    //
    //  Update camera information first-
    if (zoomTile != null) {
      zoomX = zoomTile.x * TILE_SIZE;
      zoomY = zoomTile.y * TILE_SIZE;
    }
    zoomX -= vw / 2;
    zoomY -= vh / 2;
    
    //
    //  Then, render any props, persons, or special FX-
    for (Prop prop : scene.props()) {
      Tile t = prop.origin();
      renderAt(t.x, t.y, prop.kind(), g);
    }
    
    //
    //  Render enemy-sight on top of objects but beneath persons-
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      Tile t = scene.tileAt(c.x, c.y);
      float seeAlpha = scene.fogAt(t, Person.Side.VILLAINS);
      if (seeAlpha > 0) {
        Color warns = ENEMY[Nums.clamp((int) (seeAlpha * 10), 10)];
        renderAt(c.x, c.y, 1, 1, null, warns, g);
      }
    }
    
    //
    //  Render the persons themselves-
    for (Person p : scene.persons()) {
      if (scene.fogAt(p.currentTile(), Person.Side.HEROES) <= 0) continue;
      Vec3D pos = p.exactPosition();
      renderAt(pos.x, pos.y, p.kind(), g);
      visible.add(p);
    }
    for (Person p : visible) {
      Color               teamColor = Color.GREEN;
      if (p.isHero    ()) teamColor = Color.BLUE;
      if (p.isCriminal()) teamColor = Color.RED;
      Vec3D pos         = p.exactPosition();
      float healthLevel = p.health.healthLevel();
      float stunLevel   = 1 - p.health.bleedRisk();
      Color pale = PALES[(int) (stunLevel * 9.9f)];
      
      renderAt(pos.x, pos.y + 0.9f, 1, 0.1f, null, Color.BLACK, g);
      renderAt(pos.x, pos.y + 0.9f, 1, 0.1f, null, pale, g);
      renderAt(pos.x, pos.y + 0.9f, healthLevel, 0.1f, null, teamColor, g);
      renderString(pos.x, pos.y + 0.5f, p.name(), Color.WHITE, g);
      
      if (p == selected) {
        renderAt(pos.x, pos.y, 1, 1, hoverBox, null, g);
      }
    }
    if (done != null && done.progress() >= 0) {
      done.used.renderUsageFX(done, scene, g);
    }
    
    //
    //  Then render our own fog on top of all objects-
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      Tile t = scene.tileAt(c.x, c.y);
      float fogAlpha = 1f - scene.fogAt(t, Person.Side.HEROES);
      Color black = SCALE[Nums.clamp((int) (fogAlpha * 10), 10)];
      renderAt(c.x, c.y, 1, 1, null, black, g);
    }
    
    //
    //  If complete, display a summary of the results!
    if (scene.complete()) {
      return true;
    }
    
    //
    //  Otherwise, determine what tile (and any objects above) are being
    //  selected-
    int HT = TILE_SIZE / 2;
    int hoverX = (surface.mouseX() + zoomX + HT - vx) / TILE_SIZE;
    int hoverY = (surface.mouseY() + zoomY + HT - vy) / TILE_SIZE;
    Tile hoverT = scene.tileAt(hoverX, hoverY);
    
    if (hoverT != null) {
      renderAt(hoverT.x, hoverT.y, 1, 1, hoverBox, null, g);
      Object hovered = topObjectAt(hoverT);
      boolean canDoAction = false;
      
      if (actionsView.previewActionDelivery(hovered, hoverT, surface, g)) {
        canDoAction = true;
      }
      
      if (surface.mouseClicked()) {
        if (done == null && canDoAction) {
          scene.setNextActing(selected);
          selected.actions.assignAction(actionsView.selectAction);
        }
        else {
          Person pickP = null;
          if (hovered instanceof Person) pickP = (Person) hovered;
          if (pickP != null) setSelection(pickP, true);
          else setZoomPoint(hoverT);
        }
      }
    }
    
    return true;
  }
  
  
  void renderString(float px, float py, String s, Color c, Graphics2D g) {
    int x, y;
    x = vx + (int) ((px - 0.50f) * TILE_SIZE);
    y = vy + (int) ((py + 0.15f) * TILE_SIZE);
    g.setColor(c);
    g.drawString(s, x - zoomX, y - zoomY);
  }
  

  void renderAt(float px, float py, Kind kind, Graphics2D g) {
    renderAt(px, py, kind.wide(), kind.high(), kind.sprite(), null, g);
  }
  
  
  public void renderAt(
    float px, float py, float w, float h,
    Image sprite, Color fill, Graphics2D g
  ) {
    int x, y;
    x = vx + (int) ((px - 0.5f) * TILE_SIZE);
    y = vy + (int) ((py - 0.5f) * TILE_SIZE);
    w *= TILE_SIZE;
    h *= TILE_SIZE;
    
    if (sprite != null) {
      g.drawImage(sprite, x - zoomX, y - zoomY, (int) w, (int) h, null);
    }
    if (fill != null) {
      g.setColor(fill);
      g.fillRect(x - zoomX, y - zoomY, (int) w, (int) h);
    }
  }
  
  
  public Object topObjectAt(Tile at) {
    if (at == null) return null;
    
    //  TODO:  Move this to SceneView?  It's really about UI-selection...
    final Pick <Object> pick = new Pick();
    for (Person p : at.inside()) {
      pick.compare(p, p.health.conscious() ? 1.5f : 1);
    }
    pick.compare(at.prop(), 0.5f);
    return pick.result();
  }
  
  
  public Coord screenCoord(Tile point) {
    return screenCoord(point.x, point.y);
  }
  
  
  public Coord screenCoord(float px, float py) {
    int x, y;
    x = (int) ((px - 0.0f) * TILE_SIZE) - zoomX;
    y = (int) ((py - 0.0f) * TILE_SIZE) - zoomY;
    return new Coord(x, y);
  }
}








