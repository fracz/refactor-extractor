<?php
/**
 * phpDocumentor
 *
 * PHP Version 5
 *
 * @author    Mike van Riel <mike.vanriel@naenius.com>
 * @copyright 2010-2011 Mike van Riel / Naenius (http://www.naenius.com)
 * @license   http://www.opensource.org/licenses/mit-license.php MIT
 * @link      http://phpdoc.org
 */

namespace phpDocumentor;

use \Symfony\Component\Console\Input\InputInterface;

include_once __DIR__ . '/../../vendor/.composer/autoload.php';

/**
 * Application class for phpDocumentor.
 *
 * Can be used as bootstrap when the run method is not invoked.
 *
 * @author  Mike van Riel <mike.vanriel@naenius.com>
 * @license http://www.opensource.org/licenses/mit-license.php MIT
 * @link    http://phpdoc.org
 */
class Application extends \Cilex\Application
{
    const VERSION = '2.0.0a3';

    /**
     * Initializes all components used by phpDocumentor.
     */
    function __construct()
    {
        parent::__construct('phpDocumentor', self::VERSION);

        $this->addAutoloader();
        $this->addLogging();
        $this->addConfiguration();
        $this->addEventDispatcher();
        $this->loadPlugins();

        $this->addCommandsForProjectNamespace();
    }

    protected function addCommandsForProjectNamespace()
    {
        $this->command(new \phpDocumentor\Command\Project\ParseCommand());
        $this->command(new \phpDocumentor\Command\Project\RunCommand());
        $this->command(new \phpDocumentor\Command\Project\TransformCommand());
    }

    protected function addAutoloader()
    {
        $this['autoloader'] = include __DIR__
            . '/../../vendor/.composer/autoload.php';
    }

    protected function addLogging()
    {
        $this->register(new \Cilex\Provider\MonologServiceProvider(), array(
            'monolog.name'    => 'phpDocumentor',
            'monolog.logfile' => sys_get_temp_dir().'/phpdoc.log'
        ));
    }

    /**
     * Adds the Configuration object to the DIC.
     *
     * phpDocumentor first loads the template config file (/data/phpdoc.tpl.xml)
     * and then the phpdoc.dist.xml, or the phpdoc.xml if it exists but not both,
     * from the current working directory.
     *
     * The user config file (either phpdox.dist.xml or phpdoc.xml) is merged
     * with the template file.
     *
     * @return void
     */
    protected function addConfiguration()
    {
        $this['config'] = $this->share(
            function () {
                $user_config_file = (file_exists('phpdoc.xml'))
                    ? 'phpdoc.xml'
                    : 'phpdoc.dist.xml';

                return \Zend\Config\Factory::fromFiles(
                    array(
                        __DIR__.'/../../data/phpdoc.tpl.xml',
                        $user_config_file
                    ), true
                );
            }
        );
    }

    protected function addEventDispatcher()
    {
        $this['event_dispatcher'] = $this->share(
            function () {
                return new \sfEventDispatcher();
            }
        );
        $this->linkEventDispatcherToSuperclasses();
    }

    /**
     * Temporary method to link the event dispatcher to all subelements.
     *
     * @todo A different way should be devised for the Event Dispatcher to be passed.
     *
     * @return void
     */
    protected function linkEventDispatcherToSuperclasses()
    {
        \phpDocumentor_Reflection_Abstract::$event_dispatcher = $this['event_dispatcher'];
        \phpDocumentor_Parser_Abstract::$event_dispatcher = $this['event_dispatcher'];
        \phpDocumentor_Transformer_Abstract::$event_dispatcher = $this['event_dispatcher'];
    }


    protected function loadPlugins()
    {
        $app = $this;
        $this['plugin_manager'] = $this->share(
            function () use ($app) {
                $manager = new \phpDocumentor_Plugin_Manager(
                    $app['event_dispatcher'], $app['config'], $app['autoloader']
                );
                return $manager;
            }
        );
        $this['plugin_manager']->loadFromConfiguration();
    }

    /**
     * Override method to change default command from list to project:run.
     *
     * @param \Symfony\Component\Console\Input\InputInterface $input
     *
     * @return string
     */
    protected function getCommandName(InputInterface $input)
    {
        $name = $input->getFirstArgument('command');
        return $name ? $name : 'project:run';
    }
}