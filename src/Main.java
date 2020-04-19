import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

public class Main {
    static int rejections = 0;
    static int total = 0;

    static long in;
    static long out;
    static long t = System.currentTimeMillis();

    public static void main(String[] args) throws IOException {
        Graph.random(40, 1.5).save("src/graph.txt");
        BranchBound.solve(Graph.from("src/graph.txt"), "src/output.txt");
        //Graph G = Graph.from("src/graph.txt");
        //SolutionSet2 s = new SolutionSet2(G, G.requiredEdges().iterator().next(), Collections.emptySet());
        //System.out.println(s.branch().get(0).branch().get(4).branch().get(2).branch().get(1).branch().get(3).branch().get(0).bound());
        //System.out.println(s.branch().get(0).branch().get(4).branch().get(2).branch().get(1).branch().get(3).branch().get(0).bound());
        //System.out.println(s.branch().get(0).branch().get(4).branch().get(2).branch().get(1).branch().get(3).bound());
        System.out.println(System.currentTimeMillis()-t);
    }


    public static void start() {
        out += System.currentTimeMillis() - t;
        t = System.currentTimeMillis();
    }

    public static void end() {
        in += System.currentTimeMillis() - t;
        t = System.currentTimeMillis();
    }
}
