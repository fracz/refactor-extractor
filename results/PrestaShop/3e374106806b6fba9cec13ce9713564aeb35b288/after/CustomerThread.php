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

class CustomerThreadCore extends ObjectModel
{
	public $id;
	public $id_shop;
	public $id_lang;
	public $id_contact;
	public $id_customer;
	public $id_order;
	public $id_product;
	public $status;
	public $email;
	public $token;
	public $date_add;
	public $date_upd;

	protected $table = 'customer_thread';
	protected $identifier = 'id_customer_thread';

	protected $fieldsRequired = array('id_lang', 'id_contact', 'token');
	protected $fieldsSize = array('email' => 254);

	protected $fieldsValidate = array(
		'id_lang' => 'isUnsignedId',
		'id_contact' => 'isUnsignedId',
		'id_shop' => 'isUnsignedId',
		'id_customer' => 'isUnsignedId',
		'id_order' => 'isUnsignedId',
		'id_product' => 'isUnsignedId',
		'email' => 'isEmail',
		'token' => 'isGenericName'
	);

	public	function getFields()
	{
	 	$this->validateFields();
		$fields['id_lang'] = (int)$this->id_lang;
		$fields['id_shop'] = (int)$this->id_shop;
		$fields['id_contact'] = (int)$this->id_contact;
		$fields['id_customer'] = (int)$this->id_customer;
		$fields['id_order'] = (int)$this->id_order;
		$fields['id_product'] = (int)$this->id_product;
		$fields['status'] = pSQL($this->status);
		$fields['email'] = pSQL($this->email);
		$fields['token'] = pSQL($this->token);
		$fields['date_add'] = pSQL($this->date_add);
		$fields['date_upd'] = pSQL($this->date_upd);
		return $fields;
	}

	public function delete()
	{
		if (!Validate::isUnsignedId($this->id))
			return false;
		Db::getInstance()->execute('
			DELETE FROM `'._DB_PREFIX_.'customer_message`
			WHERE `id_customer_thread` = '.(int)$this->id
		);
		return (parent::delete());
	}

	public static function getCustomerMessages($id_customer)
	{
		return Db::getInstance()->executeS('
			SELECT *
			FROM '._DB_PREFIX_.'customer_thread ct
			LEFT JOIN '._DB_PREFIX_.'customer_message cm
				ON ct.id_customer_thread = cm.id_customer_thread
			WHERE id_customer = '.(int)$id_customer
		);
	}

	public static function getIdCustomerThreadByEmailAndIdOrder($email, $id_order)
	{
		return Db::getInstance()->getValue('
			SELECT cm.id_customer_thread
			FROM '._DB_PREFIX_.'customer_thread cm
			WHERE cm.email = \''.pSQL($email).'\'
				AND cm.id_shop = '.(int)Context::getContext()->shop->getId(true).'
				AND cm.id_order = '.(int)$id_order
		);
	}

	public static function getContacts()
	{
		return Db::getInstance()->executeS('
			SELECT cl.*, COUNT(*) as total, (
				SELECT id_customer_thread
				FROM '._DB_PREFIX_.'customer_thread ct2
				WHERE status = "open" AND ct.id_contact = ct2.id_contact
				ORDER BY date_upd ASC
				LIMIT 1
			) as id_customer_thread
			FROM '._DB_PREFIX_.'customer_thread ct
			LEFT JOIN '._DB_PREFIX_.'contact_lang cl
				ON (cl.id_contact = ct.id_contact AND cl.id_lang = '.(int)Context::getContext()->language->id.')
			WHERE ct.status = "open"
				AND ct.id_contact IS NOT NULL
				AND cl.id_contact IS NOT NULL
			GROUP BY ct.id_contact HAVING COUNT(*) > 0
		');
	}

	public static function getTotalCustomerThreads($where = null)
	{
		if (is_null($where))
			return (int)Db::getInstance()->getValue('
				SELECT COUNT(*)
				FROM '._DB_PREFIX_.'customer_thread
			');
		else
			return (int)Db::getInstance()->getValue(sprintf('
				SELECT COUNT(*)
				FROM '._DB_PREFIX_.'customer_thread
				WHERE %s
			', $where));
	}

	public static function getMessageCustomerThreads($id_customer_thread)
	{
		return Db::getInstance()->executeS('
			SELECT ct.*, cm.*, cl.name subject, CONCAT(e.firstname, \' \', e.lastname) employee_name,
				CONCAT(c.firstname, \' \', c.lastname) customer_name, c.firstname
			FROM '._DB_PREFIX_.'customer_thread ct
			LEFT JOIN '._DB_PREFIX_.'customer_message cm
				ON (ct.id_customer_thread = cm.id_customer_thread)
			LEFT JOIN '._DB_PREFIX_.'contact_lang cl
				ON (cl.id_contact = ct.id_contact AND cl.id_lang = '.(int)Context::getContext()->language->id.')
			LEFT JOIN '._DB_PREFIX_.'employee e
				ON e.id_employee = cm.id_employee
			LEFT JOIN '._DB_PREFIX_.'customer c
				ON (IFNULL(ct.id_customer, ct.email) = IFNULL(c.id_customer, c.email))
			WHERE ct.id_customer_thread = '.(int)$id_customer_thread.'
			ORDER BY cm.date_add DESC
		');
	}

	public static function getNextThread($id_customer_thread)
	{
		$context = Context::getContext();
		return Db::getInstance()->getValue('
			SELECT id_customer_thread
			FROM '._DB_PREFIX_.'customer_thread ct
			WHERE ct.status = "open"
			AND ct.date_upd = (
				SELECT date_add FROM '._DB_PREFIX_.'customer_message
				WHERE (id_employee IS NULL OR id_employee = 0)
					AND id_customer_thread = '.(int)$id_customer_thread.'
				ORDER BY date_add DESC LIMIT 1
			)
			'.($context->cookie->{'customer_threadFilter_cl!id_contact'} ?
				'AND ct.id_contact = '.(int)$context->cookie->{'customer_threadFilter_cl!id_contact'} : '').'
			'.($context->cookie->{'customer_threadFilter_l!id_lang'} ?
				'AND ct.id_lang = '.(int)$context->cookie->{'customer_threadFilter_l!id_lang'} : '').
			' ORDER BY ct.date_upd ASC
		');
	}
}
