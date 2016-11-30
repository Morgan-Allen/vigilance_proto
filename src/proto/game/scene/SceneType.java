

package proto.game.scene;
import proto.common.*;
import proto.game.world.*;
import proto.util.*;



public class SceneType extends Index.Entry implements
  TileConstants, Session.Saveable
{
  
  
  /**  Data fields, construction and save/load methods-
    */
  final static Index <SceneType> INDEX = new Index <SceneType> ();
  
  final public static Object
    BORDERS = "Borders",
    FLOORS  = "Floors" ,
    DOOR    = "Wall"   ,
    WINDOW  = "Window" ,
    PROP    = "Prop"   ;
  
  final String name;
  
  Kind borders, door, window;
  Kind floors;
  Kind props[];
  float propWeights[];
  
  SceneType childPatterns[];
  Box2D childLayouts[];
  
  
  public SceneType(
    String name, String ID, Object... args
  ) {
    super(INDEX, ID);
    this.name = name;
    
    Batch <Kind> propB = new Batch();
    
    for (int i = 0; i < args.length; i += 2) {
      final Object label = args[i], val = args[i + 1];
      if (label == BORDERS) borders = (Kind) val;
      if (label == FLOORS ) floors  = (Kind) val;
      if (label == DOOR   ) door    = (Kind) val;
      if (label == WINDOW ) window  = (Kind) val;
      if (label == PROP   ) propB.add((Kind) val);
    }
    props = propB.toArray(Kind.class);
  }
  
  
  public static SceneType loadConstant(Session s) throws Exception {
    return INDEX.loadEntry(s.input());
  }
  
  
  public void saveState(Session s) throws Exception {
    INDEX.saveEntry(this, s.output());
  }
  
  
  
  /**  Actual scene generation-
    */
  public Scene generateScene(Place place, int size) {
    final Scene scene = new Scene(place, size);
    scene.setupScene();
    final Box2D area = new Box2D().set(2, 2, size - 4, size - 4);
    populateScene(scene, area);
    return scene;
  }
  
  
  void populateScene(Scene s, Box2D area) {
    final int
      minX = (int) area.xpos(),
      minY = (int) area.ypos(),
      dimX = (int) area.xdim(),
      dimY = (int) area.ydim()
    ;
    
    for (Coord c : Visit.grid(minX, minY, dimX, dimY, 1)) {
      s.addProp(floors, c.x, c.y);
    }
    /*
    for (Coord c : Visit.perimeter(minX, minY, dimX, dimY)) {
      s.addProp(borders, c.x, c.y);
    }
    
    int totalPropArea = 0, totalArea = (int) area.area();
    for (Kind propType : props) {
      totalPropArea += propType.wide() * propType.high();
    }
    //*/
  }
  
  
  /*
  private void generateForces() {
    final World world = this.world();
    final Kind BOSSES[] = {
      Villains.KIND_SLADE,
      Villains.KIND_MR_FREEZE
    };
    final Kind GOONS[] = {
      Crooks.MOBSTER,
      Crooks.MOBSTER,
      Crooks.MOBSTER
    };
    final float GOON_CHANCES[] = { 3, 2, 1 };
    final float GOON_POWERS [] = { 1, 1, 1 };
    
    float forceLimit = dangerLevel() * 10;
    float bossChance = dangerLevel() / 2;
    float hostages   = 1 + Rand.index(3);
    float forceSum   = 0;
    
    while (hostages-- > 0) {
      Person hostage = new Person(Crooks.CIVILIAN, world);
      addToTeam(hostage);
    }
    while (forceSum < forceLimit) {
      Kind ofGoon = (Kind) Rand.pickFrom(GOONS, GOON_CHANCES);
      Person goon = new Person(ofGoon, world);
      forceSum += GOON_POWERS[Visit.indexOf(ofGoon, GOONS)];
      addToTeam(goon);
    }
    
    I.say("Scene generated, team is: ");
    for (Person p : this.othersTeam()) I.say("  "+p);
  }
  
  
  private Tile findEntryPoint(Person p, int nearX, int nearY) {
    int x = nearX + 5 + Rand.index(10), y = nearY + 5 + Rand.index(10);
    Tile under = tileAt(x, y);
    int dir = T_INDEX[Rand.index(T_INDEX.length)], size = size();
    
    while (under != null) {
      if (x != Nums.clamp(x, size) || y != Nums.clamp(y, size)) break;
      if (! under.blocked()) return under;
      x += T_X[dir];
      y += T_Y[dir];
      under = tileAt(x, y);
    }
    return null;
  }
  
  
  public void setupScene() {
    super.setupScene();
    int cX = (size() / 2) - 10, cY = 5;
    
    for (Coord c : Visit.grid(cX, cY, 20, 20, 1)) {
      addProp(KIND_FLOOR, c.x, c.y);
    }
    for (Coord c : Visit.grid(cX - 1, cY - 1, 22, 22, 1)) {
      if (tileAt(c.x, c.y).prop() != null) continue;
      
      if (c.x == cX + 10 || c.y == cY + 10) {
        addProp(KIND_DOOR, c.x, c.y);
      }
      else {
        addProp(KIND_WALL, c.x, c.y);
      }
    }
    addProp(KIND_POOL_TABLE, cX + 10, cY + 10);
    
    for (Person p : othersTeam()) {
      Tile entry = findEntryPoint(p, cX, cY);
      
      I.say("Finding entry for "+p+", at: "+entry);
      if (entry != null) enterScene(p, entry.x, entry.y);
      else I.say("  COULD NOT FIND ENTRY FOR: "+p);
    }
    
    int across = (size() - (playerTeam().size())) / 2;
    for (Person p : playerTeam()) {
      enterScene(p, across++, 0);
    }
    
    //  TODO:  Some patrol routes for goons would be nice...?
  }
  //*/
  
  
  
}





