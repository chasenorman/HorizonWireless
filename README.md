Our solution applied the Branch and Bound method. 

# Project Structure, Class by Class.

## Graph

This class represents the input graphs. It keeeps an adjacency matrix, a list of edges sorted by edge weight, and a two dimensional list of all edges incident on any given node. This makes all graph computations maximally efficient. It also has several methods for computing MST, SPT, a random graph, etc. 

The Edge class represents an Edge. Each edge has a standard ordering (u, v).

## Solution

This class represents a solution to the graph problem. It contains a list of edges and nodes. It can check its validity via the verify function. It's cost can be computed via the bound function. It then can be saved with the save function.

The naive way to calculate mean pairwise distance is to use the O(n^2) solution. However, this can be computed in O(n) time using a DFS pass. Each edge will contribute to the total sum as many times as it is used for a path between nodes on either side. By multiplying the number of nodes previously seen behind an edge with the number ahead of the edge we can calculate a multiplier which can be added to the total cost. 

## SolutionSet

Solution set represents a set of solutions a la branch and bound. Solutions are built by continually adding edges. All solutions (except those with only one node) are uniquely defined by their edges. In order to avoid two solution sets containing the same solution, the sets blacklist any edges they decide not to include.

The bound method computes the a lower bound on the possible cost of any solution in the set. It does so by using Floyd-Warshall to compute the shortest path between all points required to be in this solution set (that is, points incident on edges or required to keep the graph connected). It adds all of these up and divides by the number of nodes that could plausibly be included in the graph (that is, points which would be connected if all remaining edges were to be included). This achieves a very accurate bound.

The branch function splits this set into subsets. It decides which edge to choose next. For each edge it decides not to include, it adds it to the "skipped" list. It will add all possible next edges as new solution sets. The canSkipTo function helps this function know whether valid solutions will exist in the newly created sets.

The heuristic method generates a heuristic Solution that is likely to be good. While running the bound operation, the center of the graph is computed automatically. The center is defined as the point with smallest maximum distance to any plausible node. The heuristic operation then runs Dijkstra's algorithm to compute a SPT. We chose the SPT because we found research which stated that there always exists a node from which the Mean Pairwise Distance of the SPT from that node is at most twice the optimal. This was much preffered from the poor bounds of MST.

## Solver

The solver decides which branches and bounds to compute. We experimented with many orderings. While the default for branch and bound is a stack, we found it was better to compute them in order of how many edges were selected, followed by their heuristic's bound. This effectively searches the most promising branches as we build a tree. We continually push to this set of SolutionSets, called "todo" and pull new jobs from it. It has a maximum capacity LIMIT for memory concerns. If a solution set's bound is greater than our current best solution, we don't add it, since it could not possibly generate a more optimal solution. Solvers are created to run in parallel for all questions.

## Helper Classes

UnionFind is used many times throughout the algorithms in these classes. It helps with discovering connected components efficiently. We used the exact definition from class.

Node is a LinkedList structure designed as a space and time saving mechanism. When SolutionSets create their children sets, their edges merely point to their parents edges, as they are immutable. This makes iteration, polling, and element addition more efficient.

OrderedStackTree is an attempt at having the Solver make more audacious choices, much like the "temperature" from annealing procedures. It does so by reverting progress to more ancestral nodes every so often. This did not work for us.

## Opimization

The branch and bound method is fairly restrictive in its operation, so optimizations came from three places.
1.  Ordering which edges are considered by the SolutionSet first
2.  Ordering which SolutionSets are to be explored first
3.  Deciding how to leverage Heuristic solutions

I decided to order the edges roughly based on whether or not they are in the current best solution, their edge weight, and the degree of each of the adjacent vertices, along with some randomness. I played around with these parameters.

Each time we explore a solution set we check its heuristic to see if it is a better solution. If so we save it. Thus we save better and better solutions as time goes on.

## Why?

This algorithm is great because it continually generates solutions, checks sensible paths, iterates on perviously found solutions, and checks plausible options at a blistering pace. This allowed us to solve hundreds of the small problems with guaranteed optimality in the first few minutes. 

## What resources?

Only my home computer.

## What other approaches did you try?

We only attempted branch and bounds. There were 3 iterations. We tried building a tree out from a center point which was much worse because it didn't weed out enough solutions quickly. The other iteration was a much slower version of what we currently have in place. Other changes can be found in the Optimizations section.
