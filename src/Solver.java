import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

public class Solver extends Thread {
    double best;
    String output;
    Stack<SolutionSet> todo = new Stack<>();
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

        todo.push(new SolutionSet(G));
    }

    public void run() {
        print("Starting");
        while (!todo.isEmpty()) {
            BranchBound b = todo.pop();

            if (iterations % 250000 == 0) {
                debug();
            }

            List<BranchBound> branch = b.branch();
            branch.sort(Comparator.comparingDouble(a->-a.bound()));
            for (BranchBound next : branch) {
                if (next instanceof Solution) {
                    if (next.bound() >= best) {
                        continue;
                    }
                    try {
                        ((Solution) next).save(output);
                        current = (Solution) next;
                    } catch (Exception e) {
                        throw new IllegalArgumentException();
                    }
                    best = b.bound();
                    print((best / 1000)+"");
                } else if (/*Math.max(1, 5 - 0.15*next.order())*/next.bound() < best) {
                    todo.push((SolutionSet) next);
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
        //double total = 1;
        /*for (BranchBound i : todo) {
            total -= i.size();
        }*/
        //print("percent: " + total);
    }

    public void print(String s) {
        System.out.println("[" + output + "]" + " " + s);
    }
}
