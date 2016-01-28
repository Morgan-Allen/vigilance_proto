

package proto.view;
import proto.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.common.RunGame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;



public class Surface extends JPanel implements MouseListener, MouseMotionListener {
  
  
  RunGame game;
  boolean mouseDown, mouseClick, mouseClicked;
  int mouseX, mouseY;
  
  
  
  public Surface(RunGame runGame) {
    this.game = runGame;
    
    this.setPreferredSize(new Dimension(850, 600));
    addMouseListener(this);
    addMouseMotionListener(this);
  }
  
  
  public boolean mouseIn(int x, int y, int w, int h) {
    return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
  }
  
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    Scene scene = game.scene();
    World world = game.world();
    
    if (scene != null) {
      SceneView view = scene.view();
      view.renderTo(this, g2d);
      game.print().setText(view.description());
    }
    else if (world != null) {
      WorldView view = world.view();
      view.renderTo(this, g2d);
      game.print().setText(view.description());
    }
    
    this.mouseClicked = this.mouseClick;
    this.mouseClick = false;
    this.game.print().clearInputs();
  }


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
}