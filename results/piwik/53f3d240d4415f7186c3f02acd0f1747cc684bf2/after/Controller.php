<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik_Plugins
 * @package Referers
 */
namespace Piwik\Plugins\Referers;

use Piwik\API\Request;
use Piwik\Metrics;
use Piwik\Period\Range;
use Piwik\Piwik;
use Piwik\Common;
use Piwik\ViewDataTable;
use Piwik\View;
use Piwik\Url;

/**
 *
 * @package Referers
 */
class Controller extends \Piwik\Controller
{
    public function index()
    {
        $view = new View('@Referers/index');

        $view->graphEvolutionReferers = $this->getEvolutionGraph(true, Common::REFERER_TYPE_DIRECT_ENTRY, array('nb_visits'));
        $view->nameGraphEvolutionReferers = 'Referers.getEvolutionGraph';

        // building the referers summary report
        $view->dataTableRefererType = $this->getRefererType(true);

        $nameValues = $this->getReferersVisitorsByType();

        $totalVisits = array_sum($nameValues);
        foreach ($nameValues as $name => $value) {
            $view->$name = $value;

            // calculate percent of total, if there were any visits
            if ($value != 0
                && $totalVisits != 0
            ) {
                $percentName = $name . 'Percent';
                $view->$percentName = round(($value / $totalVisits) * 100, 0);
            }
        }

        // set distinct metrics
        $distinctMetrics = $this->getDistinctReferrersMetrics();
        foreach ($distinctMetrics as $name => $value) {
            $view->$name = $value;
        }

        // calculate evolution for visit metrics & distinct metrics
        list($lastPeriodDate, $ignore) = Range::getLastDate();
        if ($lastPeriodDate !== false) {
            $date = Common::getRequestVar('date');
            $period = Common::getRequestVar('period');

            $prettyDate = self::getPrettyDate($date, $period);
            $prettyLastPeriodDate = self::getPrettyDate($lastPeriodDate, $period);

            // visit metrics
            $previousValues = $this->getReferersVisitorsByType($lastPeriodDate);
            $this->addEvolutionPropertiesToView($view, $prettyDate, $nameValues, $prettyLastPeriodDate, $previousValues);

            // distinct metrics
            $previousValues = $this->getDistinctReferrersMetrics($lastPeriodDate);
            $this->addEvolutionPropertiesToView($view, $prettyDate, $distinctMetrics, $prettyLastPeriodDate, $previousValues);
        }

        // sparkline for the historical data of the above values
        $view->urlSparklineSearchEngines = $this->getReferrerUrlSparkline(Common::REFERER_TYPE_SEARCH_ENGINE);
        $view->urlSparklineDirectEntry = $this->getReferrerUrlSparkline(Common::REFERER_TYPE_DIRECT_ENTRY);
        $view->urlSparklineWebsites = $this->getReferrerUrlSparkline(Common::REFERER_TYPE_WEBSITE);
        $view->urlSparklineCampaigns = $this->getReferrerUrlSparkline(Common::REFERER_TYPE_CAMPAIGN);

        // sparklines for the evolution of the distinct keywords count/websites count/ etc
        $view->urlSparklineDistinctSearchEngines = $this->getUrlSparkline('getLastDistinctSearchEnginesGraph');
        $view->urlSparklineDistinctKeywords = $this->getUrlSparkline('getLastDistinctKeywordsGraph');
        $view->urlSparklineDistinctWebsites = $this->getUrlSparkline('getLastDistinctWebsitesGraph');
        $view->urlSparklineDistinctCampaigns = $this->getUrlSparkline('getLastDistinctCampaignsGraph');

        $view->totalVisits = $totalVisits;
        $view->referrersReportsByDimension = $this->getReferrersReportsByDimensionView($totalVisits);

        echo $view->render();
    }

