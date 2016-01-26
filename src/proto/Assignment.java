

package proto;



public interface Assignment extends Session.Saveable {
  
  String name();
  boolean allowsAssignment(Person p);
}
