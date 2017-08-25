<?php
/**
 * PHP-DI
 *
 * @link      http://mnapoli.github.io/PHP-DI/
 * @copyright Matthieu Napoli (http://mnapoli.fr/)
 * @license   http://www.opensource.org/licenses/mit-license.php MIT (see the LICENSE file)
 */

namespace UnitTests\DI\Definition\Source;

use DI\Definition\EntryReference;
use DI\Definition\Source\AnnotationDefinitionSource;

/**
 * Test class for AnnotationDefinitionSource
 */
class AnnotationDefinitionSourceTest extends \PHPUnit_Framework_TestCase
{
    /**
     * @covers \DI\Definition\Source\AnnotationDefinitionSource::getDefinition
     */
    public function testUnknownClass()
    {
        $source = new AnnotationDefinitionSource();
        $this->assertNull($source->getDefinition('foo'));
    }

    /**
     * @covers \DI\Definition\Source\AnnotationDefinitionSource
     */
    public function testProperty1()
    {
        $source = new AnnotationDefinitionSource();
        $definition = $source->getDefinition('UnitTests\DI\Definition\Source\Fixtures\AnnotationFixture');
        $this->assertInstanceOf('DI\Definition\Definition', $definition);

        $properties = $definition->getPropertyInjections();
        $this->assertInstanceOf('DI\Definition\ClassInjection\PropertyInjection', $properties['property1']);

        $property = $properties['property1'];
        $this->assertEquals('property1', $property->getPropertyName());
        $this->assertEquals(new EntryReference('foo'), $property->getValue());
    }

    /**
     * @covers \DI\Definition\Source\AnnotationDefinitionSource
     */
    public function testConstructor()
    {
        $source = new AnnotationDefinitionSource();
        $definition = $source->getDefinition('UnitTests\DI\Definition\Source\Fixtures\AnnotationFixture');
        $this->assertInstanceOf('DI\Definition\Definition', $definition);

        $constructorInjection = $definition->getConstructorInjection();
        $this->assertInstanceOf('DI\Definition\ClassInjection\MethodInjection', $constructorInjection);

        $parameters = $constructorInjection->getParameters();
        $this->assertCount(2, $parameters);
        $this->assertEquals(new EntryReference('foo'), $parameters['param1']);
        $this->assertEquals(new EntryReference('bar'), $parameters['param2']);
    }

    /**
     * @covers \DI\Definition\Source\AnnotationDefinitionSource
     */
    public function testMethod1()
    {
        $source = new AnnotationDefinitionSource();
        $definition = $source->getDefinition('UnitTests\DI\Definition\Source\Fixtures\AnnotationFixture');
        $this->assertInstanceOf('DI\Definition\Definition', $definition);

        $methodInjections = $definition->getMethodInjections();
        $methodInjection = $methodInjections['method1'];
        $this->assertInstanceOf('DI\Definition\ClassInjection\MethodInjection', $methodInjection);

        $this->assertEmpty($methodInjection->getParameters());
    }

    /**
     * @covers \DI\Definition\Source\AnnotationDefinitionSource
     */
    public function testMethod2()
    {
        $source = new AnnotationDefinitionSource();
        $definition = $source->getDefinition('UnitTests\DI\Definition\Source\Fixtures\AnnotationFixture');
        $this->assertInstanceOf('DI\Definition\Definition', $definition);

        $methodInjections = $definition->getMethodInjections();
        $methodInjection = $methodInjections['method2'];
        $this->assertInstanceOf('DI\Definition\ClassInjection\MethodInjection', $methodInjection);

        $parameters = $methodInjection->getParameters();
        $this->assertCount(2, $parameters);
        $this->assertEquals(new EntryReference('foo'), $parameters['param1']);
        $this->assertEquals(new EntryReference('bar'), $parameters['param2']);
    }

    /**
     * @covers \DI\Definition\Source\AnnotationDefinitionSource
     */
    public function testMethod3()
    {
        $source = new AnnotationDefinitionSource();
        $definition = $source->getDefinition('UnitTests\DI\Definition\Source\Fixtures\AnnotationFixture');
        $this->assertInstanceOf('DI\Definition\Definition', $definition);

        $methodInjections = $definition->getMethodInjections();
        $methodInjection = $methodInjections['method3'];
        $this->assertInstanceOf('DI\Definition\ClassInjection\MethodInjection', $methodInjection);

        $parameters = $methodInjection->getParameters();
        $this->assertCount(2, $parameters);

        $reference = new EntryReference('UnitTests\DI\Definition\Source\Fixtures\AnnotationFixture2');
        $this->assertEquals($reference, $parameters['param1']);
        $this->assertEquals($reference, $parameters['param2']);
    }

    /**
     * @covers \DI\Definition\Source\AnnotationDefinitionSource
     */
    public function testMethod4()
    {
        $source = new AnnotationDefinitionSource();
        $definition = $source->getDefinition('UnitTests\DI\Definition\Source\Fixtures\AnnotationFixture');
        $this->assertInstanceOf('DI\Definition\Definition', $definition);

        $methodInjections = $definition->getMethodInjections();
        $methodInjection = $methodInjections['method4'];
        $this->assertInstanceOf('DI\Definition\ClassInjection\MethodInjection', $methodInjection);

        $parameters = $methodInjection->getParameters();
        $this->assertCount(2, $parameters);
        $this->assertEquals(new EntryReference('foo'), $parameters['param1']);
        $this->assertEquals(new EntryReference('bar'), $parameters['param2']);
    }

    /**
     * @covers \DI\Definition\Source\AnnotationDefinitionSource
     */
    public function testMethod5()
    {
        $source = new AnnotationDefinitionSource();
        $definition = $source->getDefinition('UnitTests\DI\Definition\Source\Fixtures\AnnotationFixture');
        $this->assertInstanceOf('DI\Definition\Definition', $definition);

        $methodInjections = $definition->getMethodInjections();
        $methodInjection = $methodInjections['method5'];
        $this->assertInstanceOf('DI\Definition\ClassInjection\MethodInjection', $methodInjection);

        $parameters = $methodInjection->getParameters();
        $this->assertCount(1, $parameters);

        $this->assertEquals(new EntryReference('bar'), $parameters['param2']);
    }
}