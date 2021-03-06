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
*  @version  Release: $Revision$
*  @license    http://opensource.org/licenses/osl-3.0.php  Open Software License (OSL 3.0)
*  International Registered Trademark & Property of PrestaShop SA
*/

class AdminControllerCore extends Controller
{
	public $path;

	public static $currentIndex;
	public $content;
	public $warnings = array();
	public $informations = array();
	public $confirmations = array();
	public $shopShareDatas = false;

	public $_languages = array();
	public $default_form_language;
	public $allow_employee_form_lang;

	public $layout = 'layout.tpl';

	public $meta_title = 'Administration panel';

	public $template = 'content.tpl';

	/** @var string Associated table name */
	public $table;

	/** @var string Object identifier inside the associated table */
	protected $identifier = false;

	/** @var string Tab name */
	public $className;

	/** @var array tabAccess */
	public $tabAccess;

	/** @var integer Tab id */
	public $id = -1;

	/** @var array noTabLink array of admintab names witch have no content */
	public $noTabLink = array('AdminCatalog', 'AdminTools', 'AdminStock', 'AdminAccounting');

	/** @var string Security token */
	public $token;

	/** @var string shop | group_shop */
	public $shopLinkType;

	/** @var string Default ORDER BY clause when $_orderBy is not defined */
	protected $_defaultOrderBy = false;

	public $tpl_form_vars = array();
	public $tpl_list_vars = array();
	public $tpl_delete_link_vars = array();
	public $tpl_option_vars = array();
	public $tpl_view_vars = array();

	public $base_tpl_view = null;
	public $base_tpl_form = null;

	/** @var bool if you want more fieldsets in the form */
	public $multiple_fieldsets = false;

	public $fields_value = false;

	/** @var array Errors displayed after post processing */
	public $_errors = array();

	/** @var define if the header of the list contains filter and sorting links or not */
	protected $list_simple_header;

	/** @var array list to be generated */
	protected $fieldsDisplay;

	/** @var array edit form to be generated */
	protected $fields_form;

	/** @var array list of option forms to be generated */
	protected $options;

	protected $shopLink;

	/** @var array Cache for query results */
	protected $_list = array();

	/** @var define if the header of the list contains filter and sorting links or not */
	protected $toolbar_title;

	/** @var array list of toolbar buttons */
	protected $toolbar_btn = null;

	/** @var array list of toolbar buttons */
	protected $toolbar_fix = true;

	/** @var boolean set to false to hide toolbar and page title */
	protected $show_toolbar = true;

	/** @var boolean set to true to show toolbar and page title for options */
	protected $show_toolbar_options = false;

	/** @var integer Number of results in list */
	protected $_listTotal = 0;

	/** @var boolean Automatically join language table if true */
	public $lang = false;

	/** @var array WHERE clause determined by filter fields */
	protected $_filter;

	/** @var array Temporary SQL table WHERE clause determinated by filter fields */
	protected $_tmpTableFilter = '';

	/** @var array Number of results in list per page (used in select field) */
	protected $_pagination = array(20, 50, 100, 300);

	/** @var string ORDER BY clause determined by field/arrows in list header */
	protected $_orderBy;

	/** @var string Order way (ASC, DESC) determined by arrows in list header */
	protected $_orderWay;

	/** @var array list of available actions for each list row - default actions are view, edit, delete, duplicate */
	protected $actions_available = array('view', 'edit', 'delete', 'duplicate');

	/** @var array list of required actions for each list row */
	protected $actions = array();

	/** @var array list of row ids associated with a given action for witch this action have to not be available */
	protected $list_skip_actions = array();

	/** @var bool boolean List content lines are clickable if true */
	protected $list_no_link = false;

	/** @var array $cache_lang cache for traduction */
	public static $cache_lang = array();

	/**
	 * @var array actions to execute on multiple selections
	 * Usage:
	 * array(
	 * 		'actionName' => array(
	 * 			'text' => $this->l('Message displayed on the submit button (mandatory)'),
	 * 			'confirm' => $this->l('If set, this confirmation message will pop-up (optional)')),
	 * 		'anotherAction' => array(...)
	 * );
	 *
	 * If your action is named 'actionName', you need to have a method named bulkactionName() that will be executed when the button is clicked.
	 */
	protected $bulk_actions;

	/**
	 * @var array ids of the rows selected
	 */
	protected $boxes;

	/** @var string Add fields into data query to display list */
	protected $_select;

	/** @var string Join tables into data query to display list */
	protected $_join;

	/** @var string Add conditions into data query to display list */
	protected $_where;

	/** @var string Group rows into data query to display list */
	protected $_group;

	/** @var string Having rows into data query to display list */
	protected $_having;

	protected $is_cms = false;

	protected $is_dnd_identifier = false;

	protected $identifiersDnd = array(
		'id_product' => 'id_product',
		'id_category' => 'id_category_to_move',
		'id_cms_category' => 'id_cms_category_to_move',
		'id_cms' => 'id_cms',
		'id_attribute' => 'id_attribute'
	);

	/** @var boolean Table records are not deleted but marked as deleted if set to true */
	protected $deleted = false;
	/**
	 * @var bool is a list filter set
	 */
	protected $filter;
	protected $noLink;
	protected $specificConfirmDelete;
	protected $colorOnBackground;
	/** @string Action to perform : 'edit', 'view', 'add', ... */
	protected $action;
	protected $display;
	protected $_includeContainer = true;

	public $tpl_folder;

	/** @var bool Redirect or not ater a creation */
	protected $_redirect = true;

	/** @var array Name and directory where class image are located */
	public $fieldImageSettings = array();

	/** @var string Image type */
	public $imageType = 'jpg';

	/** @var instanciation of the class associated with the AdminController */
	protected $object;

	/** @var current object ID */
	protected $id_object;

	public function __construct()
	{
		$controller = get_class($this);
		// temporary fix for Token retrocompatibility
		// This has to be done when url is built instead of here)
		if (strpos($controller, 'Controller'))
			$controller = substr($controller, 0, -10);

		parent::__construct();

		$this->id = Tab::getIdFromClassName($controller);
		$this->token = Tools::getAdminToken($controller.(int)$this->id.(int)$this->context->employee->id);

		$this->_conf = array(
			1 => $this->l('Deletion successful'), 2 => $this->l('Selection successfully deleted'),
			3 => $this->l('Creation successful'), 4 => $this->l('Update successful'),
			5 => $this->l('Status update successful'), 6 => $this->l('Settings update successful'),
			7 => $this->l('Image successfully deleted'), 8 => $this->l('Module downloaded successfully'),
			9 => $this->l('Thumbnails successfully regenerated'), 10 => $this->l('Message sent to the customer'),
			11 => $this->l('Comment added'), 12 => $this->l('Module installed successfully'),
			13 => $this->l('Module uninstalled successfully'), 14 => $this->l('Language successfully copied'),
			15 => $this->l('Translations successfully added'), 16 => $this->l('Module transplanted successfully to hook'),
			17 => $this->l('Module removed successfully from hook'), 18 => $this->l('Upload successful'),
			19 => $this->l('Duplication completed successfully'), 20 => $this->l('Translation added successfully but the language has not been created'),
			21 => $this->l('Module reset successfully'), 22 => $this->l('Module deleted successfully'),
			23 => $this->l('Localization pack imported successfully'), 24 => $this->l('Refund Successful'),
			25 => $this->l('Images successfully moved'),
			26 => $this->l('Cover selection saved'),
			27 => $this->l('Image shop association modified'),
			28 => $this->l('Zone affected to the selection successfully'),
		);
		if (!$this->identifier) $this->identifier = 'id_'.$this->table;
		if (!$this->_defaultOrderBy) $this->_defaultOrderBy = $this->identifier;
		$this->tabAccess = Profile::getProfileAccess($this->context->employee->id_profile, $this->id);

		// Fix for AdminHome
		if ($controller == 'AdminHome')
			$_POST['token'] = $this->token;

		if (!Shop::isFeatureActive())
			$this->shopLinkType = '';

		// Get the name of the folder containing the custom tpl files
		$this->tpl_folder = substr($controller, 5);
		$this->tpl_folder = Tools::toUnderscoreCase($this->tpl_folder).'/';

		$this->context->currency = new Currency(Configuration::get('PS_CURRENCY_DEFAULT'));
	}

	/**
	 * set default toolbar_title to admin breadcrumb
	 *
	 * @return void
	 */
	public function initToolbarTitle()
	{
		// Breadcrumbs
		$tabs = array();
		$tabs = Tab::recursiveTab($this->id, $tabs);
		$tabs = array_reverse($tabs);

		$bread = '';
		switch ($this->display)
		{
			case 'edit':
				$current_tab = array_pop($tabs);
				$tabs[] = array('name' => sprintf($this->l('Edit %s'), $current_tab['name']));
				break;

			case 'add':
				$current_tab = array_pop($tabs);
				$tabs[] = array('name' => sprintf($this->l('Add %s'), $current_tab['name']));
				break;

			case 'view':
				$current_tab = array_pop($tabs);
				$tabs[] = array('name' => sprintf($this->l('View %s'), $current_tab['name']));
				break;
		}
		// note : this should use a tpl file
		foreach ($tabs as $key => $item)
			$bread .= '<span class="breadcrumb item-'.$key.' ">'.$item['name'].'</span> : ';

		$bread = rtrim($bread, ': ');

		$this->toolbar_title = $bread;
	}

	/**
	 * Check rights to view the current tab
	 *
	 * @param bool $disable
	 * @return boolean
	 */
	public function viewAccess($disable = false)
	{
		if ($disable)
			return true;

		$this->tabAccess = Profile::getProfileAccess($this->context->employee->id_profile, $this->id);

		if ($this->tabAccess['view'] === '1')
			return true;
		return false;
	}

	/**
	 * Check for security token
	 */
	public function checkToken()
	{
		$token = Tools::getValue('token');
		return (!empty($token) && $token === $this->token);
	}

	public function ajaxProcessHelpAccess()
	{
		$this->json = true;
		$item = Tools::getValue('item');
		$iso_user = Tools::getValue('isoUser');
		$country = Tools::getValue('country');
		$version = Tools::getValue('version');

		if (isset($item) && isset($iso_user) && isset($country))
			$this->content = HelpAccess::getHelp($item, $iso_user, $country, $version);
		else
			$this->content = 'none';
		$this->display = 'content';
	}

