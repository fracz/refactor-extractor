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
*  @version  Release: $Revision: 7499 $
*  @license    http://opensource.org/licenses/osl-3.0.php  Open Software License (OSL 3.0)
*  International Registered Trademark & Property of PrestaShop SA
*/

abstract class ObjectModelCore
{
	/**
	 * List of field types
	 */
	const TYPE_INT = 1;
	const TYPE_BOOL = 2;
	const TYPE_STRING = 3;
	const TYPE_FLOAT = 4;
	const TYPE_DATE = 5;
	const TYPE_HTML = 6;

	/**
	 * List of association types
	 */
	const HAS_ONE = 1;
	const HAS_MANY = 2;

	/** @var integer Object id */
	public $id;

	/** @var integer lang id */
	protected $id_lang = null;

	protected $id_shop = null;

	private $getShopFromContext = true;

	/**
	 * @var string SQL This property shouldn't be overloaded anymore in class, use static $definition['table'] property instead
	 * @deprecated 1.5.0
	 */
	protected $table;

	/**
	 * @var string SQL This property shouldn't be overloaded anymore in class, use static $definition['primary'] property instead
	 * @deprecated 1.5.0
	 */
	protected $identifier;

	/** @var array Required fields for admin panel forms */
 	protected $fieldsRequired = array();

	/** @var fieldsRequiredDatabase */
	protected static $fieldsRequiredDatabase = null;

 	/** @var array Maximum fields size for admin panel forms */
 	protected $fieldsSize = array();

 	/** @var array Fields validity functions for admin panel forms */
 	protected $fieldsValidate = array();

	/** @var array Multilingual required fields for admin panel forms */
 	protected $fieldsRequiredLang = array();

 	/** @var array Multilingual maximum fields size for admin panel forms */
 	protected $fieldsSizeLang = array();

 	/** @var array Multilingual fields validity functions for admin panel forms */
 	protected $fieldsValidateLang = array();

	/**
	 * @deprecated 1.5.0
	 */
 	protected $tables = array();

 	/** @var array tables */
 	protected $webserviceParameters = array();

	/** @var  string path to image directory. Used for image deletion. */
	protected $image_dir = null;

	/** @var string file type of image files. Used for image deletion. */
	protected $image_format = 'jpg';

	/**
	 * @var array Contain object definition
	 * @since 1.5.0
	 */
	public static $definition = array();

	/**
	 * @var array Contain current object definition
	 */
	protected $def;

	/**
	 * Returns object validation rules (fields validity)
	 *
	 * @param string $className Child class name for static use (optional)
	 * @return array Validation rules (fields validity)
	 */
	public static function getValidationRules($className = __CLASS__)
	{
		$object = new $className();
		return array(
		'required' => $object->fieldsRequired,
		'size' => $object->fieldsSize,
		'validate' => $object->fieldsValidate,
		'requiredLang' => $object->fieldsRequiredLang,
		'sizeLang' => $object->fieldsSizeLang,
		'validateLang' => $object->fieldsValidateLang);
	}

	/**
	 * Build object
	 *
	 * @param integer $id Existing object id in order to load object (optional)
	 * @param integer $id_lang Required if object is multilingual (optional)
	 */
	public function __construct($id = null, $id_lang = null, $id_shop = null)
	{
		$this->def = self::getDefinition($this);
		$this->setDefinitionRetrocompatibility();

		if (!is_null($id_lang))
			$this->id_lang = (Language::getLanguage($id_lang) !== false) ? $id_lang : Configuration::get('PS_LANG_DEFAULT');

		if ($id_shop && $this->isLangMultishop())
		{
			$this->id_shop = (int)$id_shop;
			$this->getShopFromContext = false;
		}

		if ($this->isLangMultishop() && !$this->id_shop)
			$this->id_shop = Context::getContext()->shop->getID(true);

	 	if (!Validate::isTableOrIdentifier($this->def['primary']) || !Validate::isTableOrIdentifier($this->def['table']))
			throw new PrestashopException('Identifier or table format not valid for class '.get_class($this));

		if ($id)
		{
			// Load object from database if object id is present
			$cache_id = 'objectmodel_'.$this->def['table'].'_'.(int)$id.'_'.(int)$id_shop.'_'.(int)$id_lang;
			if (!Cache::isStored($cache_id))
			{
				$sql = 'SELECT *
						FROM `'._DB_PREFIX_.$this->def['table'].'` a '.
						($id_lang ? ('LEFT JOIN `'.pSQL(_DB_PREFIX_.$this->def['table']).'_lang` b ON (a.`'.$this->def['primary'].'` = b.`'.$this->def['primary']).'` AND `id_lang` = '.(int)($id_lang).')' : '')
						.' WHERE 1 AND a.`'.$this->def['primary'].'` = '.(int)$id.
							(($this->id_shop AND $id_lang) ? ' AND b.id_shop = '.$this->id_shop : '');
				Cache::store($cache_id, Db::getInstance()->getRow($sql));
			}

			$result = Cache::retrieve($cache_id);
			if ($result)
			{
				$this->id = (int)$id;
				foreach ($result as $key => $value)
					if (array_key_exists($key, $this))
						$this->{$key} = $value;

				if (!$id_lang && isset($this->def['multilang']) && $this->def['multilang'])
				{
					$sql = 'SELECT * FROM `'.pSQL(_DB_PREFIX_.$this->def['table']).'_lang`
							WHERE `'.$this->def['primary'].'` = '.(int)$id
							.(($this->id_shop) ? ' AND `id_shop` = '.$this->id_shop : '');
					$result = Db::getInstance()->executeS($sql);
					if ($result)
						foreach ($result as $row)
							foreach ($row AS $key => $value)
							{
								if (array_key_exists($key, $this) && $key != $this->def['primary'])
								{
									if (!is_array($this->{$key}))
										$this->{$key} = array();

									// @Todo: stripslashes() MUST BE removed in 1.4.6 and later, but is kept in 1.4.5 for a compatibility issue
									$this->{$key}[$row['id_lang']] = stripslashes($value);
								}
							}
				}
			}
		}

		if (!is_array(self::$fieldsRequiredDatabase))
		{
			$fields = $this->getfieldsRequiredDatabase(true);
			if ($fields)
				foreach ($fields AS $row)
					self::$fieldsRequiredDatabase[$row['object_name']][(int)$row['id_required_field']] = pSQL($row['field_name']);
			else
				self::$fieldsRequiredDatabase = array();
		}
	}

