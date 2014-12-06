package custom;

import map.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

public final class Custom implements Strategy {

  ConstraintSet constraints;

  public void play(Map m) {
    System.out.println("----------NEW GAME----------");
    constraints = new ConstraintSet();
    // Probe first square (corner)
    int x0 = 0;
    int y0 = 0;
    Set<Variable> toBeProbed = new HashSet<Variable>(); // vars that can be probed
    Set<Variable> potentialGuesses = new HashSet<Variable>();
    toBeProbed.add(new Variable(x0, y0));
    // Repeat until the game is lost or won
    while (!m.done()) {

      // If no valid squares to be probed, make guess
      if (toBeProbed.isEmpty()) {
        Variable guess;
        if (potentialGuesses.isEmpty()) {
          guess = makeGuess(m);
          System.out.println("Making Random Guess : " + guess.varToString());
        }
        else {
          guess = pop(potentialGuesses);
          System.out.println("Making Boundary Guess : " + guess.varToString());
        }
        toBeProbed.add(guess);
      }
      potentialGuesses.clear();
      
      // Array with squares whose adj squares may not be mine-free
      Set<Variable> questionable = new HashSet<Variable>();
      while (!toBeProbed.isEmpty()) {
        // Probe all squares known to be mine-free
        List<Variable> vars = probeAndIterate(toBeProbed, m);
        // Null only returned when a mine was probed!
        if (vars == null) return;
        questionable.addAll(vars);
        findMarkableAndMark(questionable, m);
        toBeProbed = findProbeable(questionable, m);
      }

      // CSP - first create constraints. Then attempt to find a solution
      constraints.createConstraints(questionable, m);
      // constraints.printConstraints(); // DEBUGGING 
      HashMap<Variable, Integer> solution = constraints.findSolution();
      if (solution != null) {
        // Parse solution for markeable and probeable squares
        for (HashMap.Entry<Variable, Integer> e : solution.entrySet()) {
          if (e.getValue() == 0)
            toBeProbed.add(e.getKey());
          else if (e.getValue() == 1) 
            m.mark(e.getKey().getX(), e.getKey().getY());
          else // squares that could be a mine or not -- valid guesses
            potentialGuesses.add(e.getKey());
        }
      }
    }
  }

  public void play(Map m, int c, int r) {
    play(m);
  }


  /* 
  *  Probe position of vars in toBeProbed. Then iteratively
  *  probe all mine free squares that become known from initial
  *  probing (i.e. probe all adj. zero squares)
  */
  public List<Variable> probeAndIterate(Set<Variable> toBeProbed, Map m) {
    // Contains all the mine free squares to be probed
    List<Variable> questionable = new ArrayList<Variable>();
    // Iteratively probe all known mine free squares
    while (!toBeProbed.isEmpty()) {
      Variable var = pop(toBeProbed);
      int x = var.getX();
      int y = var.getY();
      int squareNum = m.probe(x,y);
      // Unconvered a mine -- Game over!
      if (squareNum == Map.BOOM) return null;
      // If square is 0 or adj squares known to be mine free, add adj to be probed
      if (squareNum == 0 || (squareNum - adjMarked(x, y, m)) == 0) {
        List<Variable> adj = getAdjUnmarked(x, y, m);
        toBeProbed.addAll(adj);
      }
      else // adjacent mines not known to be mine free. 
       questionable.add(new Variable(x,y));
    }
    return questionable;
  }

  /*
  *  For each var v in q, determine if we can mark all the adjacent
  *  unknown squares to v. If so, mark those squares and remove v 
  *  from q.
  */
  public void findMarkableAndMark(Set<Variable> q, Map m) {
    Iterator<Variable> itr = q.iterator();
    List<Variable> toBeRemoved = new ArrayList<Variable>();
    while (itr.hasNext()) {
      Variable v = itr.next();
      int n = m.look(v.getX(), v.getY());
      int nMarked = adjMarked(v.getX(), v.getY(), m);
      int actual_n = n - nMarked;
      List<Variable> adjVars = getAdjUnmarked(v.getX(), v.getY(), m);
      // If the number on v = # adj unknown squares, those adj must be mines
      if (actual_n == adjVars.size()) {
        for (Variable adj : adjVars)
          m.mark(adj.getX(), adj.getY());
        toBeRemoved.add(v);
      }
    }
    q.removeAll(toBeRemoved);
    return;
  }

  /*
  *  For each var v in q, determine if we can probe squares adjacent
  *  to v. This occurs if the number on v equals the number of marked
  *  adjacent mines. Returns list of squares known to be mine-free
  */
  public Set<Variable> findProbeable(Set<Variable> q, Map m) {
    Iterator<Variable> itr = q.iterator();
    Set<Variable> toBeProbed = new HashSet<Variable>();
    List<Variable> toBeRemoved = new ArrayList<Variable>();
    while (itr.hasNext()) {
      Variable v = itr.next();
      int n = m.look(v.getX(), v.getY());
      int nMarked = adjMarked(v.getX(), v.getY(), m);
      // If the number on v = # adj marked, other adj must be mine free
      if (n == nMarked) {
        toBeProbed.addAll(getAdjUnmarked(v.getX(), v.getY(), m));
        toBeRemoved.add(v);
      }
    }
    q.removeAll(toBeRemoved);
    return toBeProbed;
  }

  /*
  * Returns a list of the unprobed and unmarked squares adjacent
  * to (c,r) as variables.
  */
  public static List<Variable> getAdjUnmarked(int c, int r, Map m) {
    List<Variable> adjVars = new ArrayList<Variable>();
    for (int y = r - 1; y <= r + 1; y++) {
      for (int x = c - 1; x <= c + 1; x++) {
        // skip center square
        if (x == c && y == r)
          continue;
        if (m.look(x,y) == Map.UNPROBED)
          adjVars.add(new Variable(x, y));
      }
    }
    return adjVars;
  }

  // Returns the number of marked squares adjacent to square (x,y)
  public static int adjMarked(int x, int y, Map m) {
    int numMarked = 0;
    for (int j = y - 1; j <= y + 1; j++) {
      for (int i = x - 1; i <= x + 1; i++) {
        if (m.look(i, j) == Map.MARKED) 
          numMarked++;
      }
    }
    return numMarked;
  }

  public static Variable makeGuess(Map m) {
    Variable guess;
    // First check if corners are free to guess
    guess = checkCorners(m);
    if (guess != null) return guess;
    int x = 0;
    int y = 0;
    do {
      y = m.pick(m.rows());
      x = m.pick(m.columns());
    } while (m.look(x,y) != Map.UNPROBED);
    guess = new Variable(x,y);
    return guess;
  }

  public static Variable checkCorners(Map m) {
    if (m.look(0, m.rows() - 1) == Map.UNPROBED)
      return new Variable(0, m.rows() - 1);
    else if (m.look(m.columns() - 1, m.rows() - 1) == Map.UNPROBED)
      return new Variable(m.columns() - 1, m.rows() - 1);
    else if (m.look(m.columns() - 1, 0) == Map.UNPROBED) 
      return new Variable(m.columns() - 1, 0);
    else
      return null;

  }

  public static Variable pop(Set<Variable> s) {
    Iterator<Variable> itr = s.iterator();
    if (itr.hasNext()) {
      Variable e = itr.next();
      s.remove(e);
      return e;
    }
    return null;
  }


}