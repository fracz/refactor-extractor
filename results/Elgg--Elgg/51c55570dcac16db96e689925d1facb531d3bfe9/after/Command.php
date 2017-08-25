<?php

namespace Elgg\Cli;

use Elgg\Http\OutputBufferTransport;
use RuntimeException;
use Symfony\Component\Console\Command\Command as SymfonyCommand;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

/**
 * Abstract command with some utility methods
 */
abstract class Command extends SymfonyCommand {

	use ConsoleInteractions;

	/**
	 * {@inheritdoc}
	 */
	final public function execute(InputInterface $input, OutputInterface $output) {
		$this->input = $input;
		$this->output = $output;

		elgg_set_config('debug', 'NOTICE');

		_elgg_services()->logger->setPrinter(function ($data, $level) {
			$tag = $level == 'ERROR' ? 'error' : 'info';
			$this->write(elgg_format_element($tag, [], $data));
		});

		_elgg_services()->responseFactory->setTransport(new ResponseTransport($this));

		try {
			$this->login();

			$result = $this->command();
			if (is_callable($result)) {
				$result = call_user_func($result, $this);
			}

			$this->dumpRegisters();

			$this->logout();
		} catch (\Exception $ex) {
			elgg_log($ex->getMessage(), 'ERROR');
			$result = $ex->getCode() ? : 1;
		}

		return (int) $result;
	}

	/**
	 * Command to be executed
	 *
	 * This method method should return an integer code of the error (or 0 for success).
	 * Optionally, the method can return a callable that will receive the instance of this command as an argument
	 *
	 * @return mixed
	 * @see Command::execute()
	 */
	abstract protected function command();

	/**
	 * Login a user defined by --as option
	 *
	 * @return void
	 * @throws RuntimeException
	 */
	final protected function login() {
		if (!$this->getDefinition()->hasOption('as')) {
			return;
		}
		$username = $this->option('as');
		if (!$username) {
			return;
		}
		$user = get_user_by_username($username);
		if (!$user) {
			throw new RuntimeException("User with username $username not found");
		}
		if (!login($user)) {
			throw new RuntimeException("Unable to login as $username");
		}
		elgg_log("Logged in as $username [guid: $user->guid]");
	}

	/**
	 * Logout a user
	 * @return void
	 */
	final protected function logout() {
		if (elgg_is_logged_in()) {
			logout();
		}
	}

}