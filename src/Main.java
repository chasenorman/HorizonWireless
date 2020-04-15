import java.io.IOException;
import java.util.TreeSet;

public class Main {
    static int rejections = 0;
    static int total = 0;

    public static void main(String[] args) throws IOException {
        Graph.random(10).save("src/graph.txt");
        BranchBound.solve(Graph.from("src/graph.txt"), "src/output.txt");
    }
}
