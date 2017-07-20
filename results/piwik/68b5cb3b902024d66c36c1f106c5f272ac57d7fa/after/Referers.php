<?php

class Piwik_Referers extends Piwik_Plugin
{
	public function __construct()
	{
		parent::__construct();
	}

	public function setCategoryDelimiter($delimiter)
	{
		self::$actionCategoryDelimiter = $delimiter;
	}


	public function getInformation()
	{
		$info = array(
			'name' => 'Referers',
			'description' => 'Computes all reports about the referers',
			'author' => 'Piwik',
			'homepage' => 'http://piwik.org/',
			'version' => '0.1',
			'translationAvailable' => true,
		);

		return $info;
	}

	function install()
	{
	}

	function uninstall()
	{
	}

	function getListHooksRegistered()
	{
		$hooks = array(
			'ArchiveProcessing_Day.compute' => 'archiveDay',
			'ArchiveProcessing_Period.compute' => 'archiveMonth',
		);
		return $hooks;
	}


	function archiveMonth( $notification )
	{
		$this->archiveProcessing = $notification->getNotificationObject();

		$dataTableToSum = array(
				'Referers_type',
				'Referers_keywordBySearchEngine',
				'Referers_searchEngineByKeyword',
				'Referers_keywordByCampaign',
				'Referers_urlByWebsite',
				'Referers_urlByPartner',
		);

		$this->archiveProcessing->archiveDataTable($dataTableToSum);
	}


