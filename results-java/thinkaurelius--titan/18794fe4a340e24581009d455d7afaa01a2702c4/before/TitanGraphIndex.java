package com.thinkaurelius.titan.core.schema;

import com.thinkaurelius.titan.core.PropertyKey;
import com.tinkerpop.blueprints.Element;

/**
 * @author Matthias Broecheler (me@matthiasb.com)
 */
public interface TitanGraphIndex extends TitanSchemaElement {

    public String getName();

    public String getBackingIndex();

    public Class<? extends Element> getIndexedElement();

    public PropertyKey[] getFieldKeys();

    public Parameter[] getParametersFor(PropertyKey key);

    public boolean isUnique();

}