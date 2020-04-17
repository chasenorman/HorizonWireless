import java.io.*;
import java.util.HashSet;
import java.util.TreeSet;

public class Graph {
    public static final boolean DEBUG = true;

    public static final int INF = 100000000;

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
        edges.add(e.standard());
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

        for (Edge e : edges) {
            d[e.u]++; d[e.v]++;
            s[e.u] += e.w; s[e.v] += e.w;
            m[e.u] = Math.max(m[e.u], e.w);
            m[e.v] = Math.max(m[e.v], e.w);
        }

        int[] w = new int[n];
        int[] cf = new int[n];
        double[] sp = new double[n];
        double spm = Double.NEGATIVE_INFINITY;
        int f = 0;

        for (int i = 0; i < n; i++) {
            w[i] = INF;
            cf[i] = INF;
            sp[i] = 0.2*d[i] + 0.6*(d[i]/(double)s[i]) + (0.2/m[i]);
            if (sp[i] > spm) {
                f = i;
                spm = sp[i];
            }
        }

        int[] p = new int[n];

        w[f] = 0;
        cf[f] = 0;
        p[f] = -1;

        HashSet<Integer> L = new HashSet<>();
        L.add(f);

        HashSet<Integer> S = new HashSet<>();

        double[] wd = new double[n];
        double[] jsp = new double[n];
        int u = -1;

        while (!L.isEmpty()) {
            Edge add = null;
            double wdm = Double.POSITIVE_INFINITY;
            double jspm = 0;
            for (int i : L) {
                if (wd[i] < wdm) {
                    S.clear();
                    S.add(i);
                    wdm = wd[i];
                } else if (wd[i] == wdm) {
                    S.add(i);
                }
            }

            for (int i : S) {
                if (jsp[i] >= jspm) {
                    jspm = jsp[i];
                    u = i;
                }
            }
            for (Edge e : incident[u]) {
                int a = e.v;
                if (!T.contains(e.standard())) {
                    System.out.println(p[u]);
                    double wdt = 0.85*e.w + 0.15*(cf[u] + e.w);
                    double jspt = d[a]+d[u] + ((d[a]+d[u])/(double)(s[a]+s[u]));
                    if (wdt < wd[a]) {
                        wd[a] = wdt;
                        jsp[a] = jspt;
                        p[a] = u;
                        add = e;
                    } else if (wdt == wd[a] && jspt >= jsp[a]) {
                        jsp[a] = jspt;
                        p[a] = u;
                        add = e;
                    }
                    L.add(a);
                }
            }
            if (add != null) {
                T.add(add.standard());
            }
        }

        System.out.println(Solution.isValid(this, T));
        return new Solution(this, T);
    }

    public void save(String file) throws IOException {
        BufferedWriter writer = new BufferedWriter( new FileWriter(file));
        writer.write(toString());
        writer.close();
    }
}
