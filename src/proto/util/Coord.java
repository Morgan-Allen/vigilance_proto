/**  
  *  Written by Morgan Allen.
  *  I intend to slap on some kind of open-source license here in a while, but
  *  for now, feel free to poke around for non-commercial purposes.
  */


package proto.util;


public class Coord {
  
  public int x, y;
  
  
  public Coord() {}
  
  
  public Coord(Coord c) {
    this.x = c.x;
    this.y = c.y;
  }
  
  
  public Coord(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  
  public Coord(Coord c, int offX, int offY) {
    this.x = c.x + offX;
    this.y = c.y + offY;
  }
  
  
  public void setTo(Coord c) {
    this.x = c.x;
    this.y = c.y;
  }
  
  
  public Coord roundToUnit(int unit) {
    this.x = Nums.round(x, unit, false);
    this.y = Nums.round(y, unit, false);
    return this;
  }
  
  
  public float axisDistance(Coord c) {
    return Nums.abs(c.x - x) + Nums.abs(c.y - y);
  }
  
  
  public boolean matches(Coord c) {
    return this.x == c.x && this.y == c.y;
  }
  
  
  public boolean equals(Object o) {
    if (! (o instanceof Coord)) return false;
    final Coord c = (Coord) o;
    return c.x == x && c.y == y;
  }
  
  
  public int hashCode() {
    return (x * 13) + (y % 13);
  }
  
  
  public String toString() {
    return "["+x+", "+y+"]";
  }
}





