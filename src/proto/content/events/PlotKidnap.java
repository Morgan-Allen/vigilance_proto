
package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class PlotKidnap extends Plot {
  
  
  final public static Plot.Role
    ROLE_TAILS       = new Plot.Role("role_tails"      , "Tails"      , PERP  ),
    ROLE_RANSOMS     = new Plot.Role("role_ransoms"    , "Ransoms"    , VICTIM),
    ROLE_RANSOM_HOME = new Plot.Role("role_ransom_home", "Ransom Home", VICTIM);
  
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
  
  
  protected boolean fillRoles() {
    World world = base().world();
    Pick <Person> pickR = new Pick();
    
    for (Person p : world.civilians()) {
      if (p.resides() == null) continue;
      pickR.compare(p, p.stats.levelFor(INVESTMENT));
    }
    if (pickR.empty()) return false;
    Person ransoms = pickR.result();
    
    Pick <Person> pickT = new Pick(0);
    for (Element e : ransoms.history.sortedBonds()) {
      if (e == ransoms || ! e.isPerson()) continue;
      pickT.compare((Person) e, ransoms.history.bondWith(e));
    }
    if (pickT.empty()) return false;
    Person target = pickT.result();
    
    Series <Person> aides = PlotUtils.aidesOnRoster(this);
    assignTarget(target, target.resides());
    assignRole(target , ROLE_TARGET );
    assignRole(ransoms, ROLE_RANSOMS);
    assignRole(ransoms.resides(), ROLE_RANSOM_HOME);
    assignOrganiser(base().leader(), base());
    
    PlotUtils.fillHideoutRole(this, scene());
    PlotUtils.fillExpertRole(this, BRAINS  , aides, ROLE_ENFORCER);
    PlotUtils.fillExpertRole(this, REFLEXES, aides, ROLE_TAILS   );
    
    Step.queueStep(
      "initial contacts", this,
      ROLE_ORGANISER, ROLE_BASE, ROLE_ENFORCER, ROLE_HIDEOUT,
      Lead.MEDIUM_WIRE, 24, ROLE_TAILS
    );
    Step.queueStep(
      "tailing target", this,
      ROLE_TAILS, ROLE_HIDEOUT, ROLE_TARGET, ROLE_SCENE,
      Lead.MEDIUM_SURVEIL, 24
    );
    Step.queueStep(
      "grab target", this,
      ROLE_TAILS, ROLE_HIDEOUT, ROLE_TARGET, ROLE_SCENE,
      Lead.MEDIUM_HEIST, 24, ROLE_ENFORCER
    );
    Step.queueStep(
      "deliver ransom", this,
      ROLE_ENFORCER, ROLE_HIDEOUT, ROLE_RANSOMS, ROLE_RANSOM_HOME,
      Lead.MEDIUM_WIRE, 24
    );
    Step.queueStep(
      "ransom paid", this,
      ROLE_RANSOMS, ROLE_RANSOM_HOME, ROLE_ENFORCER, ROLE_HIDEOUT,
      Lead.MEDIUM_WIRE, 24
    );
    Step.queueStep(
      "reports and payoffs", this,
      ROLE_ENFORCER, ROLE_HIDEOUT, ROLE_ORGANISER, ROLE_BASE,
      Lead.MEDIUM_WIRE, 24, ROLE_TAILS
    );
    
    return true;
  }
  
  
  protected boolean checkSuccess(Step step) {
    return true;
  }
  
  
  protected void onCompletion(Step step, boolean success) {
    Person target = (Person) target();
    if (step.hasLabel("grab target")) {
      hideout().setAttached(target, true);
    }
    else if (step.hasLabel("ransom paid")) {
      target.resides().setAttached(target, true);
      Person ransomed = (Person) filling(ROLE_RANSOMS);
      float cashGained = ransomed.stats.levelFor(INVESTMENT) * 50;
      base().finance.incPublicFunds((int) cashGained);
    }
  }
  
  
  protected float ratePlotFor(Person mastermind) {
    Person ransomed = (Person) filling(ROLE_RANSOMS);
    float cashGained = ransomed.stats.levelFor(INVESTMENT) * 50;
    float baseFunds = base().finance.publicFunds();
    return cashGained / (100 + baseFunds);
  }
}


