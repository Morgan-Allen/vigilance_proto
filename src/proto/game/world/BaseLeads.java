

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.util.*;



public class BaseLeads {
  
  
  final Base base;
  final List <Lead> open   = new List();
  final List <Lead> closed = new List();
  
  
  
  BaseLeads(Base base) {
    this.base = base;
  }
  
  
  void loadState(Session s) throws Exception {
    s.loadObjects(open  );
    s.loadObjects(closed);
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveObjects(open  );
    s.saveObjects(closed);
  }
  
  
  
  public Series <Lead> open  () { return open  ; }
  public Series <Lead> closed() { return closed; }
  
  
  
}





