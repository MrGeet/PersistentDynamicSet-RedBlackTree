package Trees;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("ALL")
public class BalancedPersistentDynamicSet<E> extends BinarySearchTree<E>
{
    protected static final Color RED = Color.red;
    protected static final Color BLACK = Color.black;
    protected Node nil;

    private ArrayList<Integer> numNodes; //number of nodes per version
    private int numVersions;
    private MainPanel mainPanel;
    protected ArrayList<Node> treeVersions;
    //stores the affected nodes in the set
    private HashSet<Node> hookedSet;
    //stores the path of the affected nodes, to keep
    //track of parent nodes
    private ArrayList<Node> hookedNodes;
    private Node addedNode;

    public static class BlackHeightException extends RuntimeException
    {
    }

    protected class Node<E> extends BinarySearchTree.BinaryTreeNode
    {
        protected Node leftChild;
        protected Node rightChild;

        /**
         * The node's color, either RED or BLACK.
         */
        protected Color color;

        /**
         * Initializes a node with the data, makes other pointers nil,
         * and makes the node red.
         *
         * @param element Data to save in the node.
         */
        public Node(E element)
        {
            super(element);
            this.color = RED;
            this.leftChild = nil;
            this.rightChild = nil;
        }

        /**
         * Returns the <code>data</code> instance variable of this
         * node and this node's color as a <code>String</code>.
         */
//        public String toString()
//        {
//            return super.toString() + ", " + (color == RED ? "red" : "black");
//        }
        public String toString(int depth)
        {
            String result = "";

            if (leftChild != nil)
                result += leftChild.toString(depth + 1);

            for (int i = 0; i < depth; i++)
                result += "  ";

            result += toString() + "\n";

            if (rightChild != nil)
                result += rightChild.toString(depth + 1);

            return result;
        }

        public Node cloneNode()
        {
            Node result = new Node(element);

            result.color = color;
            return result;
        }
    }

    protected void setNil(Node node)
    {
        node.color = BLACK;
        nil = node;
        nil.leftChild = nil;
        nil.rightChild = nil;
        nil.element = null;
    }

    public BalancedPersistentDynamicSet()
    {
        setNil(new Node(null));
        rootNode = nil;
        hookedSet = new HashSet();
        hookedNodes = new ArrayList<>();
        treeVersions = new ArrayList<Node>();
        treeVersions.add((Node) rootNode);
        numNodes = new ArrayList<>();
        numNodes.add(0);
        hookedNodes = new ArrayList<>();
//        drawPanel = new DrawPanel();
    }

    private void initialzeDrawPanel(Node node)
    {
        mainPanel = new MainPanel(node);
    }

    protected Node findParent(Node z)
    {
        Node parent = (Node) rootNode;

        if (z == nil)
        {
            return nil;
        }

        if (compare((E) z.element, rootNode.element) == 0)
        {
            return nil;
        }

        while (true)
        {
            if ((parent.rightChild != nil && compare((E) z.element, (E) parent.rightChild.element) == 0) ||
                    (parent.leftChild != nil && compare((E) z.element, (E) parent.leftChild.element) == 0))
            {
                break;
            }

            if (compare((E) z.element, (E) parent.element) < 0)
            {
                parent = (Node) parent.leftChild;
            } else
            {
                parent = (Node) parent.rightChild;
            }
        }

        return parent;
    }

    protected void leftRotate(Node x)
    {
        Node y = (Node) x.rightChild;

        // Swap the in-between subtree from y to x.
        x.rightChild = y.leftChild;
        if (x.rightChild != nil)
        {
            hookedSet.add(x.rightChild);
        }
        Node parent = findParent(x);

        // If x is the root of the entire tree, make y the root.
        if (x == rootNode)
            rootNode = y;
        else if (x == parent.leftChild)
            parent.leftChild = y;
        else
            parent.rightChild = y;

        // Relink x and y.
        y.leftChild = x;
    }

    protected void rightRotate(Node x)
    {
        Node y = (Node) x.leftChild;

        // Swap the in-between subtree from y to x.
        x.leftChild = y.rightChild;
        if (x.leftChild != nil)
        {
            hookedSet.add(x.leftChild);
        }
        Node parent = (Node) findParent(x);

        // If x is the root of the entire tree, make y the root.
        if (x == rootNode)
            rootNode = y;
        else if (parent.leftChild == x)
            parent.leftChild = y;
        else
            parent.rightChild = y;
        // Relink x and y.
        y.rightChild = x;
    }

