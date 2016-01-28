


package proto.view;
import proto.*;
import proto.common.Session;
import proto.game.scene.Ability;
import proto.game.scene.Action;
import proto.game.scene.Equipped;
import proto.game.scene.Kind;
import proto.game.scene.Person;
import proto.game.scene.Prop;
import proto.game.scene.Scene;
import proto.game.scene.Tile;
import proto.game.world.Nation;
import proto.game.world.World;
import proto.util.*;

import java.awt.*;




public class SceneView {
  
  final static int
    TILE_SIZE = 64;
  final static String IMG_DIR = "media assets/scene view/";
  
  Scene scene;
  
  Tile zoomTile;
  int zoomX, zoomY;
  Person  selected;
  Ability selectAbility;
  Action  selectAction;
  
  Image hoverBox;
  
  
  public SceneView(Scene scene) {
    this.scene = scene;
    hoverBox = Kind.loadImage(IMG_DIR+"select_circle.png");
  }
  
  
  public void loadState(Session s) throws Exception {
    zoomTile      = (Tile) s.loadObject();
    zoomX         = s.loadInt();
    zoomY         = s.loadInt();
    selected      = (Person) s.loadObject();
    selectAbility = (Ability) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(zoomTile);
    s.saveInt   (zoomX);
    s.saveInt   (zoomY);
    s.saveObject(selected);
    s.saveObject(selectAbility);
  }
  
  
  public void setZoomPoint(Tile t) {
    this.zoomTile = t;
  }
  
  
  public void setSelection(Person acting, boolean keepActiveAbility) {
    final Person old = selected;
    this.selected = acting;
    if (acting != old || ! keepActiveAbility) this.selectAbility = null;
  }
  
  
  public void setActiveAbility(Ability ability) {
    this.selectAbility = ability;
  }
  

