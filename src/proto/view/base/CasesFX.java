

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
    Element   fills     = plot.filling(role);
    Faction   faction   = plot.base().faction();
    
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
    
    String perpDesc = "", pronDesc = "";
    if (fills.isPerson()) pronDesc = "someone";
    if (fills.isPlace ()) pronDesc = "somewhere";
    if (fills.isItem  ()) pronDesc = "something";
    
    if (clue.isConfirmation()) {
      perpDesc = match.name();
    }
    if (clue.isTraitClue()) {
      if (fills.isPerson()) perpDesc = pronDesc+" with "+trait;
      else                  perpDesc = pronDesc+" "     +trait;
    }
    if (clue.isLocationClue()) {
      if (nearRange == 0) perpDesc = pronDesc+" at "  +location;
      else                perpDesc = pronDesc+" near "+location;
    }
    
    String roleDesc = role.descTemplate;
    roleDesc = roleDesc.replace("<faction>", faction.name);
    roleDesc = roleDesc.replace("<suspect>", perpDesc);
    roleDesc = roleDesc.replace("<plot>"   , nameFor(plot, base));
    
    for (Role r : plot.allRoles()) {
      Series <Element> suspects = base.leads.suspectsFor(role, plot);
      String otherDesc = "";
      if (suspects.size() == 1) otherDesc = suspects.first().name();
      else otherDesc = "the "+r.name.toLowerCase();
      roleDesc = roleDesc.replace(r.entryKey(), otherDesc);
    }
    
    desc.append(" indicates ");
    desc.append(roleDesc);
    
    return desc.toString();
  }
  
  
  public static String shortDescription(Clue clue, Base base) {
    return ""+clue;
  }
  
  
  public static String nameFor(Plot plot, Base base) {
    
    CaseFile  file      = base.leads.caseFor(plot);
    EventType type      = plot.type;
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
    
    String name = "Unknown Plot";
    if      (targKnown && aimKnown) name = type.name+" of "+plot.target();
    else if (targKnown            ) name += " (target: "+plot.target()+")";
    else if (aimKnown             ) name += " ("+type.name+")";
    return name;
  }
  
}


