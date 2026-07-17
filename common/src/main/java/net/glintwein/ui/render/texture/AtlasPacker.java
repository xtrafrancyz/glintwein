package net.glintwein.ui.render.texture;

/**
 * Texture Atlas Packer
 * <p>
 * Implements a binary tree-based lightmap / texture packing algorithm.
 * Images are recursively inserted into an atlas texture by splitting
 * free regions into smaller rectangles.
 */
public class AtlasPacker {
    // -------------------------------------------------------------------------
    // Node (internal binary tree node)
    // -------------------------------------------------------------------------

    private static class Node {
        Node child0;
        Node child1;
        public int left, top, right, bottom;
        boolean occupied;

        Node() {
        }

        Node(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        boolean isLeaf() {
            return child0 == null && child1 == null;
        }

        /**
         * Try to insert {@code img} into this node's subtree.
         *
         * @return the node where the image was placed, or {@code null} if it
         * doesn't fit anywhere in this subtree.
         */
        Node insert(int width, int height) {

            // --- internal node: try children ---
            if (!isLeaf()) {
                Node result = child0.insert(width, height);
                return (result != null) ? result : child1.insert(width, height);
            }

            // --- leaf node ---

            // Already occupied
            if (occupied) return null;

            int rw = width();
            int rh = height();

            // Image doesn't fit
            if (width > rw || height > rh) return null;

            // Perfect fit – accept immediately
            if (width == rw && height == rh) return this;

            // Split this node and recurse into the first child
            child0 = new Node();
            child1 = new Node();

            int dw = rw - width;
            int dh = rh - height;

            if (dw > dh) {
                // Split vertically (left / right)
                child0.left = left;
                child0.top = top;
                child0.right = left + width - 1;
                child0.bottom = bottom;

                child1.left = left + width;
                child1.top = top;
                child1.right = right;
                child1.bottom = bottom;
            } else {
                // Split horizontally (top / bottom)
                child0.left = left;
                child0.top = top;
                child0.right = right;
                child0.bottom = top + height - 1;

                child1.left = left;
                child1.top = top + height;
                child1.right = right;
                child1.bottom = bottom;
            }

            return child0.insert(width, height);
        }

        public int width() {
            return right - left + 1;
        }

        public int height() {
            return bottom - top + 1;
        }
    }

    // -------------------------------------------------------------------------
    // TextureAtlas
    // -------------------------------------------------------------------------

    private final int atlasWidth;
    private final int atlasHeight;
    private final Node root;

    public AtlasPacker(int width, int height) {
        this.atlasWidth = width;
        this.atlasHeight = height;
        this.root = new Node(0, 0, width - 1, height - 1);
    }

    public void reset() {
        root.child0 = null;
        root.child1 = null;
        root.occupied = false;
    }

    public Rect insert(int width, int height) {
        Node node = root.insert(width, height);
        if (node == null)
            return null;
        node.occupied = true;
        return new Rect(
            node.left, node.top, node.right + 1, node.bottom + 1,
            (float) node.left / atlasWidth,
            (float) node.top / atlasHeight,
            (float) (node.right + 1) / atlasWidth,
            (float) (node.bottom + 1) / atlasHeight
        );
    }

    public int getAtlasWidth() {
        return atlasWidth;
    }

    public int getAtlasHeight() {
        return atlasHeight;
    }

    public static class Rect {
        public final int left, top, right, bottom;
        public float u0, v0, u1, v1;

        public Rect(
            int left, int top, int right, int bottom,
            float u0, float v0, float u1, float v1
        ) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
        }

        public int width() {
            return right - left;
        }

        public int height() {
            return bottom - top;
        }
    }
}
