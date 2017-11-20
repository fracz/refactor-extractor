package com.typesafe.config.impl;

import java.util.Collection;
import java.util.Collections;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValueType;

/**
 * ConfigReference replaces ConfigReference (the older class kept for back
 * compat) and represents the ${} substitution syntax. It can resolve to any
 * kind of value.
 */
final class ConfigReference extends AbstractConfigValue implements Unmergeable {
    private static final long serialVersionUID = 1L;

    final private SubstitutionExpression expr;
    // the length of any prefixes added with relativized()
    final private int prefixLength;

    ConfigReference(ConfigOrigin origin, SubstitutionExpression expr) {
        this(origin, expr, 0);
    }

    private ConfigReference(ConfigOrigin origin, SubstitutionExpression expr, int prefixLength) {
        super(origin);
        this.expr = expr;
        this.prefixLength = prefixLength;
    }

    private ConfigException.NotResolved notResolved() {
        return new ConfigException.NotResolved(
                "need to Config#resolve(), see the API docs for Config#resolve(); substitution not resolved: "
                        + this);
    }

    @Override
    public ConfigValueType valueType() {
        throw notResolved();
    }

    @Override
    public Object unwrapped() {
        throw notResolved();
    }

    @Override
    protected ConfigReference newCopy(ConfigOrigin newOrigin) {
        return new ConfigReference(newOrigin, expr, prefixLength);
    }

    @Override
    protected boolean ignoresFallbacks() {
        return false;
    }

    @Override
    public Collection<ConfigReference> unmergedValues() {
        return Collections.singleton(this);
    }

    // ConfigReference should be a firewall against NotPossibleToResolve going
    // further up the stack; it should convert everything to ConfigException.
    // This way it's impossible for NotPossibleToResolve to "escape" since
    // any failure to resolve has to start with a ConfigReference.
    @Override
    AbstractConfigValue resolveSubstitutions(ResolveContext context) {
        context.source().replace(this, ResolveReplacer.cycleResolveReplacer);
        try {
            AbstractConfigValue v;
            try {
                v = context.source().lookupSubst(context, expr, prefixLength);
            } catch (NotPossibleToResolve e) {
                if (expr.optional())
                    v = null;
                else
                    throw new ConfigException.UnresolvedSubstitution(origin(), expr
                            + " was part of a cycle of substitutions involving " + e.traceString(),
                            e);
            }

            if (v == null && !expr.optional()) {
                throw new ConfigException.UnresolvedSubstitution(origin(), expr.toString());
            }
            return v;
        } finally {
            context.source().unreplace(this);
        }
    }

    @Override
    ResolveStatus resolveStatus() {
        return ResolveStatus.UNRESOLVED;
    }

    // when you graft a substitution into another object,
    // you have to prefix it with the location in that object
    // where you grafted it; but save prefixLength so
    // system property and env variable lookups don't get
    // broken.
    @Override
    ConfigReference relativized(Path prefix) {
        SubstitutionExpression newExpr = expr.changePath(expr.path().prepend(prefix));
        return new ConfigReference(origin(), newExpr, prefixLength + prefix.length());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean canEqual(Object other) {
        return other instanceof ConfigReference || other instanceof ConfigSubstitution;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean equals(Object other) {
        // note that "origin" is deliberately NOT part of equality
        if (other instanceof ConfigReference) {
            return canEqual(other) && this.expr.equals(((ConfigReference) other).expr);
        } else if (other instanceof ConfigSubstitution) {
            return equals(((ConfigSubstitution) other).delegate());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        // note that "origin" is deliberately NOT part of equality
        return expr.hashCode();
    }

    @Override
    protected void render(StringBuilder sb, int indent, boolean formatted) {
        sb.append(expr.toString());
    }

    SubstitutionExpression expression() {
        return expr;
    }
}