<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Tests\Component\Form\Type;

use Symfony\Component\Form\FormBuilder;
use Symfony\Component\Form\FormFactory;
use Symfony\Component\Form\Type\Loader\DefaultTypeLoader;
use Symfony\Component\Form\Type\Loader\TypeLoaderChain;
use Symfony\Component\EventDispatcher\EventDispatcher;

abstract class TestCase extends \PHPUnit_Framework_TestCase
{
    protected $themeFactory;

    protected $csrfProvider;

    protected $validator;

    protected $storage;

    protected $factory;

    protected $builder;

    protected $dispatcher;

    protected $typeLoader;

    protected $rendererFactoryLoader;

    protected function setUp()
    {
        $this->themeFactory = $this->getMock('Symfony\Component\Form\Renderer\Theme\FormThemeFactoryInterface');
        $this->themeFactory->expects($this->any())
            ->method('create')
            ->will($this->returnValue($this->getMock('Symfony\Component\Form\Renderer\Theme\FormThemeInterface')));
        $this->csrfProvider = $this->getMock('Symfony\Component\Form\CsrfProvider\CsrfProviderInterface');
        $this->validator = $this->getMock('Symfony\Component\Validator\ValidatorInterface');
        $this->dispatcher = $this->getMock('Symfony\Component\EventDispatcher\EventDispatcherInterface');
        $this->rendererFactoryLoader = $this->getMock('Symfony\Component\Form\Renderer\Loader\FormRendererFactoryLoaderInterface');

        $this->storage = $this->getMockBuilder('Symfony\Component\HttpFoundation\File\TemporaryStorage')
            ->disableOriginalConstructor()
            ->getMock();

        $this->typeLoader = new TypeLoaderChain();

        // TODO should be passed to chain constructor instead
        foreach ($this->getTypeLoaders() as $loader) {
            $this->typeLoader->addLoader($loader);
        }

        $this->factory = new FormFactory($this->typeLoader, $this->rendererFactoryLoader);

        $this->builder = new FormBuilder($this->dispatcher);
    }

    protected function getTypeLoaders()
    {
        return array(new DefaultTypeLoader($this->themeFactory, null,
                $this->validator, $this->csrfProvider, $this->storage));
    }
}