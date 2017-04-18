

package proto.common;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.content.agents.*;
import proto.content.events.*;
import proto.content.items.*;
import proto.content.places.*;



public class DebugScene extends RunGame {
  
  
  public static void main(String args[]) {
    GameSettings.reportWorldInit = false;
    GameSettings.debugScene      = true ;
    runGame(new DebugScene(), "saves/debug_scene");
  }
  
  
  protected World setupWorld() {
    this.world = new World(this, savePath);
    DefaultGame.initDefaultWorld(world);
    
    Base crooks = world.baseFor(Crooks.THE_MADE_MEN);
    Plot plot = PlotTypes.TYPE_KIDNAP.initPlot(crooks);
    plot.fillAndExpand();
    plot.printRoles();
    crooks.plots.assignRootPlot(plot, 0);
    
    Base heroes = world.baseFor(Heroes.JANUS_INDUSTRIES);
    Lead guarding = heroes.leads.leadFor(
      plot.target(), Lead.LEAD_SURVEIL_PERSON
    );
    
    int ID = 0;
    for (Person p : heroes.roster()) {
      ItemType gadget1 = ID % 2 == 0 ? Gadgets.MED_KIT     : Gadgets.BOLAS   ;
      ItemType gadget2 = ID % 2 == 0 ? Gadgets.SONIC_PROBE : Gadgets.TEAR_GAS;
      p.gear.equipItem(gadget1, PersonGear.SLOT_ITEM_1);
      p.gear.equipItem(gadget2, PersonGear.SLOT_ITEM_2);
      p.updateOnBase();
      p.addAssignment(guarding);
      ID++;
    }
    
    Step heist = plot.mainHeist();
    plot.advanceToStep(heist);
    Scene scene = plot.generateScene(heist, plot.target(), guarding);
    world.enterScene(scene);
    
    return world;
  }
}







