

package proto.game.person;
import proto.common.*;


public class PersonType extends Kind {
  
  
  String firstNames[], lastNames[];
  
  public PersonType(
    String name, String ID, String spritePath, String defaultInfo,
    String personNames[][],
    int subtype, Object... initStats
  ) {
    super(
      name, ID, spritePath, defaultInfo,
      1, 1, BLOCK_PARTIAL, false,
      TYPE_PERSON, subtype, initStats
    );
    
    if (personNames == null) personNames = new String[2][0];
    firstNames = personNames[0];
    lastNames  = personNames[1];
  }
  
  
  public String[] firstNames() { return firstNames; }
  public String[] lastNames () { return lastNames ; }

}
