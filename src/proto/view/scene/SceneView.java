

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



public class SceneView extends UINode implements TileConstants {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static int
    TILE_SIZE = 64;
  final static String
    IMG_DIR = "media assets/action view/";
  
  final static Image
    COVER_PARTIAL = Kind.loadImage(IMG_DIR+"cover_partial.png"),
    COVER_FULL    = Kind.loadImage(IMG_DIR+"cover_full.png"   );
  
  
  final ActionsView actionsView;
  
  Tile zoomTile, hoverTile;
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
  
  
  public Person selectedPerson() {
    return activePerson;
  }
  
  
  public Tile zoomTile() {
    return zoomTile;
  }
  
  
  public Tile hoveredTile() {
    return hoverTile;
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
      float fog = scene.vision.fogAt(p.currentTile(), Person.Side.HEROES);
      if (fog <= 0 && ! GameSettings.debugScene) continue;
      if (p.isCivilian() || ! p.health.conscious()) continue;
      
      Vec3D exactPos = p.exactPosition();
      float sightRange = p.stats.sightRange();
      final Box2D area = new Box2D(exactPos.x, exactPos.y, 0, 0);
      area.expandBy(Nums.ceil(sightRange));
      
      for (Coord c : Visit.grid(area)) {
        Tile atG = scene.tileAt(c.x, c.y);
        if (atG == null || ! p.actions.hasSight(atG)) continue;
        
        float dist = exactPos.distance(c.x + 0.5f, c.y + 0.5f, 0) - 0.5f;
        float glare = 1 - Nums.clamp(dist / sightRange, 0, 1);
        if (glare <= 0) continue;
        Color warns = ENEMY[Nums.clamp((int) (glare * 10), 10)];
        renderColor(c.x, c.y, 1, 1, warns, g);
      }
    }
    //
    //  Render the persons themselves-
    final List <Person> personsShown = new List <Person> () {
      protected float queuePriority(Person r) {
        return r.health.conscious() ? 1 : 0;
      }
    };
    for (Person p : scene.allPersons()) {
      float fog = scene.vision.fogAt(p.currentTile(), Person.Side.HEROES);
      if (fog <= 0 && ! GameSettings.debugScene) continue;
      personsShown.add(p);
    }
    personsShown.queueSort();
    
    for (Person p : personsShown) {
      p.renderTo(scene, this, surface, g);
    }
    for (Person p : personsShown) {
      Color               teamColor = Color.GREEN;
      if (p.isHero    ()) teamColor = Color.BLUE;
      if (p.isCriminal()) teamColor = Color.RED;
      Vec3D pos         = p.exactPosition();
      float healthLevel = p.health.healthLevel();
      float stunLevel   = 1 - p.health.harmToStunRatio();
      Color pale = PALES[(int) (stunLevel * 9.9f)];
      //
      //  Including their health-bars:
      renderColor(pos.x, pos.y + 0.9f, 1, 0.1f, Color.BLACK, g);
      renderColor(pos.x, pos.y + 0.9f, 1, 0.1f, pale, g);
      renderColor(pos.x, pos.y + 0.9f, healthLevel, 0.1f, teamColor, g);
      renderString(pos.x, pos.y + 0.5f, p.name(), Color.WHITE, g);
      
      if (p == activePerson) {
        renderSprite(pos.x, pos.y, 1, 1, 0, hoverBox, g);
      }
    }
    //
    //  Then render any action underway, and any temporary FX-
    if (done != null && done.progress() >= 0) {
      done.used.renderUsageFX(done, scene, g);
    }
    for (TempFX fx : tempFX) {
      float fSize = fx.size * 2;
      renderSprite(fx.pos.x, fx.pos.y, fSize, fSize, 0, fx.sprite, g);
      fx.framesLeft--;
      if (fx.framesLeft <= 0) tempFX.remove(fx);
    }
    //
    //  Then render our own fog on top of all objects-
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      Tile t = scene.tileAt(c.x, c.y);
      float fogAlpha = 1f - scene.vision.fogAt(t, Person.Side.HEROES);
      if (GameSettings.debugScene) fogAlpha /= 3;
      Color black = SCALE[Nums.clamp((int) (fogAlpha * 10), 10)];
      renderColor(c.x, c.y, 1, 1, black, g);
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
    hoverTile = hasFocus ? scene.tileAt(hoverX, hoverY) : null;
    
