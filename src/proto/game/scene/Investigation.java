

package proto.game.scene;
import proto.common.*;
import proto.util.*;



public class Investigation implements Session.Saveable {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final String name;
  
  float timeBegins, timeEnds;
  List <Lead> leads = new List();
  
  List <Object> involved = new List();
  boolean closed, solved;
  
  
  protected Investigation(String name) {
    this.name = name;
  }
  
  
  public Investigation(Session s) throws Exception {
    s.cacheInstance(this);
    name = s.loadString();
    
    timeBegins = s.loadFloat();
    timeEnds   = s.loadFloat();
    s.loadObjects(leads   );
    s.loadObjects(involved);
    closed = s.loadBool();
    solved = s.loadBool();
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveString(name);
    
    s.saveFloat(timeBegins);
    s.saveFloat(timeEnds  );
    s.saveObjects(leads   );
    s.saveObjects(involved);
    s.saveBool(closed);
    s.saveBool(solved);
  }
  
  
  
  /**  Supplemental setup/progression methods-
    */
  public void assignDates(float begins, float ends) {
    this.timeBegins = begins;
    this.timeEnds   = ends  ;
  }
  
  
  protected void assignLeads(Lead... leads) {
    for (Lead l : leads) this.leads.add(l);
  }
  
  
  protected boolean checkFollowed(Lead lead, boolean success) {
    if (success) involved.include(lead.reveals);
    return true;
  }
  
  
  protected void setComplete(boolean solved) {
    this.closed = true;
    this.solved = solved;
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




