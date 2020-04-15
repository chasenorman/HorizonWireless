import java.util.*;

public class SolutionSet implements BranchBound {
    Graph G;
    int[][] distance;
    TreeSet<Edge> edges;
    HashSet<Edge> forbidden;
    HashSet<Integer> must = new HashSet<>();
    int cost;

    public SolutionSet(Graph G) {
        this.G = G;
        edges = new TreeSet<>();
        forbidden = new HashSet<>();

        distance = new int[G.n][G.n];

        for(int v = 0; v < G.n; v++) { //possibly min-cuts could be used.
            if (G.incident[v].size() == 1) {
                Edge e = G.incident[v].first();
                must.add(e.v);
            }
        }
    }

    private SolutionSet(Graph G, TreeSet<Edge> edges, HashSet<Integer> prevMust) throws IllegalArgumentException {
        this.G = G;
        this.edges = edges;

        must = new HashSet<>(prevMust);
        must.add(edges.last().u);
        must.add(edges.last().v);

        forbidden = new HashSet<>();

        UnionFind u = new UnionFind(G.n);
        for (Edge e : edges) {
            assert u.find(e.u) != u.find(e.v);
            u.union(e.u, e.v);
        }
        for (Edge e : G.edges) {
            if (!edges.contains(e) && u.find(e.u) == u.find(e.v)) {
                forbidden.add(e);
            }
        }


        // Floyd-Warshall to find distance array
        distance = new int[G.n][G.n];
        for (int x = 0; x < G.n; x++) {
            for (int y = 0; y < G.n; y++) {
                distance[x][y] = x == y ? 0 : Graph.INF;
            }
        }
        for (Edge e : edges) {
            distance[e.u][e.v] = e.w;
            distance[e.v][e.u] = e.w;
        }
        for (Edge e : remaining()) {
            distance[e.u][e.v] = e.w;
            distance[e.v][e.u] = e.w;
        }


        for (int z = 0; z < G.n; z++) {
            for (int x = 0; x < G.n; x++) {
                for (int y = 0; y < G.n; y++) { // can this be improved for undirected?
                    if (distance[x][y] > distance[x][z] + distance[z][y]) {
                        distance[x][y] = distance[x][z] + distance[z][y];
                    }
                }
            }
        }

        for (int i : must) { // this is inefficient.
            for (int j : must) {
                if (distance[i][j] == Graph.INF) {
                    throw new IllegalArgumentException(); // more of these must be made.
                }
                cost += distance[i][j];
            }
        }
    }

    @Override
    public List<BranchBound> branch() {
        ArrayList<BranchBound> result = new ArrayList<>();
        if (Solution.isValid(G, edges)) {
            result.add(new Solution(G, edges));
        }

        for (Edge e : remaining()) {
            if (!forbidden.contains(e)) { // some other conditions may be required here.
                TreeSet<Edge> edges1 = new TreeSet<>(edges);
                edges1.add(e);
                try {
                    SolutionSet s = new SolutionSet(G, edges1, must);
                    result.add(s);
                } catch (IllegalArgumentException ignored) {

                }
            }
        }

        return result;
    }

    public SortedSet<Edge> remaining() {
        if (edges.isEmpty()) {
            return G.edges;
        }
        Edge e = G.edges.higher(edges.last());
        if (e == null) {
            return Collections.emptySortedSet();
        }
        return G.edges.tailSet(e);
    }

    public double bound() {
        HashSet<Integer> possible = new HashSet<>(must);
        for (Edge e : remaining()) {
            possible.add(e.u);
            possible.add(e.v);
        }

        return cost/(double)(possible.size() * (possible.size()-1));
    }

    @Override
    public boolean isSolution() {
        return false;
    }

    @Override
    public Solution heuristic() {
        return null;
    }

    public String toString() {
        return edges.toString();
    }
}
