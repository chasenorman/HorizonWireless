import java.io.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class Graph {
    public static final boolean DEBUG = true;

    public static final int INF = 100000000;

    // Adjacency List
    public final HashSet<Edge>[] incident;

    public final HashSet<Edge>[] incidentStandard;

    // Adjacency Matrix
    public final int[][] adjacency;

    // Edge set
    public final TreeSet<Edge> edges = new TreeSet<>();

    public final int n;

    public Graph(int n) {
        this.n = n;
        incident = new HashSet[n];
        incidentStandard = new HashSet[n];
        articulationPoints = new HashSet<>();
        for (int i = 0; i < n; i++){
            incident[i] = new HashSet<>();
            incidentStandard[i] = new HashSet<>();
        }
        adjacency = new int[n][n];
        for (int x = 0; x < n; x++) {
            for (int y = 0; y < n; y++) {
                adjacency[x][y] = x == y ? 0 : INF;
            }
        }
    }

    public HashSet<Integer> articulationPoints;

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
        e = e.standard();
        incidentStandard[u].add(e);
        incidentStandard[v].add(e);
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

    public static Graph random(int n, double associativity) {
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

        for (i = 0; i < n*(associativity-1); ) {
            Edge e = Edge.random(n);
            if (result.adjacency[e.u][e.v] == INF) {
                result.add(e.u, e.v, e.w);
                i++;
            }
        }

        return result;
    }

    public Solution mst() {
        Node<Edge> mst = new Node<>();
        UnionFind u = new UnionFind(n);
        for (Edge e : edges) { // this depends on ordering.
            if (u.find(e.u) != u.find(e.v)) {
                mst = new Node<>(e, mst);
                u.union(e.u, e.v);
            }
            if (mst.size == n - 1) {
                break;
            }
        }

        Node<Integer> vertices = new Node<>();
        for (int i = 0; i < n; i++){
            vertices = new Node<>(i, vertices);
        }

        return new Solution(mst, vertices, n);
    }

    public void save(String file) throws IOException {
        BufferedWriter writer = new BufferedWriter( new FileWriter(file));
        writer.write(toString());
        writer.close();
    }

    public void setArticulationPoints() {
        HashSet<Integer> result = new HashSet<Integer>();

        for (int i = 0; i < n; i++) {
            if (incident[i].size() == 1) {
                result.add(incident[i].iterator().next().v);
            }
        }

        articulationPoints = result;
    }

    public Solution shortestPathTree(int v) {
        int[] prev = new int[n];
        int[] dist = new int[n];

        PriorityQueue<Integer> q = new PriorityQueue<>(Comparator.comparingInt(o -> dist[o]));

        for (int i = 0; i < n; i++) {
            if (i != v) {
                dist[i] = INF;
            }
            q.add(i);
        }

        while (!q.isEmpty()) {
            int u = q.poll();
            for (Edge e : incident[u]) {
                int temp = dist[u] + e.w;
                if (temp < dist[e.v] || dist[e.v] == INF) {
                    dist[e.v] = temp;
                    prev[e.v] = u;
                    q.remove(e.v);
                    q.add(e.v);
                }
            }
        }

        Node<Edge> edges = new Node<>();

        for (int i = 0; i < n; i++) {
            if (i != v) {
                edges = new Node<>(new Edge(i, prev[i], adjacency[i][prev[i]]).standard(), edges);
            }
        }

        Node<Integer> vertices = new Node<>();
        for (int i = 0; i < n; i++){
            vertices = new Node<>(i, vertices);
        }

        Solution result = new Solution(edges, vertices, n);

        if (!result.verify(this)) {
            throw new IllegalArgumentException();
        }

        return result;
    }

    public int[][] distance() {
        int[][] distance = new int[n][n];
        for (int x = 0; x < n; x++) {
            System.arraycopy(adjacency[x], 0, distance[x], 0, n);
        }

        for(int k = 0; k < n; k++) {
            for(int i = 0; i < n; i++) {
                int distki = (k<i)?distance[k][i]:distance[i][k];
                if(k == i || distki == Graph.INF) {
                    continue;
                }

                for(int j = i+1; j < n; j++) {
                    int distkj = (k<j)?distance[k][j]:distance[j][k];
                    if(k == j || i == j || distkj == Graph.INF) {
                        continue;
                    }
                    int via = distki + distkj;
                    if(via < distance[i][j]) {
                        distance[i][j] = via;
                        distance[j][i] = via;
                    }
                }
            }
        }

        return distance;
    }


    public int center() {
        int[][] distance = distance();
        int[] maximum = new int[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                maximum[i] = Math.max(maximum[i], distance[i][j]);
            }
        }
        int minIndex = 0;
        for (int i = 1; i < n; i++) {
            if (maximum[minIndex] > maximum[i]) {
                minIndex = i;
            }
        }
        return minIndex;
    }
}
