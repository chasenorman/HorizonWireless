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

    static String[] compete = {"small-99","small-126","small-253","small-301","medium-11","medium-15","medium-18","medium-28","medium-31","medium-37","medium-38","medium-39","medium-45","medium-49","medium-55","medium-66","medium-67","medium-75","medium-77","medium-78","medium-80","medium-82","medium-89","medium-95","medium-99","medium-104","medium-106","medium-118","medium-119","medium-133","medium-136","medium-141","medium-143","medium-150","medium-154","medium-155","medium-156","medium-166","medium-177","medium-179","medium-183","medium-188","medium-195","medium-199","medium-202","medium-215","medium-222","medium-226","medium-230","medium-232","medium-233","medium-237","medium-246","medium-247","medium-270","medium-271","medium-274","medium-278","medium-279","medium-285","medium-287","medium-288","medium-294","medium-295","medium-301","large-3","large-4","large-8","large-11","large-12","large-14","large-15","large-16","large-17","large-18","large-21","large-23","large-27","large-28","large-30","large-31","large-34","large-37","large-39","large-42","large-43","large-45","large-47","large-49","large-51","large-52","large-55","large-57","large-58","large-63","large-64","large-65","large-66","large-77","large-78","large-79","large-80","large-82","large-86","large-89","large-93","large-95","large-96","large-97","large-99","large-101","large-104","large-106","large-113","large-114","large-117","large-118","large-119","large-120","large-121","large-124","large-129","large-130","large-133","large-135","large-136","large-137","large-139","large-141","large-143","large-145","large-150","large-154","large-155","large-156","large-157","large-160","large-161","large-163","large-166","large-168","large-169","large-172","large-173","large-174","large-176","large-178","large-180","large-181","large-183","large-185","large-186","large-187","large-188","large-195","large-198","large-200","large-201","large-202","large-205","large-211","large-214","large-215","large-217","large-218","large-220","large-226","large-228","large-233","large-237","large-238","large-239","large-242","large-246","large-253","large-254","large-256","large-258","large-264","large-266","large-268","large-270","large-274","large-275","large-278","large-279","large-285","large-287","large-288","large-291","large-294","large-295","large-297","large-299","large-301","large-304","large-306","large-307","large-308","large-309","large-310","large-311","large-312","large-313","large-314","large-315","large-316","large-317","large-318","large-319","large-320","large-321","large-322","large-323","large-324","large-325","large-326","large-328","large-329","large-330","large-331","large-332","large-333","large-334","large-335","large-336","large-337","large-338","large-339","large-340","large-341","large-342","large-343","large-344","large-346","large-348","large-349","large-350","large-351","large-352","large-353","large-354","large-355","large-356","large-358","large-360","large-361","large-363","large-364","large-365","large-366","large-367","large-368","large-371","large-372","large-373","large-374","large-375","large-376","large-377","large-378","large-379","large-380","large-381","large-382","large-383","large-384","large-385","large-386","large-387","large-388","large-389","large-390","large-391","large-392","large-393","large-394","large-395","large-396","large-397","large-398","large-399","large-400"};
    static String[] failure = {"medium-31","medium-38","medium-66","medium-67","medium-80","medium-99","medium-118","medium-202","medium-226","medium-237","medium-279","large-11","large-37","large-47","large-49","large-66","large-77","large-79","large-80","large-97","large-117","large-120","large-124","large-137","large-155","large-202","large-211","large-258","large-287","large-295","large-299","large-306","large-320","large-326","large-349","large-352","large-353","large-358","large-361","large-364","large-366","large-367","large-373","large-382","large-388","large-394","large-400"};


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
        List<String> failure = Arrays.asList(Main.failure);
        for (String file : new File(INPUT_DIR).list()) {
            String output = file.replace(".in", ".out");
            if (PRUNE && (new File(OPT_DIR + output).exists() || !only.contains(output.replace(".out","")))) {
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
