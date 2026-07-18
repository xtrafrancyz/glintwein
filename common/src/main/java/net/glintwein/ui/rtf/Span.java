package net.glintwein.ui.rtf;

import java.util.List;

public interface Span {
    void generateRenderTokens(List<RenderToken> output);
}
