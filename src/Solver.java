import java.io.IOException;
import java.util.*;

public class Solver extends Thread {
    public static final int LIMIT = 1500;
    double best;
    String output;
    TreeSet<BranchBound> todo = new TreeSet<>(Comparator.comparingDouble(s->s.heuristic().bound() + s.order()*2000));
    //Stack<BranchBound> todo = new Stack<>();
    int iterations = 1;
    Solution current;
    String opt;

    public Solver(String input, String output, String opt) throws IOException {
        this.output = output;
        this.opt = opt;
        Graph G = Graph.from(input);

        Solution s;
        try {
            s = Solution.from(G, output);
        } catch (Exception ignored) {
            s = G.shortestPathTree(G.center());
            s.save(output);
        }
        current = s;
        best = s.bound();
        //print((best / 1000)+"");

        todo.add(new SolutionSet(G));
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
        print("Starting");
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

            List<BranchBound> branch = b.branch();
            //branch.sort(Comparator.comparingDouble(a->-a.heuristic().bound()));
            for (BranchBound next : branch) {
                if (/*Math.max(1, 2 - 0.15*next.order())*/next.bound() < best) {
                    add(next);
                }
            }

            iterations++;
        }
        /*try {
            current.save(opt);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }*/
        print("DONE");
    }

    public void debug() {
    }

    public void print(String s) {
        System.out.println("[" + output + "]" + " " + s);
    }
}
