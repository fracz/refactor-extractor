/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.springframework.boot.cli.command.AbstractCommand;

/**
 * Spring Command Line Interface. This is the main entry-point for the Spring command line
 * application. This class will parse input arguments and delegate to a suitable
 * {@link Command} implementation based on the first argument.
 *
 * <p>
 * The '-d' and '--debug' switches are handled by this class, however, most argument
 * parsing is left to the {@link Command} implementation.
 *
 * @author Phillip Webb
 * @see #main(String...)
 * @see SpringCliException
 * @see Command
 */
public class SpringCli {

	public static final String CLI_APP = "spring";

	private static final Set<SpringCliException.Option> NO_EXCEPTION_OPTIONS = EnumSet
			.noneOf(SpringCliException.Option.class);

	private List<Command> commands = new ArrayList<Command>();

	private String displayName = CLI_APP + " ";

	private Map<String, Command> commandMap = new HashMap<String, Command>();

	/**
	 * Create a new {@link SpringCli} implementation with the default set of commands.
	 */
	public SpringCli(String... args) {
		for (CommandFactory factory : ServiceLoader.load(CommandFactory.class)) {
			for (Command command : factory.getCommands(this)) {
				register(command);
			}
		}
		addBaseCommands();
	}

	/**
	 * Set the command available to the CLI. Primarily used to support testing. NOTE: The
	 * 'help' command will be automatically provided in addition to this list.
	 * @param commands the commands to add
	 */
	public void setCommands(List<? extends Command> commands) {
		this.commandMap.clear();
		this.commands = new ArrayList<Command>(commands);
		for (Command command : commands) {
			this.commandMap.put(command.getName(), command);
		}
		addBaseCommands();
	}

	protected void addBaseCommands() {
		HelpCommand help = new HelpCommand();
		this.commands.add(0, help);
		this.commandMap.put(help.getName(), help);
		HintCommand hint = new HintCommand();
		this.commands.add(hint);
		this.commandMap.put(hint.getName(), hint);
	}

	/**
	 * Run the CLI and handle and errors.
	 * @param args the input arguments
	 * @return a return status code (non boot is used to indicate an error)
	 */
	public int runAndHandleErrors(String... args) {
		System.setProperty("java.awt.headless", Boolean.toString(true));
		String[] argsWithoutDebugFlags = removeDebugFlags(args);
		boolean debug = argsWithoutDebugFlags.length != args.length;
		try {
			run(argsWithoutDebugFlags);
			return 0;
		}
		catch (NoArgumentsException ex) {
			showUsage();
			return 1;
		}
		catch (Exception ex) {
			return handleError(debug, ex);
		}
	}

	private int handleError(boolean debug, Exception ex) {
		Set<SpringCliException.Option> options = NO_EXCEPTION_OPTIONS;
		if (ex instanceof SpringCliException) {
			options = ((SpringCliException) ex).getOptions();
		}
		if (!(ex instanceof NoHelpCommandArgumentsException)) {
			errorMessage(ex.getMessage());
		}
		if (options.contains(SpringCliException.Option.SHOW_USAGE)) {
			showUsage();
		}
		if (debug || options.contains(SpringCliException.Option.STACK_TRACE)) {
			printStackTrace(ex);
		}
		return 1;
	}

	/**
	 * The name of this tool when printed by the help command.
	 *
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName == null || displayName.length() == 0
				|| displayName.endsWith(" ") ? displayName : displayName + " ";
	}

	/**
	 * The name of this tool when printed by the help command.
	 *
	 * @return the displayName
	 */
	public String getDisplayName() {
		return this.displayName;
	}

	/**
	 * Parse the arguments and run a suitable command.
	 * @param args the arguments
	 * @throws Exception
	 */
	protected void run(String... args) throws Exception {
		if (args.length == 0) {
			throw new NoArgumentsException();
		}
		String commandName = args[0];
		String[] commandArguments = Arrays.copyOfRange(args, 1, args.length);
		Command command = find(commandName);
		if (command == null) {
			throw new NoSuchCommandException(commandName);
		}
		command.run(commandArguments);
	}

	public final Command find(String name) {
		return this.commandMap.get(name);
	}

	public void register(Command command) {
		String name = command.getName();
		Command existing = find(name);
		int index = this.commands.size() - 1;
		index = index >= 0 ? index : 0;
		if (existing != null) {
			index = this.commands.indexOf(existing);
			this.commands.set(index, command);
		}
		else {
			this.commands.add(index, command);
		}
		this.commandMap.put(name, command);
	}

	public void unregister(String name) {
		this.commands.remove(find(name));
	}

	public List<Command> getCommands() {
		return Collections.unmodifiableList(this.commands);
	}

	protected void showUsage() {
		Log.infoPrint("usage: " + this.displayName);
		for (Command command : this.commands) {
			if (command.isOptionCommand()) {
				Log.infoPrint("[--" + command.getName() + "] ");
			}
		}
		Log.info("");
		Log.info("       <command> [<args>]");
		Log.info("");
		Log.info("Available commands are:");
		for (Command command : this.commands) {
			if (!command.isOptionCommand() && !(command instanceof HintCommand)) {
				String usageHelp = command.getUsageHelp();
				String description = command.getDescription();
				Log.info(String.format("\n  %1$s %2$-15s\n    %3$s", command.getName(),
						(usageHelp == null ? "" : usageHelp), (description == null ? ""
								: description)));
			}
		}
		Log.info("");
		Log.info("Common options:");
		Log.info(String.format("\n  %1$s %2$-15s\n    %3$s", "-d, --debug",
				"Verbose mode",
				"Print additional status information for the command you are running"));
		Log.info("");
		Log.info("");
		Log.info("See '" + this.displayName
				+ "help <command>' for more information on a specific command.");
	}

