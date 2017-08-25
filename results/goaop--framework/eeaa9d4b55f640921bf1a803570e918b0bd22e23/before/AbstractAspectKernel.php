<?php
/**
 * Go! OOP&AOP PHP framework
 *
 * @copyright     Copyright 2012, Lissachenko Alexander <lisachenko.it@gmail.com>
 * @license       http://www.opensource.org/licenses/mit-license.php The MIT License
 */

namespace Go\Core;

use Go\Instrument\ClassLoading\UniversalClassLoader;
use Go\Instrument\ClassLoading\SourceTransformingLoader;
use Go\Instrument\Transformer\SourceTransformer;
use Go\Instrument\Transformer\AopProxyTransformer;
use Go\Instrument\Transformer\FilterInjectorTransformer;

use TokenReflection;

/**
 * Abstract aspect kernel is used to prepare an application to work with aspects.
 *
 * Realization of this class should return the path for application loader, so when the kernel has finished its work,
 * it will pass the control to the application loader.
 */
abstract class AbstractAspectKernel
{
    /**
     * Kernel options
     *
     * @var array
     */
    protected $options = array();

    /**
     * Single instance of kernel
     *
     * @var null|static
     */
    protected static $instance = null;

    /**
     * Aspect container instance
     *
     * @var null
     */
    protected $container = null;

    /**
     * Protected constructor is used to prevent direct creation, but allows customization if needed
     */
    protected function __construct() {}

    /**
     * Returns the single instance of kernel
     *
     * @return AbstractAspectKernel
     */
    public static function getInstance()
    {
        if (!self::$instance) {
            self::$instance = new static();
        }
        return self::$instance;
    }

    /**
     * Init the kernel and make adjustments
     *
     * @param array $options Associative array of options for kernel
     */
    public function init(array $options = array())
    {
        $this->options = array_merge_recursive($this->options, $options);
        $this->initLibraryLoader();


        $containerClass  = $this->getContainerClassName();
        $this->container = new $containerClass;
        $sourceLoaderFilter = new SourceTransformingLoader();
        $sourceLoaderFilter->register();

        foreach ($this->registerTransformers($sourceLoaderFilter) as $sourceTransformer) {
            $sourceLoaderFilter->addTransformer($sourceTransformer);
        }

        $sourceLoaderFilter->load($this->getApplicationLoaderPath());
    }

    /**
     * Returns the path to the application autoloader file, typical autoload.php
     *
     * @return string
     */
    abstract protected function getApplicationLoaderPath();

    /**
     * Return the name for the container class
     *
     * Override this method to extend container with custom container
     *
     * NB. If you use a custom class, then you should include it source before starting the kernel or it won't be
     * found during loading of the kernel.
     *
     * @return string
     */
    protected function getContainerClassName()
    {
        return __NAMESPACE__ . '\\AspectContainer';
    }

    /**
     * Returns list of source transformers, that will be applied to the PHP source
     *
     * @param SourceTransformingLoader $sourceLoader Instance of source loader for information
     *
     * @return array|SourceTransformer[]
     */
    protected function registerTransformers(SourceTransformingLoader $sourceLoader)
    {
        return array(
            new FilterInjectorTransformer(__DIR__, __DIR__, $sourceLoader->getId()),
            new AopProxyTransformer(
                new TokenReflection\Broker(
                    new TokenReflection\Broker\Backend\Memory()
                )
            ),
        );
    }

    /**
     * Init library autoloader.
     *
     * We cannot use any standard autoloaders in the application level because we will rewrite them on fly.
     * This will also reduce the time for library loading and prevent cyclic errors when source is loaded.
     */
    protected function initLibraryLoader()
    {
        // Default autoload paths for library
        $autoloadOptions = array(
            'autoload' => array(
                'Go'              => realpath(__DIR__ . '/../../'),
                'TokenReflection' => realpath(__DIR__ . '/../../../vendor/andrewsville/php-token-reflection/')
            )
        );
        $options = array_merge_recursive($autoloadOptions, $this->options);

        /**
         * Separate class loader for core should be used to load classes,
         * so UniversalClassLoader is moved to the custom namespace
         */
        require_once __DIR__ . '/../Instrument/ClassLoading/UniversalClassLoader.php';

        $loader = new UniversalClassLoader();
        $loader->registerNamespaces($options['autoload']);
        $loader->register();
    }
}