    @Override
    public boolean add(E o)
    {
        if (!withinView(o))
            throw new IllegalArgumentException("Outside view");
        Node newNode = new Node(o);
        boolean added = false;
        if (rootNode == nil || rootNode == null)
        {
            rootNode = newNode;
            hook(rootNode);
            added = true;
            addedNode = newNode;
        } else
        {  // find where to add newNode
            Node currentNode = (Node) rootNode;
            boolean done = false;
            while (!done)
            {
                hook(currentNode);
                int comparison = compare(o, (E) currentNode.element);
                if (comparison < 0) // newNode is less than currentNode
                {
                    if (currentNode.leftChild == nil)
                    {  // add newNode as leftChild
                        currentNode.leftChild = newNode;
                        done = true;
                        added = true;
                    } else
                        currentNode = currentNode.leftChild;
                } else if (comparison > 0)//newNode is greater than currentNode
                {
                    if (currentNode.rightChild == nil)
                    {  // add newNode as rightChild
                        currentNode.rightChild = newNode;
                        done = true;
                        added = true;
                    } else
                        currentNode = currentNode.rightChild;
                } else if (comparison == 0) // newNode equal to currentNode
                    done = true; // no duplicates in this binary tree impl.
            }
        }
        if (added)
        {
            numElements++;
            addedNode = newNode;
            if (rootNode != newNode)
                hook(newNode);
        }
        return added;
    }


    @Override
    protected void hook(BinaryTreeNode hookedNode)
    {
        hookedSet.add((Node) hookedNode);
        hookedNodes.add((Node)hookedNode);
    }

    /**
     * Creates a new version of a tree
     * @param node
     */
    protected void createNewVersion(Node node)
    {
        Node newTree = nil;
        Node newVersion = newTree;
        Node oldTree = treeVersions.get(numVersions);

        if (hookedSet.contains(node))
        {
            newTree = node.cloneNode();
            newVersion = newTree;
        }

        checkAffectedNodes(node, newTree);


        treeVersions.add(newVersion);
        hookedSet.clear();
        numVersions++;
        numNodes.add((numNodes.size() - 1) + 1);
    }

    /**
     *
     * @param node
     * @param newNode
     *
     * Builds a tree recursively if the newNode is in the affected set
     * then create a new node for it and if it's not connnect it to
     * nodes from a previous version of a tree
     */
    private void checkAffectedNodes(Node node, Node newNode)
    {
        if (hookedSet.contains(node.leftChild))
        {
            Node temp = node.leftChild;
            newNode.leftChild = node.leftChild.cloneNode();
            newNode.leftChild.leftChild = temp.leftChild;
            newNode.leftChild.rightChild = temp.rightChild;
        } else if (node.leftChild != nil)
        {
            newNode.leftChild = getNodeFromTree((E) node.leftChild.element, treeVersions.get(numVersions));
        }

        if (hookedSet.contains(node.rightChild))
        {
            Node temp = node.rightChild;
            newNode.rightChild = node.rightChild.cloneNode();
            newNode.rightChild.rightChild = temp.rightChild;
            newNode.rightChild.leftChild = temp.leftChild;
        } else if (node.rightChild != nil)
        {
            newNode.rightChild = getNodeFromTree((E) node.rightChild.element, treeVersions.get(numVersions));
        }

        if (node != nil)
        {
            checkAffectedNodes(node.leftChild, newNode.leftChild);
            checkAffectedNodes(node.rightChild, newNode.rightChild);
        }
    }

    private Node getNodeFromTree(E element, Node oldTree)
    {
        Node result = null;

        while (compare((E) oldTree.element, element) != 0)
        {
            int comparison = compare((E) oldTree.element, element);
            if (comparison > 0)
            {
                oldTree = oldTree.leftChild;
            } else
            {
                oldTree = oldTree.rightChild;
            }
        }

        result = oldTree;

        return result;
    }

    /*************************************
     *              INSERT
     ************************************/
    public void insert(E element)
    {
        treeInsert(element);
    }
    protected void treeInsert(E e)
    {
        add(e);

        insertFixup(addedNode);
        hookedNodes.clear();
        createNewVersion((Node) rootNode);
    }