	/**
	 * Set the filters used for the list display
	 */
	public function processFilter()
	{
		$filters = $this->context->cookie->getFamily($this->table.'Filter_');

		foreach ($filters as $key => $value)
		{
			/* Extracting filters from $_POST on key filter_ */
			if ($value != null && !strncmp($key, $this->table.'Filter_', 7 + Tools::strlen($this->table)))
			{
				$key = Tools::substr($key, 7 + Tools::strlen($this->table));
				/* Table alias could be specified using a ! eg. alias!field */
				$tmp_tab = explode('!', $key);
				$filter = count($tmp_tab) > 1 ? $tmp_tab[1] : $tmp_tab[0];

				if ($field = $this->filterToField($key, $filter))
				{
					$type = (array_key_exists('filter_type', $field) ? $field['filter_type'] : (array_key_exists('type', $field) ? $field['type'] : false));
					if (($type == 'date' || $type == 'datetime') && is_string($value))
						$value = unserialize($value);
					$key = isset($tmp_tab[1]) ? $tmp_tab[0].'.`'.$tmp_tab[1].'`' : '`'.$tmp_tab[0].'`';

					// Assignement by reference
					if (array_key_exists('tmpTableFilter', $field))
						$sql_filter = & $this->_tmpTableFilter;
					else if (array_key_exists('havingFilter', $field))
						$sql_filter = & $this->_filterHaving;
					else
						$sql_filter = & $this->_filter;

					/* Only for date filtering (from, to) */
					if (is_array($value))
					{
						if (isset($value[0]) && !empty($value[0]))
						{
							if (!Validate::isDate($value[0]))
								$this->_errors[] = Tools::displayError('\'from:\' date format is invalid (YYYY-MM-DD)');
							else
								$sql_filter .= ' AND '.pSQL($key).' >= \''.pSQL(Tools::dateFrom($value[0])).'\'';
						}

						if (isset($value[1]) && !empty($value[1]))
						{
							if (!Validate::isDate($value[1]))
								$this->_errors[] = Tools::displayError('\'to:\' date format is invalid (YYYY-MM-DD)');
							else
								$sql_filter .= ' AND '.pSQL($key).' <= \''.pSQL(Tools::dateTo($value[1])).'\'';
						}
					}
					else
					{
						$sql_filter .= ' AND ';
						$check_key = ($key == $this->identifier || $key == '`'.$this->identifier.'`');

						if ($type == 'int' || $type == 'bool')
							$sql_filter .= (($check_key || $key == '`active`') ? 'a.' : '').pSQL($key).' = '.(int)$value.' ';
						else if ($type == 'decimal')
							$sql_filter .= ($check_key ? 'a.' : '').pSQL($key).' = '.(float)$value.' ';
						else if ($type == 'select')
							$sql_filter .= ($check_key ? 'a.' : '').pSQL($key).' = \''.pSQL($value).'\' ';
						else
							$sql_filter .= ($check_key ? 'a.' : '').pSQL($key).' LIKE \'%'.pSQL($value).'%\' ';
					}
				}
			}
		}
	}

	/**
	 * @todo uses redirectAdmin only if !$this->ajax
	 */
	public function postProcess()
	{
		if ($this->ajax)
		{
			// from ajax-tab.php
			$action = Tools::getValue('action');
			// no need to use displayConf() here
			if (!empty($action) && method_exists($this, 'ajaxProcess'.Tools::toCamelCase($action)))
				return $this->{'ajaxProcess'.Tools::toCamelCase($action)}();
			else if (method_exists($this, 'ajaxProcess'))
				return $this->ajaxProcess();

			/*if (!empty($action) && method_exists($this, 'displayAjax'.Tools::toCamelCase($action)))
				$this->{'displayAjax'.$action}();
			else
				$this->displayAjax();	*/
		}
		else
		{
			if (!isset($this->table))
				return false;
			// set token
			$token = Tools::getValue('token') ? Tools::getValue('token') : $this->token;

			// Process list filtering
			if ($this->filter)
				$this->processFilter();

			if (!empty($this->action) && method_exists($this, 'process'.ucfirst(Tools::toCamelCase($this->action))))
			{
				/* Hook Before Action */
				Hook::exec('action'.get_class($this).ucfirst($this->action).'Before', array('controller' => $this));

				$return = $this->{'process'.Tools::toCamelCase($this->action)}($token);

				/* Hook After Action */
				Hook::exec('action'.get_class($this).ucfirst($this->action).'After', array('controller' => $this, 'return' => $return));

				return $return;
			}
			else if (method_exists($this, $this->action))
			{
				/* Hook Before Action */
				Hook::exec('action'.get_class($this).ucfirst($this->action).'Before', array('controller' => $this));

				$return = call_user_func(array($this, $this->action), $this->boxes);

				/* Hook After Action */
				Hook::exec('action'.get_class($this).ucfirst($this->action).'After', array('controller' => $this, 'return' => $return));

				return $return;
			}
		}
	}

	/**
	 * Object Delete images
	 *
	 * @param string $token
	 */
	public function processDeleteImage($token)
	{
		if (Validate::isLoadedObject($object = $this->loadObject()))
		{
			if (($object->deleteImage()))
			{
				$redirect = self::$currentIndex.'&add'.$this->table.'&'.$this->identifier.'='.Tools::getValue($this->identifier).'&conf=7&token='.$token;
				if (!$this->ajax)
					$this->redirect_after = $redirect;
				else
					$this->content = 'ok';
			}
		}
		$this->_errors[] = Tools::displayError('An error occurred during image deletion (cannot load object).');
		return $object;
	}

	/**
	 * Object Delete
	 *
	 * @param string $token
	 */
	public function processDelete($token)
	{
		if (Validate::isLoadedObject($object = $this->loadObject()) && isset($this->fieldImageSettings))
		{
			// check if request at least one object with noZeroObject
			if (isset($object->noZeroObject) && count(call_user_func(array($this->className, $object->noZeroObject))) <= 1)
			{
				$this->_errors[] = Tools::displayError('You need at least one object.').
					' <b>'.$this->table.'</b><br />'.
					Tools::displayError('You cannot delete all of the items.');
			}
			else if (array_key_exists('delete', $this->list_skip_actions) && in_array($object->id, $this->list_skip_actions['delete'])) //check if some ids are in list_skip_actions and forbid deletion
					$this->_errors[] = Tools::displayError('You cannot delete this items.');
			else
			{
				if ($this->deleted)
				{
					$object->deleteImage();
					$object->deleted = 1;
					if ($object->update())
						$this->redirect_after = self::$currentIndex.'&conf=1&token='.$token;
				}
				else if ($object->delete())
				{
					if (method_exists($object, 'cleanPositions'))
						$object->cleanPositions();
					$this->redirect_after = self::$currentIndex.'&conf=1&token='.$token;
				}
				$this->_errors[] = Tools::displayError('An error occurred during deletion.');
			}
		}
		else
		{
			$this->_errors[] = Tools::displayError('An error occurred while deleting object.').
				' <b>'.$this->table.'</b> '.
				Tools::displayError('(cannot load object)');
		}
		return $object;
	}

	/**
	 * Object update and creation
	 * TODO: split processAdd and processUpdate
	 *
	 * @param string $token
	 */
	public function processSave($token)
	{
		/* Checking fields validity */
		$this->validateRules();

		if (!count($this->_errors))
		{
			$id = (int)Tools::getValue($this->identifier);

			/* Object update */
			if (isset($id) && !empty($id))
			{
				$object = new $this->className($id);
				if (Validate::isLoadedObject($object))
				{
					/* Specific to objects which must not be deleted */
					if ($this->deleted && $this->beforeDelete($object))
					{
						// Create new one with old objet values
						$object_new = new $this->className($object->id);
						$object_new->id = null;
						$object_new->date_add = '';
						$object_new->date_upd = '';

						// Update old object to deleted
						$object->deleted = 1;
						$object->update();

						// Update new object with post values
						$this->copyFromPost($object_new, $this->table);
						$result = $object_new->add();
						if (Validate::isLoadedObject($object_new))
							$this->afterDelete($object_new, $object->id);
					}
					else
					{
						$this->copyFromPost($object, $this->table);
						$result = $object->update();
						$this->afterUpdate($object);
					}

					if ($object->id)
						$this->updateAssoShop($object->id);

					if (!$result)
					{
						$this->_errors[] = Tools::displayError('An error occurred while updating object.').
							' <b>'.$this->table.'</b> ('.Db::getInstance()->getMsgError().')';
					}
					else if ($this->postImage($object->id) && !count($this->_errors))
					{
						$parent_id = (int)Tools::getValue('id_parent', 1);
						// Specific back redirect
						if ($back = Tools::getValue('back'))
							$this->redirect_after = urldecode($back).'&conf=4';
						// Specific scene feature
						// @todo change stay_here submit name (not clear for redirect to scene ... )
						if (Tools::getValue('stay_here') == 'on' || Tools::getValue('stay_here') == 'true' || Tools::getValue('stay_here') == '1')
							$this->redirect_after = self::$currentIndex.'&'.$this->identifier.'='.$object->id.'&conf=4&updatescene&token='.$token;
						// Save and stay on same form
						// @todo on the to following if, we may prefer to avoid override redirect_after previous value
						if (Tools::isSubmit('submitAdd'.$this->table.'AndStay'))
							$this->redirect_after = self::$currentIndex.'&'.$this->identifier.'='.$object->id.'&conf=4&update'.$this->table.'&token='.$token;
						// Save and back to parent
						if (Tools::isSubmit('submitAdd'.$this->table.'AndBackToParent'))
							$this->redirect_after = self::$currentIndex.'&'.$this->identifier.'='.$parent_id.'&conf=4&token='.$token;
						// Default behavior (save and back)
						if (empty($this->redirect_after))
							$this->redirect_after = self::$currentIndex.($parent_id ? '&'.$this->identifier.'='.$object->id : '').'&conf=4&token='.$token;
					}
				}
				else
					$this->_errors[] = Tools::displayError('An error occurred while updating object.').
						' <b>'.$this->table.'</b> '.Tools::displayError('(cannot load object)');
			}

			/* Object creation */
			else
			{
				$object = new $this->className();
				$this->copyFromPost($object, $this->table);
				$this->beforeAdd($object);
				if (!$object->add())
				{
					$this->_errors[] = Tools::displayError('An error occurred while creating object.').
						' <b>'.$this->table.' ('.Db::getInstance()->getMsgError().')</b>';
				}
				 /* voluntary do affectation here */
				else if (($_POST[$this->identifier] = $object->id) && $this->postImage($object->id) && !count($this->_errors) && $this->_redirect)
				{
					$parent_id = (int)Tools::getValue('id_parent', 1);
					$this->afterAdd($object);
					$this->updateAssoShop($object->id);
					// Save and stay on same form
					if (Tools::isSubmit('submitAdd'.$this->table.'AndStay'))
						$this->redirect_after = self::$currentIndex.'&'.$this->identifier.'='.$object->id.'&conf=3&update'.$this->table.'&token='.$token;
					// Save and back to parent
					if (Tools::isSubmit('submitAdd'.$this->table.'AndBackToParent'))
						$this->redirect_after = self::$currentIndex.'&'.$this->identifier.'='.$parent_id.'&conf=3&token='.$token;
					// Default behavior (save and back)
					if (empty($this->redirect_after))
						$this->redirect_after = self::$currentIndex.($parent_id ? '&'.$this->identifier.'='.$object->id : '').'&conf=3&token='.$token;
				}
			}
		}

		$this->_errors = array_unique($this->_errors);
		if (count($this->_errors) > 0)
			return;

		return $object;
	}

	/**
	 * Change object required fields
	 *
	 * @param string $token
	 */
	public function processUpdateFields($token)
	{
		if (!is_array($fields = Tools::getValue('fieldsBox')))
			$fields = array();

		$object = new $this->className();
		if (!$object->addFieldsRequiredDatabase($fields))
			$this->_errors[] = Tools::displayError('Error in updating required fields');
		else
			$this->redirect_after = self::$currentIndex.'&conf=4&token='.$token;

		return $object;
	}

	/**
	 * Change object status (active, inactive)
	 *
	 * @param string $token
	 */
	public function processStatus($token)
	{
		if (Validate::isLoadedObject($object = $this->loadObject()))
		{
			if ($object->toggleStatus())
			{
				$id_category = (($id_category = (int)Tools::getValue('id_category')) && Tools::getValue('id_product')) ? '&id_category='.$id_category : '';
				$this->redirect_after = self::$currentIndex.'&conf=5'.$id_category.'&token='.$token;
			}
			else
				$this->_errors[] = Tools::displayError('An error occurred while updating status.');
		}
		else
			$this->_errors[] = Tools::displayError('An error occurred while updating status for object.').
				' <b>'.$this->table.'</b> '.
				Tools::displayError('(cannot load object)');

		return $object;
	}

