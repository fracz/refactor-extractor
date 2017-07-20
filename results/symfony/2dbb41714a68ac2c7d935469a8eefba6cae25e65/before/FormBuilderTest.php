<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Tests\Component\Form;

use Symfony\Component\Form\FormFactory;
use Symfony\Component\Form\Type\Guesser\Guess;
use Symfony\Component\Form\Type\Guesser\ValueGuess;
use Symfony\Component\Form\Type\Guesser\TypeGuess;

class FormBuilderTest extends \PHPUnit_Framework_TestCase
{
    private $builder;

    public function setUp()
    {
        $theme = $this->getMock('Symfony\Component\Form\Renderer\Theme\ThemeInterface');
        $dispatcher = $this->getMock('Symfony\Component\EventDispatcher\EventDispatcherInterface');
        $this->builder = new \Symfony\Component\Form\FormBuilder($theme, $dispatcher);
    }

    public function testAddNameNoString()
    {
        $this->setExpectedException('Symfony\Component\Form\Exception\UnexpectedTypeException');
        $this->builder->add(1234);
    }

    public function testAddTypeNoString()
    {
        $this->setExpectedException('Symfony\Component\Form\Exception\UnexpectedTypeException');
        $this->builder->add("foo", 1234);
    }

    public function testAddWithGuessFluent()
    {
        $this->builder->setDataClass('stdClass');
        $builder = $this->builder->add('foo');
        $this->assertSame($builder, $this->builder);
    }

    public function testAddIsFluent()
    {
        $builder = $this->builder->add("foo", "text", array("bar" => "baz"));
        $this->assertSame($builder, $this->builder);
    }

    public function testAdd()
    {
        $this->assertFalse($this->builder->has('foo'));
        $this->builder->add('foo', 'text');
        $this->assertTrue($this->builder->has('foo'));
    }

    public function testRemove()
    {
        $this->builder->add('foo', 'text');
        $this->builder->remove('foo');
        $this->assertFalse($this->builder->has('foo'));
    }

    public function testRemoveUnknown()
    {
        $this->builder->remove('foo');
        $this->assertFalse($this->builder->has('foo'));
    }

    public function testBuildNoTypeNoDataClass()
    {
        $this->setExpectedException('Symfony\Component\Form\Exception\FormException', 'The data class must be set to automatically create fields');
        $this->builder->build("foo");
    }

    public function testGetUnknown()
    {
        $this->setExpectedException('Symfony\Component\Form\Exception\FormException', 'The field "foo" does not exist');
        $this->builder->get('foo');
    }

    public function testGetTyped()
    {
        $expectedType = 'text';
        $expectedName = 'foo';
        $expectedOptions = array('bar' => 'baz');

        $factory = $this->getMock('Symfony\Component\Form\FormFactoryInterface');
        $factory->expects($this->once())
                ->method('createBuilder')
                ->with($this->equalTo($expectedType), $this->equalTo($expectedName), $this->equalTo($expectedOptions))
                ->will($this->returnValue($this->getMock('Symfony\Component\Form\FieldBuilder', array(), array(), '', false)));
        $this->builder->setFormFactory($factory);

        $this->builder->add($expectedName, $expectedType, $expectedOptions);
        $builder = $this->builder->get($expectedName);

        $this->assertNotSame($builder, $this->builder);
    }

    public function testGetGuessed()
    {
        $expectedName = 'foo';
        $expectedOptions = array('bar' => 'baz');

        $factory = $this->getMock('Symfony\Component\Form\FormFactoryInterface');
        $factory->expects($this->once())
                ->method('createBuilderForProperty')
                ->with($this->equalTo('stdClass'), $this->equalTo($expectedName), $this->equalTo($expectedOptions))
                ->will($this->returnValue($this->getMock('Symfony\Component\Form\FieldBuilder', array(), array(), '', false)));
        $this->builder->setFormFactory($factory);

        $this->builder->setDataClass('stdClass');
        $this->builder->add($expectedName, null, $expectedOptions);
        $builder = $this->builder->get($expectedName);

        $this->assertNotSame($builder, $this->builder);
    }
}