  void renderTo(Surface surface, Graphics2D g) {
    int size = scene.size();
    Action done = scene.currentAction();
    Batch <Person> visible = new Batch <Person> ();
    
    //
    //  Update camera information first-
    if (zoomTile != null) {
      zoomX = zoomTile.x * TILE_SIZE;
      zoomY = zoomTile.y * TILE_SIZE;
    }
    zoomX -= surface.getWidth () / 2;
    zoomY -= surface.getHeight() / 2;
    
    //
    //  Then, render any props, persons, or special FX-
    for (Prop prop : scene.props()) {
      Tile t = prop.origin();
      renderAt(t.x, t.y, prop.kind(), g);
    }
    for (Person p : scene.persons()) {
      if (scene.fogAt(p.location(), Person.Side.HEROES) <= 0) continue;
      Vec3D pos = p.exactPosition();
      renderAt(pos.x, pos.y, p.kind(), g);
      visible.add(p);
    }
    for (Person p : visible) {
      Color               teamColor = Color.GREEN;
      if (p.isHero    ()) teamColor = Color.BLUE;
      if (p.isCriminal()) teamColor = Color.RED;
      Vec3D pos         = p.exactPosition();
      float maxHealth   = p.maxHealth();
      float tireLevel   = 1f - (p.injury() / maxHealth);
      float healthLevel = 1f - ((p.injury() + p.fatigue()) / maxHealth);
      
      renderAt(pos.x, pos.y + 0.9f, 1, 0.1f, null, Color.BLACK, g);
      renderAt(pos.x, pos.y + 0.9f, tireLevel, 0.1f, null, Color.WHITE, g);
      renderAt(pos.x, pos.y + 0.9f, healthLevel, 0.1f, null, teamColor, g);
      renderString(pos.x, pos.y + 0.5f, p.name(), Color.WHITE, g);
      
      if (p == selected) {
        renderAt(pos.x, pos.y, 1, 1, hoverBox, null, g);
      }
    }
    if (done != null && done.progress() >= 0) {
      done.used.renderMissile(done, scene, g);
    }
    
    //
    //  Then render fog on top of all objects-
    final Color SCALE[] = new Color[10];
    for (int n = 10; n-- > 0;) {
      SCALE[n] = new Color(0, 0, 0, n / 20f);
    }
    for (Coord c : Visit.grid(0, 0, size, size, 1)) {
      Tile t = scene.tileAt(c.x, c.y);
      float fogAlpha = 1f - scene.fogAt(t, Person.Side.HEROES);
      Color black = SCALE[Nums.clamp((int) (fogAlpha * 10), 10)];
      renderAt(c.x, c.y, 1, 1, null, black, g);
    }
    
    //
    //  If complete, display a summary of the results!
    if (scene.finished()) {
      return;
    }
    
    //
    //  Then determine what tile (and any objects above) are being selected-
    int HT = TILE_SIZE / 2;
    int hoverX = (surface.mouseX + zoomX + HT) / TILE_SIZE;
    int hoverY = (surface.mouseY + zoomY + HT) / TILE_SIZE;
    Tile hoverT = scene.tileAt(hoverX, hoverY);
    
    if (hoverT != null) {
      renderAt(hoverT.x, hoverT.y, 1, 1, hoverBox, null, g);
      Object hovered = scene.topObjectAt(hoverT);
      selectAction = null;
      
      //  Note:  Delayed actions only target the self, so no selection is
      //  needed here.
      if (selectAbility != null && ! selectAbility.delayed()) {
        selectAction = selectAbility.configAction(
          selected, hoverT, hovered, scene, null
        );
      }
      if (selectAbility != null && selectAction != null) {
        int costAP = selectAction.used.costAP(selectAction);
        renderString(hoverT.x, hoverT.y - 0.5f, "AP: "+costAP, Color.GREEN, g);
      }
      if (selectAbility != null && selectAction == null) {
        renderString(hoverT.x, hoverT.y - 0.5f, "X", Color.RED, g);
      }
      if (surface.mouseClicked && done == null && selectAction != null) {
        scene.queueNextAction(selectAction);
      }
      else if (surface.mouseClicked) {
        Person pickP = null;
        if (hovered instanceof Person) pickP = (Person) hovered;
        if (pickP != null) setSelection(pickP, true);
        else setZoomPoint(hoverT);
      }
    }
  }
  
  
  String description() {
    final StringBuffer s = new StringBuffer();
    final Person   p      = selected;
    final Ability  a      = selectAbility;
    final World    world  = scene.world();
    final Action   action = scene.currentAction();
    final Printout print  = world.game().print();
    
    if (scene.finished()) {
      describeEndSummary(s);
      return s.toString();
    }
    
    if (p != null) {
      s.append("\nSelection: "+p.name());
      
      int HP = (int) (p.maxHealth() - (p.injury() + p.fatigue()));
      int armour = p.stats.levelFor(Person.ARMOUR);
      s.append("\n  Health: "+HP+"/"+p.maxHealth());
      if (p.fatigue() > 0) s.append(" (Stun "+(int) p.fatigue()+")");
      s.append("\n  AP: "+p.currentAP()+"/"+p.maxAP());
      if (armour > 0) s.append("\n  Armour: "+armour);
      if (! p.alive()) s.append("\n  Dead");
      else if (! p.conscious()) s.append("\n  Unconscious");
      
      Series <Equipped> equipped = p.equipment();
      if (equipped.size() > 0) {
        s.append("\n  Equipment:");
        for (Equipped e : equipped) s.append(" "+e.name+" ("+e.bonus+")");
      }
      
      boolean canCommand =
        action == null && p.isPlayerOwned() && p.canTakeAction()
      ;
      if (canCommand) {
        s.append("\n\n  Abilities (Press 1-9):");
        char key = '1';
        for (Ability r : p.stats.listAbilities())  {
          if (r.passive()) continue;
          s.append("\n    "+r.name());
          
          boolean canUse = r.minCostAP() <= p.currentAP();
          if (canUse) s.append(" ("+key+") AP: "+r.minCostAP());
          if (print.isPressed(key) && canUse) {
            setSelection(p, false);
            setActiveAbility(r);
          }
          key++;
        }
        if (a == null) {
          s.append("\n  Pass Turn (X)");
          if (print.isPressed('x')) {
            p.setActionPoints(0);
            scene.moveToNextPersonsTurn();
          }
        }
        //  TODO:  Allow zooming to and tabbing through party members.
        //  Note:  Delayed actions only target the self, so no selection is
        //  needed, only confirmation.
        else {
          if (a.delayed()) {
            selectAction = a.configAction(p, p.location(), p, scene, null);
            s.append("\n\n"+a.describeAction(selectAction, scene));
            s.append("\n  Confirm (C)");
            if (print.isPressed('c')) {
              scene.queueNextAction(selectAction);
            }
          }
          else {
            s.append("\n\n"+a.describeAction(selectAction, scene));
            s.append("\n  Select target");
          }
          s.append("\n  Cancel (X)");
          if (print.isPressed('x')) {
            setSelection(p, false);
          }
        }
      }
    }
    
    //
    //  General options-
    s.append("\n");
    s.append("\n  Press S to save, R to reload.");
    if (print.isPressed('s')) world.performSave();
    if (print.isPressed('r')) world.reloadFromSave();
    
    return s.toString();
  }
  
  
  void describeEndSummary(StringBuffer s) {
    boolean success = scene.wasWon();
    World world = scene.world();
    Nation site = scene.site();
    Printout print = world.game().print();
    
    s.append("\nMission ");
    if (success) s.append(" Successful.");
    else s.append(" Failed.");
    
    s.append("\nTeam Status:");
    for (Person p : scene.playerTeam()) {
      s.append("\n  "+p.name());
      if (p.currentScene() != scene) {
        s.append(" (escaped)");
      }
      else if (! p.alive()) {
        s.append(" (dead)");
      }
      else if (! p.conscious()) {
        s.append(success ? " (unconscious)" : " (captive)");
      }
      else s.append(" (okay)");
    }
    s.append("\nOther Forces:");
    for (Person p : scene.othersTeam()) {
      s.append("\n  "+p.name());
      if (p.currentScene() != scene) {
        s.append(" (escaped)");
      }
      else if (! p.alive()) {
        s.append(" (dead)");
      }
      else if (! p.conscious()) {
        s.append(success ? " (captive)" : " (unconscious)");
      }
      else s.append(" (okay)");
    }
    
    final String DESC_C[] = {
      "None", "Minimal", "Medium", "Heavy", "Total"
    };
    final String DESC_G[] = {
      "None", "Few", "Some", "Many", "All"
    };
    int colIndex = Nums.clamp((int) (scene.assessCollateral() * 5), 5);
    int getIndex = Nums.clamp((int) (scene.assessGetaways  () * 5), 5);
    s.append("\nCollateral: "+DESC_C[colIndex]);
    s.append("\nGetaways: "  +DESC_G[getIndex]);
    int trustPercent = (int) (site.trustLevel() * 100);
    int crimePercent = (int) (site.crimeLevel() * 100);
    s.append("\nRegional Trust: "+trustPercent+"%");
    s.append("\nRegional Crime: "+crimePercent+"%");
    
    s.append("\n\n  Press X to exit.");
    if (print.isPressed('x')) {
      scene.endScene();
    }
  }
  
  
  void renderString(float px, float py, String s, Color c, Graphics2D g) {
    int x, y;
    x = (int) ((px - 0.50f) * TILE_SIZE);
    y = (int) ((py + 0.15f) * TILE_SIZE);
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
    x = (int) ((px - 0.5f) * TILE_SIZE);
    y = (int) ((py - 0.5f) * TILE_SIZE);
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
}


