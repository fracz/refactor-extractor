<?php

/*
 * This file is part of the Symfony package.
 *
 * (c) Fabien Potencier <fabien.potencier@symfony-project.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Symfony\Component\Form\Type;

use Symfony\Component\Form\FormBuilder;
use Symfony\Component\Form\Renderer\Theme\ThemeInterface;
use Symfony\Component\Form\CsrfProvider\CsrfProviderInterface;
use Symfony\Component\Form\DataMapper\PropertyPathMapper;
use Symfony\Component\Form\Renderer\Plugin\FormPlugin;
use Symfony\Component\EventDispatcher\EventDispatcher;

class FormType extends AbstractType
{
    public function configure(FormBuilder $builder, array $options)
    {
        $builder->setAttribute('virtual', $options['virtual'])
            ->addRendererPlugin(new FormPlugin())
            ->setDataClass($options['data_class'])
            ->setDataMapper(new PropertyPathMapper(
                $options['data_class'],
                $options['data_constructor']
            ));

        if ($options['csrf_protection']) {
            $builder->addCsrfProtection($options['csrf_provider'], $options['csrf_field_name']);
        }
    }

    public function getDefaultOptions(array $options)
    {
        return array(
            'template' => 'form',
            'data_class' => null,
            'data_constructor' => null,
            'csrf_protection' => true,
            'csrf_field_name' => '_token',
            'csrf_provider' => null,
            'validation_groups' => null,
            'virtual' => false,
        );
    }

    public function getParent(array $options)
    {
        return 'field';
    }

    public function getName()
    {
        return 'form';
    }
}