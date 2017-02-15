

package proto.game.person;
import proto.common.*;
import proto.view.base.*;


public class PersonType extends Kind {
  
  
  String firstNames[], lastNames[];
  AbilityPalette abilityPalette;
  
  public PersonType(
    String name, String ID, String spritePath, String defaultInfo,
    String personNames[][], AbilityPalette palette,
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
    abilityPalette = palette;
  }
  
  
  public String[] firstNames() { return firstNames; }
  public String[] lastNames () { return lastNames ; }
  public AbilityPalette abilityPalette() { return abilityPalette; }
  
}