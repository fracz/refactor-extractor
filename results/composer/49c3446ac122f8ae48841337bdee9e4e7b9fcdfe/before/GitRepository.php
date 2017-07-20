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

namespace Composer\Repository;

use Composer\Package\MemoryPackage;
use Composer\Package\BasePackage;
use Composer\Package\Link;
use Composer\Package\LinkConstraint\VersionConstraint;

/**
 * @author Jordi Boggiano <j.boggiano@seld.be>
 */
class GitRepository extends ArrayRepository
{
    protected $packages;

    public function __construct($url)
    {
        $this->url = $url;
    }

    protected function initialize()
    {
        parent::initialize();

        if (preg_match('#^(?:https?|git(?:\+ssh)?|ssh)://#', $this->url)) {
            // check if the repo is on github.com, read the composer.json file & branch/tags out of it
            // otherwise, maybe we have to clone the repo to figure out what's in it
            throw new \Exception('Not implemented yet');
        } elseif (file_exists($this->url)) {
            if (!file_exists($this->url.'/composer.json')) {
                throw new \InvalidArgumentException('The repository at url '.$this->url.' does not contain a composer.json file.');
            }
            $config = json_decode(file_get_contents($this->url.'/composer.json'), true);
            if (!$config) {
                throw new \UnexpectedValueException('Config file could not be parsed: '.$this->url.'/composer.json. Probably a JSON syntax error.');
            }
        } else {
            throw new \InvalidArgumentException('Could not find repository at url '.$this->url);
        }

        $this->createPackage($config);
    }

    // TODO code re-use
    protected function createPackage($data)
    {
        $version = BasePackage::parseVersion($data['version']);

        $package = new MemoryPackage($data['name'], $version['version'], $version['type']);
        $package->setSourceType('git');
        $package->setSourceUrl($this->url);

        if (isset($data['license'])) {
            $package->setLicense($data['license']);
        }

        $links = array(
            'require',
            'conflict',
            'provide',
            'replace',
            'recommend',
            'suggest',
        );
        foreach ($links as $link) {
            if (isset($data[$link])) {
                $method = 'set'.$link.'s';
                $package->{$method}($this->createLinks($data['name'], $link.'s', $data[$link]));
            }
        }
        $this->addPackage($package);
    }

    // TODO code re-use
    protected function createLinks($name, $description, $linkSpecs)
    {
        $links = array();
        foreach ($linkSpecs as $dep => $ver) {
            preg_match('#^([>=<~]*)([\d.]+.*)$#', $ver, $match);
            if (!$match[1]) {
                $match[1] = '=';
            }
            $constraint = new VersionConstraint($match[1], $match[2]);
            $links[] = new Link($name, $dep, $constraint, $description);
        }
        return $links;
    }
}