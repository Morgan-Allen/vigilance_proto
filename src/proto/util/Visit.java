

package proto.util;

import java.lang.reflect.Array;
import java.util.Iterator;



public abstract class Visit <T> implements Iterable <T>, Iterator <T> {
  
  public void remove() {}
  public Iterator <T> iterator() { return this; }
  
  
  /**  Visits every point in the given area- a syntactic shortcut for array
    *  loops.
    */
  public static Visit <Coord> grid(
    final int minX, final int minY,
    final int xD  , final int yD  ,
    final int step
  ) {
    final int maxX = minX + xD, maxY = minY + yD;
    final Coord passed = new Coord();
    return new Visit <Coord> () {
      
      int x = minX, y = minY;
      
      final public boolean hasNext() {
        return x < maxX && y < maxY;
      }
      
      final public Coord next() {
        passed.x = x;
        passed.y = y;
        if ((y += step) >= maxY) { y = minY; x += step; }
        return passed;
      }
    };
  }
  
  
  public static Visit <Coord> grid(Box2D b) {
    final int
      minX = (int) (b.xpos() + 0.5f),
      minY = (int) (b.ypos() + 0.5f),
      dimX = (int) (b.xmax() + 0.5f) - minX,
      dimY = (int) (b.ymax() + 0.5f) - minY;
    return grid(minX, minY, dimX, dimY, 1);
  }
  
  
  /**  Couple of list-utility methods-
    */
  public static Object matchFor(Class matchClass, Series series) {
    for (Object o : series) if (o.getClass() == matchClass) return o;
    return null;
  }
  
  
  public static void appendTo(Series to, Series from) {
    for (Object o : from) to.add(o);
  }
  
  
  public static void appendTo(Series to, Object... from) {
    for (Object o : from) to.add(o);
  }
  
  

  /**  More utility methods, this time for dealing with arrays-
    */
  public static boolean empty(Object o[]) {
    if (o == null || o.length == 0) return true;
    for (Object i : o) if (i != null) return false;
    return true;
  }
  
  
  public static Object last(Object o[]) {
    if (o == null || o.length == 0) return null;
    return o[o.length - 1];
  }
  
  
  public static int indexOf(Object o, Object a[]) {
    for (int i = a.length; i-- > 0;) if (a[i] == o) return i;
    return -1;
  }
  
  
  public static Object[] compose(Class arrayClass, Object[]... arrays) {
    int length = 0, i = 0;
    for (Object a[] : arrays) length += a.length;
    final Object[] result = (Object[]) Array.newInstance(arrayClass, length);
    for (Object a[] : arrays) {
      for (Object o : a) result[i++] = o;
    }
    return result;
  }
  
  
  public static void wipe(Object array[]) {
    for (int i = array.length; i-- > 0;) array[i] = null;
  }
  
  
  public static boolean arrayIncludes(Object a[], Object e) {
    if (a == null || e == null) return false;
    for (Object o : a) if (o == e) return true;
    return false;
  }
  
  
  public static int countInside(Object m, Object a[]) {
    int count = 0;
    for (Object o : a) if (o == m) count++;
    return count;
  }
  
  
  public static float[] fromFloats(Object[] a) {
    float f[] = new float[a.length];
    for (int i = f.length; i-- > 0;) f[i] = (Float) a[i];
    return f;
  }
  
  
  public static int[] fromIntegers(Object[] a) {
    int f[] = new int[a.length];
    for (int i = f.length; i-- > 0;) f[i] = (Integer) a[i];
    return f;
  }
}





