

package proto.view.base;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Image;
import java.awt.Color;
import java.awt.Graphics2D;



public class CasesView extends UINode {
  
  /**  Constants, data fields, construction and save/load methods-
    */
  final static String
    IMG_DIR = "media assets/city map/";
  final static Image
    ALERT_IMAGE   = Kind.loadImage(IMG_DIR+"alert_symbol.png"  ),
    MYSTERY_IMAGE = Kind.loadImage(IMG_DIR+"mystery_symbol.png"),
    FILE_IMAGE    = Kind.loadImage(IMG_DIR+"file_image.png"    ),
    TRIAL_IMAGE   = Kind.loadImage(IMG_DIR+"trial_image.png"   ),
    JAILED_IMAGE  = Kind.loadImage(IMG_DIR+"jailed_image.png"  );
  
  final static Object
    ALL_CASES  = "all-cases",
    PLOT_CLUES = "plot-clues",
    PERP_LINKS = "perp-links";
  
  MapInsetView mapView;
  ScrollArea casesArea;
  
  CaseRolesView rolesView ;
  CaseCluesView cluesView ;
  CasePerpsView perpsView ;
  CaseLinksView linksView ;
  RegionView    regionView;
  
  UINode focusViews[], activeFocusView;
  StringButton backButton;
  
  Object activeFocus = null;
  List <Object> focusStack = new List();
  
  
  
  public CasesView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    int fullWide = (int) viewBounds.xdim(), fullHigh = (int) viewBounds.ydim();
    mapView = new MapInsetView(this, new Box2D(
      320, 5, fullWide - 640, fullHigh - 10
    )) {
      protected void onRegionSelect(Region region) {
        setActiveFocus(region, true);
      }
    };
    mapView.loadMapImages(
      MainView.MAPS_DIR+"city_map.png",
      MainView.MAPS_DIR+"city_districts_key.png"
    );
    mapView.resizeToFitAspectRatio();
    addChildren(mapView);
    
    Box2D casesBound  = new Box2D(0, 0, 340, fullHigh - 50);
    Box2D scrollBound = new Box2D(0, 0, 320, fullHigh - 50);
    casesArea = new ScrollArea(this, casesBound);
    addChildren(casesArea);
    UINode scrollKid = new UINode(casesArea, scrollBound) {};
    casesArea.attachScrollPane(scrollKid, (int) scrollBound.ydim());
    
    rolesView  = new CaseRolesView(scrollKid, scrollBound);
    cluesView  = new CaseCluesView(scrollKid, scrollBound);
    perpsView  = new CasePerpsView(scrollKid, scrollBound);
    linksView  = new CaseLinksView(scrollKid, scrollBound);
    regionView = new RegionView   (scrollKid, scrollBound);
    focusViews = new UINode[] {
      rolesView, cluesView, perpsView, linksView, regionView
    };
    scrollKid.addChildren(focusViews);
    
    backButton = new StringButton(
      "Back", new Box2D(250, 10, 60, 25), this
    ) {
      protected void whenClicked() {
        navigateFocusBack();
      }
    };
    addChildren(backButton);
    
