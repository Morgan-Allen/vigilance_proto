
package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import static proto.game.event.PlotUtils.*;
import static proto.game.event.LeadType.*;



public class PlotKidnap extends Plot {
  
  /**  Data fields, construction and save/load methods:
    */
  final public static Role
    ROLE_RANSOMS = role(
      "kidnap_ransoms", "Ransoms", VICTIM,
      "<suspect> is to be ransomed in <plot>."
    ),
    ROLE_HOME = role(
      "kidnap_home", "Home", SCENE,
      "<suspect> is the home for <kidnap_ransoms>."
    );
  
  boolean returnedHostage;
  
  
  public PlotKidnap(PlotType type, Base base) {
    super(type, base);
  }
  
  
  public PlotKidnap(Session s) throws Exception {
    super(s);
    returnedHostage = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveBool(returnedHostage);
  }
  
  
  
  /**  Plot evaluation, execution and life cycle:
    */
  protected boolean fillRoles() {
    World world = base().world();
    //
    //  First, decide who you intend to ransom:
    Pick <Person> pickR = new Pick();
    for (Person p : world.civilians()) {
      if (p.resides() == null) continue;
      pickR.compare(p, p.stats.levelFor(INVESTMENT));
    }
    if (pickR.empty()) return false;
    Person ransoms = pickR.result();
    //
    //  Then, pick someone that they're attached to:
    Pick <Person> pickT = new Pick(0);
    for (Element e : ransoms.history.sortedBonds()) {
      if (e == ransoms || ! e.isPerson()) continue;
      Person p = (Person) e;
      if (! p.isCivilian()) continue;
      pickT.compare(p, ransoms.history.bondWith(e));
    }
    if (pickT.empty()) return false;
    Person target = pickT.result();
    //
    //  Assign these elements their appropriate roles:
    Series <Person> aides = aidesOnRoster(this);
    fillExpertRole(this, BRAINS, aides, ROLE_ORGANISER);
    Place hideout = chooseHideout(this, target.resides(), base().HQ());
    assignRole      (ransoms.resides(), ROLE_HOME              );
    assignRole      (ransoms          , ROLE_RANSOMS, ROLE_HOME);
    assignRole      (hideout          , ROLE_HIDEOUT           );
    assignTarget    (target, target.resides(), ROLE_HIDEOUT    );
    assignMastermind(base().leader(), base().HQ()              );
    //
    //  And finally return true-
    return true;
  }
  
  
  protected int calcHoursDuration() {
    return World.HOURS_PER_DAY * 7;
  }
  
  
  protected float ratePlotFor(Person mastermind) {
    float baseFunds = base().finance.publicFunds();
    return cashGained(true) / (100 + baseFunds);
  }
  
  
  private float cashGained(boolean estimate) {
    Person ransomed = (Person) filling(ROLE_RANSOMS);
    return ransomed.stats.levelFor(INVESTMENT) * 50;
  }
  
  
  protected boolean checkSuccess() {
    return true;
  }
  
  
  protected void onCompletion(boolean success) {
    Person target = (Person) target();
    
    //  TODO:  You might actually need Steps for this to work (but it can be
    //  much more basic.)
    
    /*
    //
    //  Once the target has been grabbed, keep them captive at your hideout:
    if (step == STEP_GRABS) {
      hideout().setAttached(target, true);
      target.setCaptive(true);
    }
    //*/
    //
    //  And once the ransom is paid, release them close to home:
    if (success) {
      target.removeAssignment(this);
      target.resides().setAttached(target, true);
      target.setCaptive(false);
      base().finance.incPublicFunds((int) cashGained(false));
    }
  }
}






