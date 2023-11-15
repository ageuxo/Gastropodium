package io.github.ageuxo.Gastropodium.entity.pathing;

public class BlockEdgeBinaryHeap {
    private BlockEdgeNode[] heap = new BlockEdgeNode[128];
    private int size;

    public BlockEdgeNode insert(BlockEdgeNode node){
        if (node.heapIdx >= 0){
            throw new IllegalStateException("You can't do that! That's ILLEGAL!");
        } else {
            if (this.size == this.heap.length){
                BlockEdgeNode[] oneLess = new BlockEdgeNode[this.size << 1];
                System.arraycopy(this.heap, 0, oneLess, 0, this.size);
                this.heap = oneLess;
            }
            this.heap[this.size] = node;
            node.heapIdx = this.size;
            this.upHeap(this.size++);
            return node;
        }
    }

    public void clear(){
        this.size = 0;
    }

    public BlockEdgeNode peek(){
        return this.heap[0];
    }

    public BlockEdgeNode pop() {
        BlockEdgeNode node = this.heap[0];
        this.heap[0] = this.heap[--this.size];
        this.heap[this.size] = null;
        if (this.size > 0) {
            this.downHeap(0);
        }

        node.heapIdx = -1;
        return node;
    }

    public void remove(BlockEdgeNode pNode) {
        this.heap[pNode.heapIdx] = this.heap[--this.size];
        this.heap[this.size] = null;
        if (this.size > pNode.heapIdx) {
            if (this.heap[pNode.heapIdx].f < pNode.f) {
                this.upHeap(pNode.heapIdx);
            } else {
                this.downHeap(pNode.heapIdx);
            }
        }

        pNode.heapIdx = -1;
    }

    public void changeCost(BlockEdgeNode pPoint, float pCost) {
        float f = pPoint.f;
        pPoint.f = pCost;
        if (pCost < f) {
            this.upHeap(pPoint.heapIdx);
        } else {
            this.downHeap(pPoint.heapIdx);
        }

    }

    public int size() {
        return this.size;
    }

    private void upHeap(int index) {
        BlockEdgeNode node = this.heap[index];
        int count;
        for (float f = node.f; index > 0; index = count){
            count = index - 1 >> 1;
            BlockEdgeNode node1 = this.heap[count];
            if (!(f < node1.f)){
                break;
            }

            this.heap[index] = node1;
            node1.heapIdx = index;
        }
        this.heap[index] = node;
        node.heapIdx = index;
    }

    private void downHeap(int pIndex) {
        BlockEdgeNode node = this.heap[pIndex];
        float f = node.f;

        while(true) {
            int i = 1 + (pIndex << 1);
            int j = i + 1;
            if (i >= this.size) {
                break;
            }

            BlockEdgeNode node1 = this.heap[i];
            float f1 = node1.f;
            BlockEdgeNode node2;
            float f2;
            if (j >= this.size) {
                node2 = null;
                f2 = Float.POSITIVE_INFINITY;
            } else {
                node2 = this.heap[j];
                f2 = node2.f;
            }

            if (f1 < f2) {
                if (!(f1 < f)) {
                    break;
                }

                this.heap[pIndex] = node1;
                node1.heapIdx = pIndex;
                pIndex = i;
            } else {
                if (!(f2 < f)) {
                    break;
                }

                this.heap[pIndex] = node2;
                node2.heapIdx = pIndex;
                pIndex = j;
            }
        }

        this.heap[pIndex] = node;
        node.heapIdx = pIndex;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public BlockEdgeNode[] getHeap() {
        BlockEdgeNode[] anode = new BlockEdgeNode[this.size()];
        System.arraycopy(this.heap, 0, anode, 0, this.size());
        return anode;
    }
}
