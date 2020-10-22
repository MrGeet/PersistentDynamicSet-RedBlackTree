package Trees;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PersistentDynamicSet<E> extends BinarySearchTree<E>
{
    //    private DrawPanel drawPanel;
    private int numVersions;
    private ArrayList<Integer> numNodes; //number of nodes per version
    private MainPanel mainPanel;
    //    protected BinaryTreeNode rootNode;
//    private Comparator<? super E> comparator;//null for natural ordering
//    private E fromElement, toElement; // bounds for visible view of tree
    protected ArrayList<BinaryTreeNode> treeVersions;
    protected ArrayList<BinaryTreeNode> hookedNodes;

    public PersistentDynamicSet()
    {
        super();
        numVersions = 0;
        rootNode = null;
//        comparator = null;
//        fromElement = null;
//        toElement = null;
        treeVersions = new ArrayList<BinaryTreeNode>();
        treeVersions.add(rootNode);
        numNodes = new ArrayList<>();
        numNodes.add(0);
        hookedNodes = new ArrayList<>();
    }

    private void initialzeDrawPanel(BinaryTreeNode node)
    {
        mainPanel = new MainPanel(node);
    }

    /**
     * adds an affected node to the array
     * @param hookedNode
     */
    @Override
    protected void hook(BinaryTreeNode hookedNode)
    {
//        super.hook(hookedNode);
        hookedNodes.add(hookedNode);
    }

    /**
     * creates new nodes for all the affected nodes
     * and connects to all the nodes from a previous tree
     */
    protected void stopAddHook()
    {
//        System.out.println(treeVersions.toString());
        System.out.println(hookedNodes.toString());
        if (treeVersions.get(numVersions) == null)
        {
            treeVersions.add(hookedNodes.get(0).cloneNode());
            numVersions++;
            numNodes.add((numNodes.size() - 1) + 1);
            hookedNodes.clear();
            return;
        }

        BinaryTreeNode newTree = new BinaryTreeNode(hookedNodes.get(0).cloneNode().element);
//        System.out.println(numVersions);
//        System.out.println(treeVersions.size());
        BinaryTreeNode oldTree = treeVersions.get(numVersions);
//        System.out.println(oldTree);

        BinaryTreeNode currentNode = newTree;
        for (int i = 1; i < hookedNodes.size(); i++)
        {
            BinaryTreeNode hookedNode = hookedNodes.get(i).cloneNode();

            System.out.println(oldTree);
            int comparison = compare(hookedNode.element, currentNode.element);
            if (comparison < 0)
            {
                currentNode.leftChild = hookedNode;
                currentNode.rightChild = oldTree.rightChild;

                currentNode = currentNode.leftChild;
                oldTree = oldTree.leftChild;
            } else
            {
                currentNode.rightChild = hookedNode;
                currentNode.leftChild = oldTree.leftChild;

                currentNode = currentNode.rightChild;
                oldTree = oldTree.rightChild;
            }
        }
//        System.out.println();
        treeVersions.add(newTree);
        hookedNodes.clear();
        numVersions++;
        numNodes.add((numNodes.size() - 1) + 1);
    }

    /**
     * creates new nodes for all the affected nodes
     * and connects to all the nodes from a previous tree
     */
    public void stopRemoveHook()
    {
        System.out.println(hookedNodes.toString());

        BinaryTreeNode newTree = hookedNodes.get(hookedNodes.size()-1);
        BinaryTreeNode oldTree = treeVersions.get(numVersions);

        if (newTree==null)
        {
            treeVersions.add(null);
            hookedNodes.clear();
            numVersions++;
            numNodes.add(numNodes.get(numNodes.size() - 1) - 1);

            return;
        }

        HashSet<E> elements = new HashSet<>();
        for (int i = 0;i<hookedNodes.size()-1;i++)
        {
            elements.add(hookedNodes.get(i).element);
        }

        BinaryTreeNode newRoot = newTree.cloneNode();
        BinaryTreeNode currentNode = newRoot;
        int temp;

        if (newTree!=null &&
                compare(newTree.element,hookedNodes.get(0).element)==0)
            temp = 1;
        else
            temp = 0;

        BinaryTreeNode replacement = null;

        for (int i = temp;i<hookedNodes.size()-1;i++)
        {
            BinaryTreeNode hookedNode = hookedNodes.get(i).cloneNode();

            if (newTree.leftChild != null &&
                    compare(newTree.leftChild.element, hookedNode.element) == 0)
            {
                currentNode.leftChild = hookedNode;
                currentNode.rightChild = oldTree.rightChild;

                currentNode = currentNode.leftChild;
                newTree = newTree.leftChild;
                oldTree = oldTree.leftChild;
            } else if (newTree.rightChild != null &&
                    compare(newTree.rightChild.element, hookedNode.element) == 0)
            {
                currentNode.rightChild = hookedNode;
                currentNode.leftChild = oldTree.leftChild;

                currentNode = currentNode.rightChild;
                newTree = newTree.rightChild;
                oldTree = oldTree.rightChild;
            }
        }

        while (currentNode!=null)
        {
            boolean flag = true;
            if(newTree.leftChild!=null &&
            elements.contains(newTree.leftChild.element))
            {
                currentNode.leftChild = newTree.leftChild.cloneNode();
                currentNode = currentNode.leftChild;
                newTree = newTree.leftChild;
                oldTree = oldTree.leftChild;
                flag = false;
            }
            else if (newTree.leftChild!=null){
                currentNode.leftChild = getNodeFromTree(newTree.leftChild.element, oldTree);
            }

            if(newTree.rightChild!=null &&
                    elements.contains(newTree.rightChild.element))
            {
                currentNode.rightChild = newTree.rightChild.cloneNode();
                currentNode = currentNode.rightChild;
                newTree = newTree.rightChild;
                oldTree = oldTree.rightChild;
                flag = false;
            }
            else if (newTree.rightChild!=null){
                currentNode.rightChild = getNodeFromTree(newTree.rightChild.element, oldTree);
            }

            if (flag) break;

        }

        treeVersions.add(newRoot);
        hookedNodes.clear();
        numVersions++;
        numNodes.add(numNodes.get(numNodes.size() - 1) - 1);
    }

    /**
     * @param element
     * @param oldTree
     * @return
     *
     * Retrieve a node from an old tree
     */
    private BinaryTreeNode getNodeFromTree(E element, BinaryTreeNode oldTree)
    {
        BinaryTreeNode result = null;

        while(compare(oldTree.element,element)!=0)
        {
            int comparison = compare(oldTree.element,element);
            if (comparison>0)
            {
                oldTree = oldTree.leftChild;
            }
            else
            {
                oldTree = oldTree.rightChild;
            }
        }

        result = oldTree;

        return result;
    }

    public static void main(String[] args)
    {  // create the binary search tree
        PersistentDynamicSet<String> tree = new PersistentDynamicSet<String>();
        // build the tree
        tree.add("cow");
        tree.add("fly");
        tree.add("dog");
        tree.add("bat");
        tree.add("fox");
        tree.add("pig");

        tree.add("cat");
        tree.add("eel");
        tree.add("ant");
        tree.add("foc");
        tree.add("dad");
        System.out.println("" + tree.numElements);
        System.out.println("Original Tree: " + tree);
//        tree.remove("owl");
//        tree.remove("fly");
        tree.remove("cow");
        System.out.println("" + tree.numElements);

        tree.remove("foc");

//
        tree.remove("bat");
        tree.remove("eel");
        tree.remove("fly");
        tree.remove("pig");
        tree.remove("fox");
        tree.remove("dog");
        tree.remove("ant");
        tree.remove("dad");

        tree.remove("cat");

        JFrame frame = new JFrame("Binary Search Tree");
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


    /**
     * GUI PART
     */
    private class MainPanel extends JPanel
    {
        private DrawPanel drawPanel;
        private JComboBox<String> treesDropDown;

        private MainPanel(BinaryTreeNode node)
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
            drawPanel = new DrawPanel(node, numNodes.get(treesDropDown.getSelectedIndex()));

            treesDropDown.setSelectedIndex(numVersions);
            buttonPanel.add(new JLabel("Tree Version: "));
            buttonPanel.add(treesDropDown);
//            buttonPanel.add(prevVersion);
//            buttonPanel.add(nextVersion);

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
        private BinaryTreeNode localRootNode;
        private int numElements;

        /**
         * FIXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
         *
         * @param node
         * @param numElements
         */
        public DrawPanel(BinaryTreeNode node, int numElements)
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
            drawNode(g, localRootNode, width / 2, 0, nodeCount, new HashMap<BinaryTreeNode, Point>());
        }

        private int drawNode(Graphics g, BinaryTreeNode current,
                             int x, int level, int nodeCount, Map<BinaryTreeNode, Point> map)
        {
            int RECT = 40;

            if (current.leftChild != null)
            {
                nodeCount = drawNode(g, current.leftChild, x, level + 1, nodeCount, map);
            }

            int currentX = x + nodeCount * RECT;
            int currentY = level * 2 * RECT + RECT;
            nodeCount++;
            map.put(current, new Point(currentX, currentY));

            if (current.rightChild != null)
            {
                nodeCount = drawNode(g, current.rightChild, x, level + 1, nodeCount, map);
            }

            g.setColor(Color.black);
            if (current.leftChild != null)
            {
                Point leftPoint = map.get(current.leftChild);
                g.drawLine(currentX, currentY, leftPoint.x, leftPoint.y - RECT / 2);
            }
            if (current.rightChild != null)
            {
                Point rightPoint = map.get(current.rightChild);
                g.drawLine(currentX, currentY, rightPoint.x, rightPoint.y - RECT / 2);

            }
//            if (current instanceof BoolOperandNode) {
//                g.setColor(Color.WHITE);
//            } else {
            g.setColor(Color.YELLOW);
//            }

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
