<?php
/**
 * PHP Version 5.4
 *
 * CakePHP(tm) : Rapid Development Framework (http://cakephp.org)
 * Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright     Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 * @link          http://cakephp.org CakePHP(tm) Project
 * @since         CakePHP(tm) v 3.0.0
 * @license       MIT License (http://www.opensource.org/licenses/mit-license.php)
 */
namespace Cake\Test\TestCase\ORM;

use Cake\ORM\Entity;
use Cake\TestSuite\TestCase;

/**
 * Entity test case.
 */
class EntityTest extends TestCase {

/**
 * Tests setting a single property in an entity without custom setters
 *
 * @return void
 */
	public function testSetOneParamNoSetters() {
		$entity = new Entity;
		$entity->set('foo', 'bar');
		$this->assertEquals('bar', $entity->foo);

		$entity->set('foo', 'baz');
		$this->assertEquals('baz', $entity->foo);

		$entity->set('id', 1);
		$this->assertSame(1, $entity->id);
	}

/**
 * Tests setting multiple properties without custom setters
 *
 * @return void
 */
	public function testSetMultiplePropertiesNoSetters() {
		$entity = new Entity;
		$entity->set(['foo' => 'bar', 'id' => 1]);
		$this->assertEquals('bar', $entity->foo);
		$this->assertSame(1, $entity->id);

		$entity->set(['foo' => 'baz', 'id' => 2, 'thing' => 3]);
		$this->assertEquals('baz', $entity->foo);
		$this->assertSame(2, $entity->id);
		$this->assertSame(3, $entity->thing);
	}

/**
 * Tests setting a single property using a setter function
 *
 * @return void
 */
	public function testSetOneParamWithSetter() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['setName']);
		$entity->expects($this->once())->method('setName')
			->with('Jones')
			->will($this->returnCallback(function($name) {
				$this->assertEquals('Jones', $name);
				return 'Dr. ' . $name;
			}));
		$entity->set('name', 'Jones');
		$this->assertEquals('Dr. Jones', $entity->name);
	}

/**
 * Tests setting multiple properties using a setter function
 *
 * @return void
 */
	public function testMultipleWithSetter() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['setName', 'setStuff']);
		$entity->expects($this->once())->method('setName')
			->with('Jones')
			->will($this->returnCallback(function($name) {
				$this->assertEquals('Jones', $name);
				return 'Dr. ' . $name;
			}));
		$entity->expects($this->once())->method('setStuff')
			->with(['a', 'b'])
			->will($this->returnCallback(function($stuff) {
				$this->assertEquals(['a', 'b'], $stuff);
				return ['c', 'd'];
			}));
		$entity->set(['name' => 'Jones', 'stuff' => ['a', 'b']]);
		$this->assertEquals('Dr. Jones', $entity->name);
		$this->assertEquals(['c', 'd'], $entity->stuff);
	}

/**
 * Tests that it is possible to bypass the setters
 *
 * @return void
 */
	public function testBypassSetters() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['setName', 'setStuff']);
		$entity->expects($this->never())->method('setName');
		$entity->expects($this->never())->method('setStuff');

		$entity->set('name', 'Jones', false);
		$this->assertEquals('Jones', $entity->name);

		$entity->set('stuff', 'Thing', false);
		$this->assertEquals('Thing', $entity->stuff);

		$entity->set(['name' => 'foo', 'stuff' => 'bar'], false);
		$this->assertEquals('bar', $entity->stuff);
	}

/**
 * Tests that the constructor will set initial properties
 *
 * @return void
 */
	public function testConstructor() {
		$entity = $this->getMockBuilder('\Cake\ORM\Entity')
			->setMethods(['set'])
			->disableOriginalConstructor()
			->getMock();
		$entity->expects($this->at(0))
			->method('set')
			->with(['a' => 'b', 'c' => 'd'], true);

		$entity->expects($this->at(1))
			->method('set')
			->with(['foo' => 'bar'], false);

		$entity->__construct(['a' => 'b', 'c' => 'd']);
		$entity->__construct(['foo' => 'bar'], ['useSetters' => false]);
	}

