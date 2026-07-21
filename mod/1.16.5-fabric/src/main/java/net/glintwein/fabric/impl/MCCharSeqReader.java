package net.glintwein.fabric.impl;

import net.glintwein.ui.MinecraftTooltipRenderer;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

public class MCCharSeqReader extends MinecraftTooltipRenderer.CharSeqReader implements FormattedCharSink {
    private final FormattedCharSequence seq;

    public MCCharSeqReader(FormattedCharSequence seq) {
        this.seq = seq;
    }

    @Override
    public boolean accept(int pos, Style style, int codepoint) {
        return readChar(codepoint);
    }

    @Override
    public void run() {
        seq.accept(this);
    }
}
