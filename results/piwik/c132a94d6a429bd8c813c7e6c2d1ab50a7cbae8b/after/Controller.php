<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id: Controller.php 7446 2012-11-12 09:16:00Z EZdesign $
 *
 * @category Piwik_Plugins
 * @package Piwik_Overlay
 */

class Piwik_Overlay_Controller extends Piwik_Controller
{

	/** The index of the plugin */
	public function index()
	{
		Piwik::checkUserHasViewAccess($this->idSite);

		$template = 'index';
		if (Piwik_Config::getInstance()->General['overlay_disable_framed_mode']) {
			$template = 'index_noframe';
		}

		$view = Piwik_View::factory($template);
		$view->idSite = $this->idSite;
		$view->date = Piwik_Common::getRequestVar('date', 'today');
		$view->period = Piwik_Common::getRequestVar('period', 'day');

		// the % signs in the encoded url have been replaced with $ to make sure that
		// browsers can't break the exact encoding. we need to reverse this here.
		// see javascript Piwik_Overlay.setCurrentUrl()
		$url = Piwik_Common::getRequestVar('overlayUrl', '');
		$url = urldecode(str_replace('$', '%', $url));
		$view->targetUrl = $url;

		$view->ssl = $this->usingSsl();

		echo $view->render();
	}

	/** Render the area left of the iframe */
	public function renderSidebar()
	{
		$idSite = Piwik_Common::getRequestVar('idSite');
		$period = Piwik_Common::getRequestVar('period');
		$date = Piwik_Common::getRequestVar('date');
		$currentUrl = Piwik_Common::getRequestVar('currentUrl');
		$currentUrl = Piwik_Common::unsanitizeInputValue($currentUrl);

		$normalizedCurrentUrl = Piwik_Tracker_Action::excludeQueryParametersFromUrl($currentUrl, $idSite);
		$normalizedCurrentUrl = Piwik_Common::unsanitizeInputValue($normalizedCurrentUrl);

		// load the appropriate row of the page urls report using the label filter
		Piwik_Actions_ArchivingHelper::reloadConfig();
		$path = Piwik_Actions_ArchivingHelper::getActionExplodedNames($normalizedCurrentUrl, Piwik_Tracker_Action::TYPE_ACTION_URL);
		$path = array_map('urlencode', $path);
		$label = implode('>', $path);
		$request = new Piwik_API_Request(
			'method=Actions.getPageUrls'
			.'&idSite='.urlencode($idSite)
			.'&date='.urlencode($date)
			.'&period='.urlencode($period)
			.'&label='.urlencode($label)
			.'&format=original'
		);
 		$dataTable = $request->process();

		$data = array();
		if ($dataTable->getRowsCount() > 0)
		{
			$row = $dataTable->getFirstRow();

			$translations = Piwik_API_API::getDefaultMetricTranslations();
			$showMetrics = array('nb_hits', 'nb_visits', 'nb_uniq_visitors',
					'bounce_rate', 'exit_rate', 'avg_time_on_page');


			foreach ($showMetrics as $metric)
			{
				$value = $row->getColumn($metric);
				if ($value === false)
				{
					// skip unique visitors for period != day
					continue;
				}
				if ($metric == 'avg_time_on_page')
				{
					$value = Piwik::getPrettyTimeFromSeconds($value);
				}
				$data[] = array(
					'name' => $translations[$metric],
					'value' => $value
				);
			}
		}

		// generate page url string
		foreach ($path as &$part)
		{
			$part = preg_replace(';^/;', '', urldecode($part));
		}
		$page = '/'.implode('/', $path);
		$page = preg_replace(';/index$;', '/', $page);
		if ($page == '/')
		{
			$page = '/index';
		}

		// render template
		$view = Piwik_View::factory('sidebar');
		$view->data = $data;
		$view->location = $page;
		$view->idSite = $idSite;
		$view->period = $period;
		$view->date = $date;
		$view->currentUrl = $currentUrl;
		echo $view->render();
	}

	/**
	 * Start an Overlay session: Redirect to the tracked website. The Piwik
	 * tracker will recognize this referrer and start the session.
	 */
	public function startOverlaySession()
	{
		$idSite = Piwik_Common::getRequestVar('idsite', 0, 'int');
		Piwik::checkUserHasViewAccess($idSite);

		$sitesManager = Piwik_SitesManager_API::getInstance();
		$site = $sitesManager->getSiteFromId($idSite);
		$urls = $sitesManager->getSiteUrlsFromId($idSite);

		echo '
			<script type="text/javascript">
				function handleProtocol(url) {
					if (' . ($this->usingSsl() ? 'true' : 'false') . ') {
						return url.replace(/http:\/\//i, "https://");
					} else {
						return url.replace(/https:\/\//i, "http://");
					}
				}

				function removeUrlPrefix(url) {
					return url.replace(/http(s)?:\/\/(www\.)?/i, "");
				}

				function htmlEntities(str) {
				    return String(str).replace(/&/g, \'&amp;\').replace(/</g, \'&lt;\').replace(/>/g, \'&gt;\').replace(/"/g, \'&quot;\');
				}

				if (window.location.hash) {
					var match = false;

					var urlToRedirect = window.location.hash.substr(1);
					var urlToRedirectWithoutPrefix = removeUrlPrefix(urlToRedirect);

					var knownUrls = '.json_encode($urls).';
					for (var i = 0; i < knownUrls.length; i++) {
						var testUrl = removeUrlPrefix(knownUrls[i]);
						if (urlToRedirectWithoutPrefix.substr(0, testUrl.length) == testUrl) {
							match = true;
							window.location.href = handleProtocol(urlToRedirect);
							break;
						}
					}

					if (!match) {
						var error = "'.htmlentities(Piwik_Translate('Overlay_RedirectUrlError')).'";
						error = error.replace(/%s/, htmlEntities(urlToRedirect));
						error = error.replace(/%s/, "<br />");
						document.write(error);
					}
				}
				else {
					window.location.href = handleProtocol("'.$site['main_url'].'");
				};
			</script>
		';
	}

	/**
	 * This method is used to pass information from the iframe back to Piwik.
	 * Due to the same origin policy, we can't do that directly, so we inject
	 * an additional iframe in the Overlay session that calls this controller
	 * method.
	 * The rendered iframe is from the same origin as the Piwik window so we
	 * can bypass the same origin policy and call the parent.
	 */
	public function notifyParentIframe()
	{
		$view = Piwik_View::factory('notify_parent_iframe');
		echo $view->render();
	}

	/**
	 * Detect whether Piwik is used over SSL
	 */
	private function usingSsl()
	{
		return isset($_SERVER['HTTPS']) && $_SERVER['HTTPS'] && $_SERVER['HTTPS'] !== 'off';
	}

}