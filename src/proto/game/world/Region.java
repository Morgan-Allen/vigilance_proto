

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.*;
import proto.view.world.RegionAssets;



public class Region extends Kind {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public RegionAssets view = new RegionAssets();
  
  int     maxFacilities  = 3    ;
  float   defaultTrust   = 0.00f;
  boolean defaultMember  = false;
  Blueprint defaultFacilities[];
  
  
  public Region(String name, String ID) {
    super(name, ID, "");
  }
  
  
  
  /**  Assigning default stats and facilities-
    */
  public void attachDefaultFacilities(Blueprint... facilities) {
    this.defaultFacilities = facilities;
  }
  
  
  
  /**  Scene generation-
    */
  public Scene generateScene(District district, Lead trigger) {
    return null;
  }
}