	/**
	 * Change object position
	 *
	 * @param string $token
	 */
	public function processPosition($token)
	{
		if (!Validate::isLoadedObject($object = $this->loadObject()))
		{
			$this->_errors[] = Tools::displayError('An error occurred while updating status for object.').
				' <b>'.$this->table.'</b> '.Tools::displayError('(cannot load object)');
		}
		else if (!$object->updatePosition((int)Tools::getValue('way'), (int)Tools::getValue('position')))
			$this->_errors[] = Tools::displayError('Failed to update the position.');
		else
		{
			$id_identifier_str = ($id_identifier = (int)Tools::getValue($this->identifier)) ? '&'.$this->identifier.'='.$id_identifier : '';
			$redirect = self::$currentIndex.'&'.$this->table.'Orderby=position&'.$this->table.'Orderway=asc&conf=5'.$id_identifier_str.'&token='.$token;
			$this->redirect_after = $redirect;
		}
		return $object;
	}

	/**
	 * Cancel all filters for this tab
	 *
	 * @param string $token
	 */
	public function processResetFilters()
	{
		$filters = $this->context->cookie->getFamily($this->table.'Filter_');

		foreach ($filters as $cookie_key => $filter)
			if (strncmp($cookie_key, $this->table.'Filter_', 7 + Tools::strlen($this->table)) == 0)
			{
				$key = substr($cookie_key, 7 + Tools::strlen($this->table));
				/* Table alias could be specified using a ! eg. alias!field */
				$tmp_tab = explode('!', $key);
				$key = (count($tmp_tab) > 1 ? $tmp_tab[1] : $tmp_tab[0]);

				if (array_key_exists($key, $this->fieldsDisplay))
					unset($this->context->cookie->$cookie_key);
			}

		if (isset($this->context->cookie->{'submitFilter'.$this->table}))
			unset($this->context->cookie->{'submitFilter'.$this->table});

		if (isset($this->context->cookie->{$this->table.'Orderby'}))
			unset($this->context->cookie->{$this->table.'Orderby'});

		if (isset($this->context->cookie->{$this->table.'Orderway'}))
			unset($this->context->cookie->{$this->table.'Orderway'});

		unset($_POST);
		$this->filter = false;
	}

	/**
	 * Update options and preferences
	 *
	 * @param string $token
	 */
	protected function processUpdateOptions($token)
	{
		if ($this->tabAccess['edit'] === '1')
		{
			$this->beforeUpdateOptions();

			$languages = Language::getLanguages(false);

			foreach ($this->options as $category => $category_data)
			{
				if (!isset($category_data['fields']))
					continue;

				$fields = $category_data['fields'];

				foreach ($fields as $field => $values)
					if (isset($values['type']) && $values['type'] == 'selectLang')
					{
						foreach ($languages as $lang)
							if (Tools::getValue($field.'_'.strtoupper($lang['iso_code'])))
								$fields[$field.'_'.strtoupper($lang['iso_code'])] = array(
									'type' => 'select',
									'cast' => 'strval',
									'identifier' => 'mode',
									'list' => $values['list']
								);
					}

				/* Check required fields */
				foreach ($fields as $field => $values)
					if (isset($values['required']) && $values['required'] && !isset($_POST['configUseDefault'][$field]))
						if (isset($values['type']) && $values['type'] == 'textLang')
						{
							foreach ($languages as $language)
								if (($value = Tools::getValue($field.'_'.$language['id_lang'])) == false && (string)$value != '0')
									$this->_errors[] = Tools::displayError('field').' <b>'.$values['title'].'</b> '.Tools::displayError('is required.');
						}
						else if (($value = Tools::getValue($field)) == false && (string)$value != '0')
							$this->_errors[] = Tools::displayError('field').' <b>'.$values['title'].'</b> '.Tools::displayError('is required.');

				/* Check fields validity */
				foreach ($fields as $field => $values)
					if (isset($values['type']) && $values['type'] == 'textLang')
					{
						foreach ($languages as $language)
							if (Tools::getValue($field.'_'.$language['id_lang']) && isset($values['validation']))
								if (!Validate::$values['validation'](Tools::getValue($field.'_'.$language['id_lang'])))
									$this->_errors[] = Tools::displayError('field').' <b>'.$values['title'].'</b> '.Tools::displayError('is invalid.');
					}
					else if (Tools::getValue($field) && isset($values['validation']))
						if (!Validate::$values['validation'](Tools::getValue($field)))
							$this->_errors[] = Tools::displayError('field').' <b>'.$values['title'].'</b> '.Tools::displayError('is invalid.');

				/* Default value if null */
				foreach ($fields as $field => $values)
					if (!Tools::getValue($field) && isset($values['default']))
						$_POST[$field] = $values['default'];

				if (!count($this->_errors))
				{
					foreach ($fields as $key => $options)
					{
						if (Shop::isFeatureActive() && isset($options['visibility']) && ($options['visibility'] > Context::getContext()->shop->getContextType()))
							continue;

						if (Shop::isFeatureActive() && isset($_POST['configUseDefault'][$key]))
						{
							Configuration::deleteFromContext($key);
							continue;
						}

						// check if a method updateOptionFieldName is available
						$method_name = 'updateOption'.Tools::toCamelCase($key, true);
						if (method_exists($this, $method_name))
							$this->$method_name(Tools::getValue($key));
						else if (isset($options['type']) && in_array($options['type'], array('textLang', 'textareaLang')))
						{
							$list = array();
							foreach ($languages as $language)
							{
								$key_lang = Tools::getValue($key.'_'.$language['id_lang']);
								$val = (isset($options['cast']) ? $options['cast']($key_lang) : $key_lang);
								if ($this->validateField($val, $options))
								{
									if (Validate::isCleanHtml($val))
										$list[$language['id_lang']] = $val;
									else
										$this->_errors[] = Tools::displayError('Can not add configuration '.$key.' for lang '.Language::getIsoById((int)$language['id_lang']));
								}
							}
							Configuration::updateValue($key, $list);
						}
						else
						{
							$val = (isset($options['cast']) ? $options['cast'](Tools::getValue($key)) : Tools::getValue($key));
							if ($this->validateField($val, $options))
							{
								if (Validate::isCleanHtml($val))
									Configuration::updateValue($key, $val);
								else
									$this->_errors[] = Tools::displayError('Can not add configuration '.$key);
							}
						}
					}
					//d('after ');
				}
			}
			if (count($this->_errors) <= 0)
				$this->redirect_after = self::$currentIndex.'&conf=6&token='.$token;
		}
		else
			$this->_errors[] = Tools::displayError('You do not have permission to edit here.');

		// todo : return value ?
	}


	/**
	 * assign default action in toolbar_btn smarty var, if they are not set.
	 * uses override to specifically add, modify or remove items
	 *
	 */
	public function initToolbar()
	{
		switch ($this->display)
		{
			// @todo defining default buttons
			case 'add':
			case 'edit':
				// Default save button - action dynamically handled in javascript
				$this->toolbar_btn['save'] = array(
					'href' => '#',
					'desc' => $this->l('Save')
				);
				//no break
			case 'view':
				// Default cancel button - like old back link
				$back = Tools::safeOutput(Tools::getValue('back', ''));
				if (empty($back))
					$back = self::$currentIndex.'&token='.$this->token;

				$this->toolbar_btn['cancel'] = array(
					'href' => $back,
					'desc' => $this->l('Cancel')
				);
				break;
			case 'options':
				$this->toolbar_btn['save'] = array(
					'href' => '#',
					'desc' => $this->l('Save')
				);
				break;
			case 'view':
				break;
			default: // list
				$this->toolbar_btn['new'] = array(
					'href' => self::$currentIndex.'&amp;add'.$this->table.'&amp;token='.$this->token,
					'desc' => $this->l('Add new')
				);
		}

	}

	/**
	 * Load class object using identifier in $_GET (if possible)
	 * otherwise return an empty object, or die
	 *
	 * @param boolean $opt Return an empty object if load fail
	 * @return object
	 */
	protected function loadObject($opt = false)
	{
		$id = (int)Tools::getValue($this->identifier);
		if ($id && Validate::isUnsignedId($id))
		{
			if (!$this->object)
				$this->object = new $this->className($id);
			if (Validate::isLoadedObject($this->object))
				return $this->object;
			// throw exception
			$this->_errors[] = Tools::displayError('Object cannot be loaded (not found)');
			return false;
		}
		else if ($opt)
		{
			$this->object = new $this->className();
			return $this->object;
		}
		else
		{
			$this->_errors[] = Tools::displayError('Object cannot be loaded (identifier missing or invalid)');
			return false;
		}

		return $this->object;
	}

	/**
	 * Check if the token is valid, else display a warning page
	 */
	public function checkAccess()
	{
		if (!$this->checkToken())
		{
			// If this is an XSS attempt, then we should only display a simple, secure page
			// ${1} in the replacement string of the regexp is required,
			// because the token may begin with a number and mix up with it (e.g. $17)
			$url = preg_replace('/([&?]token=)[^&]*(&.*)?$/', '${1}'.$this->token.'$2', $_SERVER['REQUEST_URI']);
			if (false === strpos($url, '?token=') && false === strpos($url, '&token='))
				$url .= '&token='.$this->token;
			if (strpos($url, '?') === false)
				$url = str_replace('&token', '?controller=AdminHome&token', $url);

			$this->context->smarty->assign('url', htmlentities($url));
			return false;
		}
		return true;
	}

	protected function filterToField($key, $filter)
	{
		foreach ($this->fieldsDisplay as $field)
			if (array_key_exists('filter_key', $field) && $field['filter_key'] == $key)
				return $field;
		if (array_key_exists($filter, $this->fieldsDisplay))
			return $this->fieldsDisplay[$filter];
		return false;
	}

	public function displayNoSmarty()
	{
	}

	public function displayAjax()
	{
		if ($this->json)
		{
			$this->context->smarty->assign(array(
				'json' => true,
				'status' => $this->status,
			));
		}
		$this->layout = 'layout-ajax.tpl';
		return $this->display();
	}

	protected function redirect()
	{
		header('Location: '.$this->redirect_after);
		exit;
	}
	public function display()
	{
		$this->context->smarty->assign('display_header', $this->display_header);
		$this->context->smarty->assign('display_footer', $this->display_footer);
		$this->context->smarty->assign('meta_title', $this->meta_title);

		// Template override
		$tpl = $this->tpl_folder.'content.tpl';
		$tpl_action = $this->tpl_folder.$this->display.'.tpl';
		// Check if action template has been override

		// new smarty : template_dir is an array.
		// @todo : add override path to the smarty config, and checking all array item
		if (file_exists($this->context->smarty->template_dir[0].'/'.$tpl_action) && $this->display != 'view' && $this->display != 'options')
		{
			if (method_exists($this, $this->display.Tools::toCamelCase($this->className)))
				$this->{$this->display.Tools::toCamelCase($this->className)}();
			$this->context->smarty->assign('content', $this->context->smarty->fetch($tpl_action));
		}

		if (!$this->ajax)
		{
			// Check if content template has been override
			if (file_exists($this->context->smarty->template_dir[0].'/'.$tpl))
				$page = $this->context->smarty->fetch($tpl);
			else
				$page = $this->context->smarty->fetch($this->template);
		}
		else
			$page = $this->content;

		if ($conf = Tools::getValue('conf'))
			if ($this->json)
				$this->context->smarty->assign('conf', Tools::jsonEncode($this->_conf[(int)$conf]));
			else
				$this->context->smarty->assign('conf', $this->_conf[(int)$conf]);


		if ($this->json)
			$this->context->smarty->assign('errors', Tools::jsonEncode($this->_errors));
		else
			$this->context->smarty->assign('errors', $this->_errors);

		if ($this->json)
			$this->context->smarty->assign('warnings', Tools::jsonEncode($this->warnings));
		else
			$this->context->smarty->assign('warnings', $this->warnings);


		if ($this->json)
			$this->context->smarty->assign('informations', Tools::jsonEncode($this->informations));
		else
			$this->context->smarty->assign('informations', $this->informations);

		if ($this->json)
			$this->context->smarty->assign('confirmations', Tools::jsonEncode($this->confirmations));
		else
			$this->context->smarty->assign('confirmations', $this->confirmations);

		if ($this->json)
			$this->context->smarty->assign('page', Tools::jsonEncode($page));
		else
			$this->context->smarty->assign('page', $page);

		$this->context->smarty->display($this->layout);
	}

