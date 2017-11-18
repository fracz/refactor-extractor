/*
 * Copyright 2013-2014 the original author or authors.
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
package org.springframework.boot.cli.command.install;

import java.util.List;

import joptsimple.OptionSet;

import org.springframework.boot.cli.command.Command;
import org.springframework.boot.cli.command.OptionParsingCommand;
import org.springframework.boot.cli.command.options.CompilerOptionHandler;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.boot.cli.util.Log;

/**
 * {@link Command} to install additional dependencies into the CLI.
 *
 * @author Dave Syer
 * @author Andy Wilkinson
 * @since 1.2.0
 */
public class InstallCommand extends OptionParsingCommand {

	public InstallCommand() {
		super("install", "Install dependencies to the lib directory",
				new InstallOptionHandler());
	}

	@Override
	public String getUsageHelp() {
		return "[options] <coordinates>";
	}

	private static final class InstallOptionHandler extends CompilerOptionHandler {

		@Override
		protected ExitStatus run(OptionSet options) throws Exception {

			@SuppressWarnings("unchecked")
			List<String> args = (List<String>) options.nonOptionArguments();

			if (args.isEmpty()) {
				throw new IllegalArgumentException(
						"Please specify at least one dependency, in the form group:artifact:version, to install");
			}

			try {
				new Installer(options, this).install(args);
			}
			catch (Exception e) {
				String message = e.getMessage();
				Log.error(message != null ? message : e.getClass().toString());
			}

			return ExitStatus.OK;
		}

	}

}