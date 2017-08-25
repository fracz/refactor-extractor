<?php
namespace Robo\Common;

use Robo\Config;

trait Configuration
{
    public static function configure($key, $value)
    {
        Config::set(__CLASS__.".$key", $value);
    }

    protected function getConfigValue($key)
    {
        return Config::get(__CLASS__.".$key");
    }
}