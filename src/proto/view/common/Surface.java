

package proto.view.common;
import proto.*;
import proto.game.world.*;
import proto.util.*;
import proto.common.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import static java.awt.RenderingHints.*;



public class Surface extends JPanel implements
  MouseListener, MouseMotionListener, KeyListener
{
  
  final RunGame game;
  
  private boolean mouseDown, lastClick, mouseClicked;
  private int mouseX = -1, mouseY = -1, lastX, lastY, moveX, moveY;
  private Object mouseFocus = null, lastFocus = null;
  
  private Batch <Character> pressed = new Batch();
  private long numPaints = 0;
  
  
  
  public Surface(RunGame runGame) {
    this.game = runGame;
    this.setPreferredSize(new Dimension(1200, 750));
  }
  
  
  
  /**  Utility methods for input queries-
    */
  public int mouseX() { return mouseX; }
  public int mouseY() { return mouseY; }
  public int moveX() { return moveX; }
  public int moveY() { return moveY; }
  public boolean mouseDown() { return mouseDown; }
  public boolean mouseClicked() { return mouseClicked; }
  
  
  public boolean recordHover(
    int x, int y, int w, int h, Object hovered, UINode bounds
  ) {
    if (hovered == null || ! checkMouseInBound(bounds)) return false;
    if (mouseX > x && mouseX <= x + w && mouseY > y && mouseY <= y + h) {
      mouseFocus = hovered;
      return true;
    }
    return false;
  }
  
  
  private boolean checkMouseInBound(UINode from) {
    boolean okay = true;
    for (UINode node = from; node != null && okay; node = node.parent) {
      if (! node.clipContent) continue;
      if (mouseX < node.vx || mouseX > node.vx + node.vw) okay = false;
      if (mouseY < node.vy || mouseY > node.vy + node.vh) okay = false;
    }
    return okay;
  }
  
  
  public boolean wasHovered(Object focus) {
    if (lastFocus == null || focus == null) return false;
    return lastFocus.equals(focus);
  }
  
  
  public boolean tryHover(UINode node) {
    return tryHover(node, node);
  }
  
  
  public boolean tryHover(UINode node, Object hovered) {
    return tryHover(node.vx, node.vy, node.vw, node.vh, hovered, node);
  }
  
  
  public boolean tryHover(
    int x, int y, int w, int h, UINode bounds
  ) {
    return tryHover(x, y, w, h, bounds, bounds);
  }
  
  
  public boolean tryHover(
    int x, int y, int w, int h, Object hovered, UINode bounds
  ) {
    if (! recordHover(x, y, w, h, hovered, bounds)) return false;
    return wasHovered(hovered);
  }
  
  
  public boolean isPressed(char k) {
    for (Character c : pressed) if (c == k) return true;
    return false;
  }
  
  
  public Object lastFocus() {
    return lastFocus;
  }
  
  
  
  /**  Root rendering method/s-
    */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    World world = game.world();
    
    final Table <Key, ?> hints = Table.make(
      KEY_TEXT_ANTIALIASING  , VALUE_TEXT_ANTIALIAS_ON,
      KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_QUALITY,
      KEY_INTERPOLATION      , VALUE_INTERPOLATION_BILINEAR
    );
    g2d.setRenderingHints(hints);
    I.used60Frames = (numPaints++ % 60) == 0;
    
    this.moveX = (mouseX == -1) ? 0 : (lastX - mouseX);
    this.moveY = (mouseY == -1) ? 0 : (lastY - mouseY);
    
    if (world != null) {
      MainView view = world.view();
      this.lastFocus  = mouseFocus;
      this.mouseFocus = null;
      view.updateAndRender(this, g2d);
    }
    
    this.mouseClicked = this.lastClick;
    this.lastClick    = false;
    this.lastX = mouseX;
    this.lastY = mouseY;
    pressed.clear();
  }
  
  
  
  /**  Basic event listeners-
    */
  public void mouseDragged(MouseEvent e) {
    this.mouseX = e.getX();
    this.mouseY = e.getY();
  }
  
  
  public void mouseMoved(MouseEvent e) {
    this.mouseX = e.getX();
    this.mouseY = e.getY();
  }
  
  
  public void mouseClicked(MouseEvent e) {
    this.lastClick = true;
  }
  
  
  public void mousePressed(MouseEvent e) {
    this.mouseDown = true;
  }
  
  
  public void mouseReleased(MouseEvent e) {
    this.mouseDown = false;
  }
  
  
  public void mouseEntered(MouseEvent e) {
    return;
  }
  
  
  public void mouseExited(MouseEvent e) {
    return;
  }
  
  
  public void keyTyped(KeyEvent e) {
    pressed.add(e.getKeyChar());
  }
  
  
  public void keyPressed(KeyEvent e) {
    return;
  }
  
  
  public void keyReleased(KeyEvent e) {
    return;
  }
}