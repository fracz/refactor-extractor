<?php

class Piwik_LogStats_Visit
{
	protected $cookieLog = null;
	protected $visitorInfo = array();
	protected $userSettingsInformation = null;

	function __construct( $db )
	{
		$this->db = $db;

		$idsite = Piwik_Common::getRequestVar('idsite', 0, 'int');
		if($idsite <= 0)
		{
			throw new Exception("The 'idsite' in the request is invalide.");
		}

		$this->idsite = $idsite;
	}

	protected function getCurrentDate( $format = "Y-m-d")
	{
		return date($format, $this->getCurrentTimestamp() );
	}

	protected function getCurrentTimestamp()
	{
		return time();
	}

	protected function getDatetimeFromTimestamp($timestamp)
	{
		return date("Y-m-d H:i:s",$timestamp);
	}



	// test if the visitor is excluded because of
	// - IP
	// - cookie
	// - configuration option?
	private function isExcluded()
	{
		$excluded = 0;

		if($excluded)
		{
			printDebug("Visitor excluded.");
			return true;
		}

		return false;
	}

	private function getCookieName()
	{
		return Piwik_LogStats_Config::getInstance()->LogStats['cookie_name'] . $this->idsite;
	}


	/**
	 * This methods tries to see if the visitor has visited the website before.
	 *
	 * We have to split the visitor into one of the category
	 * - Known visitor
	 * - New visitor
	 *
	 * A known visitor is a visitor that has already visited the website in the current month.
	 * We define a known visitor using the algorithm:
	 *
	 * 1) Checking if a cookie contains
	 * 		// a unique id for the visitor
	 * 		- id_visitor
	 *
	 * 		// the timestamp of the last action in the most recent visit
	 * 		- timestamp_last_action
	 *
 	 *  	// the timestamp of the first action in the most recent visit
	 * 		- timestamp_first_action
	 *
	 * 		// the ID of the most recent visit (which could be in the past or the current visit)
	 * 		- id_visit
	 *
	 * 		// the ID of the most recent action
	 * 		- id_last_action
	 *
	 * 2) If the visitor doesn't have a cookie, we try to look for a similar visitor configuration.
	 * 	  We search for a visitor with the same plugins/OS/Browser/Resolution for today for this website.
	 */
	private function recognizeTheVisitor()
	{
		$this->visitorKnown = false;

		$this->cookieLog = new Piwik_LogStats_Cookie( $this->getCookieName() );
		/*
		 * Case the visitor has the piwik cookie.
		 * We make sure all the data that should saved in the cookie is available.
		 */

		if( false !== ($idVisitor = $this->cookieLog->get( Piwik_LogStats::COOKIE_INDEX_IDVISITOR )) )
		{
			$timestampLastAction = $this->cookieLog->get( Piwik_LogStats::COOKIE_INDEX_TIMESTAMP_LAST_ACTION );
			$timestampFirstAction = $this->cookieLog->get( Piwik_LogStats::COOKIE_INDEX_TIMESTAMP_FIRST_ACTION );
			$idVisit = $this->cookieLog->get( Piwik_LogStats::COOKIE_INDEX_ID_VISIT );
			$idLastAction = $this->cookieLog->get( Piwik_LogStats::COOKIE_INDEX_ID_LAST_ACTION );

			if(		$timestampLastAction !== false && is_numeric($timestampLastAction)
				&& 	$timestampFirstAction !== false && is_numeric($timestampFirstAction)
				&& 	$idVisit !== false && is_numeric($idVisit)
				&& 	$idLastAction !== false && is_numeric($idLastAction)
			)
			{
				$this->visitorInfo['visitor_idcookie'] = $idVisitor;
				$this->visitorInfo['visit_last_action_time'] = $timestampLastAction;
				$this->visitorInfo['visit_first_action_time'] = $timestampFirstAction;
				$this->visitorInfo['idvisit'] = $idVisit;
				$this->visitorInfo['visit_exit_idaction'] = $idLastAction;

				$this->visitorKnown = true;

				printDebug("The visitor is known because he has the piwik cookie (idcookie = {$this->visitorInfo['visitor_idcookie']}, idvisit = {$this->visitorInfo['idvisit']}, last action = ".date("r", $this->visitorInfo['visit_last_action_time']).") ");
			}
		}

		/*
		 * If the visitor doesn't have the piwik cookie, we look for a visitor that has exactly the same configuration
		 * and that visited the website today.
		 */
		if( !$this->visitorKnown )
		{
			$userInfo = $this->getUserSettingsInformation();
			$md5Config = $userInfo['config_md5config'];

			$visitRow = $this->db->fetch(
										" SELECT  	visitor_idcookie,
													UNIX_TIMESTAMP(visit_last_action_time) as visit_last_action_time,
													UNIX_TIMESTAMP(visit_first_action_time) as visit_first_action_time,
													idvisit,
													visit_exit_idaction
										FROM ".$this->db->prefixTable('log_visit').
										" WHERE visit_server_date = ?
											AND idsite = ?
											AND config_md5config = ?
										ORDER BY visit_last_action_time DESC
										LIMIT 1",
										array( $this->getCurrentDate(), $this->idsite, $md5Config));
			if($visitRow
				&& count($visitRow) > 0)
			{
				$this->visitorInfo['visitor_idcookie'] = $visitRow['visitor_idcookie'];
				$this->visitorInfo['visit_last_action_time'] = $visitRow['visit_last_action_time'];
				$this->visitorInfo['visit_first_action_time'] = $visitRow['visit_first_action_time'];
				$this->visitorInfo['idvisit'] = $visitRow['idvisit'];
				$this->visitorInfo['visit_exit_idaction'] = $visitRow['visit_exit_idaction'];

				$this->visitorKnown = true;

				printDebug("The visitor is known because of his userSettings+IP (idcookie = {$visitRow['visitor_idcookie']}, idvisit = {$this->visitorInfo['idvisit']}, last action = ".date("r", $this->visitorInfo['visit_last_action_time']).") ");
			}
		}
	}

