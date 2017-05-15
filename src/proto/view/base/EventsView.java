

package proto.view.base;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Graphics2D;
import java.awt.Image;



public class EventsView extends UINode {
  
  
  public EventsView(UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  Create a list-display, and render the header plus entries for each
    //  associate:
    World world = mainView.world();
    Base player = mainView.player();
    String caseDesc[] = { "Weak", "Fair", "Strong" };
    int time = world.timing.totalHours();
    
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    int across = 10, down = 10;
    boolean hasCase = false;
    draw.addEntry(
      null, "OPEN CASES", 25, null
    );
    
    for (Plot plot : player.leads.activePlots()) {
      if (plot.complete()) continue;
      Image icon = plot.icon();
      if (icon == null) icon = CasesView.ALERT_IMAGE;
      draw.addEntry(icon, CasesFX.nameFor(plot, player), 40, plot);
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
    /*
    if (draw.clicked) {
      setActiveFocus(draw.hovered, true);
    }
    //*/
    return true;
  }
  
}