    /**
     * Returns HTML for the Referrers Overview page that categorizes Referrer reports
     * & allows the user to switch between them.
     *
     * @param int $visits The number of visits for this period & site. If <= 0, the
     *                    reports are not shown, since they will have no data.
     * @return string The report viewer HTML.
     */
    private function getReferrersReportsByDimensionView($visits)
    {
        $result = '';

        // only display the reports by dimension view if there are visits
        if ($visits > 0) {
            $referrersReportsByDimension = new View\ReportsByDimension();

            $referrersReportsByDimension->addReport(
                'Referers_ViewAllReferrers', 'Referers_WidgetGetAll', 'Referers.getAll');

            $byTypeCategory = Piwik_Translate('Referers_ViewReferrersBy', Piwik_Translate('Live_GoalType'));
            $referrersReportsByDimension->addReport(
                $byTypeCategory, 'Referers_WidgetKeywords', 'Referers.getKeywords');
            $referrersReportsByDimension->addReport($byTypeCategory, 'SitesManager_Sites', 'Referers.getWebsites');
            $referrersReportsByDimension->addReport($byTypeCategory, 'Referers_Campaigns', 'Referers.getCampaigns');

            $bySourceCategory = Piwik_Translate('Referers_ViewReferrersBy', Piwik_Translate('General_Source'));
            $referrersReportsByDimension->addReport($bySourceCategory, 'Referers_Socials', 'Referers.getSocials');
            $referrersReportsByDimension->addReport(
                $bySourceCategory, 'Referers_SearchEngines', 'Referers.getSearchEngines');

            $result = $referrersReportsByDimension->render();
        }

        return $result;
    }

    public function getSearchEnginesAndKeywords()
    {
        $view = new View('@Referers/getSearchEnginesAndKeywords');
        $view->searchEngines = $this->getSearchEngines(true);
        $view->keywords = $this->getKeywords(true);
        echo $view->render();
    }

