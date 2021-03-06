<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Tests\Component\Templating\Loader;

require_once __DIR__.'/../Fixtures/ProjectTemplateDebugger.php';

use Symfony\Component\Templating\Loader\ChainLoader;
use Symfony\Component\Templating\Loader\FilesystemLoader;
use Symfony\Component\Templating\Storage\FileStorage;
use Symfony\Component\Templating\Loader\TemplateNameParser;

class ChainLoaderTest extends \PHPUnit_Framework_TestCase
{
    protected $loader1;
    protected $loader2;

    public function setUp()
    {
        $fixturesPath = realpath(__DIR__.'/../Fixtures/');
        $this->loader1 = new FilesystemLoader(new TemplateNameParser(), $fixturesPath.'/null/%name%');
        $this->loader2 = new FilesystemLoader(new TemplateNameParser(), $fixturesPath.'/templates/%name%');
    }

    public function testConstructor()
    {
        $loader = new ProjectTemplateLoader1(array($this->loader1, $this->loader2));
        $this->assertEquals(array($this->loader1, $this->loader2), $loader->getLoaders(), '__construct() takes an array of template loaders as its second argument');
    }

    public function testAddLoader()
    {
        $loader = new ProjectTemplateLoader1(array($this->loader1));
        $loader->addLoader($this->loader2);
        $this->assertEquals(array($this->loader1, $this->loader2), $loader->getLoaders(), '->addLoader() adds a template loader at the end of the loaders');
    }

    public function testLoad()
    {
        $loader = new ProjectTemplateLoader1(array($this->loader1, $this->loader2));
        $this->assertFalse($loader->load('bar'), '->load() returns false if the template is not found');
        $this->assertFalse($loader->load('foo'), '->load() returns false if the template does not exists for the given renderer');
        $this->assertInstanceOf('Symfony\Component\Templating\Storage\FileStorage', $loader->load('foo.php'), '->load() returns a FileStorage if the template exists');
    }
}

class ProjectTemplateLoader1 extends ChainLoader
{
    public function getLoaders()
    {
        return $this->loaders;
    }
}