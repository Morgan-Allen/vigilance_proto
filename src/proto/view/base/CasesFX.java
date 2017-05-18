

package proto.view.base;
import proto.common.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.util.*;



public class CasesFX {
  
  
  public static String longDescription(Clue clue, Base base) {
    
    World     world     = base.world();
    Lead      source    = clue.source();
    Plot      plot      = clue.plot();
    Role      role      = clue.role();
    Element   match     = clue.match();
    Trait     trait     = clue.trait();
    Element   location  = clue.locationNear();
    int       nearRange = clue.nearRange();
    Lead.Type leadType  = clue.leadType();
    
    StringBuffer desc = new StringBuffer();
    if (leadType != null) desc.append(leadType.name  );
    else                  desc.append("Investigation");
    
    if (source != null) {
      desc.append(" by ");
      Series <Person> did = source.onceActive();
      for (Person p : did) {
        if (p == did.first()) desc.append(""+p);
        else if (p == did.last()) desc.append(" and "+p);
        else desc.append(", "+p);
      }
    }
    
    desc.append(" at "+clue.place());
    desc.append(" at "+world.timing.timeString(clue.time()));
    desc.append(" indicates that "+nameFor(plot, base)+"'s "+role);
    
    if (clue.isConfirmation()) {
      if (match.isPerson()) desc.append(" is "+match+" at "+location);
      else desc.append(" is "+match);
    }
    if (clue.isTraitClue()) {
      desc.append(" has "+trait);
    }
    if (clue.isLocationClue()) {
      if (nearRange == 0) desc.append(" is at "+location);
      else                desc.append(" is near "+location);
    }
    
    return desc.toString();
  }
  
  
  public static String shortDescription(Clue clue, Base base) {
    return ""+clue;
  }
  
  
  public static String nameFor(Plot plot, Base base) {
    
    CaseFile  file      = base.leads.caseFor(plot);
    EventType type      = plot.type;
    int       ID        = file.caseID();
    boolean   aimKnown  = false;
    boolean   targKnown = false;
    
    for (Clue clue : file.clues()) {
      if (clue.isAim()) {
        aimKnown = true;
      }
      if (clue.isConfirmation() && clue.role() == Plot.ROLE_TARGET) {
        targKnown = true;
      }
    }
    
    String name = "Case No. "+ID;
    if      (targKnown && aimKnown) name = type.name+": "+plot.target();
    else if (targKnown            ) name += " (target: "+plot.target()+")";
    else if (aimKnown             ) name += " ("+type.name+")";
    return name;
  }
  
}


