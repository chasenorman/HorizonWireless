import java.io.IOException;
import java.util.*;

public interface BranchBound {
    List<BranchBound> branch();

    double bound();

    double size();
}
