package org.jetbrains.jet.lang.descriptors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.annotations.Annotated;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.NamespaceType;

/**
 * @author abreslav
 */
public interface NamespaceDescriptor extends Annotated, Named, DeclarationDescriptor {
    @NotNull
    JetScope getMemberScope();

    @NotNull
    NamespaceType getNamespaceType();
}