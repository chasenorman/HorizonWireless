import java.util.*;

public class SolutionSet2 implements BranchBound {
    Graph G;
    HashSet<Edge> edges; // current edges.
    HashSet<Edge> cycles; // edges which create cycles with current edges.
    HashSet<Edge> skipped; // edges which have been skipped.
    TreeSet<Edge> adjacent; // viable edges for next iteration
    HashSet<Integer> vertices;
    HashSet<Edge> requiredEdges;
    HashSet<Integer> requiredVertices;
    HashSet<Edge> possibleEdges;
    public static int[][] distance;
    long cost;
    long heuristicCost = -1;
    int maxSize;

    public SolutionSet2(Graph G, int start, Collection<Edge> skipped) { // start is articulation or skipped is already populated.
        this.G = G;
        this.edges = new HashSet<>();
        this.cycles = new HashSet<>();
        this.adjacent = new TreeSet<>(G.incidentStandard[start]);
        adjacent.removeAll(skipped);
        this.vertices = new HashSet<>();
        vertices.add(start);
        this.skipped = new HashSet<>(skipped);

        requiredEdgesInSubgraph();
        requiredNodesInSubgraph();
        possibleEdgesInSubgraph();

        Object[] s = skippable(skipped);
        if (!(Boolean)s[0]) {
            throw new IllegalArgumentException();
        }

        this.maxSize = (Integer)s[1];
        distance = G.distance();
        computeCost();
    }

    private SolutionSet2(SolutionSet2 prev, Collection<Edge> skipped, Edge next, int maxSize) {
        this.G = prev.G;
        this.edges = new HashSet<>(prev.edges);
        edges.add(next);
        this.cycles = new HashSet<>(prev.cycles);
        this.vertices = new HashSet<>(prev.vertices);
        this.maxSize = maxSize;

        int node;
        if (vertices.contains(next.u)) {
            vertices.add(next.v);
            node = next.v;
        } else {
            vertices.add(next.u);
            node = next.u;
        }

        for (Edge e : G.incident[node]) {
            if (vertices.contains(e.v) && !e.standard().equals(next)) {
                cycles.add(e.standard());
            }
        }

        this.skipped = new HashSet<>(prev.skipped);
        this.skipped.addAll(skipped);

        this.adjacent = new TreeSet<>(prev.adjacent);
        this.adjacent.addAll(G.incidentStandard[node]);
        this.adjacent.removeAll(cycles);
        this.adjacent.removeAll(skipped);
        adjacent.remove(next);

        requiredEdgesInSubgraph();
        requiredNodesInSubgraph();
        possibleEdgesInSubgraph();
        computeCost();
    }

    public void requiredNodesInSubgraph() {
        requiredVertices = new HashSet<>(vertices);

        for(int i = 0; i < G.n; i++) {
            HashSet<Edge> incident = new HashSet<>(G.incidentStandard[i]);
            incident.removeAll(cycles);
            incident.removeAll(skipped);
            if (incident.size() == 1) {
                Edge e = incident.iterator().next();
                if (e.v == i) {
                    requiredVertices.add(e.u);
                } else {
                    requiredVertices.add(e.v);
                }
            }
        }
    }

    public void requiredEdgesInSubgraph() {
        requiredEdges = edges;
    }

    @Override
    public List<BranchBound> branch() {
        Main.start();
        for (Edge e : requiredEdges) {
            if (adjacent.contains(e)) {
                Main.end();
                return new SolutionSet2(this, Collections.emptySet(), e, maxSize).branch();
            }
        }

        List<BranchBound> result = new ArrayList<>();
        if (isValid()) {
            result.add(new Solution(G, edges, vertices));
        }

        HashSet<Edge> skipped = new HashSet<>();
        for (Edge e : adjacent) {
            Object[] s = skippable(skipped);
            if(!(Boolean)s[0]) {
                break;
            }
            result.add(new SolutionSet2(this, skipped, e, (Integer)s[1]));
            skipped.add(e);
        }
        Main.end();
        return result;
    }

    @Override
    public double bound() {
        return cost/((double)maxSize*(maxSize-1));
    }

    @Override
    public boolean isSolution() {
        return false;
    }

    @Override
    public double heuristicCost() {
        if (heuristicCost == -1) {
            UnionFind u = new UnionFind(G.n);
            Graph T = new Graph(G.n);

            for (Edge e : edges) {
                assert u.find(e.u) != u.find(e.v);
                u.union(e.u, e.v);
                T.add(e.u, e.v, e.w);
            }

            PriorityQueue<Edge> possible = new PriorityQueue<>(Collections.reverseOrder()); // change compareTo.
            possible.addAll(possibleEdges);
            possible.removeAll(edges);

            for (Edge e : possible) { // has some extra edges.
                if (u.find(e.u) != u.find(e.v)) {
                    u.union(e.u, e.v);
                    T.add(e.u, e.v, e.w);
                }
            }

            boolean[] marked = new boolean[G.n];
            heuristicDFS(T, vertices.iterator().next(), marked);
        }
        return 2*heuristicCost/(double)((maxSize)*(maxSize-1));
    }

    private int heuristicDFS(Graph T, int v, boolean[] marked) {
        int sum = 0;
        marked[v] = true;
        for (Edge e : T.incident[v]) {
            if (!marked[e.v]) {
                int edge = heuristicDFS(T, e.v, marked) + 1;
                heuristicCost += edge*(maxSize - edge)*e.w;
                sum += edge;
            }
        }
        return sum;
    }

    private boolean isValid() {
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
        return true;
    }

    private void possibleEdgesInSubgraph() {
        possibleEdges = new HashSet<>(G.edges);
        possibleEdges.removeAll(this.skipped);
        possibleEdges.removeAll(cycles); // not actually required.
    }

    private Object[] skippable(Collection<Edge> skipped) {
        HashSet<Edge> possible = new HashSet<>(possibleEdges);
        possible.removeAll(skipped);

        UnionFind u = new UnionFind(G.n);
        for (Edge e : possible) {
            u.union(e.u, e.v);
        }

        int cc = u.find(vertices.iterator().next());
        int maxsize = 0;

        Outer: for(int i = 0; i < G.n; i++) {
            if (u.find(i) != cc) {
                for (Edge e : G.incident[i]) {
                    if (u.find(e.v) == cc) {
                        continue Outer;
                    }
                }
                return new Object[]{false};
            } else {
                maxsize++;
            }
        }
        return new Object[]{true, maxsize};
    }

    private void computeCost() {
        Graph T = new Graph(G.n); // inefficient graph creation.
        for (Edge e : edges) {
            T.add(e.u, e.v, e.w);
        }
        boolean[] marked = new boolean[G.n];
        DFS(T, vertices.iterator().next(), marked);
        cost *= 2;

        HashSet<Integer> other = new HashSet<>(requiredVertices);
        other.removeAll(vertices);
        for (int i : vertices) {
            for (int j : other) {
                cost += 2*distance[i][j];
            }
        }
        for (int i : other) {
            for (int j : other) {
                cost += distance[i][j];
            }
        }
    }

    private int DFS(Graph T, int v, boolean[] marked) {
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

    public String toString() {
        return edges.toString();
    }

    public int hashCode() {
        return edges.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof SolutionSet2) {
            SolutionSet2 s = (SolutionSet2) o;
            return s.edges.equals(edges);
        }
        return false;
    }
}
