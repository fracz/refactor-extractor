<?php

/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Rocketeer\Traits\BashModules;

use Illuminate\Support\Arr;
use Illuminate\Support\Str;
use Rocketeer\Traits\HasLocator;
use Rocketeer\Traits\Properties\HasHistory;

/**
 * Core handling of running commands and returning output.
 *
 * @mixin Binaries
 * @mixin Filesystem
 * @mixin Flow
 *
 * @author Maxime Fabre <ehtnam6@gmail.com>
 */
trait Core
{
    use HasLocator;
    use HasHistory;

    /**
     * Whether to run the commands locally
     * or on the server.
     *
     * @var bool
     */
    protected $local = false;

    /**
     * @var string
     */
    protected $extraOutput = null;

    //////////////////////////////////////////////////////////////////////
    /////////////////////////////// LOCAL ////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    /**
     * @param bool $local
     */
    public function setLocal($local)
    {
        $this->local = $local;
    }

    /**
     * Get which Connection to call commands with.
     *
     * @return \Rocketeer\Services\Connections\Connections\ConnectionInterface
     */
    public function getConnection()
    {
        // Return LocalConnection if in local mode
        if ($this->local || $this->rocketeer->isLocal()) {
            return $this->app->get('remote.local');
        }

        return $this->remote;
    }

    /**
     * Run a series of commands in local.
     *
     * @param callable $callback
     *
     * @return bool
     */
    public function onLocal(callable $callback)
    {
        $current = $this->rocketeer->isLocal();

        $this->rocketeer->setLocal(true);
        $results = $callback($this);
        $this->rocketeer->setLocal($current);

        return $results;
    }

    /**
     * Rune actions locally.
     *
     * @param string|array $commands
     *
     * @return string|null
     */
    public function runLocally($commands)
    {
        return $this->onLocal(function () use ($commands) {
            return $this->run($commands);
        });
    }

    ////////////////////////////////////////////////////////////////////
    ///////////////////////////// CORE METHODS /////////////////////////
    ////////////////////////////////////////////////////////////////////

    /**
     * Run actions on the remote server and gather the ouput.
     *
     * @param string|array $commands One or more commands
     * @param bool         $silent   Whether the command should stay silent no matter what
     * @param bool         $array    Whether the output should be returned as an array
     *
     * @return string|null
     */
    public function run($commands, $silent = false, $array = false)
    {
        $commands = $this->processCommands($commands);
        $verbose = $this->getOption('verbose') && !$silent;
        $pretend = $this->getOption('pretend');

        // Gather any extra output of the server
        // to be able to clean it up after
        if ($this->extraOutput === null && !$pretend) {
            $this->extraOutput = $this->getExtraOutput();
        }

        // Log the commands
        if (!$silent) {
            $this->toHistory($commands);
        }

        // Display for pretend mode
        if ($verbose || ($pretend && !$silent)) {
            $this->toOutput($commands);
            $this->displayCommands($commands);

            if ($pretend) {
                return count($commands) === 1 ? $commands[0] : $commands;
            }
        }

        // Run commands
        $output = null;
        $this->getConnection()->run($commands, function ($results) use (&$output, $verbose) {
            $output .= $results;

            if ($verbose) {
                $display = $this->cleanOutput($results);
                $this->explainer->server(trim($display));
            }
        });

        // Process and log the output and commands
        $output = $this->processOutput($output, $array, true);
        $this->toOutput($output);

        return $output;
    }

    /**
     * Run a command get the last line output to
     * prevent noise.
     *
     * @param string $commands
     *
     * @return string
     */
    public function runLast($commands)
    {
        $results = $this->runRaw($commands, true);
        $results = end($results);

        return $results;
    }

    /**
     * Run a raw command, without any processing, and
     * get its output as a string or array.
     *
     * @param string $commands
     * @param bool   $array    Whether the output should be returned as an array
     * @param bool   $trim     Whether the output should be trimmed
     *
     * @return string|string[]
     */
    public function runRaw($commands, $array = false, $trim = false)
    {
        $this->displayCommands($commands, 4);

        // Run commands
        $output = null;
        $this->getConnection()->run($commands, function ($results) use (&$output) {
            $output .= $results;
        });

        // Process the output
        $output = $this->processOutput($output, $array, $trim);

        return $output;
    }

    /**
     * Run commands silently.
     *
     * @param string|array $commands
     * @param bool         $array
     *
     * @return string|null
     */
    public function runSilently($commands, $array = false)
    {
        return $this->run($commands, true, $array);
    }

    /**
     * Run commands in a folder.
     *
     * @param string|null  $folder
     * @param string|array $tasks
     *
     * @return string|null
     */
    public function runInFolder($folder = null, $tasks = [])
    {
        // Convert to array
        if (!is_array($tasks)) {
            $tasks = [$tasks];
        }

        // Prepend folder
        array_unshift($tasks, 'cd '.$this->paths->getFolder($folder));

        return $this->run($tasks);
    }

    /**
     * Check the status of the last command.
     *
     * @return bool
     */
    public function status()
    {
        return $this->getOption('pretend') ? true : $this->getConnection()->status() === 0;
    }

