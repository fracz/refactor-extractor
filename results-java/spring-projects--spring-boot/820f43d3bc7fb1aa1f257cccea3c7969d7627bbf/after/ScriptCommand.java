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

package org.springframework.boot.cli.command;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.Script;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import joptsimple.OptionParser;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.boot.cli.Command;
import org.springframework.boot.cli.OptionHelp;
import org.springframework.boot.cli.compiler.GroovyCompiler;
import org.springframework.boot.cli.compiler.GroovyCompilerConfiguration;
import org.springframework.boot.cli.compiler.GroovyCompilerScope;
import org.springframework.boot.cli.compiler.RepositoryConfigurationFactory;
import org.springframework.boot.cli.compiler.grape.RepositoryConfiguration;
import org.springframework.util.FileCopyUtils;

/**
 * {@link Command} to run a Groovy script.
 *
 * @author Dave Syer
 */
public class ScriptCommand implements Command {

	private static final String[] DEFAULT_PATHS = new String[] { "${SPRING_HOME}/ext",
			"${SPRING_HOME}/bin" };

	private String[] paths = DEFAULT_PATHS;

	private Class<?> mainClass;

	private Object main;

	private String name;

	public ScriptCommand(String script) {
		this.name = script;
	}

	@Override
	public String getName() {
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getName();
		}
		return this.name;
	}

	@Override
	public boolean isOptionCommand() {
		return false;
	}

	@Override
	public String getDescription() {
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getDescription();
		}
		return this.name;
	}

	@Override
	public String getHelp() {
		if (getMain() instanceof OptionHandler) {
			return ((OptionHandler) getMain()).getHelp();
		}
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getHelp();
		}
		return null;
	}

	@Override
	public Collection<OptionHelp> getOptionsHelp() {
		if (getMain() instanceof OptionHandler) {
			return ((OptionHandler) getMain()).getOptionsHelp();
		}
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getOptionsHelp();
		}
		return Collections.emptyList();
	}

	@Override
	public void run(String... args) throws Exception {
		run(getMain(), args);
	}

	private void run(Object main, String[] args) throws Exception {
		if (main instanceof Command) {
			((Command) main).run(args);
		}
		else if (main instanceof OptionHandler) {
			((OptionHandler) getMain()).run(args);
		}
		else if (main instanceof Closure) {
			((Closure<?>) main).call((Object[]) args);
		}
		else if (main instanceof Runnable) {
			((Runnable) main).run();
		}
		else if (main instanceof Script) {
			Script script = (Script) this.main;
			script.setProperty("args", args);
			if (this.main instanceof GroovyObjectSupport) {
				GroovyObjectSupport object = (GroovyObjectSupport) this.main;
				if (object.getMetaClass().hasProperty(object, "parser") != null) {
					OptionParser parser = (OptionParser) object.getProperty("parser");
					if (parser != null) {
						script.setProperty("options", parser.parse(args));
					}
				}
			}
			Object result = script.run();
			run(result, args);
		}
	}

	/**
	 * Paths to search for script files.
	 *
	 * @param paths the paths to set
	 */
	public void setPaths(String[] paths) {
		this.paths = (paths == null ? null : paths.clone());
	}

	@Override
	public String getUsageHelp() {
		if (getMain() instanceof Command) {
			return ((Command) getMain()).getDescription();
		}
		return "[options] <args>";
	}

	protected Object getMain() {
		if (this.main == null) {
			try {
				this.main = getMainClass().newInstance();
			}
			catch (Exception ex) {
				throw new IllegalStateException("Cannot create main class: " + this.name,
						ex);
			}
			if (this.main instanceof OptionHandler) {
				((OptionHandler) this.main).options();
			}
			else if (this.main instanceof GroovyObjectSupport) {
				GroovyObjectSupport object = (GroovyObjectSupport) this.main;
				MetaClass metaClass = object.getMetaClass();
				MetaMethod options = metaClass.getMetaMethod("options", null);
				if (options != null) {
					options.doMethodInvoke(this.main, null);
				}
			}
		}
		return this.main;
	}

	private void compile() {
		GroovyCompiler compiler = new GroovyCompiler(new ScriptConfiguration());
		compiler.addCompilationCustomizers(new ScriptCompilationCustomizer());
		File source = locateSource(this.name);
		Class<?>[] classes;
		try {
			classes = compiler.compile(source);
		}
		catch (CompilationFailedException ex) {
			throw new IllegalStateException("Could not compile script", ex);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not compile script", ex);
		}
		this.mainClass = classes[0];
	}

	private Class<?> getMainClass() {
		if (this.mainClass == null) {
			compile();
		}
		return this.mainClass;
	}

	private File locateSource(String name) {
		String resource = name;
		if (!name.endsWith(".groovy")) {
			resource = "commands/" + name + ".groovy";
		}

		URL url = getClass().getClassLoader().getResource(resource);
		if (url != null) {
			return locateSourceFromUrl(name, url);
		}

		String home = System.getProperty("SPRING_HOME", System.getenv("SPRING_HOME"));
		if (home == null) {
			home = ".";
		}

		for (String path : this.paths) {
			String subbed = path.replace("${SPRING_HOME}", home);
			File file = new File(subbed, resource);
			if (file.exists()) {
				return file;
			}
		}

		throw new IllegalStateException("No script found for : " + name);
	}

	private File locateSourceFromUrl(String name, URL url) {
		if (url.toString().startsWith("file:")) {
			return new File(url.toString().substring("file:".length()));
		}

		// probably in JAR file
		try {
			File file = File.createTempFile(name, ".groovy");
			file.deleteOnExit();
			FileCopyUtils.copy(url.openStream(), new FileOutputStream(file));
			return file;
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not create temp file for source: "
					+ name);
		}
	}

	private static class ScriptConfiguration implements GroovyCompilerConfiguration {

		@Override
		public GroovyCompilerScope getScope() {
			return GroovyCompilerScope.EXTENSION;
		}

		@Override
		public boolean isGuessImports() {
			return true;
		}

		@Override
		public boolean isGuessDependencies() {
			return true;
		}

		@Override
		public String[] getClasspath() {
			return NO_CLASSPATH;
		}

		@Override
		public List<RepositoryConfiguration> getRepositoryConfiguration() {
			return RepositoryConfigurationFactory.createDefaultRepositoryConfiguration();
		}

	}

}