	/**
	 * Prepare fields for ObjectModel class (add, update)
	 * All fields are verified (pSQL, intval...)
	 *
	 * @return array All object fields
	 */
	public function getFields()
	{
		$this->validateFields();
		return $this->formatFields();
	}

	/**
	 * Prepare multilang fields
	 *
	 * @return array
	 */
	public function getFieldsLang()
	{
		// Retrocompatibility
		if (method_exists($this, 'getTranslationsFieldsChild'))
			return $this->getTranslationsFieldsChild();

		$this->validateFieldsLang();

		$fields = array();
		if (is_null($this->id_lang))
			foreach (Language::getLanguages(false) as $language)
				$fields[$language['id_lang']] = $this->formatFields($language['id_lang']);
		else
			$fields = $this->formatFields($this->id_lang);

		return $fields;
	}

	/**
	 * @param int $id_lang If this parameter is given, only take lang fields
	 * @return array
	 */
	public function formatFields($id_lang = null)
	{
		$fields = array();

		// Set primary key in fields
		if ($this->id)
			$fields[$this->def['primary']] = $this->id;

		// Set id_lang field for multilang fields and id_shop for multishop field
		if ($id_lang)
		{
			$fields['id_lang'] = $id_lang;
			if ($this->id_shop && $this->isLangMultishop())
				$fields['id_shop'] = (int)$this->id_shop;
		}

		foreach ($this->def['fields'] as $field => $data)
		{
			// If $id_lang take only language fields, else take only classic fields
			if (($id_lang && empty($data['lang'])) || (!$id_lang && !empty($data['lang'])))
				continue;

			// Get field value, if value is multilang and field is empty, use value from default lang
			$value = $this->$field;
			if ($id_lang && is_array($value))
			{
				if (!empty($value[$id_lang]))
					$value = $value[$id_lang];
				else if (!empty($data['required']))
					$value = $value[Configuration::get('PS_LANG_DEFAULT')];
				else
					$value = '';
			}

			// Format field value
			switch ($data['type'])
			{
				case self::TYPE_INT :
					$fields[$field] = (int)$value;
				break;

				case self::TYPE_BOOL :
					$fields[$field] = (int)$value;
				break;

				case self::TYPE_FLOAT :
					$fields[$field] = (float)$value;
				break;

				case self::TYPE_DATE :
					$fields[$field] = pSQL($value);
				break;

				case self::TYPE_STRING :
					$fields[$field] = pSQL($value);
				break;

				case self::TYPE_HTML :
					$fields[$field] = pSQL($value, true);
				break;

				default :
					if (method_exists($this, 'formatType'.$data['type']))
						$fields[$field] = $this->{'formatType'.$data['type']}($value);
				break;
			}
		}

		return $fields;
	}

	/**
	 * Save current object to database (add or update)
	 *
	 * @param bool $null_values
	 * @param bool $autodate
	 * @return boolean Insertion result
	 */
	public function save($null_values = false, $autodate = true)
	{
		return (int)$this->id > 0 ? $this->update($null_values) : $this->add($autodate, $null_values);
	}

	/**
	 * Add current object to database
	 *
	 * @param bool $null_values
	 * @param bool $autodate
	 * @return boolean Insertion result
	 */
	public function add($autodate = true, $null_values = false)
	{
		// @hook actionObject*AddBefore
		Hook::exec('actionObject'.get_class($this).'AddBefore', array('object' => $this));

		// Automatically fill dates
		if ($autodate && array_key_exists('date_add', $this))
			$this->date_add = date('Y-m-d H:i:s');
		if ($autodate && array_key_exists('date_upd', $this))
			$this->date_upd = date('Y-m-d H:i:s');

		// Database insertion
		if ($null_values)
			$result = Db::getInstance()->autoExecuteWithNullValues(_DB_PREFIX_.$this->def['table'], $this->getFields(), 'INSERT');
		else
			$result = Db::getInstance()->autoExecute(_DB_PREFIX_.$this->def['table'], $this->getFields(), 'INSERT');

		if (!$result)
			return false;

		// Get object id in database
		$this->id = Db::getInstance()->Insert_ID();
		$assos = Shop::getAssoTables();

		// Database insertion for multilingual fields related to the object
		if (isset($this->def['multilang']) && $this->def['multilang'])
		{
			$fields = $this->getFieldsLang();
			$shops = Shop::getShops(true, null, true);
			if ($fields && is_array($fields))
				foreach ($fields as &$field)
				{
					foreach (array_keys($field) AS $key)
					 	if (!Validate::isTableOrIdentifier($key))
			 				throw new PrestashopException('key '.$key.' is not table or identifier, ');
					$field[$this->def['primary']] = (int)$this->id;

					if (isset($assos[$this->def['table'].'_lang']) && $assos[$this->def['table'].'_lang']['type'] == 'fk_shop')
					{
						foreach ($shops as $id_shop)
						{
							$field['id_shop'] = (int)$id_shop;
							$result &= Db::getInstance()->AutoExecute(_DB_PREFIX_.$this->def['table'].'_lang', $field, 'INSERT');
						}
					}
					else
						$result &= Db::getInstance()->AutoExecute(_DB_PREFIX_.$this->def['table'].'_lang', $field, 'INSERT');
				}
		}

		if (!Shop::isFeatureActive())
		{
			if (isset($assos[$this->def['table']]) && $assos[$this->def['table']]['type'] == 'shop')
				$result &= $this->associateTo(Context::getContext()->shop->getID(true), 'shop');

			$assos = GroupShop::getAssoTables();
			if (isset($assos[$this->def['table']]) && $assos[$this->def['table']]['type'] == 'group_shop')
				$result &= $this->associateTo(Context::getContext()->shop->getGroupID(), 'group_shop');
		}

		// @hook actionObject*AddAfter
		Hook::exec('actionObject'.get_class($this).'AddAfter', array('object' => $this));

		return $result;
	}

