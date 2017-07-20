<?php
namespace Codeception\TestCase\Loader;

use Codeception\Lib\Parser;

class Test implements Loader
{
    protected $tests = [];

    public function getPattern()
    {
        return '~Test\.php$~';
    }

    function loadTests($path)
    {
        Parser::load($path);
        $testClasses = Parser::getClassesFromFile($path);

        foreach ($testClasses as $testClass) {
            $reflected = new \ReflectionClass($testClass);

            if (!$reflected->isInstantiable()) {
                continue;
            }

            foreach ($reflected->getMethods(\ReflectionMethod::IS_PUBLIC) as $method) {
                $test = $this->createTestFromPhpUnitMethod($reflected, $method);
                if (!$test) {
                    continue;
                }
                $tests[] = $test;
            }
        }
    }

    public function getTests()
    {
        return $this->tests;
    }

    protected function createTestFromPhpUnitMethod(\ReflectionClass $class, \ReflectionMethod $method)
    {
        if (!\PHPUnit_Framework_TestSuite::isTestMethod($method)) {
            return;
        }
        $test = \PHPUnit_Framework_TestSuite::createTest($class, $method->name);

        if ($test instanceof \PHPUnit_Framework_TestSuite_DataProvider) {
            foreach ($test->tests() as $t) {
                $this->enhancePhpunitTest($t);
            }
            return $test;
        }

        $this->enhancePhpunitTest($test);
        return $test;
    }

    protected function enhancePhpunitTest(\PHPUnit_Framework_TestCase $test)
    {
        $className = get_class($test);
        $methodName = $test->getName(false);
        $test->setDependencies(\PHPUnit_Util_Test::getDependencies($className, $methodName));

        if (!$test instanceof \Codeception\TestCase) {
            return;
        }
    }

}