/**
 * Tests getting properties with no custom getters
 *
 * @return void
 */
	public function testGetNoGetters() {
		$entity = new Entity(['id' => 1, 'foo' => 'bar']);
		$this->assertSame(1, $entity->get('id'));
		$this->assertSame('bar', $entity->get('foo'));
	}

/**
 * Tests get with custom getter
 *
 * @return void
 */
	public function testGetCustomGetters() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['getName']);
		$entity->expects($this->once())->method('getName')
			->with('Jones')
			->will($this->returnCallback(function($name) {
				$this->assertSame('Jones', $name);
				return 'Dr. ' . $name;
			}));
		$entity->set('name', 'Jones');
		$this->assertEquals('Dr. Jones', $entity->get('name'));
	}

/**
 * Test magic property setting with no custom setter
 *
 * @return void
 */
	public function testMagicSet() {
		$entity = new Entity;
		$entity->name = 'Jones';
		$this->assertEquals('Jones', $entity->name);
		$entity->name = 'George';
		$this->assertEquals('George', $entity->name);
	}

/**
 * Tests magic set with custom setter function
 *
 * @return void
 */
	public function testMagicSetWithSetter() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['setName']);
		$entity->expects($this->once())->method('setName')
			->with('Jones')
			->will($this->returnCallback(function($name) {
				$this->assertEquals('Jones', $name);
				return 'Dr. ' . $name;
			}));
		$entity->name = 'Jones';
		$this->assertEquals('Dr. Jones', $entity->name);
	}

/**
 * Tests the magic getter with a custom getter function
 *
 * @return void
 */
	public function testMagicGetWithGetter() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['getName']);
		$entity->expects($this->once())->method('getName')
			->with('Jones')
			->will($this->returnCallback(function($name) {
				$this->assertSame('Jones', $name);
				return 'Dr. ' . $name;
			}));
		$entity->set('name', 'Jones');
		$this->assertEquals('Dr. Jones', $entity->name);
	}

/**
 * Test indirectly modifying internal properties
 *
 * @return void
 */
	public function testIndirectModification() {
		$entity = new Entity;
		$entity->things = ['a', 'b'];
		$entity->things[] = 'c';
		$this->assertEquals(['a', 'b', 'c'], $entity->things);
	}

/**
 * Test indirectly modifying internal properties with a getter
 *
 * @return void
 */
	public function testIndirectModificationWithGetter() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['getThings']);
		$entity->expects($this->atLeastOnce())->method('getThings')
			->will($this->returnArgument(0));
		$entity->things = ['a', 'b'];
		$entity->things[] = 'c';
		$this->assertEquals(['a', 'b', 'c'], $entity->things);
	}

/**
 * Tests has() method
 *
 * @return void
 */
	public function testHas() {
		$entity = new Entity(['id' => 1, 'name' => 'Juan', 'foo' => null]);
		$this->assertTrue($entity->has('id'));
		$this->assertTrue($entity->has('name'));
		$this->assertFalse($entity->has('foo'));
		$this->assertFalse($entity->has('last_name'));

		$entity = $this->getMock('\Cake\ORM\Entity', ['getThings']);
		$entity->expects($this->once())->method('getThings')
			->will($this->returnValue(0));
		$this->assertTrue($entity->has('things'));
	}

/**
 * Tests unsetProperty one property at a time
 *
 * @return void
 */
	public function testUnset() {
		$entity = new Entity(['id' => 1, 'name' => 'bar']);
		$entity->unsetProperty('id');
		$this->assertFalse($entity->has('id'));
		$this->assertTrue($entity->has('name'));
		$entity->unsetProperty('name');
		$this->assertFalse($entity->has('id'));
	}

/**
 * Tests unsetProperty whith multiple properties
 *
 * @return void
 */
	public function testUnsetMultiple() {
		$entity = new Entity(['id' => 1, 'name' => 'bar', 'thing' => 2]);
		$entity->unsetProperty(['id', 'thing']);
		$this->assertFalse($entity->has('id'));
		$this->assertTrue($entity->has('name'));
		$this->assertFalse($entity->has('thing'));
	}

