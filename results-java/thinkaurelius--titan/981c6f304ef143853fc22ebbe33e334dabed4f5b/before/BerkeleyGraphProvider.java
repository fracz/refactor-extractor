package com.thinkaurelius.titan.blueprints;

import com.thinkaurelius.titan.BerkeleyStorageSetup;
import com.thinkaurelius.titan.diskstorage.configuration.BasicConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public class BerkeleyGraphProvider extends AbstractTitanGraphProvider {

    @Override
    public BasicConfiguration getTitanConfiguration(String graphName, Class<?> test, String testMethodName) {
        return BerkeleyStorageSetup.getBerkeleyJEConfiguration(graphName);
    }

}