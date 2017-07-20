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
namespace Piwik;

use Piwik\API\Proxy;
use Piwik\API\Request;
use Piwik\Plugins\API\API;
use Piwik\Plugin\Visualization;

/**
 * This class is used to load (from the API) and customize the output of a given DataTable.
 * The main() method will create an object implementing ViewInterface
 * You can customize the dataTable using the disable* methods.
 *
 * You can also customize the dataTable rendering using row metadata:
 * - 'html_label_prefix': If this metadata is present on a row, it's contents will be prepended
 *                        the label in the HTML output.
 * - 'html_label_suffix': If this metadata is present on a row, it's contents will be appended
 *                        after the label in the HTML output.
 *
 * Example:
 * In the Controller of the plugin VisitorInterest
 * <pre>
 *    function getNumberOfVisitsPerVisitDuration( $fetch = false)
 *  {
 *        $view = ViewDataTable::factory( 'cloud' );
 *        $view->init( $this->pluginName,  __FUNCTION__, 'VisitorInterest.getNumberOfVisitsPerVisitDuration' );
 *        $view->setColumnsToDisplay( array('label','nb_visits') );
 *        $view->disableSort();
 *        $view->disableExcludeLowPopulation();
 *        $view->disableOffsetInformation();
 *
 *        return $this->renderView($view, $fetch);
 *    }
 * </pre>
 *
 * @see factory() for all the available output (cloud tags, html table, pie chart, vertical bar chart)
 * @package Piwik
 * @subpackage ViewDataTable
 *
 * @api
 */
class ViewDataTable
{

    /**
     * Cache for getAllReportDisplayProperties result.
     *
     * @var array
     */
    private static $reportPropertiesCache = null;

    /**
     * Cache for getDefaultViewTypeForReports result.
     *
     * @var array
     */
    private static $defaultViewType = null;

    /**
     * Returns a Piwik_ViewDataTable_* object.
     * By default it will return a ViewDataTable_Html
     * If there is a viewDataTable parameter in the URL, a ViewDataTable of this 'viewDataTable' type will be returned.
     * If defaultType is specified and if there is no 'viewDataTable' in the URL, a ViewDataTable of this $defaultType will be returned.
     * If force is set to true, a ViewDataTable of the $defaultType will be returned in all cases.
     *
     * @param string $defaultType Any of these: table, cloud, graphPie, graphVerticalBar, graphEvolution, sparkline, generateDataChart*
     * @param string|bool $apiAction
     * @param string|bool $controllerAction
     * @param bool $forceDefault
     *
     * @throws \Exception
     * @return \Piwik\Plugin\ViewDataTable|\Piwik\Plugin\Visualization|\Piwik\Plugins\CoreVisualizations\Visualizations\Sparkline;
     */
    public static function factory($defaultType = null, $apiAction = false, $controllerAction = false, $forceDefault = false)
    {
        if (false === $controllerAction) {
            $controllerAction = $apiAction;
        }

        $defaultViewType         = self::getDefaultViewTypeForReport($apiAction);
        $defaultReportProperties = static::getDefaultPropertiesForReport($apiAction);

        if (!$forceDefault && !empty($defaultViewType)) {
            $defaultType = $defaultViewType;
        }

        $type = Common::getRequestVar('viewDataTable', $defaultType ? : 'table', 'string');

        $visualizations = static::getAvailableVisualizations();

        if (array_key_exists($type, $visualizations)) {
            return new $visualizations[$type]($controllerAction, $apiAction, $defaultReportProperties);
        }

        if (class_exists($type)) {
            return new $type($controllerAction, $apiAction, $defaultReportProperties);
        }

        throw new \Exception(sprintf('Visuzalization type %s not found', $type));
    }

    /**
     * Returns all registered visualization classes. Uses the 'Visualization.getAvailable'
     * event to retrieve visualizations.
     *
     * @return array Array mapping visualization IDs with their associated visualization classes.
     * @throws \Exception If a visualization class does not exist or if a duplicate visualization ID
     *                   is found.
     */
    public static function getAvailableVisualizations()
    {
        /** @var string[] $visualizations */
        $visualizations = array();

        /**
         * This event is used to gather all available DataTable visualizations. Callbacks should add visualization
         * class names to the incoming array.
         */
        Piwik::postEvent('Visualization.addVisualizations', array(&$visualizations));

        $result = array();

        foreach ($visualizations as $viz) {
            if (!class_exists($viz)) {
                throw new \Exception("Invalid visualization class '$viz' found in Visualization.getAvailableVisualizations.");
            }

            if (!is_subclass_of($viz, '\\Piwik\\Plugin\\ViewDataTable')) {
                throw new \Exception("Visualization class '$viz' does not extend Plugin/ViewDataTable");
            }

            $vizId = $viz::getViewDataTableId();

            if (isset($result[$vizId])) {
                throw new \Exception("Visualization ID '$vizId' is already in use!");
            }

            $result[$vizId] = $viz;
        }

        return $result;
    }

