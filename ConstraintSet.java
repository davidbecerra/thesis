package custom;

import map.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

public final class ConstraintSet {

  // Class variables
  private List<Constraint> constraints;
  private Set<Variable> constraintVars;
  private HashMap<Variable, List<Integer>> varToEqn;
  private Set<Variable> questionable;

  // Constructor
  ConstraintSet() {
    constraints = new ArrayList<Constraint>();
    constraintVars = new HashSet<Variable>();
    varToEqn = new HashMap<Variable, List<Integer>>();
    questionable = new HashSet<Variable>();
  };

  // Methods

  private void clearAll() {
    constraints.clear();
    constraintVars.clear();
    varToEqn.clear();
  }

  public void createConstraints(Set<Variable> vars, Map m) {
    clearAll();
    questionable.addAll(vars);
    Iterator<Variable> itr = questionable.iterator();
    List<Variable> toBeRemoved = new ArrayList<Variable>();
    while (itr.hasNext()) {
      Variable v = itr.next();
      int x = v.getX();
      int y = v.getY();
      List<Variable> adjVars = Custom.getAdjUnmarked(x, y, m);
      int nMarked = Custom.adjMarked(x, y, m);
      int n = m.look(x, y);
      if (n != nMarked) {
        constraints.add(new Constraint(adjVars, n - nMarked));
        constraintVars.addAll(adjVars);
        addToMap(adjVars, constraints.size() - 1);
      }
      else
        toBeRemoved.add(v);
    }
    questionable.removeAll(toBeRemoved);
    return;
  }


  // Add the equation associated with each var in vars to map
  private void addToMap(List<Variable> vars, int eqnKey) {
    for (Variable v: vars) {
      List<Integer> values = varToEqn.get(v);
      // Update associated equations if already in hashmap
      if (values != null)
        values.add(eqnKey);
      // Else, add to map
      else {
        values = new ArrayList<Integer>();
        values.add(eqnKey);
        varToEqn.put(v, values);
      }
    }
  }

  private void checkForSubsets(Constraint c1) {
  //   Iterator<Constraint> itr = constraints.iterator();
  //   Constraint toBeChecked = null;
  //   while (itr.hasNext()) {
  //     Constraint c2 = itr.next();
  //     if (c1.isSubset(c2)) {
  //       c1.update(c2);
  //       if (c1.getConstraintVars().isEmpty())
  //         return;
  //     }
  //     else if (c2.isSubset(c1)) {
  //       c2.update(c1);
  //       toBeChecked = c2;
  //       break;
  //     }
  //   }
  //   if (toBeChecked != null) {
  //     if (toBeChecked.getConstraintVars().isEmpty())
  //       constraints.remove(toBeChecked);
  //   }
    return;
  }

  // public void simplifyConstraints() {
  //   // Iterator<Constraint> itr1 = constraints.iterator();
  //   // Iterator<Constraint> itr2 = constraints.iterator();
  //   // List<Constraint> toBeRemoved = new ArrayList<Constraint>();
  //   // List<Constraint> toBeAdded = new ArrayList<Constraint>();
  //   // while (itr1.hasNext()) {
  //   //   Constraint c1 = itr1.next();
  //   //   while (itr2.hasNext()) {
  //   //     Constraint c2 = itr2.next();
  //   //     if (c1.equals(c2))
  //   //       continue;
  //   //     // c2 is subset of c1
  //   //     if (c1.isSubset(c2)) {
  //   //       Constraint jointConstr = c1.join(c2);
  //   //       toBeAdded.add(jointConstr);
  //   //       toBeRemoved.add(c1);
  //   //     }
  //   //     // c1 is subset of c2
  //   //     else if (c2.isSubset(c1)) {
  //   //       Constraint jointConstr = c2.join(c1);
  //   //       toBeAdded.add(jointConstr);
  //   //       toBeRemoved.add(c2);
  //   //     }
  //   //   }
  //   // }
  //   // constraints.removeAll(toBeRemoved);
  //   // constraints.addAll(toBeAdded);
  //   return;
  // }

