

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.base.*;
import static proto.game.person.PersonStats.*;
import static proto.game.event.LeadType.*;

import java.awt.Image;



public class Lead extends Task {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final public LeadType type;
  final public Element focus;
  
  private String lastContactID = "___";
  private int contactTime = -1;
  private List <Person> onceActive = new List();
  
  public float leadRating = 0;
  public float setResult = -1;
  public boolean noScene = false;
  
  
  
  Lead(Base base, LeadType type, Element focus) {
    super(base, Task.TIME_INDEF);
    
    this.type  = type ;
    this.focus = focus;
    
    boolean badFocus = ! type.canFollow(focus);
    if (badFocus) I.complain("Incorrect focus type for "+type+": "+focus);
  }
  
  
  public Lead(Session s) throws Exception {
    super(s);
    type          = (LeadType) s.loadObject();
    focus         = (Element ) s.loadObject();
    lastContactID = s.loadString();
    contactTime   = s.loadInt();
    s.loadObjects(onceActive);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(type);
    s.saveObject(focus);
    s.saveString(lastContactID);
    s.saveInt   (contactTime);
    s.saveObjects(onceActive);
  }
  
  
  
  /**  Task-associated methods-
    */
  public Element targetElement(Person p) {
    return focus;
  }
  
  
  public void setAssigned(Person p, boolean is) {
    super.setAssigned(p, is);
    onceActive.include(p);
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_LEAD;
  }
  
  
  public Series <Person> onceActive() {
    return onceActive;
  }
  
  
  
