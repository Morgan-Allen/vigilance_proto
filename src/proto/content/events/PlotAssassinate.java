
package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import static proto.game.person.PersonStats.*;
import static proto.game.event.PlotUtils.*;



public class PlotAssassinate extends Plot {
  
  /**  Data fields, construction and save/load methods-
    */
  final public static Role
    ROLE_MOLE = role(
      "assass_mole", "Mole", PERP,
      "<suspect> is acting as a mole for <faction>."
    ),
    ROLE_SCOUTS = role(
      "assass_scouts", "Scouts", PERP,
      "<suspect> is scouting for potential vantage points in connection with "+
      "<plot>."
    ),
    ROLE_SHOOTER = role(
      "assass_shooter", "Shooter", PERP,
      "<suspect> is the shooter for <plot>."
    );
  
  
  final public static Step
    STEP_CONTACTS = Step.stepWith(
      "assass_contacts", "Contacts",
      "",
      ROLE_MASTERMIND, ROLE_HQ, ROLE_ORGANISER, ROLE_HIDEOUT, null,
      Lead.MEDIUM_WIRE, 24
    ),
    STEP_INFILTRATE = Step.stepWith(
      "assass_infiltrate", "Infiltrate Security",
      "",
      ROLE_MOLE, ROLE_HIDEOUT, ROLE_SCENE, ROLE_SCENE, null,
      Lead.MEDIUM_COVER, 24
    ),
    STEP_STUDY = Step.stepWith(
      "assass_study", "Study Schedule",
      "",
      ROLE_MOLE, ROLE_SCENE, ROLE_TARGET, ROLE_SCENE, null,
      Lead.MEDIUM_COVER, 24
    ),
    STEP_SCOUTING = Step.stepWith(
      "assass_scouting", "Scouting Area",
      "",
      ROLE_SCOUTS, ROLE_HIDEOUT, ROLE_TARGET, ROLE_SCENE, null,
      Lead.MEDIUM_SURVEIL, 24
    ),
    STEP_SHOOTING = Step.stepWith(
      "assass_shooting", "Shooting",
      "",
      ROLE_SHOOTER, ROLE_HIDEOUT, ROLE_TARGET, ROLE_SCENE, null,
      Lead.MEDIUM_ASSAULT, 24, ROLE_SCOUTS
    ),
    STEP_REPORT = Step.stepWith(
      "assass_report", "Report and Payoffs",
      "",
      ROLE_ORGANISER, ROLE_HIDEOUT, ROLE_MASTERMIND, ROLE_HQ, null,
      Lead.MEDIUM_WIRE, 24
    );
  
  
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
    fillExpertRole(this, BRAINS  , aides, ROLE_ORGANISER       );
    fillExpertRole(this, ACCURACY, aides, ROLE_SHOOTER         );
    fillExpertRole(this, REFLEXES, aides, ROLE_SCOUTS          );
    fillExpertRole(this, PERSUADE, aides, ROLE_MOLE, ROLE_SCENE);
    Place hideout = chooseHideout(this, target.resides());
    assignRole      (hideout, ROLE_HIDEOUT                );
    assignTarget    (target , target.resides(), ROLE_SCENE);
    assignMastermind(base().leader(), base().HQ()         );
    //
    //  And last but not least, queue up the needed steps:
    queueSteps(
      STEP_CONTACTS,
      STEP_INFILTRATE,
      STEP_STUDY,
      STEP_SCOUTING,
      STEP_SHOOTING,
      STEP_REPORT
    );
    return true;
  }
  
  
  protected float ratePlotFor(Person mastermind) {
    return mastermind.history.bondWith(target()) * -1000;
  }


  protected boolean checkSuccess(Step step) {
    return true;
  }
  
  
  protected void onCompletion(Step step, boolean success) {
    Person target = (Person) target();
    if (step == STEP_SHOOTING) {
      target.health.setState(PersonHealth.State.DECEASED);
    }
  }
  
}








