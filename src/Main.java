import java.io.IOException;
import java.io.File;

public class Main {
    public static long start = System.currentTimeMillis();
    public static long temp;
    public static long inside = 0;

    public static final String INPUT_DIR = "input/";
    public static final String OUTPUT_DIR = "output/";


    public static void main(String[] args) throws IOException {
        //Graph.random(20, 2).save("input/50.in");
        run();
    }

    public static void run() throws IOException {
        for (String file : new File(INPUT_DIR).list()) {
            String output = file.replace(".in", ".out");
            new Solver(INPUT_DIR + file, OUTPUT_DIR + output).start();
        }
    }

    public static void start() {
        temp = System.currentTimeMillis();
    }

    public static void stop() {
        inside += System.currentTimeMillis()-temp;
    }

    public static double percent() {
        long total = System.currentTimeMillis() - start;
        start = System.currentTimeMillis();
        double result = inside/(double)total;
        inside = 0;
        return result;
    }
}
