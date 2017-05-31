
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
  final public static Role
    ROLE_CASING = role(
      "robbery_casing", "Casing", PERP,
      "<suspect> is casing <role_scene> as part of <plot>."
    ),
    ROLE_CRACKER = role(
      "robbery_cracker", "Cracker", PERP,
      "<suspect> is responsible for cracking safes during <plot>."
    ),
    ROLE_HACKER = role(
      "robbery_hacker", "Hacker", PERP,
      "<suspect> is responsible for bypassing security during <plot>."
    ),
    ROLE_OFFICE = role(
      "robbery_office", "Office", SCENE,
      "<robbery_hacker> is going to look up blueprints at <suspect>."
    ),
    ROLE_FENCE = role(
      "robbery_fence", "Fence", PERP,
      "<suspect> is fencing goods on behalf of <faction>."
    );
  
  final public static Step
    STEP_CONTACTS = Step.stepWith(
      "robbery_contacts", "Contacts",
      "",
      ROLE_MASTERMIND, ROLE_HQ, ROLE_ORGANISER, ROLE_HIDEOUT, null,
      MEDIUM_WIRE, 24
    ),
    STEP_CASING = Step.stepWith(
      "robbery_casing", "Casing",
      "",
      ROLE_CASING, ROLE_HIDEOUT, ROLE_TARGET, ROLE_SCENE, null,
      MEDIUM_SURVEIL, 24
    ),
    STEP_RESEARCH = Step.stepWith(
      "robbery_research", "Research",
      "",
      ROLE_HACKER, ROLE_HIDEOUT, ROLE_OFFICE, ROLE_OFFICE, null,
      MEDIUM_MEETING, 24
    ),
    STEP_HEIST = Step.stepWith(
      "robbery_heist", "Heist",
      "",
      ROLE_CASING, ROLE_HIDEOUT, ROLE_TARGET, ROLE_SCENE, null,
      MEDIUM_ASSAULT, 24, ROLE_CRACKER, ROLE_HACKER
    ),
    STEP_VALUATION = Step.stepWith(
      "robbery_valuation", "Valuation and Fencing",
      "",
      ROLE_ORGANISER, ROLE_HIDEOUT, ROLE_FENCE, ROLE_HIDEOUT, null,
      MEDIUM_MEETING, 24, ROLE_CASING, ROLE_CRACKER, ROLE_HACKER
    ),
    STEP_REPORT = Step.stepWith(
      "robbery_report", "Report and Payoffs",
      "",
      ROLE_ORGANISER, ROLE_HIDEOUT, ROLE_MASTERMIND, ROLE_HQ, null,
      MEDIUM_WIRE, 24
    );
  
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
    fillExpertRole(this, REFLEXES   , aides, ROLE_CASING   );
    fillExpertRole(this, ENGINEERING, aides, ROLE_CRACKER  );
    fillExpertRole(this, ENGINEERING, aides, ROLE_HACKER   );
    fillExpertRole(this, INVESTMENT , aides, ROLE_FENCE    );
    Place hideout = chooseHideout(this, target, base().HQ());
    assignRole      (office , ROLE_OFFICE        );
    assignRole      (hideout, ROLE_HIDEOUT       );
    assignTarget    (target , target, ROLE_SCENE );
    assignMastermind(base().leader(), base().HQ());
    //
    //  And last but not least, queue up the needed steps:
    queueSteps(
      STEP_CONTACTS,
      STEP_CASING,
      STEP_RESEARCH,
      STEP_HEIST,
      STEP_VALUATION,
      STEP_REPORT
    );
    return true;
  }
  
  
  protected float ratePlotFor(Person mastermind) {
    float baseFunds = base().finance.publicFunds();
    return cashGained(true) / (100 + baseFunds);
  }
  
  
  private float cashGained(boolean estimate) {
    return estimate ? 400 : (300 + Rand.index(201));
  }


  protected boolean checkSuccess(Step step) {
    return true;
  }
  
  
  protected void onCompletion(Step step, boolean success) {
    if (step == STEP_REPORT) {
      base().finance.incPublicFunds((int) cashGained(false));
    }
  }
  
}