	private function getUserSettingsInformation()
	{
		// we already called this method before, simply returns the result
		if(is_array($this->userSettingsInformation))
		{
			return $this->userSettingsInformation;
		}


		$plugin_Flash 			= Piwik_Common::getRequestVar( 'fla', 0, 'int');
		$plugin_Director 		= Piwik_Common::getRequestVar( 'dir', 0, 'int');
		$plugin_Quicktime		= Piwik_Common::getRequestVar( 'qt', 0, 'int');
		$plugin_RealPlayer 		= Piwik_Common::getRequestVar( 'realp', 0, 'int');
		$plugin_Pdf 			= Piwik_Common::getRequestVar( 'pdf', 0, 'int');
		$plugin_WindowsMedia 	= Piwik_Common::getRequestVar( 'wma', 0, 'int');
		$plugin_Java 			= Piwik_Common::getRequestVar( 'java', 0, 'int');
		$plugin_Cookie 			= Piwik_Common::getRequestVar( 'cookie', 0, 'int');

		$userAgent		= Piwik_Common::sanitizeInputValues(@$_SERVER['HTTP_USER_AGENT']);
		$aBrowserInfo	= Piwik_Common::getBrowserInfo($userAgent);
		$browserName	= $aBrowserInfo['name'];
		$browserVersion	= $aBrowserInfo['version'];

		$os				= Piwik_Common::getOs($userAgent);

		$resolution		= Piwik_Common::getRequestVar('res', 'unknown', 'string');
		$colorDepth		= Piwik_Common::getRequestVar('col', 32, 'numeric');


		$ip				= Piwik_Common::getIp();
		$ip 			= ip2long($ip);

		$browserLang	= Piwik_Common::sanitizeInputValues(@$_SERVER['HTTP_ACCEPT_LANGUAGE']);
		if(is_null($browserLang))
		{
			$browserLang = '';
		}


		$configurationHash = $this->getConfigHash(
												$os,
												$browserName,
												$browserVersion,
												$resolution,
												$colorDepth,
												$plugin_Flash,
												$plugin_Director,
												$plugin_RealPlayer,
												$plugin_Pdf,
												$plugin_WindowsMedia,
												$plugin_Java,
												$plugin_Cookie,
												$ip,
												$browserLang);

		$this->userSettingsInformation = array(
			'config_md5config' => $configurationHash,
			'config_os' 			=> $os,
			'config_browser_name' 	=> $browserName,
			'config_browser_version' => $browserVersion,
			'config_resolution' 	=> $resolution,
			'config_color_depth' 	=> $colorDepth,
			'config_pdf' 			=> $plugin_Pdf,
			'config_flash' 			=> $plugin_Flash,
			'config_java' 			=> $plugin_Java,
			'config_director' 		=> $plugin_Director,
			'config_quicktime' 		=> $plugin_Quicktime,
			'config_realplayer' 	=> $plugin_RealPlayer,
			'config_windowsmedia' 	=> $plugin_WindowsMedia,
			'config_cookie' 		=> $plugin_RealPlayer,
			'location_ip' 			=> $ip,
			'location_browser_lang' => $browserLang,
		);

		return $this->userSettingsInformation;
	}

