

package proto.view.scene;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.view.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class ActionsView extends UINode {
  
  
  final SceneView sceneView;
  
  Ability selectAbility;
  Action  selectAction;
  
  
  ActionsView(SceneView parent) {
    super(parent);
    this.sceneView = parent;
  }
  
  
  void setActiveAbility(Ability ability) {
    this.selectAbility = ability;
  }
  
  
  void clearSelection() {
    selectAbility = null;
    selectAction  = null;
  }
  
  
  boolean previewActionDelivery(
    Object hovered, Tile at, Surface surface, Graphics2D g
  ) {
    final Scene scene = mainView.world().activeScene();
    if (selectAbility == null || sceneView.selected == null) return false;
    
    selectAction = null;
    if (! selectAbility.delayed()) {
      selectAction = selectAbility.configAction(
        sceneView.selected, at, hovered, scene, null
      );
    }
    if (selectAction != null) {
      int costAP = selectAction.used.costAP(selectAction);
      sceneView.renderString(at.x, at.y - 0.5f, "AP: "+costAP, Color.GREEN, g);
      return true;
    }
    else {
      sceneView.renderString(at.x, at.y - 0.5f, "X", Color.RED, g);
      return false;
    }
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    
    g.setColor(new Color(0, 0, 0, 0.66f));
    g.fillRect(vx, vy, vw, vh);
    
    String desc = description(surface);
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(desc, g, vx, vy, vw, vh);
    
    return true;
  }
  

  String description(Surface surface) {
    final World world = mainView.world();
    final Scene scene = world.activeScene();
    
    if (scene == null) return null;
    final StringBuffer s = new StringBuffer();
    
    final Person   p      = sceneView.selected;
    final Ability  a      = selectAbility;
    final Action   action = scene.currentAction();
    
    /*
    if (scene.complete()) {
      describeEndSummary(s);
      return s.toString();
    }
    //*/
    
    if (p != null) {
      s.append("\nSelection: "+p.name()+" ("+p.side().name().toLowerCase()+")");
      
      final PersonHealth  PH = p.health ;
      final PersonActions PA = p.actions;
      
      int HP = (int) (PH.maxHealth() - (PH.injury() + PH.stun()));
      int armour = p.stats.levelFor(PersonStats.ARMOUR);
      s.append("\n  Health: "+HP+"/"+PH.maxHealth());
      if (PH.stun() > 0) s.append(" (Stun "+(int) PH.stun()+")");
      if (armour > 0) s.append("\n  Armour: "+armour);
      s.append("\n  Status: "+p.confidenceDescription());
      s.append("\n  AP: "+PA.currentAP()+"/"+PA.maxAP());
      
      if (PH.conscious() && PA.nextAction() != null) {
        s.append("\n  Last action: "+PA.nextAction().used);
      }
      
      Series <Equipped> equipped = p.gear.equipment();
      if (equipped.size() > 0) {
        s.append("\n  Equipment:");
        for (Equipped e : equipped) s.append(" "+e.name+" ("+e.bonus+")");
      }
      
      boolean canCommand =
        action == null && p.actions.canTakeAction() && p.isPlayerOwned()
      ;
      if (canCommand) {
        s.append("\n\n  Abilities (Press 1-9):");
        char key = '1';
        for (Ability r : p.stats.listAbilities())  {
          if (! r.active()) continue;
          s.append("\n    "+r.name());
          
          boolean canUse = r.minCostAP() <= PA.currentAP();
          if (canUse) s.append(" ("+key+") AP: "+r.minCostAP());
          if (surface.isPressed(key) && canUse) {
            sceneView.setSelection(p, false);
            setActiveAbility(r);
          }
          key++;
        }
        if (a == null) {
          s.append("\n  Pass Turn (X)");
          if (surface.isPressed('x')) {
            p.actions.onTurnEnd();
            scene.moveToNextPersonsTurn();
          }
        }
        //  TODO:  Allow zooming to and tabbing through party members.
        //  Note:  Delayed actions only target the self, so no selection is
        //  needed, only confirmation.
        else {
          if (a.delayed()) {
            selectAction = a.configAction(p, p.currentTile(), p, scene, null);
            s.append("\n\n"+a.FX.describeAction(selectAction, scene));
            s.append("\n  Confirm (Y)");
            if (surface.isPressed('y')) {
              p.actions.assignAction(selectAction);
              scene.moveToNextPersonsTurn();
            }
          }
          else {
            s.append("\n\n"+a.FX.describeAction(selectAction, scene));
            s.append("\n  Select target");
          }
          s.append("\n  Cancel (X)");
          if (surface.isPressed('x')) {
            sceneView.setSelection(p, false);
          }
        }
      }
    }
    
    //
    //  General options-
    /*
    s.append("\n");
    s.append("\n  Press S to save, R to reload.");
    if (print.isPressed('s')) world.performSave();
    if (print.isPressed('r')) world.reloadFromSave();
    //*/
    
    return s.toString();
  }
  
  
  
  /*
  void describeEndSummary(StringBuffer s) {
    final Scene scene = mainView.world().activeScene();
    if (scene == null) return;
    
    boolean success = scene.wasWon();
    World   world   = scene.world();
    Nation  site    = scene.site();
    
    Printout print = world.game().print();
    s.append("\nMission ");
    if (success) s.append(" Successful: "+scene);
    else s.append(" Failed: "+scene);
    
    s.append("\nTeam Status:");
    for (Person p : scene.playerTeam()) {
      s.append("\n  "+p.name());
      if (p.currentScene() != scene) {
        s.append(" (escaped)");
      }
      else if (! p.alive()) {
        s.append(" (dead)");
      }
      else if (! p.conscious()) {
        s.append(success ? " (unconscious)" : " (captive)");
      }
      else s.append(" (okay)");
    }
    s.append("\nOther Forces:");
    for (Person p : scene.othersTeam()) {
      s.append("\n  "+p.name());
      if (p.currentScene() != scene) {
        s.append(" (escaped)");
      }
      else if (! p.alive()) {
        s.append(" (dead)");
      }
      else if (! p.conscious()) {
        s.append(success ? " (captive)" : " (unconscious)");
      }
      else s.append(" (okay)");
    }
    
    final String DESC_C[] = {
      "None", "Minimal", "Medium", "Heavy", "Total"
    };
    final String DESC_G[] = {
      "None", "Few", "Some", "Many", "All"
    };
    int colIndex = Nums.clamp(Nums.ceil(scene.assessCollateral() * 5), 5);
    int getIndex = Nums.clamp(Nums.ceil(scene.assessGetaways  () * 5), 5);
    s.append("\nCollateral: "+DESC_C[colIndex]);
    s.append("\nGetaways: "  +DESC_G[getIndex]);
    int trustPercent = (int) (site.trustLevel() * 100);
    int crimePercent = (int) (site.crimeLevel() * 100);
    s.append("\nRegional Trust: "+trustPercent+"%");
    s.append("\nRegional Crime: "+crimePercent+"%");
    
    s.append("\n\n  Press X to exit.");
    if (print.isPressed('x')) {
      scene.endScene();
    }
  }
  //*/
  
  
  
}