    /**
     * Check the status of the last run command, return an error if any.
     *
     * @param string      $error   The message to display on error
     * @param string|null $output  The command's output
     * @param string|null $success The message to display on success
     *
     * @return bool
     */
    public function checkStatus($error, $output = null, $success = null)
    {
        // If all went well
        if ($this->status()) {
            if ($success) {
                $this->explainer->success($success);
            }

            return $output || true;
        }

        // Else display the error
        $error = sprintf('An error occured: "%s"', $error);
        if ($output) {
            $error .= ', while running:'.PHP_EOL.$output;
        }

        $this->explainer->error($error);

        return false;
    }

    ////////////////////////////////////////////////////////////////////
    /////////////////////////////// HELPERS ////////////////////////////
    ////////////////////////////////////////////////////////////////////

    /**
     * Get the current timestamp on the server.
     *
     * @return string
     */
    public function getTimestamp()
    {
        $timestamp = $this->runLast('date +"%Y%m%d%H%M%S"');
        $timestamp = trim($timestamp);
        $timestamp = preg_match('/^[0-9]{14}$/', $timestamp) ? $timestamp : date('YmdHis');

        return $timestamp;
    }

    /**
     * @return string
     */
    public function getExtraOutput()
    {
        $output = $this->runRaw([$this->shellCommand('echo ROCKETEER')]);
        $output = str_replace('ROCKETEER', null, $output);
        if ($output === "\n") {
            return;
        }

        return str_replace("\n\n", "\n", $output);
    }

    ////////////////////////////////////////////////////////////////////
    ///////////////////////////// PROCESSORS ///////////////////////////
    ////////////////////////////////////////////////////////////////////

    /**
     * Display the passed commands.
     *
     * @param string|array $commands
     * @param int          $verbosity
     */
    protected function displayCommands($commands, $verbosity = 1)
    {
        // Format command and verbosity level
        $flattened = (array) $commands;
        $flattened = implode(PHP_EOL.'$ ', $flattened);

        // Print out command if verbosity level allows it
        if ($verbosity && $this->hasCommand() && ($this->command->getVerbosity() >= $verbosity)) {
            $this->explainer->line('$ '.$flattened, 'magenta', false);
        }
    }

    /**
     * Process an array of commands.
     *
     * @param string|array $commands
     *
     * @return array
     */
    public function processCommands($commands)
    {
        $separator = $this->environment->getSeparator();
        $shell = $this->rocketeer->getOption('remote.shell');
        $shelled = $this->rocketeer->getOption('remote.shelled');
        $sudo = $this->rocketeer->getOption('remote.sudo');
        $sudoed = $this->rocketeer->getOption('remote.sudoed');

        // Prepare paths replacer
        $pattern = sprintf('#\%s([\w\d\s])#', DS);
        $replacement = sprintf('\%s$1', $separator);

        // Cast commands to array
        if (!is_array($commands)) {
            $commands = [$commands];
        }

        // Flatten and process commands
        $commands = Arr::flatten($commands);
        foreach ($commands as &$command) {
            // Replace directory separators
            if (DS !== $separator) {
                $command = preg_replace($pattern, $replacement, $command);
            }

            // Let framework process commands
            if ($framework = $this->getFramework()) {
                $command = $framework->processCommand($command);
            }

            // Create shell if asked
            if ($shell && Str::contains($command, $shelled)) {
                $command = $this->shellCommand($command);
            }

            if ($sudo && Str::contains($command, $sudoed)) {
                $command = $this->sudoCommand($sudo, $command);
            }
        }

        return $commands;
    }

    /**
     * Clean the output of various intruding bits.
     *
     * @param string $output
     *
     * @return string
     */
    protected function cleanOutput($output)
    {
        $output = str_replace($this->extraOutput, null, $output);

        return strtr($output, [
            'stdin: is not a tty' => null,
        ]);
    }

    /**
     * Pass a command through shell execution.
     *
     * @param string $command
     *
     * @return string
     */
    protected function shellCommand($command)
    {
        return "bash --login -c '".$command."'";
    }

    /**
     * Execute a command as a sudo user.
     *
     * @param string|bool $sudo
     * @param strign      $command
     *
     * @return string
     */
    protected function sudoCommand($sudo, $command)
    {
        $sudo = is_bool($sudo) ? 'sudo' : 'sudo -u '.$sudo;
        $command = $sudo.' '.$command;

        return $command;
    }

    /**
     * Process the output of a command.
     *
     * @param string $output
     * @param bool   $array  Whether to return an array or a string
     * @param bool   $trim   Whether to trim the output or not
     *
     * @return string|array
     */
    protected function processOutput($output, $array = false, $trim = true)
    {
        // Remove polluting strings
        $output = $this->cleanOutput($output);

        // Explode output if necessary
        if ($array) {
            $delimiter = $this->environment->getLineEndings() ?: PHP_EOL;
            $output = explode($delimiter, $output);
        }

        // Trim output
        if ($trim) {
            $output = is_array($output)
                ? array_filter($output)
                : trim($output);
        }

        return $output;
    }
}