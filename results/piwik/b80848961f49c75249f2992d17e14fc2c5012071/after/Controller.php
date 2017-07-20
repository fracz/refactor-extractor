<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik_Plugins
 * @package Piwik_CoreHome
 */
use Piwik\API\Request;
use Piwik\Piwik;
use Piwik\Common;
use Piwik\Date;
use Piwik\AssetManager;
use Piwik\Controller;
use Piwik\FrontController;
use Piwik\View;
use Piwik\Url;
use Piwik\UpdateCheck;
use Piwik\Site;


/**
 *
 * @package Piwik_CoreHome
 */
class Piwik_CoreHome_Controller extends Controller
{
    function getDefaultAction()
    {
        return 'redirectToCoreHomeIndex';
    }

    function redirectToCoreHomeIndex()
    {
        $defaultReport = Piwik_UsersManager_API::getInstance()->getUserPreference(Piwik::getCurrentUserLogin(), Piwik_UsersManager_API::PREFERENCE_DEFAULT_REPORT);
        $module = 'CoreHome';
        $action = 'index';

        // User preference: default report to load is the All Websites dashboard
        if ($defaultReport == 'MultiSites'
            && \Piwik\PluginsManager::getInstance()->isPluginActivated('MultiSites')
        ) {
            $module = 'MultiSites';
        }
        if ($defaultReport == Piwik::getLoginPluginName()) {
            $module = Piwik::getLoginPluginName();
        }
        $idSite = Common::getRequestVar('idSite', false, 'int');

        parent::redirectToIndex($module, $action, !empty($idSite) ? $idSite : null);
    }

    public function showInContext()
    {
        $controllerName = Common::getRequestVar('moduleToLoad');
        $actionName = Common::getRequestVar('actionToLoad', 'index');
        if ($actionName == 'showInContext') {
            throw new Exception("Preventing infinite recursion...");
        }
        $view = $this->getDefaultIndexView();
        $view->content = FrontController::getInstance()->fetchDispatch($controllerName, $actionName);
        echo $view->render();
    }

    protected function getDefaultIndexView()
    {
        $view = new View('@CoreHome/getDefaultIndexView');
        $this->setGeneralVariablesView($view);
        $view->menu = Piwik_GetMenu();
        $view->content = '';
        return $view;
    }

    protected function setDateTodayIfWebsiteCreatedToday()
    {
        $date = Common::getRequestVar('date', false);
        if ($date == 'today'
            || Common::getRequestVar('period', false) == 'range'
        ) {
            return;
        }
        $websiteId = Common::getRequestVar('idSite', false, 'int');
        if ($websiteId) {
            $website = new Site($websiteId);
            $datetimeCreationDate = $this->site->getCreationDate()->getDatetime();
            $creationDateLocalTimezone = Date::factory($datetimeCreationDate, $website->getTimezone())->toString('Y-m-d');
            $todayLocalTimezone = Date::factory('now', $website->getTimezone())->toString('Y-m-d');
            if ($creationDateLocalTimezone == $todayLocalTimezone) {
                Piwik::redirectToModule('CoreHome', 'index',
                    array('date'   => 'today',
                          'idSite' => $websiteId,
                          'period' => Common::getRequestVar('period'))
                );
            }
        }
    }

    public function index()
    {
        $this->setDateTodayIfWebsiteCreatedToday();
        $view = $this->getDefaultIndexView();
        echo $view->render();
    }

    /**
     * This method is called when the asset manager is configured in merged mode.
     * It returns the content of the css merged file.
     *
     * @see core/AssetManager.php
     */
    public function getCss()
    {
        $cssMergedFile = AssetManager::getMergedCssFileLocation();
        Piwik::serveStaticFile($cssMergedFile, "text/css");
    }

    /**
     * This method is called when the asset manager is configured in merged mode.
     * It returns the content of the js merged file.
     *
     * @see core/AssetManager.php
     */
    public function getJs()
    {
        $jsMergedFile = AssetManager::getMergedJsFileLocation();
        Piwik::serveStaticFile($jsMergedFile, "application/javascript; charset=UTF-8");
    }


