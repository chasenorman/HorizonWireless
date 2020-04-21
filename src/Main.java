import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Main {
    public static long start = System.currentTimeMillis();
    public static long temp;
    public static long inside = 0;
    public static int seed = (int)System.currentTimeMillis();

    public static final String INPUT_DIR = "inputs/";
    public static final String OUTPUT_DIR = "outputs/";
    public static final String OPT_DIR = "OPT/";

    public static void main(String[] args) throws IOException {
        //Graph.random(20, 2).save(graph);
        //cheese();
        new Solver(INPUT_DIR + "medium-228.in", OUTPUT_DIR + "medium-228.out", OPT_DIR + "medium-228.out").start();
    }

    public static double testHeuristic() throws IOException {
        double total = 0;
        for (String file : new File(OPT_DIR).list()) {
            if (file.startsWith(".")) {
                continue;
            }
            String input = file.replace(".out", ".in");
            Graph G = Graph.from(INPUT_DIR + input);
            Solution opt = Solution.from(G, OPT_DIR + file);

            if (!opt.verify(G)) {
                throw new IllegalArgumentException();
            }

            Edge[] sorted = new Edge[G.edges.size()];
            G.edges.toArray(sorted);
            Arrays.sort(sorted, G::selectionOrder);
            List<Edge> edges = Arrays.asList(sorted);

            for (Edge e : opt.edges) {
                if (edges.indexOf(e) == -1) {
                    throw new IllegalArgumentException();
                }
                total += edges.indexOf(e)/(double)edges.size();
            }
        }
        return total;
    }

    public static void cheese() throws IOException {
        for (String file : new File(INPUT_DIR).list()) {
            if (file.startsWith(".")) {
                continue;
            }

            Graph G = Graph.from(INPUT_DIR + file);
            for (int i = 0; i < G.n; i++) {
                if (G.incident[i].size() == G.n-1) {
                    Node<Integer> vertices = new Node<>();
                    Solution s = new Solution(new Node<>(), new Node<>(i, vertices), G.n);
                    System.out.println("CHEESE == " + s.verify(G) + " on " + file);
                    if (s.verify(G)) {
                        s.save("cheese/" + file.replace(".in", ".out"));
                    }
                    break;
                }
            }
        }
    }

    public static void run() throws IOException {
        for (String file : new File(INPUT_DIR).list()) {
            String output = file.replace(".in", ".out");
            if (new File(OPT_DIR + output).exists()) {
                continue;
            }
            //Graph G = Graph.from(INPUT_DIR + file);
            new Solver(INPUT_DIR + file, OUTPUT_DIR + output, OPT_DIR + output).start();
        }
    }

    public static void start() {
        temp = System.currentTimeMillis();
    }

    public static void stop() {
        inside += System.currentTimeMillis()-temp;
    }

    public static double percent() {
        long total = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        double result = inside/(double)total;
        inside = 0;
        return result;
    }
}
