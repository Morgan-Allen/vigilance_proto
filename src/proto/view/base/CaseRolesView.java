

package proto.view.base;
import proto.game.event.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.common.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;



public class CaseRolesView extends UINode {
  
  
  public CaseRolesView(UINode parent, Box2D bounds) {
    super(parent, bounds);
  }
  
  
  protected boolean renderTo(Surface surface, Graphics2D g) {
    //
    //  First, obtain basic references to relevant game objects-
    Base player = mainView.player();
    MapView parent = mainView.mapView;
    Plot plot = (Plot) parent.activeFocus;
    //
    //  Then sort of the roles involved in the crime based on quality of
    //  information-
    class RoleView {
      Role role;
      Series <Clue> clues;
      Series <Element> suspects;
    }
    List <RoleView> roles = new List <RoleView> () {
      protected float queuePriority(RoleView r) {
        if (r.suspects.size() == 1) return 1;
        return 0;
      }
    };
    for (Role role : player.leads.knownRolesFor(plot)) {
      if (Visit.arrayIncludes(Plot.NEVER_SHOW, role)) continue;
      RoleView r = new RoleView();
      r.role     = role;
      r.suspects = player.leads.suspectsFor(role, plot);
      r.clues    = player.leads.cluesFor(plot, role, false);
      roles.queueAdd(r);
    }
    //
    //  Create a list-display, attach the header, add entries for each role/
    //  confirmed suspect, and finally an option to review total evidence.
    int across = 10, down = 10;
    ViewUtils.ListDraw draw = new ViewUtils.ListDraw();
    draw.addEntry(
      null, "CASE FILE: "+CasesFX.nameFor(plot, player), 40, null
    );
    for (RoleView r : roles) {
      Role role = r.role;
      Image icon = null;
      String desc = null;
      Element match = r.suspects.size() == 1 ? r.suspects.first() : null;
      boolean canFollow = r.clues.size() > 0;
      Object refers = null;
      
      if (match != null) {
        icon = match.icon();
        desc = role+": "+match.name();
        refers = match;
      }
      else {
        icon = MapView.MYSTERY_IMAGE;
        desc = role+": Unknown";
        refers = canFollow ? role : null;
        
        if (r.clues.size() > 0) {
          desc += "\n  ";
          for (Clue c : r.clues) {
            desc += CasesFX.shortDescription(c, player)+" ";
          }
          if (! canFollow) desc += "\n  (numerous suspects)";
          else desc += "\n  ("+r.suspects.size()+" suspects)";
        }
        else {
          desc += "\n  (no evidence)";
        }
      }
      draw.addEntry(icon, desc, 40, refers);
    }
    draw.addEntry(
      MapView.FILE_IMAGE, "View All Evidence", 25,
      MapView.PLOT_CLUES
    );
    draw.performVerticalDraw(across, down, this, surface, g);
    down = draw.down;
    //
    //  Then given suitable tool-tips and click-reponses for any encountered
    //  list-elements:
    Object hovered = draw.hovered;
    String hoverDesc = "";
    if (hovered == MapView.PLOT_CLUES) {
      hoverDesc = "Review all evidence assembled on this case.";
      if (draw.clicked) {
        parent.setActiveFocus(MapView.PLOT_CLUES, false);
      }
    }
    else if (hovered instanceof Role) {
      Role role = (Role) hovered;
      Series <Clue> clues = player.leads.cluesFor(plot, role, true);
      Clue top = clues.first();
      if (top != null) {
        hoverDesc = "\nLatest Evidence:  "+CasesFX.longDescription(top, player);
      }
      else {
        hoverDesc = "\nNo Evidence";
      }
      hoverDesc += "\nClick to view suspects.";
      
      if (draw.clicked) {
        parent.setActiveFocus(hovered, false);
      }
    }
    else if (hovered instanceof Element) {
      Element element = (Element) hovered;
      Series <Clue> clues = player.leads.cluesFor(plot, element, true);
      Clue top = clues.first();
      if (top != null) {
        hoverDesc = "\nLatest Evidence:  "+CasesFX.longDescription(top, player);
      }
      else {
        hoverDesc = "\nNo Evidence";
      }
      if (draw.clicked) {
        parent.setActiveFocus(hovered, false);
      }
    }
    g.setColor(Color.LIGHT_GRAY);
    ViewUtils.drawWrappedString(
      hoverDesc, g, vx + across, vy + down, vw - (across + 10), 150
    );
    down += 150;
    
    parent.infoArea.setScrollheight(down);
    return true;
  }
}




