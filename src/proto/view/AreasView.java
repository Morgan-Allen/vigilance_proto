

package proto.view;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class AreasView extends UINode {
  
  
  final MapView mapView;
  final StringButton monitorButton, saveButton, loadButton, quitButton;
  
  private Object selectedArea;
  private Assignment selectedTask;
  
  
  AreasView(final UINode parent, Box2D viewBounds) {
    super(parent, viewBounds);
    
    mapView = new MapView(this, new Box2D(
      80, 10,
      viewBounds.xdim() - 80, viewBounds.ydim() - 80
    ));
    
    int
      across = (int) viewBounds.xdim() - 155,
      down   = (int) viewBounds.ydim() - 20 ;
    
    monitorButton = new StringButton(
      "Resume Monitoring",
      new Box2D(5, down - 45, viewBounds.xdim() - 10, 20), this
    ) {
      void whenClicked() {
        final boolean active = mainView.world.monitorActive();
        if (active) mainView.world.pauseMonitoring();
        else        mainView.world.beginMonitoring();
      }
    };
    addChildren(mapView, monitorButton);
    
    saveButton = new StringButton(
      "Save", new Box2D(across + 0, down, 50, 20), this
    ) {
      void whenClicked() {
        mainView.world.performSave();
      }
    };
    loadButton = new StringButton(
      "Reload", new Box2D(across + 50, down, 50, 20), this
    ) {
      void whenClicked() {
        mainView.world.reloadFromSave();
      }
    };
    quitButton = new StringButton(
      "Quit", new Box2D(across + 100, down, 50, 20), this
    ) {
      void whenClicked() {
        mainView.world.performSaveAndQuit();
      }
    };
    addChildren(saveButton, loadButton, quitButton);
  }
  
  
  protected void renderTo(Surface surface, Graphics2D g) {
    
    final World world = mainView.world;
    final Base  base  = world.base();
    
    int down = 10;
    
    for (Room room : base.rooms()) if (room != null) {
      g.drawImage(room.icon(), vx + 10, vy + down, 60, 60, null);
      boolean hover = surface.tryHover(vx + 10, vy + down, 60, 60, room);
      
      if (hover || selectedArea == room) {
        g.drawImage(mainView.selectSquare, vx + 10, vy + down, 60, 60, null);
      }
      
      if (hover && surface.mouseClicked()) {
        selectedArea = room;
      }
      
      ViewUtils.renderAssigned(
        room.visitors(), vx + 60 + 10, vy + down + 60, surface, g
      );
      down += 60 + 10;
    }
    
    //  TODO:  This might belong in a separate UI node?
    g.setColor(Color.WHITE);
    String timeString = "Time: "+ViewUtils.getTimeString(world);
    g.drawString(timeString, vx + 10, vy + vh - 5);
    
    String cashString = "";
    cashString +=  "Funds: " +base.currentFunds()+"";
    cashString += " Income: "+base.income      ()+"";
    cashString += " Outlay: "+base.maintenance ()+"";
    g.drawString(cashString, vx + 10, vy + vh - 25);
    
    g.setColor(Color.DARK_GRAY);
    g.drawRect(vx, vy, vw, vh);
  }
  
  
  void setSelection(Object selected) {
    final Object old = selectedArea;
    this.selectedArea = selected;
    if (old != selected) selectedTask = null;
  }
  
  
  Room selectedRoom() {
    if (selectedArea instanceof Room) return (Room) selectedArea;
    return null;
  }
  
  
  District selectedNation() {
    if (selectedArea instanceof District) return (District) selectedArea;
    return null;
  }
  
  
  void setSelectedTask(Assignment task) {
    this.selectedTask = task;
  }
  
  
  Assignment selectedTask() {
    return selectedTask;
  }
}





