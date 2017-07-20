<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Bundle\FrameworkBundle\Command;

use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\HttpKernel\KernelInterface;
use Symfony\Component\Finder\Finder;

/**
 * Clear and Warmup the cache.
 *
 * @author Francis Besset <francis.besset@gmail.com>
 * @author Fabien Potencier <fabien@symfony.com>
 */
class CacheClearCommand extends Command
{
    /**
     * @see Command
     */
    protected function configure()
    {
        $this
            ->setName('cache:clear')
            ->setDefinition(array(
                new InputOption('no-warmup', '', InputOption::VALUE_NONE, 'Do not warm up the cache')
            ))
            ->setDescription('Clear the cache')
            ->setHelp(<<<EOF
The <info>cache:clear</info> command clears the application cache for the current environment:

<info>./app/console cache:clear</info>
EOF
            )
        ;
    }

    /**
     * {@inheritdoc}
     */
    protected function execute(InputInterface $input, OutputInterface $output)
    {
        $realCacheDir = $this->container->getParameter('kernel.cache_dir');
        $oldCacheDir  = $realCacheDir.'_old';

        if (!is_writable($realCacheDir)) {
            throw new \RuntimeException(sprintf('Unable to write in "%s" directory', $this->realCacheDir));
        }

        if ($input->getOption('no-warmup')) {
            rename($realCacheDir, $oldCacheDir);
        } else {
            $warmupDir = $realCacheDir.'_new';

            $this->warmup($warmupDir);

            rename($realCacheDir, $oldCacheDir);
            rename($warmupDir, $realCacheDir);
        }

        $this->container->get('filesystem')->remove($oldCacheDir);
    }

    protected function warmup($warmupDir)
    {
        $this->container->get('filesystem')->remove($warmupDir);

        $kernel = $this->getTempKernel($this->container->get('kernel'), $warmupDir);
        $kernel->boot();

        $warmer = $kernel->getContainer()->get('cache_warmer');
        $warmer->enableOptionalWarmers();
        $warmer->warmUp($warmupDir);

        // rename container files
        $finder = new Finder();
        foreach ($finder->files()->name(get_class($kernel->getContainer()).'*')->in($warmupDir) as $file) {
            $content = file_get_contents($file);
            $content = preg_replace('/__.*__/', '', $content);
            file_put_contents(preg_replace('/__.*__/', '', $file), $content);
            unlink($file);
        }
    }

    protected function getTempKernel(KernelInterface $parent, $warmupDir)
    {
        $parentClass = get_class($parent);
        $rand = uniqid();
        $class = $parentClass.$rand;
        $rootDir = $parent->getRootDir();
        $code = <<<EOF
<?php

class $class extends $parentClass
{
    public function getCacheDir()
    {
        return '$warmupDir';
    }

    public function getRootDir()
    {
        return '$rootDir';
    }

    protected function getContainerClass()
    {
        return parent::getContainerClass().'__{$rand}__';
    }
}
EOF;
        $this->container->get('filesystem')->mkdirs($warmupDir);
        file_put_contents($file = $warmupDir.'/kernel.tmp', $code);
        require_once $file;
        @unlink($file);

        return new $class($parent->getEnvironment(), $parent->isDebug());
    }
}