	/**
	 * Update current object to database
	 *
	 * @param bool $null_values
	 * @return boolean Update result
	 */
	public function update($null_values = false)
	{
		// @hook actionObject*UpdateBefore
		Hook::exec('actionObject'.get_class($this).'UpdateBefore', array('object' => $this));

		$this->clearCache();

		// Automatically fill dates
		if (array_key_exists('date_upd', $this))
			$this->date_upd = date('Y-m-d H:i:s');

		// Database update
		if ($null_values)
			$result = Db::getInstance()->autoExecuteWithNullValues(_DB_PREFIX_.$this->def['table'], $this->getFields(), 'UPDATE', '`'.pSQL($this->def['primary']).'` = '.(int)($this->id));
		else
			$result = Db::getInstance()->autoExecute(_DB_PREFIX_.$this->def['table'], $this->getFields(), 'UPDATE', '`'.pSQL($this->def['primary']).'` = '.(int)($this->id));
		if (!$result)
			return false;

		// Database update for multilingual fields related to the object
		if (isset($this->def['multilang']) && $this->def['multilang'])
		{
			$fields = $this->getFieldsLang();
			if (is_array($fields))
			{
				foreach ($fields as $field)
				{
					foreach (array_keys($field) as $key)
						if (!Validate::isTableOrIdentifier($key))
							throw new PrestashopException('key '.$key.' is not a valid table or identifier');

					// If this table is linked to multishop system, update / insert for all shops from context
					if ($this->isLangMultishop())
					{
						$listShops = ($this->id_shop && !$this->getShopFromContext) ? array($this->id_shop) : Context::getContext()->shop->getListOfID();
						foreach ($listShops as $shop)
						{
							$field['id_shop'] = $shop;
							$where = pSQL($this->def['primary']).' = '.(int)$this->id
										.' AND id_lang = '.(int)$field['id_lang']
										.' AND id_shop = '.$field['id_shop'];

							if (Db::getInstance()->getValue('SELECT COUNT(*) FROM '.pSQL(_DB_PREFIX_.$this->def['table']).'_lang WHERE '.$where))
								$result &= Db::getInstance()->AutoExecute(_DB_PREFIX_.$this->def['table'].'_lang', $field, 'UPDATE', $where);
							else
								$result &= Db::getInstance()->AutoExecute(_DB_PREFIX_.$this->def['table'].'_lang', $field, 'INSERT');
						}
					}
					// If this table is not linked to multishop system ...
					else
					{
						$where = pSQL($this->def['primary']).' = '.(int)$this->id
									.' AND id_lang = '.(int)$field['id_lang'];
						if (Db::getInstance()->getValue('SELECT COUNT(*) FROM '.pSQL(_DB_PREFIX_.$this->def['table']).'_lang WHERE '.$where))
							$result &= Db::getInstance()->AutoExecute(_DB_PREFIX_.$this->def['table'].'_lang', $field, 'UPDATE', $where);
						else
							$result &= Db::getInstance()->AutoExecute(_DB_PREFIX_.$this->def['table'].'_lang', $field, 'INSERT');
					}
				}
			}
		}

		// @hook actionObject*UpdateAfter
		Hook::exec('actionObject'.get_class($this).'UpdateAfter', array('object' => $this));

		return $result;
	}

	/**
	 * Delete current object from database
	 *
	 * @return boolean Deletion result
	 */
	public function delete()
	{
		// @hook actionObject*DeleteBefore
		Hook::exec('actionObject'.get_class($this).'DeleteBefore', array('object' => $this));

		$this->clearCache();

		// Database deletion
		$result = Db::getInstance()->execute('DELETE FROM `'.pSQL(_DB_PREFIX_.$this->def['table']).'` WHERE `'.pSQL($this->def['primary']).'` = '.(int)$this->id);
		if (!$result)
			return false;

		// Database deletion for multilingual fields related to the object
		if (isset($this->def['multilang']) && $this->def['multilang'])
			Db::getInstance()->execute('DELETE FROM `'.pSQL(_DB_PREFIX_.$this->def['table']).'_lang` WHERE `'.pSQL($this->def['primary']).'` = '.(int)$this->id);

		$assos = Shop::getAssoTables();
		if (isset($assos[$this->def['table']]) && $assos[$this->def['table']]['type'] == 'shop')
			Db::getInstance()->Execute('DELETE FROM `'._DB_PREFIX_.$this->def['table'].'_shop` WHERE `'.$this->def['primary'].'`='.(int)$this->id);

		$assos = GroupShop::getAssoTables();
		if (isset($assos[$this->def['table']]) && $assos[$this->def['table']]['type'] == 'group_shop')
			Db::getInstance()->Execute('DELETE FROM `'._DB_PREFIX_.$this->def['table'].'_group_shop` WHERE `'.$this->def['primary'].'`='.(int)$this->id);

		// @hook actionObject*DeleteAfter
		Hook::exec('actionObject'.get_class($this).'DeleteAfter', array('object' => $this));

		return $result;
	}

