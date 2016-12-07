

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;



public abstract class Lead extends Task {
  
  
  Object origin, subject;
  List <Task> followOptions = new List();
  boolean open;
  
  
  Lead(
    Base base, int timeHours, Object origin, Object subject,
    Object... args
  ) {
    super(base, timeHours, args);
    this.origin  = origin ;
    this.subject = subject;
  }
  
  
  public Lead(Session s) throws Exception {
    super(s);
    origin  = s.loadObject();
    subject = s.loadObject();
    s.loadObjects(followOptions);
    open = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(origin );
    s.saveObject(subject);
    s.saveObjects(followOptions);
    s.saveBool(open);
  }
  
  
  
  void populateInvestigationOptions() {
    
    //  In addition, there may be multiple methods for investigating a single
    //  object (e.g, you could either tail, persuade or intimidate a suspect,
    //  stake out or guard a building, bug or sabotage a vehicle, swab or dust
    //  for clues, etc..)  And not all methods will yield immediate dividends.
    followOptions.clear();
    
    if (subject instanceof Item || subject instanceof Clue) {
      //  TODO:  Forensics.
    }
    
    if (subject instanceof Person) {
      //  TODO:  Tailing, Guard, Dialog, Interrogate/Detain.
      final Person person = (Person) subject;
      Task tailing = new LeadTail(base, this, person);
      followOptions.add(tailing);
    }
    
    if (subject instanceof Place) {
      //  TODO:  Stake-out, Guard, Search/Inspect.
    }
    
    if (subject instanceof Event) {
      final Event event = (Event) subject;
      final boolean dangerous = event.type.isDangerous(event);
      
      if (dangerous && ! event.complete()) {
        Task guarding = new TaskGuard(base, event);
        followOptions.add(guarding);
      }
      
      /*
      if (dangerous && event.complete()) {
        Task searching = new LeadSearch(base, this, event.place());
        followOptions.add(searching);
      }
      //  TODO:  Allow a search for *all* completed events?
      if (event.planStep() != null && ! dangerous) {
        for (Element e : event.planStep().gives()) {
          Person carries = (Person) e.parentOfType(Kind.TYPE_PERSON);
          if (carries == null) continue;
          Task tailing = new LeadTail(base, this, carries);
          followOptions.add(tailing);
        }
        //closeLead(event);
      }
      //*/
    }
  }
  
  
  public Series <Task> investigationOptions() {
    return followOptions;
  }
  
  
  
  
}


/*
  protected void presentMessage(final World world) {
    
    //  TODO:  Move this out to the View directory!
    StringBuffer s = new StringBuffer();
    
    for (Person p : assigned) {
      s.append(p.name());
      if (p != assigned.last()) s.append(" and ");
    }
    s.append(" tested their ");
    for (Trait t : tested) {
      s.append(t);
      if (t != Visit.last(tested)) s.append(" and ");
    }
    s.append(".");
    if (success()) s.append("  They were successful.");
    else           s.append("  They had no luck."    );
    
    boolean noLeads = true;
    //  TODO:  You need to have a central fact-repository for the investigating
    //  player instead.
    /*
    if (success) for (Lead l : parent.openLeadsFrom(this)) {
      s.append("\n\nNew lead: ");
      s.append(l.activeName);
      noLeads = false;
    }
    //*/
/*
    if (noLeads) s.append("\nNo new leads were uncovered.");
    
    for (String action : world.events().extractLogInfo(this)) {
      s.append("\n\n");
      s.append(action);
    }
    
    world.view().queueMessage(new MessageView(
      world.view(),
      icon(), "Task complete: "+choiceInfo(),
      s.toString(),
      "Dismiss"
    ) {
      protected void whenClicked(String option, int optionID) {
        world.view().dismissMessage(this);
      }
    });
  }
//*/








