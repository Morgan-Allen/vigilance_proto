

package proto.view;
import proto.util.*;

import java.awt.Graphics2D;



public abstract class UINode {
  
  
  final WorldView mainView;
  final UINode parent;
  final Box2D relBounds = new Box2D();
  final List <UINode> kids = new List();
  
  public boolean visible = true;
  
  protected int vx, vy, vw, vh;
  protected boolean clipContent = false;
  
  
  UINode() {
    this.parent = null;
    this.mainView = (WorldView) this;
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
    if (kid.parent != this) return;
    kids.toggleMember(kid, is);
  }
  
  
  protected void addChildren(UINode... kids) {
    for (UINode k : kids) setChild(k, true);
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
    
    renderTo(surface, g);
    
    for (UINode kid : kids) {
      kid.updateAndRender(surface, g);
    }
  }
  
  
  abstract void renderTo(Surface surface, Graphics2D g);
}








