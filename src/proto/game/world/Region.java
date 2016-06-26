

package proto.game.world;
import proto.common.*;
import proto.game.event.*;
import proto.game.scene.*;
import proto.util.*;
import proto.view.*;
import proto.view.world.RegionAssets;



//  TODO:  Rename this to the 'Geography' class to avoid confusion, and include
//  all the initialisation data in the content package.


public class Region extends Index.Entry implements Session.Saveable {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final static Index <Region> INDEX = new Index <Region> ();
  
  final public String name;
  final public RegionAssets view = new RegionAssets();
  
  int     maxFacilities  = 3    ;
  float   defaultTrust   = 0.00f;
  boolean defaultMember  = false;
  Blueprint defaultFacilities[];
  
  
  public Region(String name, String ID) {
    super(INDEX, ID);
    this.name = name;
  }
  
  
  public static Region loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
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
  
  
  
  /**  Rendering, debug and interface methods-
    */
  public String toString() {
    return name;
  }
}








