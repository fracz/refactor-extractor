<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 *
 * @category Piwik_Plugins
 * @package Piwik_Actions
 */

/**
 * Actions API
 *
 * @package Piwik_Actions
 */
class Piwik_Actions_API
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

	protected function getDataTable($name, $idSite, $period, $date, $expanded, $idSubtable )
	{
		Piwik::checkUserHasViewAccess( $idSite );
		$archive = Piwik_Archive::build($idSite, $period, $date );
		if($idSubtable === false)
		{
			$idSubtable = null;
		}

		if($expanded)
		{
			$dataTable = $archive->getDataTableExpanded($name, $idSubtable);
		}
		else
		{
			$dataTable = $archive->getDataTable($name, $idSubtable);
		}
		// Must be applied before Sort in this case, since the DataTable can contain both int and strings indexes
		// (in the transition period between pre 1.2 and post 1.2 datatable structure)
		$dataTable->filter('ReplaceColumnNames', array($recursive = true));
		$dataTable->filter('Sort', array('nb_visits', 'desc', $naturalSort = false, $expanded));
		$dataTable->queueFilter('ReplaceSummaryRowLabel');
		return $dataTable;
	}

	/**
	 * Backward compatibility. Fallsback to getPageTitles() instead.
	 * @deprecated Deprecated since Piwik 0.5
	 */
	public function getActions( $idSite, $period, $date, $expanded = false, $idSubtable = false )
	{
	    return $this->getPageTitles( $idSite, $period, $date, $expanded, $idSubtable );
	}

	public function getPageUrls( $idSite, $period, $date, $expanded = false, $idSubtable = false )
	{
		$dataTable = $this->getDataTable('Actions_actions_url', $idSite, $period, $date, $expanded, $idSubtable );
		$this->filterPageDatatable($dataTable);
		return $dataTable;
	}

	protected function filterPageDatatable($dataTable)
	{
		// Average time on page = total time on page / number visits on that page
		$dataTable->queueFilter('ColumnCallbackAddColumnQuotient', array('avg_time_on_page', 'sum_time_spent', 'nb_visits', 0));

		// Bounce rate = single page visits on this page / visits started on this page
		$dataTable->queueFilter('ColumnCallbackAddColumnPercentage', array('bounce_rate', 'entry_bounce_count', 'entry_nb_visits', 0));

		// % Exit = Number of visits that finished on this page / visits on this page
		$dataTable->queueFilter('ColumnCallbackAddColumnPercentage', array('exit_rate', 'exit_nb_visits', 'nb_hits', 0));
	}

	public function getPageTitles( $idSite, $period, $date, $expanded = false, $idSubtable = false)
	{
		$dataTable = $this->getDataTable('Actions_actions', $idSite, $period, $date, $expanded, $idSubtable);
		$this->filterPageDatatable($dataTable);
		return $dataTable;
	}

	public function getDownloads( $idSite, $period, $date, $expanded = false, $idSubtable = false )
	{
		$dataTable = $this->getDataTable('Actions_downloads', $idSite, $period, $date, $expanded, $idSubtable );
		return $dataTable;
	}

	public function getOutlinks( $idSite, $period, $date, $expanded = false, $idSubtable = false )
	{
		$dataTable = $this->getDataTable('Actions_outlink', $idSite, $period, $date, $expanded, $idSubtable );
		return $dataTable;
	}
}
