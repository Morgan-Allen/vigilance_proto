

package proto.view.scene;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.AffineTransform;



public class SceneView extends UINode {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static int
    TILE_SIZE = 64;
  final static String
    IMG_DIR = "media assets/scene view/";
  
  final ActionsView actionsView;
  
  Tile zoomTile;
  int zoomX, zoomY;
  Person activePerson;
  
  static class TempFX {
    Image sprite;
    float size;
    Vec3D pos = new Vec3D();
    int duration, framesLeft;
  }
  List <TempFX> tempFX = new List();
  
  
  
  public SceneView(UINode parent, Box2D bounds) {
    super(parent, bounds);
    
    actionsView = new ActionsView(this);
    setChild(actionsView, true);
  }
  
  
  public void loadState(Session s) throws Exception {
    zoomTile = (Tile) s.loadObject();
    zoomX    = s.loadInt();
    zoomY    = s.loadInt();
    activePerson = (Person) s.loadObject();
    actionsView.selectAbility = (Ability) s.loadObject();
    actionsView.selectAction  = (Action ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(zoomTile);
    s.saveInt   (zoomX   );
    s.saveInt   (zoomY   );
    s.saveObject(activePerson);
    s.saveObject(actionsView.selectAbility);
    s.saveObject(actionsView.selectAction );
  }
  
  
  
  /**  Public utility methods-
    */
  public void setZoomPoint(Tile t) {
    this.zoomTile = t;
  }
  
  
  public void setSelection(Person acting, boolean keepActiveAbility) {
    final Person old = activePerson;
    this.activePerson = acting;
    if (acting != old || ! keepActiveAbility) actionsView.clearSelection();
  }
  
  
  public void addTempFX(
    Image sprite, float size,
    float posX, float posY, float posZ, float numSeconds
  ) {
    final TempFX fx = new TempFX();
    fx.sprite = sprite;
    fx.size = size;
    fx.pos.set(posX, posY, posZ);
    fx.duration = (int) (numSeconds * RunGame.FRAME_RATE);
    fx.framesLeft = fx.duration;
    tempFX.add(fx);
  }
  
  
  
  /**  Overrides for ancestor methods-
    */
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
      zoomX -= vw / 2;
      zoomY -= vh / 2;
    }
    //
    //  Then, render any props, persons, or special FX-
    final List <Prop> propsShown = new List <Prop> () {
      protected float queuePriority(Prop r) {
        return r.renderPriority();
      }
    };
    for (Prop prop : scene.props()) {
      propsShown.add(prop);
    }
    propsShown.queueSort();
    for (Prop prop : propsShown) {
      prop.renderTo(scene, this, surface, g);
    }
    //
    //  Render enemy-sight on top of objects but beneath persons- but ONLY if
    //  the enemy is visible themselves.
    for (Person p : scene.othersTeam()) {
      float fog = scene.fogAt(p.currentTile(), Person.Side.HEROES);
      if (fog <= 0 && ! GameSettings.debugScene) continue;
      
      Vec3D exactPos = p.exactPosition();
      float sightRange = p.stats.sightRange();
      final Box2D area = new Box2D(exactPos.x, exactPos.y, 0, 0);
      area.expandBy(Nums.ceil(sightRange));
      
      for (Coord c : Visit.grid(area)) {
        float dist = exactPos.distance(c.x + 0.5f, c.y + 0.5f, 0) - 0.5f;
        float glare = Nums.clamp(dist / sightRange, 0, 1);
        if (glare <= 0) continue;
        Color warns = ENEMY[Nums.clamp((int) (glare * 10), 10)];
        renderAt(c.x, c.y, 1, 1, null, 0, warns, g);
      }
    }
    //
    //  Render the persons themselves-
    for (Person p : scene.persons()) {
      float fog = scene.fogAt(p.currentTile(), Person.Side.HEROES);
      if (fog <= 0 && ! GameSettings.debugScene) continue;
      p.renderTo(scene, this, surface, g);
      visible.add(p);
    }
    for (Person p : visible) {
      Color               teamColor = Color.GREEN;
      if (p.isHero    ()) teamColor = Color.BLUE;
      if (p.isCriminal()) teamColor = Color.RED;
      Vec3D pos         = p.exactPosition();
      float healthLevel = p.health.healthLevel();
      float stunLevel   = 1 - p.health.harmToStunRatio();
      Color pale = PALES[(int) (stunLevel * 9.9f)];
      
      renderAt(pos.x, pos.y + 0.9f, 1, 0.1f, null, 0, Color.BLACK, g);
      renderAt(pos.x, pos.y + 0.9f, 1, 0.1f, null, 0, pale, g);
      renderAt(pos.x, pos.y + 0.9f, healthLevel, 0.1f, null, 0, teamColor, g);
      renderString(pos.x, pos.y + 0.5f, p.name(), Color.WHITE, g);
      
      if (p == activePerson) {
        renderAt(pos.x, pos.y, 1, 1, hoverBox, 0, null, g);
      }
    }
    //
    //  Then render any action underway, and any temporary FX-
    if (done != null && done.progress() >= 0) {
      done.used.renderUsageFX(done, scene, g);
    }
    for (TempFX fx : tempFX) {
      float px = fx.pos.x - fx.size, py = fx.pos.y - fx.size, s2 = fx.size * 2;
      renderAt(px, py, s2, s2, fx.sprite, 0, null, g);
      fx.framesLeft--;
      if (fx.framesLeft <= 0) tempFX.remove(fx);
    }
    //
    //  Then render our own fog on top of all objects-
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      Tile t = scene.tileAt(c.x, c.y);
      float fogAlpha = 1f - scene.fogAt(t, Person.Side.HEROES);
      if (GameSettings.debugScene) fogAlpha /= 3;
      Color black = SCALE[Nums.clamp((int) (fogAlpha * 10), 10)];
      renderAt(c.x, c.y, 1, 1, null, 0, black, g);
    }
    //
    //  If complete, display a summary of the results!
    if (scene.complete() && ! GameSettings.debugScene) {
      return true;
    }
    //
    //  Otherwise, determine what tile (and any objects above) are being
    //  selected-
    boolean hasFocus = surface.tryHover(this);
    int HT = TILE_SIZE / 2;
    int hoverX = (surface.mouseX() + zoomX + HT - vx) / TILE_SIZE;
    int hoverY = (surface.mouseY() + zoomY + HT - vy) / TILE_SIZE;
    Tile hoverT = hasFocus ? scene.tileAt(hoverX, hoverY) : null;
    
    if (hoverT != null) {
      if (! hoverT.blocked()) {
        renderAt(hoverT.x, hoverT.y, 1, 1, hoverBox, 0, null, g);
      }
      Object hovered = topObjectAt(hoverT);
      boolean canDoAction = false;
      
      if (actionsView.previewActionDelivery(hovered, hoverT, surface, g)) {
        canDoAction = true;
      }
      
      if (surface.mouseClicked()) {
        if (done == null && canDoAction) {
          scene.setNextActing(activePerson);
          activePerson.actions.assignAction(actionsView.selectAction);
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
  
  
  public void renderString(
    float px, float py, String s, Color c, Graphics2D g
  ) {
    int x, y;
    x = vx + (int) ((px - 0.50f) * TILE_SIZE);
    y = vy + (int) ((py + 0.15f) * TILE_SIZE);
    g.setColor(c);
    g.drawString(s, x - zoomX, y - zoomY);
  }
  
  
  public void renderAt(
    float px, float py, float w, float h,
    Image sprite, float angle, Color fill, Graphics2D g
  ) {
    int x, y;
    x = vx + (int) ((px - 0.5f) * TILE_SIZE);
    y = vy + (int) ((py - 0.5f) * TILE_SIZE);
    w *= TILE_SIZE;
    h *= TILE_SIZE;
    
    if (sprite != null) {
      angle = Nums.toRadians(angle);
      w /= sprite.getWidth (null);
      h /= sprite.getHeight(null);
      AffineTransform t = AffineTransform.getTranslateInstance(
        x - zoomX, y - zoomY
      );
      t.rotate(angle);
      t.scale(w, h);
      g.drawImage(sprite, t, null);
    }
    if (fill != null) {
      g.setColor(fill);
      g.fillRect(x - zoomX, y - zoomY, (int) w, (int) h);
    }
  }
  
  
  public Object topObjectAt(Tile at) {
    if (at == null) return null;
    final Pick <Object> pick = new Pick();
    for (Element e : at.inside()) if (e.isPerson()) {
      final Person p = (Person) e;
      pick.compare(p, p.health.conscious() ? 1.5f : 1);
    }
    pick.compare(at.topInside(), 0.5f);
    pick.compare(at, 0.1f);
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








