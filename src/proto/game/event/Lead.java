

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
    type  = (LeadType) s.loadObject();
    focus = (Element ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(type );
    s.saveObject(focus);
  }
  
  
  
  /**  Task-associated methods-
    */
  public Element targetElement(Person p) {
    return focus;
  }
  
  
  public void setAssigned(Person p, boolean is) {
    super.setAssigned(p, is);
  }
  
  
  public int assignmentPriority() {
    return PRIORITY_LEAD;
  }
  
  
  
  /**  Extraction and screening of clues related to the case:
    */
  /*
  protected float attemptFollow(
    int tense, Plot plot, Series <Person> follow, int time
  ) {
    
    //  The attempt-key is:  plot_ID + tense + lead_type_ID + base_ID !
    
    //  The plot stores the number of accumulated hours.  Once that exceeds a
    //  certain limit, you compute the outcome.
    
    
    //
    //  First, check to see whether anything has actually changed here (i.e,
    //  avoid granting cumulative 'random' info over time.)  If it hasn't,
    //  just return.
    boolean report = GameSettings.leadsVerbose;
    if (report) {
      I.say("\nAttempting to follow "+plot);
      I.say("  Contact ID:   "+lastContactID);
      I.say("  Contact time: "+contactTime  );
      I.say("  Min. Hours:   "+type.minHours);
      I.say("  Current time: "+time         );
    }
    
    String contactID = plot.eventID+"_"+tense;
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
  //*/
  
  
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
  
  
  protected void extractClues(
    Element e, Plot plot, int time, int outcome
  ) {
    CaseFile file  = base.leads.caseFor(plot);
    Place    scene = focus.place();
    Role     role  = plot.roleFor(e);
    boolean report = GameSettings.leadsVerbose;
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
    
    if (report) {
      I.say("Recognition was: "+recognition);
    }
    
    if (recognition > 0.66f) {
      Clue confirms = Clue.confirmSuspect(plot, role, e, e.place());
      file.recordClue(confirms, this, time, scene);
    }
    else if (recognition > 0.33f) {
      Series <Clue> possible = ClueUtils.possibleClues(
        plot, e, focus, base, type
      );
      Clue gained = ClueUtils.pickFrom(possible);
      if (gained != null) file.recordClue(gained, this, time, scene);
    }
    else {
      return;
    }
  }
  
  
  protected Scene enteredScene(int tense, Plot plot, int time) {
    Role sceneRole = plot.roleFor(focus.place());
    if (noScene || tense != TENSE_PRESENT || sceneRole == null) {
      return null;
    }
    if (type.medium != MEDIUM_ASSAULT) {
      return null;
    }
    return plot.generateScene(focus, this);
  }
  
  
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    Series <Person> active = active();
    World   world  = base.world();
    int     time   = world.timing.totalHours();
    int     result = RESULT_NONE;
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
      Plot plot      = (Plot) event;
      int  tense     = plot.tense();
      int  startTime = plot.timeStarted(this);
      Series <Element> involved = plot.allInvolved();
      
      Scene scene = enteredScene(tense, plot, time);
      if (scene == null) {
        
        if (startTime == -1) {
          startTime = time;
          plot.cacheLeadResult(this, time, RESULT_NONE);
        }
        
        if (time - startTime > type.minHours) {
          attempt = configAttempt(active);
          if (setResult > 0) result = RESULT_PARTIAL;
          else result = attempt.performAttempt(1) * RESULT_HOT;
          plot.cacheLeadResult(this, startTime, result);
        }
        
        if (result != RESULT_NONE) for (Element e : involved) {
          if (! type.canDetect(e, plot, focus)) continue;
          extractClues(e, plot, time, result);
        }
      }
      else {
        MessageUtils.presentBustMessage(world.view(), scene, this, plot);
        world.enterScene(scene);
        return true;
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



