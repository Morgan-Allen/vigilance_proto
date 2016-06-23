

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
  
  
  public Image missileSprite() {
    return null;
  }
  
  
  public String describeAction(Action action, Scene scene) {
    StringBuffer s = new StringBuffer();
    s.append(basis.description);
    if (action != null && action.volley() != null) {
      Volley v = action.volley();
      int minDamage = Nums.floor(v.minDamage());
      int maxDamage = Nums.ceil (v.maxDamage());
      s.append("\n  "+minDamage+"-"+maxDamage+" damage");
      int hitPercent = Nums.clamp(v.accuracyPercent, 101);
      s.append("\n  "+hitPercent+"% to hit (armour "+v.hitsArmour+")");
    }
    return s.toString();
  }
  
  
  //  TODO:  Move this out to the 'view' package?
  
  public void renderMissile(Action action, Scene s, Graphics2D g) {
    renderMissile(action, s, missileSprite(), g);
  }
  
  
  protected void renderMissile(
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
  
  
  protected void renderBeam(
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
  
  
  protected void dodgePosition(Person self, Object from, float scale) {
    Scene s = self.currentScene();
    Tile atS = self.currentTile(), atH = s.tileUnder(from);
    Vec2D diff = new Vec2D(atS.x - atH.x, atS.y - atH.y);
    diff.normalise().scale(Nums.clamp(scale, -0.49f, 0.49f));
    self.setExactPosition(s, atS.x + diff.x, atS.y + diff.y, 0);
  }
  
}