	/**
	 * add a warning message to display at the top of the page
	 *
	 * @param string $msg
	 */
	protected function displayWarning($msg)
	{
		$this->warnings[] = $msg;
	}

	/**
	 * add a info message to display at the top of the page
	 *
	 * @param string $msg
	 */
	protected function displayInformation($msg)
	{
		$this->informations[] = $msg;
	}

	/**
	 * Assign smarty variables for the header
	 */
	public function initHeader()
	{
		// Shop context
		if (Shop::isFeatureActive())
		{
			if (Context::shop() == Shop::CONTEXT_ALL)
			{
				$shop_context = 'all';
				$shop_name = '';
			}
			else if (Context::shop() == Shop::CONTEXT_GROUP)
			{
				$shop_context = 'group';
				$shop_name = $this->context->shop->getGroup()->name;
			}
			else
			{
				$shop_context = 'shop';
				$shop_name = $this->context->shop->name;
			}

			$this->context->smarty->assign(array(
				'shop_name' => $shop_name,
				'shop_context' => $shop_context,
			));

			$you_edit_field_for = sprintf(
				$this->l('A modification of this field will be applied for the shop %s'),
				'<b>'.Context::getContext()->shop->name.'</b>'
			);
		}

			// Multishop
		$is_multishop = Shop::isFeatureActive();// && Context::shop() != Shop::CONTEXT_ALL;
		/*if ($is_multishop)
		{
			if (Context::shop() == Shop::CONTEXT_GROUP)
			{
				$shop_context = 'group';
				$shop_name = $this->context->shop->getGroup()->name;
			}
			else if (Context::shop() == Shop::CONTEXT_SHOP)
			{
				$shop_context = 'shop';
				$shop_name = $this->context->shop->name;
			}*/

		// Quick access
		$quick_access = QuickAccess::getQuickAccesses($this->context->language->id);
		foreach ($quick_access as $index => $quick)
		{
			preg_match('/controller=(.+)(&.+)?$/', $quick['link'], $admin_tab);
			if (isset($admin_tab[1]))
			{
				if (strpos($admin_tab[1], '&'))
					$admin_tab[1] = substr($admin_tab[1], 0, strpos($admin_tab[1], '&'));

				$token = Tools::getAdminToken($admin_tab[1].(int)Tab::getIdFromClassName($admin_tab[1]).(int)$this->context->employee->id);
				$quick_access[$index]['link'] .= '&token='.$token;
			}
		}

		// Tab list
		$tabs = Tab::getTabs($this->context->language->id, 0);
		foreach ($tabs as $index => $tab)
		{
			if (Tab::checkTabRights($tab['id_tab']) === true)
			{
				if ($tab['name'] == 'Stock' && Configuration::get('PS_ADVANCED_STOCK_MANAGEMENT') == 0)
				{
					unset($tabs[$index]);
					continue;
				}

				$img_cache_url = 'themes/'.$this->context->employee->bo_theme.'/img/t/'.$tab['class_name'].'.png';
				$img_exists_cache = Tools::file_exists_cache(_PS_ADMIN_DIR_.$img_cache_url);
				// retrocompatibility : change png to gif if icon not exists
				if (!$img_exists_cache)
					$img_exists_cache = Tools::file_exists_cache(_PS_ADMIN_DIR_.str_replace('.png', '.gif', $img_cache_url));
				$img = $img_exists_cache ? $img_cache_url : _PS_IMG_.'t/'.$tab['class_name'].'.png';

				if (trim($tab['module']) != '')
					$img = _MODULE_DIR_.$tab['module'].'/'.$tab['class_name'].'.png';

				// retrocompatibility
				if (!file_exists(dirname(_PS_ROOT_DIR_).$img))
					$img = str_replace('png', 'gif', $img);
				// tab[class_name] does not contains the "Controller" suffix
				$tabs[$index]['current'] = ($tab['class_name'].'Controller' == get_class($this)) || (Tab::getCurrentParentId() == $tab['id_tab']);
				$tabs[$index]['img'] = $img;
				$tabs[$index]['href'] = $this->context->link->getAdminLink($tab['class_name']);

				$sub_tabs = Tab::getTabs($this->context->language->id, $tab['id_tab']);
				foreach ($sub_tabs as $index2 => $sub_tab)
				{
					// class_name is the name of the class controller
					if (Tab::checkTabRights($sub_tab) === true)
						$sub_tabs[$index2]['href'] = $this->context->link->getAdminLink($sub_tab['class_name']);
					else
						unset($sub_tabs[$index2]);
				}
				$tabs[$index]['sub_tabs'] = $sub_tabs;
				// @todo need a better way than using noTabLink property, keeping the fact to avoid db modification
				if (!in_array($tab['class_name'], $this->noTabLink))
					array_unshift($tabs[$index]['sub_tabs'], $tabs[$index]);
			}
			else
				unset($tabs[$index]);
		}

		/* Hooks are volontary out the initialize array (need those variables already assigned) */
		$bo_color = empty($this->context->employee->bo_color) ? '#FFFFFF' : $this->context->employee->bo_color;
		$this->context->smarty->assign(array(
			'img_dir' => _PS_IMG_,
			'iso' => $this->context->language->iso_code,
			'class_name' => $this->className,
			'iso_user' => $this->context->language->id,
			'country_iso_code' => $this->context->country->iso_code,
			'version' => _PS_VERSION_,
			'autorefresh_notifications' => Configuration::get('PS_ADMIN_REFRESH_NOTIFICATION'),
			'help_box' => Configuration::get('PS_HELPBOX'),
			'round_mode' => Configuration::get('PS_PRICE_ROUND_MODE'),
			'brightness' => Tools::getBrightness($bo_color) < 128 ? 'white' : '#383838',
			'edit_field' => isset($you_edit_field_for) ? $you_edit_field_for : '\'\'',
			'lang_iso' => $this->context->language->iso_code,
			'link' => $this->context->link,
			'bo_color' => isset($this->context->employee->bo_color) ? Tools::htmlentitiesUTF8($this->context->employee->bo_color) : null,
			'shop_name' => Configuration::get('PS_SHOP_NAME'),
			'show_new_orders' => Configuration::get('PS_SHOW_NEW_ORDERS'),
			'show_new_customers' => Configuration::get('PS_SHOW_NEW_CUSTOMERS'),
			'show_new_messages' => Configuration::get('PS_SHOW_NEW_MESSAGES'),
			'first_name' => Tools::substr($this->context->employee->firstname, 0, 1),
			'last_name' => htmlentities($this->context->employee->lastname, ENT_COMPAT, 'UTF-8'),
			'base_url' => $this->context->shop->getBaseURL(),
			'employee' => $this->context->employee,
			'search_type' => Tools::getValue('bo_search_type'),
			'bo_query' => Tools::safeOutput(Tools::stripslashes(Tools::getValue('bo_query'))),
			'quick_access' => $quick_access,
			'multi_shop' => Shop::isFeatureActive(),
			'shop_list' => (Shop::isFeatureActive() ? generateShopList() : null), //@TODO refacto
			'shop' => $this->context->shop,
			'group_shop' => $this->context->shop->getGroup(),
			'tab' => $tab,
			'current_parent_id' => (int)Tab::getCurrentParentId(),
			'tabs' => $tabs,
			'install_dir_exists' => file_exists(_PS_ADMIN_DIR_.'/../install'),
			'is_multishop' => $is_multishop,
			'pic_dir' => _THEME_PROD_PIC_DIR_
		));
		$this->context->smarty->assign(array(
			'HOOK_HEADER' => Hook::exec('backOfficeHeader'),
			'HOOK_TOP' => Hook::exec('backOfficeTop'),
		));
	}

	/**
	 * Declare an action to use for each row in the list
	 */
	public function addRowAction($action)
	{
		$action = strtolower($action);
		$this->actions[] = $action;
	}

	/**
	 * Add  an action to use for each row in the list
	 */
	public function addRowActionSkipList($action, $list)
	{
		$action = strtolower($action);
		$list = (array)$list;

		if (array_key_exists($action, $this->list_skip_actions))
			$this->list_skip_actions[$action] = array_merge($this->list_skip_actions[$action], $list);
		else
			$this->list_skip_actions[$action] = $list;
	}

	/**
	 * Assign smarty variables for all default views, list and form, then call other init functions
	 */
	public function initContent()
	{
		// toolbar (save, cancel, new, ..)
		$this->initToolbar();
		if ($this->display == 'edit' || $this->display == 'add')
		{
			if (!$this->loadObject(true))
				return;

			$this->content .= $this->renderForm();
		}
		else if ($this->display == 'view')
		{
			// Some controllers use the view action without an object
			if ($this->className)
				$this->loadObject(true);
			$this->content .= $this->renderView();
		}
		else if (!$this->ajax)
		{
			$this->content .= $this->renderList();
			$this->content .= $this->renderOptions();
		}

		$this->context->smarty->assign(array(
			'content' => $this->content,
			'url_post' => self::$currentIndex.'&token='.$this->token,
		));
	}

	/**
	 * initialize the invalid doom page of death
	 *
	 * @return void
	 */
	public function initCursedPage()
	{
		$this->layout = 'invalid_token.tpl';
	}

	/**
	 * Assign smarty variables for the footer
	 */
	public function initFooter()
	{
		// We assign js and css files on the last step before display template, because controller can add many js and css files
		$this->context->smarty->assign('css_files', $this->css_files);
		$this->context->smarty->assign('js_files', array_unique($this->js_files));

		$this->context->smarty->assign(array(
			'ps_version' => _PS_VERSION_,
			'end_time' => number_format(microtime(true) - $this->timerStart, 3, '.', ''),
			'iso_is_fr' => strtoupper($this->context->language->iso_code) == 'FR',
		));

		$this->context->smarty->assign(array(
			'HOOK_FOOTER' => Hook::exec('backOfficeFooter'),
		));
	}

	/**
	 * Function used to render the list to display for this controller
	 */
	public function renderList()
	{
		if (!($this->fieldsDisplay && is_array($this->fieldsDisplay)))
			return false;
		$this->getList($this->context->language->id);

		// Empty list is ok
		if (!is_array($this->_list))
			return false;

		$helper = new HelperList();
		$this->setHelperDisplay($helper);
		$helper->tpl_vars = $this->tpl_list_vars;
		$helper->tpl_delete_link_vars = $this->tpl_delete_link_vars;
		// Check if list templates have been overriden

		// For compatibility reasons, we have to check standard actions in class attributes
		foreach ($this->actions_available as $action)
		{
			if (!in_array($action, $this->actions) && isset($this->$action) && $this->$action)
				$this->actions[] = $action;
		}

		$list = $helper->generateList($this->_list, $this->fieldsDisplay);
		$this->toolbar_fix = false;

		return $list;
	}

