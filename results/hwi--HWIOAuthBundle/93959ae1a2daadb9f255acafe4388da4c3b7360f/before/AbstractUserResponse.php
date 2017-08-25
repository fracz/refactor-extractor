<?php

/*
 * This file is part of the KnpOAuthBundle package.
 *
 * (c) KnpLabs <hello@knplabs.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Knp\Bundle\OAuthBundle\OAuth\Response;

/**
 * AbstractUserResponse
 *
 * @author Alexander <iam.asm89@gmail.com>
 */
abstract class AbstractUserResponse implements UserResponseInterface
{
    /**
     * @var array
     */
    protected $response;

    /**
     * {@inheritdoc}
     */
    public function getResponse()
    {
        return $this->response;
    }

    /**
     * {@inheritdoc}
     */
    public function setResponse($response)
    {
        $this->response = json_decode($response, true);
    }
}