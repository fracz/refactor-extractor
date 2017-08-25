<?php

/*
 * This file is part of the HWIOAuthBundle package.
 *
 * (c) Hardware.Info <opensource@hardware.info>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace HWI\Bundle\OAuthBundle\OAuth\ResourceOwner;

use Buzz\Client\ClientInterface as HttpClientInterface,
    Buzz\Message\Request as HttpRequest,
    Buzz\Message\Response as HttpResponse;

use Symfony\Component\Security\Core\Exception\AuthenticationException,
    Symfony\Component\Security\Http\HttpUtils;

use HWI\Bundle\OAuthBundle\OAuth\ResourceOwnerInterface,
    HWI\Bundle\OAuthBundle\OAuth\Response\PathUserResponse;

/**
 * GenericResourceOwner
 *
 * @author Geoffrey Bachelet <geoffrey.bachelet@gmail.com>
 * @author Alexander <iam.asm89@gmail.com>
 */
class GenericResourceOwner implements ResourceOwnerInterface
{
    /**
     * @var array
     */
    protected $options = array();

    /**
     * @var Buzz\Client\ClientInterface
     */
    protected $httpClient;

    /**
     * @param Buzz\Client\ClientInterface $httpClient
     * @param array                       $options
     */
    public function __construct(HttpClientInterface $httpClient, HttpUtils $httpUtils, array $options)
    {
        // Merge the options, then validate them
        $this->options = array_merge($this->options, $options);

        if (null !== $this->options['infos_url'] && null === $this->options['username_path']) {
            throw new \InvalidArgumentException('You must set an "username_path" to use an "infos_url"');
        }

        if (null === $this->options['infos_url'] && null !== $this->options['username_path']) {
            throw new \InvalidArgumentException('You must set an "infos_url" to use an "username_path"');
        }

        $this->httpClient = $httpClient;
        $this->httpUtils  = $httpUtils;

        $this->configure();
    }

    /**
     * Gives a chance for extending providers to customize stuff
     */
    public function configure()
    {

    }

    /**
     * Retrieve an option by name
     *
     * @throws InvalidArgumentException When the option does not exist
     * @param string                    $name The option name
     * @return mixed                    The option value
     */
    public function getOption($name)
    {
        if (!array_key_exists($name, $this->options)) {
            throw new \InvalidArgumentException(sprintf('Unknown option "%s"', $name));
        }

        return $this->options[$name];
    }

    /**
     * Performs an HTTP request
     *
     * @param string $url The url to fetch
     * @param string $method The HTTP method to use
     * @return string The response content
     */
    protected function httpRequest($url, $content = null, $method = null)
    {
        if (null === $method) {
            $method = null === $content ? HttpRequest::METHOD_GET : HttpRequest::METHOD_POST;
        }

        $request  = new HttpRequest($method, $url);
        $response = new HttpResponse();

        $request->setContent($content);

        $this->httpClient->send($request, $response);

        return $response->getContent();
    }

    /**
     * {@inheritDoc}
     */
    public function getUserInformation($accessToken)
    {
        if ($this->getOption('infos_url') === null) {
            return $accessToken;
        }

        $url = $this->getOption('infos_url').'?'.http_build_query(array(
            'access_token' => $accessToken
        ));

        $response = $this->getUserResponse();
        $response->setResponse($this->httpRequest($url));

        return $response;
    }

    /**
     * Get the response object to return.
     *
     * @return UserResponseInterface
     */
    protected function getUserResponse()
    {
        if (!isset($this->options['user_response_class'])) {
            $response = new PathUserResponse();
            $response->setPaths(array('username_path' => $this->getOption('username_path')));

            return $response;
        }

        return new $this->options['user_response_class'];
    }

    /**
     * {@inheritDoc}
     */
    public function getAuthorizationUrl($redirectUri, array $extraParameters = array())
    {
        $parameters = array_merge($extraParameters, array(
            'response_type' => 'code',
            'client_id'     => $this->getOption('client_id'),
            'scope'         => $this->getOption('scope'),
            'redirect_uri'  => $redirectUri,
        ));

        return $this->getOption('authorization_url').'?'.http_build_query($parameters);
    }

    /**
     * {@inheritDoc}
     */
    public function getAccessToken($code, $redirectUri, array $extraParameters = array())
    {
        $parameters = array_merge($extraParameters, array(
            'code'          => $code,
            'grant_type'    => 'authorization_code',
            'client_id'     => $this->getOption('client_id'),
            'client_secret' => $this->getOption('client_secret'),
            'redirect_uri'  => $redirectUri,
        ));

        $url = $this->getOption('access_token_url').'?'.http_build_query($parameters);

        $response = array();

        parse_str($this->httpRequest($url), $response);

        if (isset($response['error'])) {
            throw new AuthenticationException(sprintf('OAuth error: "%s"', $response['error']));
        }

        return $response['access_token'];
    }
}