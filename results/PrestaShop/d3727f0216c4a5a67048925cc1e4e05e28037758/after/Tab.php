<?php
/*
* 2007-2011 PrestaShop
*
* NOTICE OF LICENSE
*
* This source file is subject to the Open Software License (OSL 3.0)
* that is bundled with this package in the file LICENSE.txt.
* It is also available through the world-wide-web at this URL:
* http://opensource.org/licenses/osl-3.0.php
* If you did not receive a copy of the license and are unable to
* obtain it through the world-wide-web, please send an email
* to license@prestashop.com so we can send you a copy immediately.
*
* DISCLAIMER
*
* Do not edit or add to this file if you wish to upgrade PrestaShop to newer
* versions in the future. If you wish to customize PrestaShop for your
* needs please refer to http://www.prestashop.com for more information.
*
*  @author PrestaShop SA <contact@prestashop.com>
*  @copyright  2007-2011 PrestaShop SA
*  @version  Release: $Revision: 6844 $
*  @license    http://opensource.org/licenses/osl-3.0.php  Open Software License (OSL 3.0)
*  International Registered Trademark & Property of PrestaShop SA
*/

class TabCore extends ObjectModel
{
	/** @var string Displayed name*/
	public $name;

	/** @var string Class and file name*/
	public $class_name;

	public $module;

	/** @var integer parent ID */
	public $id_parent;

	/** @var integer position */
	public $position;

	/**
	 * @see ObjectModel::$definition
	 */
	public static $definition = array(
		'table' => 'tab',
		'primary' => 'id_tab',
		'multilang' => true,
		'fields' => array(
			'id_parent' => 	array('type' => self::TYPE_INT, 'validate' => 'isInt'),
			'position' => 	array('type' => self::TYPE_INT, 'validate' => 'isUnsignedInt'),
			'module' => 	array('type' => self::TYPE_STRING, 'validate' => 'isTabName', 'size' => 64),
			'class_name' => array('type' => self::TYPE_STRING, 'required' => true, 'size' => 64),

			// Lang fields
			'name' => 		array('type' => self::TYPE_STRING, 'lang' => true, 'validate' => 'isGenericName', 'required' => true, 'size' => 32),
		),
	);

	protected static $_getIdFromClassName = null;

	/**
	 * additionnal treatments for Tab when creating new one :
	 * - generate a new position
	 * - add access for admin profile
	 *
	 * @param boolean $autodate
	 * @param boolean $nullValues
	 * @return int id_tab
	 */
	public function add($autodate = true, $null_values = false)
	{
		$this->position = self::getNewLastPosition($this->id_parent);
		if (parent::add($autodate, $null_values))
		{
			// refresh cache when adding new tab
			self::$_getIdFromClassName[$this->class_name] = $this->id;
			return self::initAccess($this->id);
		}
		return false;
	}

