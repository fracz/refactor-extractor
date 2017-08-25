<?php
/**
 * Go! OOP&AOP PHP framework
 *
 * @copyright     Copyright 2012, Lissachenko Alexander <lisachenko.it@gmail.com>
 * @license       http://www.opensource.org/licenses/mit-license.php The MIT License
 */

namespace Go\Core;

use ReflectionClass;
use ReflectionMethod;
use ReflectionProperty;

use Go\Aop\Advisor;
use Go\Aop\ClassFilter;
use Go\Aop\Pointcut;
use Go\Aop\PointcutAdvisor;
use Go\Aop\PointFilter;
use Go\Aop\Framework\ClassFieldAccess;
use Go\Aop\Framework\ReflectionMethodInvocation;

use TokenReflection\ReflectionClass as ParsedReflectionClass;
use TokenReflection\ReflectionMethod as ParsedReflectionMethod;
use TokenReflection\ReflectionProperty as ParsedReflectionProperty;

/**
 * Advisor registry contains list of all pointcuts
 */
class AspectContainer
{
    /**
     * Prefix for properties interceptor name
     */
    const PROPERTY_PREFIX = "prop:";

    /**
     * Prefix for method interceptor name
     */
    const METHOD_PREFIX = "method:";

    /**
     * Prefix for static method interceptor name
     */
    const STATIC_METHOD_PREFIX = "static:";

    /**
     * List of advisors (aspects)
     *
     * @var array|Advisor[]
     */
    protected static $advisors = array();

    /**
     * Register an advisor in registry
     *
     * @param Advisor $advisor Instance of advisor with advice
     */
    public static function register(Advisor $advisor)
    {
        self::$advisors[] = $advisor;
    }

    /**
     * Make an advise for a class and return list of joinpoints with correct advices at that points
     *
     * @param string|ReflectionClass|ParsedReflectionClass $class Class to advise
     *
     * @return array|Advice[] List of advices for class
     */
    public static function advise($class)
    {
        $classAdvices = array();
        if (!$class instanceof ReflectionClass && !$class instanceof ParsedReflectionClass) {
            $class = new ReflectionClass($class);
        }
        foreach (self::$advisors as $advisor) {
            if ($advisor instanceof PointcutAdvisor) {
                /** @var $advisor PointcutAdvisor */
                /** @var $pointcut Pointcut */
                $pointcut = $advisor->getPointcut();
                if ($pointcut->getClassFilter()->matches($class)) {
                    $pointFilter = $pointcut->getPointFilter();
                    $classAdvices = array_merge_recursive($classAdvices, self::getClassAdvices($class, $advisor, $pointFilter));
                }
            }
        }
        return $classAdvices;
    }

    /**
     * Returns list of advices for joinpoints
     *
     * @param ReflectionClass|ParsedReflectionClass|string $class Class to inject advices
     * @param PointcutAdvisor $advisor Advisor for class
     * @param PointFilter $filter Filter for points
     *
     * @return array
     */
    private static function getClassAdvices($class, PointcutAdvisor $advisor, PointFilter $filter)
    {
        $classAdvices = array();
        $mask = ReflectionMethod::IS_PUBLIC | ReflectionMethod::IS_PROTECTED | ReflectionMethod::IS_STATIC;
        foreach ($class->getMethods($mask) as $method) {
            /** @var $method ReflectionMethod| */
            if ($method->getDeclaringClass()->getName() == $class->getName() && $filter->matches($method)) {
                $prefix = $method->isStatic() ? self::STATIC_METHOD_PREFIX : self::METHOD_PREFIX;
                $classAdvices[$prefix . $method->getName()][] = $advisor->getAdvice();
            }
        }

        foreach ($class->getProperties() as $property) {
            /** @var $property ReflectionProperty */
            if ($filter->matches($property)) {
                $classAdvices[self::PROPERTY_PREFIX . $property->getName()][] = $advisor->getAdvice();
            }
        }
        return $classAdvices;
    }
}