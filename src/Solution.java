import java.io.*;
import java.util.*;

public class Solution implements BranchBound {
    HashSet<Integer> vertices;
    TreeSet<Edge> edges;
    Graph G;
    long cost = -1;

    public Solution(Graph G, TreeSet<Edge> edges) {
        this.G = G;
        this.edges = edges;
        vertices = new HashSet<>();
        for (Edge e : edges) {
            vertices.add(e.u);
            vertices.add(e.v);
        }
    }

    public static Solution from(Graph G, String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));
        br.readLine();
        String str;
        TreeSet<Edge> edges = new TreeSet<>();
        while ((str = br.readLine()) != null) {
            if (str.isEmpty()){
                continue;
            }
            String[] result = str.split(" ");
            int x = Integer.parseInt(result[0]);
            int y = Integer.parseInt(result[1]);
            if (G.adjacency[x][y] == Graph.INF) {
                throw new IOException();
            }
            edges.add(new Edge(x, y, G.adjacency[x][y]));
        }
        br.close();
        if (!isValid(G, edges)) {
            System.out.println(edges);
            throw new IOException();
        }
        return new Solution(G, edges);
    }

    public boolean isValid() {
        return Solution.isValid(G, edges);
    }

    public static boolean isValid(Graph G, TreeSet<Edge> edges) {
        if (edges.size() < G.n/2 || edges.size() > G.n-1) {
            return false;
        }

        HashSet<Integer> vertices = new HashSet<>();
        for (Edge e : edges) {
            vertices.add(e.u);
            vertices.add(e.v);
        }

        Outer: for(int i = 0; i < G.n; i++) {
            if (!vertices.contains(i)) {
                for (Edge e : G.incident[i]) {
                    if (vertices.contains(e.v)) {
                        continue Outer;
                    }
                }
                return false;
            }
        }

        UnionFind u = new UnionFind(G.n);
        for (Edge e : edges) {
            if (u.find(e.u) == u.find(e.v)) {
                return false;
            }
            u.union(e.u, e.v);
        }

        int test = u.find(edges.first().u);
        for (Edge e : edges) {
            if (u.find(e.u) != test) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int v : vertices) {
            result.append(v).append(" ");
        }
        result.append("\n");
        for (Edge e : edges) {
            result.append(e.u).append(" ").append(e.v).append("\n");
        }
        return result.toString();
    }

    @Override
    public List<BranchBound> branch() {
        return Collections.singletonList(this);
    }

    @Override
    public double bound() {
        if (cost == -1) {
            cost = 0;
            Graph T = new Graph(G.n);
            for (Edge e : edges) {
                T.add(e.u, e.v, e.w);
            }
            boolean[] marked = new boolean[G.n];
            DFS(T, vertices.iterator().next(), marked);
        }
        return 2*cost/(double)(vertices.size()*(vertices.size() - 1));
    }

    public int DFS(Graph T, int v, boolean[] marked) {
        int sum = 0;
        marked[v] = true;
        for (Edge e : T.incident[v]) {
            if (!marked[e.v]) {
                int edge = DFS(T, e.v, marked) + 1;
                cost += edge*(vertices.size() - edge)*e.w;
                sum += edge;
            }
        }
        return sum;
    }

    @Override
    public boolean isSolution() {
        return true;
    }

    @Override
    public Solution heuristic() {
        return this;
    }

    public void save(String file) throws IOException {
        BufferedWriter writer = new BufferedWriter( new FileWriter(file));
        writer.write(toString());
        writer.close();
    }
}
