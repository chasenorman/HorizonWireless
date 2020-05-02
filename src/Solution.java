import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Solution implements BranchBound {
    Node<Edge> edges;
    Node<Integer> vertices;
    long cost = -1;
    int n;

    public Solution(Node<Edge> e, Node<Integer> v, int n) {
        this.edges = e;
        this.vertices = v;
        this.n = n;
    }

    public Solution(Node<Edge> edges, int n) {
        this.edges = edges;
        HashSet<Integer> v = new HashSet<>();
        for (Edge e : edges) {
            v.add(e.u); v.add(e.v);
        }
        vertices = new Node<>();
        for (int i : v) {
            vertices = new Node<>(i, vertices);
        }
        this.n = n;
    }

    public static Solution from(Graph G, String file) throws IOException {
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

        Solution s = new Solution(edges, vertices, G.n);
        if (!s.verify(G)) {
            throw new IOException();
        }
        return s;
    }

    public boolean verify(Graph G) {
        if (edges.size != vertices.size - 1 || G.n != n || vertices.size == 0) {
            return false;
        }

        UnionFind u = new UnionFind(G.n);
        for (Edge e : edges) {
            if (e.u >= G.n || e.v >= G.n || G.adjacency[e.u][e.v] != e.w || G.adjacency[e.u][e.v] == Graph.INF || u.find(e.u) == u.find(e.v)) {
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
        if (edges.size != vertices.size - 1 || vertices.size == 0) {
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
        writer.flush();
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

    public double size() {
        return 0;
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

    @Override
    public int order() {
        return n;
    }

    public Solution heuristic() {
        return this;
    }

    public Solution relax(Graph G) {
        for (Edge e : edges) {
            Node<Edge> next = new Node<>();
            for (Edge e2 : edges) {
                if (e2 != e) {
                    next = new Node<>(e2, next);
                }
            }
            Solution result = new Solution(next, n);
            if (result.verify(G) && result.bound() < this.bound()) {
                return result;
            }
        }
        return this;
    }

    public Solution expand(Graph G) {
        UnionFind u = new UnionFind(G.n);
        for (Edge e : edges) {
            u.union(e.u, e.v);
        }
        int cc = u.find(vertices.last);

        for (Edge e : G.edges) {
            if (u.find(e.u) == cc && u.find(e.v) != cc) {
                Solution s = new Solution(new Node<>(e, edges), new Node<Integer>(e.v, vertices), G.n);
                if (s.bound() < this.bound()) {
                    return s;
                }
            } else if (u.find(e.v) == cc && u.find(e.u) != cc) {
                Solution s = new Solution(new Node<>(e, edges), new Node<Integer>(e.u, vertices), G.n);
                if (s.bound() < this.bound()) {
                    return s;
                }
            }
        }
        return this;
    }

    public Solution settle(Graph G) {
        Solution current = this;
        Solution prev = null;
        while (prev != current) {
            prev = current;
            current = current.relax(G);
            current = current.expand(G);
        }
        return current;
    }
}
