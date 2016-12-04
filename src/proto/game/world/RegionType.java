

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.*;
import proto.view.world.RegionAssets;



public class RegionType extends Kind {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public RegionAssets view = new RegionAssets();
  
  int     maxFacilities  = 3    ;
  float   defaultTrust   = 0.00f;
  boolean defaultMember  = false;
  PlaceType defaultFacilities[];
  
  
  public RegionType(String name, String ID) {
    super(name, ID, "", Kind.TYPE_REGION);
  }
  
  
  
  /**  Assigning default stats and facilities-
    */
  public void attachDefaultFacilities(PlaceType... facilities) {
    this.defaultFacilities = facilities;
  }
}








