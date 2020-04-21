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

    int nextMaxSize = -1;
    HashSet<Integer> nextRequired;


    public SolutionSet(Graph G) {
        this.G = G;
        maxSize = G.n; // G is connected.
        sorted = new Edge[G.edges.size()];
        G.edges.toArray(sorted);
        G.setArticulationPoints();
        required = new HashSet<>(G.articulationPoints);
        Arrays.sort(sorted, G::selectionOrder);
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

        for (int i = 0; i < required.length; i++) {
            for (int j = i+1; j < required.length; j++) {
                cost += distance[required[i]][required[j]];
            }
        }
        cost *= 2;
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

        nextMaxSize = 0;
        Outer: for (int i = 0; i < G.n; i++) {
            if (u.find(i) == cc) { // i is plausible
                nextMaxSize++;
            } else {
                int found = 0;
                int add = 0;
                for (Edge e : G.incident[i]) {
                    if (u.find(e.v) == cc) {
                        if (found == 1) {
                            continue Outer;
                        }
                        add = e.v;
                        found++;
                    }
                }
                if (found == 1) {
                    nextRequired.add(add); // add must be in the final graph.
                    continue;
                }
                return false;
            }
        }

        return true;
    }

    public String toString() {
        return edges.toString();
    }
}
