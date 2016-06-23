

package proto.view.common;
import proto.util.*;

import java.awt.Graphics2D;



public abstract class UINode {
  
  
  final protected MainView mainView;
  final protected UINode parent;
  
  final public Box2D relBounds = new Box2D();
  public boolean visible = true;
  
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
  
  
  protected void setChild(UINode kid, boolean is) {
    if (kid.parent != this) {
      I.complain("Must have matching parent: "+kid+", "+kid.parent+" vs "+this);
    }
    kids.toggleMember(kid, is);
  }
  
  
  protected void addChildren(UINode... kids) {
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
      //  TODO:  IMPLEMENT THIS!
      //g = g.create(vx, vy, vw, vh);
    }
    
    if (! renderTo(surface, g)) return;
    
    for (UINode kid : kids) {
      kid.updateAndRender(surface, g);
    }
  }
  
  
  protected abstract boolean renderTo(Surface surface, Graphics2D g);
}








