

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
  
  final static Color SCALE[] = new Color[10];
  final static Color ENEMY[] = new Color[10];
  final static Color PALES[] = new Color[10];
  static {
    for (int n = 10; n-- > 0;) {
      SCALE[n] = new Color(0, 0, 0, n / 10f);
      ENEMY[n] = new Color(1, 0, 0, n / 50f);
      PALES[n] = new Color(1, 1, 1, n / 10f);
    }
  }
  
  
  final ActionsView actionsView;
  
  Vec3D zoomPoint = new Vec3D();
  Tile hoverTile, zoomTile;
  int zoomX, zoomY;
  Person selectedPerson;
  
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
    zoomPoint.loadFrom(s.input());
    setZoomPoint(zoomPoint);
    zoomX    = s.loadInt();
    zoomY    = s.loadInt();
    selectedPerson = (Person) s.loadObject();
    actionsView.selectAbility = (Ability) s.loadObject();
    actionsView.selectAction  = (Action ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    zoomPoint.saveTo(s.output());
    s.saveInt   (zoomX   );
    s.saveInt   (zoomY   );
    s.saveObject(selectedPerson);
    s.saveObject(actionsView.selectAbility);
    s.saveObject(actionsView.selectAction );
  }
  
  
  
  /**  Public utility methods-
    */
  public void setZoomPoint(Tile t) {
    setZoomPoint(new Vec3D(t.x, t.y, 0));
  }
  
  
  public void setZoomPoint(Vec3D pos) {
    zoomPoint.setTo(pos);
    Scene scene = mainView.world().activeScene();
    if (scene != null) zoomTile = scene.tileAt(pos.x, pos.y);
  }
  
  
  public void setSelection(Person acting, boolean keepActiveAbility) {
    final Person old = selectedPerson;
    this.selectedPerson = acting;
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
    return selectedPerson;
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
    //
    //  Update camera information first-
    if (zoomTile != null) {
      zoomX = (int) (zoomPoint.x * TILE_SIZE);
      zoomY = (int) (zoomPoint.y * TILE_SIZE);
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
        renderColor(c.x, c.y, 1, 1, true, warns, g);
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
      renderPerson(p, scene, surface, g);
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
      float fogAlpha = 1f - tileVisibilityKluge(t, Person.Side.HEROES);
      if (GameSettings.debugScene) fogAlpha /= 3;
      Color black = SCALE[Nums.clamp((int) (fogAlpha * 10), 10)];
      renderColor(c.x, c.y, 1, 1, true, black, g);
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
        final Image hoverBox = mainView.selectCircle;
        renderSprite(hoverTile.x, hoverTile.y, 1, 1, 0, hoverBox, g);
      }
      
      final Ability ability = actionsView.selectAbility;
      if (ability == Common.MOVE && ! hoverTile.blocked()) {
        renderCoverIndicators(hoverTile, scene, surface, g);
      }
      
      Object hovered = topObjectAt(hoverTile);
      boolean canDoAction = false;
      
      if (actionsView.previewActionDelivery(hovered, hoverTile, surface, g)) {
        canDoAction = true;
      }
      
      if (surface.mouseClicked()) {
        if (done == null && canDoAction) {
          selectedPerson.actions.assignAction(actionsView.selectAction);
          scene.pushNextAction(actionsView.selectAction);
          actionsView.clearSelection();
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
    //  We can track the position of any enemy movement in sight-
    /*
    Action baseAction = scene.actionStack().last();
    Person acting = baseAction == null ? null : baseAction.acting;
    if (
      acting != null && (true || ! acting.isHero()) &&
      scene.vision.fogAt(acting.currentTile(), Person.Side.HEROES) > 0
    ) {
      setZoomPoint(acting.exactPosition());
    }
    //*/
    //
    //  Finally, some supplementary debugging-related checks:
    if (
      GameSettings.debugLineSight && I.used60Frames &&
      zoomTile != null && zoomTile == hoverTile &&
      selectedPerson != null && zoomTile != selectedPerson.currentTile()
    ) {
      Tile.printWallsMask(scene);
      scene.vision.degreeOfSight(selectedPerson, zoomTile, true);
      scene.vision.coverFor(zoomTile, selectedPerson.currentTile(), true);
    }
    return true;
  }
  
  
  void renderPerson(Person p, Scene s, Surface surface, Graphics2D g) {
    //
    //  Render the person themselves-
    Vec3D pos = p.exactPosition();
    p.renderTo(s, this, surface, g);
    //
    //  Then their health-bar/s:
    Color               teamColor = Color.GREEN;
    if (p.isHero    ()) teamColor = Color.BLUE;
    if (p.isCriminal()) teamColor = Color.RED;
    float healthLevel = p.health.healthLevel();
    float stunLevel   = 1 - p.health.harmToStunRatio();
    Color pale = PALES[(int) (stunLevel * 9.9f)];
    renderColor(pos.x, pos.y + 0.9f, 1, 0.1f, false, Color.BLACK, g);
    renderColor(pos.x, pos.y + 0.9f, 1, 0.1f, false, pale, g);
    renderColor(pos.x, pos.y + 0.9f, healthLevel, 0.1f, false, teamColor, g);
    renderString(pos.x, pos.y + 0.5f, p.name(), Color.WHITE, g);
    //
    //  And indicators for current conditions:
    float offC = 0;
    for (Ability c : p.stats.allConditions()) {
      renderSprite(pos.x - 0.5f + offC, pos.y - 0.5f, 0.2f, 0.2f, 0, c.icon, g);
      offC += 0.2f;
    }
    //
    //  And a selection circle if needed...
    if (p == selectedPerson) {
      final Image hoverBox = mainView.selectCircle;
      renderSprite(pos.x, pos.y, 1, 1, 0, hoverBox, g);
    }
  }
  
  
  void renderCoverIndicators(Tile t, Scene s, Surface surface, Graphics2D g) {
    for (int dir : T_ADJACENT) {
      Image img = null;
      int coverLevel = t.coverVal(dir);
      if (coverLevel == Kind.BLOCK_PARTIAL) img = COVER_PARTIAL;
      if (coverLevel == Kind.BLOCK_FULL   ) img = COVER_FULL   ;
      if (img == null) continue;
      
      float scale = 0.66f;
      renderSprite(
        t.x + (T_X[dir] * scale * 1),
        t.y + (T_Y[dir] * scale * 1),
        scale, scale, (dir + 2) * 45, img, g
      );
    }
  }
  
  
  private float tileVisibilityKluge(Tile tile, Person.Side side) {
    //  
    //  TODO:  Like the names says, this is a hack necessitated by the limits
    //  of the Java2D API's rendering of images (i.e, fog has to be drawn on
    //  top, rather than as a form of tinting for specific sprites.)  Remove
    //  as soon as possible once you migrate to a proper graphics engine.
    
    Scene scene = tile.scene;
    float maxSight = scene.vision.fogAt(tile, side);
    if (maxSight > 0 || ! tile.opaque()) return maxSight;
    
    for (int dir : T_ADJACENT) {
      if (tile.hasWall(dir)) continue;
      
      Tile near = scene.tileAt(tile.x + T_X[dir], tile.y + T_Y[dir]);
      if (near == null) continue;
      if (near.hasWall((dir + 4) % 8)) continue;
      
      maxSight = Nums.max(maxSight, scene.vision.fogAt(near, side));
    }
    return maxSight;
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
    if (sprite == null) return;
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
    float px, float py, float w, float h, boolean centre,
    Color fill, Graphics2D g
  ) {
    int x, y;
    if (! centre) { px -= 0.5f; py -= 0.5f; }
    x = vx + (int) ((px - (centre ? (w / 2) : 0)) * TILE_SIZE);
    y = vy + (int) ((py - (centre ? (h / 2) : 0)) * TILE_SIZE);
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