	/**
	 * Delete several objects from database
	 *
	 * @param array $selection
	 * @return bool Deletion result
	 */
	public function deleteSelection($selection)
	{
		$result = true;
		foreach ($selection as $id)
		{
			$this->id = (int)$id;
			$result = $result AND $this->delete();
		}
		return $result;
	}

	/**
	 * Toggle object status in database
	 *
	 * @return boolean Update result
	 */
	public function toggleStatus()
	{
	 	// Object must have a variable called 'active'
	 	if (!array_key_exists('active', $this))
			throw new PrestashopException('property "active is missing in object '.get_class($this));

	 	// Update active status on object
	 	$this->active = !(int)$this->active;

		// Change status to active/inactive
		return Db::getInstance()->execute('
			UPDATE `'.pSQL(_DB_PREFIX_.$this->def['table']).'`
			SET `active` = !`active`
			WHERE `'.pSQL($this->def['primary']).'` = '.(int)$this->id
		);
	}

	/**
	 * @deprecated 1.5.0 (use getFieldsLang())
	 */
	protected function getTranslationsFields($fieldsArray)
	{
		$fields = array();

		if ($this->id_lang == NULL)
			foreach (Language::getLanguages(false) as $language)
				$this->makeTranslationFields($fields, $fieldsArray, $language['id_lang']);
		else
			$this->makeTranslationFields($fields, $fieldsArray, $this->id_lang);

		return $fields;
	}

	/**
	 * @deprecated 1.5.0
	 */
	protected function makeTranslationFields(&$fields, &$fieldsArray, $id_language)
	{
		$fields[$id_language]['id_lang'] = $id_language;
		$fields[$id_language][$this->def['primary']] = (int)($this->id);
		if ($this->id_shop && $this->isLangMultishop())
			$fields[$id_language]['id_shop'] = (int)$this->id_shop;
		foreach ($fieldsArray as $k => $field)
		{
			$html = false;
			$fieldName = $field;
			if (is_array($field))
			{
				$fieldName = $k;
				$html = (isset($field['html'])) ? $field['html'] : false;
			}

			/* Check fields validity */
			if (!Validate::isTableOrIdentifier($fieldName))
				throw new PrestashopException('identifier is not table or identifier : '.$fieldName);

			/* Copy the field, or the default language field if it's both required and empty */
			if ((!$this->id_lang && isset($this->{$fieldName}[$id_language]) && !empty($this->{$fieldName}[$id_language]))
			|| ($this->id_lang && isset($this->$fieldName) && !empty($this->$fieldName)))
				$fields[$id_language][$fieldName] = $this->id_lang ? pSQL($this->$fieldName, $html) : pSQL($this->{$fieldName}[$id_language], $html);
			else if (in_array($fieldName, $this->fieldsRequiredLang))
				$fields[$id_language][$fieldName] = $this->id_lang ? pSQL($this->$fieldName, $html) : pSQL($this->{$fieldName}[Configuration::get('PS_LANG_DEFAULT')], $html);
			else
				$fields[$id_language][$fieldName] = '';
		}
	}

	/**
	 * Check for fields validity before database interaction
	 *
	 * @param bool $die
	 * @param bool $error_return
	 * @return bool|string
	 */
	public function validateFields($die = true, $error_return = false)
	{
		$fieldsRequired = array_merge($this->fieldsRequired, (isset(self::$fieldsRequiredDatabase[get_class($this)]) ? self::$fieldsRequiredDatabase[get_class($this)] : array()));
		foreach ($fieldsRequired as $field)
			if (Tools::isEmpty($this->{$field}) && !is_numeric($this->{$field}))
			{
				if ($die)
					throw new PrestashopException('property empty : '.get_class($this).'->'.$field);
				return $error_return ? get_class($this).' -> '.$field.' is empty' : false;
			}

		foreach ($this->fieldsSize as $field => $size)
			if (isset($this->{$field}) && Tools::strlen($this->{$field}) > $size)
			{
				if ($die)
					throw new PrestashopException('fieldsize error : '.get_class($this).'->'.$field.' > '.$size);
				return $error_return ? get_class($this).' -> '.$field.' Length '.$size : false;
			}

		foreach ($this->fieldsValidate as $field => $method)
			if (!method_exists('Validate', $method))
				throw new PrestashopException('Validation function not found. '.$method);
			elseif (!empty($this->{$field}) && !call_user_func(array('Validate', $method), $this->{$field}))
			{
				if ($die)
					throw new PrestashopException('Field not valid : '.get_class($this).'->'.$field.' = '.$this->{$field});
				return $error_return ? get_class($this).' -> '.$field.' = '.$this->{$field} : false;
			}
		return true;
	}

