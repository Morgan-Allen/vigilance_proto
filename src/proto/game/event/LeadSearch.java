

package proto.game.event;
import proto.common.*;
import proto.game.world.*;
import proto.game.person.*;
import proto.util.*;
import java.awt.Image;




public class LeadSearch extends Lead {
  
  
  /**  Data fields, construction and save/load methods-
    */
  Place searched;
  Batch <Clue> clues = new Batch();
  
  
  public LeadSearch(Base base, Place searched) {
    super(base, Task.TIME_SHORT, searched, new Object[0]);
    this.searched = searched;
  }
  
  
  public LeadSearch(Session s) throws Exception {
    super(s);
    searched = (Place) s.loadObject();
    s.loadObjects(clues);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObject(searched);
    s.saveObjects(clues);
  }
  
  
  public Place targetLocation() {
    return searched;
  }
  
  
  protected void onSuccess() {
    
    //  TODO:  Produce clues leading to the participants and/or to the hideout
    //  and/or to items left at the scene.
    
    //  Participants can be interrogated, which might allow you to chase leads
    //  further up the chain of command or get an indication of further steps
    //  in a criminal plan.
  }
  
  
  protected void onFailure() {
  }
  
  
  
  
  /**  Rendering, debug and interface methods-
    */
  protected void presentMessage(World world) {
  }
  
  
  public String choiceInfo(Person p) {
    return "Searched "+searched;
  }
  
  
  public String helpInfo() {
    return null;
  }
  
  
  public String activeInfo() {
    return "Searching "+searched+" for clues";
  }
  
  
  public Image icon() {
    return searched.icon();
  }
}








