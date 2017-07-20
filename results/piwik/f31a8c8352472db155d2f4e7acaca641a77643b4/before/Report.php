<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */
namespace Piwik\Plugin;

use Piwik\API\Proxy;
use Piwik\Cache\LanguageAwareStaticCache;
use Piwik\Menu\MenuReporting;
use Piwik\Metrics;
use Piwik\Piwik;
use Piwik\Plugin\Manager as PluginManager;
use Piwik\Plugins\CoreVisualizations\Visualizations\HtmlTable;
use Piwik\Translate;
use Piwik\WidgetsList;
use Piwik\ViewDataTable\Factory as ViewDataTableFactory;

/**
 * @api
 * @since 2.5.0
 */
class Report
{
    protected $module;
    protected $action;
    protected $name;
    protected $category;
    protected $widgetTitle;
    protected $widgetParams = array();
    protected $menuTitle;
    protected $processedMetrics = array();
    protected $hasGoalMetrics = false;
    protected $metrics = array();
    protected $constantRowsCount = null;
    protected $isSubtableReport = null;
    protected $parameters = null;

    /**
     * @var \Piwik\Plugin\VisitDimension|\Piwik\Plugin\ActionDimension
     */
    protected $dimension;
    protected $documentation;

    /**
     * @var null|Report
     */
    protected $actionToLoadSubTables;
    protected $order = 1;

    public function __construct()
    {
        $classname    = get_class($this);
        $parts        = explode('\\', $classname);
        $this->module = $parts[2];
        $this->action = lcfirst($parts[4]);
        $this->processedMetrics = Metrics::getDefaultProcessedMetrics();
        $this->metrics          = array_keys(Metrics::getDefaultMetrics());

        $this->init();
    }

    protected function init()
    {
    }

    public function isEnabled()
    {
        return true;
    }

    public function checkIsEnabled()
    {
        if (!$this->isEnabled()) {
            throw new \Exception(Piwik::translate('General_ExceptionReportNotEnabled'));
        }
    }

    public function getDefaultTypeViewDataTable()
    {
        return HtmlTable::ID;
    }

    public function configureView(ViewDataTable $view)
    {

    }

    /**
     * @return string
     * @throws \Exception
     */
    public function render()
    {
        $apiProxy = Proxy::getInstance();

        if (!$apiProxy->isExistingApiAction($this->module, $this->action)) {
            throw new \Exception("Invalid action name '$this->action' for '$this->module' plugin.");
        }

        $apiAction = $apiProxy->buildApiActionName($this->module, $this->action);

        $view      = ViewDataTableFactory::build(null, $apiAction, $this->module . '.' . $this->action);
        $rendered  = $view->render();

        return $rendered;
    }

    public function configureWidget(WidgetsList $widget)
    {
        if ($this->widgetTitle) {
            $params = array();
            if (!empty($this->widgetParams) && is_array($this->widgetParams)) {
                $params = $this->widgetParams;
            }
            $widget->add($this->category, $this->widgetTitle, $this->module, $this->action, $params);
        }
    }

    public function configureReportingMenu(MenuReporting $menu)
    {
        if ($this->menuTitle) {
            $action = 'menu' . ucfirst($this->action);
            $menu->add($this->category,
                       $this->menuTitle,
                       array('module' => $this->module, 'action' => $action),
                       $this->isEnabled(),
                       $this->order);
        }
    }

    public function getMetrics()
    {
        return $this->getMetricTranslations($this->metrics);
    }

    protected function getMetricsDocumentation()
    {
        $translations  = Metrics::getDefaultMetricsDocumentation();
        $documentation = array();

        foreach ($this->metrics as $metric) {
            if (!empty($translations[$metric])) {
                $documentation[$metric] = $translations[$metric];
            }
        }

        return $documentation;
    }

    public function hasGoalMetrics()
    {
        return $this->hasGoalMetrics;
    }

    public function configureReportMetadata(&$availableReports, $infos)
    {
        if (!$this->isEnabled()) {
            return;
        }

        $report = $this->buildReportMetadata();

        if (!empty($report)) {
            $availableReports[] = $report;
        }
    }