    if (hoverTile != null) {
      if (! hoverTile.blocked()) {
        renderSprite(hoverTile.x, hoverTile.y, 1, 1, 0, hoverBox, g);
      }
      
      final Ability ability = actionsView.selectAbility;
      if (ability == Common.MOVE && ! hoverTile.blocked()) {
        renderCoverIndicators(hoverTile, surface, g);
      }
      
      Object hovered = topObjectAt(hoverTile);
      boolean canDoAction = false;
      
      if (actionsView.previewActionDelivery(hovered, hoverTile, surface, g)) {
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
          else setZoomPoint(hoverTile);
        }
      }
    }
    //
    //  Finally, some supplementary debugging-related checks:
    if (
      GameSettings.debugLineSight && I.used60Frames &&
      zoomTile != null && zoomTile == hoverTile &&
      activePerson != null && zoomTile != activePerson.currentTile()
    ) {
      Tile.printWallsMask(scene);
      scene.vision.degreeOfSight(activePerson, zoomTile, true);
      scene.vision.coverFor(zoomTile, activePerson.currentTile(), true);
    }
    return true;
  }
  
  
  void renderCoverIndicators(Tile hovered, Surface surface, Graphics2D g) {
    for (int dir : T_ADJACENT) {
      Image img = null;
      int coverLevel = hovered.coverVal(dir);
      if (coverLevel == Kind.BLOCK_PARTIAL) img = COVER_PARTIAL;
      if (coverLevel == Kind.BLOCK_FULL   ) img = COVER_FULL   ;
      if (img == null) continue;
      
      float scale = 0.66f;
      renderSprite(
        hovered.x + (T_X[dir] * scale * 1),
        hovered.y + (T_Y[dir] * scale * 1),
        scale, scale, (dir + 2) * 45, img, g
      );
    }
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
  
  
  public void renderSprite(
    float px, float py, float w, float h, float angle,
    Image sprite, Graphics2D g
  ) {
    //
    //  Firstly, determine the centre of the tile in the visual field:
    float x = vx + (px * TILE_SIZE);
    float y = vy + (py * TILE_SIZE);
    //
    //  You want to centre the image on the centre of the tile, and rotate
    //  around that point.  However, the default image-rendering routines in
    //  java will only render from the image's top-left corner and rotate from
    //  there, so we have to offset the image to compensate.
    final float
      offAngle = Nums.atan2(-w, -h) - Nums.toRadians(angle),
      offDist  = Nums.sqrt((w * w) + (h * h)) / 2,
      offX     = Nums.sin(offAngle) * offDist,
      offY     = Nums.cos(offAngle) * offDist;
    x += offX * TILE_SIZE;
    y += offY * TILE_SIZE;
    //
    //  We can then adjust the scale of the image to stretch out the sprite
    //  correctly, apply the needed transformations, and render:
    w *= TILE_SIZE * 1f / sprite.getWidth (null);
    h *= TILE_SIZE * 1f / sprite.getHeight(null);
    AffineTransform t = new AffineTransform();
    t.translate(x - zoomX, y - zoomY);
    t.rotate(Nums.toRadians(angle));
    t.scale(w, h);
    g.drawImage(sprite, t, null);
  }
  
  
  public void renderColor(
    float px, float py, float w, float h, Color fill, Graphics2D g
  ) {
    int x, y;
    x = vx + (int) ((px - (w / 2)) * TILE_SIZE);
    y = vy + (int) ((py - (h / 2)) * TILE_SIZE);
    w *= TILE_SIZE;
    h *= TILE_SIZE;
    g.setColor(fill);
    g.fillRect(x - zoomX, y - zoomY, (int) w, (int) h);
  }
  
  
  public Object topObjectAt(Tile at) {
    if (at == null) return null;
    final Pick <Object> pick = new Pick();
    for (Element e : at.inside()) {
      if (e.isPerson()) {
        final Person p = (Person) e;
        pick.compare(p, p.health.conscious() ? 20 : 15);
      }
      if (e.isProp()) {
        final Prop p = (Prop) e;
        pick.compare(p, (p.blockLevel() + 1) * (p.kind().thin() ? 0 : 1));
      }
    }
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








