<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */
namespace Piwik\Plugin;

use Piwik\Common;
use Piwik\Db;
use Piwik\Plugin\Manager as PluginManager;
use Piwik\Tracker\Request;
use Piwik\Tracker\Visitor;
use Piwik\Tracker\Action;
use Piwik\Translate;

/**
 * @api
 * @since 2.5.0
 */
abstract class VisitDimension
{
    protected $name;

    protected $fieldName = '';
    protected $fieldType = '';

    protected $segments = array();

    /**
     * Cached instances of sorted visit dimension entries
     * @var array
     */
    private static $cachedInstances = array();

    protected function configureSegments()
    {

    }

    public function hasImplementedEvent($method)
    {
        $reflectionObject = new \ReflectionObject($this);
        $declaringClass   = $reflectionObject->getMethod($method)->getDeclaringClass();

        return get_class() !== $declaringClass->name;
    }

    public function install()
    {
        try {
            if ($this->isHandlingLogVisit()) {
                $sql = "ALTER TABLE `" . Common::prefixTable("log_visit") . "` ADD `$this->fieldName` $this->fieldType";
                Db::exec($sql);
            }
        } catch (\Exception $e) {
            if (!Db::get()->isErrNo($e, '1060')) {
                throw $e;
            }
        }

        try {
            if ($this->isHandlingLogConversion()) {
                $sql = "ALTER TABLE `" . Common::prefixTable("log_conversion") . "` ADD `$this->fieldName` $this->fieldType";
                Db::exec($sql);
            }
        } catch (\Exception $e) {
            if (!Db::get()->isErrNo($e, '1060')) {
                throw $e;
            }
        }
    }

    private function isHandlingLogVisit()
    {
        if (empty($this->fieldName) || empty($this->fieldType)) {
            return false;
        }

        return $this->hasImplementedEvent('onNewVisit')
            || $this->hasImplementedEvent('onExistingVisit')
            || $this->hasImplementedEvent('onConvertedVisit');
    }

    private function isHandlingLogConversion()
    {
        if (empty($this->fieldName) || empty($this->fieldType)) {
            return false;
        }

        return $this->hasImplementedEvent('onRecordGoal');
    }

    public function uninstall()
    {
        if (empty($this->fieldName) || empty($this->fieldType)) {
            return;
        }

        try {
            if ($this->isHandlingLogVisit()) {
                $sql = "ALTER TABLE `" . Common::prefixTable("log_visit") . "` DROP COLUMN `$this->fieldName`";
                Db::exec($sql);
            }
        } catch (\Exception $e) {
            if (!Db::get()->isErrNo($e, '1091')) {
                throw $e;
            }
        }

        try {
            if ($this->isHandlingLogConversion()) {
                $sql = "ALTER TABLE `" . Common::prefixTable("log_conversion") . "` DROP COLUMN `$this->fieldName`";
                Db::exec($sql);
            }
        } catch (\Exception $e) {
            if (!Db::get()->isErrNo($e, '1091')) {
                throw $e;
            }
        }
    }

    protected function addSegment(Segment $segment)
    {
        $sqlSegment = $segment->getSqlSegment();
        if (!empty($this->fieldName) && empty($sqlSegment)) {
            $segment->setSqlSegment('log_visit.' . $this->fieldName);
        }

        $type = $segment->getType();

        if (empty($type)) {
            $segment->setType(Segment::TYPE_DIMENSION);
        }

        $this->segments[] = $segment;
    }

    /**
     * @return Segment[]
     */
    public function getSegments()
    {
        if (empty($this->segments)) {
            $this->configureSegments();
        }

        return $this->segments;
    }

    public function getFieldName()
    {
        return $this->fieldName;
    }

    public function getRequiredVisitFields()
    {
        return array();
    }

    /**
     * @param Request $request
     * @param Visitor $visitor
     * @param Action|null $action
     * @return mixed
     */
    public function onNewVisit(Request $request, Visitor $visitor, $action)
    {
        return false;
    }

    public function onExistingVisit(Request $request, Visitor $visitor, $action)
    {
        return false;
    }

    public function onConvertedVisit(Request $request, Visitor $visitor, $action)
    {
        return false;
    }

    public function onRecordGoal(Request $request, Visitor $visitor, $action)
    {
        return false;
    }

    abstract public function getName();

    /** @return \Piwik\Plugin\VisitDimension[] */
    public static function getAllDimensions()
    {
        $pluginManager = PluginManager::getInstance();
        $cacheKey      = md5(implode('', $pluginManager->getLoadedPluginsName()) . Translate::getLanguageLoaded());

        if (!array_key_exists($cacheKey, self::$cachedInstances)) {

            $plugins   = $pluginManager->getLoadedPlugins();
            $instances = array();

            foreach ($plugins as $plugin) {
                foreach (self::getDimensions($plugin) as $instance) {
                    $instances[] = $instance;
                }
            }

            usort($instances, array('self', 'sortByRequiredFields'));

            self::$cachedInstances[$cacheKey] = $instances;
        }

        return self::$cachedInstances[$cacheKey];
    }

    public static function sortByRequiredFields($a, $b)
    {
        $fields = $a->getRequiredVisitFields();

        if (empty($fields)) {
            return -1;
        }

        if (!empty($fields) && in_array($b->getFieldName(), $fields)) {
            return 1;
        }

        return 0;
    }

    /** @return \Piwik\Plugin\VisitDimension[] */
    public static function getDimensions(\Piwik\Plugin $plugin)
    {
        $dimensions = $plugin->findMultipleComponents('Columns', '\\Piwik\\Plugin\\VisitDimension');
        $instances  = array();

        foreach ($dimensions as $dimension) {
            $instances[] = new $dimension();
        }

        return $instances;
    }

}