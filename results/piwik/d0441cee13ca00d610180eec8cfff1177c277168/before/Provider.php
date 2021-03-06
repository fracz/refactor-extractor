<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html Gpl v3 or later
 * @version $Id$
 *
 * @package Piwik_Provider
 */

/**
 *
 * @package Piwik_Provider
 */
class Piwik_Provider extends Piwik_Plugin
{
	public function getInformation()
	{
		$info = array(
			'name' => 'Provider',
			'description' => 'Reports the Provider of the visitors.',
			'author' => 'Piwik',
			'homepage' => 'http://piwik.org/',
			'version' => '0.1',
			'TrackerPlugin' => true, // this plugin must be loaded during the stats logging
		);

		return $info;
	}

	function getListHooksRegistered()
	{
		$hooks = array(
			'ArchiveProcessing_Day.compute' => 'archiveDay',
			'ArchiveProcessing_Period.compute' => 'archivePeriod',
			'Tracker.newVisitorInformation' => 'logProviderInfo',
			'WidgetsList.add' => 'addWidget',
			'Menu.add' => 'addMenu',
		);
		return $hooks;
	}

	function install()
	{
		// add column hostname / hostname ext in the visit table
		$query = "ALTER IGNORE TABLE `".Piwik::prefixTable('log_visit')."` ADD `location_provider` VARCHAR( 100 ) NULL";

		// if the column already exist do not throw error. Could be installed twice...
		try {
			Zend_Registry::get('db')->query($query);
		}
		catch(Exception $e){}
	}

	function uninstall()
	{
		// add column hostname / hostname ext in the visit table
		$query = "ALTER TABLE `".Piwik::prefixTable('log_visit')."` DROP `location_provider`";
		Zend_Registry::get('db')->query($query);
	}

	function addWidget()
	{
		Piwik_AddWidget( 'Provider', 'getProvider', Piwik_Translate('Provider_WidgetProviders'));
	}

	function addMenu()
	{
		Piwik_RenameMenuEntry(	'General_Visitors', 'UserCountry_SubmenuLocations',
								'General_Visitors', 'Provider_SubmenuLocationsProvider');
	}

	function postLoad()
	{
		Piwik_AddAction('template_headerUserCountry', array('Piwik_Provider','headerUserCountry'));
		Piwik_AddAction('template_footerUserCountry', array('Piwik_Provider','footerUserCountry'));
	}

	function archivePeriod( $notification )
	{
		$archiveProcessing = $notification->getNotificationObject();

		$dataTableToSum = array(
				'Provider_hostnameExt',
		);

		$archiveProcessing->archiveDataTable($dataTableToSum);
	}

	/**
	 * Archive the provider count
	 */
	function archiveDay($notification)
	{
		$archiveProcessing = $notification->getNotificationObject();

		$recordName = 'Provider_hostnameExt';
		$labelSQL = "location_provider";
		$tableProvider = $archiveProcessing->getDataTableInterestForLabel($labelSQL);
		$record = new Piwik_ArchiveProcessing_Record_BlobArray($recordName, $tableProvider->getSerialized());
	}

	/**
	 * Logs the provider in the log_visit table
	 */
	public function logProviderInfo($notification)
	{
		$visitorInfo =& $notification->getNotificationObject();

		$hostname = $this->getHost($visitorInfo['location_ip']);
		$hostnameExtension = $this->getHostnameExt($hostname);

		// add the value to save in the table log_visit
		$visitorInfo['location_provider'] = $hostnameExtension;
	}

	/**
	 * Returns the hostname extension (site.co.jp in fvae.VARG.ceaga.site.co.jp)
	 * given the full hostname looked up from the IP
	 *
	 * @param string $hostname
	 *
	 * @return string
	 */
	private function getHostnameExt($hostname)
	{
		$extToExclude = array(
			'com', 'net', 'org', 'co'
		);

		$off = strrpos($hostname, '.');
		$ext = substr($hostname, $off);

		if(empty($off) || is_numeric($ext) || strlen($hostname) < 5)
		{
			return 'Ip';
		}
		else
		{
			$e = explode('.', $hostname);
			$s = sizeof($e);

			// if extension not correct
			if(isset($e[$s-2]) && in_array($e[$s-2], $extToExclude))
			{
				return $e[$s-3].".".$e[$s-2].".".$e[$s-1];
			}
			else
			{
				return $e[$s-2].".".$e[$s-1];
			}
		}
	}

	/**
	 * Returns the hostname given the string IP in the format ip2long
	 * php.net/ip2long
	 *
	 * @param string $ip
	 *
	 * @return string hostname
	 */
	private function getHost($ip)
	{
		return trim(strtolower(@gethostbyaddr(long2ip($ip))));
	}

	public function headerUserCountry($notification)
	{
		$out =& $notification->getNotificationObject();
		$out = '<div id="leftcolumn">';
	}

	public function footerUserCountry($notification)
	{
		$out =& $notification->getNotificationObject();
		$out = '</div>
			<div id="rightcolumn">
			<h2>Providers</h2>';
		$out .= Piwik_FrontController::getInstance()->fetchDispatch('Provider','getProvider');
		$out .= '</div>';
	}
}