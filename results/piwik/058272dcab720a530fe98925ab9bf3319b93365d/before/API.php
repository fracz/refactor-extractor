<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 *
 * @category Piwik_Plugins
 * @package Piwik_Referers
 */

/**
 * @see plugins/Referers/functions.php
 */
require_once PIWIK_INCLUDE_PATH . '/plugins/Referers/functions.php';

/**
 *
 * @package Piwik_Referers
 */
class Piwik_Referers_API
{
	static private $instance = null;
	static public function getInstance()
	{
		if (self::$instance == null)
		{
			self::$instance = new self;
		}
		return self::$instance;
	}

	/**
	 * @return Piwik_DataTable
	 */
	protected function getDataTable($name, $idSite, $period, $date, $expanded, $idSubtable = null)
	{
	    $dataTable = Piwik_Archive::getDataTableFromArchive($name, $idSite, $period, $date, $expanded, $idSubtable = null);

	    $dataTable->filter('Sort', array(Piwik_Archive::INDEX_NB_VISITS, 'desc', $naturalSort = false, $expanded));
		$dataTable->queueFilter('ReplaceColumnNames', array($expanded));
		return $dataTable;
	}

	public function getRefererType($idSite, $period, $date, $typeReferer = false)
	{
		$dataTable = $this->getDataTable('Referers_type', $idSite, $period, $date, $expanded = false);
		if($typeReferer !== false)
		{
			$dataTable->filter('Pattern', array('label', $typeReferer));
		}
		$dataTable->queueFilter('ColumnCallbackReplace', array('label', 'Piwik_getRefererTypeLabel'));
		return $dataTable;
	}

	public function getKeywords($idSite, $period, $date, $expanded = false)
	{
		$dataTable = $this->getDataTable('Referers_searchEngineByKeyword', $idSite, $period, $date, $expanded);
		return $dataTable;
	}

	public function getSearchEnginesFromKeywordId($idSite, $period, $date, $idSubtable)
	{
		$dataTable = $this->getDataTable('Referers_searchEngineByKeyword',$idSite, $period, $date, $expanded = false, $idSubtable);
		$dataTable->queueFilter('ColumnCallbackAddMetadata', array( 'label', 'url', 'Piwik_getSearchEngineUrlFromName') );
		$dataTable->queueFilter('MetadataCallbackAddMetadata', array( 'url', 'logo', 'Piwik_getSearchEngineLogoFromUrl') );

		// get the keyword and create the URL to the search result page
		$keywords = $this->getKeywords($idSite, $period, $date);
		$subTable = $keywords->getRowFromIdSubDataTable($idSubtable);
		if($subTable)
		{
    		$keyword = $subTable->getColumn('label');
    		$dataTable->queueFilter('MetadataCallbackReplace', array( 'url', 'Piwik_getSearchEngineUrlFromUrlAndKeyword', array($keyword)) );
		}
		return $dataTable;
	}

	public function getSearchEngines($idSite, $period, $date, $expanded = false)
	{
		$dataTable = $this->getDataTable('Referers_keywordBySearchEngine',$idSite, $period, $date, $expanded);
		$dataTable->queueFilter('ColumnCallbackAddMetadata', array( 'label', 'url', 'Piwik_getSearchEngineUrlFromName') );
		$dataTable->queueFilter('MetadataCallbackAddMetadata', array( 'url', 'logo', 'Piwik_getSearchEngineLogoFromUrl') );
		return $dataTable;
	}

	public function getKeywordsFromSearchEngineId($idSite, $period, $date, $idSubtable)
	{
		$dataTable = $this->getDataTable('Referers_keywordBySearchEngine',$idSite, $period, $date, $expanded = false, $idSubtable);

		// get the search engine and create the URL to the search result page
		$searchEngines = $this->getSearchEngines($idSite, $period, $date);
		$searchEngines->applyQueuedFilters();
		$subTable = $searchEngines->getRowFromIdSubDataTable($idSubtable);
		if($subTable)
		{
    		$searchEngineUrl = $subTable->getMetadata('url');
    		$dataTable->queueFilter('ColumnCallbackAddMetadata', array( 'label', 'url', 'Piwik_getSearchEngineUrlFromKeywordAndUrl', array($searchEngineUrl)));
		}
		return $dataTable;
	}

	public function getCampaigns($idSite, $period, $date, $expanded = false)
	{
		$dataTable = $this->getDataTable('Referers_keywordByCampaign',$idSite, $period, $date, $expanded);
		return $dataTable;
	}

	public function getKeywordsFromCampaignId($idSite, $period, $date, $idSubtable)
	{
		$dataTable = $this->getDataTable('Referers_keywordByCampaign',$idSite, $period, $date, $expanded = false, $idSubtable);
		return $dataTable;
	}

	public function getWebsites($idSite, $period, $date, $expanded = false)
	{
		$dataTable = $this->getDataTable('Referers_urlByWebsite',$idSite, $period, $date, $expanded);
		return $dataTable;
	}

	public function getUrlsFromWebsiteId($idSite, $period, $date, $idSubtable)
	{
		$dataTable = $this->getDataTable('Referers_urlByWebsite',$idSite, $period, $date, $expanded = false, $idSubtable);
		// the htmlspecialchars_decode call is for BC for before 1.1
		// as the Referer URL was previously encoded in the log tables, but is now recorded raw
		$dataTable->queueFilter('ColumnCallbackAddMetadata', array( 'label', 'url', create_function('$label', 'return htmlspecialchars_decode($label);')) );
		$dataTable->queueFilter('ColumnCallbackReplace', array('label', 'Piwik_getPathFromUrl'));
		return $dataTable;
	}

	public function getNumberOfDistinctSearchEngines($idSite, $period, $date)
	{
		return $this->getNumeric('Referers_distinctSearchEngines', $idSite, $period, $date);
	}

	public function getNumberOfDistinctKeywords($idSite, $period, $date)
	{
		return $this->getNumeric('Referers_distinctKeywords', $idSite, $period, $date);
	}

	public function getNumberOfDistinctCampaigns($idSite, $period, $date)
	{
		return $this->getNumeric('Referers_distinctCampaigns', $idSite, $period, $date);
	}

	public function getNumberOfDistinctWebsites($idSite, $period, $date)
	{
		return $this->getNumeric('Referers_distinctWebsites', $idSite, $period, $date);
	}

	public function getNumberOfDistinctWebsitesUrls($idSite, $period, $date)
	{
		return $this->getNumeric('Referers_distinctWebsitesUrls', $idSite, $period, $date);
	}

	private function getNumeric($name, $idSite, $period, $date)
	{
		Piwik::checkUserHasViewAccess( $idSite );
		$archive = Piwik_Archive::build($idSite, $period, $date );
		return $archive->getDataTableFromNumeric($name);
	}
}