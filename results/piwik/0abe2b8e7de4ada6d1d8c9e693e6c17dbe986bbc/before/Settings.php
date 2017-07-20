<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik
 * @package Piwik
 */
namespace Piwik\Plugin;

use Piwik\Common;
use Piwik\Option;
use Piwik\Piwik;

class Settings
{
    const TYPE_INT    = 'integer';
    const TYPE_FLOAT  = 'float';
    const TYPE_STRING = 'string';
    const TYPE_BOOL   = 'boolean';
    const TYPE_ARRAY  = 'array';

    const FIELD_TEXT     = 'text';
    const FIELD_TEXTAREA = 'textarea';
    const FIELD_CHECKBOX = 'checkbox';
    const FIELD_PASSWORD = 'password';
    const FIELD_MULTI_SELECT   = 'multiselect';
    const FIELD_SINGLE_SELECT  = 'select';

    // what about stuff like date etc?

    private $defaultTypes   = array();
    private $defaultFields  = array();
    private $defaultOptions = array();

    private $settings       = array();
    private $settingsValues = array();

    private $introduction;
    private $pluginName;

    public function __construct($pluginName)
    {
        $this->pluginName = $pluginName;

        $this->defaultTypes = array(
            static::FIELD_TEXT     => static::TYPE_STRING,
            static::FIELD_TEXTAREA => static::TYPE_STRING,
            static::FIELD_PASSWORD => static::TYPE_STRING,
            static::FIELD_CHECKBOX => static::TYPE_BOOL,
            static::FIELD_MULTI_SELECT  => static::TYPE_ARRAY,
            static::FIELD_SINGLE_SELECT => static::TYPE_STRING,
        );
        $this->defaultFields = array(
            static::TYPE_INT    => static::FIELD_TEXT,
            static::TYPE_FLOAT  => static::FIELD_TEXT,
            static::TYPE_STRING => static::FIELD_TEXT,
            static::TYPE_BOOL   => static::FIELD_CHECKBOX,
            static::TYPE_ARRAY  => static::FIELD_MULTI_SELECT,
        );
        $this->defaultOptions = array(
            'type'         => static::TYPE_STRING,
            'field'        => static::FIELD_TEXT,
            'displayedForCurrentUser' => false,
            'fieldAttributes' => array(),
            'fieldOptions'    => array(),
            'introduction'    => null,
            'description'     => null,
            'inlineHelp'      => null,
            'filter'          => null,
            'validate'        => null,
            'isUserSetting'   => false,
            'isSystemSetting' => false
        );

        $this->init();
        $this->loadSettings();
    }

    protected function init()
    {
    }

    protected function addIntroduction($introduction)
    {
        $this->introduction = $introduction;
    }

    protected function addPerUserSetting($name, $title, array $options = array())
    {
        $options['displayedForCurrentUser'] = !Piwik::isUserIsAnonymous();
        $options['isUserSetting']  = true;

        $userSettingName = $this->buildUserSettingName($name);

        $this->addSetting($name, $userSettingName, $title, $options);
    }

    protected function addSystemSetting($name, $title, array $options = array())
    {
        $options['displayedForCurrentUser'] = Piwik::isUserHasSomeAdminAccess();
        $options['isSystemSetting'] = true;

        $this->addSetting($name, $name, $title, $options);
    }

    public function getIntroduction()
    {
        return $this->introduction;
    }

    public function getSettingsForCurrentUser()
    {
        return array_values(array_filter($this->getSettings(), function ($setting) {
            return $setting['displayedForCurrentUser'];
        }));
    }

    public function getPerUserSettingValue($name, $userLogin = null)
    {
        $name = $this->buildUserSettingName($name, $userLogin);
        $this->checkIsValidUserSetting($name);

        return $this->getSettingValue($name);
    }

    public function getSystemSettingValue($name)
    {
        $this->checkIsValidSystemSetting($name);

        return $this->getSettingValue($name);
    }

    public function setPerUserSettingValue($name, $value, $userLogin = null)
    {
        $name = $this->buildUserSettingName($name, $userLogin);
        $this->checkIsValidUserSetting($name);

        $this->setSettingValue($name, $value);
    }

    public function setSystemSettingValue($name, $value)
    {
        $this->checkIsValidSystemSetting($name);

        $this->setSettingValue($name, $value);
    }

    public function save()
    {
        Option::set($this->getOptionKey(), serialize($this->settingsValues));
    }

    public function removeAllPluginSettings()
    {
        Option::delete($this->getOptionKey());
    }

