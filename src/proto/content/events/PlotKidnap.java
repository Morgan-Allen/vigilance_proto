
package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;
import static proto.game.event.PlotUtils.*;



public class PlotKidnap extends Plot {
  
  /**  Data fields, construction and save/load methods:
    */
  final public static Role
    ROLE_TAILS   = Plot.role("kidnap_tails"  , "Tails"  , PERP  ),
    ROLE_GRABS   = Plot.role("kidnap_grabs"  , "Grabs"  , PERP  ),
    ROLE_RANSOMS = Plot.role("kidnap_ransoms", "Ransoms", VICTIM),
    ROLE_HOME    = Plot.role("kidnap_home"   , "Home"   , SCENE );
  
  final public static Step
    STEP_CONTACTS = Step.stepWith(
      "kidnap_initial_contacts", "Initial Contacts",
      ROLE_MASTERMIND, ROLE_HQ, ROLE_ORGANISER, ROLE_HIDEOUT, ROLE_TARGET,
      Lead.MEDIUM_WIRE, 24
    ),
    STEP_TAILING = Step.stepWith(
      "kidnap_tailing_target", "Tailing Target",
      ROLE_TAILS, ROLE_HIDEOUT, ROLE_TARGET, ROLE_SCENE, null,
      Lead.MEDIUM_SURVEIL, 24
    ),
    STEP_GRABS = Step.stepWith(
      "kidnap_grab_target", "Grab Target",
      ROLE_TAILS, ROLE_HIDEOUT, ROLE_TARGET, ROLE_SCENE, null,
      Lead.MEDIUM_ASSAULT, 24, ROLE_GRABS
    ),
    STEP_RANSOM_DEMAND = Step.stepWith(
      "kidnap_ransom_demand", "Ransom Demand",
      ROLE_ORGANISER, ROLE_HIDEOUT, ROLE_RANSOMS, ROLE_HOME, ROLE_TARGET,
      Lead.MEDIUM_WIRE, 24
    ),
    STEP_RANSOM_PAID = Step.stepWith(
      "kidnap_ransom_paid", "Ransom Paid",
      ROLE_RANSOMS, ROLE_HOME, ROLE_ORGANISER, ROLE_HIDEOUT, ROLE_TARGET,
      Lead.MEDIUM_WIRE, 24
    ),
    STEP_REPORT = Step.stepWith(
      "kidnap_reports_and_payoffs", "Reports And Payoffs",
      ROLE_ORGANISER, ROLE_HIDEOUT, ROLE_MASTERMIND, ROLE_HQ, null,
      Lead.MEDIUM_WIRE, 24
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
    fillExpertRole(this, BRAINS  , aides, ROLE_ORGANISER);
    fillExpertRole(this, REFLEXES, aides, ROLE_TAILS    );
    fillExpertRole(this, MUSCLE  , aides, ROLE_GRABS    );
    Place hideout = chooseHideout(this, target.resides());
    assignRole      (ransoms.resides(), ROLE_HOME              );
    assignRole      (ransoms          , ROLE_RANSOMS, ROLE_HOME);
    assignRole      (hideout          , ROLE_HIDEOUT           );
    assignTarget    (target, target.resides(), ROLE_HIDEOUT    );
    assignMastermind(base().leader(), base().HQ()              );
    //
    //  And last but not least, queue up the needed steps:
    queueSteps(
      STEP_CONTACTS,
      STEP_TAILING,
      STEP_GRABS,
      STEP_RANSOM_DEMAND,
      STEP_RANSOM_PAID,
      STEP_REPORT
    );
    return true;
  }
  
  
  protected float ratePlotFor(Person mastermind) {
    float baseFunds = base().finance.publicFunds();
    return cashGained(true) / (100 + baseFunds);
  }
  
  
  protected boolean checkSuccess(Step step) {
    return true;
  }
  
  
  protected void onCompletion(Step step, boolean success) {
    Person target = (Person) target();
    //
    //  Once the target has been grabbed, keep them captive at your hideout:
    if (step == STEP_GRABS) {
      hideout().setAttached(target, true);
      target.setCaptive(true);
    }
    //
    //  And once the ransom is paid, release them close to home:
    if (step == STEP_REPORT) {
      target.removeAssignment(this);
      target.resides().setAttached(target, true);
      target.setCaptive(false);
      base().finance.incPublicFunds((int) cashGained(false));
    }
  }
  
  
  private float cashGained(boolean estimate) {
    Person ransomed = (Person) filling(ROLE_RANSOMS);
    return ransomed.stats.levelFor(INVESTMENT) * 50;
  }
}