    /**
     * @param z it's a Node that is about to be inserted
     *
     *       Insert fixup uses hookedNodes array list
     *       it stores the path to the added node
     *       That way I can keep track of parents
     *       I also have a hashset to see if the node has been affected
     */
    protected void insertFixup(Node z)
    {
        System.out.println(hookedNodes.toString());
        Node y = null;
//        Node parent = (Node) findParent(z);
//        Node grandParent = (Node) findParent(parent);
        Node parent = nil;
        Node grandParent = nil;
        if (hookedNodes.size()>1)
        {
            parent = hookedNodes.get(hookedNodes.size()-2);
        }

        if (hookedNodes.size()>2)
        {
            grandParent = hookedNodes.get(hookedNodes.size()-3);
        }

        while (parent.color == RED)
        {
            if (parent == grandParent.leftChild)
            {
                y = (Node) grandParent.rightChild;
                if (y.color == RED)
                {
                    parent.color = BLACK;
                    y.color = BLACK;
                    hookedSet.add(y);//y is affected so add to affected set

                    grandParent.color = RED;
                    z = grandParent;
                    /*Adding parent*/
//                    parent = (Node) findParent(z);
//                    grandParent = (Node) findParent(parent);
                    hookedNodes.remove(hookedNodes.size()-1);
                    hookedNodes.remove(hookedNodes.size()-1);

                    parent = nil;
                    grandParent = nil;
                    if (hookedNodes.size()>1)
                    {
                        parent = hookedNodes.get(hookedNodes.size()-2);
                    }

                    if (hookedNodes.size()>2)
                    {
                        grandParent = hookedNodes.get(hookedNodes.size()-3);
                    }
                } else
                {
                    if (z == parent.rightChild)
                    {
                        Node temp = z;
                        hookedNodes.set(hookedNodes.size()-2, z);
                        z = parent;
                        hookedNodes.set(hookedNodes.size()-1, z);

                        parent = temp;
//                        hookedNodes.remove(hookedNodes.size()-1);

//                        parent = (Node) findParent(z);
//                        grandParent = (Node) findParent(parent);

                        leftRotate(z);
                    }

                    parent.color = BLACK;
                    grandParent.color = RED;
                    rightRotate(grandParent);

                    hookedNodes.remove(grandParent);
                    grandParent = nil;
                    if (hookedNodes.size()>2)
                    grandParent = hookedNodes.get(hookedNodes.size()-3);
                }
            } else
            {
                y = (Node) grandParent.leftChild;

                if (y.color == RED)
                {
                    parent.color = BLACK;
                    y.color = BLACK;
                    //CHANGING COLOUR SO ADDING TO AFFECTED NODES
                    hookedSet.add(y);

                    grandParent.color = RED;
                    z = grandParent;

                    parent = nil;
                    grandParent = nil;
                    if (hookedNodes.size()>1)
                    {
                        parent = hookedNodes.get(hookedNodes.size()-2);
                    }

                    if (hookedNodes.size()>2)
                    {
                        grandParent = hookedNodes.get(hookedNodes.size()-3);
                    }
//                    parent = findParent(z);
//                    grandParent = findParent(parent);
                } else
                {
                    if (z == parent.leftChild)
                    {
                        Node temp = z;
                        hookedNodes.set(hookedNodes.size()-2, z);
                        z = parent;
                        hookedNodes.set(hookedNodes.size()-1, z);

                        parent = temp;

                        rightRotate(z);
                    }

                    parent.color = BLACK;
                    grandParent.color = RED;
                    leftRotate(grandParent);

                    hookedNodes.remove(grandParent);
                    grandParent = nil;
                    if (hookedNodes.size()>2)
                        grandParent = hookedNodes.get(hookedNodes.size()-2);
                }
            }
        }
        ((Node) rootNode).color = BLACK;
    }