  /**  Extraction and screening of clues related to the case:
    */
  protected float recognition(float attemptResult, Place scene, Element e) {
    //
    //  We assign a higher probability of recognition if the suspect is
    //  present on-site, if the skill-test went well, and based on a random
    //  roll.
    float recognition;
    if (setResult > 0) {
      recognition = setResult;
    }
    else {
      recognition = e == null ? 0 : (e.place() == scene ? 0.5f : -0.5f);
      recognition = (recognition + attemptResult + Rand.num()) / 3;
    }
    return recognition;
  }
  
  
  protected float attemptFollow(
    Step step, int tense, Plot plot, Series <Person> follow, int time
  ) {
    //
    //  First, check to see whether anything has actually changed here (i.e,
    //  avoid granting cumulative 'random' info over time.)  If it hasn't,
    //  just return.
    String contactID = plot.eventID+"_"+step.uniqueID()+"_"+tense;
    if (contactTime == -1 && ! contactID.equals(lastContactID)) {
      lastContactID = contactID;
      contactTime   = time;
    }
    if (contactTime == -1 || (time - contactTime) < type.minHours) {
      return RESULT_NONE;
    }
    //
    //  Then perform the actual skill-test needed to ensure success:
    attempt = configAttempt(follow);
    int outcome = setResult > 0 ? 1 : attempt.performAttempt(1);
    //
    //  Reset for the next contact and return your result.
    contactTime = -1;
    return outcome;
  }
  
  
  protected void extractClues(
    Element e, Step step, Plot plot, int time, float outcome
  ) {
    CaseFile file  = base.leads.caseFor(plot);
    Place    scene = focus.place();
    Role     role  = plot.roleFor(e);
    //
    //  Whatever happens, you have to take the risk of tipping off the perps
    //  themselves:
    if (setResult == -1) {
      plot.takeSpooking(type.profile, e);
    }
    //
    //  If recognition is strong, we get an exact confirmation of the role of
    //  the suspect and their current location.  If it's weaker, we get a
    //  partial clue, and if it's weaker still, we get no clue at all.
    float recognition = recognition(outcome, scene, e);
    if (recognition > 0.66f) {
      Clue confirms = Clue.confirmSuspect(plot, role, step, e, e.place());
      file.recordClue(confirms, this, time, scene);
    }
    else if (recognition > 0.33f) {
      Series <Clue> possible = step.possibleClues(
        plot, e, focus, step, base, type
      );
      Clue gained = step.pickFrom(possible);
      if (gained != null) file.recordClue(gained, this, time, scene);
    }
    else {
      return;
    }
    //
    //  We check separately for any element whose role is mentioned-
    float recogMention = recognition(outcome, scene, null);
    if (step.mentions != null && recogMention > 0.5f) {
      Element match = plot.filling(step.mentions);
      Clue confirms = Clue.confirmSuspect(plot, step.mentions, step, match);
      file.recordClue(confirms, this, time, scene);
    }
    //
    //  And for the overall objective of the plot-
    float recogAim = recognition(outcome, scene, null);
    if (step.canLeakAim() && recogAim > 0.5f) {
      Clue confirms = Clue.confirmAim(plot);
      file.recordClue(confirms, this, time, scene);
    }
  }
  
  
  protected Scene enteredScene(Step step, int tense, Plot plot, int time) {
    Role sceneRole = plot.roleFor(focus.place());
    if (noScene || tense != TENSE_PRESENT || ! step.involves(sceneRole)) {
      return null;
    }
    if (step.medium != MEDIUM_ASSAULT && type.medium != MEDIUM_ASSAULT) {
      return null;
    }
    return plot.generateScene(step, focus, this);
  }
  
  
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    Series <Person> active = active();
    World   world  = base.world();
    int     time   = world.timing.totalHours();
    float   result = RESULT_NONE;
    boolean report = GameSettings.eventsVerbose;
    //
    //  Remember to close the lead if it's impossible to follow.
    if (! base.leads.atKnownLocation(focus)) {
      MessageUtils.presentColdTrailMessage(world.view(), this);
      setCompleted(false);
      return true;
    }
    //
    //  Continuously monitor for events of interest connected to the current
    //  focus of your investigation, and see if any new information pops up...
    for (Event event : world.events.allEvents()) if (event.isPlot()) {
      Plot plot = (Plot) event;
      
      for (Step step : plot.allSteps()) {
        int tense = plot.tense(step);
        Series <Element> involved = plot.involved(step);
        
        Scene scene = enteredScene(step, tense, plot, time);
        if (scene == null) {
          //  TODO:  Wait.  This might not work if the tense changes, and the
          //  step then becomes detectable.
          
          result = attemptFollow(step, tense, plot, active, time);
          if (result != RESULT_NONE) for (Element e : involved) {
            if (! type.canDetect(e, step, plot, focus)) continue;
            extractClues(e, step, plot, time, result);
          }
        }
        else {
          MessageUtils.presentBustMessage(world.view(), scene, this, plot);
          world.enterScene(scene);
          return true;
        }
      }
    }
    return true;
  }
  
  
  protected Attempt configAttempt(Series <Person> attempting) {
    return type.configFollowAttempt(focus, this, attempting);
  }
  
  
  public void onSceneExit(Scene scene, EventEffects report) {
    setCompleted(report.playerWon());
  }
  
  
  protected void onCompletion() {
    super.onCompletion();
    base.leads.closeLead(this);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String activeInfo() {
    return type.name+": "+focus;
  }
  
  
  public String testInfo(Person agent) {
    String info = super.testInfo(agent);
    StringBuffer s = new StringBuffer(type.info+"\n");
    s.append(info);
    
    String profileDesc = PROFILE_DESC[type.profile];
    int confIndex = (int) (type.confidence * 3);
    String confDesc = CONFIDENCE_DESC[Nums.clamp(confIndex, 3)];
    
    s.append("\n  Conspicuousness: "+profileDesc);
    s.append("\n  Evidence Level:  "+confDesc   );
    return s.toString();
  }
  
  
  public String helpInfo() {
    return type.info;
  }
  
  
  public Image icon() {
    return type.icon;
  }
  
  
  public String choiceInfo(Person p) {
    return type.name;//+" ("+focus+")";
  }
  
  
  public float taskDaysRemaining(Person p) {
    return 1;
  }
}



