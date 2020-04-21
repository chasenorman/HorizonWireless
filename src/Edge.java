import java.util.Random;

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

    public static Edge random(int n) {
        Random r = new Random();
        return new Edge(r.nextInt(n), r.nextInt(n), r.nextInt(100000));
    }

    public Edge standard() {
        return u > v ? reversed() : this;
    }

    public boolean equals(Object o) {
        if (o instanceof Edge) {
            Edge e = (Edge)o;
            return (e.v==v && e.u == u) || (e.v==u && e.u == v);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return u*101 + v;
    }
}