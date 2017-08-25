<?php

namespace Everzet\Behat\Loaders;

use \Symfony\Components\DependencyInjection\Container;
use \Symfony\Components\Finder\Finder;

use \Everzet\Behat\Runners\FeaturesRunner;

/*
 * This file is part of the behat package.
 * (c) 2010 Konstantin Kudryashov <ever.zet@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

/**
 * Steps Container
 *
 * @package     behat
 * @subpackage  Behat
 * @author      Konstantin Kudryashov <ever.zet@gmail.com>
 */
class FeaturesLoader
{
    protected $featuresRunner;

    public function __construct($path, Container $container)
    {
        $finder = new Finder();
        $files  = $finder->files()->name('*.feature')->in($path);

        $this->featuresRunner = new FeaturesRunner($files, $container);
    }

    public function getFeaturesRunner()
    {
        return $this->featuresRunner;
    }
}