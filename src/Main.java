import java.io.IOException;
import java.util.TreeSet;

public class Main {
    static int rejections = 0;
    static int total = 0;

    static long in;
    static long out;
    static long t;

    public static void main(String[] args) throws IOException {
        //Graph.random(30).save("src/graph.txt");
        t = System.currentTimeMillis();
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
