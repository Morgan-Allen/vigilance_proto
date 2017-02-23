

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class PropEffect extends Prop {
  
  
  final public Action source;
  public int turnsLeft = -1;
  
  
  public PropEffect(PropType kind, Action source, World world) {
    super(kind, world);
    if (! kind.effect()) I.complain("PROP TYPE MUST BE EFFECT");
    this.source = source;
  }
  
  
  public PropEffect(Session s) throws Exception {
    super(s);
    source = (Action) s.loadObject();
    turnsLeft = s.loadInt();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(source);
    s.saveInt(turnsLeft);
  }
  
  
  public boolean enterScene(Scene scene, int x, int y, int facing) {
    if (! super.enterScene(scene, x, y, facing)) return false;
    scene.effectProps.add(this);
    return true;
  }
  
  
  public boolean exitScene() {
    final Scene scene = origin.scene;
    if (! super.exitScene()) return false;
    scene.effectProps.remove(this);
    return true;
  }
  
  
  protected void onTurnEnd() {
    kind().onTurnEnd(origin().scene, this);
    if (turnsLeft > 0 && --turnsLeft == 0) {
      exitScene();
    }
  }
  
}