    public function getRefererType($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    /**
     * Returns or echo's a report that shows all search keyword, website and campaign
     * referrer information in one report.
     *
     * @param bool $fetch True if the report HTML should be returned. If false, the
     *                    report is echo'd and nothing is returned.
     * @return string The report HTML or nothing if $fetch is set to false.
     */
    public function getAll($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function getKeywords($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function getSearchEnginesFromKeywordId($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function getSearchEngines($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function getKeywordsFromSearchEngineId($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function indexWebsites($fetch = false)
    {
        $view = new View('@Referers/indexWebsites');
        $view->websites = $this->getWebsites(true);
        $view->socials = $this->getSocials(true);
        if ($fetch) {
            return $view->render();
        } else {
            echo $view->render();
        }
    }

    public function getWebsites($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function getSocials($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function getUrlsForSocial($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function indexCampaigns($fetch = false)
    {
        return View::singleReport(
            Piwik_Translate('Referers_Campaigns'),
            $this->getCampaigns(true), $fetch);
    }

    public function getCampaigns($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function getKeywordsFromCampaignId($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    public function getUrlsFromWebsiteId($fetch = false)
    {
        return ViewDataTable::renderReport($this->pluginName, __FUNCTION__, $fetch);
    }

    protected function getReferersVisitorsByType($date = false)
    {
        if ($date === false) {
            $date = Common::getRequestVar('date', false);
        }

        // we disable the queued filters because here we want to get the visits coming from search engines
        // if the filters were applied we would have to look up for a label looking like "Search Engines"
        // which is not good when we have translations
        $dataTableReferersType = Request::processRequest(
            "Referers.getRefererType", array('disable_queued_filters' => '1', 'date' => $date));

        $nameToColumnId = array(
            'visitorsFromSearchEngines' => Common::REFERER_TYPE_SEARCH_ENGINE,
            'visitorsFromDirectEntry'   => Common::REFERER_TYPE_DIRECT_ENTRY,
            'visitorsFromWebsites'      => Common::REFERER_TYPE_WEBSITE,
            'visitorsFromCampaigns'     => Common::REFERER_TYPE_CAMPAIGN,
        );
        $return = array();
        foreach ($nameToColumnId as $nameVar => $columnId) {
            $value = 0;
            $row = $dataTableReferersType->getRowFromLabel($columnId);
            if ($row !== false) {
                $value = $row->getColumn(Metrics::INDEX_NB_VISITS);
            }
            $return[$nameVar] = $value;
        }
        return $return;
    }

    protected $referrerTypeToLabel = array(
        Common::REFERER_TYPE_DIRECT_ENTRY  => 'Referers_DirectEntry',
        Common::REFERER_TYPE_SEARCH_ENGINE => 'Referers_SearchEngines',
        Common::REFERER_TYPE_WEBSITE       => 'Referers_Websites',
        Common::REFERER_TYPE_CAMPAIGN      => 'Referers_Campaigns',
    );

    public function getEvolutionGraph($fetch = false, $typeReferer = false, array $columns = array())
    {
        $view = $this->getLastUnitGraph($this->pluginName, __FUNCTION__, 'Referers.getRefererType');

        $view->visualization_properties->add_total_row = true;

        // configure displayed columns
        if (empty($columns)) {
            $columns = Common::getRequestVar('columns');
            $columns = Piwik::getArrayFromApiParameter($columns);
        }
        $columns = !is_array($columns) ? array($columns) : $columns;
        $view->columns_to_display = $columns;

        // configure selectable columns
        if (Common::getRequestVar('period', false) == 'day') {
            $selectable = array('nb_visits', 'nb_uniq_visitors', 'nb_actions');
        } else {
            $selectable = array('nb_visits', 'nb_actions');
        }
        $view->visualization_properties->selectable_columns = $selectable;

        // configure displayed rows
        $visibleRows = Common::getRequestVar('rows', false);
        if ($visibleRows !== false) {
            // this happens when the row picker has been used
            $visibleRows = Piwik::getArrayFromApiParameter($visibleRows);

            // typeReferer is redundant if rows are defined, so make sure it's not used
            $view->custom_parameters['typeReferer'] = false;
        } else {
            // use $typeReferer as default
            if ($typeReferer === false) {
                $typeReferer = Common::getRequestVar('typeReferer', false);
            }
            $label = self::getTranslatedReferrerTypeLabel($typeReferer);
            $total = Piwik_Translate('General_Total');
            $visibleRows = array($label, $total);
            $view->request_parameters_to_modify['rows'] = $label . ',' . $total;
        }
        $view->visualization_properties->row_picker_match_rows_by = 'label';
        $view->visualization_properties->rows_to_display = $visibleRows;

        $view->documentation = Piwik_Translate('Referers_EvolutionDocumentation') . '<br />'
            . Piwik_Translate('General_BrokenDownReportDocumentation') . '<br />'
            . Piwik_Translate('Referers_EvolutionDocumentationMoreInfo', '&quot;'
                . Piwik_Translate('Referers_DetailsByRefererType') . '&quot;');

        return $this->renderView($view, $fetch);
    }

    public function getLastDistinctSearchEnginesGraph($fetch = false)
    {
        $view = $this->getLastUnitGraph($this->pluginName, __FUNCTION__, "Referers.getNumberOfDistinctSearchEngines");
        $view->translations['Referers_distinctSearchEngines'] = ucfirst(Piwik_Translate('Referers_DistinctSearchEngines'));
        $view->columns_to_display = array('Referers_distinctSearchEngines');
        return $this->renderView($view, $fetch);
    }

    public function getLastDistinctKeywordsGraph($fetch = false)
    {
        $view = $this->getLastUnitGraph($this->pluginName, __FUNCTION__, "Referers.getNumberOfDistinctKeywords");
        $view->translations['Referers_distinctKeywords'] = ucfirst(Piwik_Translate('Referers_DistinctKeywords'));
        $view->columns_to_display = array('Referers_distinctKeywords');
        return $this->renderView($view, $fetch);
    }

    public function getLastDistinctWebsitesGraph($fetch = false)
    {
        $view = $this->getLastUnitGraph($this->pluginName, __FUNCTION__, "Referers.getNumberOfDistinctWebsites");
        $view->translations['Referers_distinctWebsites'] = ucfirst(Piwik_Translate('Referers_DistinctWebsites'));
        $view->columns_to_display = array('Referers_distinctWebsites');
        return $this->renderView($view, $fetch);
    }

    public function getLastDistinctCampaignsGraph($fetch = false)
    {
        $view = $this->getLastUnitGraph($this->pluginName, __FUNCTION__, "Referers.getNumberOfDistinctCampaigns");
        $view->translations['Referers_distinctCampaigns'] = ucfirst(Piwik_Translate('Referers_DistinctCampaigns'));
        $view->columns_to_display = array('Referers_distinctCampaigns');
        return $this->renderView($view, $fetch);
    }

    function getKeywordsForPage()
    {
        Piwik::checkUserHasViewAccess($this->idSite);

        $requestUrl = '&date=previous1'
            . '&period=week'
            . '&idSite=' . $this->idSite;

        $topPageUrlRequest = $requestUrl
            . '&method=Actions.getPageUrls'
            . '&filter_limit=50'
            . '&format=original';
        $request = new Request($topPageUrlRequest);
        $request = $request->process();
        $tables = $request->getArray();

        $topPageUrl = false;
        $first = key($tables);
        if (!empty($first)) {
            $topPageUrls = $tables[$first];
            $topPageUrls = $topPageUrls->getRowsMetadata('url');
            $tmpTopPageUrls = array_values($topPageUrls);
            $topPageUrl = current($tmpTopPageUrls);
        }
        if (empty($topPageUrl)) {
            $topPageUrl = $this->site->getMainUrl();
        }
        $url = $topPageUrl;

        // HTML
        $api = Url::getCurrentUrlWithoutFileName()
            . '?module=API&method=Referers.getKeywordsForPageUrl'
            . '&format=php'
            . '&filter_limit=10'
            . '&token_auth=' . Piwik::getCurrentUserTokenAuth();

        $api .= $requestUrl;
        $code = '
// This function will call the API to get best keyword for current URL.
// Then it writes the list of best keywords in a HTML list
function DisplayTopKeywords($url = "")
{
	// Do not spend more than 1 second fetching the data
	@ini_set("default_socket_timeout", $timeout = 1);
	// Get the Keywords data
	$url = empty($url) ? "http://". $_SERVER["HTTP_HOST"] . $_SERVER["REQUEST_URI"] : $url;
	$api = "' . $api . '&url=" . urlencode($url);
	$keywords = @unserialize(file_get_contents($api));
	if($keywords === false || isset($keywords["result"])) {
		// DEBUG ONLY: uncomment for troubleshooting an empty output (the URL output reveals the token_auth)
		// echo "Error while fetching the <a href=\'$api\'>Top Keywords from Piwik</a>";
		return;
	}

	// Display the list in HTML
	$url = htmlspecialchars($url, ENT_QUOTES);
	$output = "<h2>Top Keywords for <a href=\'$url\'>$url</a></h2><ul>";
	foreach($keywords as $keyword) {
		$output .= "<li>". $keyword[0]. "</li>";
	}
	if(empty($keywords)) { $output .= "Nothing yet..."; }
	$output .= "</ul>";
	echo $output;
}
';

        $jsonRequest = str_replace('format=php', 'format=json', $api);
        echo "<p>This widget is designed to work in your website directly.
		This widget makes it easy to use Piwik to <i>automatically display the list of Top Keywords</i>, for each of your website Page URLs.</p>
		<p>
		<b>Example API URL</b> - For example if you would like to get the top 10 keywords, used last week, to land on the page <a target='_blank' href='$topPageUrl'>$topPageUrl</a>,
		in format JSON: you would dynamically fetch the data using <a target='_blank' href='$jsonRequest&url=" . urlencode($topPageUrl) . "'>this API request URL</a>. Make sure you encode the 'url' parameter in the URL.</p>

		<p><b>PHP Function ready to use!</b> - If you use PHP on your website, we have prepared a small code snippet that you can copy paste in your Website PHP files. You can then simply call the function <code>DisplayTopKeywords();</code> anywhere in your template, at the bottom of the content or in your blog sidebar.
		If you run this code in your page $topPageUrl, it would output the following:";

        echo "<div style='width:400px;margin-left:20px;padding:10px;border:1px solid black;'>";
        function DisplayTopKeywords($url = "", $api)
        {
            // Do not spend more than 1 second fetching the data
            @ini_set("default_socket_timeout", $timeout = 1);
            // Get the Keywords data
            $url = empty($url) ? "http://" . $_SERVER["HTTP_HOST"] . $_SERVER["REQUEST_URI"] : $url;
            $api = $api . "&url=" . urlencode($url);
            $keywords = @unserialize(file_get_contents($api));
            if ($keywords === false || isset($keywords["result"])) {
                // DEBUG ONLY: uncomment for troubleshooting an empty output (the URL output reveals the token_auth)
                //echo "Error while fetching the <a href=\'".$api."\'>Top Keywords from Piwik</a>";
                return;
            }

            // Display the list in HTML
            $url = htmlspecialchars($url, ENT_QUOTES);
            $output = "<h2>Top Keywords for <a href=\'$url\'>$url</a></h2><ul>";
            foreach ($keywords as $keyword) {
                $output .= "<li>" . $keyword[0] . "</li>";
            }
            if (empty($keywords)) {
                $output .= "Nothing yet...";
            }
            $output .= "</ul>";
            echo $output;
        }

        DisplayTopKeywords($topPageUrl, $api);

        echo "</div><br/>
		<p>Here is the PHP function that you can paste in your pages:</P>
		<textarea cols=60 rows=8>&lt;?php\n" . htmlspecialchars($code) . "\n DisplayTopKeywords();</textarea>
		";

        echo "
		<p><strong>Notes</strong>: You can for example edit the code to to make the Top search keywords link to your Website search result pages.
		<br/>On medium to large traffic websites, we recommend to cache this data, as to minimize the performance impact of calling the Piwik API on each page view.
		</p>
		";
    }

    /**
     * Returns the i18n-ized label for a referrer type.
     *
     * @param int $typeReferrer The referrer type. Referrer types are defined in Common class.
     * @return string The i18n-ized label.
     */
    public static function getTranslatedReferrerTypeLabel($typeReferrer)
    {
        $label = getRefererTypeLabel($typeReferrer);
        return Piwik_Translate($label);
    }

    /**
     * Returns the URL for the sparkline of visits with a specific referrer type.
     *
     * @param int $referrerType The referrer type. Referrer types are defined in Common class.
     * @return string The URL that can be used to get a sparkline image.
     */
    private function getReferrerUrlSparkline($referrerType)
    {
        $totalRow = Piwik_Translate('General_Total');
        return $this->getUrlSparkline(
            'getEvolutionGraph',
            array('columns'     => array('nb_visits'),
                  'rows'        => array(self::getTranslatedReferrerTypeLabel($referrerType), $totalRow),
                  'typeReferer' => $referrerType)
        );
    }

    /**
     * Returns an array containing the number of distinct referrers for each
     * referrer type.
     *
     * @param bool|string $date The date to use when getting metrics. If false, the
     *                           date query param is used.
     * @return array The metrics.
     */
    private function getDistinctReferrersMetrics($date = false)
    {
        $propertyToAccessorMapping = array(
            'numberDistinctSearchEngines' => 'getNumberOfDistinctSearchEngines',
            'numberDistinctKeywords'      => 'getNumberOfDistinctKeywords',
            'numberDistinctWebsites'      => 'getNumberOfDistinctWebsites',
            'numberDistinctWebsitesUrls'  => 'getNumberOfDistinctWebsitesUrls',
            'numberDistinctCampaigns'     => 'getNumberOfDistinctCampaigns',
        );

        $result = array();
        foreach ($propertyToAccessorMapping as $property => $method) {
            $result[$property] = $this->getNumericValue('Referers.' . $method, $date);
        }
        return $result;
    }

    /**
     * Utility method that calculates evolution values for a set of current & past values
     * and sets properties on a View w/ HTML that displays the evolution percents.
     *
     * @param View $view The view to set properties on.
     * @param string $date The date of the current values.
     * @param array $currentValues Array mapping view property names w/ present values.
     * @param string $lastPeriodDate The date of the period in the past.
     * @param array $previousValues Array mapping view property names w/ past values. Keys
     *                              in this array should be the same as keys in $currentValues.
     */
    private function addEvolutionPropertiesToView($view, $date, $currentValues, $lastPeriodDate, $previousValues)
    {
        foreach ($previousValues as $name => $pastValue) {
            $currentValue = $currentValues[$name];
            $evolutionName = $name . 'Evolution';

            $view->$evolutionName = $this->getEvolutionHtml($date, $currentValue, $lastPeriodDate, $pastValue);
        }
    }
}