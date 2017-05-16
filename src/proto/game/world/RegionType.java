

package proto.game.world;
import proto.common.*;
import proto.util.*;
import proto.view.base.*;



public class RegionType extends Kind {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  final public RegionAssets view = new RegionAssets();
  
  int     maxFacilities     = 4    ;
  float   defaultTrust      = 25   ;
  float   defaultDeterrence = 25   ;
  int     baseFunding       = 75   ;
  boolean defaultMember     = true ;
  
  Batch <PlaceType> defaultFacilities = new Batch();
  Batch <Faction  > defaultOwners     = new Batch();
  float mapX, mapY;
  
  
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
  
  
  public void attachMapCoordinates(float mapX, float mapY) {
    this.mapX = mapX;
    this.mapY = mapY;
  }
  
  
  public Vec2D mapCoords() {
    return new Vec2D(mapX, mapY);
  }
  
}



