/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.cli.compiler;

import groovy.grape.GrapeEngine;
import groovy.lang.Grab;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyClassLoader.ClassCollector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.ASTTransformationVisitor;

/**
 * Compiler for Groovy source files. Primarily a simple Facade for
 * {@link GroovyClassLoader#parseClass(File)} with the following additional features:
 * <ul>
 * <li>{@link CompilerAutoConfiguration} strategies will be read from
 * <code>META-INF/services/org.springframework.boot.cli.compiler.CompilerAutoConfiguration</code>
 * (per the standard java {@link ServiceLoader} contract) and applied during compilation</li>
 *
 * <li>Multiple classes can be returned if the Groovy source defines more than one Class</li>
 *
 * <li>Generated class files can also be loaded using
 * {@link ClassLoader#getResource(String)}</li>
 * <ul>
 *
 * @author Phillip Webb
 * @author Dave Syer
 * @author Andy Wilkinson
 */
public class GroovyCompiler {

	private static final ClassLoader AETHER_CLASS_LOADER = new URLClassLoader(
			new URL[] { GroovyCompiler.class.getResource("/internal/") });

	private GroovyCompilerConfiguration configuration;

	private ExtendedGroovyClassLoader loader;

	private ArtifactCoordinatesResolver artifactCoordinatesResolver;

	private final ASTTransformation dependencyCustomizerTransformation = new DependencyCustomizerAstTransformation();

	private final ASTTransformation dependencyCoordinatesTransformation = new DefaultDependencyCoordinatesAstTransformation();

	/**
	 * Create a new {@link GroovyCompiler} instance.
	 * @param configuration the compiler configuration
	 */
	@SuppressWarnings("unchecked")
	public GroovyCompiler(final GroovyCompilerConfiguration configuration) {
		this.configuration = configuration;
		CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
		this.loader = new ExtendedGroovyClassLoader(getClass().getClassLoader(),
				compilerConfiguration);
		if (configuration.getClasspath().length() > 0) {
			this.loader.addClasspath(configuration.getClasspath());
		}
		this.artifactCoordinatesResolver = new PropertiesArtifactCoordinatesResolver(
				this.loader);

		try {
			Class<GrapeEngine> grapeEngineClass = (Class<GrapeEngine>) AETHER_CLASS_LOADER
					.loadClass("org.springframework.boot.cli.compiler.AetherGrapeEngine");
			Constructor<GrapeEngine> constructor = grapeEngineClass.getConstructor(
					GroovyClassLoader.class, String.class, String.class, String.class);
			GrapeEngine grapeEngine = constructor.newInstance(this.loader,
					"org.springframework.boot", "spring-boot-starter-parent",
					this.artifactCoordinatesResolver.getVersion("spring-boot"));

			new GrapeEngineInstaller(grapeEngine).install();
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to install custom GrapeEngine", ex);
		}

		compilerConfiguration
				.addCompilationCustomizers(new CompilerAutoConfigureCustomizer());
	}

	public void addCompilationCustomizers(CompilationCustomizer... customizers) {
		this.loader.getConfiguration().addCompilationCustomizers(customizers);
	}

	public Object[] sources(File... files) throws CompilationFailedException, IOException {
		List<File> compilables = new ArrayList<File>();
		List<Object> others = new ArrayList<Object>();
		for (File file : files) {
			if (file.getName().endsWith(".groovy") || file.getName().endsWith(".java")) {
				compilables.add(file);
			}
			else {
				others.add(file);
			}
		}
		Class<?>[] compiled = compile(compilables.toArray(new File[compilables.size()]));
		others.addAll(0, Arrays.asList(compiled));
		return others.toArray(new Object[others.size()]);
	}

