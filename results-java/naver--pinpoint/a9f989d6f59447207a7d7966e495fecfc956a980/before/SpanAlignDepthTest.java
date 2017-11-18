package com.navercorp.pinpoint.web.calltree.span;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.navercorp.pinpoint.common.bo.SpanEventBo;

public class SpanAlignDepthTest {
    private static final boolean SYNC = false;
    private static final boolean ASYNC = true;

    private List<String> expectResult = new ArrayList<String>();
    private List<SpanEventBo> callTree = new ArrayList<SpanEventBo>();

    @Test
    public void normal() {

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(SYNC, (short) 1, 2));
        callTree.add(makeSpanEventBo(SYNC, (short) 2, 3));
        callTree.add(makeSpanEventBo(SYNC, (short) 3, 4));
        assertDepth("normal-case-1", callTree, expectResult);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("####");
        expectResult.add("####");
        expectResult.add("####");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(SYNC, (short) 1, 2));
        callTree.add(makeSpanEventBo(SYNC, (short) 2, 3));
        callTree.add(makeSpanEventBo(SYNC, (short) 3, 4));
        callTree.add(makeSpanEventBo(SYNC, (short) 4, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 5, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 6, -1));
        assertDepth("normal-case-2", callTree, expectResult);
        callTree.clear();

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("#####");
        expectResult.add("####");
        expectResult.add("####");
        expectResult.add("#####");
        expectResult.add("######");
        expectResult.add("#######");
        expectResult.add("#######");
        expectResult.add("####");
        expectResult.add("####");
        expectResult.add("####");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(SYNC, (short) 1, 2));
        callTree.add(makeSpanEventBo(SYNC, (short) 2, 3));
        callTree.add(makeSpanEventBo(SYNC, (short) 3, 4));
        callTree.add(makeSpanEventBo(SYNC, (short) 4, 5));
        callTree.add(makeSpanEventBo(SYNC, (short) 5, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 6, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 7, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 8, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 9, 4));
        callTree.add(makeSpanEventBo(SYNC, (short) 10, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 11, 5));
        callTree.add(makeSpanEventBo(SYNC, (short) 12, 6));
        callTree.add(makeSpanEventBo(SYNC, (short) 13, 7));
        callTree.add(makeSpanEventBo(SYNC, (short) 14, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 15, 4));
        callTree.add(makeSpanEventBo(SYNC, (short) 16, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 17, -1));
        assertDepth("normal-case-3", callTree, expectResult);

    }

    @Test
    public void async() {
        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("#");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(SYNC, (short) 1, -1));
        assertDepth("async-case-1", callTree, expectResult);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("####");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(ASYNC, (short) 1, 2));
        callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
        assertDepth("async-case-2", callTree, expectResult);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");
        expectResult.add("#");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(SYNC, (short) 1, -1));
        assertDepth("async-case-3", callTree, expectResult);
    }

    @Test
    public void missing() {
        expectResult.clear();
        expectResult.add("#");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 20, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 21, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 22, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 23, -1));
        assertDepth("missing-case-1", callTree, expectResult);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");
        expectResult.add("###");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(SYNC, (short) 1, 2));
        callTree.add(makeSpanEventBo(SYNC, (short) 2, 3));
//        callTree.add(makeSpanEventBo(SYNC, (short) 3, 4));
//        callTree.add(makeSpanEventBo(SYNC, (short) 4, 5));
//        callTree.add(makeSpanEventBo(SYNC, (short) 5, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 6, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 7, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 8, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 9, 4));
        callTree.add(makeSpanEventBo(SYNC, (short) 10, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 11, 5));
        callTree.add(makeSpanEventBo(SYNC, (short) 12, 6));
        callTree.add(makeSpanEventBo(SYNC, (short) 13, 7));
        callTree.add(makeSpanEventBo(SYNC, (short) 14, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 15, 4));
        callTree.add(makeSpanEventBo(SYNC, (short) 16, -1));
        callTree.add(makeSpanEventBo(SYNC, (short) 17, -1));
        assertDepth("missing-case-2", callTree, expectResult);

        expectResult.clear();
        expectResult.add("#");
        expectResult.add("##");

        callTree.clear();
        callTree.add(makeSpanEventBo(SYNC, (short) 0, 1));
//        callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
        callTree.add(makeSpanEventBo(ASYNC, (short) 1, 2));
        callTree.add(makeSpanEventBo(ASYNC, (short) 0, 1));
        assertDepth("missing-case-3", callTree, expectResult);
    }


    private void assertDepth(final String name, List<SpanEventBo> list, List<String> result) {
        System.out.println("===== " + name + "=====");
        int index = 0;
        SpanAlignDepth depth = new SpanAlignDepth(0);
        for (SpanEventBo event : list) {
            if(depth.isParentMissing(event)) {
                continue;
            }

            int currentDepth = 0;
            if(depth.findMissing(event)) {
                currentDepth = depth.getMissingDepth(event);
            } else {
                currentDepth = depth.getDepth(event);
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < currentDepth; i++) {
                sb.append("#");
            }

            final String expected = result.get(index++);
            if (expected.equals(sb.toString())) {
                System.out.println("Matched(" + index + "): " + sb.toString());
            } else {
                System.out.println("Not Matched(" + index + "): " + sb.toString() + ", expected=" + expected);
            }
            // log
        }
        System.out.println("");
    }

    private SpanEventBo makeSpanEventBo(final boolean async, final short sequence, final int depth) {
        SpanEventBo event = new SpanEventBo();
        event.setAsyncId(async ? 1 : -1);
        event.setSequence(sequence);
        event.setDepth(depth);

        return event;
    }
}