  // Finds guaranteed assignments for the given constraints
  public HashMap<Variable, Integer> findSolution() {
    if (constraintVars.isEmpty())
      return null;
    // A copy of all the constraint variables -> variables left to assign
    Set<Variable> unassignedVars = new HashSet<Variable>(constraintVars);
    HashMap<Variable, Integer> assignment = new HashMap<Variable, Integer>();
    // Invoke CSP solver method
    HashMap<Variable, Integer> result = recursiveBacktracking(unassignedVars, assignment);
    // if (result != null)
      // printAssignment(result);

    return result;
  }

  public HashMap<Variable, Integer> recursiveBacktracking(Set<Variable> unassignedVars, HashMap<Variable, Integer> assignment) {
    if (unassignedVars.isEmpty()) return assignment;
    Variable var = Custom.pop(unassignedVars);
    // List of possible solutions-> one possible solution for each possible value of var (i.e 0, 1)
    List<HashMap<Variable, Integer>> solutions = new ArrayList<HashMap<Variable, Integer>>(2);

    // Iterate through each possible value to assign var
    for (int value = 0; value < 2; value++) {
      // Proceed if this value is a consistent assignment
      if (check_consistency(var, value, assignment)) {
        // Insert var = value assignment into new assignment list. 
        HashMap<Variable, Integer> newAssignment = new HashMap<Variable, Integer>(assignment);
        newAssignment.put(var, value);

        // If more vars to assign, recurse on those variables
        if (!unassignedVars.isEmpty()) {
          HashMap<Variable,Integer> result = recursiveBacktracking(
                          new HashSet<Variable>(unassignedVars), newAssignment);
          if (result != null)
            solutions.add(result);
        }
        else
          solutions.add(newAssignment);
      }
    }
    // No solutions found - return failure
    if (solutions.isEmpty())
      return null;
    // Only 1 solution found - return that solution
    if (solutions.size() == 1) 
      return solutions.remove(0);
    // 2 solutions for each value found - return the merged solution
    else
      return mergeSolutions(solutions.get(0), solutions.get(1));
  }

  // Checks if var = value is a consistent assignment given the current assignment and constraints
  private boolean check_consistency(Variable var, int value, HashMap<Variable, Integer> assignment) {
    for (Integer eqnIndex: varToEqn.get(var)) {
      int numAssignedVars = 1;
      int numMines = value;
      Constraint constr = constraints.get(eqnIndex);
      for (Variable v: constr.getVars()) {
        if (!v.equals(var)) {
          if (assignment.get(v) != null) {
            numAssignedVars++;
            numMines += assignment.get(v);
          }
        }
      }
      int actualConstraint = constr.getConstraint() - numMines;
      int numUnassignedVars = constr.getVars().size() - numAssignedVars;
      if (numUnassignedVars < actualConstraint || actualConstraint < 0)
        return false;
    }
    return true;
  }

  private HashMap<Variable, Integer> mergeSolutions(HashMap<Variable, Integer> solA, HashMap<Variable, Integer> solB) {
    HashMap<Variable, Integer> mergedSolution = new HashMap<Variable, Integer>();
    for (Variable v : solA.keySet()) {
      if (solA.get(v) == solB.get(v))
        mergedSolution.put(v, solA.get(v));
      else
        mergedSolution.put(v, -1);
    }
    return mergedSolution;
  }

  // Print constraints to console. For debugging
  public void printConstraints() {
    System.out.println("Printing Constraints : ");
    Iterator<Constraint> itr = constraints.iterator();
    while(itr.hasNext())
      itr.next().printConstraint();
    System.out.println("Printing Constraint Variables : ");
    String output = "  ";
    Iterator<Variable> itr2 = constraintVars.iterator();
    while(itr2.hasNext())
      output += itr2.next().varToString() + " | ";
    System.out.println(output);
  }

  private void printAssignment(HashMap<Variable, Integer> assignment) {
    System.out.println("Printing assignment: ");
    String output = "  ";
    for (HashMap.Entry<Variable, Integer> e : assignment.entrySet()) {
      output += e.getKey().varToString() + " = " + e.getValue() + " | " ;
    }
    System.out.println(output);
  }
}