	/**
	 * Compile the specified Groovy source files, applying any
	 * {@link CompilerAutoConfiguration}s. All classes defined in the files will be
	 * returned from this method.
	 * @param file the file to compile
	 * @return compiled classes
	 * @throws CompilationFailedException
	 * @throws IOException
	 */
	public Class<?>[] compile(File... file) throws CompilationFailedException,
			IOException {

		this.loader.clearCache();
		List<Class<?>> classes = new ArrayList<Class<?>>();

		CompilerConfiguration compilerConfiguration = this.loader.getConfiguration();

		final CompilationUnit compilationUnit = new CompilationUnit(
				compilerConfiguration, null, this.loader);
		SourceUnit sourceUnit = new SourceUnit(file[0], compilerConfiguration,
				this.loader, compilationUnit.getErrorCollector());
		ClassCollector collector = this.loader.createCollector(compilationUnit,
				sourceUnit);
		compilationUnit.setClassgenCallback(collector);

		compilationUnit.addSources(file);

		addAstTransformations(compilationUnit);

		compilationUnit.compile(Phases.CLASS_GENERATION);
		for (Object loadedClass : collector.getLoadedClasses()) {
			classes.add((Class<?>) loadedClass);
		}
		ClassNode mainClassNode = (ClassNode) compilationUnit.getAST().getClasses()
				.get(0);
		Class<?> mainClass = null;
		for (Class<?> loadedClass : classes) {
			if (mainClassNode.getName().equals(loadedClass.getName())) {
				mainClass = loadedClass;
			}
		}
		if (mainClass != null) {
			classes.remove(mainClass);
			classes.add(0, mainClass);
		}

		return classes.toArray(new Class<?>[classes.size()]);

	}

