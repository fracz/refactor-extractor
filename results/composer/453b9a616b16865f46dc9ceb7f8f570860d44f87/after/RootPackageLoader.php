<?php

/*
 * This file is part of Composer.
 *
 * (c) Nils Adermann <naderman@naderman.de>
 *     Jordi Boggiano <j.boggiano@seld.be>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Composer\Package\Loader;

use Composer\Package\BasePackage;
use Composer\Package\Version\VersionParser;
use Composer\Repository\RepositoryManager;
use Composer\Util\ProcessExecutor;
use Composer\Package\AliasPackage;

/**
 * ArrayLoader built for the sole purpose of loading the root package
 *
 * Sets additional defaults and loads repositories
 *
 * @author Jordi Boggiano <j.boggiano@seld.be>
 */
class RootPackageLoader extends ArrayLoader
{
    private $manager;
    private $process;

    public function __construct(RepositoryManager $manager, VersionParser $parser = null, ProcessExecutor $process = null)
    {
        $this->manager = $manager;
        $this->process = $process ?: new ProcessExecutor();
        parent::__construct($parser);
    }

    public function load($config)
    {
        if (!isset($config['name'])) {
            $config['name'] = '__root__';
        }
        if (!isset($config['version'])) {
            $version = '1.0.0';

            // try to fetch current version from git branch
            if (0 === $this->process->execute('git branch --no-color --no-abbrev -v', $output)) {
                foreach ($this->process->splitLines($output) as $branch) {
                    if ($branch && preg_match('{^(?:\* ) *(?:[^/ ]+?/)?(\S+) *[a-f0-9]+ .*$}', $branch, $match)) {
                        $version = 'dev-'.$match[1];
                    }
                }
            }

            $config['version'] = $version;
        } else {
            $version = $config['version'];
        }

        $package = parent::load($config);

        $aliases = array();
        $stabilityFlags = array();
        if (isset($config['require'])) {
            $aliases = $this->extractAliases($config['require'], $aliases);
            $stabilityFlags = $this->extractStabilityFlags($config['require'], $stabilityFlags);
        }
        if (isset($config['require-dev'])) {
            $aliases = $this->extractAliases($config['require-dev'], $aliases);
            $stabilityFlags = $this->extractStabilityFlags($config['require-dev'], $stabilityFlags);
        }

        $package->setAliases($aliases);
        $package->setStabilityFlags($stabilityFlags);

        if (isset($config['minimum-stability'])) {
            $package->setMinimumStability(VersionParser::normalizeStability($config['minimum-stability']));
        }

        if (isset($config['repositories'])) {
            foreach ($config['repositories'] as $index => $repo) {
                if (isset($repo['packagist']) && $repo['packagist'] === false) {
                    continue;
                }
                if (!is_array($repo)) {
                    throw new \UnexpectedValueException('Repository '.$index.' should be an array, '.gettype($repo).' given');
                }
                if (!isset($repo['type'])) {
                    throw new \UnexpectedValueException('Repository '.$index.' ('.json_encode($repo).') must have a type defined');
                }
                $repository = $this->manager->createRepository($repo['type'], $repo);
                $this->manager->addRepository($repository);
            }
            $package->setRepositories($config['repositories']);
        }

        return $package;
    }

    private function extractAliases(array $requires, array $aliases)
    {
        foreach ($requires as $reqName => $reqVersion) {
            if (preg_match('{^([^,\s]+) +as +([^,\s]+)$}', $reqVersion, $match)) {
                $aliases[] = array(
                    'package' => strtolower($reqName),
                    'version' => $this->versionParser->normalize($match[1]),
                    'alias' => $match[2],
                    'alias_normalized' => $this->versionParser->normalize($match[2]),
                );
            }
        }

        return $aliases;
    }

    private function extractStabilityFlags(array $requires, array $stabilityFlags)
    {
        $stabilities = BasePackage::$stabilities;
        foreach ($requires as $reqName => $reqVersion) {
            // parse explicit stability flags
            if (preg_match('{^[^,\s]*?@('.implode('|', array_keys($stabilities)).')$}i', $reqVersion, $match)) {
                $name = strtolower($reqName);
                $stability = $stabilities[VersionParser::normalizeStability($match[1])];

                if (isset($stabilityFlags[$name]) && $stabilityFlags[$name] > $stability) {
                    continue;
                }
                $stabilityFlags[$name] = $stability;

                continue;
            }

            // infer flags for requirements that have an explicit -dev or -beta version specified for example
            if (preg_match('{^[^,\s@]+$}', $reqVersion) && 'stable' !== ($stabilityName = VersionParser::parseStability($reqVersion))) {
                $name = strtolower($reqName);
                $stability = $stabilities[$stabilityName];
                if (isset($stabilityFlags[$name]) && $stabilityFlags[$name] > $stability) {
                    continue;
                }
                $stabilityFlags[$name] = $stability;
            }
        }

        return $stabilityFlags;
    }
}