	/**
	 * Check for multilingual fields validity before database interaction
	 *
	 * @param bool $die
	 * @param bool $error_return
	 * @return bool|string
	 */
	public function validateFieldsLang($die = true, $error_return = false)
	{
		$defaultLanguage = (int)Configuration::get('PS_LANG_DEFAULT');
		foreach ($this->fieldsRequiredLang as $fieldArray)
		{
			if (!is_array($this->{$fieldArray}))
				continue ;
			if (!$this->{$fieldArray} || !count($this->{$fieldArray}) || ($this->{$fieldArray}[$defaultLanguage] !== '0' && empty($this->{$fieldArray}[$defaultLanguage])))
			{
				if ($die)
					throw new PrestashopException('empty for default language : '.get_class($this).'->'.$fieldArray);
				return $error_return ? get_class($this).'->'.$fieldArray.' '.Tools::displayError('is empty for default language.') : false;
			}
		}

		foreach ($this->fieldsSizeLang as $fieldArray => $size)
		{
			if (!is_array($this->{$fieldArray}))
				continue ;
			foreach ($this->{$fieldArray} as $k => $value)
				if (Tools::strlen($value) > $size)
				{
					if ($die)
						throw new PrestashopException('fieldsize error '.get_class($this).'->'.$fieldArray.' length of '.$size.' for language');
					return $error_return ? get_class($this).'->'.$fieldArray.' '.Tools::displayError('Length').' '.$size.' '.Tools::displayError('for language') : false;
				}
		}

		foreach ($this->fieldsValidateLang as $fieldArray => $method)
		{
			if (!is_array($this->{$fieldArray}))
				continue ;
			foreach ($this->{$fieldArray} as $k => $value)
				if (!method_exists('Validate', $method))
					throw new PrestashopException('Validation function not found for lang: '.$method);
				elseif (!empty($value) && !call_user_func(array('Validate', $method), $value))
				{
					if ($die)
						throw new PrestashopException('Field not valid : '.get_class($this).'->'.$fieldArray.' = '.$value. 'for language '.$k);
					return $error_return ? Tools::displayError('The following field is invalid according to the validate method ').'<b>'.$method.'</b>:<br/> ('. get_class($this).'->'.$fieldArray.' = '.$value.' '.Tools::displayError('for language').' '.$k : false;
				}
		}
		return true;
	}

	static public function displayFieldName($field, $className = __CLASS__, $htmlentities = true, Context $context = null)
	{
		global $_FIELDS;
		@include(_PS_TRANSLATIONS_DIR_.Context::getContext()->language->iso_code.'/fields.php');

		$key = $className.'_'.md5($field);
		return ((is_array($_FIELDS) && array_key_exists($key, $_FIELDS)) ? ($htmlentities ? htmlentities($_FIELDS[$key], ENT_QUOTES, 'utf-8') : $_FIELDS[$key]) : $field);
	}

	/**
	* TODO: refactor rename all calls to this to validateController
	* @deprecated since 1.5 use validateController instead
	*/
	public function validateControler($htmlentities = true)
	{
		Tools::displayAsDeprecated();
		return $this->validateController($htmlentities);
	}

	public function validateController($htmlentities = true)
	{
		$errors = array();

		/* Checking for required fields */
		$fieldsRequired = array_merge($this->fieldsRequired, (isset(self::$fieldsRequiredDatabase[get_class($this)]) ? self::$fieldsRequiredDatabase[get_class($this)] : array()));
		foreach ($fieldsRequired AS $field)
		if (($value = Tools::getValue($field, $this->{$field})) == false && (string)$value != '0')
			if (!$this->id OR $field != 'passwd')
				$errors[] = '<b>'.self::displayFieldName($field, get_class($this), $htmlentities).'</b> '.Tools::displayError('is required.');


		/* Checking for maximum fields sizes */
		foreach ($this->fieldsSize AS $field => $maxLength)
			if (($value = Tools::getValue($field, $this->{$field})) && Tools::strlen($value) > $maxLength)
				$errors[] = '<b>'.self::displayFieldName($field, get_class($this), $htmlentities).'</b> '.Tools::displayError('is too long.').' ('.Tools::displayError('Maximum length:').' '.$maxLength.')';

		/* Checking for fields validity */
		foreach ($this->fieldsValidate AS $field => $function)
		{
			// Hack for postcode required for country which does not have postcodes
			if ($value = Tools::getValue($field, $this->{$field}) OR ($field == 'postcode' AND $value == '0'))
			{
				if (!Validate::$function($value) && (!empty($value) || in_array($field, $this->fieldsRequired)))
					$errors[] = '<b>'.self::displayFieldName($field, get_class($this), $htmlentities).'</b> '.Tools::displayError('is invalid.');
				else
				{
					if ($field == 'passwd')
					{
						if ($value = Tools::getValue($field))
							$this->{$field} = Tools::encrypt($value);
					}
					else
						$this->{$field} = $value;
				}
			}
		}
		return $errors;
	}

