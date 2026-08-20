package com.solarbiscuit.compat.curios;

import com.solarbiscuit.client.femboy.FemboyRenderer;

public final class FemboyCuriosClient {
    private FemboyCuriosClient() {}

    public static void addLayers(FemboyRenderer renderer) {
        renderer.addLayer(new FemboyCuriosRenderLayer(renderer));
    }
}
