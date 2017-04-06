<?php

namespace Symfony\Bundle\FrameworkBundle\Controller;

use Symfony\Bundle\FrameworkBundle\ParamConverter\ConverterManager;

use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Symfony\Component\DependencyInjection\ContainerInterface;
use Symfony\Component\EventDispatcher\EventDispatcher;
use Symfony\Component\EventDispatcher\Event;

/**
 * Converts \ReflectionParameters for Controller actions into Objects if the ReflectionParameter have a class
 * (Typehinted).
 *
 * @author Fabien Potencier <fabien.potencier@symfony-project.org>
 * @author Henrik Bjornskov <hb@peytz.dk>
 */
class ParamConverterListener
{
    /**
     * @var ConverterManager
     */
    protected $manager;

    /**
     * @param ConverterManager   $manager
     * @param ContainerInterface $container
     */
    public function __construct(ConverterManager $manager, ContainerInterface $container)
    {
        foreach ($container->findTaggedServiceIds('param_converter.converter') as $id => $attributes) {
            $priority = isset($attributes['priority']) ? (integer) $attributes['priority'] : 0;
            $manager->add($container->get($id), $priority);
        }

        $this->manager = $manager;
    }

    /**
     * @param EventDispatcher $dispatcher
     * @param integer         $priority = 0
     */
    public function register(EventDispatcher $dispatcher, $priority = 0)
    {
        $dispatcher->connect('core.controller', array($this, 'filterController'), $priority);
    }

    /**
     * @param  Event $event
     * @param  mixed $controller
     * @throws NotFoundHttpException
     * @return mixed
     */
    public function filterController(Event $event, $controller)
    {
        if (!is_array($controller)) {
            return $controller;
        }

        $request = $event->get('request');
        $method  = new \ReflectionMethod($controller[0], $controller[1]);

        foreach ($method->getParameters() as $param) {
            if (null !== $param->getClass() && false === $request->attributes->has($param->getName())) {
                try {
                    $this->manager->apply($request, $param);
                } catch (\InvalidArgumentException $e) {
                    if (false == $param->isOptional()) {
                        throw new NotFoundHttpException($e->getMessage());
                    }
                }
            }
        }

        return $controller;
    }
}