	@SuppressWarnings("rawtypes")
	private void addAstTransformations(final CompilationUnit compilationUnit) {
		try {
			Field field = CompilationUnit.class.getDeclaredField("phaseOperations");
			field.setAccessible(true);
			LinkedList[] phaseOperations = (LinkedList[]) field.get(compilationUnit);
			processConversionOperations(phaseOperations[Phases.CONVERSION]);
		}
		catch (Exception npe) {
			throw new IllegalStateException(
					"Phase operations not available from compilation unit");
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void processConversionOperations(LinkedList conversionOperations) {
		for (int i = 0; i < conversionOperations.size(); i++) {
			Object operation = conversionOperations.get(i);

			if (operation.getClass().getName()
					.startsWith(ASTTransformationVisitor.class.getName())) {
				conversionOperations.add(i, new CompilationUnit.SourceUnitOperation() {
					@Override
					public void call(SourceUnit source) throws CompilationFailedException {
						ASTNode[] astNodes = new ASTNode[] { source.getAST() };
						GroovyCompiler.this.dependencyCustomizerTransformation.visit(
								astNodes, source);
						GroovyCompiler.this.dependencyCoordinatesTransformation.visit(
								astNodes, source);
					}
				});
				break;
			}
		}
	}

	/**
	 * {@link CompilationCustomizer} to call {@link CompilerAutoConfiguration}s.
	 */
	private class CompilerAutoConfigureCustomizer extends CompilationCustomizer {

		public CompilerAutoConfigureCustomizer() {
			super(CompilePhase.CONVERSION);
		}

		@Override
		public void call(SourceUnit source, GeneratorContext context, ClassNode classNode)
				throws CompilationFailedException {

			ImportCustomizer importCustomizer = new ImportCustomizer();

			ServiceLoader<CompilerAutoConfiguration> customizers = ServiceLoader.load(
					CompilerAutoConfiguration.class,
					GroovyCompiler.class.getClassLoader());

			// Additional auto configuration
			for (CompilerAutoConfiguration autoConfiguration : customizers) {
				if (autoConfiguration.matches(classNode)) {
					if (GroovyCompiler.this.configuration.isGuessImports()) {
						autoConfiguration.applyImports(importCustomizer);
						importCustomizer.call(source, context, classNode);
					}
					if (source.getAST().getClasses().size() > 0
							&& classNode.equals(source.getAST().getClasses().get(0))) {
						autoConfiguration.applyToMainClass(GroovyCompiler.this.loader,
								GroovyCompiler.this.configuration, context, source,
								classNode);
					}
					autoConfiguration
							.apply(GroovyCompiler.this.loader,
									GroovyCompiler.this.configuration, context, source,
									classNode);
				}
			}
			importCustomizer.call(source, context, classNode);
		}

	}

	private class DependencyCustomizerAstTransformation implements ASTTransformation {

		@Override
		public void visit(ASTNode[] nodes, SourceUnit source) {

			ServiceLoader<CompilerAutoConfiguration> customizers = ServiceLoader.load(
					CompilerAutoConfiguration.class,
					GroovyCompiler.class.getClassLoader());

			for (ASTNode astNode : nodes) {
				if (astNode instanceof ModuleNode) {
					ModuleNode module = (ModuleNode) astNode;

					DependencyCustomizer dependencyCustomizer = new DependencyCustomizer(
							GroovyCompiler.this.loader, module,
							GroovyCompiler.this.artifactCoordinatesResolver);

					ClassNode firstClass = null;

					for (ClassNode classNode : module.getClasses()) {
						if (firstClass == null) {
							firstClass = classNode;
						}
						for (CompilerAutoConfiguration autoConfiguration : customizers) {
							if (autoConfiguration.matches(classNode)) {
								if (GroovyCompiler.this.configuration
										.isGuessDependencies()) {
									autoConfiguration
											.applyDependencies(dependencyCustomizer);
								}
							}
						}
					}
				}
			}
		}
	}

	private class DefaultDependencyCoordinatesAstTransformation implements
			ASTTransformation {

		@Override
		public void visit(ASTNode[] nodes, final SourceUnit source) {
			for (ASTNode node : nodes) {
				if (node instanceof ModuleNode) {
					ModuleNode module = (ModuleNode) node;
					for (ImportNode importNode : module.getImports()) {
						visitAnnotatedNode(importNode);
					}
					for (ClassNode classNode : module.getClasses()) {
						visitAnnotatedNode(classNode);
						classNode.visitContents(new ClassCodeVisitorSupport() {
							@Override
							protected SourceUnit getSourceUnit() {
								return source;
							}

							@Override
							public void visitAnnotations(AnnotatedNode node) {
								visitAnnotatedNode(node);
							}
						});
					}
				}
			}
		}

		private void visitAnnotatedNode(AnnotatedNode annotatedNode) {
			for (AnnotationNode annotationNode : annotatedNode.getAnnotations()) {
				if (isGrabAnnotation(annotationNode)) {
					transformGrabAnnotation(annotationNode);
				}
			}
		}

		private boolean isGrabAnnotation(AnnotationNode annotationNode) {
			String annotationClassName = annotationNode.getClassNode().getName();
			return annotationClassName.equals(Grab.class.getName())
					|| annotationClassName.equals(Grab.class.getSimpleName());
		}

		private void transformGrabAnnotation(AnnotationNode grabAnnotation) {
			grabAnnotation.setMember("initClass", new ConstantExpression(false));

			Expression valueExpression = grabAnnotation.getMember("value");
			String value = null;
			if (valueExpression instanceof ConstantExpression) {
				ConstantExpression constantExpression = (ConstantExpression) valueExpression;
				Object valueObject = constantExpression.getValue();
				if (valueObject instanceof String) {
					value = (String) valueObject;
					if (isConvenienceForm(value)) {
						return;
					}
				}
			}

			applyGroupAndVersion(grabAnnotation, value);
		}

		private boolean isConvenienceForm(String value) {
			return value.contains(":") || value.contains("#");
		}

		private void applyGroupAndVersion(AnnotationNode grabAnnotation, String module) {

			if (module != null) {
				grabAnnotation.setMember("module", new ConstantExpression(module));
			}
			else {
				module = (String) ((ConstantExpression) grabAnnotation.getMembers().get(
						"module")).getValue();
			}

			if (grabAnnotation.getMember("group") == null) {
				ConstantExpression groupIdExpression = new ConstantExpression(
						GroovyCompiler.this.artifactCoordinatesResolver
								.getGroupId(module));
				grabAnnotation.setMember("group", groupIdExpression);
			}

			if (grabAnnotation.getMember("version") == null) {
				ConstantExpression versionExpression = new ConstantExpression(
						GroovyCompiler.this.artifactCoordinatesResolver
								.getVersion(module));
				grabAnnotation.setMember("version", versionExpression);
			}
		}
	}
}