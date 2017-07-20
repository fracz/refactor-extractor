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

namespace Composer\Downloader;

use Composer\Config;
use Composer\Cache;
use Composer\IO\IOInterface;
use Composer\Package\PackageInterface;
use Composer\Package\Version\VersionParser;
use Composer\Plugin\PluginEvents;
use Composer\Plugin\PreFileDownloadEvent;
use Composer\EventDispatcher\EventDispatcher;
use Composer\Util\Filesystem;
use Composer\Util\GitHub;
use Composer\Util\RemoteFilesystem;

/**
 * Base downloader for files
 *
 * @author Kirill chEbba Chebunin <iam@chebba.org>
 * @author Jordi Boggiano <j.boggiano@seld.be>
 * @author François Pluchino <francois.pluchino@opendisplay.com>
 * @author Nils Adermann <naderman@naderman.de>
 */
class FileDownloader implements DownloaderInterface
{
    protected $io;
    protected $config;
    protected $rfs;
    protected $filesystem;
    protected $cache;
    protected $outputProgress = true;

    /**
     * Constructor.
     *
     * @param IOInterface      $io              The IO instance
     * @param Config           $config          The config
     * @param EventDispatcher  $eventDispatcher The event dispatcher
     * @param Cache            $cache           Optional cache instance
     * @param RemoteFilesystem $rfs             The remote filesystem
     * @param Filesystem       $filesystem      The filesystem
     */
    public function __construct(IOInterface $io, Config $config, EventDispatcher $eventDispatcher = null, Cache $cache = null, RemoteFilesystem $rfs = null, Filesystem $filesystem = null)
    {
        $this->io = $io;
        $this->config = $config;
        $this->eventDispatcher = $eventDispatcher;
        $this->rfs = $rfs ?: new RemoteFilesystem($io);
        $this->filesystem = $filesystem ?: new Filesystem();
        $this->cache = $cache;


        if ($this->cache && $this->cache->gcIsNecessary()) {
            $this->cache->gc($config->get('cache-files-ttl'), $config->get('cache-files-maxsize'));
        }
    }

    /**
     * {@inheritDoc}
     */
    public function getInstallationSource()
    {
        return 'dist';
    }

    /**
     * {@inheritDoc}
     */
    public function download(PackageInterface $package, $path)
    {
        $url = $package->getDistUrl();
        if (!$url) {
            throw new \InvalidArgumentException('The given package is missing url information');
        }

        $this->filesystem->removeDirectory($path);
        $this->filesystem->ensureDirectoryExists($path);

        $fileName = $this->getFileName($package, $path);

        $this->io->write("  - Installing <info>" . $package->getName() . "</info> (<comment>" . VersionParser::formatVersion($package) . "</comment>)");

        $processedUrl = $this->processUrl($package, $url);
        $hostname = parse_url($processedUrl, PHP_URL_HOST);

        $preFileDownloadEvent = new PreFileDownloadEvent(PluginEvents::PRE_FILE_DOWNLOAD, $this->rfs, $processedUrl);
        if ($this->eventDispatcher) {
            $this->eventDispatcher->dispatch($preFileDownloadEvent->getName(), $preFileDownloadEvent);
        }
        $rfs = $preFileDownloadEvent->getRemoteFilesystem();

        if (strpos($hostname, '.github.com') === (strlen($hostname) - 11)) {
            $hostname = 'github.com';
        }

        try {
            try {
                if (!$this->cache || !$this->cache->copyTo($this->getCacheKey($package), $fileName)) {
                    if (!$this->outputProgress) {
                        $this->io->write('    Downloading');
                    }

                    // try to download 3 times then fail hard
                    $retries = 3;
                    while ($retries--) {
                        try {
                            $rfs->copy($hostname, $processedUrl, $fileName, $this->outputProgress);
                            break;
                        } catch (TransportException $e) {
                            // if we got an http response with a proper code, then requesting again will probably not help, abort
                            if ((0 !== $e->getCode() && !in_array($e->getCode(),array(500, 502, 503, 504))) || !$retries) {
                                throw $e;
                            }
                            if ($this->io->isVerbose()) {
                                $this->io->write('    Download failed, retrying...');
                            }
                            usleep(500000);
                        }
                    }

                    if ($this->cache) {
                        $this->cache->copyFrom($this->getCacheKey($package), $fileName);
                    }
                } else {
                    $this->io->write('    Loading from cache');
                }
            } catch (TransportException $e) {
                if (!in_array($e->getCode(), array(404, 403, 412))) {
                    throw $e;
                }
                if ('github.com' === $hostname && !$this->io->hasAuthentication($hostname)) {
                    $message = "\n".'Could not fetch '.$processedUrl.', enter your GitHub credentials '.($e->getCode() === 404 ? 'to access private repos' : 'to go over the API rate limit');
                    $gitHubUtil = new GitHub($this->io, $this->config, null, $rfs);
                    if (!$gitHubUtil->authorizeOAuth($hostname)
                        && (!$this->io->isInteractive() || !$gitHubUtil->authorizeOAuthInteractively($hostname, $message))
                    ) {
                        throw $e;
                    }
                    $rfs->copy($hostname, $processedUrl, $fileName, $this->outputProgress);
                } else {
                    throw $e;
                }
            }

            if (!file_exists($fileName)) {
                throw new \UnexpectedValueException($url.' could not be saved to '.$fileName.', make sure the'
                    .' directory is writable and you have internet connectivity');
            }

            $checksum = $package->getDistSha1Checksum();
            if ($checksum && hash_file('sha1', $fileName) !== $checksum) {
                throw new \UnexpectedValueException('The checksum verification of the file failed (downloaded from '.$url.')');
            }
        } catch (\Exception $e) {
            // clean up
            $this->filesystem->removeDirectory($path);
            $this->clearCache($package, $path);
            throw $e;
        }

        return $fileName;
    }

