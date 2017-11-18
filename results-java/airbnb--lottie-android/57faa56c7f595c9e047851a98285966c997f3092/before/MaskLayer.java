package com.airbnb.lottie.layers;

import android.graphics.drawable.Drawable;

import com.airbnb.lottie.model.LottieComposition;
import com.airbnb.lottie.model.Mask;

import java.util.List;

class MaskLayer extends AnimatableLayer {

    private final List<Mask> masks;

    MaskLayer(List<Mask> masks, LottieComposition composition, Drawable.Callback callback) {
        super(composition.getDuration(), callback);
        this.masks = masks;
    }

    List<Mask> getMasks() {
        return masks;
    }
}