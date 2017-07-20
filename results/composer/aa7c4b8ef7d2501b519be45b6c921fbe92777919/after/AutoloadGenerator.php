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

namespace Composer\Autoload;

use Composer\Installer\InstallationManager;
use Composer\Json\JsonFile;
use Composer\Package\Loader\JsonLoader;
use Composer\Package\PackageInterface;
use Composer\Repository\RepositoryInterface;

/**
 * @author Igor Wiedler <igor@wiedler.ch>
 */
class AutoloadGenerator
{
    private $localRepo;
    private $package;
    private $installationManager;

    public function __construct(RepositoryInterface $localRepo, PackageInterface $package, InstallationManager $installationManager)
    {
        $this->localRepo = $localRepo;
        $this->package = $package;
        $this->installationManager = $installationManager;
    }

    public function dump($targetFilename)
    {
        $autoloads = $this->parseAutoloads();

        $file = <<<'EOF'
<?php
// autoload.php generated by composer

require_once __DIR__.'/../vendor/symfony/symfony/src/Symfony/Component/ClassLoader/UniversalClassLoader.php';

use Symfony\Component\ClassLoader\UniversalClassLoader;

$loader = new UniversalClassLoader();

EOF;

        if (isset($autoloads['psr0'])) {
            foreach ($autoloads['psr0'] as $def) {
                foreach ($def['mapping'] as $namespace => $path) {
                    $exportedNamespace = var_export($namespace, true);
                    $exportedPath = var_export(($def['path'] ? '/'.$def['path'] : '').'/'.$path, true);
                    $file .= <<<EOF
\$loader->registerNamespace($exportedNamespace, dirname(__DIR__).$exportedPath);

EOF;
                }
            }
        }

        if (isset($autoloads['pear'])) {
            foreach ($autoloads['pear'] as $def) {
                foreach ($def['mapping'] as $prefix => $path) {
                    $exportedPrefix = var_export($prefix, true);
                    $exportedPath = var_export(($def['path'] ? '/'.$def['path'] : '').'/'.$path, true);
                    $file .= <<<EOF
\$loader->registerPrefix($exportedPrefix, dirname(__DIR__).$exportedPath);

EOF;
                }
            }
        }

        $file .= <<<'EOF'
$loader->register();

EOF;

        file_put_contents($targetFilename, $file);
    }

    private function parseAutoloads()
    {
        $installPaths = array();
        foreach ($this->localRepo->getPackages() as $package) {
            $this->populateAutoloadInformation($package);

            $installPaths[] = array(
                $package,
                $this->installationManager->getInstallPath($package)
            );
        }
        $installPaths[] = array($package, '');

        $autoloads = array();
        foreach ($installPaths as $item) {
            list($package, $installPath) = $item;

            if (null !== $package->getTargetDir()) {
                $installPath = substr($installPath, 0, -strlen('/'.$package->getTargetDir()));
            }

            foreach ($package->getAutoload() as $type => $mapping) {
                $autoloads[$type] = isset($autoloads[$type]) ? $autoloads[$type] : array();
                $autoloads[$type][] = array(
                    'mapping'   => $mapping,
                    'path'      => $installPath,
                );
            }
        }

        return $autoloads;
    }

    /**
     * Because remote repos don't include the autoload data,
     * we have to manually fetch it from the locally installed
     * packages.
     */
    private function populateAutoloadInformation(PackageInterface $package)
    {
        $path = $this->installationManager->getInstallPath($package);

        $loader = new JsonLoader();
        $fullPackage = $loader->load(new JsonFile($path.'/composer.json'));

        $package->setAutoload($fullPackage->getAutoload());
    }
}