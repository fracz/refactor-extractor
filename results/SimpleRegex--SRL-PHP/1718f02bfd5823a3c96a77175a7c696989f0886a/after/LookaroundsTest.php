<?php

namespace Tests;

use SRL\Builder;
use SRL\SRL;

class LookaroundsTest extends TestCase
{
    public function testPositiveLookahead()
    {
        $query = SRL::literally('foo')->ifFollowedBy(function (Builder $query) {
            $query->literally('bar');
        });
        $this->assertTrue($query->isMatching('foobar'));
        $this->assertFalse($query->isMatching('foobaz'));
        $this->assertFalse($query->isMatching('barfoo'));

        $query = SRL::literally('foo')->ifFollowedBy('bar');
        $this->assertTrue($query->isMatching('foobar'));
        $this->assertFalse($query->isMatching('foobaz'));
    }

    public function testNegativeLookahead()
    {
        $query = SRL::literally('foo')->ifNotFollowedBy(SRL::literally('bar'));
        $this->assertFalse($query->isMatching('foobar'));
        $this->assertTrue($query->isMatching('foobazbar'));
    }
}