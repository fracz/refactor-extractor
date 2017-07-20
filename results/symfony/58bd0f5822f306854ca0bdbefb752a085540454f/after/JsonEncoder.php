<?php

namespace Symfony\Component\Serializer\Encoder;


/*
 * This file is part of the Symfony framework.
 *
 * (c) Fabien Potencier <fabien@symfony.com>
 *
 * This source file is subject to the MIT license that is bundled
 * with this source code in the file LICENSE.
 */

/**
 * Encodes JSON data
 *
 * @author Jordi Boggiano <j.boggiano@seld.be>
 */
class JsonEncoder implements EncoderInterface, DecoderInterface
{
    /**
     * {@inheritdoc}
     */
    public function encode($data, $format)
    {
        return json_encode($data);
    }

    /**
     * {@inheritdoc}
     */
    public function decode($data, $format)
    {
        return json_decode($data, true);
    }

    /**
     * Checks whether the serializer can encode to given format
     *
     * @param string $format format name
     * @return Boolean
     */
    public function supportsEncoding($format)
    {
        return 'json' === $format;
    }

    /**
     * Checks whether the serializer can decode from given format
     *
     * @param string $format format name
     * @return Boolean
     */
    public function supportsDecoding($format)
    {
        return 'json' === $format;
    }
}