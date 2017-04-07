

package proto.game.scene;
import proto.common.*;
import proto.game.person.*;
import proto.util.*;



public class SceneVision implements TileConstants {
  
  
  /**  Data fields, construction and save/load methods-
    */
  final Scene scene;
  
  static class FogMap {
    Person.Side side;
    byte fog[][];
    Table <Person, Tile> lastSeen = new Table();
  }
  
  private FogMap allMaps[];
  
  
  
  SceneVision(Scene scene) {
    this.scene = scene;
  }
  
  
  void setupFog(int wide, int high) {
    Person.Side SIDES[] = Person.Side.values();
    allMaps = new FogMap[SIDES.length];
    
    for (Person.Side side : SIDES) {
      FogMap map = allMaps[side.ordinal()] = new FogMap();
      map.side = side;
      map.fog = new byte[wide][high];
    }
  }
  
  
  void loadState(Session s) throws Exception {
    for (FogMap map : allMaps) {
      s.loadByteArray(map.fog);
      for (int i = s.loadInt(); i-- > 0;) {
        Person p = (Person) s.loadObject();
        Tile   t = (Tile  ) s.loadObject();
        map.lastSeen.put(p, t);
      }
    }
  }
  
  
  void saveState(Session s) throws Exception {
    for (FogMap map : allMaps) {
      s.saveByteArray(map.fog);
      s.saveInt(map.lastSeen.size());
      for (Person p : map.lastSeen.keySet()) {
        s.saveObject(p);
        s.saveObject(map.lastSeen.get(p));
      }
    }
  }
  
  
  
  /**  Regular updates and basic data queries-
    */
  public void updateFog() {
    //  TODO:  You need to include visibility-lifting persistent items or
    //  debuffs here...
    for (FogMap map : allMaps) {
      for (Coord c : Visit.grid(0, 0, scene.wide, scene.high, 1)) {
        map.fog[c.x][c.y] = 0;
      }
    }
    for (Person p : scene.playerTeam) if (p.health.conscious()) {
      liftFogInSight(p);
    }
    for (Person p : scene.othersTeam) if (p.health.conscious()) {
      liftFogInSight(p);
    }
    for (PropEffect p : scene.effectProps) if (p.source != null) {
      p.kind().updateFogFor(scene, p);
    }
  }
  
  
  public float fogAt(Tile at, Person.Side side) {
    if (at == null) {
      I.say("?");
    }
    return allMaps[side.ordinal()].fog[at.x][at.y] / 100f;
  }
  
  
  
  /**  Helper methods for determining visibility for persons-
    */
  public void liftFogInSight(Person p) {
    float size = Nums.max(p.kind().wide(), p.kind().high());
    final float radius = p.stats.sightRange() + (size / 2);
    final Tile vantage[] = vantagePoints(p);
    liftFogAround(p.currentTile(), radius, p, true, vantage);
  }
  
  
  public void checkForEnemySightEntry(Person p, Action doing) {
    for (Person other : scene.allPersons) if (other.side() != p.side()) {
      float sight = degreeOfSight(other, p.currentTile(), false);
      if (sight > 0) {
        other.actions.onNoticing(p, doing);
        FogMap map = allMaps[other.side().ordinal()];
        map.lastSeen.put(p, p.currentTile());
      }
    }
  }
  
  
  public float degreeOfSight(Person p, Tile dest, boolean report) {
    final Tile vantages[] = vantagePoints(p);
    final SightLine lines[] = new SightLine[vantages.length];
    configSightLines(lines, vantages, dest);
    return degreeOfSight(lines, dest, p, report);
  }
  
  
  public Tile bestVantage(Person p, Tile dest, boolean report) {
    final Tile vantages[] = vantagePoints(p), temp[] = new Tile[1];
    final SightLine lines[] = new SightLine[1];
    
    Pick <Tile> pick = new Pick(0);
    for (Tile v : vantages) {
      temp[0] = v;
      configSightLines(lines, temp, dest);
      float sight = degreeOfSight(lines, dest, p, report);
      pick.compare(v, sight);
    }
    return pick.result();
  }
  
  
  public int coverFor(Tile at, Tile from, boolean report) {
    Scene scene = from.scene;
    final SightLine line = new SightLine();
    line.setTo(from, at);
    line.shrinkToDest(1.44f);
    if (report) I.say("\nChecking cover around "+at+" from "+from);
    
    int maxVal = 0;
    for (int dir : T_INDEX) {
      Tile t = scene.tileAt(at.x + T_X[dir], at.y + T_Y[dir]);
      if (t == null) continue;
      if (report) I.say("  Checking tile: "+t);
      maxVal = Nums.max(maxVal, t.coverLevel(line.orig, line.line, report));
    }
    
    if (report) I.say("  Best cover is: "+maxVal);
    return maxVal;
  }
  
  
  private Tile[] vantagePoints(Person p) {
    //
    //  Essentially, we allow an agent to 'peek around corners' to determine
    //  visibility, in the event that they're in full cover.
    final Tile orig = p.currentTile();
    Batch <Tile> vantage = new Batch();
    boolean nextToWall = false;
    for (int dir : T_ADJACENT) {
      Tile near = scene.tileAt(orig.x + T_X[dir], orig.y + T_Y[dir]);
      if (near == null) continue;
      if (orig.coverLevel(dir) >= Kind.BLOCK_FULL) {
        nextToWall = true;
      }
      else vantage.add(near);
    }
    //
    //  If they're *not* in full cover, they can only check visibility from
    //  their point of origin-
    if (! nextToWall) vantage.clear();
    vantage.add(orig);
    return vantage.toArray(Tile.class);
  }
  
  
  private void configSightLines(SightLine lines[], Tile[] vantage, Tile dest) {
    for (int i = lines.length; i-- > 0;) {
      SightLine line = lines[i];
      if (line == null) line = lines[i] = new SightLine();
      line.setTo(vantage[i], dest);
    }
  }
  
  
  
