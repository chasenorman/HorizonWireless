import java.io.*;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class Graph {
    public static final boolean DEBUG = true;

    public static final int INF = 100000000;

    // Adjacency List
    public final TreeSet<Edge>[] incident;

    public final TreeSet<Edge>[] incidentStandard;

    // Adjacency Matrix
    public final int[][] adjacency;

    // Edge set
    public final TreeSet<Edge> edges = new TreeSet<>();

    public final int n;

    public Graph(int n) {
        this.n = n;
        incident = new TreeSet[n];
        incidentStandard = new TreeSet[n];
        for (int i = 0; i < n; i++){
            incident[i] = new TreeSet<>();
            incidentStandard[i] = new TreeSet<>();
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
        TreeSet<Edge> mst = new TreeSet<>();
        UnionFind u = new UnionFind(n);
        for (Edge e : edges.descendingSet()) { // this depends on ordering.
            if (u.find(e.u) != u.find(e.v)) {
                mst.add(e);
                u.union(e.u, e.v);
            }
            if (mst.size() == n - 1) {
                break;
            }
        }
        return new Solution(this, mst);
    }

    public Solution cantor() {
        TreeSet<Edge> T = new TreeSet<>();

        int[] d = new int[n];
        int[] s = new int[n];
        int[] m = new int[n];
        int sumWeights = 0;

        for (Edge e : edges) {
            d[e.u]++; d[e.v]++;
            s[e.u]+=e.w; s[e.v]+=e.w;
            m[e.u] = Math.max(m[e.u], e.w);
            m[e.v] = Math.max(m[e.v], e.w);
            sumWeights += e.w;
        }

        double mean = sumWeights/(double)edges.size();
        double sum = 0;
        for (Edge e : edges) {
            sum += (mean - e.w)*(mean - e.w);
        }
        double stdDev = Math.sqrt(sum/(edges.size()-1));
        double ratio = stdDev/mean;

        double C4, C5;

        if (ratio < 0.4+0.005*(n-10)) {
            C4 = 1; C5 = 1;
        } else {
            C4 = 0.9; C5 = 0.1;
        }

        int[] w = new int[n];
        Color[] color = new Color[n];
        double[] sp = new double[n];
        int f = 0;
        double sp_max = 0;

        for (int v = 0; v < n; v++) {
            w[v] = INF;
            color[v] = Color.WHITE;
            sp[v] = 0.2*d[v] + 0.6*(d[v]/(double)s[v]) + (0.2/m[v]);
            if (sp[v] > sp_max) {
                sp_max = sp[v];
                f = v;
            }
        }

        int[] cf = new int[n];
        int[] p = new int[n];
        int[] pd = new int[n];
        int[] ps = new int[n];

        w[f] = 0; cf[f] = 0; p[f] = f; pd[f] = 0; ps[f] = 1;
        color[f] = Color.GREY;
        HashSet<Integer> L = new HashSet<>();
        L.add(f);

        double[] wd = new double[n];
        double[] jsp = new double[n];

        int spanned_vertices = 0;
        while (spanned_vertices < n) {
            int u = -1;
            for (int i : L) {
                if (u == -1 || wd[i] < wd[u]) {
                    u = i;
                } else if(wd[i] == wd[u] && jsp[i] >= jsp[u]) {
                    u = i;
                }
            }
            L.remove(u);

            for (Edge e : incident[u]) {
                if (color[e.v] == Color.BLACK) {
                    continue;
                }

                double wdt = C4*e.w + C5*(cf[u] + e.w); double jspt = (d[e.v] + d[u]) + ((d[e.v] + d[u])/(double)(s[e.v] + s[u]));

                if (wdt < wd[e.v] || (wdt == wd[e.v] && jspt >= jsp[e.v]) || (wd[e.v] == 0 && jsp[e.v] == 0)) {
                    wd[e.v] = wdt;
                    jsp[e.v] = jspt;
                    p[e.v] = u;
                    L.add(e.v);
                    color[e.v] = Color.GREY;
                }
            }
            color[u] = Color.BLACK;
            spanned_vertices++;
            if (u != p[u]) {
                T.add(new Edge(u, p[u], adjacency[u][p[u]]).standard());
            }
        }

        return new Solution(this, T);
    }

    enum Color {
        WHITE, GREY, BLACK;
    }

    public void save(String file) throws IOException {
        BufferedWriter writer = new BufferedWriter( new FileWriter(file));
        writer.write(toString());
        writer.close();
    }

    public HashSet<Integer> requiredEdges() {
        HashSet<Integer> result = new HashSet<>();
        for(int v = 0; v < n; v++) { //possibly min-cuts could be used.
            if (incident[v].size() == 1) {
                Edge e = incident[v].first();
                result.add(e.v);
            }
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
                        //prev[i][j] = k;
                    }
                }
            }
        }

        return distance;
    }
}
