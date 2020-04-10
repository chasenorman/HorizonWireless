import java.util.HashSet;
import java.util.TreeSet;

public class Solution {
    HashSet<Integer> vertices;
    Graph T;
    Graph G;

    public Solution(Graph G) {
        this.G = G;
        T = new Graph(G.n);
    }

    public boolean isValid() {
        for(int i = 0; i < G.n; i++) {
            if (!vertices.contains(i)) {
                for (Edge e : G.incident[i]) {
                    if (!vertices.contains(e.v)) {
                        return false;
                    }
                }
            }
        }
        if (T.edges.size() != G.n - 1) {
            return false;
        }
        //TODO see if graph is connected
        return true;
    }
}
