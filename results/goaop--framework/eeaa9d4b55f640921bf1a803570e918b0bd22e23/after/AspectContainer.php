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

use Go\Aop\Advice;
use Go\Aop\Advisor;
use Go\Aop\ClassFilter;
use Go\Aop\Pointcut;
use Go\Aop\PointcutAdvisor;
use Go\Aop\PointFilter;
use Go\Aop\Framework\ClassFieldAccess;
use Go\Aop\PropertyMatcher;
use Go\Aop\MethodMatcher;


use TokenReflection\ReflectionClass as ParsedReflectionClass;
use TokenReflection\ReflectionMethod as ParsedReflectionMethod;
use TokenReflection\ReflectionProperty as ParsedReflectionProperty;

/**
 * Aspect container contains list of all pointcuts and advisors
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
     * List of named pointcuts in the container
     *
     * @var array|Pointcut[]
     */
    protected $pointcuts = array();

    /**
     * List of named and indexed advisors in the container
     *
     * @var array|Advisor[]
     */
    protected $advisors = array();

    /**
     * Returns a pointcut by identifier
     *
     * @param string $id Pointcut identifier
     *
     * @return Pointcut
     *
     * @throws \OutOfBoundsException if pointcut key is invalid
     */
    public function getPointcut($id)
    {
        if (is_numeric($id) || !isset($this->pointcuts[$id])) {
            throw new \OutOfBoundsException("Unknown pointcut {$id}");
        }
        return $this->pointcuts[$id];
    }

    /**
     * Store the pointcut in the container
     *
     * @param Pointcut $pointcut Instance
     * @param string $id Key for pointcut
     */
    public function registerPointcut(Pointcut $pointcut, $id = null)
    {
        if ($id) {
            $this->pointcuts[$id] = $pointcut;
        } else {
            $this->pointcuts[] = $pointcut;
        }
    }

    /**
     * Returns an advisor by identifier
     *
     * @param string $id Advisor identifier
     *
     * @return Advisor
     *
     * @throws \OutOfBoundsException if advisor key is invalid
     */
    public function getAdvisor($id)
    {
        if (is_numeric($id) || !isset($this->advisors[$id])) {
            throw new \OutOfBoundsException("Unknown advisor {$id}");
        }
        return $this->advisors[$id];
    }

    /**
     * Store the advisor in the container
     *
     * @param Advisor $advisor Instance
     * @param string $id Key for advisor
     */
    public function registerAdvisor(Advisor $advisor, $id = null)
    {
        if ($id) {
            $this->advisors[$id] = $advisor;
        } else {
            $this->advisors[] = $advisor;
        }
    }

    /**
     * Return list of advices for class
     *
     * @param string|ReflectionClass|ParsedReflectionClass $class Class to advise
     *
     * @return array|Advice[] List of advices for class
     */
    public function getAdvicesForClass($class)
    {
        $classAdvices = array();
        if (!$class instanceof ReflectionClass && !$class instanceof ParsedReflectionClass) {
            $class = new ReflectionClass($class);
        }

        foreach ($this->advisors as $advisor) {

            if ($advisor instanceof PointcutAdvisor) {

                $pointcut = $advisor->getPointcut();
                if ($pointcut->getClassFilter()->matches($class)) {
                    $pointFilter  = $pointcut->getPointFilter();
                    $classAdvices = array_merge_recursive(
                        $classAdvices,
                        $this->getAdvicesFromAdvisor($class, $advisor, $pointFilter)
                    );
                }
            }
        }
        return $classAdvices;
    }

    /**
     * Returns list of advices from advisor and point filter
     *
     * @param ReflectionClass|ParsedReflectionClass|string $class Class to inject advices
     * @param PointcutAdvisor $advisor Advisor for class
     * @param PointFilter $filter Filter for points
     *
     * @return array
     */
    private function getAdvicesFromAdvisor($class, PointcutAdvisor $advisor, PointFilter $filter)
    {
        $classAdvices = array();

        // Check methods in class only for MethodMatcher filters
        if ($filter instanceof MethodMatcher) {

            $mask = ReflectionMethod::IS_PUBLIC | ReflectionMethod::IS_PROTECTED;
            foreach ($class->getMethods($mask) as $method) {
                /** @var $method ReflectionMethod| */
                if ($method->getDeclaringClass()->getName() == $class->getName() && $filter->matches($method)) {
                    $prefix = $method->isStatic() ? self::STATIC_METHOD_PREFIX : self::METHOD_PREFIX;
                    $classAdvices[$prefix . $method->getName()][] = $advisor->getAdvice();
                }
            }
        }

        // Check properties in class only for PropertyMatcher filters
        if ($filter instanceof PropertyMatcher) {
            $mask = ReflectionProperty::IS_PUBLIC | ReflectionProperty::IS_PROTECTED;
            foreach ($class->getProperties($mask) as $property) {
                /** @var $property ReflectionProperty */
                if ($filter->matches($property)) {
                    $classAdvices[self::PROPERTY_PREFIX . $property->getName()][] = $advisor->getAdvice();
                }
            }
        }

        return $classAdvices;
    }

}