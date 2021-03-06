<?php

namespace Dingo\Api\Tests\Stubs;

use Closure;
use ArrayIterator;
use Illuminate\Container\Container;
use Illuminate\Http\Request;
use Dingo\Api\Http\Response;
use Dingo\Api\Contract\Routing\Adapter;
use Illuminate\Http\Response as IlluminateResponse;
use Illuminate\Pipeline\Pipeline;

class RoutingAdapterStub implements Adapter
{
    protected $routes = [];

    public function dispatch(Request $request, $version)
    {
        $routes = $this->routes[$version];

        $route = $this->findRoute($request, $routes);

        $request->setRouteResolver(function () use ($route) {
            return $route;
        });

        return (new Pipeline(new Container))
            ->send($request)
            ->through([])
            ->then(function ($request) use ($route) {
                return $this->prepareResponse($request, $route->run($request));
            });
    }

    protected function findRouteClosure(array $action)
    {
        foreach ($action as $value) {
            if ($value instanceof Closure) {
                return $value;
            }
        }
    }

    protected function prepareResponse($request, $response)
    {
        if (! $response instanceof Response && ! $response instanceof IlluminateResponse) {
            $response = new Response($response);
        }

        return $response->prepare($request);
    }

    protected function findRoute(Request $request, $routeCollection)
    {
        return $routeCollection->match($request);
    }

    public function getRouteProperties($route, Request $request)
    {
        return [$route->uri(), (array) $request->getMethod(), $route->getAction()];
    }

    public function addRoute(array $methods, array $versions, $uri, $action)
    {
        $this->createRouteCollections($versions);

        $route = new \Illuminate\Routing\Route($methods, $uri, $action);
        $route->where($action['where']);
        foreach ($versions as $version) {
            $this->routes[$version]->add($route);
        }

        return $route;
    }

    public function getRoutes($version = null)
    {
        if (! is_null($version)) {
            return $this->routes[$version];
        }

        return $this->routes;
    }

    public function getIterableRoutes($version = null)
    {
        return $this->getRoutes($version);
    }

    public function setRoutes(array $routes)
    {
        //
    }

    public function prepareRouteForSerialization($route)
    {
        //
    }

    protected function createRouteCollections(array $versions)
    {
        foreach ($versions as $version) {
            if (! isset($this->routes[$version])) {
                $this->routes[$version] = new \Illuminate\Routing\RouteCollection;
            }
        }
    }
}