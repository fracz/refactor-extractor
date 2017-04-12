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

use Symfony\Component\Form\FieldBuilder;
use Symfony\Component\Form\DataTransformer\ReversedTransformer;
use Symfony\Component\Form\DataTransformer\DataTransformerChain;
use Symfony\Component\Form\DataTransformer\DateTimeToArrayTransformer;
use Symfony\Component\Form\DataTransformer\DateTimeToStringTransformer;
use Symfony\Component\Form\DataTransformer\DateTimeToTimestampTransformer;
use Symfony\Component\Form\DataTransformer\ArrayToPartsTransformer;

class DateTimeType extends AbstractType
{
    public function configure(FieldBuilder $builder, array $options)
    {
        // Only pass a subset of the options to children
        $dateOptions = array_intersect_key($options, array_flip(array(
            'years',
            'months',
            'days',
        )));
        $timeOptions = array_intersect_key($options, array_flip(array(
            'hours',
            'minutes',
            'seconds',
            'with_seconds',
        )));

        if (isset($options['date_pattern'])) {
            $dateOptions['pattern'] = $options['date_pattern'];
        }
        if (isset($options['date_widget'])) {
            $dateOptions['widget'] = $options['date_widget'];
        }
        if (isset($options['date_format'])) {
            $dateOptions['format'] = $options['date_format'];
        }

        $dateOptions['input'] = 'array';

        if (isset($options['time_pattern'])) {
            $timeOptions['pattern'] = $options['time_pattern'];
        }
        if (isset($options['time_widget'])) {
            $timeOptions['widget'] = $options['time_widget'];
        }
        if (isset($options['time_format'])) {
            $timeOptions['format'] = $options['time_format'];
        }

        $timeOptions['input'] = 'array';

        $parts = array('year', 'month', 'day', 'hour', 'minute');
        $timeParts = array('hour', 'minute');

        if ($options['with_seconds']) {
            $parts[] = 'second';
            $timeParts[] = 'second';
        }

        $builder->setClientTransformer(new DataTransformerChain(array(
                new DateTimeToArrayTransformer(array(
                    'input_timezone' => $options['data_timezone'],
                    'output_timezone' => $options['user_timezone'],
                    'fields' => $parts,
                )),
                new ArrayToPartsTransformer(array(
                    'date' => array('year', 'month', 'day'),
                    'time' => $timeParts,
                )),
            )))
            ->add('date', 'date', $dateOptions)
            ->add('time', 'time', $timeOptions);

        if ($options['input'] === 'string') {
            $builder->setNormTransformer(new ReversedTransformer(
                new DateTimeToStringTransformer(array(
                    'format' => 'Y-m-d H:i:s',
                    'input_timezone' => $options['data_timezone'],
                    'output_timezone' => $options['data_timezone'],
                ))
            ));
        } else if ($options['input'] === 'timestamp') {
            $builder->setNormTransformer(new ReversedTransformer(
                new DateTimeToTimestampTransformer(array(
                    'input_timezone' => $options['data_timezone'],
                    'output_timezone' => $options['data_timezone'],
                ))
            ));
        } else if ($options['input'] === 'array') {
            $builder->setNormTransformer(new ReversedTransformer(
                new DateTimeToArrayTransformer(array(
                    'input_timezone' => $options['data_timezone'],
                    'output_timezone' => $options['data_timezone'],
                    'fields' => $parts,
                ))
            ));
        }
    }

    public function getDefaultOptions(array $options)
    {
        return array(
            'template' => 'datetime',
            'input' => 'datetime',
            'with_seconds' => false,
            'data_timezone' => date_default_timezone_get(),
            'user_timezone' => date_default_timezone_get(),
            // Don't modify \DateTime classes by reference, we treat
            // them like immutable value objects
            'by_reference' => false,
        );
    }

    public function getParent(array $options)
    {
        return 'form';
    }

    public function getName()
    {
        return 'datetime';
    }
}