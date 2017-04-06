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

use Symfony\Component\Form\PropertyPath;
use Symfony\Component\Form\FieldBuilder;
use Symfony\Component\Form\Renderer\DefaultRenderer;
use Symfony\Component\Form\Renderer\Theme\ThemeInterface;
use Symfony\Component\Form\Renderer\Plugin\FieldPlugin;
use Symfony\Component\Form\EventListener\TrimListener;
use Symfony\Component\Form\EventListener\ValidationListener;
use Symfony\Component\Form\CsrfProvider\CsrfProviderInterface;
use Symfony\Component\Form\Validator\DelegatingValidator;
use Symfony\Component\EventDispatcher\EventDispatcher;
use Symfony\Component\Validator\ValidatorInterface;

class FieldType extends AbstractType
{
    private $theme;

    private $validator;

    public function __construct(ThemeInterface $theme, ValidatorInterface $validator)
    {
        $this->theme = $theme;
        $this->validator = $validator;
    }

    public function configure(FieldBuilder $builder, array $options)
    {
        if (false === $options['property_path']) {
            $options['property_path'] = $builder->getName();
        }

        if (null === $options['property_path'] || '' === $options['property_path']) {
            $options['property_path'] = null;
        } else {
            $options['property_path'] = new PropertyPath($options['property_path']);
        }

        $options['validation_groups'] = empty($options['validation_groups'])
            ? null
            : (array)$options['validation_groups'];

        $builder->setRequired($options['required'])
            ->setReadOnly($options['read_only'])
            ->setAttribute('by_reference', $options['by_reference'])
            ->setAttribute('property_path', $options['property_path'])
            ->setAttribute('validation_groups', $options['validation_groups'])
            ->setData($options['data'])
            ->setRenderer(new DefaultRenderer($this->theme, $options['template']))
            ->addRendererPlugin(new FieldPlugin())
            ->addValidator(new DelegatingValidator($this->validator));

        if ($options['trim']) {
            $builder->addEventSubscriber(new TrimListener());
        }
    }

    public function getDefaultOptions(array $options)
    {
        return array(
            'template' => 'text',
            'data' => null,
            'trim' => true,
            'required' => true,
            'read_only' => false,
            'max_length' => null,
            'property_path' => false,
            'by_reference' => true,
            'validation_groups' => true,
        );
    }

    public function createBuilder(array $options)
    {
        return new FieldBuilder($this->theme, new EventDispatcher());
    }

    public function getParent(array $options)
    {
        return null;
    }

    public function getName()
    {
        return 'field';
    }
}