	/**
	 * Override to render the view page
	 */
	public function renderView()
	{
		$helper = new HelperView($this);
		$this->setHelperDisplay($helper);
		$helper->tpl_vars = $this->tpl_view_vars;
		!is_null($this->base_tpl_view) ? $helper->base_tpl = $this->base_tpl_view : '';
		$view = $helper->generateView();
		$this->toolbar_fix = false;

		return $view;
	}

	/**
	 * Function used to render the form for this controller
	 */
	public function renderForm()
	{
		if (Tools::getValue('submitFormAjax'))
			$this->content .= $this->context->smarty->fetch($this->context->smarty->template_dir[0].'form_submit_ajax.tpl');
		if ($this->fields_form && is_array($this->fields_form))
		{
			if (!$this->multiple_fieldsets)
				$this->fields_form = array(array('form' => $this->fields_form));

			$this->getlanguages();
			$helper = new HelperForm($this);
			$this->setHelperDisplay($helper);
			$helper->fields_value = $this->getFieldsValue($this->object);
			$helper->tpl_vars = $this->tpl_form_vars;
			!is_null($this->base_tpl_form) ? $helper->base_tpl = $this->base_tpl_form : '';
			if ($this->tabAccess['view'])
			{
				if (Tools::getValue('back'))
					$helper->tpl_vars['back'] = Tools::safeOutput(Tools::getValue('back'));
				else
					$helper->tpl_vars['back'] = Tools::safeOutput(Tools::getValue(self::$currentIndex.'&token='.$this->token));
			}
			$form = $helper->generateForm($this->fields_form);
			$this->toolbar_fix = false;

			return $form;
		}
	}

	/**
	 * Function used to render the options for this controller
	 */
	public function renderOptions()
	{
		if ($this->options && is_array($this->options))
		{
			if ($this->display != 'options')
				$this->show_toolbar = false;

			$helper = new HelperOptions($this);
			$this->setHelperDisplay($helper);
			$helper->id = $this->id;
			$helper->tpl_vars = $this->tpl_option_vars;
			$options = $helper->generateOptions($this->options);
			$this->toolbar_fix = false;

			return $options;
		}
	}

	/**
	 * this function set various display option for helper list
	 *
	 * @param Helper $helper
	 * @return void
	 */
	public function setHelperDisplay(Helper $helper)
	{
		if (empty($this->toolbar_title))
			$this->initToolbarTitle();
		// tocheck
		if ($this->object && $this->object->id)
			$helper->id = $this->object->id;

		// @todo : move that in Helper
		$helper->title = $this->toolbar_title;
		$helper->toolbar_btn = $this->toolbar_btn;

		$helper->ps_help_context = !Configuration::get('PS_NO_HELP_CONTEXT');
		$helper->show_toolbar = $this->show_toolbar;
		$helper->toolbar_fix = $this->toolbar_fix;
		$helper->override_folder = $this->tpl_folder;
		$helper->actions = $this->actions;
		$helper->simple_header = $this->list_simple_header;
		$helper->bulk_actions = $this->bulk_actions;
		$helper->currentIndex = self::$currentIndex;
		$helper->className = $this->className;
		$helper->table = $this->table;
		$helper->orderBy = $this->_orderBy;
		$helper->orderWay = $this->_orderWay;
		$helper->listTotal = $this->_listTotal;
		$helper->shopLink = $this->shopLink;
		$helper->shopLinkType = $this->shopLinkType;
		$helper->identifier = $this->identifier;
		$helper->token = $this->token;
		$helper->languages = $this->_languages;
		$helper->specificConfirmDelete = $this->specificConfirmDelete;
		$helper->imageType = $this->imageType;
		$helper->no_link = $this->list_no_link;
		$helper->colorOnBackground = $this->colorOnBackground;
		$helper->ajax_params = (isset($this->ajax_params) ? $this->ajax_params : null);
		$helper->default_form_language = $this->default_form_language;
		$helper->allow_employee_form_lang = $this->allow_employee_form_lang;
		$helper->multiple_fieldsets = $this->multiple_fieldsets;

		// For each action, try to add the corresponding skip elements list
		$helper->list_skip_actions = $this->list_skip_actions;
	}

	public function setMedia()
	{
		$this->addCSS(_PS_CSS_DIR_.'admin.css', 'all');
		$this->addCSS(__PS_BASE_URI__.str_replace(_PS_ROOT_DIR_.DIRECTORY_SEPARATOR, '', _PS_ADMIN_DIR_).'/themes/default/admin.css', 'all');
		if ($this->context->language->is_rtl)
			$this->addCSS(_THEME_CSS_DIR_.'rtl.css');

		$this->addJquery();
		$this->addjQueryPlugin(array('cluetip', 'hoverIntent', 'scrollTo'));

		$this->addJS(array(
			_PS_JS_DIR_.'admin.js',
			_PS_JS_DIR_.'toggle.js',
			_PS_JS_DIR_.'tools.js',
			_PS_JS_DIR_.'ajax.js',
			_PS_JS_DIR_.'notifications.js',
			_PS_JS_DIR_.'helpAccess.js',
			_PS_JS_DIR_.'toolbar.js'
		));
	}
	/**
	 * use translations files to replace english expression.
	 *
	 * @param mixed $string term or expression in english
	 * @param string $class the classname (without "Controller" suffix)
	 * @param boolan $addslashes if set to true, the return value will pass through addslashes(). Otherwise, stripslashes().
	 * @param boolean $htmlentities if set to true(default), the return value will pass through htmlentities($string, ENT_QUOTES, 'utf-8')
	 * @return string the translation if available, or the english default text.
	 */
	public static function translate($string, $class, $addslashes = false, $htmlentities = true)
	{
		// every admin translations should use this method
		// @todo remove global keyword in translations files and use static
		global $_LANGADM;

		if (!is_array($_LANGADM))
		{
			$iso = Context::getContext()->language->iso_code;
			include_once(_PS_TRANSLATIONS_DIR_.$iso.'/admin.php');

			if (isset($_LANGADM))
				$_LANGADM = array_change_key_case($_LANGADM);
			else
				$_LANGADM = array();
		}

		$class = strtolower($class);
		// For traductions in a tpl folder with an underscore
		$class = str_replace('_', '', $class);

		// if the class is extended by a module, use modules/[module_name]/xx.php lang file
		if (false && Module::getModuleNameFromClass($class))
		{
			$string = str_replace('\'', '\\\'', $string);
			return Module::findTranslation(Module::$classInModule[$class], $string, $class);
		}

		$key = md5(str_replace('\'', '\\\'', $string));

		// retrocomp :
		// if value is not set, try with "AdminTab" as prefix.
		// @todo : change AdminTab to Helper
		if (isset($_LANGADM[$class.$key]))
			$str = $_LANGADM[$class.$key];
		else if (isset($_LANGADM['admincontroller'.$key]))
			$str = $_LANGADM['admincontroller'.$key];
		else if (isset($_LANGADM['helper'.$key]))
			$str = $_LANGADM['helper'.$key];
		else if (isset($_LANGADM['admintab'.$key]))
			$str = $_LANGADM['admintab'.$key];
		else
			// note in 1.5, some translations has moved from AdminXX to helper/*.tpl
			$str = $string;
		$str = $htmlentities ? htmlentities($str, ENT_QUOTES, 'utf-8') : $str;
		return str_replace('"', '&quot;', ($addslashes ? addslashes($str) : stripslashes($str)));
	}

	/**
	 * non-static method which uses AdminController::translate()
	 *
	 * @param mixed $string term or expression in english
	 * @param string $class name of the class, without "Controller" suffix
	 * @param boolan $addslashes if set to true, the return value will pass through addslashes(). Otherwise, stripslashes().
	 * @param boolean $htmlentities if set to true(default), the return value will pass through htmlentities($string, ENT_QUOTES, 'utf-8')
	 * @return string the translation if available, or the english default text.
	 */
	protected function l($string, $class = 'AdminTab', $addslashes = false, $htmlentities = true)
	{
		// classname has changed, from AdminXXX to AdminXXXController
		// So we remove 10 characters and we keep same keys
		if (strtolower(substr($class, -10)) == 'controller')
			$class = substr($class, 0, -10);
		else if ($class == 'AdminTab')
			$class = substr(get_class($this), 0, -10);
		return self::translate($string, $class, $addslashes, $htmlentities);
	}

	/**
	 * Init context and dependencies, handles POST and GET
	 */
	public function init()
	{
		parent::init();

		if (Tools::getValue('ajax'))
			$this->ajax = '1';

		/* Server Params */
		$protocol_link = (Configuration::get('PS_SSL_ENABLED')) ? 'https://' : 'http://';
		$protocol_content = (isset($useSSL) && $useSSL && Configuration::get('PS_SSL_ENABLED')) ? 'https://' : 'http://';
		$this->context->link = new Link($protocol_link, $protocol_content);

		$this->timerStart = microtime(true);

		if (isset($_GET['logout']))
			$this->context->employee->logout();

		if (get_class($this) != 'AdminLoginController' && (!isset($this->context->employee) || !$this->context->employee->isLoggedBack()))
			$this->redirect_after = $this->context->link->getAdminLink('AdminLogin').(!isset($_GET['logout']) ? '?redirect='.$_SERVER['REQUEST_URI'] : '');

		// Set current index
		$current_index = $_SERVER['SCRIPT_NAME'].(($controller = Tools::getValue('controller')) ? '?controller='.$controller : '');
		if ($back = Tools::getValue('back'))
			$current_index .= '&back='.urlencode($back);
		self::$currentIndex = $current_index;

		if ((int)Tools::getValue('liteDisplaying'))
		{
			$this->display_header = false;
			$this->display_footer = false;
			$this->content_only = false;
		}

		// Change shop context ?
		if (Shop::isFeatureActive() && Tools::getValue('setShopContext') !== false)
		{
			$this->context->cookie->shopContext = Tools::getValue('setShopContext');
			$url = parse_url($_SERVER['REQUEST_URI']);
			$query = (isset($url['query'])) ? $url['query'] : '';
			parse_str($query, $parse_query);
			unset($parse_query['setShopContext'], $parse_query['conf']);
			$this->redirect_after = $url['path'].'?'.http_build_query($parse_query);
		}
		else if (!Shop::isFeatureActive())
			$this->context->cookie->shopContext = 's-1';
		$shop_id = '';
		if ($this->context->cookie->shopContext)
		{
			$split = explode('-', $this->context->cookie->shopContext);
			if (count($split) == 2 && $split[0] == 's')
				$shop_id = (int)$split[1];
		}
		else if ($this->context->employee->id_profile == _PS_ADMIN_PROFILE_)
			$shop_id = '';
		else if ($this->context->shop->getTotalShopsWhoExists() != Employee::getTotalEmployeeShopById((int)$this->context->employee->id))
		{
			$shops = Employee::getEmployeeShopById((int)$this->context->employee->id);
			if (count($shops))
				$shop_id = (int)$shops[0];
		}
		else
			Employee::getEmployeeShopAccess((int)$this->context->employee->id);

		$this->context->shop = new Shop($shop_id);

		if ($this->ajax && method_exists($this, 'ajaxPreprocess'))
			$this->ajaxPreProcess();

		$this->context->smarty->assign(array(
			'table' => $this->table,
			'current' => self::$currentIndex,
			'token' => $this->token,
		));

		$this->initProcess();
	}

