<?php

/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 *
 */

namespace Rocketeer\Services\Connections\Credentials;

use Rocketeer\Traits\ContainerAwareTrait;

class CredentialsGatherer
{
    use ContainerAwareTrait;

    /**
     * @var array
     */
    protected $connections = [];

    /**
     * @return array
     */
    public function getCredentials()
    {
        $credentials = $this->getRepositoryCredentials();
        $connections = $this->getConnectionsCredentials();
        foreach ($connections as $connection) {
            $credentials = array_merge($credentials, $connection);
        }

        return $credentials;
    }

    /**
     * Get the Repository's credentials.
     */
    public function getRepositoryCredentials()
    {
        $credentials = [
            'SCM_REPOSITORY' => 'Where is you code located? <comment>(eg. git@github.com:rocketeers/website.git)</comment>',
            'SCM_USERNAME' => 'What is the username for it?',
            'SCM_PASSWORD' => 'And the password?',
        ];

        foreach ($credentials as $credential => $question) {
            $credentials[$credential] = $this->command->ask($question);

            // If the repository uses SSH, do not ask for username/password
            if ($credential === 'SCM_REPOSITORY' && strpos($credentials[$credential], 'git@') !== false) {
                break;
            }
        }

        return $credentials;
    }

    /**
     * Get the credentials of all connections
     *
     * @return array
     */
    public function getConnectionsCredentials()
    {
        $connectionName = null;
        if ($this->connections) {
            $this->command->writeln('Here are the current connections defined:');
            $this->command->table(['Name', 'Server', 'Username', 'Password'], $this->connections);
            if ($this->command->confirm('Do you want to add a connection to this?')) {
                $connectionName = $this->command->ask('What do you want to name it?');
            }
        } else {
            $connectionName = $this->command->ask('No connections have been set, let\'s create one, what do you want to name it?', 'production');
        }

        // If the user does not want to add any more connection
        // then we can quit
        if (!$connectionName) {
            return $this->connections;
        }

        $this->getConnectionCredentials($connectionName);

        return $this->getConnectionsCredentials();
    }

    /**
     * Get the credentials of a connection
     *
     * @param string $connectionName
     */
    public function getConnectionCredentials($connectionName)
    {
        $uppercased = strtoupper($connectionName);
        $privateKey = $this->command->confirm('Do you use an SSH key to connect to it?');

        $credentials = $privateKey ? [
            $uppercased.'_KEY' => ['Where can I find your key?', $this->paths->getUserHomeFolder().'/.ssh/id_rsa.pub'],
            $uppercased.'_HOST' => 'Where is your server located?',
            $uppercased.'_USERNAME' => 'What is the username for it?',
            $uppercased.'_ROOT' => ['Where do you want your application deployed?', '/home/www/'],
        ] : [
            $uppercased.'_HOST' => 'Where is your server located?',
            $uppercased.'_USERNAME' => 'What is the username for it?',
            $uppercased.'_PASSWORD' => 'And password?',
            $uppercased.'_ROOT' => ['Where do you want your application deployed?', '/home/www/'],
        ];

        foreach ($credentials as $credential => $question) {
            $question = (array) $question;
            $question[0] = '<fg=magenta>['.$connectionName.']</fg=magenta> '.$question[0];

            $credentials[$credential] = $this->command->ask(...$question);
        }

        $this->connections[$connectionName] = $credentials;
    }
}