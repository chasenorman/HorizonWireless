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
        //Graph.random(20, 1.5).save("src/graph.txt");
        BranchBound.solve(Graph.from("src/graph.txt"), "src/output.txt");
        //Graph G = Graph.from("src/graph.txt");
        //SolutionSet3 s = new SolutionSet3(G);
        //System.out.println(s.branch().get(1).branch().get(6).branch().get(0).branch().get(0).branch().get(0).branch().get(0).branch().get(0).branch().get(1).branch().get(0).branch().get(2).branch().get(0).branch().get(0).branch().get(0).bound());
        //System.out.println(s.branch().get(1).branch().get(6).branch().get(0).branch().get(0).branch().get(0).branch().get(0).branch().get(0).branch().get(1).branch().get(0).branch().get(2).branch().get(0).branch().get(0).bound());
        //System.out.println(s.branch().get(1).branch().get(6).branch().get(0).branch().get(0).branch().get(0).branch().get(0).branch().get(0).branch().get(1).branch().get(0).branch().get(2).branch().get(0).bound());
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