	/**
	 * Returns true if the last action was done during the last 30 minutes
	 */
	private function isLastActionInTheSameVisit()
	{
		return $this->visitorInfo['visit_last_action_time']
					>= ($this->getCurrentTimestamp() - Piwik_LogStats::VISIT_STANDARD_LENGTH);
	}

	private function isVisitorKnown()
	{
		return $this->visitorKnown === true;
	}

	/**
	 * Once we have the visitor information, we have to define if the visit is a new or a known visit.
	 *
	 * 1) When the last action was done more than 30min ago,
	 * 	  or if the visitor is new, then this is a new visit.
	 *
	 * 2) If the last action is less than 30min ago, then the same visit is going on.
	 *	Because the visit goes on, we can get the time spent during the last action.
	 *
	 * NB:
	 *  - In the case of a new visit, then the time spent
	 *	during the last action of the previous visit is unknown.
	 *
	 *	- In the case of a new visit but with a known visitor,
	 *	we can set the 'returning visitor' flag.
	 *
	 */

	/**
	 * In all the cases we set a cookie to the visitor with the new information.
	 */
	public function handle()
	{
		if(!$this->isExcluded())
		{
			$this->recognizeTheVisitor();

			// known visitor
			if($this->isVisitorKnown())
			{
				// the same visit is going on
				if($this->isLastActionInTheSameVisit())
				{
					$this->handleKnownVisit();
				}
				// new visit
				else
				{
					$this->handleNewVisit();
				}
			}
			// new visitor => new visit
			else
			{
				$this->handleNewVisit();
			}

			// we update the cookie with the new visit information
			$this->updateCookie();

		}
	}

	private function updateCookie()
	{
		printDebug("We manage the cookie...");

		// idcookie has been generated in handleNewVisit or we simply propagate the old value
		$this->cookieLog->set( 	Piwik_LogStats::COOKIE_INDEX_IDVISITOR,
								$this->visitorInfo['visitor_idcookie'] );

		// the last action timestamp is the current timestamp
		$this->cookieLog->set( 	Piwik_LogStats::COOKIE_INDEX_TIMESTAMP_LAST_ACTION,
								$this->visitorInfo['visit_last_action_time'] );

		// the first action timestamp is the timestamp of the first action of the current visit
		$this->cookieLog->set( 	Piwik_LogStats::COOKIE_INDEX_TIMESTAMP_FIRST_ACTION,
								$this->visitorInfo['visit_first_action_time'] );

		// the idvisit has been generated by mysql in handleNewVisit or simply propagated here
		$this->cookieLog->set( 	Piwik_LogStats::COOKIE_INDEX_ID_VISIT,
								$this->visitorInfo['idvisit'] );

		// the last action ID is the current exit idaction
		$this->cookieLog->set( 	Piwik_LogStats::COOKIE_INDEX_ID_LAST_ACTION,
								$this->visitorInfo['visit_exit_idaction'] );

		$this->cookieLog->save();
	}


	/**
	 * In the case of a known visit, we have to do the following actions:
	 *
	 * 1) Insert the new action
	 *
	 * 2) Update the visit information
	 */
	private function handleKnownVisit()
	{
		printDebug("Visit known.");

		/**
		 * Init the action
		 */
		$action = new Piwik_LogStats_Action( $this->db );

		$actionId = $action->getActionId();

		printDebug("idAction = $actionId");

		$serverTime 	= $this->getCurrentTimestamp();
		$datetimeServer = $this->getDatetimeFromTimestamp($serverTime);

		$this->db->query("UPDATE ". $this->db->prefixTable('log_visit')."
							SET visit_last_action_time = ?,
								visit_exit_idaction = ?,
								visit_total_actions = visit_total_actions + 1,
								visit_total_time = UNIX_TIMESTAMP(visit_last_action_time) - UNIX_TIMESTAMP(visit_first_action_time)
							WHERE idvisit = ?
							LIMIT 1",
							array( 	$datetimeServer,
									$actionId,
									$this->visitorInfo['idvisit'] )
				);
		/**
		 * Save the action
		 */
		$timespentLastAction = $serverTime - $this->visitorInfo['visit_last_action_time'];

		$action->record( 	$this->visitorInfo['idvisit'],
							$this->visitorInfo['visit_exit_idaction'],
							$timespentLastAction
			);


		/**
		 * Cookie fields to be updated
		 */
		$this->visitorInfo['visit_last_action_time'] = $serverTime;
		$this->visitorInfo['visit_exit_idaction'] = $actionId;


	}

