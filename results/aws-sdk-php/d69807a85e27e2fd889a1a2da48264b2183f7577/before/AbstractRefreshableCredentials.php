<?php
namespace Aws\Common\Credentials;

/**
 * Abstract decorator to provide a foundation for refreshable credentials.
 */
abstract class AbstractRefreshableCredentials implements
    RefreshableCredentialsInterface
{
    /** @var CredentialsInterface Wrapped credentials object */
    protected $credentials;

    /**
     * @param CredentialsInterface $credentials
     */
    public function __construct(CredentialsInterface $credentials)
    {
        $this->credentials = $credentials;
    }

    public function getAccessKeyId()
    {
        if ($this->credentials->isExpired()) {
            $this->refresh();
        }

        return $this->credentials->getAccessKeyId();
    }

    public function getSecretKey()
    {
        if ($this->credentials->isExpired()) {
            $this->refresh();
        }

        return $this->credentials->getSecretKey();
    }

    public function getSecurityToken()
    {
        if ($this->credentials->isExpired()) {
            $this->refresh();
        }

        return $this->credentials->getSecurityToken();
    }

    public function toArray()
    {
        if ($this->credentials->isExpired()) {
            $this->refresh();
        }

        return $this->credentials->toArray();
    }

    public function getExpiration()
    {
        return $this->credentials->getExpiration();
    }

    public function isExpired()
    {
        return $this->credentials->isExpired();
    }
}