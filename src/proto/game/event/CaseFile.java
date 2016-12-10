

package proto.game.event;
import proto.common.*;
import proto.util.*;



public class CaseFile implements Session.Saveable {
  
  
  final Object subject;
  List <Lead> incoming = new List();
  List <Lead> outgoing = new List();
  
  
  CaseFile(Object subject) {
    this.subject = subject;
  }
  
  
  public CaseFile(Session s) throws Exception {
    s.cacheInstance(this);
    subject = s.loadObject();
    s.loadObjects(incoming);
    s.loadObjects(outgoing);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveObject(subject);
    s.saveObjects(incoming);
    s.saveObjects(outgoing);
  }
  
  
  
}





