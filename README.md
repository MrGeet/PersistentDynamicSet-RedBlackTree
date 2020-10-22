# PersistentDynamicSet-RedBlackTree
This project contains 3 data structures.
1) Normal Binary Search Tree.
2) Persistent Dynamic Set:
It is a BST which creates new tree versions as the tree gets updated. It keeps information about the old trees as well. If you copy the entire tree the complexity would O(n), however this data structure creates makes the complexity O(log2n), because it only copies the updated (affected) nodes and keeps the old one untouched and uncopied.
3) Balanced Persistent Dynamic Set
This data structure is the same as second one, except it auto balances every. So it is a Red Black Tree which keeps track ov previous versions.

You will be able to find 3 main classes inside the project. Each main 
class corresponds to one of the data structures mentioned above.
If you run you can see a GUI example of a the tree.

The nodes have been hard-coded, in later versions I will possibly make it dynamic.
