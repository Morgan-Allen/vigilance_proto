

package proto.view.common;
import proto.util.*;
import java.awt.Graphics2D;



public class UINode {
  
  
  final protected MainView mainView;
  
  final public Box2D relBounds = new Box2D();
  public boolean visible = true;
  
  protected UINode parent;
  final List <UINode> kids = new List();
  protected int vx, vy, vw, vh;
  protected boolean clipContent = false;
  
  
  UINode() {
    this.parent   = null;
    this.mainView = (MainView) this;
  }
  
  
  protected UINode(UINode parent) {
    this.parent   = parent;
    this.mainView = parent.mainView;
  }
  
  
  protected UINode(UINode parent, Box2D bounds) {
    this(parent);
    relBounds.setTo(bounds);
  }
  
  
  public void setChild(UINode kid, boolean is) {
    if (kid.parent != this) kid.parent.setChild(kid, false);
    kids.toggleMember(kid, is);
    kid.parent = this;
  }
  
  
  public void addChildren(UINode... kids) {
    for (UINode k : kids) setChild(k, true);
  }
  
  
  public void renderNow(Surface surface, Graphics2D g) {
    updateAndRender(surface, g);
  }
  
  
  protected void updateAndRender(Surface surface, Graphics2D g) {
    if (! visible) return;
    
    final Box2D b = this.relBounds;
    vx = (int) b.xpos();
    vy = (int) b.ypos();
    vw = (int) b.xdim();
    vh = (int) b.ydim();
    
    if (parent != null) {
      vx += parent.vx;
      vy += parent.vy;
    }
    
    if (clipContent) {
      g = (Graphics2D) g.create();
      g.setClip(vx, vy, vw, vh);
    }
    
    if (! renderTo(surface, g)) return;
    
    for (UINode kid : kids) {
      kid.updateAndRender(surface, g);
    }
    
    renderAfterKids(surface, g);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    return true;
  }
  
  
  protected void renderAfterKids(Surface surface, Graphics2D g) {
    return;
  }
}








