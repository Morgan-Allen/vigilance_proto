

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;


public class FightLead extends Lead {
  
  
  final List <Person> fought = new List();
  private Scene groundScene = null;
  
  
  public FightLead(
    String name, String info, Event parent, int ID,
    Object origin, Object reveals, Person... fought
  ) {
    super(name, info, parent, ID, origin, reveals, Task.TIME_SHORT);
    Visit.appendTo(this.fought, (Object[]) fought);
  }
  
  
  public FightLead(Session s) throws Exception {
    super(s);
    s.loadObjects(fought);
  }
  
  
  public void saveState(Session s) throws Exception {
    super.saveState(s);
    s.saveObjects(fought);
  }
  
  
  public boolean attemptTask() {
    
    final World world = parent.world();
    final District site = parent.place.parent;
    this.groundScene = site.region().generateScene(site, this);
    
    /*
    this.groundScene = new Scene(world, 32);
    
    //  TODO:  Have the Region in question, or the various types of facility,
    //  populate the landscape appropriately.
    
    groundScene.assignMissionParameters(
      this, site, 0.5f, initTime + timeTaken, fought
    );
    //*/
    
    for (Person p : assigned()) groundScene.addToTeam(p);
    world.enterScene(groundScene);
    
    return true;
  }
  
  
  
  
}









