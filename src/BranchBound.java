import java.io.IOException;
import java.util.*;

public interface BranchBound extends WeakOrder {
    List<BranchBound> branch();

    double bound();

    double size();

    Solution heuristic();
}
