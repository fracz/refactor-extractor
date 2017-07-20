<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html Gpl v3 or later
 * @version $Id$
 *
 * @category Piwik_Plugins
 * @package Piwik_UsersManager
 */


/**
 *
 * @package Piwik_UsersManager
 */
class Piwik_UsersManager_Controller extends Piwik_Controller
{
	function index()
	{
		$view = Piwik_View::factory('UsersManager');

		$IdSitesAdmin = Piwik_SitesManager_API::getInstance()->getSitesIdWithAdminAccess();
		$idSiteSelected = 1;

		if(count($IdSitesAdmin) > 0)
		{
			$defaultWebsiteId = $IdSitesAdmin[0];
			$idSiteSelected = Piwik_Common::getRequestVar('idsite', $defaultWebsiteId);
		}

		if($idSiteSelected==='all')
		{
			$usersAccessByWebsite = array();
		}
		else
		{
			$usersAccessByWebsite = Piwik_UsersManager_API::getInstance()->getUsersAccessFromSite( $idSiteSelected );
		}

		// requires super user access
		$usersLogin = Piwik_UsersManager_API::getInstance()->getUsersLogin();

		// we dont want to display the user currently logged so that the user can't change his settings from admin to view...
		$currentlyLogged = Piwik::getCurrentUserLogin();
		foreach($usersLogin as $login)
		{
			if(!isset($usersAccessByWebsite[$login]))
			{
				$usersAccessByWebsite[$login] = 'noaccess';
			}
		}
		unset($usersAccessByWebsite[$currentlyLogged]);

		ksort($usersAccessByWebsite);

		$users = array();
		if(Zend_Registry::get('access')->isSuperUser())
		{
			$users = Piwik_UsersManager_API::getInstance()->getUsers();
		}

		$view->idSiteSelected = $idSiteSelected;
		$view->users = $users;
		$view->usersAccessByWebsite = $usersAccessByWebsite;
		$view->formUrl = Piwik_Url::getCurrentUrl();
		$view->websites = Piwik_SitesManager_API::getInstance()->getSitesWithAdminAccess();
		$this->setGeneralVariablesView($view);
		$view->menu = Piwik_GetAdminMenu();
		echo $view->render();
	}
}