

package proto.game.person;
import proto.common.*;



public class PersonHistory {
  
  
  /**  Data fields, construction, setup and save/load methods-
    */
  final Person person;
  String alias   = null;
  String summary = null;
  
  
  PersonHistory(Person person) {
    this.person = person;
  }
  
  
  void loadState(Session s) throws Exception {
    alias   = s.loadString();
    summary = s.loadString();
  }
  
  
  void saveState(Session s) throws Exception {
    s.saveString(alias  );
    s.saveString(summary);
  }
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public void setSummary(String s) {
    summary = s;
  }
  
  
  public void setAlias(String a) {
    alias = a;
  }
  
  
  public String summary() {
    return summary;
  }
  
  
  public String alias() {
    return alias;
  }
}



