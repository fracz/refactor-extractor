<?php
/**
 * @link http://www.yiiframework.com/
 * @copyright Copyright (c) 2008 Yii Software LLC
 * @license http://www.yiiframework.com/license/
 */

namespace yii\caching;

use Yii;
use yii\base\InvalidConfigException;
use yii\db\Connection;

/**
 * DbDependency represents a dependency based on the query result of a SQL statement.
 *
 * If the query result changes, the dependency is considered as changed.
 * The query is specified via the [[sql]] property.
 *
 * @author Qiang Xue <qiang.xue@gmail.com>
 * @since 2.0
 */
class DbDependency extends Dependency
{
	/**
	 * @var string the ID of the [[Connection|DB connection]] application component. Defaults to 'db'.
	 */
	public $connectionID = 'db';
	/**
	 * @var string the SQL query whose result is used to determine if the dependency has been changed.
	 * Only the first row of the query result will be used.
	 */
	public $sql;
	/**
	 * @var array the parameters (name=>value) to be bound to the SQL statement specified by [[sql]].
	 */
	public $params;

	/**
	 * Constructor.
	 * @param string $sql the SQL query whose result is used to determine if the dependency has been changed.
	 * @param array $params the parameters (name=>value) to be bound to the SQL statement specified by [[sql]].
	 * @param array $config name-value pairs that will be used to initialize the object properties
	 */
	public function __construct($sql, $params = array(), $config = array())
	{
		$this->sql = $sql;
		$this->params = $params;
		parent::__construct($config);
	}

	/**
	 * PHP sleep magic method.
	 * This method ensures that the database instance is set null because it contains resource handles.
	 * @return array
	 */
	public function __sleep()
	{
		$this->_db = null;
		return array_keys((array)$this);
	}

	/**
	 * Generates the data needed to determine if dependency has been changed.
	 * This method returns the value of the global state.
	 * @return mixed the data needed to determine if dependency has been changed.
	 */
	protected function generateDependencyData()
	{
		$db = $this->getDb();
		if ($db->enableQueryCache) {
			// temporarily disable and re-enable query caching
			$db->enableQueryCache = false;
			$result = $db->createCommand($this->sql, $this->params)->queryRow();
			$db->enableQueryCache = true;
		} else {
			$result = $db->createCommand($this->sql, $this->params)->queryRow();
		}
		return $result;
	}

	/**
	 * @var Connection the DB connection instance
	 */
	private $_db;

	/**
	 * Returns the DB connection instance used for caching purpose.
	 * @return Connection the DB connection instance
	 * @throws InvalidConfigException if [[connectionID]] does not point to a valid application component.
	 */
	public function getDb()
	{
		if ($this->_db === null) {
			$db = Yii::$app->getComponent($this->connectionID);
			if ($db instanceof Connection) {
				$this->_db = $db;
			} else {
				throw new InvalidConfigException("DbCacheDependency::connectionID must refer to the ID of a DB application component.");
			}
		}
		return $this->_db;
	}

	/**
	 * Sets the DB connection used by the cache component.
	 * @param Connection $value the DB connection instance
	 */
	public function setDb($value)
	{
		$this->_db = $value;
	}
}