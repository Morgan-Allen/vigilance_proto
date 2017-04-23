

package proto.view.scene;
import proto.common.Kind;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Color;
import java.awt.BasicStroke;



public class AbilityFX {
  
  
  final Ability basis;
  
  
  public AbilityFX(Ability basis) {
    this.basis = basis;
  }
  
  
  public String describeAction(Action action, Scene scene) {
    StringBuffer s = new StringBuffer();
    s.append(basis.description);
    
    if (action != null && action.volley() != null) {
      Volley v = action.volley();
      v.calcMargins();
      int minDamage = Nums.floor(v.minDamage());
      int maxDamage = Nums.ceil (v.maxDamage());
      
      s.append("\n  "+minDamage+"-"+maxDamage+" damage");
      s.append(" (target armour "+v.hitsArmour+")");
      
      s.append("\n  "+v.accuracyMargin+"% to hit");
      for (Volley.Modifier m : v.extractModifiers(
        v.selfAccuracy, v.hitsDefence
      )) {
        int aimMult = m.stat == v.selfAccuracy ? 1 : -1;
        s.append("\n    "+m.source+": "+I.signNum(m.modValue * aimMult));
      }
      
      s.append("\n  "+v.critPercent+"% to crit");
      for (Volley.Modifier m : v.extractModifiers(v.critPercent)) {
        s.append("\n    "+m.source+": "+I.signNum(m.modValue));
      }
      
      s.append("\n  "+v.stunPercent+"% stun damage");
      for (Volley.Modifier m : v.extractModifiers(v.stunDamage)) {
        s.append("\n    "+m.source+": "+I.signNum(m.modValue));
      }
      
      s.append("\n");
    }
    
    //  TODO:  This needs to be worked on as a separate feature.
    /*
    else if (action != null && ! action.used.ranged()) {
      Tile path[] = action.path();
      Person.Side foes = Person.Side.VILLAINS;
      float hideChance = action.acting.stats.hidingRange() / 10f;
      
      //  TODO:  MOVE THIS TO THE ACTIONS CLASS!
      float sumFog = 0;
      int sightChance = 0;
      for (Tile t : path) {
        float fog = scene.vision.fogAt(t, foes);
        sumFog += 1 - fog;
        sightChance += 20 * fog * Nums.clamp(fog - hideChance, 0, 1);
      }
      sumFog /= path.length;
      sightChance = Nums.clamp(sightChance, 101);
      
      s.append("\n  Average enemy fog: "+(int) (sumFog * 100)+"%");
      s.append("\n  Chance of detection: "+sightChance+"%");
    }
    //*/
    
    return s.toString();
  }
  
  
  public void previewAction(Action action, Scene s, Graphics2D g) {
    //
    //  
    if (! Visit.empty(action.path())) {
      previewPath(action.path(), s, g);
    }
    //
    //  This is used to draw elliptical arcs for grenades and throwables:
    //  TODO:  Codify this a little more neatly...
    Volley volley = action.volley();
    ItemType weapon = volley == null ? null : volley.weaponType();
    if (weapon != null && weapon.subtype() == Kind.SUBTYPE_WING_BLADE) {
      previewCurvedTrajectory(action, s, g);
    }
    //
    //  
    else if (action.used.ranged()) {
      previewTrajectory(action, s, g);
    }
  }
  
  
  public void previewPath(Tile path[], Scene s, Graphics2D g) {
    g.setStroke(new BasicStroke(2));
    g.setColor(new Color(1, 1, 1, 0.5f));
    
    for (int i = 0; i < path.length - 1; i++) {
      Tile a = path[i], b = path[i + 1];
      drawLine(a, b, s, g);
    }
  }
  
  
  public void previewTrajectory(Action a, Scene s, Graphics2D g) {
    Tile t = s.tileUnder(a.target), o = a.acting.currentTile();
    Tile v = s.vision.bestVantage(a.acting, t, false);
    if (v == null) return;
    
    g.setStroke(new BasicStroke(2));
    g.setColor(new Color(1, 1, 0, 0.5f));
    drawLine(o, v, s, g);
    drawLine(v, t, s, g);
  }
  
  
  public void previewCurvedTrajectory(Action a, Scene s, Graphics2D g) {
    Tile t = s.tileUnder(a.target), v = a.acting.currentTile();
    SceneView SV = s.view();
    int TS = SV.tileSize;
    int offX = SV.zoomX, offY = SV.zoomY;
    int x = (v.x * TS) - offX, y = (v.y * TS) - offY;
    int w = (t.x - v.x) * TS, h = (t.y - v.y) * TS;
    
    int numPoints = Nums.max(10, (int) s.distance(t, v));
    Vec2D between = new Vec2D(w, h), perp = between.perp(null);
    perp.normalise().scale(between.length() / 5f);
    if (perp.y > 0) perp.scale(-1);
    Vec2D last = new Vec2D(x + w, y + h);
    
    g.setStroke(new BasicStroke(2));
    g.setColor(new Color(1, 1, 0, 0.5f));
    for (int n = numPoints + 1; n-- > 0;) {
      float mid = n * 1f / numPoints;
      
      Vec2D point = new Vec2D(between).scale(mid);
      mid *= 4 * (1 - mid);
      point.x += x + (perp.x * mid);
      point.y += y + (perp.y * mid);
      
      g.drawLine((int) last.x, (int) last.y, (int) point.x, (int) point.y);
      last = point;
    }
  }
  
  
  private void drawLine(Tile a, Tile b, Scene s, Graphics2D g) {
    SceneView SV = s.view();
    int TS = SV.tileSize;
    int offX = SV.zoomX, offY = SV.zoomY;
    g.drawLine(
      (a.x * TS) - offX,
      (a.y * TS) - offY,
      (b.x * TS) - offX,
      (b.y * TS) - offY
    );
  }
  
  
  
