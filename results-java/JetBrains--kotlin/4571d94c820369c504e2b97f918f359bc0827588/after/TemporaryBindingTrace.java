package org.jetbrains.jet.lang.resolve;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.CollectingErrorHandler;
import org.jetbrains.jet.lang.ErrorHandler;
import org.jetbrains.jet.lang.JetDiagnostic;
import org.jetbrains.jet.util.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author abreslav
 */
public class TemporaryBindingTrace implements BindingTrace {
    private final BindingContext parentContext;
    private final MutableManyMap map = ManyMapImpl.create();
    private final List<JetDiagnostic> diagnostics = Lists.newArrayList();
    private final ErrorHandler errorHandler = new CollectingErrorHandler(diagnostics);

    private final BindingContext bindingContext = new BindingContext() {
        @Override
        public Collection<JetDiagnostic> getDiagnostics() {
            throw new UnsupportedOperationException(); // TODO
        }

        @Override
        public <K, V> V get(ReadOnlySlice<K, V> slice, K key) {
            return TemporaryBindingTrace.this.get(slice, key);
        }
    };

    public TemporaryBindingTrace(BindingContext parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    @NotNull
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public BindingContext getBindingContext() {
        return bindingContext;
    }

    @Override
    public <K, V> void record(WritableSlice<K, V> slice, K key, V value) {
        map.put(slice, key, value);
    }

    @Override
    public <K> void record(WritableSlice<K, Boolean> slice, K key) {
        record(slice, key, true);
    }

    @Override
    public <K, V> V get(ReadOnlySlice<K, V> slice, K key) {
        if (map.containsKey(slice, key)) {
            return map.get(slice, key);
        }
        return parentContext.get(slice, key);
    }

    public void addAllMyDataTo(BindingTrace trace) {
        for (Map.Entry<ManyMapKey<?, ?>, ?> entry : map) {
            ManyMapKey manyMapKey = entry.getKey();
            Object value = entry.getValue();

            //noinspection unchecked
            trace.record(manyMapKey.getSlice(), manyMapKey.getKey(), value);
        }

        AnalyzingUtils.applyHandler(trace.getErrorHandler(), diagnostics);
    }
}