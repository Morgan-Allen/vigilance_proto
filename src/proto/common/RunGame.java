

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
    GAME_HOURS_PER_REAL_SECOND = 8 ;
  
  String savePath;
  Surface surface;
  World world;
  boolean paused = true;
  
  
  protected static void runGame(final RunGame game, final String savePath) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        game.savePath = savePath;
        game.setupAssets();
        game.initUI();
        
        if (! game.attemptReload(game.savePath)) {
          game.world = game.setupWorld();
          game.runWorldTests(game.world, false);
        }
        
        Timer timer = new Timer(1000 / FRAME_RATE, game);
        timer.start();
        game.setVisible(true);
      }
    });
  }
  
  
  private void setupAssets() {
    //  TODO:  Throw up a loading screen?
    Assets.compileAssetList("proto");
    Assets.advanceAssetLoading(-1);
  }
  
  
  public boolean attemptReload(String savePath) {
    if (! Assets.exists(savePath)) return false;
    this.world = null;
    Session s = Session.loadSession(savePath, true);
    this.world = (World) s.loaded()[0];
    if (world != null) {
      world.attachToGame(this, savePath);
      this.runWorldTests(world, true);
    }
    return true;
  }
  
  
  public void actionPerformed(ActionEvent e) {
    if (world != null && world.activeScene() != null) {
      world.activeScene().updateScene();
    }
    else if (world != null && ! paused) {
      float hoursGap = GAME_HOURS_PER_REAL_SECOND * 1f / FRAME_RATE;
      world.updateWorld(hoursGap);
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
  
  
  protected void runWorldTests(World world, boolean afterLoad) {
    return;
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








