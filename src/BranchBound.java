import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.*;

public interface BranchBound extends Comparable<BranchBound> {
    List<BranchBound> branch();

    double bound();

    boolean isSolution();

    default double heuristicCost() {
        return bound();
    }

    default int compareTo(BranchBound o) {
        return Double.compare(heuristicCost(), o.heuristicCost());
    }

    public static void solve(Graph G, String output) throws IOException {
        double best = Double.POSITIVE_INFINITY;

        try {
            Solution s = Solution.from(G, output);
            best = s.bound();
            if (!s.isValid()) {
                throw new IllegalArgumentException();
            }
            System.out.println("Loaded solution with bound " + (best/1000));
        } catch (Exception ignored) {
            Solution s = G.mst();
            s.save(output);
            best = s.bound();
            System.out.println("No loaded solution found. Using MST " + (best/1000));
        }

        //PriorityQueue<BranchBound> todo = new PriorityQueue<>();
        Stack<BranchBound> todo = new Stack<>();
        todo.add(new SolutionSet3(G));

        while (!todo.isEmpty()) {
            //BranchBound b = todo.poll();
            BranchBound b = todo.pop();


            if (b.bound() > best) {
                Main.rejections++;
                continue;
            }

            if (b.isSolution()) {
                ((Solution3) b).save(output);
                best = b.bound();
                System.out.println(best/1000);
            }
            else {
                for (BranchBound next : b.branch()) {
                    Main.total++;

                    if (next.bound() <= best) {
                        todo.add(next);
                    } else {
                        Main.rejections++;
                    }

                    if (Main.total % 100000 == 0) {
                        System.out.println("rejection rate: " + Main.rejections / (float)Main.total + ", size: " + todo.size() + ", percent: " + Main.in/(float)Main.out);
                        Main.total = 0;
                        Main.rejections = 0;
                    }
                }
            }
        }
    }
}
