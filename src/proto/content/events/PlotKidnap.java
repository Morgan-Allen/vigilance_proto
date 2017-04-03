
package proto.content.events;
import proto.common.*;
import proto.game.event.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import static proto.game.person.PersonStats.*;



public class PlotKidnap extends Plot {
  
  
  final public static Plot.Role
    ROLE_TAILS   = new Plot.Role("role_tails"  , "Tails"  , PERP  ),
    ROLE_GRABS   = new Plot.Role("role_grabs"  , "Grabs"  , PERP  ),
    ROLE_DRUGS   = new Plot.Role("role_drugs"  , "Drugs"  , PERP  ),
    ROLE_RANSOMS = new Plot.Role("role_ransoms", "Ransoms", VICTIM);
  
  
  Step ransomStep;
  boolean returnedHostage;
  
  
  public PlotKidnap(PlotType type, Base base) {
    super(type, base);
  }
  
  
  public PlotKidnap(Session s) throws Exception {
    super(s);
    ransomStep = (Step) s.loadObject();
    returnedHostage = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(ransomStep);
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
    for (Element e : ransoms.history.sortedBonds()) if (e.isPerson()) {
      pickT.compare((Person) e, ransoms.history.bondWith(e));
    }
    if (pickT.empty()) return false;
    Person target = pickT.result();

    Pick <Place > pickH = new Pick();
    for (Region r : world.regionsInRange(target.region(), 1)) {
      for (Place b : r.buildSlots()) {
        if (b == null || b.isBase()) continue;
        pickH.compare(b, Rand.num());
      }
    }
    if (pickH.empty()) return false;
    Place hideout = pickH.result();
    
    Series <Person> goons = PlotUtils.goonsOnRoster(this);
    assignRole(ransoms, ROLE_RANSOMS);
    assignTarget   (target, target.resides());
    assignOrganiser(base().leader(), hideout);
    PlotUtils.fillExpertRole(this, SIGHT_RANGE, goons, ROLE_TAILS);
    PlotUtils.fillExpertRole(this, MEDICINE   , goons, ROLE_DRUGS);
    PlotUtils.fillExpertRole(this, MUSCLE     , goons, ROLE_GRABS);
    assignRole(filling(ROLE_GRABS), Plot.ROLE_ENFORCER);
    
    queueMeeting(
      "initial meeting",
      Plot.ROLE_ORGANISER, ROLE_TAILS, ROLE_DRUGS
    );
    queueMessage(
      "gives info",
      Plot.ROLE_ORGANISER,
      Plot.ROLE_ENFORCER,
      Plot.ROLE_TARGET
    );
    queueStep(
      "tails victim",
      Lead.MEDIUM_SURVEIL,
      World.HOURS_PER_DAY,
      ROLE_TAILS, Plot.ROLE_TARGET
    );
    queueMeeting(
      "measures dose",
      ROLE_TAILS, ROLE_DRUGS, ROLE_GRABS
    );
    queueHeist(
      "heist",
      Plot.ROLE_TARGET, ROLE_TAILS, ROLE_GRABS, Plot.ROLE_ENFORCER
    );
    ransomStep = queueStep(
      "ransom",
      Lead.MEDIUM_WIRE,
      World.HOURS_PER_DAY,
      Plot.ROLE_ORGANISER, ROLE_RANSOMS
    );
    return false;
  }
  
  
  protected boolean checkSuccess(Step step) {
    return true;
  }
  
  
  protected void onCompletion(Step step, boolean success) {
    Person target = (Person) target();
    if (step.isHeist()) {
      hideout().setAttached(target, true);
    }
    else if (step == ransomStep) {
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













