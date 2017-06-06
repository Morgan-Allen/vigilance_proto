
package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import static proto.game.event.PlotUtils.*;
import static proto.game.event.LeadType.*;



public class PlotAssassinate extends Plot {
  
  /**  Data fields, construction and save/load methods-
    */
  public PlotAssassinate(PlotType type, Base base) {
    super(type, base);
  }
  
  
  public PlotAssassinate(Session s) throws Exception {
    super(s);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
  }
  
  
  
  /**  Plot evaluation, execution and life cycle:
    */
  protected boolean fillRoles() {
    World world = base().world();
    Person boss = base().leader();
    //
    //  First, decide who you intend to murder:
    Pick <Person> pickT = new Pick(0);
    for (Person p : world.civilians()) {
      pickT.compare(p, 0 - boss.history.bondWith(p));
    }
    if (pickT.empty()) return false;
    Person target = pickT.result();
    //
    //  Assign these elements their appropriate roles:
    Series <Person> aides = aidesOnRoster(this);
    fillExpertRole(this, BRAINS  , aides, ROLE_ORGANISER);
    Place hideout = chooseHideout(this, target.resides(), base().HQ());
    assignRole      (hideout, ROLE_HIDEOUT                );
    assignTarget    (target , target.resides(), ROLE_SCENE);
    assignMastermind(base().leader(), base().HQ()         );
    //
    //  And finally return-
    return true;
  }
  
  
  protected float ratePlotFor(Person mastermind) {
    return mastermind.history.bondWith(target()) * -3;
  }
  
  
  protected boolean checkSuccess() {
    return true;
  }
  
  
  protected void onCompletion(boolean success) {
    Person target = (Person) target();
    if (success) {
      target.health.setState(PersonHealth.State.DECEASED);
    }
  }
  
}





