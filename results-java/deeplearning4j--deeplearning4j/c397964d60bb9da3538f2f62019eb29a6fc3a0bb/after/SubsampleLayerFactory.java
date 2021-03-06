package org.deeplearning4j.nn.layers.factory;

import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.ParamInitializer;
import org.deeplearning4j.nn.params.SubsampleParamInitializer;

/**
 * @author Adam Gibson
 */
public class SubsampleLayerFactory extends DefaultLayerFactory {
    public SubsampleLayerFactory(Class<? extends Layer> layerClazz) {
        super(layerClazz);
    }

    @Override
    public ParamInitializer initializer() {
        return new SubsampleParamInitializer();
    }
}