/**
 * Tests the magic __isset() method
 *
 * @return void
 */
	public function testMagicIsset() {
		$entity = new Entity(['id' => 1, 'name' => 'Juan', 'foo' => null]);
		$this->assertTrue(isset($entity->id));
		$this->assertTrue(isset($entity->name));
		$this->assertFalse(isset($entity->foo));
		$this->assertFalse(isset($entity->thing));
	}

/**
 * Tests the magic __unset() method
 *
 * @return void
 */
	public function testMagicUnset() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['unsetProperty']);
		$entity->expects($this->at(0))
			->method('unsetProperty')
			->with('foo');
		unset($entity->foo);
	}

/**
 * Tests isset with array access
 *
 * @return void
 */
	public function testIssetArrayAccess() {
		$entity = new Entity(['id' => 1, 'name' => 'Juan', 'foo' => null]);
		$this->assertTrue(isset($entity['id']));
		$this->assertTrue(isset($entity['name']));
		$this->assertFalse(isset($entity['foo']));
		$this->assertFalse(isset($entity['thing']));
	}

/**
 * Tests get property with array access
 *
 * @return void
 */
	public function testGetArrayAccess() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['get']);
		$entity->expects($this->at(0))
			->method('get')
			->with('foo')
			->will($this->returnValue('worked'));

		$entity->expects($this->at(1))
			->method('get')
			->with('bar')
			->will($this->returnValue('worked too'));

		$this->assertEquals('worked', $entity['foo']);
		$this->assertEquals('worked too', $entity['bar']);
	}

/**
 * Tests set with array access
 *
 * @return void
 */
	public function testSetArrayAccess() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['set']);
		$entity->expects($this->at(0))
			->method('set')
			->with(['foo' => 1])
			->will($this->returnSelf());

		$entity->expects($this->at(1))
			->method('set')
			->with(['bar' => 2])
			->will($this->returnSelf());

		$entity['foo'] = 1;
		$entity['bar'] = 2;
	}

/**
 * Tests unset with array access
 *
 * @return void
 */
	public function testUnsetArrayAccess() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['unsetProperty']);
		$entity->expects($this->at(0))
			->method('unsetProperty')
			->with('foo');
		unset($entity['foo']);
	}

/**
 * Tests that the method cache will only report the methods for the called class,
 * this is, calling methods defined in another entity will not cause a fatal error
 * when trying to call directly an inexistent method in another class
 *
 * @return void
 */
	public function testMethodCache() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['setFoo', 'getBar']);
		$entity2 = $this->getMock('\Cake\ORM\Entity', ['setBar']);
		$entity->expects($this->once())->method('setFoo');
		$entity->expects($this->once())->method('getBar');
		$entity2->expects($this->once())->method('setBar');

		$entity->set('foo', 1);
		$entity->get('bar');
		$entity2->set('bar', 1);
	}

/**
 * Tests that long properties in the entity are inflected correctly
 *
 * @return void
 */
	public function testSetGetLongProperyNames() {
		$entity = $this->getMock('\Cake\ORM\Entity', ['getVeryLongProperty', 'setVeryLongProperty']);
		$entity->expects($this->once())->method('getVeryLongProperty');
		$entity->expects($this->once())->method('setVeryLongProperty');
		$entity->get('very_long_property');
		$entity->set('very_long_property', 1);
	}

/**
 * Tests serializing an entity as json
 *
 * @return void
 */
	public function testJsonSerialize() {
		$data = ['name' => 'James', 'age' => 20, 'phones' => ['123', '457']];
		$entity = new Entity($data);
		$this->assertEquals(json_encode($data), json_encode($entity));
	}

/**
 * Tests the extract method
 *
 * @return void
 */
	public function testExtract() {
		$entity = new \Cake\ORM\Entity([
			'id' => 1,
			'title' => 'Foo',
			'author_id' => 3
		]);
		$expected = ['author_id' => 3, 'title' => 'Foo', ];
		$this->assertEquals($expected, $entity->extract(['author_id', 'title']));

		$expected = ['id' => 1];
		$this->assertEquals($expected, $entity->extract(['id']));

		$expected = [];
		$this->assertEquals($expected, $entity->extract([]));

		$expected = ['id' => 1, 'crazyness' => null];
		$this->assertEquals($expected, $entity->extract(['id', 'crazyness']));
	}