    protected function buildReportMetadata()
    {
        $report = array(
            'category' => $this->getCategory(),
            'name'     => $this->getName(),
            'module'   => $this->getModule(),
            'action'   => $this->getAction()
        );

        if (null !== $this->parameters) {
            $report['parameters'] = $this->parameters;
        }

        if (!empty($this->dimension)) {
            $report['dimension'] = $this->dimension->getName();
        }

        if (!empty($this->documentation)) {
            $report['documentation'] = $this->documentation;
        }

        if (null !== $this->isSubtableReport) {
            $report['isSubtableReport'] = $this->isSubtableReport;
        }

        $report['metrics']              = $this->getMetrics();
        $report['metricsDocumentation'] = $this->getMetricsDocumentation();
        $report['processedMetrics']     = $this->processedMetrics;

        if (!empty($this->actionToLoadSubTables)) {
            $report['actionToLoadSubTables'] = $this->actionToLoadSubTables;
        }

        if (null !== $this->constantRowsCount) {
            $report['constantRowsCount'] = $this->constantRowsCount;
        }

        $report['order'] = $this->order;

        return $report;
    }

    /**
     * @return Report[]
     */
    public function getRelatedReports()
    {
        return array();
    }

    public function getName()
    {
        return $this->name;
    }

    public function getAction()
    {
        return $this->action;
    }

    public function getCategory()
    {
        return Piwik::translate($this->category);
    }

    public function getDimension()
    {
        return $this->dimension;
    }

    public function getOrder()
    {
        return $this->order;
    }

    public function getModule()
    {
        return $this->module;
    }

    public function getMenuTitle()
    {
        return $this->menuTitle;
    }

    public function getActionToLoadSubTables()
    {
        return $this->actionToLoadSubTables;
    }

    public static function factory($module, $action)
    {
        if (empty($module) || empty($action)) {
            return;
        }

        foreach (self::getAllReports() as $report) {
            if ($report->module === $module && $report->action === $action && empty($report->parameters)) {
                return $report;
            }
        }
    }

    /** @return \Piwik\Plugin\Report[] */
    public static function getAllReports()
    {
        $reports = PluginManager::getInstance()->findMultipleComponents('Reports', '\\Piwik\\Plugin\\Report');
        $cache   = new LanguageAwareStaticCache('Reports' . implode('', $reports));

        if (!$cache->has()) {
            $instances = array();

            foreach ($reports as $report) {
                $instances[] = new $report();
            }

            usort($instances, array('self', 'sort'));

            $cache->set($instances);
        }

        return $cache->get();
    }

    /**
     * API metadata are sorted by category/name,
     * with a little tweak to replicate the standard Piwik category ordering
     *
     * @param array|Report $a
     * @param array|Report $b
     * @return int
     */
    public static function sort($a, $b)
    {
        static $order = null;
        if (is_null($order)) {
            $order = array(
                Piwik::translate('General_MultiSitesSummary'),
                Piwik::translate('VisitsSummary_VisitsSummary'),
                Piwik::translate('Goals_Ecommerce'),
                Piwik::translate('General_Actions'),
                Piwik::translate('Events_Events'),
                Piwik::translate('Actions_SubmenuSitesearch'),
                Piwik::translate('Referrers_Referrers'),
                Piwik::translate('Goals_Goals'),
                Piwik::translate('General_Visitors'),
                Piwik::translate('DevicesDetection_DevicesDetection'),
                Piwik::translate('UserSettings_VisitorSettings'),
            );
        }

        $catA = is_object($a) ? $a->category : $a['category'];
        $catB = is_object($b) ? $b->category : $b['category'];

        $orderA = is_object($a) ? $a->order : @$a['order'];
        $orderB = is_object($b) ? $b->order : @$b['order'];

        return ($category = strcmp(array_search($catA, $order), array_search($catB, $order))) == 0
            ? ($orderA < $orderB ? -1 : 1)
            : $category;
    }

    private function getMetricTranslations($metricsToTranslate)
    {
        $translations = Metrics::getDefaultMetricTranslations();
        $metrics = array();

        foreach ($metricsToTranslate as $metric) {
            if (!empty($translations[$metric])) {
                $metrics[$metric] = $translations[$metric];
            } else {
                $metrics[$metric] = $metric;
            }
        }

        return $metrics;
    }
}