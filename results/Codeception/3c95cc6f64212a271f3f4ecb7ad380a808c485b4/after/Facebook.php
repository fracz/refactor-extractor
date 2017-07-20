<?php
/**
 * @author tiger
 */

namespace Codeception\Util\Driver;

class Facebook extends \BaseFacebook
{
    protected $logCallback;

    public function __construct($config, $logCallback = null)
    {
        if (is_callable($logCallback)) {
            $this->logCallback = $logCallback;
        }

        parent::__construct($config); // TODO: Change the autogenerated stub
    }

    /**
     * @inheritdoc
     */
    protected function setPersistentData($key, $value)
    {
        // TODO: Implement setPersistentData() method.
    }

    /**
     * @inheritdoc
     */
    protected function getPersistentData($key, $default = false)
    {
        // TODO: Implement getPersistentData() method.
    }

    /**
     * @inheritdoc
     */
    protected function clearPersistentData($key)
    {
        // TODO: Implement clearPersistentData() method.
    }

    /**
     * @inheritdoc
     */
    protected function clearAllPersistentData()
    {
        // TODO: Implement clearAllPersistentData() method.
    }

    /**
     * @inheritdoc
     */
    public function api( /* polymorphic */)
    {
        if (is_callable($this->logCallback)) {
            call_user_func($this->logCallback, 'Facebook API request', func_get_args());
        }

        $response = call_user_func_array('parent::api', func_get_args());

        if (is_callable($this->logCallback)) {
            call_user_func($this->logCallback, 'Facebook API response', $response);
        }

        return $response;
    }

    /**
     * @param array $permissions
     *
     * @return array
     */
    public function createTestUser(array $permissions)
    {
        $response = $this->api(
                         $this->getAppId() . '/accounts/test-users',
                             'POST',
                             array(
                                 'installed'    => true,
                                 'permissions'  => implode(',', $permissions),
                                 'access_token' => $this->getApplicationAccessToken(),
                             )
        );

        // set user access token
        $this->setAccessToken($response['access_token']);

        return $response;
    }

    public function deleteTestUser($testUserID)
    {
        $this->api(
             $testUserID,
                 'DELETE',
                 array('access_token' => $this->getApplicationAccessToken())
        );
    }

    public function getLastPostsForTestUser()
    {
        return $this->api('me/posts', 'GET');
    }
}