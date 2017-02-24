

package proto.game.scene;
import proto.util.*;



public class SightLine {
  
  Tile vantage;
  Tile dest;
  Vec2D orig = new Vec2D(), line = new Vec2D();
  
  
  void setTo(Tile vantage, Tile dest) {
    this.vantage = vantage;
    this.dest = dest;
    orig.set(vantage.x + 0.5f  , vantage.y + 0.5f  );
    line.set(dest.x - vantage.x, dest.y - vantage.y);
  }
  
  
  void shrinkToDest(float length) {
    Vec2D atDest = line.add(orig, null);
    line.normalise().scale(length);
    orig.setTo(atDest.sub(line));
  }
  
}
