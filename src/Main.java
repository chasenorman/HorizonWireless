import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        //Graph.random(20, 2).save("src/graph.txt");
        BranchBound.solve(Graph.from("src/graph.txt"), "src/output.txt");
    }
}
