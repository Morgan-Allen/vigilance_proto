

package proto;
import util.*;



public class MoveSearch extends Search <Tile> {
  
  Person moves;
  Tile dest;
  Tile temp[] = new Tile[8];
  boolean getNear = false;
  
  
  public MoveSearch(Person moves, Tile init, Tile dest) {
    super(init, -1);
    this.moves = moves;
    this.dest = dest;
    getNear = ! canEnter(dest);
  }
  
  
  protected Tile[] adjacent(Tile spot) {
    for (int n : TileConstants.T_INDEX) {
      temp[n] = dest.scene.tileAt(
        spot.x + TileConstants.T_X[n],
        spot.y + TileConstants.T_Y[n]
      );
    }
    return temp;
  }
  
  
  protected boolean endSearch(Tile best) {
    if (getNear) return dest.scene.distance(best, dest) < 2;
    return best == dest;
  }
  
  
  protected boolean canEnter(Tile spot) {
    if (spot == moves.location) return true;
    if (spot.blocked()) return false;
    return true;
  }
  
  
  protected float cost(Tile prior, Tile spot) {
    return spot.scene.distance(prior, spot);
  }
  
  
  protected float estimate(Tile spot) {
    return dest.scene.distance(spot, dest);
  }
  
  
  protected void setEntry(Tile spot, Entry flag) {
    spot.flag = flag;
  }
  
  
  protected Entry entryFor(Tile spot) {
    return (Entry) spot.flag;
  }
}