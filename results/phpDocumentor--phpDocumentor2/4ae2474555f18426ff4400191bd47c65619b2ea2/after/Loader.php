<?php

namespace phpDocumentor\Configuration;

use JMS\Serializer\Serializer;
use phpDocumentor\Console\Input\ArgvInput;
use Symfony\Component\Console\Application;

class Loader
{
    /** @var \JMS\Serializer\Serializer */
    private $serializer;

    /** @var Merger */
    private $merger;

    /**
     * Registers the dependencies with the loader.
     *
     * @param Serializer  $serializer Object used to serialize configuration files to objects.
     * @param Merger      $merger     Object that merges variables, including objects.
     */
    public function __construct(Serializer $serializer, Merger $merger)
    {
        $this->serializer = $serializer;
        $this->merger     = $merger;
    }

    /**
     * Loads the configuration from the provided paths and returns a populated configuration object.
     *
     * @param string $templatePath          Path to configuration file containing default settings.
     * @param string $userConfigurationPath Path to a file containing user overrides.
     * @param string $class                 The class to instantiate and populate with these options.
     *
     * @return object
     */
    public function load($templatePath, $userConfigurationPath, $class = 'phpDocumentor\Configuration')
    {
        $input = new ArgvInput();
        $userConfigFilePath = $input->getParameterOption('config');

        if ($userConfigFilePath && $userConfigFilePath != 'none' && is_readable($userConfigFilePath)) {
            chdir(dirname($userConfigFilePath));
        } else {
            $userConfigFilePath = $userConfigFilePath != 'none' ? null : 'none';
        }

        $config = $this->serializer->deserialize(file_get_contents($templatePath), $class, 'xml');

        if ($userConfigFilePath != 'none') {
            $userConfigFilePath = $userConfigFilePath ?: $userConfigurationPath;
            $userConfigFile = $this->serializer->deserialize(file_get_contents($userConfigFilePath), $class, 'xml');

            $config = $this->merger->run($config, $userConfigFile);
        }

        return $config;
    }
}