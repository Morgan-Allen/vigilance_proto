

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;
import proto.view.base.*;
import proto.view.common.*;



public class EventEffects implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  public int outcomeState = Scene.STATE_INIT;
  public float forceRating, collateralRating, getawaysRating;
  public float trustEffect, deterEffect;
  public List <Person> involved   = new List();
  public List <Person> captives   = new List();
  public List <Person> casualties = new List();
  
  
  public EventEffects() {
    return;
  }
  
  
  public EventEffects(Session s) throws Exception {
    s.cacheInstance(this);
    outcomeState = s.loadInt();
    forceRating      = s.loadFloat();
    collateralRating = s.loadFloat();
    getawaysRating   = s.loadFloat();
    trustEffect      = s.loadFloat();
    deterEffect      = s.loadFloat();
    s.loadObjects(involved  );
    s.loadObjects(captives  );
    s.loadObjects(casualties);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveInt(outcomeState);
    s.saveFloat(forceRating     );
    s.saveFloat(collateralRating);
    s.saveFloat(getawaysRating  );
    s.saveFloat(trustEffect     );
    s.saveFloat(deterEffect     );
    s.saveObjects(involved  );
    s.saveObjects(captives  );
    s.saveObjects(casualties);
  }
  
  
  
  /**  Report compositions for scenes and events-
    */
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
        if (p.currentScene() != scene || ! scene.wasWon()) sumAway++;
        else captives.add(p);
        numCrooks++;
      }
      if (! p.health.healthy()) {
        casualties.add(p);
      }
      involved.add(p);
    }
    
    outcomeState = scene.wasWon() ? Scene.STATE_WON : Scene.STATE_LOST;
    forceRating      = sumForce * 0.5f / Nums.max(1, numCrooks   );
    collateralRating = sumHurt  * 1.0f / Nums.max(1, numCivilians);
    getawaysRating   = sumAway  * 1.5f / Nums.max(1, numCrooks   );
    
    final boolean playerWon = outcomeState == Scene.STATE_WON;
    deterEffect += playerWon ? 10 : 0;
    deterEffect += (forceRating * 5 ) - (getawaysRating   * 10);
    trustEffect += playerWon ? 10 : 0;
    trustEffect -= (forceRating * 20) + (collateralRating * 40);
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
  
  
  float rateDamage(Person p) {
    float damage = p.health.injury() / p.health.maxHealth();
    
    if      (p.health.dead    ()) damage *= 4.00f;
    else if (p.health.critical()) damage *= 2.00f;
    else if (p.health.crippled()) damage *= 2.00f;
    else                          damage -= 0.50f;
    return Nums.clamp(damage, 0, 4);
  }
  
  
  
  /**  Applying and querying after-effects:
    */
  public void applyEffects(Place place) {
    final Region region = place.region();
    region.incLevel(Region.DETERRENCE, deterEffect, true);
    region.incLevel(Region.TRUST     , trustEffect, true);
  }
  
  
  public boolean playerWon   () { return outcomeState == Scene.STATE_WON   ; }
  public boolean playerLost  () { return outcomeState == Scene.STATE_LOST  ; }
  public boolean playerAbsent() { return outcomeState == Scene.STATE_ABSENT; }
  
  
  
  /**  Rendering, debug and interface methods:
    *  TODO:  Move this out to the view package...
    */
  final static String FORCE_DESC[] = {
    "None", "Minimal", "Moderate", "Heavy", "EXCESSIVE"
  };
  final static String COLLATERAL_DESC[] = {
    "None", "Minimal", "Medium", "Heavy", "TOTAL"
  };
  final static String GETAWAYS_DESC[] = {
    "None", "Few", "Some", "Many", "ALL"
  };
  
  
  public void presentMessageForScene(final Scene scene, final Event event) {
    
    StringBuffer h = new StringBuffer();
    h.append("\nMission ");
    if (playerWon()) h.append(" Successful: "+scene);
    else h.append(" Failed: "+scene);
    
    StringBuffer s = new StringBuffer();
    s.append("\nCaptives:");
    for (Person p : captives) if (! casualties.includes(p)) {
      s.append("\n  "+p.name());
    }
    s.append("\nCasualties:");
    for (Person p : casualties) {
      s.append("\n  "+p.name());
      
      float health = p.health.totalHarm() / p.health.maxHealth();
      health /= PersonHealth.HP_DEATH_PERCENT / 100f;
      s.append(" ("+((int) (health * 100))+"% injury)");
      
      String desc = "";
      if (p.health.critical()) desc = " (critical condition)";
      if (p.health.crippled()) desc = " (crippled)";
      if (p.health.dead    ()) desc = " (DEAD)";
      s.append(desc);
    }
    
    s.append("\n\n");
    s.append("\nUse of Force: "+descFrom(forceRating     , FORCE_DESC     ));
    s.append("\nCollateral: "  +descFrom(collateralRating, COLLATERAL_DESC));
    s.append("\nGetaways: "    +descFrom(getawaysRating  , GETAWAYS_DESC  ));
    Region region = scene.site().region();
    s.append("\n"+region+" Trust "     +I.signNum((int) trustEffect)+"%");
    s.append("\n"+region+" Deterrence "+I.signNum((int) deterEffect)+"%");
    
    final MainView view = scene.world().view();
    view.queueMessage(new MessageView(
      view, scene.icon(), h.toString(), s.toString(), "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        scene.performSceneExit();
        view.dismissMessage(this);
      }
    });
    
    if (event instanceof Plot) {
      Trial trial = scene.world().council.nextTrialFor((Plot) event);
      if (trial != null) MessageUtils.presentTrialMessage(view, trial);
    }
  }
  
  
  String descFrom(float rating, String desc[]) {
    if (rating <= 0) return desc[0];
    int len = desc.length;
    int index = (int) (rating * (len - 1));
    return desc[Nums.clamp(1 + index, len)];
  }
}












