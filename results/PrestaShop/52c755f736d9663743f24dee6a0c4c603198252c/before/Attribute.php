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
*  @version  Release: $Revision: 7307 $
*  @license    http://opensource.org/licenses/osl-3.0.php  Open Software License (OSL 3.0)
*  International Registered Trademark & Property of PrestaShop SA
*/

class AttributeCore extends ObjectModel
{
	/** @var integer Group id which attribute belongs */
	public $id_attribute_group;

	/** @var string Name */
	public $name;
	public $color;
	public $position;
	public $default;

 	protected $fieldsRequired = array('id_attribute_group');
	protected $fieldsValidate = array('id_attribute_group' => 'isUnsignedId', 'color' => 'isColor', 'position' => 'isInt');
 	protected $fieldsRequiredLang = array('name');
 	protected $fieldsSizeLang = array('name' => 64);
 	protected $fieldsValidateLang = array('name' => 'isGenericName');

	protected $table = 'attribute';
	protected $identifier = 'id_attribute';
	protected	$image_dir = _PS_COL_IMG_DIR_;

	protected $webserviceParameters = array(
		'objectsNodeName' => 'product_option_values',
		'objectNodeName' => 'product_option_value',
		'fields' => array(
			'id_attribute_group' => array('xlink_resource'=> 'product_options'),
		)
	);

	public function __construct($id = null, $id_lang = null, $id_shop = null)
	{
		$this->image_dir = _PS_COL_IMG_DIR_;

		parent::__construct($id, $id_lang, $id_shop);
	}

	public function getFields()
	{
		$this->validateFields();

		$fields['id_attribute_group'] = (int)$this->id_attribute_group;
		$fields['color'] = pSQL($this->color);
		$fields['position'] = (int)$this->position;

		return $fields;
	}

	/**
	* Check then return multilingual fields for database interaction
	*
	* @return array Multilingual fields
	*/
	public function getTranslationsFieldsChild()
	{
		$this->validateFieldsLang();
		return $this->getTranslationsFields(array('name'));
	}

