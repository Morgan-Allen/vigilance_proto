

package proto.view;
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
  
  private boolean mouseDown, mouseClick, mouseClicked;
  private int mouseX, mouseY;
  private Object mouseFocus = null, lastFocus = null;
  
  private Batch <Character> pressed = new Batch();
  private long numPaints = 0;
  
  
  
  public Surface(RunGame runGame) {
    this.game = runGame;
    
    this.setPreferredSize(new Dimension(1200, 750));
    addMouseListener(this);
    addMouseMotionListener(this);
  }
  
  
  
  /**  Utility methods for input queries-
    */
  public int mouseX() { return mouseX; }
  public int mouseY() { return mouseY; }
  public boolean mouseDown() { return mouseDown; }
  public boolean mouseClicked() { return mouseClicked; }
  
  
  public void recordHover(int x, int y, int w, int h, Object hovered) {
    if (mouseX > x && mouseX <= x + w && mouseY > y && mouseY <= y + h) {
      mouseFocus = hovered;
    }
  }
  
  
  public boolean wasHovered(Object focus) {
    if (lastFocus == null || focus == null) return false;
    return lastFocus.equals(focus);
  }
  
  
  public boolean tryHover(UINode node) {
    return tryHover(node.vx, node.vy, node.vw, node.vh, node);
  }
  
  
  public boolean tryHover(int x, int y, int w, int h, Object hovered) {
    recordHover(x, y, w, h, hovered);
    return wasHovered(hovered);
  }
  
  
  public boolean isPressed(char k) {
    for (Character c : pressed) if (c == k) return true;
    return false;
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
    
    if (world != null) {
      WorldView view = world.view();
      this.lastFocus  = mouseFocus;
      this.mouseFocus = null;
      view.updateAndRender(this, g2d);
    }
    
    this.mouseClicked = this.mouseClick;
    this.mouseClick = false;
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
    this.mouseClick = true;
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