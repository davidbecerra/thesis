package custom;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public final class Constraint implements Comparable<Constraint> {

  private List<Variable> variables;
  private int n;  

  Constraint(List<Variable> vars, int n) {
    variables = new ArrayList<Variable>();
    variables.addAll(vars);
    this.n = n;
  }

  public int getConstraint() {
    return n;
  }

  public List<Variable> getVars() {
    return variables;
  }
  
  public int compareTo(Constraint c) {
    int output = this.n - c.getConstraint();
    // output += this.variables.size() - c.getVars().size();
    return output;
  }

  public boolean equals(Object o) {
    if (o instanceof Constraint) {
      Constraint c = (Constraint) o;
      return (this.n == c.getConstraint() && this.variables.equals(c.getVars()));
    }
    else
      return false;
  }


  /* Returns true if c is a subset of this. In other words, this contains all the vars
  *  in c, and the constraint on c is at most this.
  */
  public boolean isSubset(Constraint c) {
    // if (vars.containsAll(c.getvars()) && this.n <= c.getConstraintNum())
      return true;
  //   else
  //     return false;
    // return (variables.isSubset(c.getvars()) && this.n >= c.getConstraintNum());
  }

  public void update(Constraint c) {
    // this.n -= c.getConstraint();
    // variables.removeAll(c.getVars().asList());
  }

  public void printConstraint() {
    String output = "";
    for (Variable v: variables)
      output += v.varToString() + " ";
    output += "= " + n;
    System.out.println(output);
  }

}