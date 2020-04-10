public class Edge implements Comparable<Edge> {
    int u;
    int v;
    int w; // default weight times 1000. Doesnt affect the solution to the problem

    public Edge(int u, int v, int w) {
        this.u = u;
        this.v = v;
        this.w = w;
    }

    public int compareTo(Edge e) {
        if(w != e.w){
            return w - e.w;
        } else if (v != e.v) {
            return v - e.v;
        } else {
            return u - e.u;
        }
    }

    public Edge reversed() {
        return new Edge(v, u, w);
    }

    public String toString() {
        return u + " " + v + " " + w/(float)1000;
    }
}