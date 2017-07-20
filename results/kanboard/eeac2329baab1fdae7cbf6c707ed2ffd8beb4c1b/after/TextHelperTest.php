<?php

require_once __DIR__.'/Base.php';

use Helper\Text;

class TextHelperTest extends Base
{
    public function testMarkdown()
    {
        $h = new Text($this->container);

        $this->assertEquals('<p>Test</p>', $h->markdown('Test'));

        $this->assertEquals(
            '<p>Task #123</p>',
            $h->markdown('Task #123')
        );

        $this->assertEquals(
            '<p>Task <a href="?controller=a&amp;action=b&amp;c=d&amp;task_id=123">#123</a></p>',
            $h->markdown('Task #123', array('controller' => 'a', 'action' => 'b', 'params' => array('c' => 'd')))
        );

        $this->assertEquals(
            '<p>Check that: <a href="http://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags/1732454#1732454">http://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags/1732454#1732454</a></p>',
            $h->markdown(
                'Check that: http://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags/1732454#1732454',
                array('controller' => 'a', 'action' => 'b', 'params' => array('c' => 'd'))
            )
        );
    }

    public function testFormatBytes()
    {
        $h = new Text($this->container);

        $this->assertEquals('1k', $h->bytes(1024));
        $this->assertEquals('33.71k', $h->bytes(34520));
    }

    public function testTruncate()
    {
        $h = new Text($this->container);

        $this->assertEquals('abc', $h->truncate('abc'));
        $this->assertEquals(str_repeat('a', 85).' [...]', $h->truncate(str_repeat('a', 200)));
    }

    public function testContains()
    {
        $h = new Text($this->container);

        $this->assertTrue($h->contains('abc', 'b'));
        $this->assertFalse($h->contains('abc', 'd'));
    }

    public function testInList()
    {
        $h = new Text($this->container);
        $this->assertEquals('?', $h->in('a', array('b' => 'c')));
        $this->assertEquals('c', $h->in('b', array('b' => 'c')));
    }
}