	/** When creating a new tab $id_tab, this add default rights to the table access
	 *
	 * @todo this should not be public static but protected
	 * @param int $id_tab
	 * @return boolean true if succeed
	 */
	public static function initAccess($id_tab, Context $context = null)
	{
		if (!$context)
			$context = Context::getContext();
	 	if (!$context->employee || !$context->employee->id_profile)
	 		return false;
	 	/* Profile selection */
	 	$profiles = Db::getInstance()->executeS('
	 		SELECT `id_profile`
	 		FROM '._DB_PREFIX_.'profile
	 		WHERE `id_profile` != 1
	 	');
	 	if (!$profiles || empty($profiles))
	 		return false;

	 	/* Query definition */
		// note : insert ignore should be avoided
	 	$query = 'INSERT IGNORE INTO `'._DB_PREFIX_.'access` (`id_profile`, `id_tab`, `view`, `add`, `edit`, `delete`) VALUES ';
		// default admin
		$query .= '(1, '.(int)$id_tab.', 1, 1, 1, 1),';

	 	foreach ($profiles as $profile)
	 	{
			// no cast needed for profile[id_profile], which cames from db
			// And we disable all profile but current one
	 	 	$rights = $profile['id_profile'] == $context->employee->id_profile ? 1 : 0;
			$query .= '('.$profile['id_profile'].', '.(int)$id_tab.', '.$rights.', '.$rights.', '.$rights.', '.$rights.'),';
	 	}
		$query = trim($query, ', ');
	 	return Db::getInstance()->execute($query);
	}

	public function delete()
	{
	 	if (Db::getInstance()->execute('DELETE FROM '._DB_PREFIX_.'access WHERE `id_tab` = '.(int)$this->id) && parent::delete())
			return $this->cleanPositions($this->id_parent);
		return false;
	}

	/**
	 * Get tab id
	 *
	 * @return integer tab id
	 */
	public static function getCurrentTabId()
	{
		return self::getIdFromClassName(Tools::getValue('tab'));
	}

	/**
	 * Get tab parent id
	 *
	 * @return integer tab parent id
	 */
	public static function getCurrentParentId()
	{
	 	if ($result = Db::getInstance(_PS_USE_SQL_SLAVE_)->getRow('
	 		SELECT `id_parent`
	 		FROM `'._DB_PREFIX_.'tab`
	 		WHERE LOWER(class_name) = \''.pSQL(Tools::strtolower(Tools::getValue('controller'))).'\''))
		 	return $result['id_parent'];
 		return -1;
	}

	/**
	 * Get tab
	 *
	 * @return array tab
	 */
	public static function getTab($id_lang, $id_tab)
	{
		/* Tabs selection */
		return Db::getInstance(_PS_USE_SQL_SLAVE_)->getRow('
			SELECT *
			FROM `'._DB_PREFIX_.'tab` t
			LEFT JOIN `'._DB_PREFIX_.'tab_lang` tl
				ON (t.`id_tab` = tl.`id_tab` AND tl.`id_lang` = '.(int)$id_lang.')
			WHERE t.`id_tab` = '.(int)$id_tab
		);
	}

	/**
	 * Get tabs
	 *
	 * @return array tabs
	 */
	static $_cache_tabs = array();
	public static function getTabs($id_lang, $id_parent = null)
	{
		if (!isset(self::$_cache_tabs[$id_lang]))
		{
			self::$_cache_tabs[$id_lang] = array();
			$result = Db::getInstance(_PS_USE_SQL_SLAVE_)->executeS('
				SELECT *
				FROM `'._DB_PREFIX_.'tab` t
				LEFT JOIN `'._DB_PREFIX_.'tab_lang` tl
					ON (t.`id_tab` = tl.`id_tab` AND tl.`id_lang` = '.(int)$id_lang.')
				ORDER BY t.`position` ASC
			');
			foreach ($result as $row)
			{
				if (!isset(self::$_cache_tabs[$id_lang][$row['id_parent']]))
					self::$_cache_tabs[$id_lang][$row['id_parent']] = array();
				self::$_cache_tabs[$id_lang][$row['id_parent']][] = $row;
			}
		}
		if ($id_parent === null)
		{
			$array_all = array();
			foreach (self::$_cache_tabs[$id_lang] as $array_parent)
				$array_all = array_merge($array_all, $array_parent);
			return $array_all;
		}
		return (isset(self::$_cache_tabs[$id_lang][$id_parent]) ? self::$_cache_tabs[$id_lang][$id_parent] : array());
	}

	/**
	 * Get tab id from name
	 *
	 * @param string class_name
	 * @return int id_tab
	 */
	public static function getIdFromClassName($class_name)
	{
		if (self::$_getIdFromClassName === null)
		{
			self::$_getIdFromClassName = array();
			$result = Db::getInstance(_PS_USE_SQL_SLAVE_)->executeS('SELECT id_tab, class_name FROM `'._DB_PREFIX_.'tab`');
			foreach ($result as $row)
				self::$_getIdFromClassName[$row['class_name']] = $row['id_tab'];
		}
		return (isset(self::$_getIdFromClassName[$class_name]) ? (int)self::$_getIdFromClassName[$class_name] : false);
	}

	public static function getNbTabs($id_parent = null)
	{
		return (int)Db::getInstance()->getValue('
			SELECT COUNT(*)
			FROM `'._DB_PREFIX_.'tab` t
			'.(!is_null($id_parent) ? 'WHERE t.`id_parent` = '.(int)$id_parent : '')
		);
	}

	/**
	 * return an available position in subtab for parent $id_parent
	 *
	 * @param mixed $id_parent
	 * @return int
	 */
	public static function getNewLastPosition($id_parent)
	{
		return (Db::getInstance()->getValue('
			SELECT IFNULL(MAX(position),0)+1
			FROM `'._DB_PREFIX_.'tab`
			WHERE `id_parent` = '.(int)$id_parent
		));
	}

	public function move($direction)
	{
		$nb_tabs = self::getNbTabs($this->id_parent);
		if ($direction != 'l' && $direction != 'r')
			return false;
		if ($nb_tabs <= 1)
			return false;
		if ($direction == 'l' && $this->position <= 1)
			return false;
		if ($direction == 'r' && $this->position >= $nb_tabs)
			return false;

		$new_position = ($direction == 'l') ? $this->position - 1 : $this->position + 1;
		Db::getInstance()->execute('
			UPDATE `'._DB_PREFIX_.'tab` t
			SET position = '.(int)$this->position.'
			WHERE id_parent = '.(int)$this->id_parent.'
				AND position = '.(int)$new_position
		);
		$this->position = $new_position;
		return $this->update();
	}

	public function cleanPositions($id_parent)
	{
		$result = Db::getInstance()->executeS('
			SELECT `id_tab`
			FROM `'._DB_PREFIX_.'tab`
			WHERE `id_parent` = '.(int)$id_parent.'
			ORDER BY `position`
		');
		$sizeof = count($result);
		for ($i = 0; $i < $sizeof; ++$i)
			Db::getInstance()->execute('
				UPDATE `'._DB_PREFIX_.'tab`
				SET `position` = '.($i + 1).'
				WHERE `id_tab` = '.(int)$result[$i]['id_tab']
			);
		return true;
	}

	public function updatePosition($way, $position)
	{
		if (!$res = Db::getInstance()->executeS('
			SELECT t.`id_tab`, t.`position`, t.`id_parent`
			FROM `'._DB_PREFIX_.'tab` t
			WHERE t.`id_parent` = '.(int)$this->id_parent.'
			ORDER BY t.`position` ASC'
		))
			return false;

		foreach ($res as $tab)
			if ((int)$tab['id_tab'] == (int)$this->id)
				$moved_tab = $tab;

		if (!isset($moved_tab) || !isset($position))
			return false;
		// < and > statements rather than BETWEEN operator
		// since BETWEEN is treated differently according to databases
		$result = (Db::getInstance()->execute('
			UPDATE `'._DB_PREFIX_.'tab`
			SET `position`= `position` '.($way ? '- 1' : '+ 1').'
			WHERE `position`
			'.($way
				? '> '.(int)$moved_tab['position'].' AND `position` <= '.(int)$position
				: '< '.(int)$moved_tab['position'].' AND `position` >= '.(int)$position).'
			AND `id_parent`='.(int)$moved_tab['id_parent'])
		&& Db::getInstance()->execute('
			UPDATE `'._DB_PREFIX_.'tab`
			SET `position` = '.(int)$position.'
			WHERE `id_parent` = '.(int)$moved_tab['id_parent'].'
			AND `id_tab`='.(int)$moved_tab['id_tab']));
		return $result;
	}

	public static function checkTabRights($id_tab)
	{
		static $tabAccesses = null;

		if ($tabAccesses === null)
			$tabAccesses = Profile::getProfileAccesses(Context::getContext()->employee->id_profile);

		if (isset($tabAccesses[(int)$id_tab]['view']))
			return ($tabAccesses[(int)$id_tab]['view'] === '1');
		return false;
}

	public static function recursiveTab($id_tab, $tabs)
	{
		$admin_tab = Tab::getTab((int)Context::getContext()->language->id, $id_tab);
		$tabs[] = $admin_tab;
		if ($admin_tab['id_parent'] > 0)
			$tabs = Tab::recursiveTab($admin_tab['id_parent'], $tabs);
		return $tabs;
	}
}