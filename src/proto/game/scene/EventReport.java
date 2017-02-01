

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.game.event.*;
import proto.util.*;
import proto.view.common.MainView;
import proto.view.common.MessageView;



public class EventReport implements Session.Saveable {
  
  
  final static String FORCE_DESC[] = {
    "None", "Minimal", "Moderate", "Heavy", "EXCESSIVE"
  };
  final static String COLLATERAL_DESC[] = {
    "None", "Minimal", "Medium", "Heavy", "TOTAL"
  };
  final static String GETAWAYS_DESC[] = {
    "None", "Few", "Some", "Many", "ALL"
  };
  
  
  public int outcomeState = Scene.STATE_INIT;
  public float forceRating, collateralRating, getawaysRating;
  public float trustEffect, deterEffect;
  public List <Person> involved = new List();
  public List <Lead> newLeads = new List();
  
  
  public EventReport() {
    return;
  }
  
  
  public EventReport(Session s) throws Exception {
    s.cacheInstance(this);
    outcomeState = s.loadInt();
    forceRating      = s.loadFloat();
    collateralRating = s.loadFloat();
    getawaysRating   = s.loadFloat();
    trustEffect      = s.loadFloat();
    deterEffect      = s.loadFloat();
    s.loadObjects(involved);
    s.loadObjects(newLeads);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveInt(outcomeState);
    s.saveFloat(forceRating     );
    s.saveFloat(collateralRating);
    s.saveFloat(getawaysRating  );
    s.saveFloat(trustEffect     );
    s.saveFloat(deterEffect     );
    s.saveObjects(involved);
    s.saveObjects(newLeads);
  }
  
  
  public void composeFromScene(Scene scene) {
    if (outcomeState != Scene.STATE_INIT) I.complain("Already composed!");
    
    float sumAway = 0, sumForce = 0, numCrooks = 0;
    float sumHurt = 0, numCivilians = 0;
    for (Person p : scene.didEnter()) {
      if (p.isHero()) {
        
      }
      else if (p.isCivilian()) {
        sumHurt += rateDamage(p);
        numCivilians++;
      }
      else {
        sumForce += rateDamage(p);
        if (p.health.conscious() || p.currentScene() != scene) sumAway++;
        numCrooks++;
      }
      involved.add(p);
    }
    
    outcomeState = scene.wasWon() ? Scene.STATE_WON : Scene.STATE_LOST;
    forceRating      = sumForce / Nums.max(1, numCrooks   );
    collateralRating = sumHurt  / Nums.max(1, numCivilians);
    getawaysRating   = sumAway  / Nums.max(1, numCrooks   );

    final boolean playerWon  = outcomeState == Scene.STATE_WON;
    deterEffect += playerWon ? 10 : 0;
    deterEffect += (forceRating * 5) - (getawaysRating * 20);
    trustEffect += playerWon ? 10 : 0;
    trustEffect -= (forceRating * 20) + (collateralRating * 40);
  }
  
  
  float rateDamage(Person p) {
    float damage = p.health.injury() / p.health.maxHealth();
    if (! p.health.alive()) damage += 3;
    return damage / 2f;
  }
  
  
  public void composeFromEvent(
    Event event, float collateral, float getaways
  ) {
    if (outcomeState != Scene.STATE_INIT) I.complain("Already composed!");
    this.outcomeState     = Scene.STATE_ABSENT;
    this.forceRating      = 0;
    this.collateralRating = collateral;
    this.getawaysRating   = getaways;
    deterEffect -= 5;
    trustEffect -= collateral * 10;
  }
  
  
  public void applyOutcomeEffects(Place place) {
    final Region region = place.region();
    region.nudgeCurrentStat(Region.DETERRENCE, deterEffect);
    region.nudgeCurrentStat(Region.TRUST     , trustEffect);
  }
  
  
  public boolean playerWon   () { return outcomeState == Scene.STATE_WON   ; }
  public boolean playerLost  () { return outcomeState == Scene.STATE_LOST  ; }
  public boolean playerAbsent() { return outcomeState == Scene.STATE_ABSENT; }
  
  
  
  void presentMessageForScene(Scene scene) {
    
    StringBuffer h = new StringBuffer();
    h.append("\nMission ");
    if (playerWon()) h.append(" Successful: "+scene);
    else h.append(" Failed: "+scene);

    StringBuffer s = new StringBuffer();
    
    //  TODO:  You need to report on injured or hospitalised subjects as well.
    s.append("\nPersonnel Status:");
    for (Person p : involved) {
      if (p.isHero()) {
        if (! p.health.alive()) {
          s.append("\n  "+p.name());
          s.append(" (dead)");
        }
        else if (! p.health.conscious()) {
          s.append("\n  "+p.name());
          s.append(playerWon() ? " (unconscious)" : " (captive)");
        }
      }
      else {
        if (p.currentScene() != scene) {
          s.append("\n  "+p.name());
          s.append(" (escaped)");
        }
        else if (! p.health.alive()) {
          s.append("\n  "+p.name());
          s.append(" (dead)");
        }
        else if (! p.health.conscious()) {
          s.append("\n  "+p.name());
          s.append(playerWon() ? " (captive)" : " (unconscious)");
          int inj = (int) p.health.injury(), maxH = p.health.maxHealth();
          s.append(" (injury "+inj+"/"+maxH+")");
        }
      }
    }
    
    s.append("\n\n");
    s.append("\nUse of Force: "+descFrom(forceRating     , FORCE_DESC     ));
    s.append("\nCollateral: "  +descFrom(collateralRating, COLLATERAL_DESC));
    s.append("\nGetaways: "    +descFrom(getawaysRating  , GETAWAYS_DESC  ));
    Region region = scene.targetLocation().region();
    s.append("\n"+region+" trust      "+I.signNum((int) trustEffect)+"%");
    s.append("\n"+region+" deterrence "+I.signNum((int) deterEffect)+"%");
    
    final MainView view = scene.world().view();
    view.queueMessage(new MessageView(
      view, scene.icon(), h.toString(), s.toString(), "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        view.dismissMessage(this);
      }
    });
  }
  
  
  String descFrom(float rating, String desc[]) {
    if (rating == 0) return desc[0];
    int len = desc.length;
    int index = (int) (rating * (len - 1));
    return desc[Nums.clamp(1 + index, len)];
  }
}












