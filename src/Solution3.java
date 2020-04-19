import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class Solution3 implements BranchBound {
    Node<Edge> edges;
    Node<Integer> vertices;
    long cost = -1;
    int n;

    public Solution3(Node<Edge> e, Node<Integer> v, int n) {
        this.edges = e;
        this.vertices = v;
        this.n = n;
    }

    public static Solution3 from(Graph G, String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(file)));

        String str = br.readLine();
        String[] arr = str.split(" ");
        Node<Integer> vertices = new Node<>();
        for (String v : arr) {
            vertices = new Node<>(Integer.parseInt(v), vertices);
        }

        Node<Edge> edges = new Node<>();
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
            edges = new Node<>(new Edge(x, y, G.adjacency[x][y]), edges);
        }
        br.close();

        Solution3 s = new Solution3(edges, vertices, G.n);
        if (!s.verify(G)) {
            throw new IOException();
        }
        return s;
    }

    public boolean verify(Graph G) {
        if (edges.size != vertices.size - 1 || G.n != n) {
            return false;
        }

        UnionFind u = new UnionFind(G.n);
        for (Edge e : edges) {
            if (e.u >= G.n || e.v >= G.n || G.adjacency[e.u][e.v] != e.w) {
                return false;
            }
            u.union(e.u, e.v);
        }

        int cc = u.find(vertices.last);
        for (int v : vertices) {
            if (v >= G.n || u.find(v) != cc) {
                return false;
            }
        }

        Outer: for (int i = 0; i < G.n; i++) {
            if (u.find(i) != cc) {
                for (Edge e : G.incident[i]) {
                    if (u.find(e.v) == cc) {
                        continue Outer;
                    }
                }
                return false;
            }
        }

        return true;
    }

    public boolean verifyPartial(Graph G) {
        if (edges.size != vertices.size - 1) {
            return false;
        }

        UnionFind u = new UnionFind(G.n);
        for (Edge e : edges) {
            u.union(e.u, e.v);
        }

        int cc = u.find(vertices.last);
        for (int v : vertices) {
            if (u.find(v) != cc) {
                return false;
            }
        }

        Outer: for (int i = 0; i < G.n; i++) {
            if (u.find(i) != cc) {
                for (Edge e : G.incident[i]) {
                    if (u.find(e.v) == cc) {
                        continue Outer;
                    }
                }
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

    public List<BranchBound> branch() {
        return Collections.singletonList(this);
    }

    public void save(String file) throws IOException {
        BufferedWriter writer = new BufferedWriter( new FileWriter(file));
        writer.write(toString());
        writer.close();
    }

    public double bound() {
        if (cost == -1) {
            cost = 0;
            Node<Edge>[] incident = new Node[n];
            boolean[] marked = new boolean[n];
            for (int i = 0; i < n; i++) {
                incident[i] = new Node<>();
            }
            for (Edge e : edges) {
                incident[e.u] = new Node<>(e, incident[e.u]);
                incident[e.v] = new Node<>(e.reversed(), incident[e.v]);
            }
            DFS(incident, marked, vertices.last);
        }
        return 2*cost/(double)(vertices.size*(vertices.size-1));
    }

    @Override
    public boolean isSolution() {
        return true;
    }

    private int DFS(Node<Edge>[] adjacency, boolean[] marked, int v) {
        int sum = 0;
        marked[v] = true;
        for (Edge e : adjacency[v]) {
            if (!marked[e.v]) {
                int edge = DFS(adjacency, marked, e.v) + 1;
                cost += edge*(vertices.size - edge)*e.w;
                sum += edge;
            }
        }
        return sum;
    }
}