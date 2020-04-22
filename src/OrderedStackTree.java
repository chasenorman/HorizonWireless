import java.util.*;

public class OrderedStackTree<E extends WeakOrder> {

    /* Public Namespace */
    public OrderedStackTree() {
        _root = new OSTNode<>();
        _head = new Stack<>();
        _boughs = new ArrayList<>();
        _branchNext = 0;
        _head.add(_root);
        _boughs.add(_head);
    }

    /** Checks if the ost is empty. */
    public boolean isEmpty() {
        return _head.peek().isEmpty();
    }

    /** Peeks the top of _head, which will have an element unless the stacktree is empty.
     * @return The top item at the top of head.
     */
    public E peek() {
        return _head.peek().peek();
    }

    /** Removes and returns the top item from the top of head. If this empties the top of head, it will
     * find a new head, removing empty leaves.
     *
     *  This is expensive if you empty your stack, so don't do that too often.
     * @return The top item at the top of head.
     */
    public E pop() {
        E item = _head.peek().pop();
        if (_head.peek().isEmpty()) {
            _findNewHead();
        }
        return item;
    }

    /** Pushes to _head at whatever order the item is.
     * @param item The item to push
     * @return The item you pushed.
     */
    public E push(E item) {
        int order = item.order();
        if (_branchNext > 0) {
            _branch();
        }
        _climbTo(order);
        return _head.elementAt(order).push(item);
    }

    /** The next time you push, it'll branch the tree so that there's a new node of order order.
     * @param order > 0; The order at which the new StackTreeNode should be.
     */
    public void branch(int order) {
        _branchNext = order;
    }

    /** Switches _head to be a different, older path to leaf nodes. These are not guarnateed to be
     * unique, the number of them will never decrease. Hence, boughNum can be at maximum the number
     * of times you've called branch().
     * @param boughNum >= 0; which bough you want to switch to.
     */
    public void toBough(int boughNum) {
        _head = _boughs.get(boughNum);
        _findNewHead();
    }

    /* Private Namespace */
    /** A node in an OrderedStackTree, extends Stack. */
    static private class OSTNode<E extends Object & WeakOrder> extends Stack<E> {
        public OSTNode() {
            super();
            _children = new Stack<>();
        }

        /** Makes this node have a child, keeping track of its sibling relationship. */
        public OSTNode<E> haveChild() {
            OSTNode<E> child = new OSTNode<>();
            if (hasChildren()) {
                child.olderSibling = youngestChild();
            }
            _children.push(child);
            return child;
        }

        public boolean hasChildren() {
            return _children.size() != 0;
        }

        public OSTNode<E> youngestChild() {
            return _children.peek();
        }

        public OSTNode<E> oldestChild() {
            return _children.elementAt(0);
        }

        public boolean removeChild(OSTNode<E> child) {
            return _children.remove(child);
        }

        public OSTNode<E> olderSibling;

        private Stack<OSTNode<E>> _children;
    }

    /** Makes the node at the top of _head have a child, adding it to _head. */
    private void _newLeaf() {
        OSTNode<E> child = _head.peek().haveChild();
        _head.push(child);
    }

    /** Branches _head, assuming that something is going to get pushed immediately and
     * that _branchNext > 0. Sets _branchNext back to 0. */
    private void _branch() {
        Stack<OSTNode<E>> oldHead = _head;
        _head = new Stack<OSTNode<E>>();
        _boughs.add(_head);
        for (int index = 0; index < _branchNext; index++) {
            _head.push(oldHead.elementAt(index));
        }
        _newLeaf();
        _branchNext = 0;
    }

    /** Adds to _head the oldest (leftmost) child of the top of _head, creating one if it does't exist. */
    private void _climbHead() {
        if (_head.peek().hasChildren()) {
            _head.push(_head.peek().oldestChild());
        } else {
            _newLeaf();
        }
    }

    /** Climbs head until it's of size _order or more.
     * @param order _head will be at least this size afterwards.
     */
    private void _climbTo(int order) {
        while(order >= _head.size()) {
            _climbHead();
        }
    }

    /** Climbs as far up _head as it can. */
    private void _climbToTop() {
        while(_head.peek().hasChildren()) {
            _climbHead();
        }
    }

    /** Finds a new head, removing empty leaf stacks. Gurantees that _head.peek() is either
     * nonempty or root.
     *
     * This might cause some boughs to overlap. Oh well.
     * */
    private void _findNewHead() {
        while(_head.peek().isEmpty() && _head.peek() != _root) {
            OSTNode<E> top = _head.pop();
            _head.peek().removeChild(top);
            _climbToTop();
        }
        _climbToTop();
    }

    /** The unique node of order 0. */
    private OSTNode<E> _root;
    /** A stack representing the 'active branch'. */
    private Stack<OSTNode<E>> _head;
    /** The collection of paths to the leaf nodes. */
    private ArrayList<Stack<OSTNode<E>>> _boughs;
    /** > 0 if, during the next push, it should make a new node of order _branchNext. */
    private int _branchNext;
}