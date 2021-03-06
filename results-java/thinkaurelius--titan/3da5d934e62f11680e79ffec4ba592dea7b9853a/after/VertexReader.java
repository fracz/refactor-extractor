package com.thinkaurelius.faunus.formats.titan.input;

import com.thinkaurelius.titan.diskstorage.StaticBuffer;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public interface VertexReader {

    public long getVertexId(StaticBuffer key);

}