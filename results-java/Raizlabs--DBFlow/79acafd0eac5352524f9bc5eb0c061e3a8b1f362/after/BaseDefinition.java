package com.raizlabs.android.dbflow.processor.definition;

import com.raizlabs.android.dbflow.processor.model.ProcessorManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.Arrays;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Description: Holds onto a common-set of fields and provides a common-set of methods to output class files.
 */
public abstract class BaseDefinition implements TypeDefinition {

    public final ProcessorManager manager;

    public ClassName elementClassName;
    public TypeName elementTypeName;
    public ClassName outputClassName;

    public Element element;
    public TypeElement typeElement;
    public String elementName;

    public String packageName;

    public BaseDefinition(Element element, ProcessorManager processorManager) {
        this.manager = processorManager;
        this.element = element;
        elementTypeName = TypeName.get(element.asType());
        elementName = element.getSimpleName().toString();
        packageName = manager.getElements().getPackageOf(element).toString();
    }

    public BaseDefinition(TypeElement element, ProcessorManager processorManager) {
        this.manager = processorManager;
        this.typeElement = element;
        elementClassName = ClassName.get(typeElement);
        elementName = element.getSimpleName().toString();
        packageName = manager.getElements().getPackageOf(element).toString();
    }

    protected void setOutputClassName(String postfix) {
        outputClassName = ClassName.get(elementClassName.packageName(), elementClassName.simpleName() + postfix);
    }

    @Override
    public TypeSpec getTypeSpec() {
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(outputClassName.simpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterfaces(Arrays.asList(getImplementsClasses()));
        TypeName extendsClass = getExtendsClass();
        if (extendsClass != null) {
            typeBuilder.superclass(extendsClass);
        }
        typeBuilder.addJavadoc("This is generated code. Please do not modify");
        onWriteDefinition(typeBuilder);
        return typeBuilder.build();
    }

    public String getSourceFileName() {
        return packageName + "." + definitionClassName;
    }

    public ProcessorManager getManager() {
        return manager;
    }

    protected TypeName getExtendsClass() {
        return null;
    }

    protected TypeName[] getImplementsClasses() {
        return new TypeName[0];
    }

    public void onWriteDefinition(TypeSpec.Builder typeBuilder) {

    }
}