	/**
	 *
	 */
	public function archiveDay( $notification )
	{
		$archiveProcessing = $notification->getNotificationObject();
		$query = "SELECT 	referer_type,
							referer_name,
							referer_keyword,
							referer_url,
							count(distinct visitor_idcookie) as nb_uniq_visitors,
							count(*) as nb_visits,
							sum(visit_total_actions) as nb_actions,
							max(visit_total_actions) as max_actions,
							sum(visit_total_time) as sum_visit_length,
							sum(case visit_total_actions when 1 then 1 else 0 end) as bounce_count
				 	FROM ".$archiveProcessing->logTable."
				 	WHERE visit_server_date = ?
				 		AND idsite = ?
				 	GROUP BY referer_type, referer_name, referer_keyword";
		$query = $archiveProcessing->db->query($query, array( $archiveProcessing->strDateStart, $archiveProcessing->idsite ));

		$timer = new Piwik_Timer;


		$interestBySearchEngine =
			$interestByKeyword =
			$keywordBySearchEngine =
			$searchEngineByKeyword =
			$interestByWebsite[Piwik_Common::REFERER_TYPE_WEBSITE] =
			$interestByWebsite[Piwik_Common::REFERER_TYPE_PARTNER] =
			$urlByWebsite[Piwik_Common::REFERER_TYPE_WEBSITE] =
			$urlByWebsite[Piwik_Common::REFERER_TYPE_PARTNER] =
			$interestByNewsletter =
			$keywordByCampaign =
			$interestByCampaign =
			$interestByType = array();

		while($rowBefore = $query->fetch() )
		{
			$row = array(
				Piwik_Archive::INDEX_NB_UNIQ_VISITORS 	=> $rowBefore['nb_uniq_visitors'],
				Piwik_Archive::INDEX_NB_VISITS 			=> $rowBefore['nb_visits'],
				Piwik_Archive::INDEX_NB_ACTIONS 		=> $rowBefore['nb_actions'],
				Piwik_Archive::INDEX_MAX_ACTIONS 		=> $rowBefore['max_actions'],
				Piwik_Archive::INDEX_SUM_VISIT_LENGTH 	=> $rowBefore['sum_visit_length'],
				Piwik_Archive::INDEX_BOUNCE_COUNT 		=> $rowBefore['bounce_count'],
				'referer_type' 							=> $rowBefore['referer_type'],
				'referer_name' 							=> $rowBefore['referer_name'],
				'referer_keyword'						=> $rowBefore['referer_keyword'],
				'referer_url'							=> $rowBefore['referer_url'],
			);

			switch($row['referer_type'])
			{
				case Piwik_Common::REFERER_TYPE_SEARCH_ENGINE:

					if(!isset($interestBySearchEngine[$row['referer_name']])) $interestBySearchEngine[$row['referer_name']]= $archiveProcessing->getNewInterestRow();
					if(!isset($interestByKeyword[$row['referer_keyword']])) $interestByKeyword[$row['referer_keyword']]= $archiveProcessing->getNewInterestRow();
					if(!isset($keywordBySearchEngine[$row['referer_name']][$row['referer_keyword']])) $keywordBySearchEngine[$row['referer_name']][$row['referer_keyword']]= $archiveProcessing->getNewInterestRow();
					if(!isset($searchEngineByKeyword[$row['referer_keyword']][$row['referer_name']])) $searchEngineByKeyword[$row['referer_keyword']][$row['referer_name']]= $archiveProcessing->getNewInterestRow();

					$archiveProcessing->updateInterestStats( $row, $interestBySearchEngine[$row['referer_name']]);
					$archiveProcessing->updateInterestStats( $row, $interestByKeyword[$row['referer_keyword']]);
					$archiveProcessing->updateInterestStats( $row, $keywordBySearchEngine[$row['referer_name']][$row['referer_keyword']]);
					$archiveProcessing->updateInterestStats( $row, $searchEngineByKeyword[$row['referer_keyword']][$row['referer_name']]);
				break;

				case Piwik_Common::REFERER_TYPE_WEBSITE:
				case Piwik_Common::REFERER_TYPE_PARTNER:

					// for a website we remove the HOST from the url, to save some bytes in the DB
					// for partners URLs we keep the full URL as the partner's name can be an alias and
					// so is not necessarily the hostname of the URL...
					if($row['referer_type']==Piwik_Common::REFERER_TYPE_WEBSITE
						&& !empty($row['referer_url']))
					{
						$row['referer_url'] = Piwik_Common::getPathAndQueryFromUrl($row['referer_url']);
					}

					if(!isset($interestByWebsite[$row['referer_type']][$row['referer_name']])) $interestByWebsite[$row['referer_type']][$row['referer_name']]= $archiveProcessing->getNewInterestRow();
					$archiveProcessing->updateInterestStats( $row, $interestByWebsite[$row['referer_type']][$row['referer_name']]);

					if(!isset($urlByWebsite[$row['referer_type']][$row['referer_name']][$row['referer_url']])) $urlByWebsite[$row['referer_type']][$row['referer_name']][$row['referer_url']]= $archiveProcessing->getNewInterestRow();
					$archiveProcessing->updateInterestStats( $row, $urlByWebsite[$row['referer_type']][$row['referer_name']][$row['referer_url']]);

				break;

				case Piwik_Common::REFERER_TYPE_NEWSLETTER:
					if(!isset($interestByNewsletter[$row['referer_name']])) $interestByNewsletter[$row['referer_name']]= $archiveProcessing->getNewInterestRow();
					$archiveProcessing->updateInterestStats( $row, $interestByNewsletter[$row['referer_name']]);

				break;

				case Piwik_Common::REFERER_TYPE_CAMPAIGN:
					if(!empty($row['referer_keyword']))
					{
						if(!isset($keywordByCampaign[$row['referer_name']][$row['referer_keyword']])) $keywordByCampaign[$row['referer_name']][$row['referer_keyword']]= $archiveProcessing->getNewInterestRow();
						$archiveProcessing->updateInterestStats( $row, $keywordByCampaign[$row['referer_name']][$row['referer_keyword']]);
					}
					if(!isset($interestByCampaign[$row['referer_name']])) $interestByCampaign[$row['referer_name']]= $archiveProcessing->getNewInterestRow();
					$archiveProcessing->updateInterestStats( $row, $interestByCampaign[$row['referer_name']]);
				break;
			}

			if(!isset($interestByType[$row['referer_type']] )) $interestByType[$row['referer_type']] = $archiveProcessing->getNewInterestRow();
			$archiveProcessing->updateInterestStats($row, $interestByType[$row['referer_type']]);
		}
//		echo "after loop = ". $timer;

//		Piwik::log("By search engine:");
//		Piwik::log($interestBySearchEngine);
//		Piwik::log("By keyword:");
//		Piwik::log($interestByKeyword);
//		Piwik::log("Kwd by search engine:");
//		Piwik::log($keywordBySearchEngine);
//		Piwik::log("Search engine by keyword:");
//		Piwik::log($searchEngineByKeyword);
//

//		Piwik::log("By campaign:");
//		Piwik::log($interestByCampaign);
//		Piwik::log("Kwd by campaign:");
//		Piwik::log($keywordByCampaign);


//		Piwik::log("By referer type:");
//		Piwik::log($interestByType);

//		Piwik::log("By website:");
//		Piwik::log($interestByWebsite[Piwik_Common::REFERER_TYPE_WEBSITE]);
//		Piwik::log("Urls by website:");
//		Piwik::log($urlByWebsite[Piwik_Common::REFERER_TYPE_WEBSITE]);
//		Piwik::log("By partner website:");
//		Piwik::log($interestByWebsite[Piwik_Common::REFERER_TYPE_PARTNER]);
//		Piwik::log("Urls by partner website:");
//		Piwik::log($urlByWebsite[Piwik_Common::REFERER_TYPE_PARTNER]);

		Piwik::printMemoryUsage("Middle of ".get_class($this)." ");

		$data = $archiveProcessing->getDataTableSerialized($interestByType);
		$record = new Piwik_ArchiveProcessing_Record_Blob_Array('Referers_type', $data);

		$data = $archiveProcessing->getDataTablesSerialized($keywordBySearchEngine, $interestBySearchEngine);
		$record = new Piwik_ArchiveProcessing_Record_Blob_Array('Referers_keywordBySearchEngine', $data);

//		var_export($data);

		$data = $archiveProcessing->getDataTablesSerialized($searchEngineByKeyword, $interestByKeyword);
		$record = new Piwik_ArchiveProcessing_Record_Blob_Array('Referers_searchEngineByKeyword', $data);

		$data = $archiveProcessing->getDataTablesSerialized($keywordByCampaign, $interestByCampaign);
		$record = new Piwik_ArchiveProcessing_Record_Blob_Array('Referers_keywordByCampaign', $data);

		$data = $archiveProcessing->getDataTablesSerialized($urlByWebsite[Piwik_Common::REFERER_TYPE_WEBSITE], $interestByWebsite[Piwik_Common::REFERER_TYPE_WEBSITE]);
		$record = new Piwik_ArchiveProcessing_Record_Blob_Array('Referers_urlByWebsite', $data);

		$data = $archiveProcessing->getDataTablesSerialized($urlByWebsite[Piwik_Common::REFERER_TYPE_PARTNER], $interestByWebsite[Piwik_Common::REFERER_TYPE_PARTNER]);
		$record = new Piwik_ArchiveProcessing_Record_Blob_Array('Referers_urlByPartner', $data);

		Piwik::printMemoryUsage("End of ".get_class($this)." ");
//		echo "after serialization = ". $timer;
	}
}