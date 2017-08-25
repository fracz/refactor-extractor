<?php

namespace Everzet\Behat\Runner;

use Symfony\Component\DependencyInjection\Container;
use Symfony\Component\Finder\Finder;

class FeaturesRunner extends BaseRunner implements RunnerInterface
{
    protected $position = 0;

    public function __construct(Finder $featureFiles, Container $container)
    {
        foreach ($featureFiles as $file) {
            $this->addChildRunner(new FeatureRunner(
                $container->getParserService()->parseFile($file)
              , $container
              , $this
            ));
        }

        parent::__construct('suite', $container->getEventDispatcherService());
    }

    protected function doRun()
    {
        foreach ($this as $runner) {
            $runner->run();
        }
    }
}