    /*************************************
     *              DELETE
     ************************************/
    /**
     * Restores the red-black properties of the tree after a deletion.
     *
     * @param x Node at which there may be a violation.
     */
    protected void deleteFixup(Node x)
    {
        Node parent = findParent(x);
        if (x==nil)
        parent = hookedNodes.get(hookedNodes.size()-1);
//        Node grandParent = findParent(parent);

        while (x != rootNode && x.color == BLACK)
        {
            if (parent.leftChild == x)
            {
                Node w = (Node) parent.rightChild;

                if (w.color == RED)
                {
                    w.color = BLACK;
                    hookedSet.add(w);

                    parent.color = RED;
                    leftRotate(parent);

                    parent = findParent(x);
//                    grandParent = findParent(parent);
                    w = parent.rightChild;
                }

                if (w.leftChild.color == BLACK
                        && w.rightChild.color == BLACK)
                {
                    w.color = RED;
                    hookedSet.add(w);
                    x = parent;
                    parent = findParent(x);
//                    grandParent = findParent(parent);
                } else
                {
                    if (((Node) w.rightChild).color == BLACK)
                    {
                        ((Node) w.leftChild).color = BLACK;
                        w.color = RED;
                        hookedSet.add(w);
                        rightRotate(w);
//                        parent = findParent(x);
//                        grandParent = findParent(parent);
                        w = parent.rightChild;
                    }

                    w.color = parent.color;
                    parent.color = BLACK;
                    w.rightChild.color = BLACK;
                    hookedSet.add(w);
                    hookedSet.add(w.rightChild);
                    leftRotate(parent);
                    x = (Node) rootNode;
                    parent = nil;
                }
            } else
            {
                Node w = parent.leftChild;

                if (w.color == RED)
                {
                    w.color = BLACK;
                    hookedSet.add(w);

                    parent.color = RED;
                    rightRotate(parent);

                    parent = findParent(x);
                    w = parent.leftChild;
                }

                if ((w.rightChild).color == BLACK
                        && (w.leftChild).color == BLACK)
                {
                    w.color = RED;
                    hookedSet.add(w);

                    x = parent;
                    parent = findParent(parent);
                } else
                {
                    if ((w.leftChild).color == BLACK)
                    {
                        (w.rightChild).color = BLACK;
                        w.color = RED;
                        hookedSet.add(w.rightChild);
                        hookedSet.add(w);

                        leftRotate(w);
//                        parent = findParent(x);
                        w = parent.leftChild;
                    }

                    w.color = (parent).color;
                    hookedSet.add(w);

                    (parent).color = BLACK;
                    (w.leftChild).color = BLACK;
                    hookedSet.add(w.leftChild);

                    rightRotate(parent);
                    x = (Node) rootNode;
                    parent = nil;
                }
            }
        }
        x.color = BLACK;
        hookedNodes.clear();
    }

    //It's a hook method that checks if the removed case needs a fixup
    protected void removedNode(Node deletedNode, Node replacement)
    {
        Node delete = (Node)deletedNode;
        Node replace = (Node)replacement;
        System.out.println(hookedSet.toString());
//        System.out.println(hookedNodes.toString());
        if (replacement!=nil)
        hookedSet.add(replacement);
//        replacement.color = deletedNode.color;
        System.out.println(hookedNodes.toString());
//        Node parent = findParent(deletedNode);
        if (((Node) deletedNode).color==Color.BLACK )
        {
            deleteFixup(replace);
        }

        createNewVersion((Node)rootNode);
    }

    //Implementing a normal remove method
    public boolean remove(Object o)
    {
        boolean removed = false;
        E element = (E) o; // unchecked, could throw exception
        if (!withinView(element))
            throw new IllegalArgumentException("Outside view");
        if (rootNode != nil)
        {  // check if root to be removed
            if (compare(element, rootNode.element) == 0)
            {
//                hook(rootNode);
                removed = true;
                Node replacementNode = makeReplacement((Node)rootNode);
                rootNode = replacementNode;
                replacementNode.color = Color.BLACK;
//
                removedNode((Node)rootNode,replacementNode);
            }
            else
            {  // search for the element o
                Node parentNode = (Node) rootNode;
                Node removalNode;
                // determine whether to traverse to left or right of root
                if (compare(element, rootNode.element) < 0)
                    removalNode = parentNode.leftChild;
                else // compare(element, rootNode.element)>0
                    removalNode = parentNode.rightChild;

                while (removalNode != nil && !removed)
                {  // determine whether the removalNode has been found
                    int comparison = compare(element, (E)removalNode.element);
                    hook(parentNode);
                    if (comparison == 0)
                    {
                        Node replacementNode;
//                        hook(removalNode);
                        if (removalNode == parentNode.leftChild)
                        {
                            replacementNode
                                    = makeReplacement(removalNode);
                            parentNode.leftChild = replacementNode;
                        }
                        else // removalNode==parentNode.rightChild
                        {
                            replacementNode
                                    = makeReplacement(removalNode);
                            parentNode.rightChild = replacementNode;
                        }
//                        replacementNode.color = removalNode.color;

                        removedNode(removalNode,replacementNode);
                        removed = true;
                    } else // determine whether to traverse to left or right
                    {
                        parentNode = removalNode;
//                        hook(removalNode);
//                        System.out.println(removalNode + " + ");
                        if (comparison < 0)
                            removalNode = removalNode.leftChild;
                        else // comparison>0
                            removalNode = removalNode.rightChild;
                    }
                }
            }
        }
        if (removed)
        {
            numElements--;
            hookedSet.clear();
        }
        return removed;
    }

