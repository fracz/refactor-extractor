package org.jetbrains.jet.lang.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.TypeParameterDescriptor;
import org.jetbrains.jet.lang.resolve.scopes.SubstitutingScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author abreslav
 */
public class TypeSubstitutor {

    public interface TypeSubstitution {
        TypeSubstitution EMPTY = new TypeSubstitution() {
            @Override
            public TypeProjection get(TypeConstructor key) {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return true;
            }
        };

        @Nullable
        TypeProjection get(TypeConstructor key);
        boolean isEmpty();
    }

    public static class MapToTypeSubstitutionAdapter implements TypeSubstitution {
        private final @NotNull Map<TypeConstructor, TypeProjection> substitutionContext;

        public MapToTypeSubstitutionAdapter(@NotNull Map<TypeConstructor, TypeProjection> substitutionContext) {
            this.substitutionContext = substitutionContext;
        }

        @Override
        public TypeProjection get(TypeConstructor key) {
            return substitutionContext.get(key);
        }

        @Override
        public boolean isEmpty() {
            return substitutionContext.isEmpty();
        }
    }

    public static final TypeSubstitutor EMPTY = create(TypeSubstitution.EMPTY);

    public static final class SubstitutionException extends Exception {
        public SubstitutionException(String message) {
            super(message);
        }
    }

    public static TypeSubstitutor create(@NotNull TypeSubstitution substitution) {
        return new TypeSubstitutor(substitution);
    }

    public static TypeSubstitutor create(@NotNull TypeSubstitution... substitutions) {
        return create(new CompositeTypeSubstitution(substitutions));
    }

    public static TypeSubstitutor create(@NotNull Map<TypeConstructor, TypeProjection> substitutionContext) {
        return create(new MapToTypeSubstitutionAdapter(substitutionContext));
    }

