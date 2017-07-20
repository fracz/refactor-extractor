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

namespace Composer\Installer;

use Composer\Downloader\DownloaderInterface;
use Composer\Package\PackageInterface;
use Composer\Downloader\DownloaderInterface;

/**
 * @author Jordi Boggiano <j.boggiano@seld.be>
 * @author Konstantin Kudryashov <ever.zet@gmail.com>
 */
class LibraryInstaller implements InstallerInterface
{
    private $dir;
    private $preferSource;
    private $downloaders = array();

    public function __construct($dir = 'vendor', $preferSource = false)
    {
        $this->dir = $dir;
        $this->preferSource = $preferSource;

        if (!is_dir($this->dir)) {
            if (file_exists($this->dir)) {
                throw new \UnexpectedValueException(
                    $this->dir.' exists and is not a directory.'
                );
            }
            if (!mkdir($this->dir, 0777, true)) {
                throw new \UnexpectedValueException(
                    $this->dir.' does not exist and could not be created.'
                );
            }
        }
    }

    public function setDownloader($type, DownloaderInterface $downloader = null)
    {
        if (null === $downloader) {
            unset($this->downloaders[$type]);

            return;
        }

        $this->downloaders[$type] = $downloader;
    }

    public function getDownloader($type)
    {
        if (!isset($this->downloaders[$type])) {
            throw new \UnexpectedValueException('Unknown source type: '.$type);
        }

        return $this->downloaders[$type];
    }

    public function install(PackageInterface $package)
    {
        if (!($this->preferSource && $package->getSourceType()) && $package->getDistType()) {
            $downloader = $this->getDownloader($package->getDistType());

            return $downloader->download(
                $package, $this->dir, $package->getDistUrl(), $package->getDistSha1Checksum()
            );
        }

        if ($package->getSourceType()) {
            $downloader = $this->getDownloader($package->getSourceType());

            return $downloader->download(
                $package, $this->dir, $package->getSourceUrl()
            );
        }

        throw new \InvalidArgumentException('Package should have dist or source specified');
    }

    public function isInstalled(PackageInterface $package)
    {
        if ($package->getSourceType()) {
            $downloader = $this->getDownloader($package->getSourceType());

            if ($downloader->isDownloaded($package, $this->dir)) {
                return true;
            }
        }

        if ($package->getDistType()) {
            $downloader = $this->getDownloader($package->getDistType());

            if ($downloader->isDownloaded($package, $this->dir)) {
                return true;
            }
        }

        return false;
    }
}