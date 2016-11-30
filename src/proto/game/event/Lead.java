

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;

import proto.view.common.*;
import java.awt.Image;






/*
public class Lead extends Task {
  
  
  /**  Data fields, construction and save/load methods-
    */
  /*
  final public int ID;
  final public Element origin;
  final public Element goes[];
  
  float chance;
  boolean red, cold;
  
  
  public Lead(
    int ID, Element origin, Object reveals,
    int timeHours, World world, Object... args
  ) {
    super(timeHours, world, args);
    
    this.ID      = ID    ;
    this.origin  = origin;
    if (reveals instanceof Element[]) this.goes = (Element[]) reveals;
    else this.goes = new Element[] { (Element) reveals };
  }
  
  
  public Lead(Session s) throws Exception {
    super(s);
    ID     = s.loadInt();
    origin = (Element) s.loadObject();
    goes   = (Element[]) s.loadObjectArray(Element.class);
    
    chance = s.loadFloat();
    red    = s.loadBool();
    cold   = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveInt   (ID    );
    s.saveObject(origin);
    s.saveObjectArray(goes);
    
    s.saveFloat(chance);
    s.saveBool (red   );
    s.saveBool (cold  );
  }
  
  
  
  /**  Follow-up and execution-
    */
/*
  protected void onSuccess() {
    //if (! parent.checkFollowed(this, true)) return;
  }
  
  
  protected void onFailure() {
    //if (! parent.checkFollowed(this, false)) return;
  }
  
  /*
  public boolean open() {
    //  TODO:  You need to have a central fact-repository for the investigating
    //  player instead.
    boolean known = false;//parent.known.includes(origin);
    boolean done = complete();
    return known && ! done;
  }
  //*/
/*
  
  
  public Place targetLocation() {
    return origin.location();
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
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
  
  
  public Image icon() {
    return origin.kind.sprite();
  }
  
  
  public String choiceInfo() {
    return "";// activeName;
  }
  
  
  public String activeInfo() {
    return "";// activeName+" ("+parent.name()+")";
  }
  
  
  public String helpInfo() {
    return "";// helpInfo;
  }
}

//*/








