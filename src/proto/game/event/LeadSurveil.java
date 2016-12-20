

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import proto.game.person.*;
import static proto.game.event.CaseFile.*;

import java.awt.Image;




public class LeadSurveil extends Lead {
  
  
  Element subject;
  Event involved, prior;
  boolean doInit = true;
  
  
  public LeadSurveil(Base base, Element tailed) {
    super(base, Task.TIME_INDEF, tailed, new Object[0]);
    this.subject = tailed;
  }
  
  
  public LeadSurveil(Session s) throws Exception {
    super(s);
    subject  = (Element) s.loadObject();
    involved = (Event  ) s.loadObject();
    prior    = (Event  ) s.loadObject();
    doInit   = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(subject );
    s.saveObject(involved);
    s.saveObject(prior   );
    s.saveBool  (doInit  );
  }
  
  
  public Place targetLocation() {
    return subject.place();
  }
  
  
  public void updateAssignment() {
    super.updateAssignment();
    //
    //  Firstly, we look out for any events that the subject might be involved
    //  in (either as a participant or as a venue.)
    if (subject.isPerson()) {
      final Object task = ((Person) subject).assignment();
      if (task instanceof Event) involved = (Event) task;
    }
    if (subject.isPlace()) {
      for (Element e : subject.attached()) if (e.isPerson()) {
        final Object task = ((Person) e).assignment();
        if (task instanceof Event) { involved = (Event) task; break; }
      }
    }
    //
    //  Then we look out for any changes in behaviour on the subject's part and
    //  terminate the task either when new intel is uncovered or a long time
    //  passes without any leads.
    if (involved != prior && involved.hasBegun()) {
      if (performTest() && checkForNewIntel()) setCompleted(true);
      prior = involved;
    }
    else if (hoursSoFar() > Task.TIME_LONG) {
      setCompleted(false);
    }
  }
  
  
  private boolean checkForNewIntel() {
    if (involved == null || involved.planStep() == null) return false;
    
    final PlanStep step = involved.planStep();
    final Place place = subject.place();
    boolean intel = false;
    I.say("\nSurveillance succeeded on "+subject+", event: "+involved);
    //
    //  First, give tipoffs on anyone directly involved in the crime underway:
    for (Element e : step.needs()) if (e != null) {
      I.say("  Involved: "+e+" at "+e.place());
      
      if (e.type == Kind.TYPE_PERSON && e.place() == place) {
        final CaseFile fileE = base.leads.caseFor(e);
        intel |= fileE.recordCurrentRole(involved, this);
      }
    }
    //
    //  Then, see if you overhear anything about where the next crime is going
    //  down, or where the boss might be hiding.
    PlanStep after = step.plan.stepAfter(step);
    Place hideout = step.plan.agent.base();
    float overhearChance = 0.5f;
    boolean stepTip = Rand.num() < overhearChance;
    boolean baseTip = Rand.num() < overhearChance;
    if (GameSettings.freeTipoffs || true) stepTip = baseTip = true;
    
    //  TODO:  You shouldn't neccesarily tip off the next step in the plan-
    //  only the next step that this step contributes to!
    if (stepTip && after != null) {
      Event afterEvent = after.matchedEvent();
      Place scene = afterEvent.targetLocation();
      final CaseFile fileP = base.leads.caseFor(scene);
      intel |= fileP.recordRole(afterEvent, ROLE_SCENE, this);
    }
    
    if (baseTip && hideout != null) {
      final CaseFile fileH = base.leads.caseFor(hideout);
      intel |= fileH.recordRole(involved, ROLE_HIDEOUT, this);
    }
    
    return intel;
  }
  
  
  protected void onSuccess() {
    //
    //  TODO:  Present a message for success.
    //base.leads.confirmLead(this, involved);
  }
  
  
  protected void onFailure() {
    I.say("\nSurveillance failed on "+subject);
    //
    //  TODO:  Present a message for failure.
    //base.leads.closeLead(this);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public String choiceInfo() {
    return "Surveil "+subject;
  }
  
  
  public String helpInfo() {
    return "";
  }
  
  
  public String activeInfo() {
    return "Surveilling "+subject;
  }
  
  
  public Image icon() {
    return subject.kind().sprite();
  }
}

