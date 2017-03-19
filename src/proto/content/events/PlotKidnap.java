

package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class PlotKidnap extends Plot {
  
  
  final static Plot.Role
    ROLE_TAILS = new Plot.Role("role_tails", "Tails"),
    ROLE_GRABS = new Plot.Role("role_grabs", "Grabs"),
    ROLE_DRUGS = new Plot.Role("role_drugs", "Drugs");
  
  
  public PlotKidnap(PlotType type, Base base) {
    super(type, base);
  }
  
  
  public PlotKidnap(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  protected boolean fillRoles() {
    
    World world = base().world();
    Pick <Person> pickT = new Pick();
    Pick <Place > pickH = new Pick();
    
    for (Region r : world.regions()) for (Place b : r.buildSlots()) {
      if (b == null || b.isBase()) continue;
      
      for (Person p : b.residents()) {
        //  TODO:  Use closeness to another, wealthy person here...
        pickT.compare(p, Rand.num());
      }
      //  TODO:  Use anonymity or something (?) to select a hideout here...
      pickH.compare(b, Rand.num());
    }
    if (pickT.empty() || pickH.empty()) return false;
    
    
    Person target = pickT.result();
    Place hideout = pickH.result();
    Series <Person> goons = PlotUtils.goonsOnRoster(this);
    
    assignTarget   (target, target.resides());
    assignOrganiser(base().leader(), hideout);
    PlotUtils.fillExpertRole(this, SIGHT_RANGE, goons, ROLE_TAILS);
    PlotUtils.fillExpertRole(this, MEDICINE   , goons, ROLE_DRUGS);
    PlotUtils.fillExpertRole(this, MUSCLE     , goons, ROLE_GRABS);
    assignRole(filling(ROLE_GRABS), Plot.ROLE_ENFORCER);
    
    queueSteps(
      Lead.MEDIUM_MEET, World.HOURS_PER_DAY, Plot.ROLE_ORGANISER,
      ROLE_TAILS, ROLE_DRUGS
    );
    queueStep(
      Lead.MEDIUM_MEET,
      World.HOURS_PER_DAY,
      Plot.ROLE_ORGANISER, Plot.ROLE_ENFORCER
    ).setInfoGiven(Plot.ROLE_TARGET);
    
    queueStep(
      Lead.MEDIUM_SURVEIL,
      World.HOURS_PER_DAY,
      ROLE_TAILS, Plot.ROLE_TARGET
    );
    queueStep(
      Lead.MEDIUM_MEET,
      World.HOURS_PER_DAY,
      ROLE_TAILS, ROLE_DRUGS, ROLE_GRABS
    );
    queueStep(
      Lead.MEDIUM_HEIST,
      World.HOURS_PER_DAY,
      Plot.ROLE_TARGET, ROLE_TAILS, ROLE_GRABS, Plot.ROLE_ENFORCER
    );
    
    return false;
  }
  
  
  protected void onCompletion(Step step) {
    
  }
  
  
  
  
}









