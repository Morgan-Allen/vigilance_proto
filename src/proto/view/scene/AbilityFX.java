

package proto.view.scene;
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
      int minDamage = Nums.floor(v.minDamage());
      int maxDamage = Nums.ceil (v.maxDamage());
      
      s.append("\n  "+minDamage+"-"+maxDamage+" damage");
      int hitPercent = Nums.clamp(v.accuracyMargin, 101);
      s.append("\n  "+hitPercent+"% to hit (armour "+v.hitsArmour+")");
    }
    return s.toString();
  }
  
  
  public void tracePath(Tile path[], Scene s, Graphics2D g) {
    final int TS = SceneView.TILE_SIZE;
    final SceneView SV = s.view();
    int offX = SV.zoomX, offY = SV.zoomY;
    
    g.setStroke(new BasicStroke(2));
    g.setColor(new Color(1, 1, 1, 0.5f));
    
    for (int i = 0; i < path.length - 1; i++) {
      Tile a = path[i], b = path[i + 1];
      g.drawLine(
        (a.x * TS) - offX,
        (a.y * TS) - offY,
        (b.x * TS) - offX,
        (b.y * TS) - offY
      );
    }
  }
  
  
  public void renderMissile(Action action, Scene s, Graphics2D g) {
    renderMissile(action, s, basis.missileSprite(), g);
  }
  
  
  public void renderMissile(
    Action action, Scene s, Image sprite, Graphics2D g
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
    
    s.view().renderAt(pX, pY, 0.5f, 0.5f, sprite, null, g);
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
    s.view().renderAt(px, py, r2, r2, sprite, null, g);
  }
  
  
  public void dodgePosition(Person self, Object from, float scale) {
    Scene s = self.currentScene();
    Tile atS = self.currentTile(), atH = s.tileUnder(from);
    Vec2D diff = new Vec2D(atS.x - atH.x, atS.y - atH.y);
    diff.normalise().scale(Nums.clamp(scale, -0.49f, 0.49f));
    self.setExactPosition(s, atS.x + diff.x, atS.y + diff.y, 0);
  }
  
}