	/**
	 * Retrieve GET and POST value and translate them to actions
	 */
	public function initProcess()
	{
		// Filter memorization
		if (isset($_POST) && !empty($_POST) && isset($this->table))
			foreach ($_POST as $key => $value)
				if (is_array($this->table))
				{
					foreach ($this->table as $table)
						if (strncmp($key, $table.'Filter_', 7) === 0 || strncmp($key, 'submitFilter', 12) === 0)
							$this->context->cookie->$key = !is_array($value) ? $value : serialize($value);
				}
				else if (strncmp($key, $this->table.'Filter_', 7) === 0 || strncmp($key, 'submitFilter', 12) === 0)
					$this->context->cookie->$key = !is_array($value) ? $value : serialize($value);
		if (isset($_GET) && !empty($_GET) && isset($this->table))
			foreach ($_GET as $key => $value)
				if (is_array($this->table))
				{
					foreach ($this->table as $table)
						if (strncmp($key, $table.'OrderBy', 7) === 0 || strncmp($key, $table.'Orderway', 8) === 0)
							$this->context->cookie->$key = $value;
				}
				else if (strncmp($key, $this->table.'OrderBy', 7) === 0 || strncmp($key, $this->table.'Orderway', 12) === 0)
					$this->context->cookie->$key = $value;

		// Manage list filtering
		if (Tools::isSubmit('submitFilter'.$this->table) || $this->context->cookie->{'submitFilter'.$this->table} !== false)
			$this->filter = true;

		$this->id_object = (int)Tools::getValue('id_'.$this->table);

		/* Delete object image */
		if (isset($_GET['deleteImage']))
		{
			if ($this->tabAccess['delete'] === '1')
				$this->action = 'delete_image';
			else
				$this->_errors[] = Tools::displayError('You do not have permission to delete here.');
		}
		/* Delete object */
		else if (isset($_GET['delete'.$this->table]))
		{
			if ($this->tabAccess['delete'] === '1')
				$this->action = 'delete';
			else
				$this->_errors[] = Tools::displayError('You do not have permission to delete here.');
		}
		/* Change object statuts (active, inactive) */
		else if ((isset($_GET['status'.$this->table]) || isset($_GET['status'])) && Tools::getValue($this->identifier))
		{
			if ($this->tabAccess['edit'] === '1')
				$this->action = 'status';
			else
				$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
		}
		/* Move an object */
		else if (isset($_GET['position']))
		{
			if ($this->tabAccess['edit'] == '1')
				$this->action = 'position';
			else
				$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
		}
		else if ($submitted_action = Tools::getValue('submitAction'.$this->table))
				$this->action = $submitted_action;
		else if (Tools::getValue('submitAdd'.$this->table) || Tools::getValue('submitAdd'.$this->table.'AndStay'))
		{
			// case 1: updating existing entry
			if ($this->id_object)
			{
				if ($this->tabAccess['edit'] === '1')
				{
					$this->action = 'save';
					$this->display = 'edit';
				}
				else
					$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
			}
			// case 2: creating new entry
			else
			{
				if ($this->tabAccess['add'] === '1')
				{
					$this->action = 'save';
					$this->display = 'edit';
				}
				else
					$this->_errors[] = Tools::displayError('You do not have permission to add here.');
			}
		}
		else if (isset($_GET['add'.$this->table]))
		{
			if ($this->tabAccess['add'] === '1')
			{
				$this->action = 'new';
				$this->action = 'Informations';
				$this->display = 'add';
			}
			else
				$this->_errors[] = Tools::displayError('You do not have permission to add here.');
		}
		else if (isset($_GET['update'.$this->table]) && isset($_GET['id_'.$this->table]))
		{
			if ($this->tabAccess['edit'] === '1')
				$this->display = 'edit';
			else
				$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
		}
		else if (isset($_GET['view'.$this->table]))
		{
			if ($this->tabAccess['view'] === '1')
			{
				$this->display = 'view';
				$this->action = 'view';
			}
			else
				$this->_errors[] = Tools::displayError('You do not have permission to view here.');
		}
		/* Cancel all filters for this tab */
		else if (isset($_POST['submitReset'.$this->table]))
			$this->action = 'reset_filters';
		/* Submit options list */
		else if (Tools::getValue('submitOptions'.$this->table) || Tools::getValue('submitOptions'))
			$this->action = 'update_options';
		else if (Tools::isSubmit('submitFields') && $this->requiredDatabase && $this->tabAccess['add'] === '1' && $this->tabAccess['delete'] === '1')
			$this->action = 'update_fields';
		else if (is_array($this->bulk_actions))
			foreach ($this->bulk_actions as $bulk_action => $params)
			{
				if (Tools::isSubmit('submitBulk'.$bulk_action.$this->table) || Tools::isSubmit('submitBulk'.$bulk_action))
				{
					$this->action = 'bulk'.$bulk_action;
					$this->boxes = Tools::getValue($this->table.'Box');
					break;
				}
				else if(Tools::isSubmit('submitBulk'))
				{
					$this->action = 'bulk'.Tools::getValue('select_submitBulk');
					$this->boxes = Tools::getValue($this->table.'Box');
					break;
				}
			}
		else if (!empty($this->options) && empty($this->fieldsDisplay))
			$this->display = 'options';
	}