	public function getWebserviceParameters($wsParamsAttributeName = null)
	{
		$defaultResourceParameters = array(
			'objectSqlId' => $this->def['primary'],
			'retrieveData' => array(
				'className' => get_class($this),
				'retrieveMethod' => 'getWebserviceObjectList',
				'params' => array(),
				'table' => $this->def['table'],
			),
			'fields' => array(
				'id' => array('sqlId' => $this->def['primary'], 'i18n' => false),
			),
		);

		if (is_null($wsParamsAttributeName))
			$wsParamsAttributeName = 'webserviceParameters';


		if (!isset($this->{$wsParamsAttributeName}['objectNodeName']))
			$defaultResourceParameters['objectNodeName'] = $this->def['table'];
		if (!isset($this->{$wsParamsAttributeName}['objectsNodeName']))
			$defaultResourceParameters['objectsNodeName'] = $this->def['table'].'s';

		if (isset($this->{$wsParamsAttributeName}['associations']))
			foreach ($this->{$wsParamsAttributeName}['associations'] as $assocName => &$association)
			{
				if (!array_key_exists('setter', $association) || (isset($association['setter']) && !$association['setter']))
					$association['setter'] = Tools::toCamelCase('set_ws_'.$assocName);
				if (!array_key_exists('getter', $association))
					$association['getter'] = Tools::toCamelCase('get_ws_'.$assocName);
			}


		if (isset($this->{$wsParamsAttributeName}['retrieveData']) && isset($this->{$wsParamsAttributeName}['retrieveData']['retrieveMethod']))
			unset($defaultResourceParameters['retrieveData']['retrieveMethod']);

		$resourceParameters = array_merge_recursive($defaultResourceParameters, $this->{$wsParamsAttributeName});
		if (isset($this->fieldsSize))
			foreach ($this->fieldsSize as $fieldName => $maxSize)
			{
				if (!isset($resourceParameters['fields'][$fieldName]))
					$resourceParameters['fields'][$fieldName] = array('required' => false);
				$resourceParameters['fields'][$fieldName] = array_merge(
					$resourceParameters['fields'][$fieldName],
					$resourceParameters['fields'][$fieldName] = array('sqlId' => $fieldName, 'maxSize' => $maxSize, 'i18n' => false)
				);
			}
		if (isset($this->fieldsValidate))
			foreach ($this->fieldsValidate as $fieldName => $validateMethod)
			{
				if (!isset($resourceParameters['fields'][$fieldName]))
					$resourceParameters['fields'][$fieldName] = array('required' => false);
				$resourceParameters['fields'][$fieldName] = array_merge(
					$resourceParameters['fields'][$fieldName],
					$resourceParameters['fields'][$fieldName] = array(
						'sqlId' => $fieldName,
						'validateMethod' => (
								array_key_exists('validateMethod', $resourceParameters['fields'][$fieldName]) ?
								array_merge($resourceParameters['fields'][$fieldName]['validateMethod'], array($validateMethod)) :
								array($validateMethod)
							),
						'i18n' => false
					)
				);
			}
		if (isset($this->fieldsRequired))
		{
			$fieldsRequired = array_merge($this->fieldsRequired, (isset(self::$fieldsRequiredDatabase[get_class($this)]) ? self::$fieldsRequiredDatabase[get_class($this)] : array()));
			foreach ($fieldsRequired as $fieldRequired)
			{
				if (!isset($resourceParameters['fields'][$fieldRequired]))
					$resourceParameters['fields'][$fieldRequired] = array();
				$resourceParameters['fields'][$fieldRequired] = array_merge(
					$resourceParameters['fields'][$fieldRequired],
					$resourceParameters['fields'][$fieldRequired] = array('sqlId' => $fieldRequired, 'required' => true, 'i18n' => false)
				);
			}
		}
		if (isset($this->fieldsSizeLang))
			foreach ($this->fieldsSizeLang as $fieldName => $maxSize)
			{
				if (!isset($resourceParameters['fields'][$fieldName]))
					$resourceParameters['fields'][$fieldName] = array('required' => false);
				$resourceParameters['fields'][$fieldName] = array_merge(
					$resourceParameters['fields'][$fieldName],
					$resourceParameters['fields'][$fieldName] = array('sqlId' => $fieldName, 'maxSize' => $maxSize, 'i18n' => true)
				);
			}
		if (isset($this->fieldsValidateLang))
			foreach ($this->fieldsValidateLang as $fieldName => $validateMethod)
			{
				if (!isset($resourceParameters['fields'][$fieldName]))
					$resourceParameters['fields'][$fieldName] = array('required' => false);
				$resourceParameters['fields'][$fieldName] = array_merge(
					$resourceParameters['fields'][$fieldName],
					$resourceParameters['fields'][$fieldName] = array(
						'sqlId' => $fieldName,
						'validateMethod' => (
								array_key_exists('validateMethod', $resourceParameters['fields'][$fieldName]) ?
								array_merge($resourceParameters['fields'][$fieldName]['validateMethod'], array($validateMethod)) :
								array($validateMethod)
							),
						'i18n' => true
					)
				);
			}

		if (isset($this->fieldsRequiredLang))
			foreach ($this->fieldsRequiredLang as $field)
			{
				if (!isset($resourceParameters['fields'][$field]))
					$resourceParameters['fields'][$field] = array();
				$resourceParameters['fields'][$field] = array_merge(
					$resourceParameters['fields'][$field],
					$resourceParameters['fields'][$field] = array('sqlId' => $field, 'required' => true, 'i18n' => true)
				);
			}

		if (isset($this->date_add))
			$resourceParameters['fields']['date_add']['setter'] = false;
		if (isset($this->date_upd))
			$resourceParameters['fields']['date_upd']['setter'] = false;

		foreach ($resourceParameters['fields'] as $key => &$resourceParametersField)
			if (!isset($resourceParametersField['sqlId']))
				$resourceParametersField['sqlId'] = $key;
		return $resourceParameters;
	}

	public function getWebserviceObjectList($sql_join, $sql_filter, $sql_sort, $sql_limit)
	{
		$assoc = Shop::getAssoTables();

		if (array_key_exists($this->def['table'] ,$assoc))
		{
			$multi_shop_join = ' LEFT JOIN `'._DB_PREFIX_.$this->def['table'].'_'.$assoc[$this->def['table']]['type'].'` AS multi_shop_'.$this->def['table'].' ON (main.'.$this->def['primary'].' = '.'multi_shop_'.$this->def['table'].'.'.$this->def['primary'].')';
			$class_name = WebserviceRequest::$ws_current_classname;
			$vars = get_class_vars($class_name);
			foreach ($vars['shopIDs'] as $id_shop)
				$OR[] = ' multi_shop_'.$this->def['table'].'.id_shop = '.$id_shop.' ';
			$multi_shop_filter = ' AND ('.implode('OR', $OR).') ';
			$sql_filter = $multi_shop_filter.' '.$sql_filter;
			$sql_join = $multi_shop_join .' '. $sql_join;
		}
		$query = '
		SELECT DISTINCT main.`'.$this->def['primary'].'` FROM `'._DB_PREFIX_.$this->def['table'].'` AS main
		'.$sql_join.'
		WHERE 1 '.$sql_filter.'
		'.($sql_sort != '' ? $sql_sort : '').'
		'.($sql_limit != '' ? $sql_limit : '').'
		';
		return Db::getInstance(_PS_USE_SQL_SLAVE_)->executeS($query);
	}

