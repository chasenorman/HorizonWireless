import java.io.IOException;
import java.util.*;

public class Solver extends Thread {
    public static final int LIMIT = 1500;
    double best;
    String output;
    TreeSet<BranchBound> todo = new TreeSet<>(Comparator.comparingDouble(s->s.heuristic().bound() + s.order()*2000));
    int iterations = 1;
    Solution current;
    String opt;
    Graph G;

    public Solver(String input, String output, String opt) throws IOException {
        this.output = output;
        this.opt = opt;
        G = Graph.from(input);

        Solution s;
        try {
            s = Solution.from(G, output);
        } catch (Exception ignored) {
            s = G.shortestPathTree(G.center());
            s.save(output);
        }
        current = s;
        best = s.bound();
        todo.add(new SolutionSet(G, this));
    }

    public BranchBound next() {
        return todo.pollFirst();
    }

    public void add(BranchBound next) {
        if (todo.size() < LIMIT) {
            todo.add(next);
        } else if (next.heuristic().bound() < todo.last().heuristic().bound()) {
            todo.pollLast();
            todo.add(next);
        }
    }

    public void run() {
        print("Starting with " + (best/1000));
        while (!todo.isEmpty()) {
            BranchBound b = next();

            Solution heuristic = b.heuristic();
            if (heuristic.bound() < best) {
                try {
                    heuristic.save(output);
                } catch (Exception e) {
                    throw new IllegalArgumentException();
                }
                current = heuristic;
                best = heuristic.bound();
                print((best / 1000)+"");
            }

            for (BranchBound next : b.branch()) {
                if (next.bound() < best) {
                    add(next);
                }
            }

            iterations++;
        }
        print("DONE");
    }

    public void debug() {
    }

    public void print(String s) {
        System.out.println("[" + output + "]" + " " + s);
    }

    public double score(Edge e) { // lower is better.
        double ud = G.incident[e.u].size();
        double vd = G.incident[e.v].size();
        Random r = new Random(e.u + 101*e.v + 10001*Main.seed);
        double n = Math.sqrt(e.w);
        double d = 10;//ud*vd;
        boolean inCurrent = false;
        for (Edge o : current.edges) {
            if (o.equals(e)) {
                inCurrent = true;
                break;
            }
        }

        return 5*r.nextDouble() + n/d - (inCurrent?100:0);
    }

    public int selectionOrder(Edge e1, Edge e2) {
        return Double.compare(score(e2), score(e1));
    }
}
