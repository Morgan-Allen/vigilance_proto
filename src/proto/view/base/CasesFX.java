

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
    Step      step      = clue.step();
    Element   match     = clue.match();
    Trait     trait     = clue.trait();
    Element   location  = clue.locationNear();
    int       nearRange = clue.nearRange();
    Lead.Type leadType  = clue.leadType();
    Element   fills     = plot.filling(role);
    Faction   faction   = plot.base().faction();
    int       plotTime  = plot.timeScheduled(plot.mainHeist());
    
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
    if      (fills == null   ) pronDesc = "nothing";
    else if (fills.isPerson()) pronDesc = "someone";
    else if (fills.isPlace ()) pronDesc = "an unknown location";
    else if (fills.isItem  ()) pronDesc = "something";
    
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
    if (clue.isAim()) {
      
    }
    
    Table <String, String> replacements = new Table();
    replacements.put("<faction>", faction.name       );
    replacements.put("<suspect>", perpDesc           );
    replacements.put("<plot>"   , nameFor(plot, base));
    replacements.put("<aim>"    , plot.type.name     );
    for (Role r : plot.allRoles()) {
      Series <Element> suspects = base.leads.suspectsFor(r, plot);
      String otherDesc = "";
      if (suspects.size() == 1) otherDesc = suspects.first().name();
      else otherDesc = "an unknown "+r.name.toLowerCase();
      replacements.put("<"+r.entryKey()+">", otherDesc);
    }
    
    String stepDesc = step.descTemplate();
    stepDesc = replaceKeywords(stepDesc, replacements);
    replacements.put("<step>", stepDesc);
    replacements.put("<time>", world.timing.timeString(plotTime));
    
    String roleDesc = replaceKeywords(role.descTemplate, replacements);
    desc.append(" indicates ");
    desc.append(roleDesc);
    
    return desc.toString();
  }
  
  
  static String replaceKeywords(
    String template, Table <String, String> replacements
  ) {
    for (String key : replacements.keySet()) {
      template = template.replace(key, replacements.get(key));
    }
    return template;
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
    else if (targKnown            ) name += " (Target: "+plot.target()+")";
    else if (aimKnown             ) name = ""+type.name+" (Unknown Target)";
    return name;
  }
  
}


