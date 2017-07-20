<?php
/**
 * CakePHP(tm) : Rapid Development Framework (http://cakephp.org)
 * Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright     Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 * @link          http://cakephp.org CakePHP(tm) Project
 * @since         CakePHP(tm) v 3.0.0
 * @license       MIT License (http://www.opensource.org/licenses/mit-license.php)
 */
namespace Cake\Model\Behavior;

use ArrayObject;
use Cake\Collection\Collection;
use Cake\Database\Expression\QueryExpression;
use Cake\Event\Event;
use Cake\ORM\Behavior;
use Cake\ORM\Entity;
use Cake\ORM\Table;
use Cake\ORM\TableRegistry;

class TreeBehavior extends Behavior {

/**
 * Table instance
 *
 * @var \Cake\ORM\Table
 */
	protected $_table;

/**
 * Default config
 *
 * These are merged with user-provided configuration when the behavior is used.
 *
 * @var array
 */
	protected static $_defaultConfig = [
		'implementedFinders' => [
			'path' => 'findPath',
			'children' => 'findChildren',
		],
		'parent' => 'parent_id',
		'left' => 'lft',
		'right' => 'rght',
		'scope' => null
	];

/**
 * Constructor
 *
 * @param Table $table The table this behavior is attached to.
 * @param array $config The config for this behavior.
 */
	public function __construct(Table $table, array $config = []) {
		parent::__construct($table, $config);
		$this->_table = $table;
	}

