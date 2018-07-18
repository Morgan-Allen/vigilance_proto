

package proto.game.world;
import proto.common.*;
import proto.util.*;
//import proto.view.base.*;
import proto.view.world.*;



public class RegionType extends Kind {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  String nameUsed;
  final public RegionAssets view = new RegionAssets();
  
  RegionType bordering[];
  int     maxFacilities     = 4    ;
  float   defaultTrust      = 25   ;
  float   defaultDeterrence = 25   ;
  int     baseFunding       = 75   ;
  boolean defaultMember     = true ;
  
  Batch <PlaceType> defaultFacilities = new Batch();
  Batch <Faction  > defaultOwners     = new Batch();
  
  
  public RegionType(String name, String ID) {
    super(name, ID, "", Kind.TYPE_REGION);
  }
  
  
  public int baseFunding() {
    return baseFunding;
  }
  
  
  
  /**  Assigning default stats and facilities-
    */
  public void attachFacilities(Faction owner, PlaceType... facilities) {
    for (PlaceType p : facilities) {
      defaultFacilities.add(p);
      defaultOwners.add(owner);
    }
  }
  
  public void setBordering(RegionType... others) {
    this.bordering = others;
  }
  
  public RegionType[] bordering() {
    return bordering;
  }
  
  
  
  /**  Attached supplementary media and graphics data-
    */
  public void attachName(String name) {
    this.nameUsed = name;
  }
  
  public String name() {
    return nameUsed;
  }
  
}