    setActiveFocus(ALL_CASES, false);
  }
  
  
  protected void renderAfterKids(Surface surface, Graphics2D g) {
    Person person = mainView.rosterView.selectedPerson();
    if (person == null) return;
    
    g.setColor(Color.WHITE);
    final Assignment task = person.topAssignment();
    String assignDesc = "None";
    if (task != null) {
      assignDesc = task.activeInfo();
    }
    else {
      assignDesc = "At Base";
    }
    
    ViewUtils.drawWrappedString(
      person+" is "+assignDesc, g,
      vx + 360, vy + 10, 320, 45
    );
    
    renderCases(surface, g);
  }
  

  protected void renderCases(Surface surface, Graphics2D g) {
    //
    //  Create a list-display, and render the header plus entries for each
    //  associate:
    World world = mainView.world();
    Base player = mainView.player();
    String caseDesc[] = { "Weak", "Fair", "Strong" };
    int time = world.timing.totalHours();
    
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = vw - 320, down = 10;
    boolean hasCase = false;
    draw.addEntry(
      null, "OPEN CASES", 25, null
    );
    
    for (Plot plot : player.leads.activePlots()) {
      if (plot.complete()) continue;
      Image icon = plot.icon();
      if (icon == null) icon = CasesView.ALERT_IMAGE;
      draw.addEntry(icon, CaseFX.nameFor(plot, player), 40, plot);
      hasCase = true;
    }
    
    for (Trial trial : world.council.upcomingTrials()) {
      Image icon = CasesView.TRIAL_IMAGE;
      String desc = trial.toString();
      float evidence = trial.rateEvidence();
      int date = (trial.timeBegins() - time) / World.HOURS_PER_DAY;
      desc += "\n  Date: "+date+" days";
      desc += "  Evidence: "+caseDesc[Nums.clamp((int) (evidence * 3), 3)];
      draw.addEntry(icon, desc, 50, trial.plot());
      hasCase = true;
    }
    if (! hasCase) {
      draw.addEntry(null, "    None", 25, null);
    }
    
    draw.addEntry(
      null, "ONGOING TASKS", 25, null
    );
    //  TODO:  Make this an interface for the relevant objects?
    class Upcoming { Object ref; float daysLeft; Image icon; }
    List <Upcoming> schedule = new List <Upcoming> () {
      protected float queuePriority(Upcoming r) {
        return r.daysLeft;
      }
    };
    for (Place place : world.places()) {
      if (place.buildProgress() < 1) {
        Upcoming o = new Upcoming();
        o.ref      = place;
        o.daysLeft = place.buildDaysRemaining();
        o.icon     = place.icon();
        schedule.add(o);
      }
    }
    for (Task task : player.activeAgentTasks()) {
      for (Person p : task.assigned()) {
        Upcoming o = new Upcoming();
        o.ref      = task;
        o.daysLeft = task.taskDaysRemaining(p);
        o.icon     = p.icon();
        schedule.add(o);
      }
    }
    schedule.queueSort();
    for (Upcoming o : schedule) {
      String desc = ""+o.ref+" ("+I.shorten(o.daysLeft, 1)+" days)";
      draw.addEntry(o.icon, desc, 25, null);
    }
    if (schedule.empty()) {
      draw.addEntry(null, "    None", 25, null);
    }
    
    draw.performDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  If one is selected, zoom to that element:
    if (draw.clicked) {
      setActiveFocus(draw.hovered, true);
    }
  }
  
  
  /**  Helper methods for navigation related to case-files:
    */
  public Object priorFocus() {
    return focusStack.atIndex(focusStack.size() - 2);
  }
  
  
  //  TODO:  You may need the ability to pass in arbitrary views.
  
  public void setActiveFocus(Object focus, boolean wipeStack) {
    if (wipeStack) {
      focusStack.clear();
      focusStack.add(focus);
    }
    else if (focus != this.activeFocus) {
      focusStack.add(focus);
    }
    
    this.activeFocus = focus;
    this.activeFocusView = null;
    backButton.visible = focusStack.size() > 1;
    
    if (focus == ALL_CASES) {
      activeFocusView = null;
    }
    else if (focus == PLOT_CLUES) {
      activeFocusView = cluesView;
    }
    else if (focus == PERP_LINKS) {
      activeFocusView = linksView;
    }
    else if (focus instanceof Plot) {
      activeFocusView = rolesView;
    }
    else if (focus instanceof Role) {
      activeFocusView = perpsView;
    }
    else if (focus instanceof Region) {
      activeFocusView = regionView;
      mapView.setSelectedRegion((Region) focus);
    }
    else if (focus instanceof Element) {
      activeFocusView = perpsView;
      Element e = (Element) focus;
      mapView.setSelectedRegion(e.region());
    }
    for (UINode node : focusViews) {
      node.visible = node == activeFocusView;
    }
  }
  
  
  public void navigateFocusBack() {
    focusStack.removeLast();
    Object before = focusStack.last();
    if (before == null) before = ALL_CASES;
    this.activeFocus = before;
    setActiveFocus(before, false);
  }
  
}



