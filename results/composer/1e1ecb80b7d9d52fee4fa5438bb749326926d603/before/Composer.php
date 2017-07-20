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

use Composer\Downloader\DownloaderInterface;
use Composer\Installer\InstallerInterface;
use Composer\Repository\ComposerRepository;
use Composer\Repository\PlatformRepository;
use Composer\Repository\GitRepository;
use Composer\Repository\PearRepository;

/**
 * @author Jordi Boggiano <j.boggiano@seld.be>
 */
class Composer
{
    const VERSION = '1.0.0-DEV';

    protected $repositories = array();
    protected $downloaders = array();
    protected $installers = array();

    public function __construct()
    {
        $this->addRepository('Packagist', array('composer' => 'http://packagist.org'));
    }

    /**
     * Add downloader for type
     *
     * @param string              $type
     * @param DownloaderInterface $downloader
     */
    public function addDownloader($type, DownloaderInterface $downloader)
    {
        $type = strtolower($type);
        $this->downloaders[$type] = $downloader;
    }

    /**
     * Get type downloader
     *
     * @param string $type
     *
     * @return DownloaderInterface
     */
    public function getDownloader($type)
    {
        $type = strtolower($type);
        if (!isset($this->downloaders[$type])) {
            throw new \UnexpectedValueException('Unknown source type: '.$type);
        }
        return $this->downloaders[$type];
    }

    /**
     * Add installer for type
     *
     * @param  string            $type
     * @param InstallerInterface $installer
     */
    public function addInstaller($type, InstallerInterface $installer)
    {
        $type = strtolower($type);
        $this->installers[$type] = $installer;
    }

    /**
     * Get type installer
     *
     * @param string $type
     *
     * @return InstallerInterface
     */
    public function getInstaller($type)
    {
        $type = strtolower($type);
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
        if (is_array($spec) && count($spec) === 1) {
            return $this->repositories[$name] = $this->createRepository($name, key($spec), current($spec));
        }
        throw new \UnexpectedValueException('Invalid repositories specification '.json_encode($spec).', should be: {"type": "url"}');
    }

    public function getRepositories()
    {
        return $this->repositories;
    }

    public function createRepository($name, $type, $spec)
    {
        if (is_string($spec)) {
            $spec = array('url' => $spec);
        }
        $spec['url'] = rtrim($spec['url'], '/');

        switch ($type) {
        case 'git-bare':
        case 'git-multi':
            throw new \Exception($type.' repositories not supported yet');
            break;

        case 'git':
            return new GitRepository($spec['url']);

        case 'composer':
            return new ComposerRepository($spec['url']);

        case 'pear':
            return new PearRepository($spec['url'], $name);

        default:
            throw new \UnexpectedValueException('Unknown repository type: '.$type.', could not create repository '.$name);
        }
    }
}