/**
 * Tests dirty() method on a newly created object
 *
 * @return void
 */
	public function testDirty() {
		$entity = new \Cake\ORM\Entity([
			'id' => 1,
			'title' => 'Foo',
			'author_id' => 3
		]);
		$this->assertTrue($entity->dirty('id'));
		$this->assertTrue($entity->dirty('title'));
		$this->assertTrue($entity->dirty('author_id'));

		$entity->dirty('id', false);
		$this->assertFalse($entity->dirty('id'));
		$this->assertTrue($entity->dirty('title'));
		$entity->dirty('title', false);
		$this->assertFalse($entity->dirty('title'));
	}

/**
 * Tests dirty() when altering properties values and adding new ones
 *
 * @return void
 */
	public function testDirtyChangingProperties() {
		$entity = new \Cake\ORM\Entity([
			'title' => 'Foo',
		]);
		$entity->dirty('title', false);
		$this->assertFalse($entity->dirty('title'));
		$entity->set('title', 'Foo');
		$this->assertFalse($entity->dirty('title'));
		$entity->set('title', 'Foo');
		$this->assertFalse($entity->dirty('title'));
		$entity->set('title', 'Something Else');
		$this->assertTrue($entity->dirty('title'));

		$entity->set('something', 'else');
		$this->assertTrue($entity->dirty('something'));
	}

/**
 * Tests extract only dirty properties
 *
 * @return void
 */
	public function testExtractDirty() {
		$entity = new \Cake\ORM\Entity([
			'id' => 1,
			'title' => 'Foo',
			'author_id' => 3
		]);
		$entity->dirty('id', false);
		$entity->dirty('title', false);
		$expected = ['author_id' => 3];
		$result = $entity->extract(['id', 'title', 'author_id'], true);
		$this->assertEquals($expected, $result);
	}

/**
 * Tests the clean method
 *
 * @return void
 */
	public function testClean() {
		$entity = new \Cake\ORM\Entity([
			'id' => 1,
			'title' => 'Foo',
			'author_id' => 3
		]);
		$this->assertTrue($entity->dirty('id'));
		$this->assertTrue($entity->dirty('title'));
		$this->assertTrue($entity->dirty('author_id'));

		$entity->clean();
		$this->assertFalse($entity->dirty('id'));
		$this->assertFalse($entity->dirty('title'));
		$this->assertFalse($entity->dirty('author_id'));
	}

/**
 * Tests the isNew method
 *
 * @return void
 */
	public function testIsNew() {
		$entity = new \Cake\ORM\Entity([
			'id' => 1,
			'title' => 'Foo',
			'author_id' => 3
		]);
		$this->assertNull($entity->isNew());
		$entity->isNew(true);
		$this->assertTrue($entity->isNew());
		$entity->isNew(false);
		$this->assertFalse($entity->isNew());
	}

/**
 * Tests the constructor when passing the markClean option
 *
 * @return void
 */
	public function testConstructorWithClean() {
		$entity = $this->getMockBuilder('\Cake\ORM\Entity')
			->setMethods(['clean'])
			->disableOriginalConstructor()
			->getMock();
		$entity->expects($this->never())->method('clean');
		$entity->__construct(['a' => 'b', 'c' => 'd']);

		$entity = $this->getMockBuilder('\Cake\ORM\Entity')
			->setMethods(['clean'])
			->disableOriginalConstructor()
			->getMock();
		$entity->expects($this->once())->method('clean');
		$entity->__construct(['a' => 'b', 'c' => 'd'], ['markClean' => true]);
	}

/**
 * Tests the constructor when passing the markClean option
 *
 * @return void
 */
	public function testConstructorWithMarkNew() {
		$entity = $this->getMockBuilder('\Cake\ORM\Entity')
			->setMethods(['isNew'])
			->disableOriginalConstructor()
			->getMock();
		$entity->expects($this->never())->method('clean');
		$entity->__construct(['a' => 'b', 'c' => 'd']);

		$entity = $this->getMockBuilder('\Cake\ORM\Entity')
			->setMethods(['isNew'])
			->disableOriginalConstructor()
			->getMock();
		$entity->expects($this->once())->method('isNew');
		$entity->__construct(['a' => 'b', 'c' => 'd'], ['markNew' => true]);
	}

}