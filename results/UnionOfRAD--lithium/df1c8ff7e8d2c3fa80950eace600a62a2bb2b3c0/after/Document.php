<?php
/**
 * Lithium: the most rad php framework
 *
 * @copyright     Copyright 2009, Union of RAD (http://union-of-rad.org)
 * @license       http://opensource.org/licenses/bsd-license.php The BSD License
 */

namespace lithium\data\model;

use \Iterator;

/**
 * `Document` is an alternative to `model\RecordSet`, which is optimized for organizing collections
 * of records from document-oriented databases such as CouchDB or MongoDB.
 */
class Document extends \lithium\util\Collection {

	/**
	 * The fully-namespaced class name of the model object to which this document is bound. This
	 * is usually the model that executed the query which created this object.
	 *
	 * @var string
	 */
	protected $_model = null;

	/**
	 * A reference to the object that originated this record set; usually an instance of
	 * `lithium\data\Source` or `lithium\data\source\Database`. Used to load column definitions and
	 * lazy-load records.
	 *
	 * @var object
	 */
	protected $_handle = null;

	/**
	 * A reference to the query object that originated this record set; usually an instance of
	 * `lithium\data\model\Query`.
	 *
	 * @var object
	 */
	protected $_query = null;

	/**
	 * A pointer or resource that is used to load records from the object (`$_handle`) that
	 * originated this record set.
	 *
	 * @var resource
	 */
	protected $_result = null;

	/**
	 * A reference to this object's parent `Document` object.
	 *
	 * @var object
	 */
	protected $_parent = null;

	/**
	 * Indicates whether this document has already been created in the database.
	 *
	 * @var boolean
	 */
	protected $_exists = false;

	/**
	 * The class dependencies for `Document`.
	 *
	 * @var array
	 */
	protected $_classes = array(
		'media' => '\lithium\http\Media',
		'record' => '\lithium\data\model\Document',
		'recordSet' => '\lithium\data\model\Document'
	);

	protected $_hasInitialized = false;

	protected $_autoConfig = array(
		'items', 'classes' => 'merge', 'handle', 'model', 'result', 'query', 'parent', 'exists'
	);

	public function __construct($config = array()) {
		if (isset($config['data']) && !isset($config['items'])) {
			$config['items'] = $config['data'];
			unset($config['data']);
		}
		parent::__construct($config);
	}

	/**
	 * Magic php method used when asking for field as property on document
	 *
	 * @example $doc->id
	 * @param $name field name
	 * @return mixed
	 */
	public function __get($name) {
		if (!isset($this->_items[$name])) {
			return null;
		}
		$items = $this->_items[$name];

		if ($this->_isComplexType($items) && !$items instanceof Iterator) {
			$model = $this->_model;
			$parent = $this;
			$this->_items[$name] = $this->_record('recordSet', $this->_items[$name]);
		}
		return $this->_items[$name];
	}

	/**
	 * Set a value to $name
	 *
	 * @example $doc->set('title', 'Lorem Ipsum');
	 * @param $name field
	 * @param $value
	 * @return void
	 */
	public function set($name, $value = null) {
		$this->__set($name, $value);
	}

	/**
	 * Magical method called by setting a property on the document instance
	 *
	 * @example $document->title = 'Lorem Ipsum';
	 * @param $name field
	 * @param $value
	 * @return void
	 */
	public function __set($name, $value = null) {
		if (is_array($name) && empty($value)) {
			$this->_items = $name + $this->_items;
			return;
		}
		if ($this->_isComplexType($value) && !$value instanceof Iterator) {
			$value = $this->_record('recordSet', $value);
		}
		$this->_items[$name] = $value;
	}

	/**
	 * Return pointer to first item and return it's data as an array
	 *
	 * @return array of values of current item
	 */
	public function rewind() {
		$this->_valid = (reset($this->_items) !== false);

		if (!$this->_valid && !$this->_hasInitialized) {
			$this->_hasInitialized = true;

			if ($record = $this->_populate()) {
				$this->_valid = true;
				return $record;
			}
		}
		return current($this->_items);
	}

	/**
	 * Magic php methoed used when model method is called on document instance
	 * return null also if no model is set
	 *
	 * @param $method
	 * @param $params
	 * @return mixed
	 */
	public function __call($method, $params = array()) {
		if (!$model = $this->_model) {
			return null;
		}
		array_unshift($params, $this);
		$class = $model::invokeMethod('_instance');
		return call_user_func_array(array(&$class, $method), $params);
	}

	/**
	 * Returns the next record in the set, and advances the object's internal pointer. If the end of
	 * the set is reached, a new record will be fetched from the data source connection handle
	 * (`$_handle`). If no more records can be fetched, returns `null`.
	 *
	 * @return object Returns the next record in the set, or `null`, if no more records are
	 *                available.
	 */
	public function next() {
		$this->_valid = (next($this->_items) !== false);
		$this->_valid = $this->_valid ?: !is_null($this->_populate());
		return $this->_valid ? $this->current() : null;
	}

	/**
	 * Returns value of _exists, assumed boolean for datasource key exists
	 *
	 * return boolean
	 */
	public function exists() {
		return $this->_exists;
	}

	/**
	 * Gets the raw data associated with this document.
	 *
	 * @return array Returns a raw array of document data
	 */
	public function data() {
		return $this->to('array');
	}

	public function _isComplexType($data) {
		if (is_scalar($data) || !$data) {
			return false;
		}
		if (is_array($data)) {
			if (array_keys($data) == range(0, count($data) - 1)) {
				if (array_filter($data, 'is_scalar') == array_filter($data)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 *
	 * @param $id
	 * @return void
	 */
	protected function _update($id = null) {
		if ($id) {
			$model = $this->_model;
			$key = $model::meta('key');
			$this->__set($key, $id);
		}
		$this->_exists = true;
	}

	/**
	 *
	 * @param $items
	 * @param $key
	 * @return array
	 */
	protected function _populate($items = null, $key = null) {
		if ($this->_closed() || !$this->_handle) {
			return;
		}
		if (!$items = $items ?: $this->_handle->result('next', $this->_result, $this)) {
			return $this->_close();
		}
		return $this->_items[] = $record = $this->_record('record', $items);
	}

	/**
	 *
	 * @param $class
	 * @param $items
	 * @return object
	 */
	protected function _record($classType, $items) {
		$parent = $this;
		$model = $this->_model;
		$exists = true;
		return new $this->_classes[$classType](compact('model', 'items', 'parent', 'exists'));
	}

	/**
	 * Executes when the associated result resource pointer reaches the end of its record set. The
	 * resource is freed by the connection, and the reference to the connection is unlinked.
	 *
	 * @return void
	 */
	protected function _close() {
		if (!$this->_closed()) {
			$this->_result = $this->_handle->result('close', $this->_result, $this);
			unset($this->_handle);
			$this->_handle = null;
		}
	}

	/**
	 * Checks to see if this record set has already fetched all available records and freed the
	 * associated result resource.
	 *
	 * @return boolean Returns true if all records are loaded and the database resources have been
	 *         freed, otherwise returns false.
	 */
	protected function _closed() {
		return (empty($this->_result) || empty($this->_handle));
	}
}

?>