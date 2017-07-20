<?php
namespace Grav\Common;

use Grav\Common\Config\Config;
use RocketTheme\Toolbox\File\YamlFile;

/**
 * Class Theme
 * @package Grav\Common
 */
class Theme extends Plugin
{
    /**
     * Constructor.
     *
     * @param Grav   $grav
     * @param Config $config
     * @param string $name
     */
    public function __construct(Grav $grav, Config $config, $name)
    {
        parent::__construct($name, $grav, $config);
    }

    /**
     * Persists to disk the theme parameters currently stored in the Grav Config object
     *
     * @param string $theme_name The name of the theme whose config it should store.
     *
     * @return true
     */
    public static function saveConfig($theme_name)
    {
        if (!$theme_name) {
            return false;
        }

        $grav = Grav::instance();
        $locator = $grav['locator'];
        $filename = 'config://themes/' . $theme_name . '.yaml';
        $file = YamlFile::instance($locator->findResource($filename, true, true));
        $content = $grav['config']->get('themes.' . $theme_name);
        $file->save($content);
        $file->free();

        return true;
    }
}