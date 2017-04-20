
package proto.view.common;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.scene.*;
import proto.view.base.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;




public class MainView extends UINode {
  
  final public static String
    MAPS_DIR = "media assets/city map/",
    ACTS_DIR = "media assets/action view/",
    MNUI_DIR = "media assets/main UI/"
  ;
  
  private World world;
  SceneView  sceneView ;
  UINode     mainUI    ;
  final public RosterView rosterView;
  
  
  UINode tabsNode;
  UINode tabButtons[], tabContent[];
  UINode currentTab = null;
  
  final public EquipmentView equipView  ;
  final public TrainingView  trainView  ;
  final public CasesView     casesView  ;
  final public InvestingView investView ;
  final public HistoryView   historyView;
  
  final public ProgressOptionsView progOptions;
  final public StatsReadoutView    mainReadout;
  
  final public Image selectCircle, selectSquare;
  
  UINode messageShown = null;
  List <UINode> messageQueue = new List();
  ClickMenu clickMenu = null;
  
  
  
  
  public MainView(World world) {
    
    super();
    this.world = world;
    
    int fullHigh = 750, fullWide = 1200;
    this.relBounds.set(0, 0, fullWide, fullHigh);
    
    sceneView = new SceneView(this, new Box2D(
      0, 0, fullWide, fullHigh
    ));
    mainUI = new UINode(this, new Box2D(
      0, 0, fullWide, fullHigh
    ));
    addChildren(sceneView, mainUI);
    
    rosterView = new RosterView(mainUI, new Box2D(
      0, fullHigh - 120, fullWide, 120
    ));
    mainUI.addChildren(rosterView);
    
    //  Add views for each tab:
    final Box2D tabSubBounds = new Box2D(0, 25, fullWide, fullHigh - 145);
    casesView = new CasesView (mainUI, tabSubBounds);
    investView  = new InvestingView(mainUI, tabSubBounds);
    equipView   = new EquipmentView(mainUI, tabSubBounds);
    trainView   = new TrainingView (mainUI, tabSubBounds);
    historyView = new HistoryView  (mainUI, tabSubBounds);
    tabContent = new UINode[] {
      casesView, investView, equipView, trainView
    };
    
    tabsNode = new UINode(mainUI, new Box2D(0, 0, fullWide, 25));
    Box2D blank = new Box2D();
    final String tabNames[] = {
      "Mission Control", "Investments", "Armory", "Training & Outfit"
    };
    tabButtons = new UINode[tabContent.length];
    int butW = (int) (tabsNode.relBounds.xdim() / tabButtons.length);
    
    for (int i = tabContent.length; i-- > 0;) {
      final UINode content = tabContent[i], button;
      button = tabButtons[i] = new StringButton(tabNames[i], blank, tabsNode) {
        protected void whenClicked() {
          switchToTab(content);
        }
      };
      button.relBounds.set(butW * i, 0, butW, 25);
      content.visible = false;
    }
    mainUI  .addChildren(tabsNode  );
    tabsNode.addChildren(tabButtons);
    mainUI  .addChildren(tabContent);
    
    switchToTab(casesView);
    
    progOptions = new ProgressOptionsView(mainUI, new Box2D(
      (fullWide / 2) - 100, fullHigh - 170, 200, 50
    ));
    mainReadout = new StatsReadoutView(mainUI, new Box2D(
      0, fullHigh - 170, (fullWide / 2) - 100, 50
    ));
    mainUI.addChildren(progOptions, mainReadout);
    
    selectCircle  = Kind.loadImage(ACTS_DIR+"select_circle.png");
    selectSquare  = Kind.loadImage(MNUI_DIR+"select_square.png");
  }
  
  
  public SceneView sceneView() {
    return sceneView;
  }
  
  
  public RunGame game() {
    return world.game();
  }
  
  
  public World world() {
    return world;
  }
  
  
  public Base player() {
    return world.playerBase();
  }
  
  
  
  /**  Switching tabs:
    */
  public void switchToTab(UINode content) {
    int index = Visit.indexOf(content, tabContent);
    for (int i = tabButtons.length; i-- > 0;) {
      ((StringButton) tabButtons[i]).toggled = i == index;
    }
    for (UINode c : tabContent) {
      c.visible = c == content;
    }
  }
  
  
  
  /**  Various custom popup-presentation methods:
    */
  public void queueMessage(UINode message) {
    messageQueue.add(message);
    game().setPaused(true);
  }
  
  
  public void dismissMessage(UINode message) {
    messageQueue.remove(message);
  }
  
  
  public void showClickMenu(ClickMenu menu) {
    this.clickMenu = menu;
    setChild(menu, true);
  }
  
  
  public void hideClickMenu(ClickMenu menu) {
    setChild(menu, false);
    this.clickMenu = null;
  }
  
  
  
  /**  Rendering methods-
    */
  protected void updateAndRender(Surface surface, Graphics2D g) {
    final UINode topMessage = messageQueue.first();
    
    if (world.activeScene() != null) {
      sceneView.visible = true ;
      mainUI   .visible = false;
    }
    else {
      sceneView.visible = false;
      mainUI   .visible = true ;
    }
    
    if (topMessage != messageShown) {
      if (topMessage != null) {
        setChild(topMessage, true);
      }
      if (messageShown != null) {
        setChild(messageShown, false);
      }
      messageShown = topMessage;
    }
    
    super.updateAndRender(surface, g);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    g.setColor(Color.BLACK);
    g.fillRect(vx, vy, vw, vh);
    return true;
  }
  
}











