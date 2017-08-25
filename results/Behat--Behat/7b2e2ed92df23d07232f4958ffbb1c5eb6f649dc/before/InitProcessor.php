<?php

namespace Behat\Behat\Console\Processor;

use Symfony\Component\DependencyInjection\ContainerInterface,
    Symfony\Component\Console\Input\InputInterface,
    Symfony\Component\Console\Input\InputOption,
    Symfony\Component\Console\Output\OutputInterface;

use Behat\Behat\PathLocator;

class InitProcessor implements ProcessorInterface
{
    /**
     * @see     Behat\Behat\Console\Configuration\ProcessorInterface::getInputOptions()
     */
    public function getInputOptions()
    {
        return array(
            new InputOption('--init',           null,
                InputOption::VALUE_NONE,
                '         ' .
                'Create <comment>features</comment> directory structure.'
            ),
        );
    }

    /**
     * @see     Behat\Behat\Console\Configuration\ProcessorInterface::process()
     */
    public function process(ContainerInterface $container, InputInterface $input, OutputInterface $output)
    {
        $locator = $container->get('behat.path_locator');

        if ($input->hasOption('init') && $input->getOption('init')) {
            $this->initFeaturesDirectoryStructure($locator, $output);
            exit(0);
        }
    }

    /**
     * Creates features path structure (initializes behat tests structure).
     *
     * @param   Behat\Behat\PathLocator                             $locator    path locator
     * @param   Symfony\Component\Console\Input\OutputInterface     $output     output console
     */
    protected function initFeaturesDirectoryStructure(PathLocator $locator, OutputInterface $output)
    {
        $basePath       = realpath($locator->getWorkPath()) . DIRECTORY_SEPARATOR;
        $featuresPath   = $locator->getFeaturesPath();
        $bootstrapPath  = $locator->getBootstrapPath();

        if (!is_dir($featuresPath)) {
            mkdir($featuresPath, 0777, true);
            $output->writeln(
                '<info>+d</info> ' .
                str_replace($basePath, '', realpath($featuresPath)) .
                ' <comment>- place your *.feature files here</comment>'
            );
        }

        if (!is_dir($bootstrapPath)) {
            mkdir($bootstrapPath, 0777, true);
            $output->writeln(
                '<info>+d</info> ' .
                str_replace($basePath, '', realpath($bootstrapPath)) .
                ' <comment>- place bootstrap scripts and static files here</comment>'
            );

            file_put_contents(
                $bootstrapPath . DIRECTORY_SEPARATOR . 'FeatureContext.php',
                $this->getFeatureContextSkelet()
            );

            $output->writeln(
                '<info>+f</info> ' .
                str_replace($basePath, '', realpath($bootstrapPath)) . DIRECTORY_SEPARATOR .
                'FeatureContext.php <comment>- place your feature related code here</comment>'
            );
        }
    }

    /**
     * Returns feature context skelet.
     *
     * @return  string
     */
    protected function getFeatureContextSkelet()
    {
return <<<'PHP'
<?php

use Behat\Behat\Context\ClosuredContextInterface,
    Behat\Behat\Context\TranslatedContextInterface,
    Behat\Behat\Context\BehatContext,
    Behat\Behat\Exception\Pending;
use Behat\Gherkin\Node\PyStringNode,
    Behat\Gherkin\Node\TableNode;

//
// Require 3rd-party libraries here:
//
//   require_once 'PHPUnit/Autoload.php';
//   require_once 'PHPUnit/Framework/Assert/Functions.php';
//

/**
 * Features context.
 */
class FeatureContext extends BehatContext
{
    /**
     * Initializes context.
     * Every scenario gets it's own context object.
     *
     * @param   array   $parameters     context parameters (set them up through behat.yml)
     */
    public function __construct(array $parameters)
    {
        // Initialize your context here
    }

//
// Place your definition and hook methods here:
//
//    /**
//     * @Given /^I have done something with "([^"]*)"$/
//     */
//    public function iHaveDoneSomethingWith($argument)
//    {
//        doSomethingWith($argument);
//    }
//
}

PHP;
    }
}