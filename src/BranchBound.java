import java.io.IOException;
import java.util.*;

public interface BranchBound {
    List<BranchBound> branch();

    double bound();

    double size();

    static void solve(Graph G, String output) throws IOException {
        double best;
        Solution s;
        try {
            s = Solution.from(G, output);
        } catch (Exception e) {
            s = null;
        }

        if (s == null || !s.verify(G)) {
            s = G.shortestPathTree(G.center());
            best = s.bound();
            System.out.println("No loaded solution found. Using Shortest Path Tree " + (best/1000));
            s.save(output);
        } else {
            best = s.bound();
            System.out.println("Loaded solution with bound " + (best / 1000));
        }

        Stack<BranchBound> todo = new Stack<>();
        todo.add(new SolutionSet(G));

        int iterations = 1;
        while (!todo.isEmpty()) {
            BranchBound b = todo.pop();

            if (iterations % 100000 == 0) {
                double total = 1;
                for (BranchBound i : todo) {
                    total -= i.size();
                }
                System.out.println("percent: " + total);
            }

            if (b instanceof Solution) {
                ((Solution) b).save(output);
                best = b.bound();
                System.out.println(best/1000);
            }
            else {
                for (BranchBound next : b.branch()) {
                    if (next.bound() <= best) {
                        todo.add(next);
                    }
                }
            }

            iterations++;
        }
    }
}
