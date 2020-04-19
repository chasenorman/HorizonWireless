import java.io.IOException;

public class Main {
    public static long start = System.currentTimeMillis();
    public static long temp;
    public static long inside = 0;


    public static void main(String[] args) throws IOException {
        //Graph.random(20, 2).save("src/graph.txt");
        BranchBound.solve(Graph.from("src/graph.txt"), "src/output.txt");
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
