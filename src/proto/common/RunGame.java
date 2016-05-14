

package proto.common;
import proto.game.world.*;
import proto.util.*;
import proto.view.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



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
  
  
  
  /**  Setup and construction-
    */
  Surface surface;
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
    
    if (! attemptReload(DEFAULT_SAVE_PATH)) {
      this.world = new World(this, DEFAULT_SAVE_PATH);
      world.initDefaultNations();
      world.initDefaultBase();
    }
    
    Timer timer = new Timer(1000 / FRAME_RATE, this);
    timer.start();
  }
  
  
  private void initUI() {
    this.setLayout(new BorderLayout());
    add(this.surface = new Surface (this), BorderLayout.CENTER);
    
    pack();
    setTitle("Run Game");
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }
  
  
  public void actionPerformed(ActionEvent e) {
    if (world   != null) world.updateWorld();
    if (surface != null) surface.repaint();
  }
  
  
  public boolean attemptReload(String savePath) {
    if (! Assets.exists(savePath)) return false;
    this.world = null;
    Session s = Session.loadSession(savePath, true);
    this.world = (World) s.loaded()[0];
    if (world != null) world.attachToGame(this, savePath);
    return true;
  }
  
  
  
  /**  Public access methods-
    */
  public World world() {
    return world;
  }
  
  
  /*
  public Scene scene() {
    return world == null ? null : world.enteredScene();
  }
  //*/
  
  
  public Surface surface() {
    return surface;
  }
}