	protected void errorMessage(String message) {
		Log.error(message == null ? "Unexpected error" : message);
	}

	protected void printStackTrace(Exception ex) {
		Log.error("");
		Log.error(ex);
		Log.error("");
	}

	private String[] removeDebugFlags(String[] args) {
		List<String> rtn = new ArrayList<String>(args.length);
		for (String arg : args) {
			if (!("-d".equals(arg) || "--debug".equals(arg))) {
				rtn.add(arg);
			}
		}
		return rtn.toArray(new String[rtn.size()]);
	}

	/**
	 * Internal {@link Command} used for 'help' requests.
	 */
	private class HelpCommand extends AbstractCommand {

		public HelpCommand() {
			super("help", "Get help on commands", true);
		}

		@Override
		public String getUsageHelp() {
			return "command";
		}

		@Override
		public String getHelp() {
			return null;
		}

		@Override
		public Collection<OptionHelp> getOptionsHelp() {
			List<OptionHelp> help = new ArrayList<OptionHelp>();
			for (final Command command : SpringCli.this.commands) {
				if (!(command instanceof HelpCommand)
						&& !(command instanceof HintCommand)) {
					help.add(new OptionHelp() {

						@Override
						public Set<String> getOptions() {
							return Collections.singleton(command.getName());
						}

						@Override
						public String getUsageHelp() {
							return "";
						}
					});
				}
			}
			return help;
		}

		@Override
		public void run(String... args) throws Exception {
			if (args.length == 0) {
				throw new NoHelpCommandArgumentsException();
			}
			String commandName = args[0];
			for (Command command : SpringCli.this.commands) {
				if (command.getName().equals(commandName)) {
					Log.info(SpringCli.this.displayName + command.getName() + " - "
							+ command.getDescription());
					Log.info("");
					if (command.getUsageHelp() != null) {
						Log.info("usage: " + SpringCli.this.displayName
								+ command.getName() + " " + command.getUsageHelp());
						Log.info("");
					}
					if (command.getHelp() != null) {
						Log.info(command.getHelp());
					}
					return;
				}
			}
			throw new NoSuchCommandException(commandName);
		}

	}

	/**
	 * Provides hints for shell auto-completion. Expects to be called with the current
	 * index followed by a list of arguments already typed.
	 */
	private class HintCommand extends AbstractCommand {

		public HintCommand() {
			super("hint", "Provides hints for shell auto-completion");
		}

		@Override
		public void run(String... args) throws Exception {
			try {
				int index = (args.length == 0 ? 0 : Integer.valueOf(args[0]) - 1);
				List<String> arguments = new ArrayList<String>(args.length);
				for (int i = 2; i < args.length; i++) {
					arguments.add(args[i]);
				}
				String starting = "";
				if (index < arguments.size()) {
					starting = arguments.remove(index);
				}
				if (index == 0) {
					showCommandHints(starting);
				}
				else if ((arguments.size() > 0) && (starting.length() > 0)) {
					String command = arguments.remove(0);
					showCommandOptionHints(command,
							Collections.unmodifiableList(arguments), starting);
				}
			}
			catch (Exception ex) {
				// Swallow and provide no hints
			}
		}

		private void showCommandHints(String starting) {
			for (Command command : SpringCli.this.commands) {
				if (isHintMatch(command, starting)) {
					Log.info(command.getName() + " " + command.getDescription());
				}
			}
		}

		private boolean isHintMatch(Command command, String starting) {
			if (command instanceof HintCommand) {
				return false;
			}
			return command.getName().startsWith(starting)
					|| (command.isOptionCommand() && ("--" + command.getName())
							.startsWith(starting));
		}

		private void showCommandOptionHints(String commandName,
				List<String> specifiedArguments, String starting) {
			Command command = find(commandName);
			if (command != null) {
				for (OptionHelp help : command.getOptionsHelp()) {
					if (!alreadyUsed(help, specifiedArguments)) {
						for (String option : help.getOptions()) {
							if (option.startsWith(starting)) {
								Log.info(option + " " + help.getUsageHelp());
							}
						}
					}
				}
			}
		}

		private boolean alreadyUsed(OptionHelp help, List<String> specifiedArguments) {
			for (String argument : specifiedArguments) {
				if (help.getOptions().contains(argument)) {
					return true;
				}
			}
			return false;
		}
	}

	static class NoHelpCommandArgumentsException extends SpringCliException {

		private static final long serialVersionUID = 1L;

		public NoHelpCommandArgumentsException() {
			super(Option.SHOW_USAGE);
		}

	}

	static class NoArgumentsException extends SpringCliException {

		private static final long serialVersionUID = 1L;

	}

	/**
	 * The main CLI entry-point.
	 * @param args CLI arguments
	 */
	public static void main(String... args) {
		String[] init = new String[1];
		int index = 0;
		String arg = args[0];
		if (arg.startsWith("--init")) {
			if (arg.contains("=") || args.length < 2) {
				init[0] = arg;
				index = 1;
			}
			else {
				init[0] = arg + "=" + args[1];
				index = 2;
			}
		}
		if (index > 0) {
			String[] newargs = new String[args.length - index];
			System.arraycopy(args, index, newargs, 0, newargs.length);
			args = newargs;
		}
		else {
			init = new String[0];
		}
		int exitCode = new SpringCli(init).runAndHandleErrors(args);
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}
}