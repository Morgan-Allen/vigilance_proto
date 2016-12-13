

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;
import proto.game.person.*;
import java.awt.Image;




public class LeadTail extends Lead {
  
  
  Person tailed;
  Event involved;
  
  
  public LeadTail(Base base, Person tailed) {
    super(base, Task.TIME_INDEF, tailed, new Object[0]);
    this.tailed = tailed;
  }
  
  
  public LeadTail(Session s) throws Exception {
    super(s);
    tailed   = (Person) s.loadObject();
    involved = (Event ) s.loadObject();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(tailed  );
    s.saveObject(involved);
  }
  
  
  public Place targetLocation() {
    return tailed.place();
  }
  
  
  public void updateAssignment() {
    super.updateAssignment();
    
    final Object task = tailed.assignment();
    if (task instanceof Event) involved = (Event) task;
    
    if (involved != null && involved.hasBegun()) {
      attemptTask();
    }
    else if (hoursSoFar() > Task.TIME_LONG) {
      setCompleted(false);
    }
  }
  
  
  protected void onSuccess() {
    final PlanStep step = involved.planStep();
    if (step == null) { onFailure(); return; }
    I.say("Tailing succeeded!");
    //
    //  First, give tipoffs on anyone directly involved in the crime underway:
    final CaseFile file = base.leads.caseFor(tailed);
    file.recordInvolvement(involved, CaseFile.LEVEL_EVIDENCE);
    
    for (Element e : step.needs()) {
      if (e.type == Kind.TYPE_PERSON) {
        final CaseFile fileE = base.leads.caseFor(e);
        fileE.recordInvolvement(involved, CaseFile.LEVEL_EVIDENCE);
      }
    }
    //
    //  Then, see if you overhear anything about the next step in the plot, or
    //  where the boss might be hiding.
    PlanStep after = step.plan.stepAfter(step);
    Place hideout = step.plan.agent.base();
    float overhearChance = 0.5f;
    boolean stepTip = Rand.num() < overhearChance;
    boolean baseTip = Rand.num() < overhearChance;
    if (GameSettings.freeTipoffs) stepTip = baseTip = true;
    
    if (stepTip && after != null) {
      Event afterEvent = after.matchedEvent();
      for (Element n : after.needs()) {
        if (Visit.arrayIncludes(step.gives(), n)) {
          final CaseFile fileN = base.leads.caseFor(n);
          fileN.recordInvolvement(afterEvent, CaseFile.LEVEL_TIPOFF);
        }
      }
    }
    
    if (baseTip && hideout != null) {
      final CaseFile fileH = base.leads.caseFor(hideout);
      fileH.recordHideoutEvidence(CaseFile.LEVEL_TIPOFF);
    }
    
    //
    //  TODO:  Present a message for success.
    //base.leads.confirmLead(this, involved);
  }
  
  
  protected void onFailure() {
    I.say("Tailing failed!");
    //
    //  TODO:  Present a message for failure.
    //base.leads.closeLead(this);
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public String choiceInfo() {
    return "Tail "+subject;
  }
  
  
  public String helpInfo() {
    //  TODO:  You need to describe what brings you to the next lead.
    
    //  (e.g, A was seen talking to B.)
    //  An acceptable hack might be providing the description as a direct
    //  string.
    
    return
      "After tailing "+subject+", they appear to be involved in plans to "+
      involved+".  This could be a chance to catch them red-handed.";
  }
  
  
  public String activeInfo() {
    return "Tailing "+tailed;
  }
  
  
  public Image icon() {
    return tailed.kind().sprite();
  }
}


