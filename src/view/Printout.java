

package view;
import proto.RunGame;
import util.Batch;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextPane;



public class Printout extends JTextPane implements KeyListener {
  
  RunGame game;
  Batch <Character> pressed = new Batch();
  
  
  public Printout(RunGame game) {
    this.game = game;
    this.setPreferredSize(new Dimension(250, 600));
    addKeyListener(this);
  }
  
  
  void clearInputs() {
    pressed.clear();
  }
  
  
  boolean isPressed(char k) {
    for (Character c : pressed) if (c == k) return true;
    return false;
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


