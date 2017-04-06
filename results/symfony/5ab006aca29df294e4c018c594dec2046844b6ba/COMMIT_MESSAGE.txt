commit 5ab006aca29df294e4c018c594dec2046844b6ba
Merge: c1aa618 382b083
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Apr 12 12:27:45 2012 +0200

    merged branch Tobion/generator-dumper (PR #3894)

    Commits
    -------

    382b083 [Routing] improved generated class by PhpGeneratorDumper
    27a05f4 [Routing] small optimization of PhpGeneratorDumper

    Discussion
    ----------

    [Routing] improved PhpGeneratorDumper

    Test pass: yes
    BC break: no

    The first commit only replaces arrays with strings and makes some cosmetic changes, so that it's more readable. This makes the `PhpGeneratorDumper` consistent in style with `PhpMatcherDumper` that I fixed recently and should slightly improve performance of the generation of the class.

    The second commit changes the output of the `PhpGeneratorDumper->dump` and tries to optimize the resulting class that is used to generate URLs. It's best explained with an example.

    Before my changes:

    ```php
    class ProjectUrlGenerator extends Symfony\Component\Routing\Generator\UrlGenerator
    {
        static private $declaredRouteNames = array(
           'Test' => true,
           'Test2' => true,
        );

        /**
         * Constructor.
         */
        public function __construct(RequestContext $context)
        {
            $this->context = $context;
        }

        public function generate($name, $parameters = array(), $absolute = false)
        {
            if (!isset(self::$declaredRouteNames[$name])) {
                throw new RouteNotFoundException(sprintf('Route "%s" does not exist.', $name));
            }

            $escapedName = str_replace('.', '__', $name);

            list($variables, $defaults, $requirements, $tokens) = $this->{'get'.$escapedName.'RouteInfo'}();

            return $this->doGenerate($variables, $defaults, $requirements, $tokens, $parameters, $name, $absolute);
        }

        private function getTestRouteInfo()
        {
            return array(array (  0 => 'foo',), array (), array (), array (  0 =>   array (    0 => 'variable',    1 => '/',    2 => '[^/]+?',    3 => 'foo',  ),  1 =>   array (    0 => 'text',    1 => '/testing',  ),));
        }

        private function getTest2RouteInfo()
        {
            return array(array (), array (), array (), array (  0 =>   array (    0 => 'text',    1 => '/testing2',  ),));
        }
    }
    ```

    After my changes in second commit:

    ```php
    class ProjectUrlGenerator extends Symfony\Component\Routing\Generator\UrlGenerator
    {
        static private $declaredRoutes = array(
            'Test' => array (  0 =>   array (    0 => 'foo',  ),  1 =>   array (  ),  2 =>   array (  ),  3 =>   array (    0 =>     array (      0 => 'variable',      1 => '/',      2 => '[^/]+?',      3 => 'foo',    ),    1 =>     array (      0 => 'text',      1 => '/testing',    ),  ),),
            'Test2' => array (  0 =>   array (  ),  1 =>   array (  ),  2 =>   array (  ),  3 =>   array (    0 =>     array (      0 => 'text',      1 => '/testing2',    ),  ),),
        );

        /**
         * Constructor.
         */
        public function __construct(RequestContext $context)
        {
            $this->context = $context;
        }

        public function generate($name, $parameters = array(), $absolute = false)
        {
            if (!isset(self::$declaredRoutes[$name])) {
                throw new RouteNotFoundException(sprintf('Route "%s" does not exist.', $name));
            }

            list($variables, $defaults, $requirements, $tokens) = self::$declaredRoutes[$name];

            return $this->doGenerate($variables, $defaults, $requirements, $tokens, $parameters, $name, $absolute);
        }
    }
    ```

    As you can see, there is no need to escape the route name and invoke a special method anymore. Instead the route properties are included in the static route array directly, that existed anyway. Is also easier to read as defined routes and their properties are in the same place.