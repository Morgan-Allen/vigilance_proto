

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.game.scene.SceneTypeUnits.*;



public class Scenery implements Session.Saveable, TileConstants {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  int wide, high;
  Tile tiles[][] = new Tile[0][0];
  Prop fills[][] = new Prop[1][2];
  List <Prop> props = new List();
  
  static class Room {
    
    SceneType type;
    SceneTypeUnits.Unit unit;
    int ID;
    int minX, minY, wide, high;
    
    public String toString() { return "R["+unit+"]"; }
  }
  
  int gridW, gridH, resolution;
  int offX, offY, facing;
  List <Wing> wings     = new List();
  List <Room> rooms     = new List();
  List <Room> corridors = new List();
  
  
  
  public Scenery(int wide, int high, boolean forTesting) {
    this.wide = wide;
    this.high = high;
    this.setupScene(forTesting);
  }
  
  
  protected void setupScene(boolean forTesting) {
    initTiling(wide, high);
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      tiles[c.x][c.y] = new Tile(this, c.x, c.y);
    }
  }
  
  
  public Scenery(Session s) throws Exception {
    s.cacheInstance(this);
    wide = s.loadInt();
    high = s.loadInt();
    initTiling(wide, high);
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      tiles[c.x][c.y] = (Tile) s.loadObject();
    }
    for (Coord c : Visit.grid(0, 0, fills.length, fills[0].length, 1)) {
      fills[c.x][c.y] = (Prop) s.loadObject();
    }
    s.loadObjects(props);
  }
  
  
  public void saveState(Session s) throws Exception {
    s.saveInt(wide);
    s.saveInt(high);
    for (Coord c : Visit.grid(0, 0, wide, high, 1)) {
      s.saveObject(tiles[c.x][c.y]);
    }
    for (Coord c : Visit.grid(0, 0, fills.length, fills[0].length, 1)) {
      s.saveObject(fills[c.x][c.y]);
    }
    s.saveObjects(props);
  }
  
  
  
  /**  Helper methods for hierarchical composition-
    */
  public void setGridResolution(int resolution) {
    this.resolution = resolution;
    this.gridW      = wide / resolution;
    this.gridH      = high / resolution;
  }
  
  
  public void setUnitParameters(int offX, int offY, int facing) {
    this.offX   = offX;
    this.offY   = offY;
    this.facing = facing;
  }
  
  
  public Wing wingUnder(int x, int y) {
    for (Wing wing : wings) if (wing.contains(x, y)) return wing;
    return null;
  }
  
  
  public void attachWings(Series <Wing> wings) {
    this.wings.clear();
    Visit.appendTo(this.wings, wings);
  }
  
  
  public Series <Wing> wings() {
    return wings;
  }
  
  
  public Room roomUnder(int x, int y) {
    Tile under = tileAt(x, y);
    return under == null ? null : under.room;
  }
  
  
  public void recordRoom(Room area, Box2D bounds) {
    for (Coord c : Visit.grid(bounds)) {
      Tile under = tileAt(c.x, c.y);
      if (under != null) under.room = area;
    }
    rooms.include(area);
  }
  
  
  public Series <Room> rooms() {
    return rooms;
  }
  
  
  
  /**  Supplemental query methods-
    */
  public int wide() {
    return wide;
  }
  
  
  public int high() {
    return high;
  }
  
  
  public Series <Prop> props() {
    return props;
  }
  
  
  public Tile tileAt(int x, int y) {
    try { return tiles[x][y]; }
    catch (ArrayIndexOutOfBoundsException e) { return null; }
  }
  
  
  public Tile tileAt(float x, float y) {
    return tileAt((int) (x + 0.5f), (int) (y + 0.5f));
  }
  
  
  public Tile tileUnder(Object object) {
    if (object instanceof Tile  ) return  (Tile  ) object;
    if (object instanceof Person) return ((Person) object).currentTile();
    if (object instanceof Prop  ) return ((Prop  ) object).origin;
    return null;
  }
  
  
  public float distance(Object a, Object b) {
    return distance(tileUnder(a), tileUnder(b));
  }
  
  
  public float distance(Tile a, Tile b) {
    final int xd = a.x - b.x, yd = a.y - b.y;
    return Nums.sqrt((xd * xd) + (yd * yd));
  }
  
  
  public int direction(Tile from, Tile to) {
    float angle = new Vec2D(to.x - from.x, to.y - from.y).toAngle();
    angle = (angle + 360 + 45) % 360;
    int dir = 2 * (int) (angle / 90);
    return (N + dir + 8) % 8;
  }
  
  
  public Visit <Tile> tilesInArea(Box2D area) {
    final Visit <Coord> base = Visit.grid(area);
    return new Visit <Tile> () {
      
      public Tile next() {
        Coord c = base.next();
        return tileAt(c.x, c.y);
      }
      
      public boolean hasNext() {
        return base.hasNext();
      }
    };
  }
  
  
  
  /**  Supplementary population methods for use during initial setup-
    */
  protected void initTiling(int wide, int high) {
    tiles = new Tile[wide][high];
    int wallW = (wide * 2) + 1, wallH = (high * 2) + 1;
    fills = new Prop[wallW][wallH];
  }
  
  
  protected void tearDownScene() {
    tiles = new Tile[1][0];
    fills = new Prop[1][0];
  }
  
  
  public Prop addProp(PropType type, int x, int y, int facing, World w) {
    if (type == null) { I.complain("NULL PROP TYPE SUPPLIED!"); return null; }
    final Prop prop = new Prop(type, w);
    return prop.enterScene(this, x, y, facing) ? prop : null;
  }
  
  
  public Series <Prop> propsOfKind(Kind type) {
    Batch ofKind = new Batch();
    for (Prop p : props) if (p.kind == type) ofKind.add(p);
    return ofKind;
  }
}





