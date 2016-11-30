

package proto.game.event;
import proto.common.*;
import proto.game.person.*;
import proto.game.scene.*;
import proto.game.world.*;
import proto.util.*;



/*
public class FightLead extends Lead {
  
  
  final List <Person> fought = new List();
  private Scene groundScene = null;
  
  
  public FightLead(
    int ID, Place origin, Person... fought
  ) {
    super(ID, origin, new Element[0], Task.TIME_SHORT, origin.world());
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
    final Place place = targetLocation();
    final Region district = place.parent;
    
    //*
    //  TODO:  Have the Region in question, or the various types of facility,
    //  populate the landscape appropriately.
    this.groundScene = district.region().generateScene(district, this);
    groundScene.assignMissionParameters(
      this, place, 0.5f, initTime + timeTaken, fought
    );
    //*/
    /*
    
    for (Person p : assigned()) groundScene.addToTeam(p);
    world.enterScene(groundScene);
    
    return true;
  }
}
//*/









