<?php
function Piwik_GetAdminMenu()
{
	return Piwik_AdminMenu::getInstance()->get();
}

function Piwik_AddAdminMenu( $adminMenuName, $url )
{
	return Piwik_AdminMenu::getInstance()->add($adminMenuName, $url);
}

function Piwik_RenameAdminMenuEntry($adminMenuOriginal, $adminMenuRenamed)
{
	Piwik_AdminMenu::getInstance()->rename($adminMenuOriginal, $adminMenuRenamed);
}

class Piwik_AdminMenu
{
	private $adminMenu = null;
	static private $instance = null;

	/**
	 * @return Piwik_AdminMenu
	 */
	static public function getInstance()
	{
		if (self::$instance == null)
		{
			$c = __CLASS__;
			self::$instance = new $c();
		}
		return self::$instance;
	}

	/*
	 * @return array
	 */
	public function get()
	{
		if(!is_null($this->adminMenu))
		{
			return;
		}

		Piwik_PostEvent('AdminMenu.add');

		foreach($this->adminMenu as $key => &$element)
		{
			if(is_null($element))
			{
				unset($this->adminMenu[$key]);
			}
		}
		return $this->adminMenu;
	}

	/*
	 * @return void
	 */
	public function add($adminMenuName, $url)
	{
		if(!isset($this->adminMenu[$adminMenuName]))
		{
			$this->adminMenu[$adminMenuName] = $url;
		}
	}

	/*
	 * @return void
	 */
	public function rename($adminMenuOriginal, $adminMenuRenamed)
	{
		$save = $this->adminMenu[$adminMenuOriginal];
		unset($this->adminMenu[$adminMenuOriginal]);
		$this->adminMenu[$adminMenuRenamed] = $save;
	}
}