  /**  Generic methods for check sight-lines and lifting fog accordingly-
    */
  public void liftFogAround(
    Tile point, float radius, Person looks,
    boolean checkSight, Tile... vantage
  ) {
    //
    //  Firstly, determine which fog-map to consult for the agent in question-
    byte fog[][] = allMaps[looks.side().ordinal()].fog;
    //
    //  Then generate sight-lines for each vantage point (which, by default, is
    //  simply the central tile itself.)
    if (Visit.empty(vantage)) vantage = new Tile[] { point };
    SightLine lines[] = new SightLine[vantage.length];
    //
    //  We iterate over all tiles within range:
    final int lim = Nums.ceil(radius);
    for (Coord c : Visit.grid(
      point.x - lim, point.y - lim, lim * 2, lim * 2, 1
    )) {
      Tile t = scene.tileAt(c.x, c.y);
      if (t == null) continue;
      float dist = scene.distance(t, point) - 0.5f;
      if (dist >= radius) continue;
      //
      //  Then generate suitable sight-line/s and check for visibility before
      //  raising fog-
      configSightLines(lines, vantage, t);
      byte val = (byte) (100 * Nums.clamp(1.5f - (dist / radius), 0, 1));
      if (checkSight) val *= degreeOfSight(lines, t, looks, false);
      fog[t.x][t.y] = (byte) Nums.max(val, fog[t.x][t.y]);
    }
  }
  
  
  public float degreeOfSight(
    SightLine lines[], Tile dest, Person p, boolean report
  ) {
    float bestSight = 0;
    
    Box2D area = new Box2D(dest.x, dest.y, 0, 0);
    for (SightLine l : lines) {
      if (l.dest != dest) I.complain("Sights to do not converge on target!");
      area.include(l.vantage.x, l.vantage.y, 0);
    }
    area.incHigh(1);
    area.incWide(1);
    
    for (SightLine line : lines) {
      float sight = 1f;
      final Vec2D l = line.line, o = line.orig;
      if (report) {
        I.say("Checking line of sight between "+line.vantage+" and "+dest);
        I.say("  Origin: "+o);
        I.say("  Vector: "+l);
      }
      
      for (Coord c : Visit.grid(area)) {
        float lineDist = l.lineDist(c.x + 0.5f - o.x, c.y + 0.5f - o.y);
        if (report) I.say("    "+c+": "+lineDist+" away");
        if (lineDist > 0.5f) continue;
        final Tile t = scene.tiles[c.x][c.y];
        sight *= t.blocksSight(o, l, report) ? 0 : 1;
        if (report) I.say("    "+c+" LoS: "+sight);
      }
      
      bestSight = Nums.max(sight, bestSight);
    }
    
    return bestSight;
  }
  
}



