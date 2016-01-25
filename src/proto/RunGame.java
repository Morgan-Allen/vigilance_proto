

package proto;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import util.*;



public class RunGame extends JFrame implements ActionListener {
  
  
  final public static int
    FRAME_RATE = 25;
  final public static String
    DEFAULT_SAVE_PATH = "saves/main_save.vgl";
  
  
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        RunGame ex = new RunGame();
        ex.setVisible(true);
      }
    });
  }
  
  
  public static boolean onMainThread() {
    //  Filler method- replace later!
    return true;
  }
  
  
  public static boolean mainThreadBegun() {
    //  Filler method- replace later!
    return true;
  }
  
  
  Surface surface;
  Description description;
  World world;
  
  
  public RunGame() {
    setupAssets();
    setupWorld();
    initUI();
  }
  
  
  private void setupAssets() {
    //  TODO:  Throw up a loading screen?
    Assets.compileAssetList("proto");
    Assets.advanceAssetLoading(-1);
  }
  
  
  private void setupWorld() {
    
    if (Assets.exists(DEFAULT_SAVE_PATH)) {
      Session s = Session.loadSession(DEFAULT_SAVE_PATH, true);
      this.world = (World) s.loaded()[0];
      world.game = this;
      world.savePath = DEFAULT_SAVE_PATH;
    }
    else {
      this.world = new World(this, DEFAULT_SAVE_PATH);
      world.initDefaultNations();
      world.initDefaultBase();
    }
    
    Timer timer = new Timer(1000 / FRAME_RATE, this);
    timer.start();
  }
  
  
  private void initUI() {
    this.setLayout(new BorderLayout());
    add(this.surface     = new Surface    (this), BorderLayout.CENTER);
    add(this.description = new Description(this), BorderLayout.EAST  );
    
    pack();
    setTitle("Run Game");
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  
  public void actionPerformed(ActionEvent e) {
    if (world != null) world.updateWorld();
    if (surface != null) surface.repaint();
    if (description != null) description.repaint();
  }
}


class Surface extends JPanel implements MouseListener, MouseMotionListener {
  
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
    
    if (game.world.enteredScene != null) {
      game.world.enteredScene.renderTo(this, g2d);
      game.description.setText(game.world.enteredScene.description());
    }
    else {
      game.world.renderTo(this, g2d);
      game.description.setText(game.world.description());
    }
    
    this.mouseClicked = this.mouseClick;
    this.mouseClick = false;
    this.game.description.clearInputs();
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


class Description extends JTextPane implements KeyListener {
  
  RunGame game;
  Batch <Character> pressed = new Batch();
  
  
  Description(RunGame game) {
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