	public function getFieldsRequiredDatabase($all = false)
	{
		return Db::getInstance()->executeS('
		SELECT id_required_field, object_name, field_name
		FROM '._DB_PREFIX_.'required_field
		'.(!$all ? 'WHERE object_name = \''.pSQL(get_class($this)).'\'' : ''));
	}

	public function addFieldsRequiredDatabase($fields)
	{
		if (!is_array($fields))
			return false;

		if (!Db::getInstance()->execute('DELETE FROM '._DB_PREFIX_.'required_field WHERE object_name = \''.pSQL(get_class($this)).'\''))
			return false;

		foreach ($fields AS $field)
			if (!Db::getInstance()->AutoExecute(_DB_PREFIX_.'required_field', array('object_name' => pSQL(get_class($this)), 'field_name' => pSQL($field)), 'INSERT'))
				return false;
		return true;
	}

	public function clearCache($all = false)
	{
		if ($all)
			Cache::clean('objectmodel_'.$this->def['table'].'_*');
		else if ($this->id)
			Cache::clean('objectmodel_'.$this->def['table'].'_'.(int)$this->id.'_*');
	}

	/**
	 * Check if current object is associated to a shop
	 *
	 * @since 1.5.0
	 * @param int $id_shop
	 * @return bool
	 */
	public function isAssociatedToShop($id_shop = null)
	{
		if (is_null($id_shop))
			$id_shop = Context::getContext()->shop->getID();

		$sql = 'SELECT id_shop
				FROM `'.pSQL(_DB_PREFIX_.$this->def['table']).'_shop`
				WHERE `'.$this->def['primary'].'` = '.(int)$this->id.'
					AND id_shop = '.(int)$id_shop;
		return (bool)Db::getInstance()->getValue($sql);
	}

	/**
	 * This function associate an item to its context
	 *
	 * @param int|array $id_shops
	 * @param string $type
	 * @return boolean
	 */
	public function associateTo($id_shops, $type = 'shop')
	{
		if (!$this->id)
			return;
		$sql = '';
		if (!is_array($id_shops))
			$id_shops = array($id_shops);

		foreach ($id_shops as $id_shop)
		{
			if (($type == 'shop' && !$this->isAssociatedToShop($id_shop)) || ($type == 'group_shop' && !$this->isAssociatedToGroupShop($id_shop)))
				$sql .= '('.(int)$this->id.','.(int)$id_shop.'),';
		}

		if (!empty($sql))
			return (bool)Db::getInstance()->execute('INSERT INTO `'._DB_PREFIX_.$this->def['table'].'_'.$type.'` (`'.$this->def['primary'].'`, `id_'.$type.'`) VALUES '.rtrim($sql,','));
		return true;
	}

	/**
	 * Check if current object is associated to a group shop
	 *
	 * @since 1.5.0
	 * @param int $id_group_shop
	 * @return bool
	 */
	public function isAssociatedToGroupShop($id_group_shop = null)
	{
		if (is_null($id_group_shop))
			$id_group_shop = Context::getContext()->shop->getGroupID();

		$sql = 'SELECT id_group_shop
				FROM `'.pSQL(_DB_PREFIX_.$this->def['table']).'_group_shop`
				WHERE `'.$this->def['primary'].'`='.(int)$this->id.' AND id_group_shop='.(int)$id_group_shop;
		return (bool)Db::getInstance()->getValue($sql);
	}

	/**
	 * @since 1.5.0
	 */
	public function duplicateShops($id)
	{
		$asso = Shop::getAssoTables();
		if (!isset($asso[$this->def['table']]) || $asso[$this->def['table']]['type'] != 'shop')
			return false;

		$sql = 'SELECT id_shop
				FROM '._DB_PREFIX_.$this->def['table'].'_shop
				WHERE '.$this->def['primary'].' = '.(int)$id;
		if ($results = Db::getInstance()->executeS($sql))
		{
			$ids = array();
			foreach ($results as $row)
				$ids[] = $row['id_shop'];
			return $this->associateTo($ids);
		}

		return false;
	}

	public function isLangMultishop()
	{
		return isset($this->def['multishop']) && $this->def['multishop'] && isset($this->def['multilang']) && $this->def['multilang'];
	}

	/**
	 * Delete images associated with the object
	 *
	 * @return bool success
	 */
	public function deleteImage()
	{
		if (!$this->id)
			return false;

		/* Deleting object images and thumbnails (cache) */
		if ($this->image_dir)
		{
			if (file_exists($this->image_dir.$this->id.'.'.$this->image_format)
				&& !unlink($this->image_dir.$this->id.'.'.$this->image_format))
				return false;
		}
		if (file_exists(_PS_TMP_IMG_DIR_.$this->def['table'].'_'.$this->id.'.'.$this->image_format)
			&& !unlink(_PS_TMP_IMG_DIR_.$this->def['table'].'_'.$this->id.'.'.$this->image_format))
			return false;
		if (file_exists(_PS_TMP_IMG_DIR_.$this->def['table'].'_mini_'.$this->id.'.'.$this->image_format)
			&& !unlink(_PS_TMP_IMG_DIR_.$this->def['table'].'_mini_'.$this->id.'.'.$this->image_format))
			return false;

		$types = ImageType::getImagesTypes();
		foreach ($types AS $image_type)
			if (file_exists($this->image_dir.$this->id.'-'.stripslashes($image_type['name']).'.'.$this->image_format)
			&& !unlink($this->image_dir.$this->id.'-'.stripslashes($image_type['name']).'.'.$this->image_format))
				return false;
		return true;
	}

	/**
	* Specify if an ObjectModel is already in database
	*
	* @param $id_entity entity id
	* @return boolean
	*/
	public static function existsInDatabase($id_entity, $table)
	{
		$row = Db::getInstance()->getRow('
		SELECT `id_'.$table.'` as id
		FROM `'._DB_PREFIX_.$table.'` e
		WHERE e.`id_'.$table.'` = '.(int)($id_entity));

		return isset($row['id']);
	}

	/**
	 * This method is allow to know if a entity is currently used
	 * @since 1.5.0.1
	 * @param string $table name of table linked to entity
	 * @param bool $has_active_column true if the table has an active column
	 * @return bool
	 */
	public static function isCurrentlyUsed($table, $has_active_column = false)
	{
		$query = new DbQuery();
		$query->select('`id_'.pSQL($table).'`');
		$query->from(pSQL($table));
		if ($has_active_column)
			$query->where('`active` = 1');
		return (bool)Db::getInstance()->getValue($query);
	}

	/**
	 * Get object identifier name
	 *
	 * @since 1.5.0
	 * @return string
	 */
	public function getIdentifier()
	{
		return $this->def['primary'];
	}

	/**
	 * Get list of fields related to language to validate
	 *
	 * @since 1.5.0
	 * @return array
	 */
	public function getFieldsValidateLang()
	{
		return $this->fieldsValidateLang;
	}

	/**
	 * Fill an object with given data. Data must be an array with this syntax: array(objProperty => value, objProperty2 => value, etc.)
	 *
	 * @since 1.5.0
	 * @param array $data
	 * @param int $id_lang
	 */
	public function hydrate(array $data, $id_lang = null)
	{
		$this->id_lang = $id_lang;
		if (isset($data[$this->def['primary']]))
			$this->id = $data[$this->def['primary']];
		foreach ($data as $key => $value)
			if (array_key_exists($key, $this))
				$this->$key = $value;
	}

	/**
	 * Fill (hydrate) a list of objects in order to get a collection of these objects
	 *
	 * @since 1.5.0
	 * @param string $class Class of objects to hydrate
	 * @param array $datas List of data (multi-dimensional array)
	 * @param int $id_lang
	 * @return array
	 */
	public static function hydrateCollection($class, array $datas, $id_lang = null)
	{
		if (!class_exists($class))
			throw new PrestashopException("Class '$class' not found");

		$collection = array();
		$rows = array();
		if ($datas)
		{
			$definition = ObjectModel::getDefinition($class);
			if (!array_key_exists($definition['primary'], $datas[0]))
				throw new PrestashopException("Identifier '{$definition['primary']}' not found for class '$class'");

			foreach ($datas as $row)
			{
				// Get object common properties
				$id = $row[$definition['primary']];
				if (!isset($rows[$id]))
					$rows[$id] = $row;

				// Get object lang properties
				if (isset($row['id_lang']) && !$id_lang)
					foreach ($definition['fields'] as $field => $data)
						if (!empty($data['lang']))
						{
							if (!is_array($rows[$id][$field]))
								$rows[$id][$field] = array();
							$rows[$id][$field][$row['id_lang']] = $row[$field];
						}
			}
		}

		// Hydrate objects
		foreach ($rows as $row)
		{
			$obj = new $class;
			$obj->hydrate($row, $id_lang);
			$collection[] = $obj;
		}
		return $collection;
	}

	public static function getDefinition($class, $field = null)
	{
		$reflection = new ReflectionClass($class);
		$definition = $reflection->getStaticPropertyValue('definition');
		if ($field)
			return isset($definition[$field]) ? $definition[$field] : null;
		return $definition;
	}

	/**
	 * Retrocompatibility for classes without $definition static
	 * Remove this in 1.6 !
	 *
	 * @since 1.5.0
	 */
	protected function setDefinitionRetrocompatibility()
	{
		// Retrocompatibility with $table property ($definition['table'])
		if (isset($this->def['table']))
			$this->table = $this->def['table'];
		else
			$this->def['table'] = $this->table;

		// Retrocompatibility with $identifier property ($definition['primary'])
		if (isset($this->def['primary']))
			$this->identifier = $this->def['primary'];
		else
			$this->def['primary'] = $this->identifier;

		// Check multilang retrocompatibility
		if (method_exists($this, 'getTranslationsFieldsChild'))
			$this->def['multilang'] = true;

		// Retrocompatibility with $fieldsValidate, $fieldsRequired and $fieldsSize properties ($definition['fields'])
		if (isset($this->def['fields']))
		{
			foreach ($this->def['fields'] as $field => $data)
			{
				$suffix = (isset($data['lang']) && $data['lang']) ? 'Lang' : '';
				if (isset($data['validate']))
					$this->{'fieldsValidate'.$suffix}[$field] = $data['validate'];
				if (isset($data['required']) && $data['required'])
					$this->{'fieldsRequired'.$suffix}[] = $field;
				if (isset($data['size']))
					$this->{'fieldsSize'.$suffix}[$field] = $data['size'];
			}
		}
		else
		{
			$this->def['fields'] = array();
			$suffix = (isset($data['lang']) && $data['lang']) ? 'Lang' : '';
			foreach ($this->{'fieldsValidate'.$suffix} as $field => $validate)
				$this->def['fields'][$field]['validate'] = $validate;
			foreach ($this->{'fieldsRequired'.$suffix} as $field)
				$this->def['fields'][$field]['required'] = true;
			foreach ($this->{'fieldsSize'.$suffix} as $field => $size)
				$this->def['fields'][$field]['size'] = $size;
		}
	}
}