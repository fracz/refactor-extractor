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

	public $_languages = array();
	public $default_form_language;
	public $allow_employee_form_lang;

	public $content_only = false;
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

	/** @var string Security token */
	public $token;

	protected $_object;

	/** @var string shop | group_shop */
	public $shopLinkType;

	/** @var string Default ORDER BY clause when $_orderBy is not defined */
	protected $_defaultOrderBy = false;

	/** @var array Errors displayed after post processing */
	public $_errors = array();

	protected $list_display;

	/** @var array list of option forms to be generated */
	protected $options;

	protected $_listSkipDelete = array();

	protected $shopLink;

	/** @var array Cache for query results */
	protected $_list = array();

	/** @var integer Number of results in list */
	protected $_listTotal = 0;

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

	/** @var array list of available actions for each list row */
	protected $actions_available = array('view', 'edit', 'delete', 'duplicate');

	/** @var array list of required actions for each list row */
	protected $actions = array();

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

	protected $view;
	protected $edit;
	protected $delete;
	protected $duplicate;
	protected $deleted;
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
	protected $tpl_folder;

	/** @var bool Redirect or not ater a creation */
	protected $_redirect = true;

	/** @var array Name and directory where class image are located */
	public $fieldImageSettings = array();

	/** @var string Image type */
	public $imageType = 'jpg';

	public function __construct()
	{
	// retro-compatibility : className for admin without controller
	// This can be overriden in controllers (like for AdminCategories or AdminProducts
		$controller = get_class($this);
		// @todo : move this in class AdminCategoriesController and AdminProductsController
		if ($controller == 'AdminCategoriesController' && $controller == 'AdminProductsController')
			$controller = 'AdminCatalogController';

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
		);
		if (!$this->identifier) $this->identifier = 'id_'.$this->table;
		if (!$this->_defaultOrderBy) $this->_defaultOrderBy = $this->identifier;
		$this->tabAccess = Profile::getProfileAccess($this->context->employee->id_profile, $this->id);

		// Fix for AdminHome
		if ($controller == 'AdminHome')
			$_POST['token'] = $this->token;

		if (!Shop::isMultiShopActivated())
			$this->shopLinkType = '';

		// Get the name of the folder containing the custom tpl files
		$this->tpl_folder = strtolower($controller[5]).substr($controller, 6);
		$this->tpl_folder = Tools::toUnderscoreCase($this->tpl_folder).'/';
	}

	/**
	 * Check rights to view the current tab
	 *
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

	public function postProcess()
	{
		if ($this->ajax)
		{
			// from ajax-tab.php
			if (method_exists($this, 'ajaxPreprocess'))
				$this->ajaxPreProcess();

			$action = Tools::getValue('action');
			// no need to use displayConf() here
			if (!empty($action) && method_exists($this, 'ajaxProcess'.Tools::toCamelCase($action)))
				$this->{'ajaxProcess'.Tools::toCamelCase($action)}();
			else
				$this->ajaxProcess();

			// @TODO We should use a displayAjaxError
			/*$this->displayErrors();
			if (!empty($action) && method_exists($this, 'displayAjax'.Tools::toCamelCase($action)))
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

			// Sub included tab postProcessing
			$this->includeSubTab('postProcess', array('status', 'submitAdd1', 'submitDel', 'delete', 'submitFilter', 'submitReset'));
			switch ($this->action)
			{
				/* Delete object image */
				case 'delete_image':
					if (Validate::isLoadedObject($object = $this->loadObject()))
						if (($object->deleteImage()))
							Tools::redirectAdmin(self::$currentIndex.'&add'.$this->table.'&'.$this->identifier.'='.Tools::getValue($this->identifier).'&conf=7&token='.$token);
					$this->_errors[] = Tools::displayError('An error occurred during image deletion (cannot load object).');
					break;
				/* Delete object */
				case 'delete':
					if (Validate::isLoadedObject($object = $this->loadObject()) && isset($this->fieldImageSettings))
					{
						// check if request at least one object with noZeroObject
						if (isset($object->noZeroObject) && count(call_user_func(array($this->className, $object->noZeroObject))) <= 1)
							$this->_errors[] = Tools::displayError('You need at least one object.').' <b>'.$this->table.'</b><br />'.Tools::displayError('You cannot delete all of the items.');
						else
						{
							if ($this->deleted)
							{
								$object->deleteImage();
								$object->deleted = 1;
								if ($object->update())
									Tools::redirectAdmin(self::$currentIndex.'&conf=1&token='.$token);
							}
							else if ($object->delete())
							{
								if (method_exists($object, 'cleanPositions'))
									$object->cleanPositions();
								Tools::redirectAdmin(self::$currentIndex.'&conf=1&token='.$token);
							}
							$this->_errors[] = Tools::displayError('An error occurred during deletion.');
						}
					}
					else
						$this->_errors[] = Tools::displayError('An error occurred while deleting object.').' <b>'.$this->table.'</b> '.Tools::displayError('(cannot load object)');
					break;

				/* Change object statuts (active, inactive) */
				case 'status':
					if (Validate::isLoadedObject($object = $this->loadObject()))
					{
						if ($object->toggleStatus())
							Tools::redirectAdmin(self::$currentIndex.'&conf=5'.((($id_category = (int)(Tools::getValue('id_category'))) && Tools::getValue('id_product')) ? '&id_category='.$id_category : '').'&token='.$token);
						else
							$this->_errors[] = Tools::displayError('An error occurred while updating status.');
					}
					else
						$this->_errors[] = Tools::displayError('An error occurred while updating status for object.').' <b>'.$this->table.'</b> '.Tools::displayError('(cannot load object)');
					break;

				/* Move an object */
				case 'position':
					if (!Validate::isLoadedObject($object = $this->loadObject()))
						$this->_errors[] = Tools::displayError('An error occurred while updating status for object.').' <b>'.$this->table.'</b> '.Tools::displayError('(cannot load object)');
					else if (!$object->updatePosition((int)(Tools::getValue('way')), (int)(Tools::getValue('position'))))
						$this->_errors[] = Tools::displayError('Failed to update the position.');
					else
						Tools::redirectAdmin(self::$currentIndex.'&'.$this->table.'Orderby=position&'.$this->table.'Orderway=asc&conf=5'.(($id_category = (int)(Tools::getValue($this->identifier))) ? ('&'.$this->identifier.'='.$id_category) : '').'&token='.$token);
						Tools::redirectAdmin(self::$currentIndex.'&'.$this->table.'Orderby=position&'.$this->table.'Orderway=asc&conf=5'.((($id_category = (int)(Tools::getValue('id_category'))) && Tools::getValue('id_product')) ? '&id_category='.$id_category : '').'&token='.$token);
					break;

				/* Delete multiple objects */
				case 'multiple_delete':
					if (isset($_POST[$this->table.'Box']))
					{
						$object = new $this->className();
						if (isset($object->noZeroObject) &&
							// Check if all object will be deleted
							(count(call_user_func(array($this->className, $object->noZeroObject))) <= 1 || count($_POST[$this->table.'Box']) == count(call_user_func(array($this->className, $object->noZeroObject)))))
							$this->_errors[] = Tools::displayError('You need at least one object.').' <b>'.$this->table.'</b><br />'.Tools::displayError('You cannot delete all of the items.');
						else
						{
							$result = true;
							if ($this->deleted)
							{
								foreach (Tools::getValue($this->table.'Box') as $id)
								{
									$to_delete = new $this->className($id);
									$to_delete->deleted = 1;
									$result = $result && $to_delete->update();
								}
							}
							else
								$result = $object->deleteSelection(Tools::getValue($this->table.'Box'));

							if ($result)
								Tools::redirectAdmin(self::$currentIndex.'&conf=2&token='.$token);
							$this->_errors[] = Tools::displayError('An error occurred while deleting selection.');
						}
					}
					else
						$this->_errors[] = Tools::displayError('You must select at least one element to delete.');
					break;

				/* Create or update an object */
				case 'save':
					/* Checking fields validity */
					$this->validateRules();
					if (!count($this->_errors))
					{
						$id = (int)(Tools::getValue($this->identifier));

						/* Object update */
						if (isset($id) && !empty($id))
						{
							if ($this->tabAccess['edit'] === '1' || ($this->table == 'employee' && $this->context->employee->id == Tools::getValue('id_employee') && Tools::isSubmit('updateemployee')))
							{
								$object = new $this->className($id);
								if (Validate::isLoadedObject($object))
								{
									/* Specific to objects which must not be deleted */
									if ($this->deleted && $this->beforeDelete($object))
									{
										// Create new one with old objet values
										$objectNew = new $this->className($object->id);
										$objectNew->id = null;
										$objectNew->date_add = '';
										$objectNew->date_upd = '';

										// Update old object to deleted
										$object->deleted = 1;
										$object->update();

										// Update new object with post values
										$this->copyFromPost($objectNew, $this->table);
										$result = $objectNew->add();
										if (Validate::isLoadedObject($objectNew))
											$this->afterDelete($objectNew, $object->id);
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
										$this->_errors[] = Tools::displayError('An error occurred while updating object.').' <b>'.$this->table.'</b> ('.Db::getInstance()->getMsgError().')';
									else if ($this->postImage($object->id) && !count($this->_errors))
									{
										$parent_id = (int)(Tools::getValue('id_parent', 1));
										// Specific back redirect
										if ($back = Tools::getValue('back'))
											Tools::redirectAdmin(urldecode($back).'&conf=4');
										// Specific scene feature
										if (Tools::getValue('stay_here') == 'on' || Tools::getValue('stay_here') == 'true' || Tools::getValue('stay_here') == '1')
											Tools::redirectAdmin(self::$currentIndex.'&'.$this->identifier.'='.$object->id.'&conf=4&updatescene&token='.$token);
										// Save and stay on same form
										if (Tools::isSubmit('submitAdd'.$this->table.'AndStay'))
											Tools::redirectAdmin(self::$currentIndex.'&'.$this->identifier.'='.$object->id.'&conf=4&update'.$this->table.'&token='.$token);
										// Save and back to parent
										if (Tools::isSubmit('submitAdd'.$this->table.'AndBackToParent'))
											Tools::redirectAdmin(self::$currentIndex.'&'.$this->identifier.'='.$parent_id.'&conf=4&token='.$token);
										// Default behavior (save and back)
										Tools::redirectAdmin(self::$currentIndex.($parent_id ? '&'.$this->identifier.'='.$object->id : '').'&conf=4&token='.$token);
									}
								}
								else
									$this->_errors[] = Tools::displayError('An error occurred while updating object.').' <b>'.$this->table.'</b> '.Tools::displayError('(cannot load object)');
							}
							else
								$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
						}

						/* Object creation */
						else
						{
							if ($this->tabAccess['add'] === '1')
							{
								$object = new $this->className();
								$this->copyFromPost($object, $this->table);
								if (!$object->add())
									$this->_errors[] = Tools::displayError('An error occurred while creating object.').' <b>'.$this->table.' ('.Db::getInstance()->getMsgError().')</b>';
								else if (($_POST[$this->identifier] = $object->id /* voluntary */) && $this->postImage($object->id) && !count($this->_errors) && $this->_redirect)
								{
									$parent_id = (int)(Tools::getValue('id_parent', 1));
									$this->afterAdd($object);
									$this->updateAssoShop($object->id);
									// Save and stay on same form
									if (Tools::isSubmit('submitAdd'.$this->table.'AndStay'))
										Tools::redirectAdmin(self::$currentIndex.'&'.$this->identifier.'='.$object->id.'&conf=3&update'.$this->table.'&token='.$token);
									// Save and back to parent
									if (Tools::isSubmit('submitAdd'.$this->table.'AndBackToParent'))
										Tools::redirectAdmin(self::$currentIndex.'&'.$this->identifier.'='.$parent_id.'&conf=3&token='.$token);
									// Default behavior (save and back)
									Tools::redirectAdmin(self::$currentIndex.($parent_id ? '&'.$this->identifier.'='.$object->id : '').'&conf=3&token='.$token);
								}
							}
							else
								$this->_errors[] = Tools::displayError('You do not have permission to add here.');
						}
					}
					$this->_errors = array_unique($this->_errors);
					break;

				/* Cancel all filters for this tab */
				case 'reset_filters':
					$filters = $this->context->cookie->getFamily($this->table.'Filter_');
					foreach ($filters as $cookieKey => $filter)
						if (strncmp($cookieKey, $this->table.'Filter_', 7 + Tools::strlen($this->table)) == 0)
							{
								$key = substr($cookieKey, 7 + Tools::strlen($this->table));
								/* Table alias could be specified using a ! eg. alias!field */
								$tmpTab = explode('!', $key);
								$key = (count($tmpTab) > 1 ? $tmpTab[1] : $tmpTab[0]);
								if (array_key_exists($key, $this->fieldsDisplay))
									unset($this->context->cookie->$cookieKey);
							}
					if (isset($this->context->cookie->{'submitFilter'.$this->table}))
						unset($this->context->cookie->{'submitFilter'.$this->table});
					if (isset($this->context->cookie->{$this->table.'Orderby'}))
						unset($this->context->cookie->{$this->table.'Orderby'});
					if (isset($this->context->cookie->{$this->table.'Orderway'}))
						unset($this->context->cookie->{$this->table.'Orderway'});
					unset($_POST);
					$this->filter = false;
					break;

				/* Submit options list */
				case 'update_options':
					$this->updateOptions($token);
					break;

				case 'update_fields':
					if (!is_array($fields = Tools::getValue('fieldsBox')))
						$fields = array();

					$object = new $this->className();
					if (!$object->addFieldsRequiredDatabase($fields))
						$this->_errors[] = Tools::displayError('Error in updating required fields');
					else
						Tools::redirectAdmin(self::$currentIndex.'&conf=4&token='.$token);
					break;

				default:
					if (method_exists($this, $this->action))
						call_user_func(array($this, $this->action), $this->boxes);
			}
			/* Manage list filtering */
			if ($this->filter)
			{
				$_POST = array_merge($this->context->cookie->getFamily($this->table.'Filter_'), (isset($_POST) ? $_POST : array()));
				foreach ($_POST as $key => $value)
				{
					/* Extracting filters from $_POST on key filter_ */
					if ($value != null && !strncmp($key, $this->table.'Filter_', 7 + Tools::strlen($this->table)))
					{
						$key = Tools::substr($key, 7 + Tools::strlen($this->table));
						/* Table alias could be specified using a ! eg. alias!field */
						$tmpTab = explode('!', $key);
						$filter = count($tmpTab) > 1 ? $tmpTab[1] : $tmpTab[0];
						if ($field = $this->filterToField($key, $filter))
						{
							$type = (array_key_exists('filter_type', $field) ? $field['filter_type'] : (array_key_exists('type', $field) ? $field['type'] : false));
							if (($type == 'date' || $type == 'datetime') && is_string($value))
								$value = unserialize($value);
							$key = isset($tmpTab[1]) ? $tmpTab[0].'.`'.$tmpTab[1].'`' : '`'.$tmpTab[0].'`';
							if (array_key_exists('tmpTableFilter', $field))
								$sqlFilter = & $this->_tmpTableFilter;
							else if (array_key_exists('havingFilter', $field))
								$sqlFilter = & $this->_filterHaving;
							else
								$sqlFilter = & $this->_filter;

							/* Only for date filtering (from, to) */
							if (is_array($value))
							{
								if (isset($value[0]) && !empty($value[0]))
								{
									if (!Validate::isDate($value[0]))
										$this->_errors[] = Tools::displayError('\'from:\' date format is invalid (YYYY-MM-DD)');
									else
										$sqlFilter .= ' AND `'.bqSQL($key).'` >= \''.pSQL(Tools::dateFrom($value[0])).'\'';
								}

								if (isset($value[1]) && !empty($value[1]))
								{
									if (!Validate::isDate($value[1]))
										$this->_errors[] = Tools::displayError('\'to:\' date format is invalid (YYYY-MM-DD)');
									else
										$sqlFilter .= ' AND `'.bqSQL($key).'` <= \''.pSQL(Tools::dateTo($value[1])).'\'';
								}
							}
							else
							{
								$sqlFilter .= ' AND ';
								if ($type == 'int' || $type == 'bool')
									$sqlFilter .= (($key == $this->identifier || $key == '`'.$this->identifier.'`' || $key == '`active`') ? 'a.' : '').pSQL($key).' = '.(int)($value).' ';
								else if ($type == 'decimal')
									$sqlFilter .= (($key == $this->identifier || $key == '`'.$this->identifier.'`') ? 'a.' : '').pSQL($key).' = '.(float)($value).' ';
								else if ($type == 'select')
									$sqlFilter .= (($key == $this->identifier || $key == '`'.$this->identifier.'`') ? 'a.' : '').pSQL($key).' = \''.pSQL($value).'\' ';
								else
									$sqlFilter .= (($key == $this->identifier || $key == '`'.$this->identifier.'`') ? 'a.' : '').pSQL($key).' LIKE \'%'.pSQL($value).'%\' ';
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Display form
	 */
	public function displayForm($firstCall = true)
	{
		$content = '';
		$allowEmployeeFormLang = Configuration::get('PS_BO_ALLOW_EMPLOYEE_FORM_LANG') ? Configuration::get('PS_BO_ALLOW_EMPLOYEE_FORM_LANG') : 0;
		if ($allowEmployeeFormLang && !$this->context->cookie->employee_form_lang)
			$this->context->cookie->employee_form_lang = (int)(Configuration::get('PS_LANG_DEFAULT'));
		$useLangFromCookie = false;
		$this->_languages = Language::getLanguages(false);
		if ($allowEmployeeFormLang)
			foreach ($this->_languages as $lang)
				if ($this->context->cookie->employee_form_lang == $lang['id_lang'])
					$useLangFromCookie = true;
		if (!$useLangFromCookie)
			$this->_defaultFormLanguage = (int)(Configuration::get('PS_LANG_DEFAULT'));
		else
			$this->_defaultFormLanguage = (int)($this->context->cookie->employee_form_lang);

		// Only if it is the first call to displayForm, otherwise it has already been defined
		if ($firstCall)
		{
			$content .='
			<script type="text/javascript">
				$(document).ready(function() {
					id_language = '.$this->_defaultFormLanguage.';
					languages = new Array();';
			foreach ($this->_languages as $k => $language)
				$content .= '
					languages['.$k.'] = {
						id_lang: '.(int)$language['id_lang'].',
						iso_code: \''.$language['iso_code'].'\',
						name: \''.htmlentities($language['name'], ENT_COMPAT, 'UTF-8').'\'
					};';
			$content .= '
					displayFlags(languages, id_language, '.$allowEmployeeFormLang.');
				});
			</script>';
		}
		return $content;
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
		if ($id = (int)(Tools::getValue($this->identifier)) AND Validate::isUnsignedId($id))
		{
			if (!$this->_object)
				$this->_object = new $this->className($id);
			if (Validate::isLoadedObject($this->_object))
				return $this->_object;
			$this->_errors[] = Tools::displayError('Object cannot be loaded (not found)');
		}
		else if ($opt)
		{
			$this->_object = new $this->className();
			return $this->_object;
		}
		else
			$this->_errors[] = Tools::displayError('Object cannot be loaded (identifier missing or invalid)');

		$this->content = $this->displayErrors();
	}

	/**
	 * Check if the token is valid, else display a warning page
	 */
	public function checkAccess()
	{
		if (!$this->checkToken())
		{
			// If this is an XSS attempt, then we should only display a simple, secure page
			// ${1} in the replacement string of the regexp is required, because the token may begin with a number and mix up with it (e.g. $17)
			$url = preg_replace('/([&?]token=)[^&]*(&.*)?$/', '${1}'.$this->token.'$2', $_SERVER['REQUEST_URI']);
			if (false === strpos($url, '?token=') && false === strpos($url, '&token='))
				$url .= '&token='.$this->token;
			if (strpos($url, '?') === false)
				$url = str_replace('&token', '?controller=AdminHome&token', $url);

			$this->context->smarty->assign('url', htmlentities($url));
			$this->context->smarty->display('invalid_token.tpl');
		}
	}

	/**
	 * @TODO
	 */
	public function includeSubTab($methodname, $actions = array())
	{
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
		echo $this->content;
	}

	public function display()
	{
		$this->context->smarty->assign('content', $this->content);
		$this->context->smarty->assign('meta_title', $this->meta_title);

		// Template override
		$tpl = $this->tpl_folder.'content.tpl';
		$tpl_action = $this->tpl_folder.$this->display.'.tpl';
		// Check if action template has been override

		if (file_exists($this->context->smarty->template_dir.'/'.$tpl_action))
			$this->context->smarty->assign('content', $this->context->smarty->fetch($tpl_action));

		// Check if content template has been override
		if (file_exists($this->context->smarty->template_dir.'/'.$tpl))
			$page = $this->context->smarty->fetch($tpl);
		else
			$page = $this->context->smarty->fetch($this->template);

		if ($this->content_only)
			echo $page;
		else
		{
			if ($conf = Tools::getValue('conf'))
				$this->context->smarty->assign('conf', $this->_conf[(int)($conf)]);

			$this->context->smarty->assign('errors', $this->_errors);
			$this->context->smarty->assign('warnings', $this->warnings);
			$this->context->smarty->assign('informations', $this->informations);
		}

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
		if (Shop::isMultiShopActivated())
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
				$youEditFieldFor = sprintf($this->l('A modification of this field will be applied for the shop %s'), '<b>'.Context::getContext()->shop->name.'</b>');
		}

			// Multishop
		$is_multishop = Shop::isMultiShopActivated();// && Context::shop() != Shop::CONTEXT_ALL;
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
			preg_match('/tab=(.+)(&.+)?$/', $quick['link'], $adminTab);
			if (isset($adminTab[1]))
			{
				if (strpos($adminTab[1], '&'))
					$adminTab[1] = substr($adminTab[1], 0, strpos($adminTab[1], '&'));
				$quick_access[$index]['link'] .= '&token='.Tools::getAdminToken($adminTab[1].(int)(Tab::getIdFromClassName($adminTab[1])).(int)($this->context->employee->id));
			}
		}

		// Tab list
		$tabs = Tab::getTabs($this->context->language->id, 0);
		foreach ($tabs as $index => $tab)
		{
			if (Tab::checkTabRights($tab['id_tab']) === true)
			{
				$img_exists_cache = Tools::file_exists_cache(_PS_ADMIN_DIR_.'/themes/'.$this->context->employee->bo_theme.'/img/t/'.$tab['class_name'].'.gif');
				$img = ($img_exists_cache ? 'themes/'.Context::getContext()->employee->bo_theme.'/img/' : _PS_IMG_).'t/'.$tab['class_name'].'.gif';

				if (trim($tab['module']) != '')
					$img = _MODULE_DIR_.$tab['module'].'/'.$tab['class_name'].'.gif';

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
			}
			else
				unset($tabs[$index]);
		}
		// Breadcrumbs
		$home_token = Tools::getAdminToken('AdminHome'.intval(Tab::getIdFromClassName('AdminHome')).(int)$this->context->employee->id);
		$tabs_breadcrumb = array();
		$tabs_breadcrumb = Tab::recursiveTab($this->id, $tabs_breadcrumb);
		$tabs_breadcrumb = array_reverse($tabs_breadcrumb);

		foreach ($tabs_breadcrumb as $key => $item)
			for ($i = 0; $i < (count($tabs_breadcrumb) - 1); $i++)
				$tabs_breadcrumb[$key]['token'] = Tools::getAdminToken($item['class_name'].intval($item['id_tab']).(int)$this->context->employee->id);


		/* Hooks are volontary out the initialize array (need those variables already assigned) */
		$this->context->smarty->assign(array(
			'img_dir' => _PS_IMG_,
			'iso' => $this->context->language->iso_code,
			'class_name' => $this->className,
			'iso_user' => $this->context->language->id,
			'country_iso_code' => $this->context->country->iso_code,
			'version' => _PS_VERSION_,
			'help_box' => Configuration::get('PS_HELPBOX'),
			'round_mode' => Configuration::get('PS_PRICE_ROUND_MODE'),
			'brightness' => Tools::getBrightness(empty($this->context->employee->bo_color) ? '#FFFFFF' : $this->context->employee->bo_color) < 128 ? 'white' : '#383838',
			'edit_field' => isset($youEditFieldFor) ? $youEditFieldFor : '\'\'',
			'lang_iso' => $this->context->language->iso_code,
			'link' => $this->context->link,
			'bo_color' => isset($this->context->employee->bo_color) ? Tools::htmlentitiesUTF8($this->context->employee->bo_color) : null,
			'shop_name' => Configuration::get('PS_SHOP_NAME'),
			'show_new_orders' => Configuration::get('PS_SHOW_NEW_ORDERS'),
			'show_new_customers' => Configuration::get('PS_SHOW_NEW_CUSTOMERS'),
			'show_new_messages' => Configuration::get('PS_SHOW_NEW_MESSAGES'),
			'token_admin_orders' => Tools::getAdminTokenLite('AdminOrders'),
			'token_admin_customers' => Tools::getAdminTokenLite('AdminCustomers'),
			'token_admin_messages' => Tools::getAdminTokenLite('AdminMessages'),
			'token_admin_employees' => Tools::getAdminTokenLite('AdminEmployees'),
			'token_admin_search' => Tools::getAdminTokenLite('AdminSearch'),
			'first_name' => Tools::substr($this->context->employee->firstname, 0, 1),
			'last_name' => htmlentities($this->context->employee->lastname, ENT_COMPAT, 'UTF-8'),
			'base_url' => $this->context->shop->getBaseURL(),
			'employee' => $this->context->employee,
			'search_type' => Tools::getValue('bo_search_type'),
			'bo_query' => Tools::safeOutput(Tools::stripslashes(Tools::getValue('bo_query'))),
			'quick_access' => $quick_access,
			'multi_shop' => Shop::isMultiShopActivated(),
			'shop_list' => (Shop::isMultiShopActivated() ? generateShopList() : null), //@TODO refacto
			'tab' => $tab,
			'current_parent_id' => (int)Tab::getCurrentParentId(),
			'tabs' => $tabs,
			'install_dir_exists' => file_exists(_PS_ADMIN_DIR_.'/../install'),
			'home_token' => $home_token,
			'tabs_breadcrumb' => $tabs_breadcrumb,
			'is_multishop' => $is_multishop,

		));
		$this->context->smarty->assign(array(
			'HOOK_HEADER' => Module::hookExec('backOfficeHeader'),
			'HOOK_TOP' => Module::hookExec('backOfficeTop'),
		));
	}

	/**
	 * Declare an action to use for each row in the list
	 */
	public function addRowAction($action)
	{
		$this->actions[] = $action;
	}

	/**
	 * Assign smarty variables for the page main content
	 */
	public function initContent()
	{
		$this->context->smarty->assign(array(
			'current' => self::$currentIndex,
			'token' => $this->token
		));

		if ($this->display == 'edit' || $this->display == 'add')
		{
			if (!($obj = $this->loadObject(true)))
				return;

			if (isset($this->fields_form))
			{
				$this->getlanguages();
				$helper = new HelperForm();
				// Check if form template has been overriden
				if (file_exists($this->context->smarty->template_dir.'/'.$this->tpl_folder.'form.tpl'))
					$helper->tpl = $this->tpl_folder.'form.tpl';
				$helper->currentIndex = self::$currentIndex;
				$helper->token = $this->token;
				$helper->table = $this->table;
				$helper->id = $obj->id;
				$helper->languages = $this->_languages;
				$helper->default_form_language = $this->default_form_language;
				$helper->allow_employee_form_lang = $this->allow_employee_form_lang;
				$helper->fields_value = $this->getFieldsValue($obj);
				$this->content .= $helper->generateForm($this->fields_form);
			}
			else
			// TODO delete when all forms use the helper
				$this->content .= $this->displayForm();

			if ($this->tabAccess['view'])
			{
				if (Tools::getValue('back'))
					$this->context->smarty->assign('back', Tools::safeOutput(Tools::getValue('back')));
				else
					$this->context->smarty->assign('back', Tools::safeOutput(Tools::getValue(self::$currentIndex.'&token='.$this->token)));
			}
		}
		else if ($this->display == 'list')
		{
			$this->getList($this->context->language->id);

			$helper = new HelperList();
			// Check if list templates have been overriden
			if (file_exists($this->context->smarty->template_dir.'/'.$this->tpl_folder.'list_header.tpl'))
				$helper->header_tpl = $this->tpl_folder.'list_header.tpl';
			if (file_exists($this->context->smarty->template_dir.'/'.$this->tpl_folder.'list_content.tpl'))
				$helper->header_tpl = $this->tpl_folder.'list_content.tpl';
			if (file_exists($this->context->smarty->template_dir.'/'.$this->tpl_folder.'list_footer.tpl'))
				$helper->header_tpl = $this->tpl_folder.'list_footer.tpl';

			// For compatibility reasons, we have to check standard actions in class attributes
			foreach ($this->actions_available as $action)
			{
				if (!in_array($action, $this->actions) && isset($this->$action) && $this->$action)
					$this->actions[] = $action;
			}

			$helper->actions = $this->actions;
			$helper->bulk_actions = $this->bulk_actions;
			$helper->currentIndex = self::$currentIndex;
			$helper->className = $this->className;
			$helper->table = $this->table;
			$helper->shopLink = $this->shopLink;
			$helper->shopLinkType = $this->shopLinkType;
			$helper->identifier = $this->identifier;
			$helper->token = $this->token;
			$helper->imageType = $this->imageType;
			$helper->no_add = isset($this->no_add) ? $this->no_add : false;
			$helper->colorOnBackground = $this->colorOnBackground;

			// For each action, try to add the corresponding skip elements list
			foreach ($this->actions as $action)
			{
				$skip_attribute = '_listSkip'.ucfirst($action);
				if (isset($this->$skip_attribute))
					$helper->$skip_attribute = $this->$skip_attribute;
			}

			$this->content .= $helper->generateList($this->_list, $this->fieldsDisplay);
		}
		else if ($this->display == 'options')
		{
			$helper = new HelperOptions();
			$this->content .= $helper->generateOptions();
		}
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
			'HOOK_FOOTER' => Module::hookExec('backOfficeFooter'),
		));
	}

	public function setMedia()
	{
		$this->addCSS(_PS_CSS_DIR_.'admin.css', 'all');
		$this->addCSS(__PS_BASE_URI__.str_replace(_PS_ROOT_DIR_.DIRECTORY_SEPARATOR, '', _PS_ADMIN_DIR_).'/themes/default/admin.css', 'all');
		if ($this->context->language->is_rtl)
			$this->addCSS(_THEME_CSS_DIR_.'rtl.css');

		$this->addJquery();
		$this->addjQueryPlugin(array('cluetip', 'hoverIntent'));

		$this->addJS(array(
			_PS_JS_DIR_.'admin.js',
			_PS_JS_DIR_.'toggle.js',
			_PS_JS_DIR_.'tools.js',
			_PS_JS_DIR_.'ajax.js',
			_PS_JS_DIR_.'notifications.js'
		));
	}

	public static function translate($string, $class, $addslashes = false, $htmlentities = true)
	{
		$class = strtolower($class);
		// if the class is extended by a module, use modules/[module_name]/xx.php lang file
		//$currentClass = get_class($this);
		if (false && Module::getModuleNameFromClass($class))
		{
			$string = str_replace('\'', '\\\'', $string);
			return Module::findTranslation(Module::$classInModule[$class], $string, $class);
		}
		global $_LANGADM;
		if (is_array($_LANGADM))
			$_LANGADM = array_change_key_case($_LANGADM);
		else
			$_LANGADM = array();

        //if ($class == __CLASS__)
        //        $class = 'AdminTab';

		$key = md5(str_replace('\'', '\\\'', $string));

		$str = (key_exists($class.$key, $_LANGADM)) ? $_LANGADM[$class.$key] : ((key_exists($class.$key, $_LANGADM)) ? $_LANGADM[$class.$key] : $string);
		$str = $htmlentities ? htmlentities($str, ENT_QUOTES, 'utf-8') : $str;
		return str_replace('"', '&quot;', ($addslashes ? addslashes($str) : stripslashes($str)));
	}

	/**
	 * use translations files to replace english expression.
	 *
	 * @param mixed $string term or expression in english
	 * @param string $class
	 * @param boolan $addslashes if set to true, the return value will pass through addslashes(). Otherwise, stripslashes().
	 * @param boolean $htmlentities if set to true(default), the return value will pass through htmlentities($string, ENT_QUOTES, 'utf-8')
	 * @return string the translation if available, or the english default text.
	 */
	protected function l($string, $class = 'AdminTab', $addslashes = false, $htmlentities = true)
	{
		$class = get_class($this);
		return self::translate($string, $class, $addslashes, $htmlentities);
	}

	/**
	 * Init context and dependencies, handles POST and GET
	 */
	public function init()
	{
		// ob_start();
		if (Tools::getValue('ajax'))
			$this->ajax = '1';
		$this->checkAccess();
		$this->timerStart = microtime(true);

		if (isset($_GET['logout']))
			$this->context->employee->logout();

		if (!isset($this->context->employee) || !$this->context->employee->isLoggedBack())
			Tools::redirectAdmin('login.php?redirect='.$_SERVER['REQUEST_URI']);

		// Set current index
		$currentIndex = $_SERVER['SCRIPT_NAME'].(($controller = Tools::getValue('controller')) ? '?controller='.$controller : '');

		if ($back = Tools::getValue('back'))
			$currentIndex .= '&back='.urlencode($back);
		self::$currentIndex = $currentIndex;
		$iso = $this->context->language->iso_code;
		include(_PS_TRANSLATIONS_DIR_.$iso.'/errors.php');
		include(_PS_TRANSLATIONS_DIR_.$iso.'/fields.php');
		include(_PS_TRANSLATIONS_DIR_.$iso.'/admin.php');

		/* Server Params */
		$protocol_link = (Configuration::get('PS_SSL_ENABLED')) ? 'https://' : 'http://';
		$protocol_content = (isset($useSSL) && $useSSL && Configuration::get('PS_SSL_ENABLED')) ? 'https://' : 'http://';
		$link = new Link($protocol_link, $protocol_content);
		$this->context->link = $link;
		//define('_PS_BASE_URL_', Tools::getShopDomain(true));
		//define('_PS_BASE_URL_SSL_', Tools::getShopDomainSsl(true));

		$this->context->currency = new Currency(Configuration::get('PS_CURRENCY_DEFAULT'));

		// Change shop context ?
		if (Shop::isMultiShopActivated() && Tools::getValue('setShopContext') !== false)
		{
			$this->context->cookie->shopContext = Tools::getValue('setShopContext');
			$url = parse_url($_SERVER['REQUEST_URI']);
			$query = (isset($url['query'])) ? $url['query'] : '';
			parse_str($query, $parseQuery);
			unset($parseQuery['setShopContext']);
			Tools::redirectAdmin($url['path'].'?'.http_build_query($parseQuery));
		}

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
			$shop_id = (int)$shops[0];
		}
		else
			Employee::getEmployeeShopAccess((int)$this->context->employee->id);

		$this->context->shop = new Shop($shop_id);

		/* Filter memorization */
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

		/* Manage list filtering */
		if (Tools::isSubmit('submitFilter'.$this->table) || $this->context->cookie->{'submitFilter'.$this->table} !== false)
			$this->filter = true;

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
		{
			if ($submitted_action == 'delete')
				if ($this->tabAccess['delete'] === '1')
					$this->action = 'multiple_delete';
				else
					$this->_errors[] = Tools::displayError('You do not have permission to delete here.');
			else
				$this->action = $submitted_action;
		}
		else if (Tools::getValue('submitAdd'.$this->table))
		{
			$this->action = 'save';
			$this->display = 'edit';
			//$this->id_entity = (int)$_GET['id_'.$this->table];
		}
		else if (isset($_GET['add'.$this->table]))
		{
			$this->action = 'new';
			$this->display = 'add';
		}
		else if (isset($_GET['update'.$this->table]) && isset($_GET['id_'.$this->table]))
		{
			// @TODO move the employee condition to AdminEmployee
			if ($this->tabAccess['edit'] === '1' || ($this->table == 'employee' && $this->context->employee->id == Tools::getValue('id_employee')))
				$this->display = 'edit';
			else
				$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
		}
		if (isset($_GET['view'.$this->table]))
		{
			if ($this->tabAccess['view'] === '1')
				$this->display = 'view';
			else
				$this->_errors[] = Tools::displayError('You do not have permission to view here.');
		}
		/* Cancel all filters for this tab */
		else if (isset($_POST['submitReset'.$this->table]))
			$this->action = 'reset_filters';
		/* Submit options list */
		else if (Tools::getValue('submitOptions'.$this->table))
			$this->action = 'update_options';
		else if (Tools::isSubmit('submitFields') && $this->requiredDatabase && $this->tabAccess['add'] === '1' && $this->tabAccess['delete'] === '1')
			$this->action = 'update_fields';
		else if (is_array($this->bulk_actions))
			foreach ($this->bulk_actions as $bulk_action => $params)
			{
				if (Tools::isSubmit('submitBulk'.$bulk_action.$this->table))
				{
					$this->action = 'bulk'.$bulk_action;
					$this->boxes = Tools::getValue($this->table.'Box');
					break;
				}
			}
	}

	/**
	 * Display errors
	 */
	public function displayErrors()
	{
		// @TODO includesubtab
		$content = $this->includeSubTab('displayErrors');
		return $content;
	}

	/**
	 * Get the current objects' list form the database
	 *
	 * @param integer $id_lang Language used for display
	 * @param string $orderBy ORDER BY clause
	 * @param string $_orderWay Order way (ASC, DESC)
	 * @param integer $start Offset in LIMIT clause
	 * @param integer $limit Row count in LIMIT clause
	 */
	public function getList($id_lang, $orderBy = null, $orderWay = null, $start = 0, $limit = null, $id_lang_shop = false)
	{
		/* Manage default params values */
		if (empty($limit))
			$limit = ((!isset($this->context->cookie->{$this->table.'_pagination'})) ? $this->_pagination[1] : $limit = $this->context->cookie->{$this->table.'_pagination'});

		if (!Validate::isTableOrIdentifier($this->table))
			die (Tools::displayError('Table name is invalid:').' "'.$this->table.'"');

		if (empty($orderBy))
			$orderBy = $this->context->cookie->__get($this->table.'Orderby') ? $this->context->cookie->__get($this->table.'Orderby') : $this->_defaultOrderBy;
		if (empty($orderWay))
			$orderWay = $this->context->cookie->__get($this->table.'Orderway') ? $this->context->cookie->__get($this->table.'Orderway') : 'ASC';

		$limit = (int)(Tools::getValue('pagination', $limit));
		$this->context->cookie->{$this->table.'_pagination'} = $limit;


		/* Check params validity */
		if (!Validate::isOrderBy($orderBy) || !Validate::isOrderWay($orderWay)
			|| !is_numeric($start) || !is_numeric($limit)
			|| !Validate::isUnsignedId($id_lang))
			die(Tools::displayError('get list params is not valid'));

		/* Determine offset from current page */
		if ((isset($_POST['submitFilter'.$this->table]) ||
		isset($_POST['submitFilter'.$this->table.'_x']) ||
		isset($_POST['submitFilter'.$this->table.'_y'])) &&
		!empty($_POST['submitFilter'.$this->table]) &&
		is_numeric($_POST['submitFilter'.$this->table]))
			$start = (int)($_POST['submitFilter'.$this->table] - 1) * $limit;

		/* Cache */
		$this->_lang = (int)($id_lang);
		$this->_orderBy = $orderBy;
		$this->_orderWay = Tools::strtoupper($orderWay);

		/* SQL table : orders, but class name is Order */
		$sqlTable = $this->table == 'order' ? 'orders' : $this->table;

		// Add SQL shop restriction
		$selectShop = $joinShop = $whereShop = '';
		if ($this->shopLinkType)
		{
			$selectShop = ', shop.name as shop_name ';
			$joinShop = ' LEFT JOIN '._DB_PREFIX_.$this->shopLinkType.' shop
							ON a.id_'.$this->shopLinkType.' = shop.id_'.$this->shopLinkType;
			$whereShop = $this->context->shop->sqlRestriction($this->shopShareDatas, 'a', $this->shopLinkType);
		}
		$assos = Shop::getAssoTables();
		if (isset($assos[$this->table]) && $assos[$this->table]['type'] == 'shop')
		{
			$filterKey = $assos[$this->table]['type'];
			$idenfierShop = $this->context->shop->getListOfID();
		}
		else if (Context::shop() == Shop::CONTEXT_GROUP)
		{
			$assos = GroupShop::getAssoTables();
			if (isset($assos[$this->table]) && $assos[$this->table]['type'] == 'group_shop')
			{
				$filterKey = $assos[$this->table]['type'];
				$idenfierShop = array($this->context->shop->getGroupID());
			}
		}

		$filterShop = '';
		if (isset($filterKey))
		{
			if (!$this->_group)
				$this->_group = 'GROUP BY a.'.pSQL($this->identifier);
			else if (!preg_match('#(\s|,)\s*a\.`?'.pSQL($this->identifier).'`?(\s|,|$)#', $this->_group))
				$this->_group .= ', a.'.pSQL($this->identifier);

			if (Shop::isMultiShopActivated() && Context::shop() != Shop::CONTEXT_ALL && !preg_match('#`?'.preg_quote(_DB_PREFIX_.$this->table.'_'.$filterKey).'`? *sa#', $this->_join))
				$filterShop = 'JOIN `'._DB_PREFIX_.$this->table.'_'.$filterKey.'` sa ON (sa.'.$this->identifier.' = a.'.$this->identifier.' AND sa.id_'.$filterKey.' IN ('.implode(', ', $idenfierShop).'))';
		}

		/* Query in order to get results with all fields */
		$sql = 'SELECT SQL_CALC_FOUND_ROWS
			'.($this->_tmpTableFilter ? ' * FROM (SELECT ' : '').'
			'.($this->lang ? 'b.*, ' : '').'a.*'.(isset($this->_select) ? ', '.$this->_select.' ' : '').$selectShop.'
			FROM `'._DB_PREFIX_.$sqlTable.'` a
			'.$filterShop.'
			'.($this->lang ? 'LEFT JOIN `'._DB_PREFIX_.$this->table.'_lang` b ON (b.`'.$this->identifier.'` = a.`'.$this->identifier.'` AND b.`id_lang` = '.(int)$id_lang.($id_lang_shop ? ' AND b.`id_shop`='.(int)$id_lang_shop : '').')' : '').'
			'.(isset($this->_join) ? $this->_join.' ' : '').'
			'.$joinShop.'
			WHERE 1 '.(isset($this->_where) ? $this->_where.' ' : '').($this->deleted ? 'AND a.`deleted` = 0 ' : '').(isset($this->_filter) ? $this->_filter : '').$whereShop.'
			'.(isset($this->_group) ? $this->_group.' ' : '').'
			'.((isset($this->_filterHaving) || isset($this->_having)) ? 'HAVING ' : '').(isset($this->_filterHaving) ? ltrim($this->_filterHaving, ' AND ') : '').(isset($this->_having) ? $this->_having.' ' : '').'
			ORDER BY '.(($orderBy == $this->identifier) ? 'a.' : '').'`'.pSQL($orderBy).'` '.pSQL($orderWay).
			($this->_tmpTableFilter ? ') tmpTable WHERE 1'.$this->_tmpTableFilter : '').'
			LIMIT '.(int)$start.','.(int)$limit;
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
	}

	public function getFieldsValue($obj)
	{
		foreach ($this->fields_form['input'] as $input)
			if (empty($this->fields_value[$input['name']]))
				if (isset($input['lang']) && $input['lang'])
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
	protected function getFieldValue($obj, $key, $id_lang = null)
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
	public function validateRules($className = false)
	{
		if (!$className)
			$className = $this->className;

		/* Class specific validation rules */
		$rules = call_user_func(array($className, 'getValidationRules'), $className);

		if ((count($rules['requiredLang']) || count($rules['sizeLang']) || count($rules['validateLang'])))
		{
			/* Language() instance determined by default language */
			$default_language = new Language((int)(Configuration::get('PS_LANG_DEFAULT')));

			/* All availables languages */
			$languages = Language::getLanguages(false);
		}

		/* Checking for required fields */
		foreach ($rules['required'] as $field)
			if (($value = Tools::getValue($field)) == false && (string)$value != '0')
				if (!Tools::getValue($this->identifier) || ($field != 'passwd' && $field != 'no-picture'))
					$this->_errors[] = $this->l('the field').' <b>'.call_user_func(array($className, 'displayFieldName'), $field, $className).'</b> '.$this->l('is required');

		/* Checking for multilingual required fields */
		foreach ($rules['requiredLang'] as $fieldLang)
			if (($empty = Tools::getValue($fieldLang.'_'.$default_language->id)) === false || $empty !== '0' && empty($empty))
				$this->_errors[] = $this->l('the field').' <b>'.call_user_func(array($className, 'displayFieldName'), $fieldLang, $className).'</b> '.$this->l('is required at least in').' '.$default_language->name;

		/* Checking for maximum fields sizes */
		foreach ($rules['size'] as $field => $maxLength)
			if (Tools::getValue($field) !== false && Tools::strlen(Tools::getValue($field)) > $maxLength)
				$this->_errors[] = $this->l('the field').' <b>'.call_user_func(array($className, 'displayFieldName'), $field, $className).'</b> '.$this->l('is too long').' ('.$maxLength.' '.$this->l('chars max').')';

		/* Checking for maximum multilingual fields size */
		foreach ($rules['sizeLang'] as $fieldLang => $maxLength)
			foreach ($languages as $language)
				if (Tools::getValue($fieldLang.'_'.$language['id_lang']) !== false && Tools::strlen(Tools::getValue($fieldLang.'_'.$language['id_lang'])) > $maxLength)
					$this->_errors[] = $this->l('the field').' <b>'.call_user_func(array($className, 'displayFieldName'), $fieldLang, $className).' ('.$language['name'].')</b> '.$this->l('is too long').' ('.$maxLength.' '.$this->l('chars max, html chars including').')';

		/* Overload this method for custom checking */
		$this->_childValidation();

		/* Checking for fields validity */
		foreach ($rules['validate'] as $field => $function)
			if (($value = Tools::getValue($field)) !== false && ($field != 'passwd'))
				if (!Validate::$function($value))
					$this->_errors[] = $this->l('the field').' <b>'.call_user_func(array($className, 'displayFieldName'), $field, $className).'</b> '.$this->l('is invalid');

		/* Checking for passwd_old validity */
		if (($value = Tools::getValue('passwd')) != false)
		{
			if ($className == 'Employee' && !Validate::isPasswdAdmin($value))
				$this->_errors[] = $this->l('the field').' <b>'.call_user_func(array($className, 'displayFieldName'), 'passwd', $className).'</b> '.$this->l('is invalid');
			else if ($className == 'Customer' && !Validate::isPasswd($value))
				$this->_errors[] = $this->l('the field').' <b>'.call_user_func(array($className, 'displayFieldName'), 'passwd', $className).'</b> '.$this->l('is invalid');
		}

		/* Checking for multilingual fields validity */
		foreach ($rules['validateLang'] as $fieldLang => $function)
			foreach ($languages as $language)
				if (($value = Tools::getValue($fieldLang.'_'.$language['id_lang'])) !== false && !empty($value))
					if (!Validate::$function($value))
						$this->_errors[] = $this->l('the field').' <b>'.call_user_func(array($className, 'displayFieldName'), $fieldLang, $className).' ('.$language['name'].')</b> '.$this->l('is invalid');
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
						$object->{$field}[(int)$language['id_lang']] = $_POST[$field.'_'.(int)($language['id_lang'])];
		}
	}

	protected function updateAssoShop($id_object = false)
	{
		if (!Shop::isMultiShopActivated())
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
											VALUES('.(int)$asso['id_object'].', '.(int)$asso['id_'.$type].')');
	}

	protected function validateField($value, $field)
	{
		if (isset($field['validation']))
		{
			if ((!isset($field['empty']) || !$field['empty'] || (isset($field['empty']) && $field['empty'] && $value)) && method_exists('Validate', $field['validation']))
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
	 * Update options and preferences
	 *
	 * @param string $token
	 */
	protected function updateOptions($token)
	{
		if ($this->tabAccess['edit'] === '1')
		{
			$this->beforeUpdateOptions();

			$languages = Language::getLanguages(false);

			foreach ($this->options as $option_list)
			{
				foreach ($option_list as $category => $categoryData)
				{
					$fields = $categoryData['fields'];

					/* Check required fields */
					foreach ($fields as $field => $values)
						if (isset($values['required']) && $values['required'] && !isset($_POST['configUseDefault'][$field]))
							if (isset($values['type']) && $values['type'] == 'textLang')
							{
								foreach ($languages as $language)
									if (($value = Tools::getValue($field.'_'.$language['id_lang'])) == false && (string)$value != '0')
										$this->_errors[] = Tools::displayError('field').' <b>'.$values['title'].'</b> '.Tools::displayError('is required.');
							}
							elseif (($value = Tools::getValue($field)) == false && (string)$value != '0')
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
						elseif (Tools::getValue($field) && isset($values['validation']))
							if (!Validate::$values['validation'](Tools::getValue($field)))
								$this->_errors[] = Tools::displayError('field').' <b>'.$values['title'].'</b> '.Tools::displayError('is invalid.');

					/* Default value if null */
					foreach ($fields as $field => $values)
						if (!Tools::getValue($field) && isset($values['default']))
							$_POST[$field] = $values['default'];

					if (1||!count($this->_errors))
					{
						foreach ($fields as $key => $options)
						{
							if (isset($options['visibility']) && $options['visibility'] > Context::getContext()->shop->getContextType())
								continue;

							if (Shop::isMultiShopActivated() && isset($_POST['configUseDefault'][$key]))
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
									$val = (isset($options['cast']) ? $options['cast'](Tools::getValue($key.'_'.$language['id_lang'])) : Tools::getValue($key.'_'.$language['id_lang']));
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
					}
				}
			}

			if (count($this->_errors) <= 0)
				Tools::redirectAdmin(self::$currentIndex.'&conf=6&token='.$token);
		}
		else
			$this->_errors[] = Tools::displayError('You do not have permission to edit here.');
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

	protected function uploadImage($id, $name, $dir, $ext = false, $width = NULL, $height = NULL)
	{
		if (isset($_FILES[$name]['tmp_name']) && !empty($_FILES[$name]['tmp_name']))
		{
			// Delete old image
			if (Validate::isLoadedObject($object = $this->loadObject()))
				$object->deleteImage();
			else
				return false;


			// Check image validity
			$max_size = isset($this->maxImageSize) ? $this->maxImageSize : 0;
			if ($error = checkImage($_FILES[$name], Tools::getMaxUploadSize($max_size)))
				$this->_errors[] = $error;
			elseif (!$tmpName = tempnam(_PS_TMP_IMG_DIR_, 'PS') || !move_uploaded_file($_FILES[$name]['tmp_name'], $tmpName))
				return false;
			else
			{
				$tmpName = $_FILES[$name]['tmp_name'];
				// Copy new image
				if (!imageResize($tmpName, _PS_IMG_DIR_.$dir.$id.'.'.$this->imageType, (int)$width, (int)$height, ($ext ? $ext : $this->imageType)))
					$this->_errors[] = Tools::displayError('An error occurred while uploading image.');
				if (count($this->_errors))
					return false;
				if ($this->afterImageUpload())
				{
					unlink($tmpName);
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
	 */
	protected function bulkDelete($boxes)
	{
		if (is_array($boxes) && !empty($boxes))
		{
			$object = new $this->className();
			if (isset($object->noZeroObject) &&
				// Check if all object will be deleted
				(count(call_user_func(array($this->className, $object->noZeroObject))) <= 1 || count($boxes) == count(call_user_func(array($this->className, $object->noZeroObject)))))
				$this->_errors[] = Tools::displayError('You need at least one object.').' <b>'.$this->table.'</b><br />'.Tools::displayError('You cannot delete all of the items.');
			else
			{
				$result = true;
				if ($this->deleted)
				{
					foreach ($boxes as $id)
					{
						$to_delete = new $this->className($id);
						$to_delete->deleted = 1;
						$result = $result && $to_delete->update();
					}
				}
				else
					$result = $object->deleteSelection(Tools::getValue($this->table.'Box'));

				if ($result)
					Tools::redirectAdmin(self::$currentIndex.'&conf=2&token='.$this->token);
				$this->_errors[] = Tools::displayError('An error occurred while deleting selection.');
			}
		}
		else
			$this->_errors[] = Tools::displayError('You must select at least one element to delete.');
	}
}