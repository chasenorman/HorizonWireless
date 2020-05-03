import java.util.*;

public class SolutionSet implements BranchBound {
    Node<Edge> edges = new Node<>();
    Node<Edge> skipped = new Node<>();
    Node<Integer> vertices = new Node<>();
    Graph G;
    int nextIndex = 0;
    Edge[] sorted;
    HashSet<Integer> required;

    long cost = -1;
    int maxSize;
    int center = -1;

    int nextMaxSize = -1;
    HashSet<Integer> nextRequired;

    Solution heuristic;


    public SolutionSet(Graph G, Solver S) {
        this.G = G;
        maxSize = G.n; // G is connected.
        sorted = new Edge[G.edges.size()];
        G.edges.toArray(sorted);
        this.center = G.center();
        G.setArticulationPoints();
        required = new HashSet<>(G.articulationPoints);
        Arrays.sort(sorted, S::selectionOrder);
    }

    private SolutionSet(SolutionSet prev, Node<Edge> edges, Node<Edge> skipped, int nextIndex, int maxSize, HashSet<Integer> required) {
        this.edges = edges;
        this.skipped = skipped;
        vertices = prev.vertices;
        this.required = required;

        boolean u = false, v = false;
        for (int i : vertices) {
            if (i == edges.last.u) {
                u = true;
                if (v) {
                    break;
                }
            } else if (i == edges.last.v) {
                v = true;
                if (u) {
                    break;
                }
            }
        }

        if (!u) {
            vertices = new Node<>(edges.last.u, vertices);
        }
        if (!v) {
            vertices = new Node<>(edges.last.v, vertices);
        }

        G = prev.G;
        sorted = prev.sorted;

        this.nextIndex = nextIndex;
        this.maxSize = maxSize;
    }

    public double size() {
        if (nextIndex > 30) {
            return 0;
        }
        return 1./(1<<nextIndex); // presumes around half of trees will include each particular edge.
    }

    @Override
    public List<BranchBound> branch() {
        List<BranchBound> result = new ArrayList<>();

        Solution solution = new Solution(edges, vertices, G.n);
        if (solution.verifyPartial(G)) {
            result.add(solution);
        }

        UnionFind u = new UnionFind(G.n);
        for (Edge e : edges) {
            u.union(e.u, e.v);
        }

        Node<Edge> nextSkipped = skipped;
        for (int i = nextIndex; i < sorted.length; i++) {
            if (u.find(sorted[i].v) != u.find(sorted[i].u) && canSkipTo(i)) {
                Node<Edge> nextEdges = new Node<>(sorted[i], edges);
                SolutionSet s = new SolutionSet(this, nextEdges, nextSkipped, i + 1, nextMaxSize, nextRequired);
                result.add(s);
                nextSkipped = new Node<>(sorted[i], nextSkipped);
            }
        }
        return result;
    }

    @Override
    public double bound() {
        if (cost == -1) {
            computeCost();
        }
        return cost/(double)(maxSize*(maxSize-1));
    }