    private Node makeReplacement(Node removalNode)
    {
        Node replacementNode = nil;
        // check cases when removalNode has only one child
        if (removalNode.leftChild != nil && removalNode.rightChild == nil)
        {
            replacementNode = removalNode.leftChild;
            hook(replacementNode);
        }
        else if (removalNode.leftChild == nil && removalNode.rightChild != nil)
        {
            replacementNode = removalNode.rightChild;
            hook(replacementNode);
        }
            // check case when removalNode has two children
        else if (removalNode.leftChild != nil
                && removalNode.rightChild != nil)
        {  // find the inorder successor and use it as replacementNode
            Node parentNode = removalNode;
            replacementNode = removalNode.rightChild;
            hook(replacementNode);
            if (replacementNode.leftChild == nil)
            {
                // replacementNode can be pushed up one level to replace
                // removalNode, move the left child of removalNode to be
                // the left child of replacementNode
                replacementNode.leftChild = removalNode.leftChild;
//                hook(replacementNode);
            }
            else
            {  //find left-most descendant of right subtree of removalNode
                do
                {
                    parentNode = replacementNode;
                    replacementNode = replacementNode.leftChild;
                    hook(replacementNode);
                }
                while (replacementNode.leftChild != nil);
                // move the right child of replacementNode to be the left
                // child of the parent of replacementNode
                parentNode.leftChild = replacementNode.rightChild;
                // move the children of removalNode to be children of
                // replacementNode
                replacementNode.leftChild = removalNode.leftChild;
                replacementNode.rightChild = removalNode.rightChild;
            }
        }
        // else both leftChild and rightChild null so no replacementNode
        return replacementNode;
    }

    public Object successor(Object node)
    {
        Node x = (Node) node;

        if (x.rightChild != nil)
            return treeMinimum(x.rightChild);

        Node y = findParent(x);
        while (y != nil && x == y.rightChild)
        {
            x = y;
            y = findParent(y);
        }

        return y;
    }

    protected Object treeMinimum(Node x)
    {
        while (x.leftChild != nil)
            x = x.leftChild;

        return x;
    }

    public int blackHeight(Node z)
    {
        if (z == nil)
            return 0;

        int left = blackHeight((Node) z.leftChild);
        int right = blackHeight((Node) z.rightChild);
        if (left == right)
            if (z.color == BLACK)
                return left + 1;
            else
                return left;
        else
            throw new BlackHeightException();
    }

    public int blackHeight()
    {
        return blackHeight((Node) rootNode);
    }

    public String toString()
    {
        return rootNode.toString(0);
    }

    public static void main(String[] args)
    {
        BalancedPersistentDynamicSet<String> tree = new BalancedPersistentDynamicSet<String>();
        // build the tree

        tree.insert("ant");
        tree.insert("bat");

        tree.insert("cow");
        tree.insert("dog");
        tree.insert("fly");
        tree.insert("fox");
        tree.insert("cat");
        tree.insert("eel");

        tree.remove("cow");
        tree.remove("eel");

        tree.remove("fly");
        tree.remove("fox");
        tree.remove("ant");
        tree.remove("bat");
        tree.remove("cat");

        System.out.println(tree.rootNode.toString());
        JFrame frame = new JFrame("Balanced Persistent Dynamic Set");
        tree.initialzeDrawPanel(tree.treeVersions.get(tree.numVersions));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(tree.mainPanel);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        int screenHeight = dimension.height;
        int screenWidth = dimension.width;
        frame.pack();             //resize frame apropriately for its content
        //positions frame in center of screen
        frame.setLocation(new Point((screenWidth / 2) - (frame.getWidth() / 2),
                (screenHeight / 2) - (frame.getHeight() / 2)));
        frame.setVisible(true);
    }

