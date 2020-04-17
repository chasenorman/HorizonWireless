import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.*;

public interface BranchBound extends Comparable<BranchBound> {


    List<BranchBound> branch();

    double bound();

    boolean isSolution();

    Solution heuristic();

    default int compareTo(BranchBound o) {
        return Double.compare(o.bound(), bound());
    }

    public static void solve(Graph G, String output) throws IOException {
        double best = Double.POSITIVE_INFINITY;

        try {
            Solution s = Solution.from(G, output);
            best = s.bound();
            System.out.println("Loaded solution with bound " + (best/1000));
        } catch (Exception ignored) {
            Solution s = G.mst();
            s.save(output);
            best = s.bound();
            System.out.println("No loaded solution found. Using MST " + (best/1000));
        }

        Stack<BranchBound> todo = new Stack<>();
        todo.add(new SolutionSet(G));

        while (!todo.isEmpty()) {
            BranchBound b = todo.pop();

            if (b.bound() >= best) {
                Main.rejections++;
                continue;
            }

            if (b.isSolution()) {
                ((Solution) b).save(output);
                best = b.bound();
                System.out.println(best/1000);
            }
            else {
                for (BranchBound next : b.branch()) {
                    Main.total++;

                    if (next.bound() < best) {
                        todo.add(next);
                    } else {
                        Main.rejections++;
                    }

                    if (Main.total % 10000 == 5000) {
                        System.out.println("rejection rate: " + Main.rejections / (float)Main.total + ", size: " + todo.size() + ", percent: " + Main.in/(float)Main.out);
                    }
                }
            }
        }
    }
}