    public function removeAllSettingsForUser($userLogin)
    {
        foreach ($this->settingsValues as $name => $value) {
            $setting = $this->getSetting($name);

            if (!empty($setting) && $setting['isSystemSetting']) {
                continue;
            }

            $findName = $this->buildUserSettingName($name, $userLogin);
            if ($name == $findName) {
                unset($this->settingsValues[$name]);
            }
        }

        $this->save();
    }

    private function getSettingValue($name)
    {
        if (!array_key_exists($name, $this->settingsValues)) {
            $setting = $this->getSetting($name);

            return $setting['defaultValue'];
        }

        return $this->settingsValues[$name];
    }

    private function setSettingValue($name, $value)
    {
        $this->checkIsValidSetting($name);
        $setting = $this->getSetting($name);

        if ($setting['validate'] && $setting['validate'] instanceof \Closure) {
            call_user_func($setting['validate'], $value, $setting);
        }

        if ($setting['filter'] && $setting['filter'] instanceof \Closure) {
            $value = call_user_func($setting['filter'], $value, $setting);
        } else {
            settype($value, $setting['type']);
        }

        $this->settingsValues[$name] = $value;
    }

    private function addSetting($unmodifiedName, $name, $title, array $options)
    {
        if (!ctype_alnum($unmodifiedName)) {
            // TODO escape name?
            $msg = sprintf('The setting name %s is not valid. Only alpha and numerical characters are allowed', $unmodifiedName);
            throw new \Exception($msg);
        }

        if (array_key_exists('field', $options) && !array_key_exists('type', $options)) {
            $options['type']  = $this->defaultTypes[$options['field']];
        } elseif (array_key_exists('type', $options) && !array_key_exists('field', $options)) {
            $options['field'] = $this->defaultFields[$options['type']];
        }

        if (!array_key_exists('validate', $options) && array_key_exists('fieldOptions', $options)) {
            $options['validate'] = function ($value) use ($options, $title) {
                if (!array_key_exists($value, $options['fieldOptions'])) {
                    throw new \Exception(sprintf('The selected value for field "%s" is not allowed.', $title));
                }
            };
        }

        $setting          = array_merge($this->defaultOptions, $options);
        $setting['name']  = $name;
        $setting['title'] = $title;
        $setting['unmodifiedName'] = $unmodifiedName;

        $this->settings[] = $setting;
    }

    private function getOptionKey()
    {
        return 'Plugin_' . $this->pluginName . '_Settings';
    }

    private function loadSettings()
    {
        $values = Option::get($this->getOptionKey());

        if (!empty($values)) {
            $this->settingsValues = unserialize($values);
        }
    }

    private function checkIsValidUserSetting($name)
    {
        $this->checkIsValidSetting($name);

        $setting = $this->getSetting($name);

        if (!$setting['isUserSetting']) {
            throw new \Exception(sprintf('The setting %s is not a user setting', $name));
        }
    }

    private function checkIsValidSystemSetting($name)
    {
        $this->checkIsValidSetting($name);

        $setting = $this->getSetting($name);

        if (!$setting['isSystemSetting']) {
            throw new \Exception(sprintf('The setting %s is not a system setting', $name));
        }
    }

    private function checkIsValidSetting($name)
    {
        $setting = $this->getSetting($name);

        if (empty($setting)) {
            // TODO escape $name? or is it automatically escaped?
            throw new \Exception(sprintf('The setting %s does not exist', $name));
        }

        if (!$setting['displayedForCurrentUser']) {
            throw new \Exception(sprintf('You are not allowed to change the value of the setting %s', $name));
        }
    }

    private function getSettings()
    {
        return $this->settings;
    }

    private function getSetting($name)
    {
        foreach ($this->settings as $setting) {
            if ($name == $setting['name']) {
                return $setting;
            }
        }
    }

    /**
     * @param $name
     * @param null $userLogin  if null, the current user login will be used.
     * @return string
     */
    private function buildUserSettingName($name, $userLogin = null)
    {
        if (is_null($userLogin)) {
            $userLogin = Piwik::getCurrentUserLogin();
        }

        // the asterisk tag is indeed important here and better than an underscore. Imagine a plugin has the settings
        // "api_password" and "api". A user having the login "_password" could otherwise under circumstances change the
        // setting for "api" although he is not allowed to. It is not so important at the moment because only alNum is
        // currently allowed as a name this might change in the future.
        $appendix = '#' . $userLogin . '#';

        if (Common::stringEndsWith($name, $appendix)) {
            return $name;
        }

        return $name . $appendix;
    }

}