  /**  Rendering of various special FX during action execution-
    */
  public void renderMissile(
    Action action, Scene s, Image sprite, float size, Graphics2D g
  ) {
    if (sprite == null) return;
    Person using = action.acting;
    Tile target = s.tileUnder(action.target);
    float progress = action.progress();
    
    Vec3D exactPos = using.exactPosition();
    float pX = exactPos.x * (1 - progress);
    float pY = exactPos.y * (1 - progress);
    pX += target.x * progress;
    pY += target.y * progress;
    
    //  TODO:  Include rotation here!
    s.view().renderSprite(pX, pY, size, size, 0, sprite, g);
  }
  
  
  public void renderBeam(
    Action action, Scene s,
    Color beamTone, Color coreTone, int beamWide, Graphics2D g
  ) {
    if (beamTone == null || coreTone == null) return;
    Person using = action.acting;
    Tile target = s.tileUnder(action.target);
    
    Coord orig = s.view().screenCoord(using.currentTile());
    Coord dest = s.view().screenCoord(target);
    
    g.setColor(beamTone);
    g.setStroke(new BasicStroke(beamWide * 4));
    g.drawLine(orig.x, orig.y, dest.x, dest.y);
    
    g.setColor(coreTone);
    g.setStroke(new BasicStroke(beamWide * 2));
    g.drawLine(orig.x, orig.y, dest.x, dest.y);
  }
  
  
  public void renderBurst(
    Object at, Scene s, float radius, Image sprite, Graphics2D g
  ) {
    Tile under = s.tileUnder(at);
    float px = under.x - radius, py = under.y - radius, r2 = radius * 2;
    s.view().renderSprite(px, py, r2, r2, 0, sprite, g);
  }
  
  
  public void dodgePosition(Person self, Object from, float scale) {
    Scene s = self.currentScene();
    Tile atS = self.currentTile(), atH = s.tileUnder(from);
    Vec2D diff = new Vec2D(atS.x - atH.x, atS.y - atH.y);
    diff.normalise().scale(Nums.clamp(scale, -0.49f, 0.49f));
    self.setExactPosition(s, atS.x + diff.x, atS.y + diff.y, 0);
  }
  
}






