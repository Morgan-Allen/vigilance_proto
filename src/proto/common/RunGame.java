

package proto.common;
import proto.game.world.*;
import proto.view.*;
import proto.view.common.Surface;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public abstract class RunGame extends JFrame implements ActionListener {
  
  
  /**  Setup and construction-
    */
  final public static int
    FRAME_RATE                 = 25,
    SLOW_HOURS_PER_REAL_SECOND = 8,
    FAST_HOURS_PER_REAL_SECOND = 24 * 8
  ;
  
  protected String savePath;
  protected Surface surface;
  protected World world;
  boolean paused = true;
  
  
  protected static void runGame(final RunGame game, final String savePath) {
    game.savePath = savePath;
    game.setupAssets();
    game.initUI();
    
    if (! game.attemptReload(game.savePath)) {
      game.world = game.setupWorld();
      game.runTests(game.world, false, false);
    }
    
    Timer timer = new Timer(1000 / FRAME_RATE, game);
    timer.start();
    game.setVisible(true);
  }
  
  
  private void setupAssets() {
    //  TODO:  Throw up a loading screen?
    Assets.compileAssetList("proto");
    Assets.advanceAssetLoading(-1);
  }
  
  
  public boolean attemptReload(String savePath) {
    if (! Assets.exists(savePath)) return false;
    this.world  = null;
    this.paused = true;
    Session s = Session.loadSession(savePath, true);
    this.world = (World) s.loaded()[0];
    if (world != null) {
      world.attachToGame(this, savePath);
      this.runTests(world, true, false);
    }
    return true;
  }
  
  
  public void actionPerformed(ActionEvent e) {
    if (world != null && world.activeScene() != null) {
      world.activeScene().updateScene();
    }
    else if (world != null && ! paused) {
      world.updateWorldInRealTime(1f / FRAME_RATE);
    }
    if (surface != null) {
      surface.repaint();
    }
  }
  
  
  public boolean paused() {
    return paused;
  }
  
  
  public void setPaused(boolean paused) {
    this.paused = paused;
  }
  
  
  public static boolean onMainThread() {
    //  Filler method- replace later!
    return true;
  }
  
  
  public static boolean mainThreadBegun() {
    //  Filler method- replace later!
    return true;
  }
  
  
  
  /**  Actual world setup for the first run (not when saving/loading)-
    */
  protected abstract World setupWorld();
  
  
  protected boolean runTests(World world, boolean afterLoad, boolean suite) {
    return true;
  }
  
  
  public void assignWorld(World world) {
    this.world = world;
  }
  
  
  public World world() {
    return world;
  }
  
  
  
  /**  Support methods for the UI-
    */
  public Surface surface() {
    return surface;
  }
  
  
  private void initUI() {
    this.setLayout(new BorderLayout());
    add(this.surface = new Surface(this), BorderLayout.CENTER);
    
    pack();
    setTitle("Vigilance");
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this   .addKeyListener        (surface);
    surface.addMouseListener      (surface);
    surface.addMouseMotionListener(surface);
  }
}








