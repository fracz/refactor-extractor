package com.thinkaurelius.faunus.util;

import com.thinkaurelius.faunus.FaunusVertex;
import com.thinkaurelius.faunus.util.TaggedHolder;
import junit.framework.TestCase;
import org.apache.hadoop.io.WritableComparator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class TaggedHolderTest extends TestCase {

    public void testRawComparison() throws IOException {
        TaggedHolder<FaunusVertex> holder1 = new TaggedHolder<FaunusVertex>('a', new FaunusVertex(10));
        TaggedHolder<FaunusVertex> holder2 = new TaggedHolder<FaunusVertex>('b', new FaunusVertex(11));

        ByteArrayOutputStream bytes1 = new ByteArrayOutputStream();
        holder1.write(new DataOutputStream(bytes1));
        ByteArrayOutputStream bytes2 = new ByteArrayOutputStream();
        holder2.write(new DataOutputStream(bytes2));

        assertEquals(-1, WritableComparator.get(TaggedHolder.class).compare(bytes1.toByteArray(), 0, bytes1.size(), bytes2.toByteArray(), 0, bytes2.size()));
        assertEquals(1, WritableComparator.get(TaggedHolder.class).compare(bytes2.toByteArray(), 0, bytes2.size(), bytes1.toByteArray(), 0, bytes1.size()));
        assertEquals(0, WritableComparator.get(TaggedHolder.class).compare(bytes1.toByteArray(), 0, bytes1.size(), bytes1.toByteArray(), 0, bytes1.size()));
    }

    public void testSerialization1() throws IOException {
        FaunusVertex vertex = new FaunusVertex(1l);
        TaggedHolder<FaunusVertex> holder1 = new TaggedHolder<FaunusVertex>('a', vertex);
        assertEquals(holder1.get(), vertex);
        assertEquals(holder1.getTag(), 'a');

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        holder1.write(new DataOutputStream(bytes));
        TaggedHolder<FaunusVertex> holder2 = new TaggedHolder<FaunusVertex>(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));

        assertEquals(holder1, holder2);
        assertEquals(holder2.get(), vertex);
        assertEquals(holder2.getTag(), 'a');

        assertEquals(holder1.compareTo(holder2), 0);
    }
}