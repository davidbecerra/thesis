package custom;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/*
*  Set implementation done as an Arraylist without duplicate elements. Allows for removal of first element.
*/

public class ListSet<E> {
  private List<E> s;

  public ListSet() {
    s = new ArrayList<E>();
  }

  public boolean add(E e) {
    if (!s.contains(e)) {
      s.add(e);
      return true;
    }
    else
      return false;
  }

  public void addAll(Collection<? extends E> c) {
    if (c == null)
      return;
    for (E e : c){
      add(e);
    }
  }

  public E pop() {
    if (!s.isEmpty())
      return s.remove(0);
    else
      return null;
  }

  public boolean isEmpty() {
    return s.isEmpty();
  }

  public boolean isMember(E e) {
    return s.contains(e);
  }

  public List<E> asList() {
    return s;
  }

  public Iterator<E> iterator() {
    return s.iterator();
  }

  public boolean remove(Object o) {
    return s.remove(o);
  }

  public void removeAll(Collection<? extends E> c) {
    if (c == null)
      return;
    for (E e : c)
      s.remove(e);
  }

  // Returns true if s2 is a subset of this. O/w returns false
  public boolean isSubset(ListSet<E> s2) {
    List<E> l = s2.asList();
    return s.containsAll(l);
  }

  public boolean equals(Object o) {
    if (o instanceof ListSet<?>) {
      ListSet<?> l = (ListSet<?>) o;
      return s.equals(l.asList());
    }
    return false;
  }

  // Return the set S - S2
  public ListSet<E> set_difference(ListSet<E> s2) {
    ListSet<E> output = new ListSet<E>();
    for (E e : s) {
      if (s2.isMember(e))
        continue;
      else
        output.add(e);
    }
    return output;
  }
}