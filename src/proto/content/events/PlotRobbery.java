
package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import static proto.game.event.PlotUtils.*;
import static proto.game.event.LeadType.*;
import proto.content.agents.Civilians;
import proto.content.places.Facilities;



public class PlotRobbery extends Plot {
  
  /**  Data fields, construction and save/load methods-
    */
  final static PlaceType THEFT_VENUES[] = {
    Facilities.BUSINESS_PARK,
    Facilities.LOUNGE,
    Civilians.PENTHOUSE
  };
  
  
  public PlotRobbery(PlotType type, Base base) {
    super(type, base);
  }
  
  
  public PlotRobbery(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  
  /**  Plot evaluation, execution and life cycle:
    */
  protected boolean fillRoles() {
    World world = base().world();
    //
    //  First, decide where you intend to rob:
    Pick <Place> pickT = new Pick(0);
    for (Place p : world.publicPlaces()) {
      if (! Visit.arrayIncludes(THEFT_VENUES, p.kind())) continue;
      if (p.base() == base()) continue;
      pickT.compare(p, Rand.num());
    }
    if (pickT.empty()) return false;
    Place target = pickT.result();
    //
    //  Second, if possible, find out where to get building records:
    Pick <Place> pickO = new Pick();
    for (Place p : world.council.courts()) {
      pickO.compare(p, 0 - world.distanceBetween(target.region(), p.region()));
    }
    Place office = pickO.result();
    //
    //  Assign these elements their appropriate roles:
    Series <Person> aides = aidesOnRoster(this);
    fillExpertRole(this, BRAINS     , aides, ROLE_ORGANISER);
    Place hideout = chooseHideout(this, target, base().HQ());
    assignRole      (hideout, ROLE_HIDEOUT       );
    assignTarget    (target , target, ROLE_SCENE );
    assignMastermind(base().leader(), base().HQ());
    //
    //  And finally return true-
    return true;
  }
  
  
  protected float ratePlotFor(Person mastermind) {
    float baseFunds = base().finance.publicFunds();
    return cashGained(true) / (100 + baseFunds);
  }
  
  
  private float cashGained(boolean estimate) {
    return estimate ? 400 : (300 + Rand.index(201));
  }
  
  
  protected boolean checkSuccess() {
    return true;
  }
  
  
  protected void onCompletion(boolean success) {
    if (success) {
      base().finance.incPublicFunds((int) cashGained(false));
    }
  }
  
}





