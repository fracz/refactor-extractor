<?php

/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik_Plugins
 * @package Piwik_DevicesDetection
 */
use Piwik\Core\Config;
use Piwik\Core\Common;

require_once PIWIK_INCLUDE_PATH . "/plugins/DevicesDetection/UserAgentParserEnhanced/UserAgentParserEnhanced.php";
require_once PIWIK_INCLUDE_PATH . '/plugins/DevicesDetection/functions.php';

class Piwik_DevicesDetection extends Piwik_Plugin
{
    /**
     * @see Piwik_Plugin::getInformation
     */
    public function getInformation()
    {
        return array(
            'description' => "[Beta Plugin] " . Piwik_Translate("DevicesDetection_description"),
            'author' => 'Piwik and Clearcode.cc',
            'author_homepage' => 'http://clearcode.cc',
            'version' => '1.12-b6',
        );
    }


    /**
     * @see Piwik_Plugin::getListHooksRegistered
     */
    public function getListHooksRegistered()
    {
        return array(
            'ArchiveProcessing_Day.compute' => "archiveDay",
            'ArchiveProcessing_Period.compute' => 'archivePeriod',
            'Menu.add' => 'addMenu',
            'Tracker.newVisitorInformation' => 'parseMobileVisitData',
            'WidgetsList.add' => 'addWidgets',
            'API.getReportMetadata' => 'getReportMetadata',
            'API.getSegmentsMetadata' => 'getSegmentsMetadata',
        );
    }

    /**
     * Defines API reports.
     * Also used to define Widgets, and Segment(s)
     *
     * @return array Category, Report Name, API Module, API action, Translated column name, & optional segment info
     *
     */
    protected function getRawMetadataReports()
    {
        $report = array(
            array(
                'DevicesDetection_DevicesDetection',
                'DevicesDetection_DeviceType',
                'DevicesDetection',
                'getType',
                'DevicesDetection_DeviceType',

                // Segment
                'deviceType',
                'log_visit.config_device_type',
                implode(", ", UserAgentParserEnhanced::$deviceTypes), // comma separated examples
                create_function('$type', 'return array_search( strtolower(trim(urldecode($type))), UserAgentParserEnhanced::$deviceTypes);')
            ),
            // device brands report
            array(
                'DevicesDetection_DevicesDetection',
                'DevicesDetection_DeviceBrand',
                'DevicesDetection',
                'getBrand',
                'DevicesDetection_DeviceBrand',
            ),
            // device model report
            array(
                'DevicesDetection_DevicesDetection',
                'DevicesDetection_DeviceModel',
                'DevicesDetection',
                'getModel',
                'DevicesDetection_DeviceModel',
            ),
            // device OS family report
            array(
                'DevicesDetection_DevicesDetection',
                'DeviceDetection_OperatingSystemFamilies',
                'DevicesDetection',
                'getOsFamilies',
                'DeviceDetection_OperatingSystemFamilies',
            ),
            // device OS version report
            array(
                'DevicesDetection_DevicesDetection',
                'DeviceDetection_OperatingSystemVersions',
                'DevicesDetection',
                'getOsVersions',
                'DeviceDetection_OperatingSystemVersions',
            ),
            // Browser family report
            array(
                'DevicesDetection_DevicesDetection',
                'DevicesDetection_BrowsersFamily',
                'DevicesDetection',
                'getBrowserFamilies',
                'DevicesDetection_BrowsersFamily',
            ),
            // Browser versions report
            array(
                'DevicesDetection_DevicesDetection',
                'DevicesDetection_BrowserVersions',
                'DevicesDetection',
                'getBrowserVersions',
                'DevicesDetection_BrowserVersions',
            ),
        );
        return $report;
    }

    public function addWidgets()
    {
        foreach ($this->getRawMetadataReports() as $report) {
            list($category, $name, $controllerName, $controllerAction) = $report;
            if ($category == false)
                continue;
            Piwik_AddWidget($category, $name, $controllerName, $controllerAction);
        }
    }


