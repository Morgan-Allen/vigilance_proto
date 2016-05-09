

package proto.game.scene;
import proto.common.*;
import proto.util.*;



public class Investigation implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final String name;
  
  List <Lead> leads = new List();
  
  List <Object> involved = new List();
  
  
  Investigation(String name) {
    this.name = name;
  }
  
  
  public Investigation(Session s) throws Exception {
    s.cacheInstance(this);
    name = s.loadString();
    s.loadObjects(leads   );
    s.loadObjects(involved);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveString(name);
    s.saveObjects(leads   );
    s.saveObjects(involved);
  }
  
  
  
  /**  Methods for override as needed-
    */
  protected boolean checkFollowed(Lead lead, boolean success) {
    if (success) involved.include(lead.reveals);
    return true;
  }
  
  
  
  /**  Lead-compilation for general assessment and UI purposes-
    */
  public Series <Lead> knownLeads() {
    final Batch <Lead> known = new Batch();
    for (Lead l : leads) if (involved.includes(l.origin)) known.add(l);
    return known;
  }
  
  
  public Series <Lead> leadsFrom(Object origin) {
    final Batch <Lead> from = new Batch();
    for (Lead l : leads) if (l.origin == origin) from.add(l);
    return from;
  }
  
  
  public Series <Object> involved() {
    return involved;
  }
}




