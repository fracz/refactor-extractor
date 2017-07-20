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

/**
 * Downloader interface.
 *
 * @author Konstantin Kudryashov <ever.zet@gmail.com>
 * @author Jordi Boggiano <j.boggiano@seld.be>
 */
interface DownloaderInterface
{
    /**
     * Downloads specific package into specific folder from dist.
     *
     * @param   PackageInterface    $package    package instance
     * @param   string              $path       download path
     */
    function distDownload(PackageInterface $package, $path);

    /**
     * Downloads specific package into specific folder from source.
     *
     * @param   PackageInterface    $package    package instance
     * @param   string              $path       download path
     */
    function sourceDownload(PackageInterface $package, $path);

    /**
     * Updates specific package in specific folder from initial to target version.
     *
     * @param   PackageInterface    $initial    initial package
     * @param   PackageInterface    $target     updated package
     * @param   string              $path       download path
     */
    function distUpdate(PackageInterface $initial, PackageInterface $target, $path);

    /**
     * Updates specific package in specific folder from initial to target version.
     *
     * @param   PackageInterface    $initial    initial package
     * @param   PackageInterface    $target     updated package
     * @param   string              $path       download path
     */
    function sourceUpdate(PackageInterface $initial, PackageInterface $target, $path);

    /**
     * Removes specific package from specific folder.
     *
     * @param   PackageInterface    $package    package instance
     * @param   string              $path       download path
     */
    function remove(PackageInterface $package, $path);
}