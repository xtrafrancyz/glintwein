package net.glintwein.ui.render.texture;

/**
 * Texture Atlas Packer
 * <p>
 * Implements a binary tree-based lightmap / texture packing algorithm.
 * Images are recursively inserted into an atlas texture by splitting
 * free regions into smaller rectangles.
 */
public class AtlasPacker {
    public static class Rectangle {
        public final int left, top, right, bottom;

        public Rectangle(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        public int width() {
            return right - left + 1;
        }

        public int height() {
            return bottom - top + 1;
        }

        @Override
        public String toString() {
            return String.format("Rect(%d,%d -> %d,%d  [%dx%d])",
                left, top, right, bottom, width(), height());
        }
    }

    // -------------------------------------------------------------------------
    // Node (internal binary tree node)
    // -------------------------------------------------------------------------

    private static class Node {
        Node[] child = new Node[2];
        Rectangle rect;
        boolean occupied;

        Node(Rectangle rect) {
            this.rect = rect;
        }

        boolean isLeaf() {
            return child[0] == null && child[1] == null;
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
                Node result = child[0].insert(width, height);
                return (result != null) ? result : child[1].insert(width, height);
            }

            // --- leaf node ---

            // Already occupied
            if (occupied) return null;

            int rw = rect.width();
            int rh = rect.height();

            // Image doesn't fit
            if (width > rw || height > rh) return null;

            // Perfect fit – accept immediately
            if (width == rw && height == rh) return this;

            // Split this node and recurse into the first child
            child[0] = new Node(null);
            child[1] = new Node(null);

            int dw = rw - width;
            int dh = rh - height;

            if (dw > dh) {
                // Split vertically (left / right)
                child[0].rect = new Rectangle(rect.left,
                    rect.top,
                    rect.left + width - 1,
                    rect.bottom);
                child[1].rect = new Rectangle(rect.left + width,
                    rect.top,
                    rect.right,
                    rect.bottom);
            } else {
                // Split horizontally (top / bottom)
                child[0].rect = new Rectangle(rect.left,
                    rect.top,
                    rect.right,
                    rect.top + height - 1);
                child[1].rect = new Rectangle(rect.left,
                    rect.top + height,
                    rect.right,
                    rect.bottom);
            }

            return child[0].insert(width, height);
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
        this.root = new Node(new Rectangle(0, 0, width - 1, height - 1));
    }

    public void reset() {
        root.child[0] = null;
        root.child[1] = null;
        root.occupied = false;
    }

    public Rect insert(int width, int height) {
        Node node = root.insert(width, height);
        if (node == null)
            return null;
        node.occupied = true;
        Rectangle r = node.rect;
        return new Rect(
            r.left, r.top, r.right + 1, r.bottom + 1,
            (float) r.left / atlasWidth,
            (float) r.top / atlasHeight,
            (float) (r.right + 1) / atlasWidth,
            (float) (r.bottom + 1) / atlasHeight
        );
    }

    public int getAtlasWidth() {
        return atlasWidth;
    }

    public int getAtlasHeight() {
        return atlasHeight;
    }

    public static class Rect {
        public final float left, top, right, bottom;
        public float u0, v0, u1, v1;

        public Rect(
            float left, float top, float right, float bottom,
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
    }
}
