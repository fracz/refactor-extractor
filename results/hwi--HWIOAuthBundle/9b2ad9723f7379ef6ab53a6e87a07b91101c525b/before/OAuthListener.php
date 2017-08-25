<?php

namespace Knp\OauthBundle\Security\Http\Firewall;

use Symfony\Component\Security\Http\Firewall\AbstractAuthenticationListener;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Security\Core\Exception\AuthenticationException;
use Knp\OAuthBundle\Security\Core\Authentication\Token\OAuthToken;
use Buzz\Client\ClientInterface as HttpClientInterface,
    Buzz\Message\Request as HttpRequest,
    Buzz\Message\Response as HttpResponse;

class OAuthListener extends AbstractAuthenticationListener
{
    private $httpClient, $oauthOptions;

    public function setHttpClient(HttpClientInterface $client)
    {
        $this->httpClient = $client;
    }

    public function setOAuthOptions(array $options)
    {
        $this->oauthOptions = $options;
    }

    protected function attemptAuthentication(Request $request)
    {
        if (!is_object($this->httpClient)) {
            throw new \InvalidArgumentException(sprintf('Could not use "%s" as an HTTP client', var_export($this->httpClient, true)));
        }

        if (!$this->httpClient instanceof HttpClientInterface) {
            throw new \InvalidArgumentException(sprintf('Could not use instance of "%s" as an HTTP client', get_class($this->httpClass)));
        }

        $accessTokenUrl  = $this->getAccessTokenUrl($request);

        $hRequest        = new HttpRequest(HttpRequest::METHOD_GET, $accessTokenUrl);
        $hResponse       = new HttpResponse();

        $this->httpClient->send($hRequest, $hResponse);

        $response = array();

        parse_str($hResponse->getContent(), $response);

        if (null === $response || isset($response['error'])) {
            return;
        }

        $token = new OAuthToken($response['access_token']);

        return $this->authenticationManager->authenticate($token);
    }

    private function getAccessTokenUrl(Request $request)
    {
        return $this->oauthOptions['access_token_url'].'?'.http_build_query(array(
            'client_id'     => $this->oauthOptions['client_id'],
            'client_secret' => $this->oauthOptions['secret'],
            'code'          => $request->get('code'),
            'grant_type'    => 'authorization_code',
            'redirect_url'  => urldecode($request->get('redirect_uri'))
        ));
    }
}