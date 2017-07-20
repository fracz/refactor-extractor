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
namespace Cake\Database\Schema;

use Cake\Database\Exception;
use Cake\Database\Schema\Table;

/**
 * Schema generation/reflection features for MySQL
 */
class MysqlSchema extends BaseSchema {

/**
 * {@inheritdoc}
 */
	public function listTablesSql($config) {
		return ['SHOW TABLES FROM ' . $this->_driver->quoteIdentifier($config['database']), []];
	}

/**
 * {@inheritdoc}
 */
	public function describeTableSql($name, $config) {
		return ['SHOW FULL COLUMNS FROM ' . $this->_driver->quoteIdentifier($name), []];
	}

/**
 * {@inheritdoc}
 */
	public function describeIndexSql($table, $config) {
		return ['SHOW INDEXES FROM ' . $this->_driver->quoteIdentifier($table), []];
	}

/**
 * Convert a MySQL column type into an abstract type.
 *
 * The returned type will be a type that Cake\Database\Type can handle.
 *
 * @param string $column The column type + length
 * @return array Array of column information.
 * @throws Cake\Database\Exception When column type cannot be parsed.
 */
	protected function _convertColumn($column) {
		preg_match('/([a-z]+)(?:\(([0-9,]+)\))?\s*([a-z]+)?/i', $column, $matches);
		if (empty($matches)) {
			throw new Exception(__d('cake_dev', 'Unable to parse column type from "%s"', $column));
		}

		$col = strtolower($matches[1]);
		$length = $precision = null;
		if (isset($matches[2])) {
			$length = $matches[2];
			if (strpos($matches[2], ',') !== false) {
				list($length, $precision) = explode(',', $length);
			}
			$length = (int)$length;
			$precision = (int)$precision;
		}

		if (in_array($col, array('date', 'time', 'datetime', 'timestamp'))) {
			return ['type' => $col, 'length' => null];
		}
		if (($col === 'tinyint' && $length === 1) || $col === 'boolean') {
			return ['type' => 'boolean', 'length' => null];
		}

		$unsigned = (isset($matches[3]) && strtolower($matches[3]) === 'unsigned');
		if (strpos($col, 'bigint') !== false || $col === 'bigint') {
			return ['type' => 'biginteger', 'length' => $length, 'unsigned' => $unsigned];
		}
		if (strpos($col, 'int') !== false) {
			return ['type' => 'integer', 'length' => $length, 'unsigned' => $unsigned];
		}
		if ($col === 'char' && $length === 36) {
			return ['type' => 'uuid', 'length' => null];
		}
		if ($col === 'char') {
			return ['type' => 'string', 'fixed' => true, 'length' => $length];
		}
		if (strpos($col, 'char') !== false || $col === 'tinytext') {
			return ['type' => 'string', 'length' => $length];
		}
		if (strpos($col, 'text') !== false) {
			return ['type' => 'text', 'length' => $length];
		}
		if (strpos($col, 'blob') !== false || $col === 'binary') {
			return ['type' => 'binary', 'length' => $length];
		}
		if (strpos($col, 'float') !== false || strpos($col, 'double') !== false) {
			return [
				'type' => 'float',
				'length' => $length,
				'precision' => $precision,
				'unsigned' => $unsigned
			];
		}
		if (strpos($col, 'decimal') !== false) {
			return [
				'type' => 'decimal',
				'length' => $length,
				'precision' => $precision,
				'unsigned' => $unsigned
			];
		}
		return ['type' => 'text', 'length' => null];
	}

/**
 * {@inheritdoc}
 *
 */
	public function convertFieldDescription(Table $table, $row) {
		$field = $this->_convertColumn($row['Type']);
		$field += [
			'null' => $row['Null'] === 'YES' ? true : false,
			'default' => $row['Default'],
			'collate' => $row['Collation'],
			'comment' => $row['Comment'],
		];
		$table->addColumn($row['Field'], $field);
	}

/**
 * {@inheritdoc}
 *
 */
	public function convertIndexDescription(Table $table, $row) {
		$type = null;
		$columns = $length = [];

		$name = $row['Key_name'];
		if ($name === 'PRIMARY') {
			$name = $type = Table::CONSTRAINT_PRIMARY;
		}

		$columns[] = $row['Column_name'];

		if ($row['Index_type'] === 'FULLTEXT') {
			$type = Table::INDEX_FULLTEXT;
		} elseif ($row['Non_unique'] == 0 && $type !== 'primary') {
			$type = Table::CONSTRAINT_UNIQUE;
		} elseif ($type !== 'primary') {
			$type = Table::INDEX_INDEX;
		}

		if (!empty($row['Sub_part'])) {
			$length[$row['Column_name']] = $row['Sub_part'];
		}
		$isIndex = (
			$type === Table::INDEX_INDEX ||
			$type === Table::INDEX_FULLTEXT
		);
		if ($isIndex) {
			$existing = $table->index($name);
		} else {
			$existing = $table->constraint($name);
		}

		// MySQL multi column indexes come back as multiple rows.
		if (!empty($existing)) {
			$columns = array_merge($existing['columns'], $columns);
			$length = array_merge($existing['length'], $length);
		}
		if ($isIndex) {
			$table->addIndex($name, [
				'type' => $type,
				'columns' => $columns,
				'length' => $length
			]);
		} else {
			$table->addConstraint($name, [
				'type' => $type,
				'columns' => $columns,
				'length' => $length
			]);
		}
	}

/**
 * {@inheritdoc}
 *
 */
	public function describeForeignKeySql($table, $config) {
		$sql = 'SELECT * FROM information_schema.key_column_usage AS kcu
			INNER JOIN information_schema.referential_constraints AS rc
			ON (kcu.CONSTRAINT_NAME = rc.CONSTRAINT_NAME)
			WHERE kcu.TABLE_SCHEMA = ? AND kcu.TABLE_NAME = ?';

		return [$sql, [$config['database'], $table]];
	}

/**
 * {@inheritdoc}
 *
 */
	public function convertForeignKeyDescription(Table $table, $row) {
		$data = [
			'type' => Table::CONSTRAINT_FOREIGN,
			'columns' => [$row['COLUMN_NAME']],
			'references' => [$row['REFERENCED_TABLE_NAME'], $row['REFERENCED_COLUMN_NAME']],
			'update' => $this->_convertOnClause($row['UPDATE_RULE']),
			'delete' => $this->_convertOnClause($row['DELETE_RULE']),
		];
		$name = $row['CONSTRAINT_NAME'];
		$table->addConstraint($name, $data);
	}

/**
 * {@inheritdoc}
 *
 */
	public function truncateTableSql(Table $table) {
		return [sprintf('TRUNCATE TABLE `%s`', $table->name())];
	}

/**
 * {@inheritdoc}
 *
 */
	public function createTableSql(Table $table, $columns, $constraints, $indexes) {
		$content = implode(",\n", array_merge($columns, $constraints, $indexes));
		$content = sprintf("CREATE TABLE `%s` (\n%s\n)", $table->name(), $content);
		$options = $table->options();
		if (isset($options['engine'])) {
			$content .= sprintf(' ENGINE=%s', $options['engine']);
		}
		if (isset($options['charset'])) {
			$content .= sprintf(' DEFAULT CHARSET=%s', $options['charset']);
		}
		if (isset($options['collate'])) {
			$content .= sprintf(' COLLATE=%s', $options['collate']);
		}
		return [$content];
	}

/**
 * {@inheritdoc}
 *
 */
	public function columnSql(Table $table, $name) {
		$data = $table->column($name);
		$out = $this->_driver->quoteIdentifier($name);
		$typeMap = [
			'integer' => ' INTEGER',
			'biginteger' => ' BIGINT',
			'boolean' => ' BOOLEAN',
			'binary' => ' BLOB',
			'float' => ' FLOAT',
			'decimal' => ' DECIMAL',
			'text' => ' TEXT',
			'date' => ' DATE',
			'time' => ' TIME',
			'datetime' => ' DATETIME',
			'timestamp' => ' TIMESTAMP',
			'uuid' => ' CHAR(36)',
		];
		$specialMap = [
			'string' => true,
		];
		if (isset($typeMap[$data['type']])) {
			$out .= $typeMap[$data['type']];
		}
		if (isset($specialMap[$data['type']])) {
			switch ($data['type']) {
				case 'string':
					$out .= !empty($data['fixed']) ? ' CHAR' : ' VARCHAR';
					if (!isset($data['length'])) {
						$data['length'] = 255;
					}
				break;
			}
		}
		$hasLength = ['integer', 'string'];
		if (in_array($data['type'], $hasLength, true) && isset($data['length'])) {
			$out .= '(' . (int)$data['length'] . ')';
		}

		$hasPrecision = ['float', 'decimal'];
		if (
			in_array($data['type'], $hasPrecision, true) &&
			(isset($data['length']) || isset($data['precision']))
		) {
			$out .= '(' . (int)$data['length'] . ',' . (int)$data['precision'] . ')';
		}

		$hasUnsigned = ['float', 'decimal', 'integer', 'biginteger'];
		if (
			in_array($data['type'], $hasUnsigned, true) &&
			isset($data['unsigned']) && $data['unsigned'] === true
		) {
			$out .= ' UNSIGNED';
		}

		if (isset($data['null']) && $data['null'] === false) {
			$out .= ' NOT NULL';
		}
		if (in_array($data['type'], ['integer', 'biginteger']) && in_array($name, (array)$table->primaryKey())) {
			$out .= ' AUTO_INCREMENT';
		}
		if (isset($data['null']) && $data['null'] === true) {
			$out .= $data['type'] === 'timestamp' ? ' NULL' : ' DEFAULT NULL';
			unset($data['default']);
		}
		if (isset($data['default']) && $data['type'] !== 'timestamp') {
			$out .= ' DEFAULT ' . $this->_driver->schemaValue($data['default']);
		}
		if (
			isset($data['default']) &&
			$data['type'] === 'timestamp' &&
			strtolower($data['default']) === 'current_timestamp'
		) {
			$out .= ' DEFAULT CURRENT_TIMESTAMP';
		}
		if (isset($data['comment'])) {
			$out .= ' COMMENT ' . $this->_driver->schemaValue($data['comment']);
		}
		return $out;
	}

/**
 * {@inheritdoc}
 *
 */
	public function constraintSql(Table $table, $name) {
		$data = $table->constraint($name);
		if ($data['type'] === Table::CONSTRAINT_PRIMARY) {
			$columns = array_map(
				[$this->_driver, 'quoteIdentifier'],
				$data['columns']
			);
			return sprintf('PRIMARY KEY (%s)', implode(', ', $columns));
		}
		if ($data['type'] === Table::CONSTRAINT_UNIQUE) {
			$out = 'UNIQUE KEY ';
		}
		if ($data['type'] === Table::CONSTRAINT_FOREIGN) {
			$out = 'CONSTRAINT ';
		}
		$out .= $this->_driver->quoteIdentifier($name);
		return $this->_keySql($out, $data);
	}

/**
 * {@inheritdoc}
 *
 */
	public function indexSql(Table $table, $name) {
		$data = $table->index($name);
		if ($data['type'] === Table::INDEX_INDEX) {
			$out = 'KEY ';
		}
		if ($data['type'] === Table::INDEX_FULLTEXT) {
			$out = 'FULLTEXT KEY ';
		}
		$out .= $this->_driver->quoteIdentifier($name);
		return $this->_keySql($out, $data);
	}

/**
 * Helper method for generating key SQL snippets.
 *
 * @param string $prefix The key prefix
 * @param array $data Key data.
 * @return string
 */
	protected function _keySql($prefix, $data) {
		$columns = array_map(
			[$this->_driver, 'quoteIdentifier'],
			$data['columns']
		);
		foreach ($data['columns'] as $i => $column) {
			if (isset($data['length'][$column])) {
				$columns[$i] .= sprintf('(%d)', $data['length'][$column]);
			}
		}
		if ($data['type'] === Table::CONSTRAINT_FOREIGN) {
			return $prefix . sprintf(
				' FOREIGN KEY (%s) REFERENCES %s (%s) ON UPDATE %s ON DELETE %s',
				implode(', ', $columns),
				$this->_driver->quoteIdentifier($data['references'][0]),
				$this->_driver->quoteIdentifier($data['references'][1]),
				$this->_foreignOnClause($data['update']),
				$this->_foreignOnClause($data['delete'])
			);
		}
		return $prefix . ' (' . implode(', ', $columns) . ')';
	}

}