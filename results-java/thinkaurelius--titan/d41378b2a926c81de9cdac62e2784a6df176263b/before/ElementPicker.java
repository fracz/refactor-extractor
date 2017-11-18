package com.thinkaurelius.titan.hadoop.mapreduce.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.thinkaurelius.titan.hadoop.*;
import com.thinkaurelius.titan.hadoop.FaunusProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class ElementPicker {

    protected ElementPicker() {
    }

    public static String getPropertyAsString(final FaunusElement element, final String key) {
        if (key.equals(Tokens._ID) || key.equals(Tokens.ID))
            return element.getId().toString();
        else if (key.equals(Tokens._PROPERTIES)) {
            final ListMultimap<String, Object> properties = ArrayListMultimap.create();
            for (final FaunusProperty property : element.getProperties()) {
                properties.put(property.getType().getName(), property.getValue());
            }
            properties.put(Tokens._ID, element.getId());
            if (element instanceof StandardFaunusEdge)
                properties.put(Tokens._LABEL, ((StandardFaunusEdge) element).getLabel());

            return properties.toString();
        } else if (key.equals(Tokens.LABEL) && element instanceof StandardFaunusEdge) {
            return ((StandardFaunusEdge) element).getLabel();
        } else {
            if (element instanceof HadoopVertex) {
                List values = new ArrayList();
                Iterables.addAll(values, ((HadoopVertex) element).getProperties(key));
                if (values.size() == 0)
                    return Tokens.NULL;
                else if (values.size() == 1)
                    return values.iterator().next().toString();
                else {
                    return values.toString();
                }
            } else {
                final Object value = element.getProperty(key);
                if (null != value)
                    return value.toString();
                else
                    return Tokens.NULL;
            }
        }
    }

    public static Object getProperty(final FaunusElement element, final String key) {
        if (key.equals(Tokens._ID) || key.equals(Tokens.ID))
            return element.getId();
        else if (key.equals(Tokens._PROPERTIES)) {
            final ListMultimap<String, Object> properties = ArrayListMultimap.create();
            for (final FaunusProperty property : element.getProperties()) {
                properties.put(property.getType().getName(), property.getValue());
            }
            properties.put(Tokens._ID, element.getId());
            return properties;
        } else if (key.equals(Tokens.LABEL) && element instanceof StandardFaunusEdge) {
            return ((StandardFaunusEdge) element).getLabel();
        } else {
            if (element instanceof HadoopVertex) {
                List values = new ArrayList();
                Iterables.addAll(values, ((HadoopVertex) element).getProperties(key));
                if (values.size() == 0)
                    return null;
                else if (values.size() == 1)
                    return values.iterator().next();
                else {
                    return values;
                }
            } else
                return element.getProperty(key);
        }
    }
}