    /**
     * Returns all available visualizations that are not part of the CoreVisualizations plugin.
     *
     * @return array Array mapping visualization IDs with their associated visualization classes.
     */
    public static function getNonCoreVisualizations()
    {
        $result = array();

        foreach (static::getAvailableVisualizations() as $vizId => $vizClass) {
            if (false === strpos($vizClass, 'Piwik\\Plugins\\CoreVisualizations')
                && false === strpos($vizClass, 'Piwik\\Plugins\\Goals\\Visualizations\\Goals')) {
                $result[$vizId] = $vizClass;
            }
        }

        return $result;
    }

    /**
     * Convenience method that creates and renders a ViewDataTable for a API method.
     *
     * @param string $pluginName The name of the plugin (eg, UserSettings).
     * @param string $apiAction The name of the API action (eg, getResolution).
     * @param bool $fetch If true, the result is returned, if false it is echo'd.
     * @throws \Exception
     * @return string|null See $fetch.
     */
    public static function renderReport($pluginName, $apiAction, $fetch = true)
    {
        /** @var Proxy $apiProxy */
        $apiProxy = Proxy::getInstance();

        if (!$apiProxy->isExistingApiAction($pluginName, $apiAction)) {
            throw new \Exception("Invalid action name '$apiAction' for '$pluginName' plugin.");
        }

        $apiAction = $apiProxy->buildApiActionName($pluginName, $apiAction);

        $view      = static::factory(null, $apiAction);
        $rendered  = $view->render();

        if ($fetch) {
            return $rendered;
        } else {
            echo $rendered;
        }
    }

    /**
     * Returns the defaut view properties for a report, if any.
     *
     * Plugins can associate callbacks with the Visualization.getReportDisplayProperties
     * event to set the default properties of reports.
     *
     * @param string $apiAction
     * @return array
     */
    private static function getDefaultPropertiesForReport($apiAction)
    {
        $reportDisplayProperties = self::getAllReportDisplayProperties();
        return isset($reportDisplayProperties[$apiAction]) ? $reportDisplayProperties[$apiAction] : array();
    }

    /**
     * Returns the default viewDataTable ID to use when determining which visualization to use.
     *
     * @param string $apiAction
     */
    private static function getDefaultViewTypeForReport($apiAction)
    {
        $defaultViewTypes = self::getDefaultViewTypeForReports();
        return isset($defaultViewTypes[$apiAction]) ? $defaultViewTypes[$apiAction] : array();
    }

    /**
     * Returns a list of default viewDataTables ID to use when determining which visualization to use for multiple
     * reports.
     */
    private static function getDefaultViewTypeForReports()
    {
        if (null === self::$defaultViewType) {
            self::$defaultViewType = array();
            /**
             * This event is triggered to gather the default view types for each available report. By default a table
             * is used. If you define your own report, you may want to subscribe to this event to define another
             * Visualization that should be used by default to display your report. For instance a Pie, a Bar or a
             * Cloud.
             *
             * Example:
             * ```
             * public function getDefaultViewTypeForReports(&$defaultViewTypes)
             * {
             *     $defaultViewTypes['Referrers.getSocials']       = HtmlTable::ID;
             *     $defaultViewTypes['Referrers.getUrlsForSocial'] = Pie::ID;
             *     )
             * }
             * ```
             */
            Piwik::postEvent('Visualization.getDefaultViewTypeForReports', array(&self::$defaultViewType));
        }

        return self::$defaultViewType;
    }

    /**
     * Returns the list of display properties for all available reports.
     *
     * @return array
     */
    private static function getAllReportDisplayProperties()
    {
        if (null === self::$reportPropertiesCache) {
            self::$reportPropertiesCache = array();
            /**
             * This event is triggered to gather the report display properties for each available report. If you define
             * your own report, you want to subscribe to this event to define how your report shall be displayed in the
             * Piwik UI.
             *
             * Example:
             * ```
             * public function getReportDisplayProperties(&$properties)
             * {
             *     $properties['Provider.getProvider'] = array(
             *         'translations' => array('label' => Piwik::translate('Provider_ColumnProvider')),
             *         'filter_limit' => 5
             *     )
             * }
             * ```
             */
            Piwik::postEvent('Visualization.getReportDisplayProperties', array(&self::$reportPropertiesCache));
        }

        return self::$reportPropertiesCache;
    }
}