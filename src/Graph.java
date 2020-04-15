import java.io.*;
import java.util.TreeSet;

public class Graph {
    public static final boolean DEBUG = true;

    public static final int INF = 10000000;

    // Adjacency List
    public final TreeSet<Edge>[] incident;

    // Adjacency Matrix
    public final int[][] adjacency;

    // Edge set
    public final TreeSet<Edge> edges = new TreeSet<>();

    public final int n;

    public Graph(int n) {
        this.n = n;
        incident = new TreeSet[n];
        for (int i = 0; i < n; i++){
            incident[i] = new TreeSet<>();
        }
        adjacency = new int[n][n];
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                adjacency[x][y] = x == y ? 0 : INF;
            }
        }
    }

    public static Graph from(String s) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(s)));
        String str = br.readLine();
        Graph g = new Graph(Integer.parseInt(str));
        while ((str = br.readLine()) != null) {
            if (str.isEmpty()){
                continue;
            }
            String[] result = str.split(" ");
            g.add(Integer.parseInt(result[0]), Integer.parseInt(result[1]), (int)(Float.parseFloat(result[2])*1000));
        }
        br.close();
        return g;
    }

    public void add(int u, int v, int w) {
        if (adjacency[u][v] != INF) {
            throw new IllegalArgumentException();
        }
        Edge e = new Edge(u, v, w);
        incident[u].add(e);
        incident[v].add(e.reversed());
        adjacency[u][v] = w;
        adjacency[v][u] = w;
        edges.add(e);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(n + "\n");
        for (Edge e : edges) {
            result.append(e).append("\n");
        }
        return result.toString();
    }

    public static Graph random(int n) {
        Graph result = new Graph(n);

        UnionFind u = new UnionFind(n);
        int i = 0;
        while (i < n-1) {
            Edge e = Edge.random(n);
            if (u.find(e.u) != u.find(e.v)) {
                u.union(e.u, e.v);
                result.add(e.u, e.v, e.w);
                i++;
            }
        }

        for (i = 0; i < n; ) {
            Edge e = Edge.random(n);
            if (result.adjacency[e.u][e.v] == INF) {
                result.add(e.u, e.v, e.w);
                i++;
            }
        }

        return result;
    }

    public void save(String file) throws IOException {
        BufferedWriter writer = new BufferedWriter( new FileWriter(file));
        writer.write(toString());
        writer.close();
    }
}
