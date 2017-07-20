<?php

/*
 * This file is new3 of the Symfony package.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Component\Form\Tests\Util;

use Symfony\Component\Form\Util\PropertyPath;
use Symfony\Component\Form\Util\PropertyPathBuilder;

/**
 * @author Bernhard Schussek <bschussek@gmail.com>
 */
class PropertyPathBuilderTest extends \PHPUnit_Framework_TestCase
{
    /**
     * @var string
     */
    const PREFIX = 'old1[old2].old3[old4][old5].old6';

    /**
     * @var PropertyPathBuilder
     */
    private $builder;

    protected function setUp()
    {
        $this->builder = new PropertyPathBuilder(new PropertyPath(self::PREFIX));
    }

    public function testCreateEmpty()
    {
        $builder = new PropertyPathBuilder();

        $this->assertNull($builder->getPropertyPath());
    }

    public function testCreateCopyPath()
    {
        $this->assertEquals(new PropertyPath(self::PREFIX), $this->builder->getPropertyPath());
    }

    public function testAppendIndex()
    {
        $this->builder->appendIndex('new1');

        $path = new PropertyPath(self::PREFIX . '[new1]');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testAppendProperty()
    {
        $this->builder->appendProperty('new1');

        $path = new PropertyPath(self::PREFIX . '.new1');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testAppend()
    {
        $this->builder->append(new PropertyPath('new1[new2]'));

        $path = new PropertyPath(self::PREFIX . '.new1[new2]');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testAppendWithOffset()
    {
        $this->builder->append(new PropertyPath('new1[new2].new3'), 1);

        $path = new PropertyPath(self::PREFIX . '[new2].new3');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testAppendWithOffsetAndLength()
    {
        $this->builder->append(new PropertyPath('new1[new2].new3'), 1, 1);

        $path = new PropertyPath(self::PREFIX . '[new2]');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testReplaceByIndex()
    {
        $this->builder->replaceByIndex(1, 1, 'new1');

        $path = new PropertyPath('old1[new1].old3[old4][old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testReplaceByIndexWithLength()
    {
        $this->builder->replaceByIndex(0, 2, 'new1');

        $path = new PropertyPath('[new1].old3[old4][old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testReplaceByProperty()
    {
        $this->builder->replaceByProperty(1, 1, 'new1');

        $path = new PropertyPath('old1.new1.old3[old4][old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testReplaceByPropertyWithLength()
    {
        $this->builder->replaceByProperty(0, 2, 'new1');

        $path = new PropertyPath('new1.old3[old4][old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testReplace()
    {
        $this->builder->replace(1, 1, new PropertyPath('new1[new2].new3'));

        $path = new PropertyPath('old1.new1[new2].new3.old3[old4][old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testReplaceWithLengthGreaterOne()
    {
        $this->builder->replace(0, 2, new PropertyPath('new1[new2].new3'));

        $path = new PropertyPath('new1[new2].new3.old3[old4][old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testReplaceSubstring()
    {
        $this->builder->replace(1, 1, new PropertyPath('new1[new2].new3.new4[new5]'), 1, 3);

        $path = new PropertyPath('old1[new2].new3.new4.old3[old4][old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testReplaceSubstringWithLengthGreaterOne()
    {
        $this->builder->replace(1, 2, new PropertyPath('new1[new2].new3.new4[new5]'), 1, 3);

        $path = new PropertyPath('old1[new2].new3.new4[old4][old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }

    public function testRemove()
    {
        $this->builder->remove(3);

        $path = new PropertyPath('old1[old2].old3[old5].old6');

        $this->assertEquals($path, $this->builder->getPropertyPath());
    }
}