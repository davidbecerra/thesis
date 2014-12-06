package custom;

public final class Variable {

  private int x;
  private int y;

  Variable(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
  
  public String varToString() {
    return "x" + x + "," + y;
  }

  public boolean equals(Object v) {
    if (v instanceof Variable) {
      Variable var = (Variable) v;
      if (var.getX() == x && var.getY() == y)
        return true;
      return false;
    }
    else
      return false;
  }

  public int hashCode() {
    return x * 100 + y;
  }
}