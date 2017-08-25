<?php
/**
 * Fluent storage implementation for an OAuth 2.0 Client
 *
 * @package   lucadegasperi/oauth2-server-laravel
 * @author    Luca Degasperi <luca@lucadegasperi.com>
 * @copyright Copyright (c) Luca Degasperi
 * @licence   http://mit-license.org/
 * @link      https://github.com/lucadegasperi/oauth2-server-laravel
 */

namespace LucaDegasperi\OAuth2Server\Repositories;

use League\OAuth2\Server\Storage\ClientInterface;
use League\OAuth2\Server\Storage\Adapter;
use League\OAuth2\Server\Entity\Client;
use DB;

class FluentClient extends Adapter implements ClientInterface
{
    protected $limitClientsToGrants = false;

    /**
     * @param bool $limitClientsToGrants
     */
    public function __construct($limitClientsToGrants = false)
    {
        $this->limitClientsToGrants = $limitClientsToGrants;
    }

    /**
     * @return bool
     */
    public function areClientsLimitedToGrants()
    {
        return $this->limitClientsToGrants;
    }

    /**
     * @param bool $limit whether or not to limit clients to grants
     */
    public function limitClientsToGrants($limit = false)
    {
        $this->limitClientsToGrants = $limit;
    }

    /**
     * {@inheritdoc}
     */
    public function get($clientId, $clientSecret = null, $redirectUri = null, $grantType = null)
    {
        $query = null;

        if (! is_null($redirectUri) && is_null($clientSecret)) {
            $query = DB::table('oauth_clients')
                   ->select(
                       'oauth_clients.id as id',
                       'oauth_clients.secret as secret',
                       'oauth_client_endpoints.redirect_uri as redirect_uri',
                       'oauth_clients.name as name')
                   ->join('oauth_client_endpoints', 'oauth_clients.id', '=', 'oauth_client_endpoints.client_id')
                   ->where('oauth_clients.id', $clientId)
                   ->where('oauth_client_endpoints.redirect_uri', $redirectUri);
        } elseif (! is_null($clientSecret) && is_null($redirectUri)) {
            $query = DB::table('oauth_clients')
                   ->select(
                       'oauth_clients.id as id',
                       'oauth_clients.secret as secret',
                       'oauth_clients.name as name')
                   ->where('oauth_clients.id', $clientId)
                   ->where('oauth_clients.secret', $clientSecret);
        } elseif (! is_null($clientSecret) && ! is_null($redirectUri)) {
            $query = DB::table('oauth_clients')
                   ->select(
                       'oauth_clients.id as id',
                       'oauth_clients.secret as secret',
                       'oauth_client_endpoints.redirect_uri as redirect_uri',
                       'oauth_clients.name as name')
                   ->join('oauth_client_endpoints', 'oauth_clients.id', '=', 'oauth_client_endpoints.client_id')
                   ->where('oauth_clients.id', $clientId)
                   ->where('oauth_clients.secret', $clientSecret)
                   ->where('oauth_client_endpoints.redirect_uri', $redirectUri);
        }

        if ($this->limitClientsToGrants === true and ! is_null($grantType)) {
            $query = $query->join('oauth_client_grants', 'oauth_clients.id', '=', 'oauth_client_grants.client_id')
                   ->join('oauth_grants', 'oauth_grants.id', '=', 'oauth_client_grants.grant_id')
                   ->where('oauth_grants.id', $grantType);
        }

        $result = $query->first();

        if (is_null($result)) {
            return null;
        }

        return (new Client($this->getServer()))
               ->setId($result->id)
               ->setSecret($result->secret)
               ->setName($result->name)
               ->setRedirectUri(isset($result->redirect_uri) ? $result->redirect_uri : null);
    }
}