    /**
     * Get segments meta data
     */
    public function getSegmentsMetadata(&$segments)
    {
        // Note: only one field segmented so far: deviceType
        foreach ($this->getRawMetadataReports() as $report) {
            @list($category, $name, $apiModule, $apiAction, $columnName, $segment, $sqlSegment, $acceptedValues) = $report;

            if (empty($segment)) continue;
            $segments[] = array(
                'type'           => 'dimension',
                'category'       => Piwik_Translate('General_Visit'),
                'name'           => $columnName,
                'segment'        => $segment,
                'acceptedValues' => $acceptedValues,
                'sqlSegment'     => $sqlSegment,
                'sqlFilter'      => isset($sqlFilter) ? $sqlFilter : false,
            );
        }
    }

    public function getReportMetadata(&$reports)
    {
        $i = 0;
        foreach ($this->getRawMetadataReports() as $report) {
            list($category, $name, $apiModule, $apiAction, $columnName) = $report;
            if ($category == false)
                continue;

            $report = array(
                'category' => Piwik_Translate($category),
                'name' => Piwik_Translate($name),
                'module' => $apiModule,
                'action' => $apiAction,
                'dimension' => Piwik_Translate($columnName),
                'order' => $i++
            );

            $translation = $name . 'Documentation';
            $translated = Piwik_Translate($translation, '<br />');
            if ($translated != $translation) {
                $report['documentation'] = $translated;
            }


            $reports[] = $report;
        }
    }

    public function install()
    {
// we catch the exception
        try {
            $q1 = "ALTER TABLE `" . Common::prefixTable("log_visit") . "`
                ADD `config_os_version` VARCHAR( 10 ) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `config_os` ,
                ADD `config_device_type` TINYINT( 10 ) NULL DEFAULT NULL AFTER `config_browser_version` ,
                ADD `config_device_brand` VARCHAR( 100 ) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `config_device_type` ,
                ADD `config_device_model` VARCHAR( 100 ) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `config_device_brand`";
            Piwik_Exec($q1);
            // conditionaly add this column
            if (@Config::getInstance()->Debug['store_user_agent_in_visit']) {
                $q2 = "ALTER TABLE `" . Common::prefixTable("log_visit") . "`
                ADD `config_debug_ua` VARCHAR( 512 ) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL AFTER `config_device_model`";
                Piwik_Exec($q2);
            }
        } catch (Exception $e) {
           if (!Zend_Registry::get('db')->isErrNo($e, '1060')) {
                throw $e;
            }
        }
    }

    public function parseMobileVisitData(&$visitorInfo, $extraInfo)
    {
        $userAgent = $extraInfo['UserAgent'];

        $UAParser = new UserAgentParserEnhanced($userAgent);
        $UAParser->parse();
        $deviceInfo['config_browser_name'] = $UAParser->getBrowser("short_name");
        $deviceInfo['config_browser_version'] = $UAParser->getBrowser("version");
        $deviceInfo['config_os'] = $UAParser->getOs("short_name");
        $deviceInfo['config_os_version'] = $UAParser->getOs("version");
        $deviceInfo['config_device_type'] = $UAParser->getDevice();
        $deviceInfo['config_device_model'] = $UAParser->getModel();
        $deviceInfo['config_device_brand'] = $UAParser->getBrand();

        if (@Config::getInstance()->Debug['store_user_agent_in_visit']) {
            $deviceInfo['config_debug_ua'] = $userAgent;
        }

        $visitorInfo = array_merge($visitorInfo, $deviceInfo);
        printDebug("Device Detection:");
        printDebug($deviceInfo);
    }

    public function archiveDay(Piwik_ArchiveProcessor_Day $archiveProcessor)
    {
        $archiving = new Piwik_DevicesDetection_Archiver($archiveProcessor);
        if($archiving->shouldArchive()) {
            $archiving->archiveDay();
        }
    }

    public function archivePeriod(Piwik_ArchiveProcessor_Period $archiveProcessor)
    {
        $archiving = new Piwik_DevicesDetection_Archiver($archiveProcessor);
        if($archiving->shouldArchive()) {
            $archiving->archivePeriod();
        }
    }

    public function addMenu()
    {
        Piwik_AddMenu('General_Visitors', 'DevicesDetection_submenu', array('module' => 'DevicesDetection', 'action' => 'index'));
    }
}