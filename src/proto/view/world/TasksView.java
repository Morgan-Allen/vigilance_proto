


package proto.view.world;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.view.common.*;
import proto.view.scene.SceneView;
import proto.util.*;
import java.awt.Color;
import java.awt.Graphics2D;




public class TasksView extends UINode {
  
  
  
  public TasksView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  static class MapTask {
    String name;
    int costTP;
    
    MapTask(String name, int costTP) {
      this.name = name;
      this.costTP = costTP;
    }
    
    void performTask(Person acting, Element focus, World world) {
      return;
    }
    
    boolean canTarget(
      Person acting, Element focus, World world,
      StringBuffer failLog
    ) {
      return false;
    }
  }
  
  //  TODO:  Move this out into the game package...
  
  final static MapTask
    TASK_SURVEIL = new MapTask("Surveil", 8) {
      
      boolean canTarget(
        Person acting, Element focus, World world,
        StringBuffer failLog
      ) {
        if (focus.isPerson() || focus.isPlace()) return true;
        return false;
      }
      
      void performTask(Person acting, Element focus, World world) {
        return;
      }
    },
    TASK_FORENSICS = new MapTask("Forensics", 6) {
      
      boolean canTarget(
        Person acting, Element focus, World world,
        StringBuffer failLog
      ) {
        if (focus.isPlace()) return true;
        return false;
      }
      
      void performTask(Person acting, Element focus, World world) {
        return;
      }
    },
    TASK_WIRETAP = new MapTask("Wiretap", 8) {
      
      boolean canTarget(
        Person acting, Element focus, World world,
        StringBuffer failLog
      ) {
        if (focus.isPerson() || focus.isPlace()) return true;
        return false;
      }
      
      void performTask(Person acting, Element focus, World world) {
        return;
      }
    },
    TASK_QUESTION = new MapTask("Question", 4) {
      
      boolean canTarget(
        Person acting, Element focus, World world,
        StringBuffer failLog
      ) {
        if (focus.isPerson()) return true;
        return false;
      }
      
      void performTask(Person acting, Element focus, World world) {
        return;
      }
    },
    ALL_TASKS[] = { TASK_SURVEIL, TASK_WIRETAP, TASK_QUESTION, TASK_FORENSICS }
  ;
  
  
  MapTask activeTask;
  Person activePerson;
  
  
  void setActiveTask(MapTask task, Person active) {
    this.activeTask   = task;
    this.activePerson = active;
  }
  
  
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  First, print the general description for what the agent is trying-
    g.setColor(new Color(0, 0, 0, 0.66f));
    g.fillRect(vx, vy, vw, vh);
    String desc = description(surface);
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(desc, g, vx, vy, vw, vh);
    /*
    //
    //  Then try jumping to the next agent on the team once a given action is
    //  completed, after a short delay-
    if (delayToJump > 0) {
      Action taken = parent.scene().currentAction();
      if (taken == null && --delayToJump <= 0) {
        jumpToNextAgent(parent.selectedPerson());
      }
    }
    //*/
    return true;
  }
  
  
  String description(Surface surface) {
    StringBuffer s = new StringBuffer();
    
    World  world  = mainView.world();
    Base   base   = mainView.player();
    Person active = mainView.selectedPerson();
    
    if (active != null) {
      s.append("\nSelection: "+active.name());
      s.append("\n  Time Points: "+active.actions.currentTP());
      s.append("\n  Location:    "+active.place());
      boolean canCommand = active.isPlayerOwned();
      
      if (canCommand) {
        char key = '1';
        s.append("\n\nAssignments:");
        for (MapTask task : ALL_TASKS) {
          s.append("\n    "+task.name);
          
          boolean canUse = task.costTP <= active.actions.currentTP();
          if (canUse) s.append(" ("+key+") AP: "+task.costTP);
          
          if (surface.isPressed(key) && canUse) {
            setActiveTask(task, active);
          }
          key++;
        }
      }
    }
    
    return s.toString();
  }
  
  
  
  /**  Rendering previews for an action-
    */
  boolean previewTaskDelivery(
    Object hovered, Coord at, Surface surface, Graphics2D g
  ) {
    final World world = mainView.world();
    final Person selected = mainView.selectedPerson();
    if (activeTask == null) return false;
    
    StringBuffer failLog = new StringBuffer();
    if (activeTask.canTarget(activePerson, (Element) hovered, world, failLog)) {
      return true;
    }
    else {
      //parent.renderString(at.x, at.y - 0.5f, ""+failLog, Color.RED, g);
      return false;
    }
  }
  
  
  void confirmTask(Object focus) {
    final World world = mainView.world();
    activeTask.performTask(activePerson, (Element) focus, world);
  }
  
}

  
  
  





