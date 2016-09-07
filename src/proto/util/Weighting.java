

package proto.util;



public class Weighting <T> {
  
  
  class Entry { T ref; float weight; Entry next; }
  
  Entry first = null;
  float sumWeights = 0;
  
  
  
  public void add(T ref, float weight) {
    Entry e = new Entry();
    e.ref    = ref;
    e.weight = weight;
    e.next   = first;
    first = e;
    sumWeights += e.weight;
  }
  
  
  public T result() {
    float roll = Rand.num() * sumWeights, soFar = 0;
    for (Entry e = first; e != null; e = e.next) {
      soFar += e.weight;
      if (soFar >= roll) return e.ref;
    }
    return null;
  }
  
}