    private class MainPanel extends JPanel
    {
        private DrawPanel drawPanel;
        private JComboBox<String> treesDropDown;

        private MainPanel(Node node)
        {
            super(new BorderLayout());
            super.setPreferredSize(new Dimension(700, 500));
            JPanel buttonPanel = new JPanel();

            treesDropDown = new JComboBox<>();
            updateDropDown();

            treesDropDown.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    int index = treesDropDown.getSelectedIndex();
                    drawPanel.changeNode(index);
                    drawPanel.setNumElements(numNodes.get(index));
                }
            });
//            drawPanel = new DrawPanel(node, numNodes.get(treesDropDown.getSelectedIndex()));
            drawPanel = new DrawPanel(node, numNodes.get(treesDropDown.getSelectedIndex()));

            treesDropDown.setSelectedIndex(numVersions);
            buttonPanel.add(new JLabel("Tree Version: "));
            buttonPanel.add(treesDropDown);

            super.add(drawPanel, BorderLayout.CENTER);
            super.add(buttonPanel, BorderLayout.SOUTH);
        }

        public void updateDropDown()
        {
            for (int i = 0; i <= numVersions; i++)
            {
                treesDropDown.addItem("Version: " + i);
            }
        }
    }

    private class DrawPanel extends JPanel
    {
        private Node localRootNode;
        private int numElements;

        /**
         * FIXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         *
         * @param node
         * @param numElements
         */
        public DrawPanel(Node node, int numElements)
        {
            super();
            localRootNode = node;
            this.numElements = numElements;
            super.setBackground(Color.WHITE);
            super.setPreferredSize(new Dimension(700, 500));
        }

        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            if (localRootNode != null)
            {
                drawTree(g, getWidth());
            }
        }

        public void drawTree(Graphics g, int width)
        {
            int nodeCount = 0 - (numElements / 2);
            drawNode(g, localRootNode, width / 2, 0, nodeCount, new HashMap<Node, Point>());
        }

        private int drawNode(Graphics g, Node current,
                             int x, int level, int nodeCount, Map<Node, Point> map)
        {
            int RECT = 40;

            if (current.leftChild != nil)
            {
                nodeCount = drawNode(g, current.leftChild, x, level + 1, nodeCount, map);
            }

            int currentX = x + nodeCount * RECT;
            int currentY = level * 2 * RECT + RECT;
            nodeCount++;
            map.put(current, new Point(currentX, currentY));

            if (current.rightChild != nil)
            {
                nodeCount = drawNode(g, current.rightChild, x, level + 1, nodeCount, map);
            }

            g.setColor(Color.black);
            if (current.leftChild != nil)
            {
                Point leftPoint = map.get(current.leftChild);
                g.drawLine(currentX, currentY, leftPoint.x, leftPoint.y - RECT / 2);
            }
            if (current.rightChild != nil)
            {
                Point rightPoint = map.get(current.rightChild);
                g.drawLine(currentX, currentY, rightPoint.x, rightPoint.y - RECT / 2);

            }
            if (current.color == Color.BLACK)
            {
                g.setColor(Color.LIGHT_GRAY);
            } else if (current.color == Color.RED)
            {
                g.setColor(Color.RED);
            }

            Point currentPoint = map.get(current);
            g.fillRect(currentPoint.x - RECT / 2, currentPoint.y - RECT / 2, RECT, RECT);
            g.setColor(Color.BLACK);
            g.drawRect(currentPoint.x - RECT / 2, currentPoint.y - RECT / 2, RECT, RECT);
            Font f = new Font("courier new", Font.BOLD, 16);
            g.setFont(f);
            int tempWidth = g.getFontMetrics().stringWidth(current.toString());
            g.drawString(current.toString(), currentPoint.x - tempWidth / 2, currentPoint.y);
            return nodeCount;
        }

        private void changeNode(int index)
        {
            localRootNode = treeVersions.get(index);
            this.repaint();
        }

        public void setNumElements(int numElements)
        {
            this.numElements = numElements;
        }
    }
}
