

package proto.view.scene;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.view.common.*;
import proto.util.*;

import java.awt.Color;
import java.awt.Graphics2D;



public class ActionsView extends UINode {
  
  
  final SceneView sceneView;
  
  Ability selectAbility;
  Action  selectAction;
  long delayToJump = 0;
  
  
  ActionsView(SceneView parent) {
    super(parent);
    this.sceneView = parent;
  }
  
  
  void setActiveAbility(Ability ability) {
    this.selectAbility = ability;
  }
  
  
  void confirmActionChoice(Action action) {
    Person agent = action.acting;
    agent.actions.assignAction(action);
    action.scene().pushNextAction(action);
    clearChoices();
  }
  
  
  void clearChoices() {
    selectAbility = null;
    selectAction  = null;
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  First, print the general description for what the agent is trying-
    g.setColor(new Color(0, 0, 0, 0.66f));
    g.fillRect(vx, vy, vw, vh);
    String desc = description(surface);
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(desc, g, vx, vy, vw, vh);
    //
    //  Then try jumping to the next agent on the team once a given action is
    //  completed, after a short delay-
    final Scene scene = sceneView.scene();
    Person agent = sceneView.selectedPerson();
    if (scene.currentAction() == null & delayToJump <= 0) {
      if (agent == null || ! agent.actions.canTakeAction()) {
        delayToJump = 30;
      }
    }
    if (delayToJump > 0 && --delayToJump <= 0) {
      sceneView.jumpToNextAgent(agent);
    }
    return true;
  }
  

  String description(Surface surface) {
    final World world = mainView.world();
    final Scene scene = world.activeScene();
    
    if (scene == null) return null;
    if (scene.complete() && ! GameSettings.debugScene) return "";
    
    final StringBuffer s = new StringBuffer();
    final Person   p      = sceneView.selectedPerson();
    final Ability  a      = selectAbility;
    final Action   action = scene.currentAction();
    
    if (p != null) {
      s.append("\nSelection: "+p.name()+" ("+p.side().name().toLowerCase()+")");
      
      final PersonHealth  PH = p.health ;
      final PersonActions PA = p.actions;
      final PersonStats   PS = p.stats  ;
      
      int HP = (int) (PH.maxHealth() - (PH.injury() + PH.stun()));
      int armour = p.stats.levelFor(PersonStats.ARMOUR);
      s.append("\n  Health: "+HP+"/"+PH.maxHealth());
      if (PH.stun() > 0) s.append(" (Stun "+(int) PH.stun()+")");
      if (armour > 0) s.append("\n  Armour: "+armour);
      s.append("\n  Status: "+p.confidenceDescription());
      s.append("\n  AP: "+PA.currentAP()+"/"+PS.maxActionPoints());
      
      if (PH.conscious() && PA.nextAction() != null) {
        s.append("\n  Last action: "+PA.nextAction().used);
      }
      
      Series <Item> equipped = p.gear.equipped();
      if (equipped.size() > 0) {
        s.append("\n  Equipment:");
        for (Item e : equipped) {
          s.append("\n    "+e.name());
        }
      }
      
      Series <Ability> conditions = p.stats.allConditions();
      if (conditions.size() > 0) {
        s.append("\n  Conditions:");
        for (Ability c : conditions) {
          s.append("\n    "+c.name());
        }
      }
      
      boolean canCommand =
        action == null && p.actions.canTakeAction() && p.isPlayerOwned()
      ;
      if (canCommand) {
        s.append("\n\n  Abilities (Press 1-9):");
        char key = '1';
        for (Ability r : p.stats.listAbilities()) {
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
            p.onTurnEnd();
          }
        }
        //  TODO:  Allow zooming to and tabbing through party members.
        //  Note:  Delayed actions only target the self, so no selection is
        //  needed, only confirmation.
        else {
          if (a.delayed() || a.selfOnly()) {
            selectAction = a.configAction(
              p, p.currentTile(), p, scene, null, null
            );
            s.append("\n\n"+a.FX.describeAction(selectAction, scene));
            s.append("\n  Confirm (Y)");
            if (surface.isPressed('y')) {
              confirmActionChoice(selectAction);
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
    s.append("\n");
    s.append("\n  Press S to save, R to reload.");
    if (surface.isPressed('s')) world.performSave();
    if (surface.isPressed('r')) world.reloadFromSave();
    
    return s.toString();
  }
  
  
  
  /**  Rendering previews for an action-
    */
  boolean previewActionDelivery(
    Object hovered, Tile at, Surface surface, Graphics2D g
  ) {
    final Scene scene = mainView.world().activeScene();
    final Person selected = sceneView.selectedPerson();
    if (selectAbility == null || selected == null) return false;
    
    StringBuffer failLog = new StringBuffer();
    selectAction = null;
    if (! (selectAbility.delayed() || selectAbility.selfOnly())) {
      selectAction = selectAbility.configAction(
        selected, at, hovered, scene, null, failLog
      );
    }
    if (selectAction != null) {
      int costAP = selectAction.used.costAP(selectAction);
      selectAction.used.FX.previewAction(selectAction, scene, g);
      sceneView.renderString(at.x, at.y - 0.5f, "AP: "+costAP, Color.GREEN, g);
      return true;
    }
    else {
      sceneView.renderString(at.x, at.y - 0.5f, ""+failLog, Color.RED, g);
      return false;
    }
  }
}




