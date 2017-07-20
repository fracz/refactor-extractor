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

use Composer\Package\PackageInterface;
use Composer\Util\ProcessExecutor;
use Composer\Util\Svn as SvnUtil;

/**
 * @author Ben Bieker <mail@ben-bieker.de>
 * @author Till Klampaeckel <till@php.net>
 */
class SvnDownloader extends VcsDownloader
{
    /**
     * @var bool
     */
    protected $useAuth = false;

    /**
     * @var \Composer\Util\Svn
     */
    protected $util;

    /**
     * {@inheritDoc}
     */
    public function doDownload(PackageInterface $package, $path)
    {
        $url =  $package->getSourceUrl();
        $ref =  $package->getSourceReference();

        $util = $this->getUtil($url);

        $command = $util->getCommand("svn co", sprintf("%s/%s", $url, $ref), $path);

        $this->io->write("    Checking out ".$package->getSourceReference());
        $this->execute($command, $util);
    }

    /**
     * {@inheritDoc}
     */
    public function doUpdate(PackageInterface $initial, PackageInterface $target, $path)
    {
        $url = $target->getSourceUrl();
        $ref = $target->getSourceReference();

        $util    = $this->getUtil($url);
        $command = $util->getCommand("svn switch", sprintf("%s/%s", $url, $ref));

        $this->io->write("    Checking out " . $ref);
        $this->execute(sprintf('cd %s && %s', $path, $command), $util);
    }

    /**
     * {@inheritDoc}
     */
    protected function enforceCleanDirectory($path)
    {
        $this->process->execute('svn status', $output, $path);
        if (trim($output)) {
            throw new \RuntimeException('Source directory ' . $path . ' has uncommitted changes');
        }
    }

    /**
     * Wrap {@link \Composer\Util\ProcessExecutor::execute().
     *
     * @param string  $cmd
     * @param SvnUtil $util
     *
     * @return string
     */
    protected function execute($command, SvnUtil $util)
    {
        $status = $this->process->execute($command, $output);
        if (0 === $status) {
            return $output;
        }

        // this could be any failure, since SVN exits with 1 always
        if (empty($output)) {
            $output = $this->process->getErrorOutput();
        }

        if (!$this->io->isInteractive()) {
            return $output;
        }

        // the error is not auth-related
        if (false === stripos($output, 'authorization failed:')) {
            return $output;
        }

        // no authorization has been detected so far
        if (!$this->useAuth) {
            $this->useAuth = $util->doAuthDance()->hasAuth();
            $credentials   = $util->getCredentialString();

            // restart the process
            $output = $this->execute($command . ' ' . $credentials, $util);
        } else {
            $this->io->write("Authorization failed: {$command}");
        }

        return $output;
    }

    /**
     * @param string $url
     *
     * @return \Composer\Util\Svn
     */
    protected function getUtil($url)
    {
        return new SvnUtil($url, $this->io);
    }
}