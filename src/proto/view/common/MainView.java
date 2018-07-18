
package proto.view.common;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import proto.view.scene.*;
import proto.view.base.*;
import proto.view.world.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;




public class MainView extends UINode {
  
  final public static String
    MAPS_DIR = "media assets/city map/",
    ACTS_DIR = "media assets/action view/",
    MNUI_DIR = "media assets/main UI/"
  ;
  final public static int
    TAB_EQUIP = 1,
    TAB_TRAIN = 2,
    TAB_PSYCH = 3
  ;
  
  private World world;
  SceneView sceneView;
  UINode    mainUI   ;
  
  /*
  final public RosterView rosterView;
  final public BasicInfoBar basicBar;
  UINode tabsNode;
  UINode tabButtons[], tabContent[];
  UINode currentTab = null;
  final UINode optionsButton;
  
  final public EquipmentView equipView  ;
  final public TrainingView  trainView  ;
  final public MapView       mapView    ;
  final public HistoryView   historyView;
  //*/
  
  final public Image selectCircle, selectSquare;
  
  MapView mapView;
  
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
    
    final Box2D tabSubBounds = new Box2D(0, 25, fullWide, fullHigh - 160);
    mapView     = new MapView      (mainUI, tabSubBounds);
    
    /*
    rosterView = new RosterView(mainUI, new Box2D(
      0, fullHigh - 120, fullWide, 120
    ));
    mainUI.addChildren(rosterView);
    
    basicBar = new BasicInfoBar(mainUI, new Box2D(
      0, fullHigh - 145, fullWide, 25
    ));
    mainUI.addChildren(basicBar);
    
    tabsNode = new UINode(mainUI, new Box2D(0, 0, fullWide, 25));
    Box2D blank = new Box2D();
    final String tabNames[] = {
      "City Map", "Spending", "Outfit & Regimen"
    };
    tabButtons = new UINode[tabNames.length];
    
    //  Add views for each tab:
    final Box2D tabSubBounds = new Box2D(0, 25, fullWide, fullHigh - 160);
    mapView     = new MapView      (mainUI, tabSubBounds);
    equipView   = new EquipmentView(mainUI, tabSubBounds);
    trainView   = new TrainingView (mainUI, tabSubBounds);
    historyView = new HistoryView  (mainUI, tabSubBounds);
    tabContent  = new UINode[] { mapView, equipView, trainView };
    
    int butW = (int) ((fullWide - 210) / tabButtons.length);
    for (int i = tabContent.length; i-- > 0;) {
      final UINode content = tabContent[i], button;
      button = tabButtons[i] = new StringButton(tabNames[i], blank, tabsNode) {
        protected void whenClicked() {
          switchToTab(content);
        }
      };
      button.relBounds.set(0 + (butW * i), 0, butW, 25);
      content.visible = false;
    }
    mainUI  .addChildren(tabContent);
    mainUI  .addChildren(tabsNode  );
    tabsNode.addChildren(tabButtons);
    switchToTab(mapView);
    
    optionsButton = new StringButton("Game Options", blank, tabsNode) {
      protected void whenClicked() {
        showOptionsPane();
      }
    };
    optionsButton.relBounds.set(fullWide - 205, 0, 200, 25);
    tabsNode.addChildren(optionsButton);
    //*/
    
    selectCircle  = Kind.loadImage(ACTS_DIR+"select_circle.png");
    selectSquare  = Kind.loadImage(MNUI_DIR+"select_square.png");
  }
  
  
  public SceneView sceneView() {
    return sceneView;
  }
  
  
  public MapView mapView() {
    return mapView;
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
  
  
  public Person selectedPerson() {
    //  TODO:  Restore this...
    return null;
  }
  
  
  
  /**  Switching tabs:
    */
  public void switchToTab(int tabID) {
    //  TODO:  Restore this...
    /*
    int index = Visit.indexOf(content, tabContent);
    for (int i = tabButtons.length; i-- > 0;) {
      ((StringButton) tabButtons[i]).toggled = i == index;
    }
    for (UINode c : tabContent) {
      c.visible = c == content;
    }
    //*/
  }
  
  
  public void setSelectedPerson(Person agent) {
    //  TODO:  Restore this...
  }
  
  
  public void setMonitoring(boolean start) {
    RunGame game = mainView.game();
    if (start) {
      mapView.showEventsFocus();
      //switchToTab(mapView);
      game.setPaused(false);
    }
    else game.setPaused(true);
  }
  
  
  
  /**  Various custom popup-presentation methods:
    */
  public void queueMessage(UINode message) {
    if (messageQueue.includes(message)) return;
    messageQueue.add(message);
    game().setPaused(true);
  }
  
  
  public void dismissMessage(UINode message) {
    messageQueue.remove(message);
    setChild(message, false);
  }
  
  
  public void showClickMenu(ClickMenu menu) {
    this.clickMenu = menu;
    setChild(menu, true);
  }
  
  
  public void hideClickMenu(ClickMenu menu) {
    setChild(menu, false);
    this.clickMenu = null;
  }
  
  
  public void showOptionsPane() {
    MessageView optionsPane = new MessageView(
      this, null, "Game Options", "",
      "Save Progress",
      "Reload Game",
      "Save and Quit",
      "Cancel"
    ) {
      protected void whenClicked(String option, int optionID) {
        if (optionID == 0) {
          world().performSave();
          dismissMessage(this);
        }
        if (optionID == 1) {
          world().reloadFromSave();
        }
        if (optionID == 2) {
          world().performSaveAndQuit();
        }
        if (optionID == 3) {
          dismissMessage(this);
        }
      }
    };
    queueMessage(optionsPane);
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











