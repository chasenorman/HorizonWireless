import java.io.IOException;

public class Main {
    static long in;
    static long out;
    static long t = System.currentTimeMillis();

    public static void main(String[] args) throws IOException {
        Graph.random(20, 2).save("src/graph.txt");
        BranchBound.solve(Graph.from("src/graph.txt"), "src/output.txt");
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
