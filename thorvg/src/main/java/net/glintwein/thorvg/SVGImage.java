package net.glintwein.thorvg;

import io.github.xtrafrancyz.jthorvg.Picture;
import io.github.xtrafrancyz.jthorvg.Thorvg;

public class SVGImage extends ThorvgElement {
    public SVGImage(String svg) {
        this(svg.getBytes());
    }

    public SVGImage(byte[] svg) {
        setContent(new SVGContent(svg));
    }

    private class SVGContent implements ThorvgElement.Content {
        private byte[] svgData;
        private Picture picture;

        SVGContent(byte[] svgData) {
            this.svgData = svgData;
        }

        @Override
        public void init(AsyncCanvas canvas) {
            picture = Thorvg.newPicture();
            picture.loadData(svgData, "svg", "", true);
            float[] size = picture.getSize();
            if (size[0] > 0 && size[1] > 0)
                setContentSize(size[0], size[1]);
            svgData = null;

            canvas.canvas().add(picture);
        }

        @Override
        public void resize(AsyncCanvas canvas) {
            picture.setSize(canvas.width(), canvas.height());
        }

        @Override
        public boolean update(AsyncCanvas canvas) {
            return false;
        }

        @Override
        public boolean shouldUpdate() {
            return false;
        }

        @Override
        public void release() {
            if (picture != null) {
                picture.close();
                picture = null;
            }
        }
    }
}
