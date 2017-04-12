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

use Symfony\Component\Form\FormInterface;
use Symfony\Component\Form\FormBuilder;
use Symfony\Component\Form\DataTransformer\BooleanToStringTransformer;
use Symfony\Component\Form\Renderer\FormRendererInterface;

class RadioType extends AbstractType
{
    public function configure(FormBuilder $builder, array $options)
    {
        $builder->setClientTransformer(new BooleanToStringTransformer())
            ->setAttribute('value', $options['value']);
    }

    public function buildRenderer(FormRendererInterface $renderer, FormInterface $form)
    {
        $renderer->setBlock('radio');
        $renderer->setVar('value', $form->getAttribute('value'));
        $renderer->setVar('checked', (bool)$form->getData());

        if ($renderer->hasParent()) {
            $renderer->setVar('name', $renderer->getParent()->getVar('name'));
        }
    }

    public function getDefaultOptions(array $options)
    {
        return array(
            'value' => null,
        );
    }

    public function getParent(array $options)
    {
        return 'field';
    }

    public function getName()
    {
        return 'radio';
    }
}