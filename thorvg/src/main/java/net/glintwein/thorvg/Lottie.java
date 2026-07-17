package net.glintwein.thorvg;

import io.github.xtrafrancyz.jthorvg.LottieAnimation;
import io.github.xtrafrancyz.jthorvg.Picture;
import io.github.xtrafrancyz.jthorvg.Thorvg;
import net.glintwein.Glintwein;
import net.glintwein.util.ResourceLoaderUtil;

import java.io.InputStream;

public class Lottie extends ThorvgElement {
    private final LottieContent content;

    public Lottie(InputStream json) {
        this(ResourceLoaderUtil.toBytes(json));
    }

    public Lottie(String json) {
        this(json.getBytes());
    }

    public Lottie(byte[] json) {
        setContent(content = new LottieContent(json));
    }

    private class LottieContent implements Content {
        private byte[] jsonData;

        private LottieAnimation lottieAnimation;
        private Picture picture;
        private long durationMillis;
        private float totalFrame;

        private long start = Glintwein.time;
        private long cachedTime;

        LottieContent(byte[] jsonData) {
            this.jsonData = jsonData;
        }

        @Override
        public void init(AsyncCanvas canvas) {
            lottieAnimation = Thorvg.newLottieAnimation();
            picture = lottieAnimation.getPicture();
            picture.loadData(jsonData, "lottie+json", "", true);
            float[] size = picture.getSize();
            if (size[0] > 0 && size[1] > 0)
                setContentSize(size[0], size[1]);
            jsonData = null;

            durationMillis = (long) (this.lottieAnimation.getDuration() * 1000d);
            totalFrame = this.lottieAnimation.getTotalFrame();

            canvas.canvas().add(picture);
        }

        @Override
        public void resize(AsyncCanvas canvas) {
            picture.setSize(canvas.width(), canvas.height());
        }

        @Override
        public boolean update(AsyncCanvas canvas) {
            return lottieAnimation.setFrame(totalFrame * getAnimationProgress());
        }

        public float getAnimationProgress() {
            long elapsed = time() - start;
            return (float) (elapsed % durationMillis) / durationMillis;
        }

        private long time() {
            synchronized (this) {
                return cachedTime;
            }
        }

        @Override
        public boolean shouldUpdate() {
            synchronized (this) {
                cachedTime = Glintwein.time;
            }

            return true;
        }

        @Override
        public void release() {
            if (lottieAnimation != null) {
                lottieAnimation.close();
                lottieAnimation = null;
            }
        }
    }
}
