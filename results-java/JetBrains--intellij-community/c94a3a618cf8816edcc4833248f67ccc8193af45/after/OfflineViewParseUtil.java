/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * User: anna
 * Date: 05-Jan-2007
 */
package com.intellij.codeInspection.offlineViewer;

import com.intellij.codeInspection.reference.SmartRefElementPointerImpl;
import com.thoughtworks.xstream.io.xml.XppReader;
import gnu.trove.THashMap;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NonNls;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OfflineViewParseUtil {
  @NonNls private static final String PACKAGE = "package";
  @NonNls private static final String DESCRIPTION = "description";
  @NonNls private static final String HINTS = "hints";
  @NonNls private static final String LINE = "line";

  private OfflineViewParseUtil() {
  }

  public static Map<String, List<OfflineProblemDescriptor>> parse(final String problems) {
    final TObjectIntHashMap<String> fqName2IdxMap = new TObjectIntHashMap<String>();
    final Map<String, List<OfflineProblemDescriptor>> package2Result = new THashMap<String, List<OfflineProblemDescriptor>>();
    final XppReader reader = new XppReader(new StringReader(problems));
    try {
      while(reader.hasMoreChildren()) {
        reader.moveDown(); //problem
        final OfflineProblemDescriptor descriptor = new OfflineProblemDescriptor();
        while(reader.hasMoreChildren()) {
          reader.moveDown();
          if (SmartRefElementPointerImpl.ENTRY_POINT.equals(reader.getNodeName())) {
            descriptor.setType(reader.getAttribute(SmartRefElementPointerImpl.TYPE_ATTR));
            descriptor.setFQName(reader.getAttribute(SmartRefElementPointerImpl.FQNAME_ATTR));
            final List<String> parentTypes = new ArrayList<String>();
            final List<String> parentNames = new ArrayList<String>();
            int deep = 0;
            while (true) {
              if (reader.hasMoreChildren()) {
                reader.moveDown();
                parentTypes.add(reader.getAttribute(SmartRefElementPointerImpl.TYPE_ATTR));
                parentNames.add(reader.getAttribute(SmartRefElementPointerImpl.FQNAME_ATTR));
                deep ++;
              } else {
                while (deep-- > 0) {
                  reader.moveUp();
                }
                break;
              }
            }
            if (!parentTypes.isEmpty() && !parentNames.isEmpty()) {
              descriptor.setParentType(parentTypes.toArray(new String[parentTypes.size()]));
              descriptor.setParentFQName(parentNames.toArray(new String[parentNames.size()]));
            }
          }
          if (DESCRIPTION.equals(reader.getNodeName())) {
            descriptor.setDescription(reader.getValue());
          }
          if (LINE.equals(reader.getNodeName())) {
            descriptor.setLine(Integer.parseInt(reader.getValue()));
          }
          if (HINTS.equals(reader.getNodeName())) {
            while(reader.hasMoreChildren()) {
              reader.moveDown();
              List<String> hints = descriptor.getHints();
              if (hints == null) {
                hints = new ArrayList<String>();
                descriptor.setHints(hints);
              }
              hints.add(reader.getValue());
              reader.moveUp();
            }
          }
          while(reader.hasMoreChildren()) {
            reader.moveDown();
            if (PACKAGE.equals(reader.getNodeName())) {
              final String packageName = reader.getValue();
              List<OfflineProblemDescriptor> descriptors = package2Result.get(packageName);
              if (descriptors == null) {
                descriptors = new ArrayList<OfflineProblemDescriptor>();
                package2Result.put(packageName, descriptors);
              }
              descriptors.add(descriptor);
            }
            reader.moveUp();
          }
          reader.moveUp();
        }

        final String fqName = descriptor.getFQName();
        if (!fqName2IdxMap.containsKey(fqName)) {
          fqName2IdxMap.put(fqName, 0);
        }
        int idx = fqName2IdxMap.get(fqName);
        descriptor.setProblemIndex(idx);
        fqName2IdxMap.put(fqName, idx + 1);

        reader.moveUp();
      }
    }
    finally {
      reader.close();
    }
    return package2Result;
  }

}