    public void computeCost() {
        cost = 0;

        int[][] distance = new int[G.n][G.n];
        for (int x = 0; x < G.n; x++) {
            for (int y = 0; y < G.n; y++) {
                distance[x][y] = x == y ? 0 : Graph.INF;
            }
        }
        for (Edge e : edges) {
            distance[e.u][e.v] = e.w;
            distance[e.v][e.u] = e.w;
        }
        for (int i = nextIndex; i < sorted.length; i++) {
            Edge e = sorted[i];
            distance[e.u][e.v] = e.w;
            distance[e.v][e.u] = e.w;
        }

        //https://cs.stackexchange.com/questions/26344/floyd-warshall-algorithm-on-undirected-graph
        for(int k = 0; k < G.n; k++) {
            for(int i = 0; i < G.n; i++) {
                int distki = (k<i)?distance[k][i]:distance[i][k];
                if(k == i || distki == Graph.INF) {
                    continue;
                }

                for(int j = i+1; j < G.n; j++) {
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

        Integer[] required = this.required.toArray(new Integer[0]);
        int[] longest = new int[G.n];


        for (int i = 0; i < required.length; i++) {
            for (int j = i+1; j < required.length; j++) {
                if (distance[required[i]][required[j]] > longest[required[i]]) {
                    longest[required[i]] = distance[required[i]][required[j]];
                }
                if (distance[required[i]][required[j]] > longest[required[j]]) {
                    longest[required[j]] = distance[required[i]][required[j]];
                }
                cost += distance[required[i]][required[j]];
            }
        }
        cost *= 2;

        if (required.length != 0) {
            center = required[0];
            for (int r : required) {
                if(longest[r] < longest[center]) {
                    center = r;
                }
            }
        }
    }

    private boolean canSkipTo(int index) {
        UnionFind u = new UnionFind(G.n);

        for (Edge e : edges) {
            u.union(e.u, e.v);
        }
        for (int i = index; i < sorted.length; i++) {
            u.union(sorted[i].u, sorted[i].v);
        }

        int cc = u.find(sorted[index].v);

        nextRequired = new HashSet<>();

        for (int i : vertices) {
            if (u.find(i) != cc) {
                return false;
            }
            nextRequired.add(i);
        }

        nextRequired.add(sorted[index].u);
        nextRequired.add(sorted[index].v);

        nextMaxSize = 0;
        Outer: for (int i = 0; i < G.n; i++) {
            if (u.find(i) == cc) { // i is plausible
                nextMaxSize++;
            } else {
                boolean found = false;
                int add = 0;
                for (Edge e : G.incident[i]) {
                    if (u.find(e.v) == cc) {
                        if (found) {
                            continue Outer;
                        }
                        add = e.v;
                        found = true;
                    }
                }
                if (found) {
                    nextRequired.add(add); // add must be in the final graph. TODO paths to these nodes?
                    continue;
                }
                return false;
            }
        }

        return true;
    }

    public Solution heuristic() {
        if (heuristic != null) {
            return heuristic;
        }

        if (edges.size == 0) {
            return G.shortestPathTree(center);
        }

        if (center == -1) {
            computeCost();
        }

        int[] prev = new int[G.n];
        int[] dist = new int[G.n];
        Node<Edge>[] incident = new Node[G.n];
        for (int i = 0; i < G.n; i++) {
            incident[i] = new Node<>();
        }

        UnionFind uf = new UnionFind(G.n);
        for (Edge e : edges) {
            incident[e.u] = new Node<>(e, incident[e.u]);
            incident[e.v] = new Node<>(e.reversed(), incident[e.v]);
            uf.union(e.u, e.v);
        }
        for (int i = nextIndex; i < sorted.length; i++) {
            Edge e = sorted[i];
            if (uf.find(e.u) != uf.find(e.v)) {
                incident[e.u] = new Node<>(e, incident[e.u]);
                incident[e.v] = new Node<>(e.reversed(), incident[e.v]);
            }
        }

        PriorityQueue<Integer> q = new PriorityQueue<>(Comparator.comparingInt(o -> dist[o]));

        for (int i = 0; i < G.n; i++) {
            if (i != center) {
                dist[i] = Graph.INF;
            }
            q.add(center);
        }

        while (!q.isEmpty()) {
            int u = q.poll();
            for (Edge e : incident[u]) {
                int temp = dist[u] + e.w;
                if (temp < dist[e.v]) {
                    dist[e.v] = temp;
                    prev[e.v] = u;
                    q.remove(e.v);
                    q.add(e.v);
                }
            }
        }

        Node<Edge> edges = new Node<>();

        for (int i = 0; i < G.n; i++) {
            if (i != center && dist[i] != Graph.INF) {
                edges = new Node<>(new Edge(i, prev[i], G.adjacency[i][prev[i]]).standard(), edges);
            }
        }

        //could compute vertices
        Solution result = new Solution(edges, G.n).settle(G).replace(G);

        if (!result.verify(G)) {
            throw new IllegalArgumentException();
        }

        heuristic = result;
        return heuristic;
    }

    public String toString() {
        return edges.toString();
    }

    @Override
    public int order() {
        return edges.size;
    }
}
