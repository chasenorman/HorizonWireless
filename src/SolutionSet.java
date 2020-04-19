import java.util.*;

public class SolutionSet implements BranchBound {
    Node<Edge> edges = new Node<>();
    Node<Edge> skipped = new Node<>(); // there will eventually be duplicates in skipped and cycles.
    Node<Integer> vertices = new Node<>();
    Graph G;
    int nextIndex = 0;
    Edge[] sorted;
    HashSet<Integer> articulationPoints;

    long cost = -1;
    int maxSize;

    int nextMaxSize = -1;


    public SolutionSet(Graph G) {
        this.G = G;
        maxSize = G.n; // G is connected.
        sorted = new Edge[G.edges.size()];
        G.edges.toArray(sorted);
        G.setArticulationPoints();
        articulationPoints = new HashSet<>(G.articulationPoints);
        Arrays.sort(sorted, SolutionSet::selectionOrder);
    }

    private SolutionSet(SolutionSet prev, Node<Edge> edges, Node<Edge> skipped, int nextIndex, int maxSize) {
        this.edges = edges;
        this.skipped = skipped;
        vertices = prev.vertices;
        this.articulationPoints = new HashSet<>(prev.articulationPoints);

        boolean u = false, v = false;
        for (int i : vertices) {
            if (i == edges.last.u) {
                u = true;
            } else if (i == edges.last.v) {
                v = true;
            }
        }

        if (!u) {
            vertices = new Node<>(edges.last.u, vertices);
            articulationPoints.remove(edges.last.u);
        }
        if (!v) {
            vertices = new Node<>(edges.last.v, vertices);
            articulationPoints.remove(edges.last.v);
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
                SolutionSet s = new SolutionSet(this, nextEdges, nextSkipped, i + 1, nextMaxSize);
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
                        //prev[i][j] = k;
                    }
                }
            }
        }

        for (Node<Integer> i = vertices; i.size != 0; i = i.prev) {
            for (Node<Integer> j = i.prev; j.size != 0; j = j.prev) {
                if (distance[i.last][j.last] == Graph.INF) {
                    throw new IllegalArgumentException();
                }
                cost += 2*distance[i.last][j.last];
            }
        }

        Integer[] required = articulationPoints.toArray(new Integer[0]);

        for (int i = 0; i < required.length; i++) {
            for (int j = i+1; j < required.length; j++) {
                if (distance[required[i]][required[j]] == Graph.INF) {
                    throw new IllegalArgumentException();
                }
                cost += 2*distance[required[i]][required[j]];
            }
        }

        for (int i : vertices) {
            for (int j : required) {
                if (distance[i][j] == Graph.INF) {
                    throw new IllegalArgumentException();
                }
                cost += 2*distance[i][j];
            }
        }
        // TODO: One-of-isms? Required nodes arent articulation.
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

        for (int i : vertices) {
            if (u.find(i) != cc) {
                return false;
            }
        }

        nextMaxSize = 0;
        Outer: for (int i = 0; i < G.n; i++) {
            if (u.find(i) == cc) {
                nextMaxSize++;
            } else {
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

    private static int selectionOrder(Edge e1, Edge e2) {
        return (e2.w - e1.w);
    }

    public String toString() {
        return edges.toString();
    }
}
