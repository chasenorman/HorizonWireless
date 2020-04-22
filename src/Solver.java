import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;

public class Solver extends Thread {
    double best;
    String output;
    Stack<BranchBound> todo = new Stack<>();
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

    public void run() {
        print("Starting");
        while (!todo.isEmpty()) {
            BranchBound b = todo.pop();

            if (b.bound() >= best) {
                continue;
            }

            if (iterations % 250000 == 0) {
                debug();
            }

            if (b instanceof Solution) {
                try {
                    ((Solution) b).save(output);
                    current = (Solution) b;
                } catch (Exception e) {
                    throw new IllegalArgumentException();
                }
                best = b.bound();
                print((best / 1000)+"");
            } else {
                for (BranchBound next : b.branch()) {
                    if (next.bound() < best) {
                        todo.add(next);
                    }
                }
            }

            iterations++;
        }
        try {
            current.save(opt);
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
        print("OPTIMAL");
    }

    public void debug() {
        double total = 1;
        for (BranchBound i : todo) {
            total -= i.size();
        }
        print("percent: " + total);
    }

    public void print(String s) {
        System.out.println("[" + output + "]" + " " + s);
    }
}