	/**
	 * In the case of a new visit, we have to do the following actions:
	 *
	 * 1) Insert the new action
	 *
	 * 2) Insert the visit information
	 */
	private function handleNewVisit()
	{
		printDebug("New Visit.");

		/**
		 * Get the variables from the REQUEST
		 */

		// Configuration settings
		$userInfo = $this->getUserSettingsInformation();

		// General information
		$localTime				= Piwik_Common::getRequestVar( 'h', $this->getCurrentDate("H"), 'numeric')
							.':'. Piwik_Common::getRequestVar( 'm', $this->getCurrentDate("i"), 'numeric')
							.':'. Piwik_Common::getRequestVar( 's', $this->getCurrentDate("s"), 'numeric');
		$serverDate 	= $this->getCurrentDate();
		$serverTime 	= $this->getCurrentTimestamp();

		if($this->isVisitorKnown())
		{
			$idcookie = $this->visitorInfo['visitor_idcookie'];
			$returningVisitor = 1;
		}
		else
		{
			$idcookie = $this->getVisitorUniqueId();
			$returningVisitor = 0;
		}

		$defaultTimeOnePageVisit = Piwik_LogStats_Config::getInstance()->LogStats['default_time_one_page_visit'];

		// Location information
		$country 		= Piwik_Common::getCountry($userInfo['location_browser_lang']);
		$continent		= Piwik_Common::getContinent( $country );

		//Referer information
		$refererInfo = $this->getRefererInformation();

		/**
		 * Init the action
		 */
		$action = new Piwik_LogStats_Action( $this->db );

		$actionId = $action->getActionId();

		printDebug("idAction = $actionId");


		/**
		 * Save the visitor
		 */
		$informationToSave = array(
			//'idvisit' => ,
			'idsite' 				=> $this->idsite,
			'visitor_localtime' 	=> $localTime,
			'visitor_idcookie' 		=> $idcookie,
			'visitor_returning' 	=> $returningVisitor,
			'visit_first_action_time' => $this->getDatetimeFromTimestamp($serverTime),
			'visit_last_action_time' =>  $this->getDatetimeFromTimestamp($serverTime),
			'visit_server_date' 	=> $serverDate,
			'visit_entry_idaction' 	=> $actionId,
			'visit_exit_idaction' 	=> $actionId,
			'visit_total_actions' 	=> 1,
			'visit_total_time' 		=> $defaultTimeOnePageVisit,
			'referer_type' 			=> $refererInfo['referer_type'],
			'referer_name' 			=> $refererInfo['referer_name'],
			'referer_url' 			=> $refererInfo['referer_url'],
			'referer_keyword' 		=> $refererInfo['referer_keyword'],
			'config_md5config' 		=> $userInfo['config_md5config'],
			'config_os' 			=> $userInfo['config_os'],
			'config_browser_name' 	=> $userInfo['config_browser_name'],
			'config_browser_version' => $userInfo['config_browser_version'],
			'config_resolution' 	=> $userInfo['config_resolution'],
			'config_color_depth' 	=> $userInfo['config_color_depth'],
			'config_pdf' 			=> $userInfo['config_pdf'],
			'config_flash' 			=> $userInfo['config_flash'],
			'config_java' 			=> $userInfo['config_java'],
			'config_director' 		=> $userInfo['config_director'],
			'config_quicktime' 		=> $userInfo['config_quicktime'],
			'config_realplayer' 	=> $userInfo['config_realplayer'],
			'config_windowsmedia' 	=> $userInfo['config_windowsmedia'],
			'config_cookie' 		=> $userInfo['config_cookie'],
			'location_ip' 			=> $userInfo['location_ip'],
			'location_browser_lang' => $userInfo['location_browser_lang'],
			'location_country' 		=> $country,
			'location_continent' 	=> $continent,
		);


		$fields = implode(", ", array_keys($informationToSave));
		$values = substr(str_repeat( "?,",count($informationToSave)),0,-1);

		$this->db->query( "INSERT INTO ".$this->db->prefixTable('log_visit').
						" ($fields) VALUES ($values)", array_values($informationToSave));

		$idVisit = $this->db->lastInsertId();

		// Update the visitor information attribute with this information array
		$this->visitorInfo = $informationToSave;
		$this->visitorInfo['idvisit'] = $idVisit;

		// we have to save timestamp in the object properties, whereas mysql eats some other datetime format
		$this->visitorInfo['visit_first_action_time'] = $serverTime;
		$this->visitorInfo['visit_last_action_time'] = $serverTime;

		/**
		 * Save the action
		 */
		$action->record( $idVisit, 0, 0 );

	}

