

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.game.world.*;
import proto.util.*;
import proto.game.scene.SceneTypeUnits.*;



public class Scenery implements Session.Saveable, TileConstants {
  
  
  /**  Data fields, constructors and save/load methods-
    */
  SceneType type;
  int wide, high;
  Tile tiles[][] = new Tile[0][0];
  Prop fills[][] = new Prop[1][2];
  List <Prop> props = new List();
  
  public static class Room {
    
    SceneType type;
    SceneTypeUnits.Unit unit;
    
    int ID;
    int minX, minY, wide, high;
    
    Batch <Island> bridgeFor = new Batch();
    
    public String toString() { return "R["+unit+"]"; }
  }
  
  public static class Island {
    boolean exterior;
    
    Batch <Coord > gridPoints = new Batch();
    Batch <Island> neighbours = new Batch();
    Batch <Room  > bridges    = new Batch();

    public boolean exterior() { return exterior; }
    public Series <Coord> gridPoints() { return gridPoints; }
    
    public String toString() {
      String prefix = exterior ? "Exterior" : "Interior";
      return prefix+"["+gridPoints.first()+"+"+(gridPoints.size() - 1)+"]";
    }
  }
  
  int gridW, gridH;//, resolution;
  int offX, offY, facing;
  
  Wing   wingsPattern  [][];
  Island islandsPattern[][];
  List <Room  > rooms   = new List();
  List <Wing  > wings   = new List();
  List <Island> islands = new List();
  
  
  
  public Scenery(SceneType type, int wide, int high, boolean forTesting) {
    this.type = type;
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
  public void setupWingsGrid(Series <Wing> wings) {
    
    this.gridW          = Nums.ceil(wide * 1f / type.resolution);
    this.gridH          = Nums.ceil(high * 1f / type.resolution);
    this.wingsPattern   = new Wing  [gridW][gridH];
    this.islandsPattern = new Island[gridW][gridH];
    
    this.wings.clear();
    Visit.appendTo(this.wings, wings);
    
    for (Coord c : Visit.grid(0, 0, gridW, gridH, 1)) {
      int tx = (int) ((c.x + 0.5f) * type.resolution);
      int ty = (int) ((c.y + 0.5f) * type.resolution);
      wingsPattern[c.x][c.y] = null;
      for (Wing w : wings) if (w.contains(tx, ty)) {
        wingsPattern[c.x][c.y] = w;
        break;
      }
    }
    
    boolean mark[][] = new boolean[gridW][gridH];
    Box2D limit = new Box2D(-0.5f, -0.5f, gridW, gridH);
    
    for (Coord c : Visit.grid(0, 0, gridW, gridH, 1)) {
      if (mark[c.x][c.y]) continue;
      Wing under = wingUnderGrid(c.x, c.y);
      
      Island island = new Island();
      List <Coord> frontier = new List();
      
      Coord init = new Coord(c);
      island.gridPoints.add(init);
      frontier.add(init);
      mark[init.x][init.y] = true;
      
      while (! frontier.empty()) {
        Coord i = frontier.removeFirst();
        
        for (int dir : T_ADJACENT) {
          Coord n = new Coord(i.x + T_X[dir], i.y + T_Y[dir]);
          if (! limit.contains(n)) continue;
          if (mark[n.x][n.y]) continue;
          
          Wing   underN = wingUnderGrid  (n.x, n.y);
          Island underI = islandUnderGrid(n.x, n.y);
          
          if (underI != null && underI != island) {
            island.neighbours.include(underI);
            underI.neighbours.include(island);
          }
          
          if ((under == null) != (underN == null)) continue;
          island.gridPoints.add(n);
          frontier.addLast(n);
          mark[n.x][n.y] = true;
        }
      }
      
      island.exterior = under == null;
      islands.add(island);
      for (Coord p : island.gridPoints) islandsPattern[p.x][p.y] = island;
    }
  }
  
  
  public void setUnitParameters(int offX, int offY, int facing) {
    this.offX   = offX;
    this.offY   = offY;
    this.facing = facing;
  }
  
  
  public Coord gridPoint(Tile t) {
    return gridPoint(t.x, t.y);
  }
  
  
  public Coord gridPoint(int x, int y) {
    return new Coord(x / type.resolution, y / type.resolution);
  }
  
  
  public Tile gridMidpoint(Coord c) {
    return gridMidpoint(c.x, c.y);
  }
  
  
  public Tile gridMidpoint(int gx, int gy) {
    int hr = type.resolution / 2;
    return tileAt((gx * type.resolution) + hr, (gy * type.resolution) + hr);
  }
  
  
  public Island islandUnder(int x, int y) {
    if (x < 0 || y < 0 || x >= wide || y >= high) return null;
    return islandsPattern[x / type.resolution][y / type.resolution];
  }
  
  
  public Island islandUnderGrid(int gx, int gy) {
    if (gx < 0 || gy < 0 || gx >= gridW || gy >= gridH) return null;
    return islandsPattern[gx][gy];
  }
  
  
  public Series <Island> islands() {
    return islands;
  }
  
  
  public Wing wingUnder(int x, int y) {
    if (x < 0 || y < 0 || x >= wide || y >= high) return null;
    return wingsPattern[x / type.resolution][y / type.resolution];
  }
  
  
  public Wing wingUnderGrid(int gx, int gy) {
    if (gx < 0 || gy < 0 || gx >= gridW || gy >= gridH) return null;
    return wingsPattern[gx][gy];
  }
  
  
  public Series <Wing> wings() {
    return wings;
  }
  
  
  public Room roomUnder(int x, int y) {
    Tile under = tileAt(x, y);
    return under == null ? null : under.room;
  }
  
  
  public Room roomUnderGrid(int gx, int gy) {
    Tile mid = gridMidpoint(gx, gy);
    return mid == null ? null : mid.room;
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
  
  
  public void setAsEntrance(Room room, Island island) {
    room.bridgeFor.include(island);
    island.bridges.include(room);
  }
  
  
  public boolean hasBridgeBetween(Island island, Island other) {
    for (Room r : island.bridges) {
      if (r.bridgeFor.includes(other)) return true;
    }
    return false;
  }
  
  
  
  /**  Supplemental query methods-
    */
  public SceneType type() {
    return type;
  }
  
  
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