	public function delete()
	{
		if (($result = Db::getInstance()->executeS('
			SELECT `id_product_attribute`
			FROM `'._DB_PREFIX_.'product_attribute_combination`
			WHERE `'.$this->identifier.'` = '.(int)$this->id)) === false)
			return false;
		$combination_ids = array();
		if (Db::getInstance()->numRows())
		{
			foreach ($result as $row)
				$combination_ids[] = (int)$row['id_product_attribute'];
			if (Db::getInstance()->execute('
				DELETE FROM `'._DB_PREFIX_.'product_attribute_combination`
				WHERE `'.$this->identifier.'` = '.(int)$this->id) === false)
				return false;
			if (Db::getInstance()->execute('
				DELETE FROM `'._DB_PREFIX_.'product_attribute`
				WHERE `id_product_attribute` IN ('.implode(', ', $combination_ids).')') === false)
				return false;
		}

		/* Reinitializing position */
		$this->cleanPositions((int)$this->id_attribute_group);

		$return = parent::delete();
		if ($return)
			Hook::exec('afterDeleteAttribute', array('id_attribute' => $this->id));
		return $return;
	}

	public function update($null_values = false)
	{
		$return = parent::update($null_values);
		if ($return)
			Hook::exec('afterSaveAttribute', array('id_attribute' => $this->id));
		return $return;
	}

	public function add($autodate = true, $null_values = false)
	{
		if ($this->position <= 0)
			$this->position = Attribute::getHigherPosition($this->id_attribute_group) + 1;

		$return = parent::add($autodate, $null_values);
		if ($return)
			Hook::exec('afterSaveAttribute', array('id_attribute' => $this->id));
		return $return;
	}

	/**
	 * Get all attributes for a given language
	 *
	 * @param integer $id_lang Language id
	 * @param boolean $notNull Get only not null fields if true
	 * @return array Attributes
	 */
	public static function getAttributes($id_lang, $not_null = false)
	{
		if (!Combination::isFeatureActive())
			return array();
		return Db::getInstance()->executeS('
			SELECT ag.*, agl.*, a.`id_attribute`, al.`name`, agl.`name` AS `attribute_group`
			FROM `'._DB_PREFIX_.'attribute_group` ag
			LEFT JOIN `'._DB_PREFIX_.'attribute_group_lang` agl
				ON (ag.`id_attribute_group` = agl.`id_attribute_group` AND agl.`id_lang` = '.(int)$id_lang.')
			LEFT JOIN `'._DB_PREFIX_.'attribute` a
				ON a.`id_attribute_group` = ag.`id_attribute_group`
			LEFT JOIN `'._DB_PREFIX_.'attribute_lang` al
				ON (a.`id_attribute` = al.`id_attribute` AND al.`id_lang` = '.(int)$id_lang.')
			'.($not_null ? 'WHERE a.`id_attribute` IS NOT NULL AND al.`name` IS NOT NULL' : '').'
			ORDER BY agl.`name` ASC, a.`position` ASC
		');
	}

	/**
	 * Get quantity for a given attribute combinaison
	 * Check if quantity is enough to deserve customer
	 *
	 * @param integer $id_product_attribute Product attribute combinaison id
	 * @param integer $qty Quantity needed
	 * @return boolean Quantity is available or not
	 */
	public static function checkAttributeQty($id_product_attribute, $qty, Shop $shop = null)
	{
		if (!$shop)
			$shop = Context::getContext()->shop;

		$result = StockAvailable::getQuantityAvailableByProduct(null, (int)$id_product_attribute, $shop->getID());

		/*$sql = 'SELECT quantity
				FROM '._DB_PREFIX_.'stock_available
				WHERE id_product_attribute = '.(int)$id_product_attribute
				.$shop->addSqlRestriction();
		$result = (int)Db::getInstance()->getValue($sql);*/

		return ($result && $qty <= $result);
	}

	/**
	 * Get quantity for product with attributes quantity
	 *
	 * @deprecated since 1.5.0, use Product->getStock()
	 * @param integer $id_product
	 * @return mixed Quantity or false
	 */
	public static function getAttributeQty($id_product)
	{
		Tools::displayAsDeprecated();

		return StockAvailable::getQuantityAvailableByProduct($id_product);

		/*
		$row = Db::getInstance()->getRow('
			SELECT SUM(quantity) as quantity
			FROM `'._DB_PREFIX_.'product_attribute`
			WHERE `id_product` = '.(int)$id_product
		);

		if ($row['quantity'] !== null)
			return (int)$row['quantity'];
		return false;
		*/
	}

	/**
	 * Update array with veritable quantity
	 *
	 * @deprecated since 1.5.0
	 * @param array &$arr
	 * @return bool
	 */
	public static function updateQtyProduct(&$arr)
	{
		Tools::displayAsDeprecated();

		$id_product = (int)$arr['id_product'];
		$qty = self::getAttributeQty($id_product);

		if ($qty !== false)
		{
			$arr['quantity'] = (int)$qty;
			return true;
		}
		return false;
	}

	/**
	 * Return true if attribute is color type
	 *
	 * @acces public
	 * @return bool
	 */
	public function isColorAttribute()
	{
		if (!Db::getInstance()->getRow('
			SELECT `group_type`
			FROM `'._DB_PREFIX_.'attribute_group`
			WHERE `id_attribute_group` = (
				SELECT `id_attribute_group`
				FROM `'._DB_PREFIX_.'attribute`
				WHERE `id_attribute` = '.(int)$this->id.')
			AND group_type = \'color\''))
			return false;
		return Db::getInstance()->numRows();
	}

	/**
	 * Get minimal quantity for product with attributes quantity
	 *
	 * @acces public static
	 * @param integer $id_product_attribute
	 * @return mixed Minimal Quantity or false
	 */
	public static function getAttributeMinimalQty($id_product_attribute)
	{
		$minimal_quantity = Db::getInstance()->getValue('
			SELECT `minimal_quantity`
			FROM `'._DB_PREFIX_.'product_attribute`
			WHERE `id_product_attribute` = '.(int)$id_product_attribute
		);

		if ($minimal_quantity > 1)
			return (int)$minimal_quantity;
		return false;
	}

	/**
	 * Move an attribute inside its group
	 * @param boolean $way Up (1)  or Down (0)
	 * @param integer $position
	 * @return boolean Update result
	 */
	public function updatePosition($way, $position)
	{
		if (!$res = Db::getInstance()->executeS('
			SELECT a.`id_attribute`, a.`position`, a.`id_attribute_group`
			FROM `'._DB_PREFIX_.'attribute` a
			WHERE a.`id_attribute_group` = '.(int)Tools::getValue('id_attribute_group', 1).'
			ORDER BY a.`position` ASC'
		))
			return false;

		foreach ($res as $attribute)
			if ((int)$attribute['id_attribute'] == (int)$this->id)
				$moved_attribute = $attribute;

		if (!isset($moved_attribute) || !isset($position))
			return false;
p('
			UPDATE `'._DB_PREFIX_.'attribute`
			SET `position`= `position` '.($way ? '- 1' : '+ 1').'
			WHERE `position`
			'.($way
				? '> '.(int)$moved_attribute['position'].' AND `position` <= '.(int)$position
				: '< '.(int)$moved_attribute['position'].' AND `position` >= '.(int)$position).'
			AND `id_attribute_group`='.(int)$moved_attribute['id_attribute_group']);
		// < and > statements rather than BETWEEN operator
		// since BETWEEN is treated differently according to databases
		return (Db::getInstance()->execute('
			UPDATE `'._DB_PREFIX_.'attribute`
			SET `position`= `position` '.($way ? '- 1' : '+ 1').'
			WHERE `position`
			'.($way
				? '> '.(int)$moved_attribute['position'].' AND `position` <= '.(int)$position
				: '< '.(int)$moved_attribute['position'].' AND `position` >= '.(int)$position).'
			AND `id_attribute_group`='.(int)$moved_attribute['id_attribute_group'])
		&& Db::getInstance()->execute('
			UPDATE `'._DB_PREFIX_.'attribute`
			SET `position` = '.(int)$position.'
			WHERE `id_attribute` = '.(int)$moved_attribute['id_attribute'].'
			AND `id_attribute_group`='.(int)$moved_attribute['id_attribute_group']));
	}

	/**
	 * Reorder attribute position in group $id_attribute_group.
	 * Call it after deleting an attribute from a group.
	 *
	 * @param int $id_attribute_group
	 * @param bool $use_last_attribute
	 * @return bool $return
	 */
	public function cleanPositions($id_attribute_group, $use_last_attribute = true)
	{
		$return = true;

		$sql = '
		SELECT `id_attribute`
		FROM `'._DB_PREFIX_.'attribute`
		WHERE `id_attribute_group` = '.(int)$id_attribute_group;
		// when delete, you must use $use_last_attribute
		if ($use_last_attribute)
			$sql .= '
			AND `id_attribute` != '.(int)$this->id;
		$sql .= '
		ORDER BY `position`';
		$result = Db::getInstance()->executeS($sql);

		$i = 0;
		foreach ($result as $value)
			$return = Db::getInstance()->execute('
			UPDATE `'._DB_PREFIX_.'attribute`
			SET `position` = '.(int)$i++.'
			WHERE `id_attribute_group` = '.(int)$id_attribute_group.'
			AND `id_attribute` = '.(int)$value['id_attribute']);
		return $return;
	}

	/**
	 * getHigherPosition
	 *
	 * Get the higher attribute position from a group attribute
	 *
	 * @param integer $id_attribute_group
	 * @return integer $position
	 */
	public static function getHigherPosition($id_attribute_group)
	{
		$sql = 'SELECT MAX(`position`)
				FROM `'._DB_PREFIX_.'attribute`
				WHERE id_attribute_group = '.(int)$id_attribute_group;
		$position = DB::getInstance()->getValue($sql);
		return (is_numeric($position)) ? $position : -1;
	}
}
