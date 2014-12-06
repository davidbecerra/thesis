package custom;

import java.util.Iterator;

public class MySet<E> extends java.util.HashSet<E> {

  private static final long serialVersionUID = 1457096803280378274L;

  public E pop() {
    Iterator<E> itr = this.iterator();
    if (itr.hasNext()) {
      E e = itr.next();
      this.remove(e);
      return e;
    }
    return null;
  }
}