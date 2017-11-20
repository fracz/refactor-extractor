package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xtremelabs.robolectric.util.TestAttributeSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Boolean.TRUE;

public class ViewLoader extends XmlLoader {
    private Map<String, ViewNode> viewNodesByLayoutName = new HashMap<String, ViewNode>();
    private StringResourceLoader stringResourceLoader;
    private AttrResourceLoader attrResourceLoader;

    public ViewLoader(ResourceExtractor resourceExtractor, StringResourceLoader stringResourceLoader, AttrResourceLoader attrResourceLoader) {
        super(resourceExtractor);
        this.stringResourceLoader = stringResourceLoader;
        this.attrResourceLoader = attrResourceLoader;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document) throws Exception {
        ViewNode topLevelNode = new ViewNode("top-level", new HashMap<String, String>());
        processChildren(document.getChildNodes(), topLevelNode);
        viewNodesByLayoutName.put(
                "layout/" + xmlFile.getName().replace(".xml", ""),
                topLevelNode.getChildren().get(0));
    }

    private void processChildren(NodeList childNodes, ViewNode parent) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent);
        }
    }

    private void processNode(Node node, ViewNode parent) {
        String name = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();
        Map<String, String> attrMap = new HashMap<String, String>();
        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = attributes.item(i);
                attrMap.put(attr.getNodeName(), attr.getNodeValue());
            }
        }

        if (name.equals("requestFocus")) {
            parent.attributes.put("android:focus", "true");
            parent.requestFocusOverride = true;
        } else if (!name.startsWith("#")) {
            ViewNode viewNode = new ViewNode(name, attrMap);
            if (parent != null) parent.addChild(viewNode);

            String idAttr = getIdAttr(node);
            if (idAttr != null && idAttr.startsWith("@+id/")) {
                idAttr = idAttr.substring(5);

                Integer id = resourceExtractor.getResourceStringToId().get("id/" + idAttr);
                if (id == null) {
                    throw new RuntimeException("unknown id " + getIdAttr(node));
                }
                viewNode.setId(id);
            }

            processChildren(node.getChildNodes(), viewNode);
        }
    }

    public View inflateView(Context context, String key) {
        return inflateView(context, key, null);
    }

    private View inflateView(Context context, String key, Map<String, String> attributes) {
        ViewNode viewNode = viewNodesByLayoutName.get(key);
        if (viewNode == null) {
            throw new RuntimeException("no such layout " + key);
        }
        try {
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    if (!entry.getKey().equals("layout")) {
                        viewNode.attributes.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            return viewNode.inflate(context, null);
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + key, e);
        }
    }

    public View inflateView(Context context, int resourceId) {
        return inflateView(context, resourceExtractor.getResourceName(resourceId));
    }

    private class ViewNode {
        private String name;
        private final Map<String, String> attributes;

        private ViewNode parent;
        private List<ViewNode> children = new ArrayList<ViewNode>();
        private Integer id;
        boolean requestFocusOverride = false;

        public ViewNode(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        public List<ViewNode> getChildren() {
            return children;
        }

        public void addChild(ViewNode viewNode) {
            viewNode.parent = this;
            children.add(viewNode);
        }

        public View inflate(Context context, View parent) throws Exception {
            View view = create(context, (ViewGroup) parent);

            if (id != null && view.getId() == 0) {
                view.setId(id);
            }
            for (ViewNode child : children) {
                child.inflate(context, view);
            }
            return view;
        }

        private void applyAttributes(View view) {
            view.setVisibility(View.VISIBLE);

            String visibility = attributes.get("android:visibility");
            if (visibility != null) {
                if (visibility.equals("gone")) {
                    view.setVisibility(View.GONE);
                } else if (visibility.equals("invisible")) {
                    view.setVisibility(View.INVISIBLE);
                }
            }

            Boolean enabled = getAttributeAsBool("android:enabled");
            if (enabled != null) {
                view.setEnabled(enabled);
            }

            checkFocusOverride(view);
            if (!anyParentHasFocus(view)) {
                Boolean focusRequested = getAttributeAsBool("android:focus");
                if (TRUE.equals(focusRequested) || view.isFocusableInTouchMode()) {
                    view.requestFocus();
                }
            }

            if (view instanceof TextView) {
                int drawableTop = 0;
                int drawableRight = 0;
                int drawableBottom = 0;
                int drawableLeft = 0;

                String text = attributes.get("android:text");
                if (text != null) {
                    if (text.startsWith("@string/")) {
                        text = stringResourceLoader.getValue(text.substring(1));
                    }

                    ((TextView) view).setText(text);
                }
                text = attributes.get("android:drawableTop");
                if (text != null) {
                    Integer resId = resourceExtractor.getResourceStringToId().get(text.substring(1));
                    if (resId != null) {
                        drawableTop = resId;
                    }
                }
                text = attributes.get("android:drawableRight");
                if (text != null) {
                    Integer resId = resourceExtractor.getResourceStringToId().get(text.substring(1));
                    if (resId != null) {
                        drawableRight = resId;
                    }
                }
                text = attributes.get("android:drawableBottom");
                if (text != null) {
                    Integer resId = resourceExtractor.getResourceStringToId().get(text.substring(1));
                    if (resId != null) {
                        drawableBottom = resId;
                    }
                }
                text = attributes.get("android:drawableLeft");
                if (text != null) {
                    Integer resId = resourceExtractor.getResourceStringToId().get(text.substring(1));
                    if (resId != null) {
                        drawableLeft = resId;
                    }
                }
                ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
            }

            if (view instanceof CheckBox) {
                String text = attributes.get("android:checked");
                if (text != null) {
                    ((CheckBox) view).setChecked(Boolean.valueOf(text));
                }
            }

            if (view instanceof ImageView) {
                String text = attributes.get("android:src");
                if (text != null) {
                    if (text.startsWith("@drawable/")) {
                        Integer resId = resourceExtractor.getResourceStringToId().get(text.substring(1));
                        ((ImageView) view).setImageResource(resId);
                    }
                }
            }
        }

        private void checkFocusOverride(View view) {
            if (requestFocusOverride) {
                View root = view;
                View parent = (View) root.getParent();
                while (parent != null) {
                    root = parent;
                    parent = (View) root.getParent();
                }
                root.clearFocus();
            }
        }

        private boolean anyParentHasFocus(View view) {
            while (view != null) {
                if (view.hasFocus()) return true;
                view = (View) view.getParent();
            }
            return false;
        }

        private Boolean getAttributeAsBool(String key) {
            String stringValue = attributes.get(key);
            if ("true".equals(stringValue)) {
                return true;
            }
            if ("false".equals(stringValue)) {
                return false;
            }
            return null;
        }

        private View create(Context context, ViewGroup parent) throws Exception {
            // todo: clean this up [pg/xw 20101028] should applyAttributes always be called?
            if (name.equals("include")) {
                String layout = attributes.get("layout");
                View view = inflateView(context, layout.substring(1), attributes);
                addToParent(parent, view);
                return view;
            } else if (name.equals("merge")) {
                LinearLayout view = new LinearLayout(context);
                addToParent(parent, view);
                return view;
            } else {
                View view = constructView(context);
                addToParent(parent, view);
                applyAttributes(view);
                return view;
            }
        }

        private void addToParent(ViewGroup parent, View view) {
            if (parent != null) {
                parent.addView(view);
            }
        }

        private View constructView(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Class<? extends View> clazz = pickViewClass();
            try {
                TestAttributeSet attributeSet = new TestAttributeSet(attributes, resourceExtractor, attrResourceLoader, clazz);
                return ((Constructor<? extends View>) clazz.getConstructor(Context.class, AttributeSet.class)).newInstance(context, attributeSet);
            } catch (NoSuchMethodException e) {
                try {
                    return ((Constructor<? extends View>) clazz.getConstructor(Context.class)).newInstance(context);
                } catch (NoSuchMethodException e1) {
                    return ((Constructor<? extends View>) clazz.getConstructor(Context.class, String.class)).newInstance(context, "");
                }
            }
        }

        private Class<? extends View> pickViewClass() {
            Class<? extends View> clazz = loadClass(name);
            if (clazz == null) {
                clazz = loadClass("android.view." + name);
            }
            if (clazz == null) {
                clazz = loadClass("android.widget." + name);
            }
            if (clazz == null) {
                clazz = loadClass("com.google.android.maps." + name);
            }

            if (clazz == null) {
                throw new RuntimeException("couldn't find view class " + name);
            }
            return clazz;
        }

        private Class<? extends View> loadClass(String className) {
            try {
                //noinspection unchecked
                return (Class<? extends View>) getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}