	/**
	 * Get the current objects' list form the database
	 *
	 * @param integer $id_lang Language used for display
	 * @param string $order_by ORDER BY clause
	 * @param string $_orderWay Order way (ASC, DESC)
	 * @param integer $start Offset in LIMIT clause
	 * @param integer $limit Row count in LIMIT clause
	 */
	public function getList($id_lang, $order_by = null, $order_way = null, $start = 0, $limit = null, $id_lang_shop = false)
	{
		/* Manage default params values */
		$use_limit = true;
		if ($limit === false)
			$use_limit = false;
		else if (empty($limit))
		{
			if (isset($this->context->cookie->{$this->table.'_pagination'}) && $this->context->cookie->{$this->table.'_pagination'})
				$limit = $this->context->cookie->{$this->table.'_pagination'};
			else
				$limit = $this->_pagination[1];
		}

		if (!Validate::isTableOrIdentifier($this->table))
			throw new PrestashopException(sprintf('Table name %s is invalid:', $this->table));

		if (empty($order_by))
			$order_by = $this->context->cookie->__get($this->table.'Orderby') ? $this->context->cookie->__get($this->table.'Orderby') : $this->_defaultOrderBy;
		if (empty($order_way))
			$order_way = $this->context->cookie->__get($this->table.'Orderway') ? $this->context->cookie->__get($this->table.'Orderway') : 'ASC';

		$limit = (int)Tools::getValue('pagination', $limit);
		$this->context->cookie->{$this->table.'_pagination'} = $limit;

		/* Check params validity */
		if (!Validate::isOrderBy($order_by) || !Validate::isOrderWay($order_way)
			|| !is_numeric($start) || !is_numeric($limit)
			|| !Validate::isUnsignedId($id_lang))
			throw new PrestashopException('get list params is not valid');

		/* Determine offset from current page */
		if ((isset($_POST['submitFilter'.$this->table]) ||
		isset($_POST['submitFilter'.$this->table.'_x']) ||
		isset($_POST['submitFilter'.$this->table.'_y'])) &&
		!empty($_POST['submitFilter'.$this->table]) &&
		is_numeric($_POST['submitFilter'.$this->table]))
			$start = ((int)$_POST['submitFilter'.$this->table] - 1) * $limit;

		/* Cache */
		$this->_lang = (int)$id_lang;
		$this->_orderBy = $order_by;
		$this->_orderWay = Tools::strtoupper($order_way);

		/* SQL table : orders, but class name is Order */
		$sql_table = $this->table == 'order' ? 'orders' : $this->table;

		// Add SQL shop restriction
		$select_shop = $join_shop = $where_shop = '';
		if ($this->shopLinkType)
		{
			$select_shop = ', shop.name as shop_name ';
			$join_shop = ' LEFT JOIN '._DB_PREFIX_.$this->shopLinkType.' shop
							ON a.id_'.$this->shopLinkType.' = shop.id_'.$this->shopLinkType;
			$where_shop = $this->context->shop->addSqlRestriction($this->shopShareDatas, 'a', $this->shopLinkType);
		}

		$assos = Shop::getAssoTables();
		if (isset($assos[$this->table]) && $assos[$this->table]['type'] == 'shop')
		{
			$filter_key = $assos[$this->table]['type'];
			$idenfier_shop = $this->context->shop->getListOfID();
		}
		else if (Context::shop() == Shop::CONTEXT_GROUP)
		{
			$assos = GroupShop::getAssoTables();
			if (isset($assos[$this->table]) && $assos[$this->table]['type'] == 'group_shop')
			{
				$filter_key = $assos[$this->table]['type'];
				$idenfier_shop = array($this->context->shop->getGroupID());
			}
		}

		$filter_shop = '';
		if (isset($filter_key))
		{
			if (!$this->_group)
				$this->_group = ' GROUP BY a.'.pSQL($this->identifier);
			else if (!preg_match('#(\s|,)\s*a\.`?'.pSQL($this->identifier).'`?(\s|,|$)#', $this->_group))
				$this->_group .= ', a.'.pSQL($this->identifier);

			$test_join = !preg_match('#`?'.preg_quote(_DB_PREFIX_.$this->table.'_'.$filter_key).'`? *sa#', $this->_join);
			if (Shop::isFeatureActive() && Context::shop() != Shop::CONTEXT_ALL && $test_join)
			{
				$filter_shop = ' JOIN `'._DB_PREFIX_.$this->table.'_'.$filter_key.'` sa ';
				$filter_shop .= 'ON (sa.'.$this->identifier.' = a.'.$this->identifier.' AND sa.id_'.$filter_key.' IN ('.implode(', ', $idenfier_shop).'))';
			}
		}

		/* Query in order to get results with all fields */
		$lang_join = '';
		if ($this->lang)
		{
			$lang_join = 'LEFT JOIN `'._DB_PREFIX_.$this->table.'_lang` b ON (b.`'.$this->identifier.'` = a.`'.$this->identifier.'`';
			$lang_join .= ' AND b.`id_lang` = '.(int)$id_lang;
			if ($id_lang_shop)
			 	 $lang_join .= ' AND b.`id_shop`='.(int)$id_lang_shop;
			$lang_join .= ')';
		}

		$having_clause = '';
		if (isset($this->_filterHaving) || isset($this->_having))
		{
			 $having_clause = ' HAVING ';
			 if (isset($this->_filterHaving))
			 	$having_clause .= ltrim($this->_filterHaving, ' AND ');
			 if (isset($this->_having))
			 	$having_clause .= $this->_having.' ';
		}

		$sql = 'SELECT SQL_CALC_FOUND_ROWS
			'.($this->_tmpTableFilter ? ' * FROM (SELECT ' : '').'
			'.($this->lang ? 'b.*, ' : '').'a.*'.(isset($this->_select) ? ', '.$this->_select.' ' : '').$select_shop.'
			FROM `'._DB_PREFIX_.$sql_table.'` a
			'.$filter_shop.'
			'.$lang_join.'
			'.(isset($this->_join) ? $this->_join.' ' : '').'
			'.$join_shop.'
			WHERE 1 '.(isset($this->_where) ? $this->_where.' ' : '').($this->deleted ? 'AND a.`deleted` = 0 ' : '').
			(isset($this->_filter) ? $this->_filter : '').$where_shop.'
			'.(isset($this->_group) ? $this->_group.' ' : '').'
			'.$having_clause.'
			ORDER BY '.(($order_by == $this->identifier) ? 'a.' : '').'`'.pSQL($order_by).'` '.pSQL($order_way).
			($this->_tmpTableFilter ? ') tmpTable WHERE 1'.$this->_tmpTableFilter : '').
			(($use_limit === true) ? ' LIMIT '.(int)$start.','.(int)$limit : '');

		$this->_list = Db::getInstance()->executeS($sql);
		$this->_listTotal = Db::getInstance()->getValue('SELECT FOUND_ROWS() AS `'._DB_PREFIX_.$this->table.'`');
	}

	public function getlanguages()
	{
		$cookie = $this->context->cookie;
		$this->allow_employee_form_lang = Configuration::get('PS_BO_ALLOW_EMPLOYEE_FORM_LANG') ? Configuration::get('PS_BO_ALLOW_EMPLOYEE_FORM_LANG') : 0;
		if ($this->allow_employee_form_lang && !$cookie->employee_form_lang)
			$cookie->employee_form_lang = (int)Configuration::get('PS_LANG_DEFAULT');
		$use_lang_from_cookie = false;
		$this->_languages = Language::getLanguages(false);
		if ($this->allow_employee_form_lang)
			foreach ($this->_languages as $lang)
				if ($cookie->employee_form_lang == $lang['id_lang'])
					$use_lang_from_cookie = true;
		if (!$use_lang_from_cookie)
			$this->default_form_language = (int)Configuration::get('PS_LANG_DEFAULT');
		else
			$this->default_form_language = (int)$cookie->employee_form_lang;

		foreach ($this->_languages as $k => $language)
			$this->_languages[$k]['is_default'] = (int)($language['id_lang'] == $this->default_form_language);

		return $this->_languages;
	}


	/**
	 * Return the list of fields value
	 *
	 * @param object $obj Object
	 * @return array
	 */
	public function getFieldsValue($obj)
	{
		foreach ($this->fields_form as $fieldset)
			if (isset($fieldset['form']['input']))
				foreach ($fieldset['form']['input'] as $input)
					if (empty($this->fields_value[$input['name']]))
						if (isset($input['type']) && ($input['type'] == 'group_shop' || $input['type'] == 'shop'))
						{
							if ($obj->id)
							{
								if ($input['type'] == 'group_shop')
									$result = GroupShop::getGroupShopById((int)$obj->id, $this->identifier, $this->table);
								else
									$result = Shop::getShopById((int)$obj->id, $this->identifier, $this->table);

								foreach ($result as $row)
									$this->fields_value['shop'][$row['id_'.$input['type']]][] = $row[$this->identifier];
							}
						}
						else if (isset($input['lang']) && $input['lang'])
							foreach ($this->_languages as $language)
								$this->fields_value[$input['name']][$language['id_lang']] = $this->getFieldValue($obj, $input['name'], $language['id_lang']);
						else
							$this->fields_value[$input['name']] = $this->getFieldValue($obj, $input['name']);

		return $this->fields_value;
	}

	/**
	 * Return field value if possible (both classical and multilingual fields)
	 *
	 * Case 1 : Return value if present in $_POST / $_GET
	 * Case 2 : Return object value
	 *
	 * @param object $obj Object
	 * @param string $key Field name
	 * @param integer $id_lang Language id (optional)
	 * @return string
	 */
	public function getFieldValue($obj, $key, $id_lang = null)
	{
		if ($id_lang)
			$default_value = ($obj->id && isset($obj->{$key}[$id_lang])) ? $obj->{$key}[$id_lang] : '';
		else
			$default_value = isset($obj->{$key}) ? $obj->{$key} : '';

		return Tools::getValue($key.($id_lang ? '_'.$id_lang : ''), $default_value);
	}

	/**
	 * Manage page display (form, list...)
	 *
	 * @param string $className Allow to validate a different class than the current one
	 */
	public function validateRules($class_name = false)
	{
		if (!$class_name)
			$class_name = $this->className;

		/* Class specific validation rules */
		$rules = call_user_func(array($class_name, 'getValidationRules'), $class_name);

		if ((count($rules['requiredLang']) || count($rules['sizeLang']) || count($rules['validateLang'])))
		{
			/* Language() instance determined by default language */
			$default_language = new Language((int)Configuration::get('PS_LANG_DEFAULT'));

			/* All availables languages */
			$languages = Language::getLanguages(false);
		}

		/* Checking for required fields */
		foreach ($rules['required'] as $field)
			if (($value = Tools::getValue($field)) == false && (string)$value != '0')
				if (!Tools::getValue($this->identifier) || ($field != 'passwd' && $field != 'no-picture'))
					$this->_errors[] = $this->l('the field').
						' <b>'.call_user_func(array($class_name, 'displayFieldName'), $field, $class_name).'</b> '.
						$this->l('is required');

		/* Checking for multilingual required fields */
		foreach ($rules['requiredLang'] as $field_lang)
			if (($empty = Tools::getValue($field_lang.'_'.$default_language->id)) === false || $empty !== '0' && empty($empty))
				$this->_errors[] = $this->l('the field').
					' <b>'.call_user_func(array($class_name, 'displayFieldName'), $field_lang, $class_name).'</b> '.
					$this->l('is required at least in').' '.$default_language->name;

		/* Checking for maximum fields sizes */
		foreach ($rules['size'] as $field => $max_length)
			if (Tools::getValue($field) !== false && Tools::strlen(Tools::getValue($field)) > $max_length)
				$this->_errors[] = $this->l('the field').
					' <b>'.call_user_func(array($class_name, 'displayFieldName'), $field, $class_name).'</b> '.
					$this->l('is too long').' ('.$max_length.' '.$this->l('chars max').')';

		/* Checking for maximum multilingual fields size */
		foreach ($rules['sizeLang'] as $field_lang => $max_length)
			foreach ($languages as $language)
			{
				$field_lang = Tools::getValue($field_lang.'_'.$language['id_lang']);
				if ($field_lang !== false && Tools::strlen($field_lang) > $max_length)
					$this->_errors[] = $this->l('the field').
						' <b>'.call_user_func(array($class_name, 'displayFieldName'), $field_lang, $class_name).' ('.$language['name'].')</b> '.
						$this->l('is too long').' ('.$max_length.' '.$this->l('chars max, html chars including').')';
			}
		/* Overload this method for custom checking */
		$this->_childValidation();

		/* Checking for fields validity */
		foreach ($rules['validate'] as $field => $function)
			if (($value = Tools::getValue($field)) !== false && ($field != 'passwd'))
				if (!Validate::$function($value) && !empty($value))
					$this->_errors[] = $this->l('the field').
						' <b>'.call_user_func(array($class_name, 'displayFieldName'), $field, $class_name).'</b> '.
						$this->l('is invalid');

		/* Checking for passwd_old validity */
		if (($value = Tools::getValue('passwd')) != false)
		{
			if ($class_name == 'Employee' && !Validate::isPasswdAdmin($value))
				$this->_errors[] = $this->l('the field').
					' <b>'.call_user_func(array($class_name, 'displayFieldName'), 'passwd', $class_name).'</b> '.
					$this->l('is invalid');
			else if ($class_name == 'Customer' && !Validate::isPasswd($value))
				$this->_errors[] = $this->l('the field').
					' <b>'.call_user_func(array($class_name, 'displayFieldName'), 'passwd', $class_name).
					'</b> '.$this->l('is invalid');
		}

		/* Checking for multilingual fields validity */
		foreach ($rules['validateLang'] as $field_lang => $function)
			foreach ($languages as $language)
				if (($value = Tools::getValue($field_lang.'_'.$language['id_lang'])) !== false && !empty($value))
					if (!Validate::$function($value))
						$this->_errors[] = $this->l('the field').
							' <b>'.call_user_func(array($class_name, 'displayFieldName'), $field_lang, $class_name).' ('.$language['name'].')</b> '.
							$this->l('is invalid');
	}

	/**
	 * Overload this method for custom checking
	 */
	protected function _childValidation(){}

	/**
	 * Display object details
	 */
	public function viewDetails(){}

	/**
	 * Called before deletion
	 *
	 * @param object $object Object
	 * @return boolean
	 */
	protected function beforeDelete($object)
	{
		return true;
	}

	/**
	 * Called before deletion
	 *
	 * @param object $object Object
	 * @return boolean
	 */
	protected function afterDelete($object, $oldId)
	{
		return true;
	}

	protected function afterAdd($object)
	{
		return true;
	}

	protected function afterUpdate($object)
	{
		return true;
	}

	/**
	 * Check rights to view the current tab
	 *
	 * @return boolean
	 */

	protected function afterImageUpload()
	{
		return true;
	}

	/**
	 * Copy datas from $_POST to object
	 *
	 * @param object &$object Object
	 * @param string $table Object table
	 */
	protected function copyFromPost(&$object, $table)
	{
		/* Classical fields */
		foreach ($_POST as $key => $value)
			if (key_exists($key, $object) && $key != 'id_'.$table)
			{
				/* Do not take care of password field if empty */
				if ($key == 'passwd' && Tools::getValue('id_'.$table) && empty($value))
					continue;
				/* Automatically encrypt password in MD5 */
				if ($key == 'passwd' && !empty($value))
					$value = Tools::encrypt($value);
				$object->{$key} = $value;
			}

		/* Multilingual fields */
		$rules = call_user_func(array(get_class($object), 'getValidationRules'), get_class($object));
		if (count($rules['validateLang']))
		{
			$languages = Language::getLanguages(false);
			foreach ($languages as $language)
				foreach (array_keys($rules['validateLang']) as $field)
					if (isset($_POST[$field.'_'.(int)$language['id_lang']]))
						$object->{$field}[(int)$language['id_lang']] = $_POST[$field.'_'.(int)$language['id_lang']];
		}
	}

	/**
	 * Returns an array with selected shops and type (group or boutique shop)
	 *
	 * @param string $table
	 * @param int $id_object
	 */
	protected static function getAssoShop($table, $id_object = false)
	{
		$shop_asso = Shop::getAssoTables();
		$group_shop_asso = GroupShop::getAssoTables();
		if (isset($shop_asso[$table]) && $shop_asso[$table]['type'] == 'shop')
			$type = 'shop';
		else if (isset($group_shop_asso[$table]) && $group_shop_asso[$table]['type'] == 'group_shop')
			$type = 'group_shop';
		else
			return;

		$assos = array();
		foreach ($_POST as $k => $row)
		{
			if (!preg_match('/^checkBox'.Tools::toCamelCase($type, true).'Asso_'.$table.'_([0-9]+)?_([0-9]+)$/Ui', $k, $res))
				continue;
			$id_asso_object = (!empty($res[1]) ? $res[1] : $id_object);
			$assos[] = array('id_object' => (int)$id_asso_object, 'id_'.$type => (int)$res[2]);
		}
		return array($assos, $type);
	}

	/**
	 * Update the associations of shops
	 *
	 * @param int $id_object
	 * @param int $new_id_object
	 */
	protected function updateAssoShop($id_object = false, $new_id_object = false)
	{
		if (!Shop::isFeatureActive())
			return;

		$shop_asso = Shop::getAssoTables();
		$group_shop_asso = GroupShop::getAssoTables();
		if (isset($shop_asso[$this->table]) && $shop_asso[$this->table]['type'] == 'shop')
			$type = 'shop';
		else if (isset($group_shop_asso[$this->table]) && $group_shop_asso[$this->table]['type'] == 'group_shop')
			$type = 'group_shop';
		else
			return;

		$assos = array();
		foreach ($_POST as $k => $row)
		{
			if (!preg_match('/^checkBox'.Tools::toCamelCase($type, true).'Asso_'.$this->table.'_([0-9]+)?_([0-9]+)$/Ui', $k, $res))
				continue;
			$id_asso_object = (!empty($res[1]) ? $res[1] : $id_object);
			$assos[] = array('id_object' => (int)$id_asso_object, 'id_'.$type => (int)$res[2]);
		}

		Db::getInstance()->execute('DELETE FROM '._DB_PREFIX_.$this->table.'_'.$type.($id_object ? ' WHERE `'.$this->identifier.'`='.(int)$id_object : ''));

		foreach ($assos as $asso)
			Db::getInstance()->execute('INSERT INTO '._DB_PREFIX_.$this->table.'_'.$type.' (`'.pSQL($this->identifier).'`, id_'.$type.')
											VALUES('.($new_id_object ? $new_id_object : (int)$asso['id_object']).', '.(int)$asso['id_'.$type].')');
	}

	protected function validateField($value, $field)
	{
		if (isset($field['validation']))
		{
			$valid_method_exists = method_exists('Validate', $field['validation']);
			if ((!isset($field['empty']) || !$field['empty'] || (isset($field['empty']) && $field['empty'] && $value)) && $valid_method_exists)
			{
				if (!Validate::$field['validation']($value))
				{
					$this->_errors[] = Tools::displayError($field['title'].' : Incorrect value');
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Can be overriden
	 */
	public function beforeUpdateOptions()
	{
	}

	/**
	 * displayAssoShop
	 * @todo : create assoshop.tpl and use smarty var asso_shop in pages
	 *
	 * @param string $type
	 * @return void
	 */
	protected function displayAssoShop($type = 'shop')
	{
		if (!Shop::isFeatureActive() || (!$this->object && $this->context->shop->getContextType() != Shop::CONTEXT_ALL))
			return;

		if ($type != 'shop' && $type != 'group_shop')
			$type = 'shop';

		$assos = array();
		$sql = 'SELECT id_'.$type.', `'.pSQL($this->identifier).'`
				FROM `'._DB_PREFIX_.pSQL($this->table).'_'.$type.'`';
		foreach (Db::getInstance()->executeS($sql) as $row)
			$assos[$row['id_'.$type]][] = $row[$this->identifier];

		$html = <<<EOF
			<script type="text/javascript">
			$().ready(function()
			{
				// Click on "all shop"
				$('.input_all_shop').click(function()
				{
					var checked = $(this).attr('checked');
					$('.input_group_shop').attr('checked', checked);
					$('.input_shop').attr('checked', checked);
				});

				// Click on a group shop
				$('.input_group_shop').click(function()
				{
					$('.input_shop[value='+$(this).val()+']').attr('checked', $(this).attr('checked'));
					check_all_shop();
				});

				// Click on a shop
				$('.input_shop').click(function()
				{
					check_group_shop_status($(this).val());
					check_all_shop();
				});

				// Initialize checkbox
				$('.input_shop').each(function(k, v)
				{
					check_group_shop_status($(v).val());
					check_all_shop();
				});
			});

			function check_group_shop_status(id_group)
			{
				var groupChecked = true;
				$('.input_shop[value='+id_group+']').each(function(k, v)
				{
					if (!$(v).attr('checked'))
						groupChecked = false;
				});
				$('.input_group_shop[value='+id_group+']').attr('checked', groupChecked);
			}

			function check_all_shop()
			{
				var allChecked = true;
				$('.input_group_shop').each(function(k, v)
				{
					if (!$(v).attr('checked'))
						allChecked = false;
				});
				$('.input_all_shop').attr('checked', allChecked);
			}
			</script>
EOF;

		$html .= '<div class="assoShop">';
		$html .= '<table class="table" cellpadding="0" cellspacing="0" width="100%">
					<tr><th>'.$this->l('Shop').'</th></tr>';
		$html .= '<tr'.(($type == 'group_shop') ? ' class="alt_row"' : '').'>
					<td>
						<label class="t">
							<input class="input_all_shop" type="checkbox" /> '.$this->l('All shops').'
						</label>
					</td>
				</tr>';
		foreach (Shop::getTree() as $group_id => $group_data)
		{
			$group_checked = ($type == 'group_shop' && ((isset($assos[$group_id]) && in_array($this->object->id, $assos[$group_id])) || !$this->object->id));
			$html .= '<tr'.(($type == 'shop') ? ' class="alt_row"' : '').'>';
			$html .= '<td>
						<img style="vertical-align: middle;" alt="" src="../img/admin/lv2_b.gif" />
						<label class="t">
							<input class="input_group_shop" type="checkbox"
								name="checkBoxGroupShopAsso_'.$this->table.'_'.$this->object->id.'_'.$group_id.'" value="'.$group_id.'" '.
								($group_checked ? 'checked="checked"' : '').' /> '.
							$group_data['name'].'
						</label>
					</td>';
			$html .= '</tr>';

			if ($type == 'shop')
			{
				$total = count($group_data['shops']);
				$j = 0;
				foreach ($group_data['shops'] as $shop_id => $shop_data)
				{
					$checked = ((isset($assos[$shop_id]) && in_array($this->object->id, $assos[$shop_id])) || !$this->object->id);
					$html .= '<tr>';
					$html .= '<td>
								<img style="vertical-align: middle;" alt="" src="../img/admin/lv3_'.(($j < $total - 1) ? 'b' : 'f').'.png" />
								<label class="child">';
					$html .= '<input class="input_shop" type="checkbox" value="'.$group_id.'"
									name="checkBoxShopAsso_'.$this->table.'_'.$this->object->id.'_'.$shop_id.'" id="checkedBox_'.$shop_id.'" '.
									($checked ? 'checked="checked"' : '').' /> ';
					$html .= $shop_data['name'].'</label></td>';
					$html .= '</tr>';
					$j++;
				}
			}
		}
		$html .= '</table></div>';
		$this->context->smarty->assign('asso_shop', $html);
		return $html;
	}



	/**
	 * Overload this method for custom checking
	 *
	 * @param integer $id Object id used for deleting images
	 * @return boolean
	 */
	protected function postImage($id)
	{
		if (isset($this->fieldImageSettings['name']) && isset($this->fieldImageSettings['dir']))
			return $this->uploadImage($id, $this->fieldImageSettings['name'], $this->fieldImageSettings['dir'].'/');
		else if (!empty($this->fieldImageSettings))
			foreach ($this->fieldImageSettings as $image)
				if (isset($image['name']) && isset($image['dir']))
					$this->uploadImage($id, $image['name'], $image['dir'].'/');
		return !count($this->_errors) ? true : false;
	}

	protected function uploadImage($id, $name, $dir, $ext = false, $width = null, $height = null)
	{
		if (isset($_FILES[$name]['tmp_name']) && !empty($_FILES[$name]['tmp_name']))
		{
			// Delete old image
			if (Validate::isLoadedObject($object = $this->loadObject()))
				$object->deleteImage();
			else
				return false;

			// Check image validity
			$max_size = isset($this->max_image_size) ? $this->max_image_size : 0;
			if ($error = checkImage($_FILES[$name], Tools::getMaxUploadSize($max_size)))
				$this->_errors[] = $error;
			else if (!$tmp_name = tempnam(_PS_TMP_IMG_DIR_, 'PS') || !move_uploaded_file($_FILES[$name]['tmp_name'], $tmp_name))
				return false;
			else
			{
				$tmp_name = $_FILES[$name]['tmp_name'];
				// Copy new image
				if (!imageResize($tmp_name, _PS_IMG_DIR_.$dir.$id.'.'.$this->imageType, (int)$width, (int)$height, ($ext ? $ext : $this->imageType)))
					$this->_errors[] = Tools::displayError('An error occurred while uploading image.');
				if (count($this->_errors))
					return false;
				if ($this->afterImageUpload())
				{
					unlink($tmp_name);
					return true;
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * Delete multiple items
	 *
	 * @param array $boxes ids of the item to be processed
	 * @return boolean true if succcess
	 */
	protected function processBulkDelete($token)
	{
		if (is_array($this->boxes) && !empty($this->boxes))
		{
			$object = new $this->className();
			if (isset($object->noZeroObject))
			{
				$objects_count = count(call_user_func(array($this->className, $object->noZeroObject)));

				// Check if all object will be deleted
				if ($objects_count <= 1 || count($this->boxes) == $objects_count)
					$this->_errors[] = Tools::displayError('You need at least one object.').
						' <b>'.$this->table.'</b><br />'.
						Tools::displayError('You cannot delete all of the items.');
			}
			else
			{
				$result = true;
				if ($this->deleted)
				{
					foreach ($this->boxes as $id)
					{
						$to_delete = new $this->className($id);
						$to_delete->deleted = 1;
						$result = $result && $to_delete->update();
					}
				}
				else
					$result = $object->deleteSelection(Tools::getValue($this->table.'Box'));

				if ($result)
					$this->redirect_after = self::$currentIndex.'&conf=2&token='.$token;
				$this->_errors[] = Tools::displayError('An error occurred while deleting selection.');
			}
		}
		else
			$this->_errors[] = Tools::displayError('You must select at least one element to delete.');

		return $result;
	}

	protected function processBulkaffectzone($token)
	{
		if (is_array($this->boxes) && !empty($this->boxes))
		{
			$object = new $this->className();
			$result = $object->affectZoneToSelection(Tools::getValue($this->table.'Box'), Tools::getValue('zone_to_affect'));

			if ($result)
				$this->redirect_after = self::$currentIndex.'&conf=28&token='.$token;
			$this->_errors[] = Tools::displayError('An error occurred while affecting a zone to the selection.');
		}
		else
			$this->_errors[] = Tools::displayError('You must select at least one element to affect a new zone.');

		return $result;
	}

	/**
	  * @TODO delete method after AdminProducts cleanup
	  * Display flags in forms for translations
	  *
	  * @param array $languages All languages available
	  * @param integer $default_language Default language id
	  * @param string $ids Multilingual div ids in form
	  * @param string $id Current div id]
	  * @param boolean $use_vars_instead_of_ids use an js vars instead of ids seperate by "¤"
	  *
		* @param return define the return way : false for a display, true for a return
		*
		*	@return string
	  */
	public function getTranslationsFlags($languages, $default_language, $ids, $id, $return = false, $use_vars_instead_of_ids = false)
	{
		if (count($languages) == 1)
			return false;
		$output = '
		<div class="displayed_flag">
			<img src="../img/l/'.$default_language.'.jpg" class="pointer" id="language_current_'.$id.'" onclick="toggleLanguageFlags(this);" alt="" />
		</div>
		<div id="languages_'.$id.'" class="language_flags">
			'.$this->l('Choose language:').'<br /><br />';
		foreach ($languages as $language)
			if ($use_vars_instead_of_ids)
				$output .= '<img src="../img/l/'.(int)$language['id_lang'].'.jpg" class="pointer" alt="'.$language['name'].'" title="'.$language['name'].'"
								onclick="changeLanguage(\''.$id.'\', '.$ids.', '.$language['id_lang'].', \''.$language['iso_code'].'\');" /> ';
			else
				$output .= '<img src="../img/l/'.(int)$language['id_lang'].'.jpg" class="pointer" alt="'.$language['name'].'" title="'.$language['name'].'"
								onclick="changeLanguage(\''.$id.'\', \''.$ids.'\', '.$language['id_lang'].', \''.$language['iso_code'].'\');" /> ';
		$output .= '</div>';

		if ($return)
			return $output;

		return $output;
	}

	/**
	 * Called before Add
	 *
	 * @param object $object Object
	 * @return boolean
	 */
	protected function beforeAdd($object)
	{
		return true;
	}
}
