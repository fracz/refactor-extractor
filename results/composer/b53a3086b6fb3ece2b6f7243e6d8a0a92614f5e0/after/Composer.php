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

namespace Composer;

use Composer\Repository\ComposerRepository;
use Composer\Repository\PlatformRepository;

/**
 * @author Jordi Boggiano <j.boggiano@seld.be>
 */
class Composer
{
    protected $repositories = array();
    protected $downloaders = array();
    protected $installers = array();

    public function __construct()
    {
        $this->addRepository('Packagist', array('composer' => 'http://packagist.org'));
        $this->addRepository('Platform', array('platform' => ''));
    }

    public function addDownloader($type, $downloader)
    {
        $this->downloaders[$type] = $downloader;
    }

    public function getDownloader($type)
    {
        if (!isset($this->downloaders[$type])) {
            throw new \UnexpectedValueException('Unknown source type: '.$type);
        }
        return $this->downloaders[$type];
    }

    public function addInstaller($type, $installer)
    {
        $this->installers[$type] = $installer;
    }

    public function getInstaller($type)
    {
        if (!isset($this->installers[$type])) {
            throw new \UnexpectedValueException('Unknown dependency type: '.$type);
        }
        return $this->installers[$type];
    }

    public function addRepository($name, $spec)
    {
        if (null === $spec) {
            unset($this->repositories[$name]);
        }
        if (is_array($spec) && count($spec)) {
            return $this->repositories[$name] = $this->createRepository(key($spec), current($spec));
        }
        throw new \UnexpectedValueException('Invalid repositories specification '.var_export($spec, true));
    }

    public function getRepositories()
    {
        return $this->repositories;
    }

    public function createRepository($type, $url)
    {
        $url = rtrim($url, '/');

        switch ($type) {
        case 'git-bare':
        case 'git-package':
        case 'git-multi':
            throw new \Exception($type.' repositories not supported yet');
            break;

        case 'platform':
            return new PlatformRepository;

        case 'composer':
            return new ComposerRepository($url);
        }
    }
}