<?php
/**
 * PHP Version 5.4
 *
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
namespace Cake\Test\TestCase\ORM;

use Cake\Core\Configure;
use Cake\Database\Connection;
use Cake\ORM\Query;
use Cake\ORM\Table;

/**
 * Tests Query class
 *
 */
class QueryTest extends \Cake\TestSuite\TestCase {

	public function setUp() {
		$this->connection = new Connection(Configure::read('Datasource.test'));
	}

	public function tearDown() {
		$this->connection->execute('DROP TABLE IF EXISTS articles');
		$this->connection->execute('DROP TABLE IF EXISTS authors');
		Table::clearRegistry();
	}


/**
 * Test helper for creating tables.
 *
 * @return void
 */
	protected function _createAuthorsAndArticles() {
		$table = 'CREATE TEMPORARY TABLE authors(id int, name varchar(50))';
		$this->connection->execute($table);

		$table = 'CREATE TEMPORARY TABLE articles(id int, title varchar(20), body varchar(50), author_id int)';
		$this->connection->execute($table);

		Table::config('authors', ['connection' => $this->connection]);
		Table::config('articles', ['connection' => $this->connection]);
	}

/**
 * Auxiliary function to insert a couple rows in a newly created table
 *
 * @return void
 */
	protected function _insertTwoRecords() {
		$this->_createAuthorsAndArticles();

		$data = ['id' => '1', 'name' => 'Chuck Norris'];
		$result = $this->connection->insert('authors', $data, ['id' => 'integer', 'name' => 'string']);

		$result->bindValue(1, '2', 'integer');
		$result->bindValue(2, 'Bruce Lee');
		$result->execute();

		$data = ['id' => '1', 'title' => 'a title', 'body' => 'a body', 'author_id' => 1];
		$result = $this->connection->insert(
			'articles',
			$data,
			['id' => 'integer', 'title' => 'string', 'body' => 'string', 'author_id' => 'integer']
		);

		$result->bindValue(1, '2', 'integer');
		$result->bindValue(2, 'another title');
		$result->bindValue(3, 'another body');
		$result->bindValue(4, 2);
		$result->execute();

		return $result;
	}

/**
 * Tests that fully defined belongsTo and hasOne relationships are joined correctly
 *
 * @return void
 **/
	public function testContainToJoinsOneLevel() {
		$contains = ['client' => [
			'associationType' => 'belongsTo',
			'table' => 'clients',
			'fields' => false,
			'order' => [
				'associationType' => 'hasOne',
				'orderType' => ['associationType' => 'belongsTo', 'fields' => false],
				'fields' => false,
				'stuff' => [
					'associationType' => 'hasOne',
					'table' => 'things',
					'fields' => false,
					'stuffType' => ['associationType' => 'belongsTo', 'fields' => false]
				]
			],
			'company' => [
				'associationType' => 'belongsTo',
				'fields' => false,
				'table' => 'organizations',
				'foreignKey' => 'organization_id',
				'category' => ['associationType' => 'belongsTo', 'fields' => false]
			]
		]];

		$table = Table::build('foo', ['schema' => ['id' => ['type' => 'integer']]]);
		$query = $this->getMock('\Cake\ORM\Query', ['join'], [$this->connection]);

		$query->expects($this->at(0))->method('join')
			->with(['client' => [
				'table' => 'clients',
				'type' => 'left',
				'conditions' => ['client.id = foo.client_id']
			]])
			->will($this->returnValue($query));

		$query->expects($this->at(1))->method('join')
			->with(['order' => [
				'table' => 'orders',
				'type' => 'left',
				'conditions' => ['order.client_id = client.id']
			]])
			->will($this->returnValue($query));

		$query->expects($this->at(2))->method('join')
			->with(['orderType' => [
				'table' => 'order_types',
				'type' => 'left',
				'conditions' => ['orderType.id = order.order_type_id']
			]])
			->will($this->returnValue($query));

		$query->expects($this->at(3))->method('join')
			->with(['stuff' => [
				'table' => 'things',
				'type' => 'left',
				'conditions' => ['stuff.order_id = order.id']
			]])
			->will($this->returnValue($query));


		$query->expects($this->at(4))->method('join')
			->with(['stuffType' => [
				'table' => 'stuff_types',
				'type' => 'left',
				'conditions' => ['stuffType.id = stuff.stuff_type_id']
			]])
			->will($this->returnValue($query));

		$query->expects($this->at(5))->method('join')
			->with(['company' => [
				'table' => 'organizations',
				'type' => 'left',
				'conditions' => ['company.id = client.company_id']
			]])
			->will($this->returnValue($query));

		$query->expects($this->at(6))->method('join')
			->with(['category' => [
				'table' => 'categories',
				'type' => 'left',
				'conditions' => ['category.id = company.category_id']
			]])
			->will($this->returnValue($query));

		$s = $query
			->select('foo.id')
			->repository($table)
			->contain($contains)->sql();
	}

/**
 * Test that fields for contained models are aliased and added to the select clause
 *
 * @return void
 **/
	public function testContainToFieldsPredefined() {
		$contains = ['client' => [
			'associationType' => 'belongsTo',
			'fields' => ['name', 'company_id', 'client.telephone'],
			'table' => 'clients',
			'order' => [
				'associationType' => 'hasOne',
				'fields' => ['total', 'placed']
			]
		]];

		$table = Table::build('foo', ['schema' => ['id' => ['type' => 'integer']]]);
		$query = new Query($this->connection);

		$query->select('foo.id')->repository($table)->contain($contains)->sql();
		$select = $query->clause('select');
		$expected = [
			'foo__id' => 'foo.id', 'client__name' => 'client.name',
			'client__company_id' => 'client.company_id',
			'client__telephone' => 'client.telephone',
			'order__total' => 'order.total', 'order__placed' => 'order.placed'
		];
		$this->assertEquals($expected, $select);
	}


/**
 * Tests that default fields for associations are added to the select clause when
 * none is specified
 *
 * @return void
 **/
	public function testContainToFieldsDefault() {
		$contains = [
			'client' => [
				'associationType' => 'belongsTo',
				'order' => [
					'associationType' => 'hasOne',
				]
			]
		];

		$schema1 = [
			'id' => ['type' => 'integer'],
			'name' => ['type' => 'string'],
			'phone' => ['type' => 'string']
		];
		$schema2 = [
			'id' => ['type' => 'integer'],
			'total' => ['type' => 'string'],
			'placed' => ['type' => 'datetime']
		];
		$table = Table::build('foo', ['schema' => ['id' => ['type' => 'integer']]]);
		Table::build('client', ['schema' => $schema1]);
		Table::build('order', ['schema' => $schema2]);

		$query = new Query($this->connection);
		$query->select()->repository($table)->contain($contains)->sql();
		$select = $query->clause('select');
		$expected = [
			'foo__id' => 'foo.id', 'client__name' => 'client.name',
			'client__id' => 'client.id', 'client__phone' => 'client.phone',
			'order__id' => 'order.id', 'order__total' => 'order.total',
			'order__placed' => 'order.placed'
		];
		$this->assertEquals($expected, $select);

		$contains['client']['fields'] = ['name'];
		$query = new Query($this->connection);
		$query->select('foo.id')->repository($table)->contain($contains)->sql();
		$select = $query->clause('select');
		$expected = ['foo__id' => 'foo.id', 'client__name' => 'client.name'];
		$this->assertEquals($expected, $select);

		$contains['client']['fields'] = [];
		$contains['client']['order']['fields'] = false;
		$query = new Query($this->connection);
		$query->select()->repository($table)->contain($contains)->sql();
		$select = $query->clause('select');
		$expected = [
			'foo__id' => 'foo.id',
			'client__id' => 'client.id',
			'client__name' => 'client.name',
			'client__phone' => 'client.phone',
		];
		$this->assertEquals($expected, $select);
	}

/**
 * Tests that results are grouped correctly when using contain()
 *
 * @return void
 **/
	public function testContainResultFetchingOneLevel() {
		$this->_insertTwoRecords();
		Table::map('author', 'authors');

		$query = new Query($this->connection);
		$contain = ['author' => ['associationType' => 'belongsTo']];

		$table = Table::build('article', ['table' => 'articles']);
		$results = $query->repository($table)->select()->contain($contain)->toArray();
		$expected = [
			[
				'article' => [
					'id' => (int) 1,
					'title' => 'a title',
					'body' => 'a body',
					'author_id' => (int) 1
				],
				'author' => [
					'id' => (int) 1,
					'name' => 'Chuck Norris'
				]
			],
			[
				'article' => [
					'id' => (int) 2,
					'title' => 'another title',
					'body' => 'another body',
					'author_id' => (int) 2
				],
				'author' => [
					'id' => (int) 2,
					'name' => 'Bruce Lee'
				]
			]
		];
		$this->assertSame($expected, $results);
	}

}