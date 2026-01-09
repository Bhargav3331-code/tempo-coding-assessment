class HierarchyFilter {
    public static Hierarchy filter(Hierarchy hierarchy, java.util.function.IntPredicate nodeIdPredicate) {
        int n = hierarchy.size();

        // Temporary storage (max possible size = n)
        int[] tmpIds = new int[n];
        int[] tmpDepths = new int[n];

        // ancestorAllowed[d] == whether the node at depth d is included
        boolean[] ancestorAllowed = new boolean[n + 1];

        int outSize = 0;

        for (int i = 0; i < n; i++) {
            int id = hierarchy.nodeId(i);
            int depth = hierarchy.depth(i);

            boolean parentAllowed = depth == 0 || ancestorAllowed[depth - 1];
            boolean allowed = parentAllowed && nodeIdPredicate.test(id);

            ancestorAllowed[depth] = allowed;

            if (allowed) {
                tmpIds[outSize] = id;

                // Compute new depth = number of allowed ancestors
                int newDepth = 0;
                for (int d = 0; d < depth; d++) {
                    if (ancestorAllowed[d]) {
                        newDepth++;
                    }
                }

                tmpDepths[outSize] = newDepth;
                outSize++;
            }
        }

        // Copy into correctly sized arrays
        int[] nodeIds = new int[outSize];
        int[] depths = new int[outSize];
        System.arraycopy(tmpIds, 0, nodeIds, 0, outSize);
        System.arraycopy(tmpDepths, 0, depths, 0, outSize);

        return new ArrayBasedHierarchy(nodeIds, depths);
    }


  class FilterTest {

    @Test
    void testFilter_givenExample() {
        Hierarchy unfiltered = new ArrayBasedHierarchy(
            new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
            new int[]{0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2}
        );

        Hierarchy filteredActual =
            HierarchyFilter.filter(unfiltered, nodeId -> nodeId % 3 != 0);

        Hierarchy filteredExpected = new ArrayBasedHierarchy(
            new int[]{1, 2, 5, 8, 10, 11},
            new int[]{0, 1, 1, 0, 1, 2}
        );

        assertEquals(filteredExpected.formatString(), filteredActual.formatString());
    }

    @Test
    void testFilter_removeRoot_removesWholeTree() {
        Hierarchy h = new ArrayBasedHierarchy(
            new int[]{1, 2, 3},
            new int[]{0, 1, 2}
        );

        Hierarchy filtered = HierarchyFilter.filter(h, id -> id != 1);

        assertEquals("[]", filtered.formatString());
    }

    @Test
    void testFilter_removeIntermediateNode_removesSubtree() {
        Hierarchy h = new ArrayBasedHierarchy(
            new int[]{1, 2, 3, 4},
            new int[]{0, 1, 2, 1}
        );

        // Remove node 2 => nodes 2 and 3 disappear
        Hierarchy filtered = HierarchyFilter.filter(h, id -> id != 2);

        Hierarchy expected = new ArrayBasedHierarchy(
            new int[]{1, 4},
            new int[]{0, 1}
        );

        assertEquals(expected.formatString(), filtered.formatString());
    }

    @Test
    void testFilter_keepAll() {
        Hierarchy h = new ArrayBasedHierarchy(
            new int[]{1, 2, 3},
            new int[]{0, 1, 1}
        );

        Hierarchy filtered = HierarchyFilter.filter(h, id -> true);

        assertEquals(h.formatString(), filtered.formatString());
    }

    @Test
    void testFilter_removeAll() {
        Hierarchy h = new ArrayBasedHierarchy(
            new int[]{1, 2, 3},
            new int[]{0, 1, 1}
        );

        Hierarchy filtered = HierarchyFilter.filter(h, id -> false);

        assertEquals("[]", filtered.formatString());
    }

    @Test
    void testFilter_singleNode() {
        Hierarchy h = new ArrayBasedHierarchy(
            new int[]{42},
            new int[]{0}
        );

        Hierarchy filtered = HierarchyFilter.filter(h, id -> id == 42);

        assertEquals("[42:0]", filtered.formatString());
    }
}
}
