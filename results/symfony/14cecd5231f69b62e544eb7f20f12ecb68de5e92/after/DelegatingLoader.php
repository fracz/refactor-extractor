<?php

namespace Symfony\Components\Routing\Loader;

/*
 * This file is part of the Symfony framework.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * This source file is subject to the MIT license that is bundled
 * with this source code in the file LICENSE.
 */

/**
 * DelegatingLoader delegates route loading to other loaders using a loader resolver.
 *
 * @package    Symfony
 * @subpackage Components_Routing
 * @author     Fabien Potencier <fabien.potencier@symfony-project.com>
 */
class DelegatingLoader extends Loader
{
    protected $resolver;

    /**
     * Constructor.
     *
     * @param \Symfony\Components\Routing\Loader\LoaderResolverInterface $resolver A LoaderResolverInterface instance
     */
    public function __construct(LoaderResolverInterface $resolver)
    {
        $this->resolver = $resolver;
    }

    /**
     * Loads a resource.
     *
     * @param  mixed $resource A resource
     *
     * @return \Symfony\Components\Routing\RouteCollection A RouteCollection instance
     */
    public function load($resource)
    {
        $loader = $this->resolver->resolve($resource);

        if (false === $loader) {
            throw new \InvalidArgumentException(sprintf('Unable to load the "%s" routing resource.', is_string($resource) ? $resource : (is_object($resource) ? get_class($resource) : 'RESOURCE')));
        }

        return $loader->load($resource);
    }

    /**
     * Returns true if this class supports the given resource.
     *
     * @param  mixed $resource A resource
     *
     * @return Boolean true if this class supports the given resource, false otherwise
     */
    public function supports($resource)
    {
        foreach ($this->resolver->getLoaders() as $loader) {
            if ($loader->supports($resource)) {
                return true;
            }
        }

        return false;
    }
}