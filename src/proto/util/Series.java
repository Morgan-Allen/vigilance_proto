/**  
  *  Written by Morgan Allen.
  *  I intend to slap on some kind of open-source license here in a while, but
  *  for now, feel free to poke around for non-commercial purposes.
  */
package proto.util;


public interface Series <T> extends Iterable <T> {
  
  int size();
  void add(T t);
  boolean includes(T t);
  T first();
  T last();
  boolean empty();
  
  int indexOf(T t);
  T atIndex(int index);
  Object[] toArray();
  T[] toArray(Class typeClass);
}