    //  --------------------------------------------------------
    //  ROW EVOLUTION
    //  The following methods render the popover that shows the
    //  evolution of a singe or multiple rows in a data table
    //  --------------------------------------------------------

    /** Render the entire row evolution popover for a single row */
    public function getRowEvolutionPopover()
    {
        $rowEvolution = $this->makeRowEvolution($isMulti = false);
        $view = new View('@CoreHome/getRowEvolutionPopover');
        echo $rowEvolution->renderPopover($this, $view);
    }

    /** Render the entire row evolution popover for multiple rows */
    public function getMultiRowEvolutionPopover()
    {
        $rowEvolution = $this->makeRowEvolution($isMulti = true);
        $view = new View('@CoreHome/getMultiRowEvolutionPopover');
        echo $rowEvolution->renderPopover($this, $view);
    }

    /** Generic method to get an evolution graph or a sparkline for the row evolution popover */
    public function getRowEvolutionGraph($fetch = false, $rowEvolution = null)
    {
        if (empty($rowEvolution)) {
            $label = Common::getRequestVar('label', '', 'string');
            $isMultiRowEvolution = strpos($label, ',') !== false;

            $rowEvolution = $this->makeRowEvolution($isMultiRowEvolution, $graphType = 'graphEvolution');
            $rowEvolution->useAvailableMetrics();
        }

        $view = $rowEvolution->getRowEvolutionGraph();
        return $this->renderView($view, $fetch);
    }

    /** Utility function. Creates a RowEvolution instance. */
    private function makeRowEvolution($isMultiRowEvolution, $graphType = null)
    {
        if ($isMultiRowEvolution) {
            return new Piwik_CoreHome_DataTableRowAction_MultiRowEvolution($this->idSite, $this->date, $graphType);
        } else {
            return new Piwik_CoreHome_DataTableRowAction_RowEvolution($this->idSite, $this->date, $graphType);
        }
    }

    /**
     * Forces a check for updates and re-renders the header message.
     *
     * This will check piwik.org at most once per 10s.
     */
    public function checkForUpdates()
    {
        Piwik::checkUserHasSomeAdminAccess();
        $this->checkTokenInUrl();

        // perform check (but only once every 10s)
        UpdateCheck::check($force = false, UpdateCheck::UI_CLICK_CHECK_INTERVAL);

        $view = new View('@CoreHome/checkForUpdates');
        $this->setGeneralVariablesView($view);
        echo $view->render();
    }

    /**
     * Renders and echo's the in-app donate form w/ slider.
     */
    public function getDonateForm()
    {
        $view = new View('@CoreHome/getDonateForm');
        if (Common::getRequestVar('widget', false)
            && Piwik::isUserIsSuperUser()
        ) {
            $view->footerMessage = Piwik_Translate('CoreHome_OnlyForAdmin');
        }
        echo $view->render();
    }

    /**
     * Renders and echo's HTML that displays the Piwik promo video.
     */
    public function getPromoVideo()
    {
        $view = new View('@CoreHome/getPromoVideo');
        $view->shareText = Piwik_Translate('CoreHome_SharePiwikShort');
        $view->shareTextLong = Piwik_Translate('CoreHome_SharePiwikLong');
        $view->promoVideoUrl = 'http://www.youtube.com/watch?v=OslfF_EH81g';
        echo $view->render();
    }

    /**
     * Redirects the user to a paypal so they can donate to Piwik.
     */
    public function redirectToPaypal()
    {
        $parameters = Request::getRequestArrayFromString($request = null);
        foreach ($parameters as $name => $param) {
            if ($name == 'idSite'
                || $name == 'module'
                || $name == 'action'
            ) {
                unset($parameters[$name]);
            }
        }

        $url = "https://www.paypal.com/cgi-bin/webscr?".Url::getQueryStringFromParameters($parameters);

        header("Location: $url");
        exit;
    }
}