    /**
     * {@inheritDoc}
     */
    public function setOutputProgress($outputProgress)
    {
        $this->outputProgress = $outputProgress;

        return $this;
    }

    protected function clearCache(PackageInterface $package, $path)
    {
        if ($this->cache) {
            $fileName = $this->getFileName($package, $path);
            $this->cache->remove($this->getCacheKey($package));
        }
    }

    /**
     * {@inheritDoc}
     */
    public function update(PackageInterface $initial, PackageInterface $target, $path)
    {
        $this->remove($initial, $path);
        $this->download($target, $path);
    }

    /**
     * {@inheritDoc}
     */
    public function remove(PackageInterface $package, $path)
    {
        $this->io->write("  - Removing <info>" . $package->getName() . "</info> (<comment>" . VersionParser::formatVersion($package) . "</comment>)");
        if (!$this->filesystem->removeDirectory($path)) {
            // retry after a bit on windows since it tends to be touchy with mass removals
            if (!defined('PHP_WINDOWS_VERSION_BUILD') || (usleep(250000) && !$this->filesystem->removeDirectory($path))) {
                throw new \RuntimeException('Could not completely delete '.$path.', aborting.');
            }
        }
    }

    /**
     * Gets file name for specific package
     *
     * @param  PackageInterface $package package instance
     * @param  string           $path    download path
     * @return string           file name
     */
    protected function getFileName(PackageInterface $package, $path)
    {
        return $path.'/'.pathinfo(parse_url($package->getDistUrl(), PHP_URL_PATH), PATHINFO_BASENAME);
    }

    /**
     * Process the download url
     *
     * @param  PackageInterface $package package the url is coming from
     * @param  string           $url     download url
     * @return string           url
     *
     * @throws \RuntimeException If any problem with the url
     */
    protected function processUrl(PackageInterface $package, $url)
    {
        if (!extension_loaded('openssl') && 0 === strpos($url, 'https:')) {
            throw new \RuntimeException('You must enable the openssl extension to download files via https');
        }

        return $url;
    }

    private function getCacheKey(PackageInterface $package)
    {
        if (preg_match('{^[a-f0-9]{40}$}', $package->getDistReference())) {
            return $package->getName().'/'.$package->getDistReference().'.'.$package->getDistType();
        }

        return $package->getName().'/'.$package->getVersion().'-'.$package->getDistReference().'.'.$package->getDistType();
    }
}