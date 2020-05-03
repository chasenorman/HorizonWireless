import java.io.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Main {
    public static long start = System.currentTimeMillis();
    public static long temp;
    public static long inside = 0;
    public static int seed = (int)System.currentTimeMillis();

    public static final String INPUT_DIR = "inputs/";
    public static final String OUTPUT_DIR = "outputs/";
    public static final String OPT_DIR = "OPT/";
    public static boolean PRUNE = true;

    public static final int PROCESSES = 1;
    public static final int PROCESS = 0;

    static String[] compete = {"small-99","medium-31","medium-100","medium-115","medium-218","large-31","large-38","large-39","large-42","large-55","large-79","large-89","large-97","large-117","large-139","large-141","large-143","large-146","large-154","large-161","large-169","large-188","large-202","large-211","large-233","large-239","large-258","large-274","large-279","large-285","large-321","large-334","large-337","large-339","large-340","large-342","large-361","large-370","large-372","large-374","large-376","large-380","large-385","large-386","large-387","large-388","large-391","large-392","large-394","large-396","large-399","large-400"};

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            run();
        } else if (args[0].equals("settle")) {
            settle();
        } else if (args[0].equals("all")) {
            PRUNE = false;
            run();
        } else if (args[0].equals("recover")) {
            recover();
        } else if (args[0].equals("check")) {
            check();
        } else if (args[0].equals("replace")) {
            replace();
        } else {
            System.out.println("unknown args.");
        }
    }

    public static void check() throws IOException {
        for (String file : new File(INPUT_DIR).list()) {
            String output = file.replace(".in", ".out");
            if (file.startsWith(".")) {
                continue;
            }
            Graph G = Graph.from(INPUT_DIR + file);
            Solution current = Solution.from(G,OUTPUT_DIR + output);
            if (!current.verify(G)) {
                System.out.println(file + " FAILED CHECK");
            }
        }
    }

    public static void settle() throws IOException {
        List<String> only = Arrays.asList(compete);
        for (String file : new File(INPUT_DIR).list()) {
            String output = file.replace(".in", ".out");
            if (file.startsWith(".")) {
                continue;
            }
            Graph G = Graph.from(INPUT_DIR + file);
            Solution current = Solution.from(G,OUTPUT_DIR + output);
            Solution attempt = current.settle(G);
            if (attempt.bound() < current.bound()) {
                attempt.save(OUTPUT_DIR + output);
                System.out.println(file + " " + (attempt.bound()/1000));
            }
        }
    }

    public static void replace() throws IOException {
        List<String> only = Arrays.asList(compete);
        for (String file : new File(INPUT_DIR).list()) {
            String output = file.replace(".in", ".out");
            if (file.startsWith(".")) {
                continue;
            }
            Graph G = Graph.from(INPUT_DIR + file);
            Solution current = Solution.from(G,OUTPUT_DIR + output);
            Solution attempt = current.replace(G);
            if (attempt.bound() < current.bound()) {
                attempt.save(OUTPUT_DIR + output);
                System.out.println(file + " " + (attempt.bound()/1000));
            }
        }
    }

    public static void cheese() throws IOException {
        for (String file : new File(INPUT_DIR).list()) {
            if (file.startsWith(".")) {
                continue;
            }

            Graph G = Graph.from(INPUT_DIR + file);
            Solution s = G.cheese();
            if (s != null) {
                s.save(OUTPUT_DIR + file);
                System.out.println(file + " cheesed!");
            }
        }
    }

    public static void run() throws IOException {
        List<String> only = Arrays.asList(compete);
        for (String file : new File(INPUT_DIR).list()) {
            String output = file.replace(".in", ".out");
            if (file.startsWith(".") || file.hashCode() % PROCESSES != PROCESS) {
                continue;
            }
            if (PRUNE && !only.contains(output.replace(".out",""))) {
                continue;
            }
            Graph G = Graph.from(INPUT_DIR + file);
            Solution s = G.cheese();
            if (s != null) {
                s.save(OUTPUT_DIR + file);
                System.out.println(file + " cheesed!");
                continue;
            }
            new Solver(INPUT_DIR + file, OUTPUT_DIR + output, OPT_DIR + output).start();
        }
    }

    public static void recover() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("submission.json"));
        String[] tokens = br.readLine().split("\"");
        for (int i = 1; i < tokens.length; i+=4) {
            String output = tokens[i].replace(".in", ".out");
            String text = tokens[i+2].replace("\\n", "\n");
            BufferedWriter writer = new BufferedWriter( new FileWriter("save/" + output));
            writer.write(text);
            writer.flush();
            writer.close();
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