	public function findPath($query, $options) {
		if (empty($options['for'])) {
			throw new \InvalidArgumentException("The 'for' key is required for find('path')");
		}

		$config = $this->config();
		list($left, $right) = [$config['left'], $config['right']];
		$node = $this->_table->get($options['for'], ['fields' => [$left, $right]]);

		return $this->_scope($query)
			->where([
				"$left <=" => $node->get($left),
				"$right >=" => $node->get($right)
			]);
	}

/**
 * Get the number of child nodes.
 *
 * @param integer|string $id The ID of the record to read
 * @param boolean $direct whether to count direct, or all, children
 * @return integer Number of child nodes.
 */
	public function childCount($id, $direct = false) {
		$config = $this->config();
		list($parent, $left, $right) = [$config['parent'], $config['left'], $config['right']];

		if ($direct) {
			$count = $this->_table->find()
				->where([$parent => $id])
				->count();
			return $count;
		}

		$node = $this->_table->get($id, [$this->_table->primaryKey() => $id]);

		return ($node->{$right} - $node->{$left} - 1) / 2;
	}

/**
 * Get the child nodes of the current model
 *
 * Available options are:
 *
 * - for: The ID of the record to read.
 * - direct: Boolean, whether to return only the direct (true), or all (false), children. default to false (all children).
 *
 * If the direct option is set to true, only the direct children are returned (based upon the parent_id field)
 * If false is passed for the id parameter, top level, or all (depending on direct parameter appropriate) are counted.
 *
 * @param array $options Array of options as described above
 * @return \Cake\ORM\Query
 * @throws \Cake\ORM\Error\RecordNotFoundException When node was not found
 * @throws \InvalidArgumentException When the 'for' key is not passed in $options
 */
	public function findChildren($query, $options) {
		$config = $this->config();
		$options += ['for' => null, 'direct' => false];
		list($parent, $left, $right) = [$config['parent'], $config['left'], $config['right']];
		list($for, $direct) = [$options['for'], $options['direct']];
		$primaryKey = $this->_table->primaryKey();

		if (empty($for)) {
			throw new \InvalidArgumentException("The 'for' key is required for find('children')");
		}

		if ($query->clause('order') === null) {
			$query->order([$left => 'ASC']);
		}

		if ($direct) {
			return $this->_scope($query)->where([$parent => $for]);
		}

		$node = $this->_scope($this->_table->find())
			->select([$right, $left])
			->where([$primaryKey => $for])
			->first();

		if (!$node) {
			throw new \Cake\ORM\Error\RecordNotFoundException("Node \"{$for}\ was not found in the tree.");
		}

		return $this->_scope($query)
			->where([
				"{$right} <" => $node->{$right},
				"{$left} >" => $node->{$left}
			]);
	}

/**
 * Reorder the node without changing the parent.
 *
 * If the node is the first child, or is a top level node with no previous node this method will return false
 *
 * @param integer|string $id The ID of the record to move
 * @param integer|boolean $number How many places to move the node, or true to move to first position
 * @return boolean true on success, false on failure
 */
	public function moveUp($id, $number = 1) {
		$config = $this->config();
		list($parent, $left, $right) = [$config['parent'], $config['left'], $config['right']];
		$primaryKey = $this->_table->primaryKey();

		if (!$number) {
			return false;
		}

		$node = $this->_scope($this->_table->find())
			->select([$parent, $left, $right])
			->where([$primaryKey => $id])
			->first();

		if (!$node) {
			return false;
		}

		if ($node->{$parent}) {
			$parentNode = $this->_table->get($node->{$parent}, ['fields' => [$left, $right]]);

			if (($node->{$left} - 1) == $parentNode->{$left}) {
				return false;
			}
		}

		$previousNode = $this->_scope($this->_table->find())
			->select([$left, $right])
			->where([$right => ($node->{$left} - 1)])
			->first();

		if (!$previousNode) {
			return false;
		}

		$edge = $this->_getMax();
		$this->_sync($edge - $previousNode->{$left} + 1, '+', "BETWEEN {$previousNode->{$left}} AND {$previousNode->{$right}}");
		$this->_sync($node->{$left} - $previousNode->{$left}, '-', "BETWEEN {$node->{$left}} AND {$node->{$right}}");
		$this->_sync($edge - $previousNode->{$left} - ($node->{$right} - $node->{$left}), '-', "> {$edge}");

		if (is_int($number)) {
			$number--;
		}

		if ($number) {
			$this->moveUp($id, $number);
		}

		return true;
	}

/**
 * Reorder the node without changing the parent.
 *
 * If the node is the last child, or is a top level node with no subsequent node this method will return false
 *
 * @param integer|string $id The ID of the record to move
 * @param integer|boolean $number How many places to move the node or true to move to last position
 * @return boolean true on success, false on failure
 */
	public function moveDown($id, $number = 1) {
		$config = $this->config();
		list($parent, $left, $right) = [$config['parent'], $config['left'], $config['right']];
		$primaryKey = $this->_table->primaryKey();

		if (!$number) {
			return false;
		}

		$node = $this->_scope($this->_table->find())
			->select([$parent, $left, $right])
			->where([$primaryKey => $id])
			->first();

		if (!$node) {
			return false;
		}

		if ($node->{$parent}) {
			$parentNode = $this->_table->get($node->{$parent}, ['fields' => [$left, $right]]);

			if (($node->{$right} + 1) == $parentNode->{$right}) {
				return false;
			}
		}

		$nextNode = $this->_scope($this->_table->find())
			->select([$left, $right])
			->where([$left => $node->{$right} + 1])
			->first();

		if (!$nextNode) {
			return false;
		}

		$edge = $this->_getMax();
		$this->_sync($edge - $node->{$left} + 1, '+', "BETWEEN {$node->{$left}} AND {$node->{$right}}");
		$this->_sync($nextNode->{$left} - $node->{$left}, '-', "BETWEEN {$nextNode->{$left}} AND {$nextNode->{$right}}");
		$this->_sync($edge - $node->{$left} - ($nextNode->{$right} - $nextNode->{$left}), '-', "> {$edge}");

		if (is_int($number)) {
			$number--;
		}

		if ($number) {
			$this->moveDown($id, $number);
		}

		return true;
	}

/**
 * Recovers the lft and right column values out of the hirearchy defined by the
 * parent column.
 *
 * @return void
 */
	public function recover() {
		$this->_table->connection()->transactional(function() {
			$this->_recoverTree();
		});
	}

/**
 * Recursive method used to recover a single level of the tree
 *
 * @param integer $counter The Last left column value that was assigned
 * @param mixed $parentId the parent id of the level to be recovered
 * @return integer Ne next value to use for the left column
 */
	protected function _recoverTree($counter = 0, $parentId = null) {
		$config = $this->config();
		list($parent, $left, $right) = [$config['parent'], $config['left'], $config['right']];
		$pk = (array)$this->_table->primaryKey();

		$query = $this->_scope($this->_table->query())
			->select($pk)
			->where(function($exp) use ($parentId, $parent) {
				return $parentId === null ? $exp->isNull($parent) : $exp->eq($parent, $parentId);
			})
			->order($pk)
			->hydrate(false)
			->bufferResults(false);

		$leftCounter = $counter;
		foreach ($query as $row) {
			$counter++;
			$counter = $this->_recoverTree($counter, $row[$pk[0]]);
		}

		if ($parentId === null) {
			return $counter;
		}

		$this->_table->updateAll(
			[$left => $leftCounter, $right => $counter + 1],
			[$pk[0] => $parentId]
		);

		return $counter + 1;
	}

/**
 * Get the maximum index value in the table.
 *
 * @return integer
 */
	protected function _getMax() {
		return $this->_getMaxOrMin('max');
	}

/**
 * Get the minimum index value in the table.
 *
 * @return integer
 */
	protected function _getMin() {
		return $this->_getMaxOrMin('min');
	}

/**
 * Get the maximum|minimum index value in the table.
 *
 * @param string $maxOrMin Either 'max' or 'min'
 * @return integer
 */
	protected function _getMaxOrMin($maxOrMin = 'max') {
		extract($this->config());
		$LorR = $maxOrMin === 'max' ? $right : $left;
		$DorA = $maxOrMin === 'max' ? 'DESC' : 'ASC';

		$edge = $this->_scope($this->_table->find())
			->select([$LorR])
			->order([$LorR => $DorA])
			->first();

		if (empty($edge->{$LorR})) {
			return 0;
		}

		return $edge->{$LorR};
	}

	protected function _sync($shift, $dir = '+', $conditions = null, $field = 'both') {
		extract($this->config());

		if ($field === 'both') {
			$this->_sync($shift, $dir, $conditions, $left);
			$field = $right;
		}

		// updateAll + scope
		$exp = new QueryExpression();
		$exp->add("{$field} = ({$field} {$dir} {$shift})");

		$query = $this->_scope($this->_table->query());
		$query->update()
			->set($exp);

		if ($conditions) {
			$conditions = "{$field} {$conditions}";
			$query->where($conditions);
		}

		$statement = $query->execute();
		$success = $statement->rowCount() > 0;

		return $success;
	}

	protected function _scope($query) {
		$config = $this->config();

		if (empty($config['scope'])) {
			return $query;
		} elseif (is_array($config['scope'])) {
			return $query->where($config['scope']);
		} elseif (is_callable($config['scope'])) {
			return $config['scope']($query);
		}

		return $query;
	}
}