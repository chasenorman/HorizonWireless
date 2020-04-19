import java.util.*;

public class SolutionSet implements BranchBound {
    static class Node implements Iterable<Edge> {
        public static Node NULL = new Node();

        Edge e;
        Node n;
        int size = 0;

        public Node(Edge e, Node n) {
            this.e = e;
            this.n = n;
            size = n.size + 1;
        }

        private Node() {}

        public TreeSet<Edge> edges() {
            TreeSet<Edge> result = new TreeSet<>();
            Node n = this;
            while (n != NULL) {
                result.add(n.e);
                n = n.n;
            }
            return result;
        }

        public Iterator<Edge> iterator() {
            Node t = this;
            return new Iterator<Edge>() {
                Node current = t;

                @Override
                public boolean hasNext() {
                    return current != NULL;
                }

                @Override
                public Edge next() {
                    Edge result = current.e;
                    current = current.n;
                    return result;
                }
            };
        }
    }

    Graph G;
    Node edge = Node.NULL;
    HashSet<Edge> forbidden;
    HashSet<Integer> must = new HashSet<>();
    int cost;
    int maxsize = 0;

    public SolutionSet(Graph G) {
        this.G = G;
        forbidden = new HashSet<>();

        for(int v = 0; v < G.n; v++) { //possibly min-cuts could be used.
            if (G.incident[v].size() == 1) {
                Edge e = G.incident[v].first();
                must.add(e.v);
            }
        }
    }

    private SolutionSet() { }

    private static SolutionSet next(Graph G, Node edge, HashSet<Integer> prevMust) {
        SolutionSet s = new SolutionSet();

        s.G = G;
        s.edge = edge;

        s.must = new HashSet<>(prevMust);
        s.must.add(edge.e.u);
        s.must.add(edge.e.v);

        // Floyd-Warshall to find distance array
        int[][] distance = new int[G.n][G.n];
        for (int x = 0; x < G.n; x++) {
            for (int y = 0; y < G.n; y++) {
                distance[x][y] = x == y ? 0 : Graph.INF;
            }
        }
        for (Edge e : edge) {
            distance[e.u][e.v] = e.w;
            distance[e.v][e.u] = e.w;
        }
        for (Edge e : s.remaining()) {
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

        Object[] arr = s.must.toArray();
        for (int i = 0; i < arr.length; i++) { // this is inefficient.
            for (int j = i+1; j < arr.length; j++) {
                if (distance[(Integer)arr[i]][(Integer)arr[j]] == Graph.INF) {
                    return null;
                }
                s.cost += 2*distance[(Integer)arr[i]][(Integer)arr[j]];
            }
        }

        s.forbidden = new HashSet<>();

        UnionFind u = new UnionFind(G.n);
        for (Edge e : edge) {
            assert u.find(e.u) != u.find(e.v);
            u.union(e.u, e.v);
        }
        for (Edge e : G.edges) {
            if (u.find(e.u) == u.find(e.v)) {
                s.forbidden.add(e);
            }
        }

        SortedSet<Edge> f = G.edges.headSet(edge.e);
        s.forbidden.addAll(f);
        for (Edge e : edge) {
            s.forbidden.remove(e);
        }

        int v = s.must.iterator().next();
        HashSet<Integer> possible = new HashSet<>();
        for (int i = 0; i < G.n; i++) {
            if (distance[v][i] != Graph.INF) {
                possible.add(i);
            }
        }
        s.maxsize = possible.size();

        Outer: for(int i = 0; i < G.n; i++) {
            if (!possible.contains(i)) {
                for (Edge e : G.incident[i]) {
                    if (possible.contains(e.v)) {
                        continue Outer;
                    }
                }
                return null;
            }
        }

        return s;
    }

    @Override
    public List<BranchBound> branch() {
        ArrayList<BranchBound> result = new ArrayList<>();
        if (isValidSolution()) {
            result.add(new Solution(G, edge.edges()));
        }


        SortedSet<Edge> remaining = new TreeSet<>(remaining());
        remaining.removeAll(forbidden); // some other conditions may be required here.


        for (Edge e : remaining) {
            Node n = new Node(e, edge);

            SolutionSet s = next(G, n, must);
            if (s != null) {
                result.add(s);
            }
        }
        return result;
    }

    private boolean isValidSolution() {
        HashSet<Integer> vertices = new HashSet<>();
        for (Edge e : edge) {
            vertices.add(e.u);
            vertices.add(e.v);
        }

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

        UnionFind u = new UnionFind(G.n);
        for (Edge e : edge) {
            u.union(e.u, e.v);
        }

        int test = u.find(edge.e.u);
        for (Edge e : edge) {
            if (u.find(e.u) != test) {
                return false;
            }
        }
        return true;
    }


    public SortedSet<Edge> remaining() {
        if (edge == Node.NULL) {
            return G.edges;
        }
        return G.edges.tailSet(edge.e,false);
    }

    public double bound() {
        return cost/(double)(maxsize * (maxsize-1));
    }

    @Override
    public boolean isSolution() {
        return false;
    }

    @Override
    public double heuristicCost() {
        return bound();
    }

    public String toString() {
        return edge.edges().toString();
    }
}
