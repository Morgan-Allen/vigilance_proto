

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
  public boolean updateAssignment() {
    if (! super.updateAssignment()) return false;
    Series <Person> active = active();
    World   world  = base.world();
    int     time   = world.timing.totalHours();
    int     result = RESULT_NONE;
    boolean report = GameSettings.leadsVerbose;
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
      //
      //  If no scene has been generated, we attempt to follow suspects and
      //  extract clues-
      if (scene == null) {
        //
        //  If you haven't started following the plot yet, store your current
        //  starting time and a null result.
        if (startTime == -1) {
          startTime = time;
          plot.cacheLeadResult(this, time, RESULT_NONE);
          if (report) I.say("\nBegan following lead: "+this);
        }
        //
        //  If you've followed the suspect for long enough, obtain a result and
        //  store it-
        if (time - startTime > type.minHours) {
          attempt = configAttempt(active);
          if (setResult > 0) result = RESULT_PARTIAL;
          else result = attempt.performAttempt(1) * RESULT_PARTIAL;
          plot.cacheLeadResult(this, startTime, result);
          if (report) I.say("\nFinished following lead: "+this);
        }
        //
        //  If a result has been generated, attempt to extract clues for each
        //  of the participants:
        if (result != RESULT_NONE) for (Element e : involved) {
          if (! type.canDetect(e, plot, focus)) continue;
          if (report) I.say("\nExtracting clues on "+e);
          extractClues(e, plot, time, result, report);
        }
      }
      //
      //  If a scene has been generated, enter it instead-
      else {
        if (report) I.say("\nWill enter generated scene!- "+scene);
        MessageUtils.presentBustMessage(world.view(), scene, this, plot);
        world.enterScene(scene);
        return true;
      }
    }
    return true;
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
  
  
  protected void extractClues(
    Element e, Plot plot, int time, float checkResult, boolean report
  ) {
    CaseFile file  = base.leads.caseFor(plot);
    Place    scene = focus.place();
    Role     role  = plot.roleFor(e);
    //
    //  If no fixed result was specified, we average the check result with a
    //  random value and a bonus for tailing the perp directly:
    if (setResult == -1) {
      plot.takeSpooking(type.profile, e);
      checkResult = (checkResult + Rand.num() + (e == focus ? 1 : 0)) / 3f;
    }
    //
    //  If a fixed result has been specified, we don't spook the perps and use
    //  that value instead:
    else {
      checkResult = e == focus ? 1 : setResult;
    }
    if (report) {
      I.say("\nChecking to recognise "+e+" as "+role+" for "+plot);
      I.say("  Check result was: "+checkResult);
    }
    //
    //  If recognition is strong, we get an exact confirmation of the role of
    //  the suspect and their current location.  If it's weaker, we get a
    //  partial clue, and if it's weaker still, we get no clue at all.
    if (checkResult >= 0.66f) {
      Clue confirms = Clue.confirmSuspect(plot, role, e, e.place());
      file.recordClue(confirms, this, time, scene);
    }
    else if (checkResult >= 0.33f) {
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