	/**
	 * Returns an array containing the following information:
	 * - referer_type
	 *		- direct			-- absence of referer URL OR referer URL has the same host
	 *		- site				-- based on the referer URL
	 *		- search_engine		-- based on the referer URL
	 *		- campaign			-- based on campaign URL parameter
	 *		- newsletter		-- based on newsletter URL parameter
	 *		- partner			-- based on partner URL parameter
	 *
	 * - referer_name
	 * 		- ()
	 * 		- piwik.net			-- site host name
	 * 		- google.fr			-- search engine host name
	 * 		- adwords-search	-- campaign name
	 * 		- beta-release		-- newsletter name
	 * 		- my-nice-partner	-- partner name
	 *
	 * - referer_keyword
	 * 		- ()
	 * 		- ()
	 * 		- my keyword
	 * 		- my paid keyword
	 * 		- ()
	 * 		- ()
	 *
	 * - referer_url : the same for all the referer types
	 *
	 */
	private function getRefererInformation()
	{
		// bool that says if the referer detection is done
		$refererAnalyzed = false;
		$typeRefererAnalyzed = Piwik_Common::REFERER_TYPE_DIRECT_ENTRY;
		$nameRefererAnalyzed = '';
		$keywordRefererAnalyzed = '';

		$refererUrl	= Piwik_Common::getRequestVar( 'urlref', '', 'string');
		$currentUrl	= Piwik_Common::getRequestVar( 'url', '', 'string');

		$refererUrlParse = @parse_url($refererUrl);
		$currentUrlParse = @parse_url($currentUrl);

		if(isset($refererUrlParse['host'])
			&& !empty($refererUrlParse['host']))
		{

			$refererHost = $refererUrlParse['host'];
			$refererSH = $refererUrlParse['scheme'].'://'.$refererUrlParse['host'];

			/*
			 * Search engine detection
			 */
			if( !$refererAnalyzed )
			{
				/*
				 * A referer is a search engine if the URL's host is in the SearchEngines array
				 * and if we found the keyword in the URL.
				 *
				 * For example if someone comes from http://www.google.com/partners.html this will not
				 * be counted as a search engines, but as a website referer from google.com (because the
				 * keyword couldn't be found in the URL)
				 */
				require_once PIWIK_DATAFILES_INCLUDE_PATH . "/SearchEngines.php";

				if(array_key_exists($refererHost, $GLOBALS['Piwik_SearchEngines']))
				{
					// which search engine ?
					$searchEngineName = $GLOBALS['Piwik_SearchEngines'][$refererHost][0];
					$variableName = $GLOBALS['Piwik_SearchEngines'][$refererHost][1];

					// if there is a query, there may be a keyword...
					if(isset($refererUrlParse['query']))
					{
						$query = $refererUrlParse['query'];

						//TODO: change the search engine file and use REGEXP; performance downside?
						//TODO: port the phpmyvisites google-images hack here

						// search for keywords now &vname=keyword
						$key = strtolower(Piwik_Common::getParameterFromQueryString($query, $variableName));

						//TODO test the search engine non-utf8 support
						// for search engines that don't use utf-8
						if((function_exists('iconv'))
							&& (isset($GLOBALS['Piwik_SearchEngines'][$refererHost][2])))
						{
							$charset = trim($GLOBALS['searchEngines'][$refererHost][2]);

							if(!empty($charset))
							{
								$key = htmlspecialchars(
											@iconv(	$charset,
													'utf-8//TRANSLIT',
													htmlspecialchars_decode($key, Piwik_Common::HTML_ENCODING_QUOTE_STYLE))
											, Piwik_Common::HTML_ENCODING_QUOTE_STYLE);
							}
						}


						if(!empty($key))
						{
							$refererAnalyzed = true;
							$typeRefererAnalyzed = Piwik_Common::REFERER_TYPE_SEARCH_ENGINE;
							$nameRefererAnalyzed = $searchEngineName;
							$keywordRefererAnalyzed = $key;
						}
					}
				}
			}

			/*
			 * Newsletter analysis
			 */
			if( !$refererAnalyzed )
			{
				if(isset($currentUrlParse['query']))
				{
					$newsletterVariableName = Piwik_LogStats_Config::getInstance()->LogStats['newsletter_var_name'];
					$newsletterVar = Piwik_Common::getParameterFromQueryString( $currentUrlParse['query'], $newsletterVariableName);

					if($newsletterVar !== false && !empty($newsletterVar))
					{
						$refererAnalyzed = true;
						$typeRefererAnalyzed = Piwik_Common::REFERER_TYPE_NEWSLETTER;
						$nameRefererAnalyzed = $newsletterVar;
					}
				}
			}

			/*
			 * Partner analysis
			 */
			 //TODO handle partner from a list of known partner URLs
			if( !$refererAnalyzed )
			{
				if(isset($currentUrlParse['query']))
				{
					$partnerVariableName = Piwik_LogStats_Config::getInstance()->LogStats['partner_var_name'];
					$partnerVar = Piwik_Common::getParameterFromQueryString($currentUrlParse['query'], $partnerVariableName);

					if($partnerVar !== false && !empty($partnerVar))
					{
						$refererAnalyzed = true;
						$typeRefererAnalyzed = Piwik_Common::REFERER_TYPE_PARTNER;
						$nameRefererAnalyzed = $partnerVar;
					}
				}
			}

			/*
			 * Campaign analysis
			 */
			if( !$refererAnalyzed )
			{
				if(isset($currentUrlParse['query']))
				{
					$campaignVariableName = Piwik_LogStats_Config::getInstance()->LogStats['campaign_var_name'];
					$campaignName = Piwik_Common::getParameterFromQueryString($currentUrlParse['query'], $campaignVariableName);

					if( $campaignName !== false && !empty($campaignName))
					{
						$campaignKeywordVariableName = Piwik_LogStats_Config::getInstance()->LogStats['campaign_keyword_var_name'];
						$campaignKeyword = Piwik_Common::getParameterFromQueryString($currentUrlParse['query'], $campaignKeywordVariableName);

						$refererAnalyzed = true;
						$typeRefererAnalyzed = Piwik_Common::REFERER_TYPE_CAMPAIGN;
						$nameRefererAnalyzed = $campaignName;

						if(!empty($campaignKeyword))
						{
							$keywordRefererAnalyzed = $campaignKeyword;
						}
					}
				}
			}

			/*
			 * Direct entry (referer host is similar to current host)
			 * And we have previously tried to detect the newsletter/partner/campaign variables in the URL
			 * so it can only be a direct access
			 */
			if( !$refererAnalyzed )
			{
				$currentUrlParse = @parse_url($currentUrl);

				if(isset($currentUrlParse['host']))
				{
					$currentHost = $currentUrlParse['host'];
					$currentSH = $currentUrlParse['scheme'].'://'.$currentUrlParse['host'];

					if($currentHost == $refererHost)
					{
						$refererAnalyzed = true;
						$typeRefererAnalyzed = Piwik_Common::REFERER_TYPE_DIRECT_ENTRY;
					}
				}

			}

			/*
			 * Normal website referer
			 */
			if( !$refererAnalyzed )
			{
				$refererAnalyzed = true;
				$typeRefererAnalyzed = Piwik_Common::REFERER_TYPE_WEBSITE;
				$nameRefererAnalyzed = $refererHost;
			}
		}


		$refererInformation = array(
			'referer_type' 		=> $typeRefererAnalyzed,
			'referer_name' 		=> $nameRefererAnalyzed,
			'referer_keyword' 	=> $keywordRefererAnalyzed,
			'referer_url' 		=> $refererUrl,
		);

		return $refererInformation;
	}

	private function getConfigHash( $os, $browserName, $browserVersion, $resolution, $colorDepth, $plugin_Flash, $plugin_Director, $plugin_RealPlayer, $plugin_Pdf, $plugin_WindowsMedia, $plugin_Java, $plugin_Cookie, $ip, $browserLang)
	{
		return md5( $os . $browserName . $browserVersion . $resolution . $colorDepth . $plugin_Flash . $plugin_Director . $plugin_RealPlayer . $plugin_Pdf . $plugin_WindowsMedia . $plugin_Java . $plugin_Cookie . $ip . $browserLang );
	}

	private function getVisitorUniqueId()
	{
		if($this->isVisitorKnown())
		{
			return -1;
		}
		else
		{
			return Piwik_Common::generateUniqId();
		}
	}

}
?>