    public static TypeSubstitutor create(@NotNull JetType context) {
        return create(TypeUtils.buildSubstitutionContext(context));
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final @NotNull TypeSubstitution substitution;

    protected TypeSubstitutor(@NotNull TypeSubstitution substitution) {
        this.substitution = substitution;
    }

    public boolean inRange(@NotNull TypeConstructor typeConstructor) {
        return substitution.get(typeConstructor) != null;
    }

    public boolean isEmpty() {
        return substitution.isEmpty();
    }

    @NotNull
    public TypeSubstitution getSubstitution() {
        return substitution;
    }

    @NotNull
    public JetType safeSubstitute(@NotNull JetType type, @NotNull Variance howThisTypeIsUsed) {
        if (isEmpty()) {
            return type;
        }

        try {
            return unsafeSubstitute(type, howThisTypeIsUsed);
        } catch (SubstitutionException e) {
            return ErrorUtils.createErrorType(e.getMessage());
        }
    }

    @Nullable
    public JetType substitute(@NotNull JetType type, @NotNull Variance howThisTypeIsUsed) {
        if (isEmpty()) {
            return type;
        }

        try {
            return unsafeSubstitute(type, howThisTypeIsUsed);
        } catch (SubstitutionException e) {
            return null;
        }
    }

    @NotNull
    private JetType unsafeSubstitute(@NotNull JetType type, @NotNull Variance howThisTypeIsUsed) throws SubstitutionException {
        if (ErrorUtils.isErrorType(type)) return type;

        TypeProjection value = getValueWithCorrectNullability(substitution, type);
        if (value != null) {
            TypeConstructor constructor = type.getConstructor();
            assert constructor.getDeclarationDescriptor() instanceof TypeParameterDescriptor;

            TypeParameterDescriptor typeParameterDescriptor = (TypeParameterDescriptor) constructor.getDeclarationDescriptor();

            TypeProjection result = substitutionResult(typeParameterDescriptor, howThisTypeIsUsed, Variance.INVARIANT, value);

            return TypeUtils.makeNullableIfNeeded(result.getType(), type.isNullable());
        }

        return specializeType(type, howThisTypeIsUsed);
    }

    private TypeProjection getValueWithCorrectNullability(TypeSubstitution substitution, JetType type) {
        TypeProjection typeProjection = substitution.get(type.getConstructor());
        if (typeProjection == null) return null;

        return type.isNullable() ? makeNullableProjection(typeProjection) : typeProjection;
    }

    @NotNull
    private static TypeProjection makeNullableProjection(@NotNull TypeProjection value) {
        return new TypeProjection(value.getProjectionKind(), TypeUtils.makeNullable(value.getType()));
    }

    private JetType specializeType(JetType subjectType, Variance callSiteVariance) throws SubstitutionException {
        if (ErrorUtils.isErrorType(subjectType)) return subjectType;

        List<TypeProjection> newArguments = new ArrayList<TypeProjection>();
        List<TypeProjection> arguments = subjectType.getArguments();
        for (int i = 0, argumentsSize = arguments.size(); i < argumentsSize; i++) {
            TypeProjection argument = arguments.get(i);
            TypeParameterDescriptor parameterDescriptor = subjectType.getConstructor().getParameters().get(i);
            newArguments.add(substituteInProjection(
                    substitution,
                    argument,
                    parameterDescriptor,
                    callSiteVariance));
        }
        return new JetTypeImpl(
                subjectType.getAnnotations(),
                subjectType.getConstructor(),
                subjectType.isNullable(),
                newArguments,
                new SubstitutingScope(subjectType.getMemberScope(), this));
    }

    @NotNull
    private TypeProjection substituteInProjection(
            @NotNull TypeSubstitution substitutionContext,
            @NotNull TypeProjection passedProjection,
            @NotNull TypeParameterDescriptor correspondingTypeParameter,
            @NotNull Variance contextCallSiteVariance) throws SubstitutionException {
        JetType typeToSubstituteIn = passedProjection.getType();
        if (ErrorUtils.isErrorType(typeToSubstituteIn)) return passedProjection;

        Variance passedProjectionKind = passedProjection.getProjectionKind();
        Variance parameterVariance = correspondingTypeParameter.getVariance();

        Variance effectiveProjectionKind = asymmetricOr(passedProjectionKind, parameterVariance);
        Variance effectiveContextVariance = contextCallSiteVariance.superpose(effectiveProjectionKind);

        TypeProjection projectionValue = getValueWithCorrectNullability(substitutionContext, typeToSubstituteIn);
        if (projectionValue != null) {
            assert typeToSubstituteIn.getConstructor().getDeclarationDescriptor() instanceof TypeParameterDescriptor;

            if (!allows(parameterVariance, passedProjectionKind)) {
                return TypeUtils.makeStarProjection(correspondingTypeParameter);
            }

            return substitutionResult(correspondingTypeParameter, effectiveContextVariance, passedProjectionKind, projectionValue);
        }
        return new TypeProjection(
                passedProjectionKind,
                specializeType(
                        typeToSubstituteIn,
                        effectiveContextVariance));
    }

    private TypeProjection substitutionResult(
            TypeParameterDescriptor correspondingTypeParameter,
            Variance effectiveContextVariance,
            Variance passedProjectionKind,
            TypeProjection value) throws SubstitutionException {
        Variance projectionKindValue = value.getProjectionKind();
        JetType typeValue = value.getType();
        Variance effectiveProjectionKindValue = asymmetricOr(passedProjectionKind, projectionKindValue);
        JetType effectiveTypeValue;
        switch (effectiveContextVariance) {
            case INVARIANT:
                effectiveProjectionKindValue = projectionKindValue;
                effectiveTypeValue = typeValue;
                break;
            case IN_VARIANCE:
                if (projectionKindValue == Variance.OUT_VARIANCE) {
                    throw new SubstitutionException(""); // TODO
//                    effectiveProjectionKindValue = Variance.INVARIANT;
//                    effectiveTypeValue = JetStandardClasses.getNothingType();
                }
                else {
                    effectiveTypeValue = typeValue;
                }
                break;
            case OUT_VARIANCE:
                if (projectionKindValue == Variance.IN_VARIANCE) {
                    effectiveProjectionKindValue = Variance.INVARIANT;
                    effectiveTypeValue = correspondingTypeParameter.getUpperBoundsAsType();
                }
                else {
                    effectiveTypeValue = typeValue;
                }
                break;
            default:
                throw new IllegalStateException(effectiveContextVariance.toString());
        }

//            if (!allows(effectiveContextVariance, projectionKindValue)) {
//                throw new SubstitutionException(""); // TODO : error message
//            }
//
        return new TypeProjection(effectiveProjectionKindValue, specializeType(effectiveTypeValue, effectiveContextVariance));
    }

    private static Variance asymmetricOr(Variance a, Variance b) {
        return a == Variance.INVARIANT ? b : a;
    }

    private static boolean allows(Variance declarationSiteVariance, Variance callSiteVariance) {
        switch (declarationSiteVariance) {
            case INVARIANT: return true;
            case IN_VARIANCE: return callSiteVariance != Variance.OUT_VARIANCE;
            case OUT_VARIANCE: return callSiteVariance != Variance.IN_VARIANCE;
        }
        throw new IllegalStateException(declarationSiteVariance.toString());
    }
}