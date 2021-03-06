<?php

namespace Symfony\Framework\WebBundle\Controller;

use Symfony\Foundation\LoggerInterface;
use Symfony\Components\DependencyInjection\ContainerInterface;
use Symfony\Components\HttpKernel\HttpKernelInterface;

/*
 * This file is part of the Symfony framework.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * This source file is subject to the MIT license that is bundled
 * with this source code in the file LICENSE.
 */

/**
 * ControllerManager.
 *
 * @package    Symfony
 * @subpackage Framework_WebBundle
 * @author     Fabien Potencier <fabien.potencier@symfony-project.com>
 */
class ControllerManager
{
    protected $container;
    protected $logger;

    public function __construct(ContainerInterface $container, LoggerInterface $logger = null)
    {
        $this->container = $container;
        $this->logger = $logger;
    }

    /**
     * Renders a Controller and returns the Response content.
     *
     * @param string $controller A controller name to execute (a string like BlogBundle:Post:index), or a relative URI
     * @param array  $options    An array of options
     *
     * @return string The Response content
     */
    public function render($controller, array $options = array())
    {
        $request = $this->container->getRequestService();

        // controller or URI?
        if (0 === strpos($controller, '/')) {
            $subRequest = Request::create($controller, 'get', array(), $request->cookies->all(), array(), $request->server->all());
        } else {
            $options['path']['_controller'] = $controller;
            $options['path']['_format'] = $request->getRequestFormat();
            $subRequest = $request->duplicate($options['query'], null, $options['path']);
        }

        try {
            return $this->container->getKernelService()->handle($subRequest, HttpKernelInterface::EMBEDDED_REQUEST, true);
        } catch (\Exception $e) {
            if ($options['alt']) {
                $alt = $options['alt'];
                unset($options['alt']);
                $options['path'] = isset($alt[1]) ? $alt[1] : array();
                $options['query'] = isset($alt[2]) ? $alt[2] : array();

                return $this->render($alt[0], $options);
            }

            if (!$options['ignore_errors']) {
                throw $e;
            }
        }
    }

    /**
     * Creates the Controller instance associated with the controller string
     *
     * @param string $controller A controller name (a string like BlogBundle:Post:index)
     *
     * @return array An array composed of the Controller instance and the Controller method
     *
     * @throws \InvalidArgumentException|\LogicException If the controller can't be found
     */
    public function findController($controller)
    {
        list($bundle, $controller, $action) = explode(':', $controller);
        $class = null;
        $logs = array();
        foreach (array_keys($this->container->getParameter('kernel.bundle_dirs')) as $namespace) {
            $try = $namespace.'\\'.$bundle.'\\Controller\\'.$controller.'Controller';
            if (!class_exists($try)) {
                if (null !== $this->logger) {
                    $logs[] = sprintf('Failed finding controller "%s:%s" from namespace "%s" (%s)', $bundle, $controller, $namespace, $try);
                }
            } else {
                if (!in_array($namespace.'\\'.$bundle.'\\Bundle', array_map(function ($bundle) { return get_class($bundle); }, $this->container->getKernelService()->getBundles()))) {
                    throw new \LogicException(sprintf('To use the "%s" controller, you first need to enable the Bundle "%s" in your Kernel class.', $try, $namespace.'\\'.$bundle));
                }

                $class = $try;

                break;
            }
        }

        if (null === $class) {
            if (null !== $this->logger) {
                foreach ($logs as $log) {
                    $this->logger->info($log);
                }
            }

            throw new \InvalidArgumentException(sprintf('Unable to find controller "%s:%s".', $bundle, $controller));
        }

        $controller = new $class($this->container);

        $method = $action.'Action';
        if (!method_exists($controller, $method)) {
            throw new \InvalidArgumentException(sprintf('Method "%s::%s" does not exist.', $class, $method));
        }

        if (null !== $this->logger) {
            $this->logger->info(sprintf('Using controller "%s::%s"%s', $class, $method, isset($file) ? sprintf(' from file "%s"', $file) : ''));
        }

        return array($controller, $method);
    }

    /**
     * @throws \RuntimeException When value for argument given is not provided
     */
    public function getMethodArguments(array $path, $controller, $method)
    {
        $r = new \ReflectionObject($controller);

        $arguments = array();
        foreach ($r->getMethod($method)->getParameters() as $param) {
            if (array_key_exists($param->getName(), $path)) {
                $arguments[] = $path[$param->getName()];
            } elseif ($param->isDefaultValueAvailable()) {
                $arguments[] = $param->getDefaultValue();
            } else {
                throw new \RuntimeException(sprintf('Controller "%s::%s()" requires that you provide a value for the "$%s" argument (because there is no default value or because there is a non optional argument after this one).', $controller, $method, $param->getName()));
            }
        }

        return $arguments;
    }
}