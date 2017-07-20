<?php
/**
 * PaginatorComponentTest file
 *
 * Series of tests for paginator component.
 *
 * CakePHP(tm) Tests <http://book.cakephp.org/2.0/en/development/testing.html>
 * Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the LICENSE.txt
 * Redistributions of files must retain the above copyright notice
 *
 * @copyright     Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 * @link          http://book.cakephp.org/2.0/en/development/testing.html CakePHP(tm) Tests
 * @since         CakePHP(tm) v 2.0
 * @license       http://www.opensource.org/licenses/mit-license.php MIT License
 */
namespace Cake\Test\TestCase\Controller\Component;

use Cake\Controller\Component\PaginatorComponent;
use Cake\Controller\Controller;
use Cake\Core\Configure;
use Cake\Database\ConnectionManager;
use Cake\Error;
use Cake\Network\Request;
use Cake\ORM\TableRegistry;
use Cake\TestSuite\TestCase;
use Cake\Utility\Hash;

/**
 * PaginatorTestController class
 *
 */
class PaginatorTestController extends Controller {

/**
 * components property
 *
 * @var array
 */
	public $components = array('Paginator');
}

class PaginatorComponentTest extends TestCase {

/**
 * fixtures property
 *
 * @var array
 */
	public $fixtures = array('core.post', 'core.comment', 'core.author');

/**
 * setup
 *
 * @return void
 */
	public function setUp() {
		parent::setUp();
		return $this->markTestIncomplete('Need to revisit once models work again.');

		Configure::write('App.namespace', 'TestApp');

		$this->request = new Request('controller_posts/index');
		$this->request->params['pass'] = array();
		$this->Controller = new Controller($this->request);
		$this->Paginator = new PaginatorComponent($this->getMock('Cake\Controller\ComponentRegistry'), array());
		$this->Paginator->Controller = $this->Controller;
		$this->Post = $this->getMock('Cake\ORM\Table', [], [], '', false);
		$this->Controller->Post = $this->Post;
		$this->Controller->Post->alias = 'Post';
	}

/**
 * tearDown
 *
 * @return void
 */
	public function tearDown() {
		parent::tearDown();
		TableRegistry::clear();
	}

/**
 * Test that non-numeric values are rejected for page, and limit
 *
 * @return void
 */
	public function testPageParamCasting() {
		$this->Post->expects($this->any())
			->method('alias')
			->will($this->returnValue('Posts'));

		$query = $this->_getMockFindQuery();
		$this->Post->expects($this->at(1))
			->method('find')
			->will($this->returnValue($query));

		$this->Post->expects($this->at(2))
			->method('find')
			->will($this->returnValue($query));

		$this->request->query = array('page' => '1 " onclick="alert(\'xss\');">');
		$this->Paginator->settings = array('limit' => 1, 'maxLimit' => 10);
		$this->Paginator->paginate($this->Post);
		$this->assertSame(1, $this->request->params['paging']['Posts']['page'], 'XSS exploit opened');
	}

/**
 * test that unknown keys in the default settings are
 * passed to the find operations.
 *
 * @return void
 */
	public function testPaginateExtraParams() {
		$this->Controller->request->query = array('page' => '-1');
		$this->Paginator->settings = array(
			'PaginatorPosts' => array(
				'contain' => array('PaginatorAuthor'),
				'maxLimit' => 10,
				'group' => 'PaginatorPosts.published',
				'order' => array('PaginatorPosts.id' => 'ASC')
			),
		);
		$table = $this->_getMockPosts(['find']);
		$query = $this->_getMockFindQuery();
		$table->expects($this->at(0))
			->method('find')
			->with('all', [
				'conditions' => [],
				'contain' => ['PaginatorAuthor'],
				'fields' => null,
				'group' => 'PaginatorPosts.published',
				'limit' => 10,
				'order' => ['PaginatorPosts.id' => 'ASC'],
				'page' => 1,
			])
			->will($this->returnValue($query));

		$table->expects($this->at(1))
			->method('find')
			->with('all', [
				'conditions' => [],
				'contain' => ['PaginatorAuthor'],
				'group' => 'PaginatorPosts.published',
			])
			->will($this->returnValue($query));

		$this->Paginator->paginate($table);
	}

/**
 * Test that special paginate types are called and that the type param doesn't leak out into defaults or options.
 *
 * @return void
 */
	public function testPaginateCustomFinder() {
		$Controller = new PaginatorTestController($this->request);
		$Controller->request->params['pass'][] = '1';
		$Controller->request->query = [];
		$Controller->constructClasses();

		$Controller->Paginator->settings = array(
			'PaginatorPosts' => array(
				'popular',
				'fields' => array('id', 'title'),
				'maxLimit' => 10,
			)
		);

		$table = $this->_getMockPosts(['findPopular']);
		$query = $this->_getMockFindQuery();

		$table->expects($this->any())
			->method('findPopular')
			->will($this->returnValue($query));

		$Controller->Paginator->paginate($table);
		$this->assertEquals('popular', $Controller->request->params['paging']['PaginatorPosts']['findType']);
	}

/**
 * test that flat default pagination parameters work.
 *
 * @return void
 */
	public function testDefaultPaginateParams() {
		$Controller = new PaginatorTestController($this->request);
		$Controller->request->query = [];
		$Controller->constructClasses();
		$Controller->Paginator->settings = array(
			'order' => 'PaginatorPosts.id DESC',
			'maxLimit' => 10,
		);

		$table = $this->_getMockPosts(['find']);
		$query = $this->_getMockFindQuery();

		$table->expects($this->at(0))
			->method('find')
			->with('all', [
				'conditions' => [],
				'fields' => null,
				'limit' => 10,
				'page' => 1,
				'order' => 'PaginatorPosts.id DESC'
			])
			->will($this->returnValue($query));

		$table->expects($this->at(1))
			->method('find')
			->will($this->returnValue($query));

		$Controller->Paginator->paginate($table);
	}

/**
 * test paginate() and virtualField interactions
 *
 * @return void
 */
	public function testPaginateOrderVirtualField() {
		$this->markTestIncomplete('Need to revisit once models work again.');
		$Controller = new PaginatorTestController($this->request);
		$Controller->uses = array('PaginatorControllerPost', 'PaginatorControllerComment');
		$Controller->request->query = [];
		$Controller->constructClasses();
		$Controller->PaginatorControllerPost->virtualFields = array(
			'offset_test' => 'PaginatorControllerPost.id + 1'
		);

		$Controller->Paginator->settings = array(
			'fields' => array('id', 'title', 'offset_test'),
			'order' => array('offset_test' => 'DESC'),
			'maxLimit' => 10,
		);
		$result = $Controller->Paginator->paginate('PaginatorControllerPost');
		$this->assertEquals(array(4, 3, 2), Hash::extract($result, '{n}.PaginatorControllerPost.offset_test'));

		$Controller->request->query = array('sort' => 'offset_test', 'direction' => 'asc');
		$result = $Controller->Paginator->paginate('PaginatorControllerPost');
		$this->assertEquals(array(2, 3, 4), Hash::extract($result, '{n}.PaginatorControllerPost.offset_test'));
	}

/**
 * test paginate() and virtualField on joined model
 *
 * @return void
 */
	public function testPaginateOrderVirtualFieldJoinedModel() {
		$this->markTestIncomplete('Need to revisit once models work again.');
		$Controller = new PaginatorTestController($this->request);
		$Controller->uses = array('PaginatorControllerPost');
		$Controller->request->query = [];
		$Controller->constructClasses();
		$Controller->PaginatorControllerPost->recursive = 0;
		$Controller->Paginator->settings = array(
			'order' => array('PaginatorAuthor.joined_offset' => 'DESC'),
			'maxLimit' => 10,
		);
		$result = $Controller->Paginator->paginate('PaginatorControllerPost');
		$this->assertEquals(array(4, 2, 2), Hash::extract($result, '{n}.PaginatorAuthor.joined_offset'));

		$Controller->request->query = array('sort' => 'PaginatorAuthor.joined_offset', 'direction' => 'asc');
		$result = $Controller->Paginator->paginate('PaginatorControllerPost');
		$this->assertEquals(array(2, 2, 4), Hash::extract($result, '{n}.PaginatorAuthor.joined_offset'));
	}

/**
 * test that option merging prefers specific models
 *
 * @return void
 */
	public function testMergeOptionsModelSpecific() {
		$this->Paginator->settings = array(
			'page' => 1,
			'limit' => 20,
			'maxLimit' => 100,
			'Post' => array(
				'page' => 1,
				'limit' => 10,
				'maxLimit' => 50,
			)
		);
		$result = $this->Paginator->mergeOptions('Silly');
		$this->assertEquals($this->Paginator->settings, $result);

		$result = $this->Paginator->mergeOptions('Post');
		$expected = array('page' => 1, 'limit' => 10, 'maxLimit' => 50);
		$this->assertEquals($expected, $result);
	}

/**
 * test mergeOptions with customFind key
 *
 * @return void
 */
	public function testMergeOptionsCustomFindKey() {
		$this->request->query = [
			'page' => 10,
			'limit' => 10
		];
		$this->Paginator->settings = [
			'page' => 1,
			'limit' => 20,
			'maxLimit' => 100,
			'findType' => 'myCustomFind'
		];
		$result = $this->Paginator->mergeOptions('Post');
		$expected = array(
			'page' => 10,
			'limit' => 10,
			'maxLimit' => 100,
			'findType' => 'myCustomFind'
		);
		$this->assertEquals($expected, $result);
	}

/**
 * test merging options from the querystring.
 *
 * @return void
 */
	public function testMergeOptionsQueryString() {
		$this->request->query = array(
			'page' => 99,
			'limit' => 75
		);
		$this->Paginator->settings = array(
			'page' => 1,
			'limit' => 20,
			'maxLimit' => 100,
		);
		$result = $this->Paginator->mergeOptions('Post');
		$expected = array('page' => 99, 'limit' => 75, 'maxLimit' => 100);
		$this->assertEquals($expected, $result);
	}

/**
 * test that the default whitelist doesn't let people screw with things they should not be allowed to.
 *
 * @return void
 */
	public function testMergeOptionsDefaultWhiteList() {
		$this->request->query = array(
			'page' => 10,
			'limit' => 10,
			'fields' => array('bad.stuff'),
			'recursive' => 1000,
			'conditions' => array('bad.stuff'),
			'contain' => array('bad')
		);
		$this->Paginator->settings = array(
			'page' => 1,
			'limit' => 20,
			'maxLimit' => 100,
		);
		$result = $this->Paginator->mergeOptions('Post');
		$expected = array('page' => 10, 'limit' => 10, 'maxLimit' => 100);
		$this->assertEquals($expected, $result);
	}

/**
 * test that modifying the whitelist works.
 *
 * @return void
 */
	public function testMergeOptionsExtraWhitelist() {
		$this->request->query = array(
			'page' => 10,
			'limit' => 10,
			'fields' => array('bad.stuff'),
			'recursive' => 1000,
			'conditions' => array('bad.stuff'),
			'contain' => array('bad')
		);
		$this->Paginator->settings = array(
			'page' => 1,
			'limit' => 20,
			'maxLimit' => 100,
		);
		$this->Paginator->whitelist[] = 'fields';
		$result = $this->Paginator->mergeOptions('Post');
		$expected = array(
			'page' => 10, 'limit' => 10, 'maxLimit' => 100, 'fields' => array('bad.stuff')
		);
		$this->assertEquals($expected, $result);
	}

/**
 * test mergeOptions with limit > maxLimit in code.
 *
 * @return void
 */
	public function testMergeOptionsMaxLimit() {
		$this->Paginator->settings = array(
			'limit' => 200,
			'paramType' => 'named',
		);
		$result = $this->Paginator->mergeOptions('Post');
		$expected = array('page' => 1, 'limit' => 200, 'maxLimit' => 200, 'paramType' => 'named');
		$this->assertEquals($expected, $result);

		$this->Paginator->settings = array(
			'maxLimit' => 10,
			'paramType' => 'named',
		);
		$result = $this->Paginator->mergeOptions('Post');
		$expected = array('page' => 1, 'limit' => 20, 'maxLimit' => 10, 'paramType' => 'named');
		$this->assertEquals($expected, $result);

		$this->request->params['named'] = array(
			'limit' => 500
		);
		$this->Paginator->settings = array(
			'limit' => 150,
			'paramType' => 'named',
		);
		$result = $this->Paginator->mergeOptions('Post');
		$expected = array('page' => 1, 'limit' => 150, 'maxLimit' => 150, 'paramType' => 'named');
		$this->assertEquals($expected, $result);
	}

/**
 * Integration test to ensure that validateSort is being used by paginate()
 *
 * @return void
 */
	public function testValidateSortInvalid() {
		$table = $this->_getMockPosts(['find']);
		$query = $this->_getMockFindQuery();

		$table->expects($this->at(0))
			->method('find')
			->with('all', [
				'fields' => null,
				'limit' => 20,
				'conditions' => [],
				'page' => 1,
				'order' => ['PaginatorPosts.id' => 'asc'],
			])
			->will($this->returnValue($query));

		$table->expects($this->at(1))
			->method('find')
			->will($this->returnValue($query));

		$this->Controller->request->query = [
			'page' => 1,
			'sort' => 'id',
			'direction' => 'herp'
		];
		$this->Paginator->paginate($table);
		$this->assertEquals('PaginatorPosts.id', $this->Controller->request->params['paging']['PaginatorPosts']['sort']);
		$this->assertEquals('asc', $this->Controller->request->params['paging']['PaginatorPosts']['direction']);
	}

/**
 * test that invalid directions are ignored.
 *
 * @return void
 */
	public function testValidateSortInvalidDirection() {
		$model = $this->getMock('Cake\ORM\Table');
		$model->expects($this->any())
			->method('alias')
			->will($this->returnValue('model'));
		$model->expects($this->any())
			->method('hasField')
			->will($this->returnValue(true));

		$options = array('sort' => 'something', 'direction' => 'boogers');
		$result = $this->Paginator->validateSort($model, $options);

		$this->assertEquals('asc', $result['order']['model.something']);
	}

/**
 * Test that a really large page number gets clamped to the max page size.
 *
 * @expectedException Cake\Error\NotFoundException
 * @return void
 */
	public function testOutOfRangePageNumberGetsClamped() {
		$Controller = new PaginatorTestController($this->request);
		$Controller->request->query['page'] = 3000;
		$Controller->constructClasses();

		$table = TableRegistry::get('PaginatorPosts');
		$Controller->Paginator->paginate($table);
	}

/**
 * Test that a really REALLY large page number gets clamped to the max page size.
 *
 * @expectedException Cake\Error\NotFoundException
 * @return void
 */
	public function testOutOfVeryBigPageNumberGetsClamped() {
		$Controller = new PaginatorTestController($this->request);
		$Controller->request->query = [
			'page' => '3000000000000000000000000',
		];
		$Controller->constructClasses();

		$table = TableRegistry::get('PaginatorPosts');
		$Controller->Paginator->paginate($table);
	}

/**
 * test that fields not in whitelist won't be part of order conditions.
 *
 * @return void
 */
	public function testValidateSortWhitelistFailure() {
		$model = $this->getMock('Cake\ORM\Table');
		$model->expects($this->any())
			->method('alias')
			->will($this->returnValue('model'));
		$model->expects($this->any())->method('hasField')->will($this->returnValue(true));

		$options = array('sort' => 'body', 'direction' => 'asc');
		$result = $this->Paginator->validateSort($model, $options, array('title', 'id'));

		$this->assertNull($result['order']);
	}

/**
 * test that fields in the whitelist are not validated
 *
 * @return void
 */
	public function testValidateSortWhitelistTrusted() {
		$model = $this->getMock('Cake\ORM\Table');
		$model->expects($this->any())
			->method('alias')
			->will($this->returnValue('model'));
		$model->expects($this->never())->method('hasField');

		$options = array('sort' => 'body', 'direction' => 'asc');
		$result = $this->Paginator->validateSort($model, $options, array('body'));

		$expected = array('body' => 'asc');
		$this->assertEquals($expected, $result['order']);
	}

/**
 * test that virtual fields work.
 *
 * @return void
 */
	public function testValidateSortVirtualField() {
		$model = $this->getMock('Cake\ORM\Table');
		$model->expects($this->any())
			->method('alias')
			->will($this->returnValue('model'));

		$model->expects($this->at(1))
			->method('hasField')
			->with('something')
			->will($this->returnValue(false));

		$model->expects($this->at(2))
			->method('hasField')
			->with('something', true)
			->will($this->returnValue(true));

		$options = array('sort' => 'something', 'direction' => 'desc');
		$result = $this->Paginator->validateSort($model, $options);

		$this->assertEquals('desc', $result['order']['something']);
	}

/**
 * test that sorting fields is alias specific
 *
 * @return void
 */
	public function testValidateSortSharedFields() {
		$model = $this->getMock('Cake\ORM\Table');
		$model->expects($this->any())
			->method('alias')
			->will($this->returnValue('model'));
		$model->Child = $this->getMock('Cake\ORM\Table');
		$model->Child->expects($this->any())
			->method('alias')
			->will($this->returnValue('Child'));

		$model->expects($this->never())
			->method('hasField');

		$model->Child->expects($this->at(0))
			->method('hasField')
			->with('something')
			->will($this->returnValue(true));

		$options = array('sort' => 'Child.something', 'direction' => 'desc');
		$result = $this->Paginator->validateSort($model, $options);

		$this->assertEquals('desc', $result['order']['Child.something']);
	}
/**
 * test that multiple sort works.
 *
 * @return void
 */
	public function testValidateSortMultiple() {
		$model = $this->getMock('Cake\ORM\Table');
		$model->expects($this->any())
			->method('alias')
			->will($this->returnValue('model'));
		$model->expects($this->any())->method('hasField')->will($this->returnValue(true));

		$options = array(
			'order' => array(
				'author_id' => 'asc',
				'title' => 'asc'
			)
		);
		$result = $this->Paginator->validateSort($model, $options);
		$expected = array(
			'model.author_id' => 'asc',
			'model.title' => 'asc'
		);

		$this->assertEquals($expected, $result['order']);
	}

/**
 * Test that no sort doesn't trigger an error.
 *
 * @return void
 */
	public function testValidateSortNoSort() {
		$model = $this->getMock('Cake\ORM\Table');
		$model->expects($this->any())
			->method('alias')
			->will($this->returnValue('model'));
		$model->expects($this->any())->method('hasField')->will($this->returnValue(true));

		$options = array('direction' => 'asc');
		$result = $this->Paginator->validateSort($model, $options, array('title', 'id'));
		$this->assertFalse(isset($result['order']));

		$options = array('order' => 'invalid desc');
		$result = $this->Paginator->validateSort($model, $options, array('title', 'id'));

		$this->assertEquals($options['order'], $result['order']);
	}

/**
 * Test sorting with incorrect aliases on valid fields.
 *
 * @return void
 */
	public function testValidateSortInvalidAlias() {
		$model = $this->getMock('Cake\ORM\Table');
		$model->expects($this->any())
			->method('alias')
			->will($this->returnValue('model'));
		$model->expects($this->any())->method('hasField')->will($this->returnValue(true));

		$options = array('sort' => 'Derp.id');
		$result = $this->Paginator->validateSort($model, $options);
		$this->assertEquals(array(), $result['order']);
	}

/**
 * test that maxLimit is respected
 *
 * @return void
 */
	public function testCheckLimit() {
		$result = $this->Paginator->checkLimit(array('limit' => 1000000, 'maxLimit' => 100));
		$this->assertEquals(100, $result['limit']);

		$result = $this->Paginator->checkLimit(array('limit' => 'sheep!', 'maxLimit' => 100));
		$this->assertEquals(1, $result['limit']);

		$result = $this->Paginator->checkLimit(array('limit' => '-1', 'maxLimit' => 100));
		$this->assertEquals(1, $result['limit']);

		$result = $this->Paginator->checkLimit(array('limit' => null, 'maxLimit' => 100));
		$this->assertEquals(1, $result['limit']);

		$result = $this->Paginator->checkLimit(array('limit' => 0, 'maxLimit' => 100));
		$this->assertEquals(1, $result['limit']);
	}

/**
 * Integration test for checkLimit() being applied inside paginate()
 *
 * @return void
 */
	public function testPaginateMaxLimit() {
		$table = TableRegistry::get('PaginatorPosts');

		$this->Controller->paginate = [
			'maxLimit' => 100,
		];
		$this->Controller->request->query = [
			'limit' => '1000'
		];
		$this->Paginator->paginate($table);
		$this->assertEquals(100, $this->Controller->request->params['paging']['PaginatorPosts']['limit']);

		$this->Controller->request->query = [
			'limit' => '10'
		];
		$this->Paginator->paginate($table);
		$this->assertEquals(10, $this->Controller->request->params['paging']['PaginatorPosts']['limit']);
	}

/**
 * test paginate() and virtualField overlapping with real fields.
 *
 * @return void
 */
	public function testPaginateOrderVirtualFieldSharedWithRealField() {
		$this->markTestIncomplete('Need to revisit once models work again.');
		$Controller = new Controller($this->request);
		$Controller->uses = array('PaginatorControllerPost', 'PaginatorControllerComment');
		$Controller->constructClasses();
		$Controller->PaginatorControllerComment->virtualFields = array(
			'title' => 'PaginatorControllerComment.comment'
		);
		$Controller->PaginatorControllerComment->bindModel(array(
			'belongsTo' => array(
				'PaginatorControllerPost' => array(
					'className' => 'PaginatorControllerPost',
					'foreignKey' => 'article_id'
				)
			)
		), false);

		$Controller->paginate = array(
			'fields' => array(
				'PaginatorControllerComment.id',
				'title',
				'PaginatorControllerPost.title'
			),
		);
		$Controller->request->params['named'] = array(
			'sort' => 'PaginatorControllerPost.title',
			'direction' => 'desc'
		);
		$result = Hash::extract(
			$Controller->paginate('PaginatorControllerComment'),
			'{n}.PaginatorControllerComment.id'
		);
		$result1 = array_splice($result, 0, 2);
		sort($result1);
		$this->assertEquals(array(5, 6), $result1);

		sort($result);
		$this->assertEquals(array(1, 2, 3, 4), $result);
	}

/**
 * test paginate() and custom find, to make sure the correct count is returned.
 *
 * @return void
 */
	public function testPaginateCustomFind() {
		$idExtractor = function ($result) {
			$ids = [];
			foreach ($result as $record) {
				$ids[] = $record->id;
			}
			return $ids;
		};

		$table = TableRegistry::get('PaginatorPosts');
		$data = array('author_id' => 3, 'title' => 'Fourth Article', 'body' => 'Article Body, unpublished', 'published' => 'N');
		$result = $table->save(new \Cake\ORM\Entity($data));
		$this->assertNotEmpty($result);

		$result = $this->Paginator->paginate($table);
		$this->assertCount(4, $result, '4 rows should come back');
		$this->assertEquals(array(1, 2, 3, 4), $idExtractor($result));

		$result = $this->Controller->request->params['paging']['PaginatorPosts'];
		$this->assertEquals(4, $result['current']);
		$this->assertEquals(4, $result['count']);

		$this->Paginator->settings = array('findType' => 'published');
		$result = $this->Paginator->paginate($table);
		$this->assertCount(3, $result, '3 rows should come back');
		$this->assertEquals(array(1, 2, 3), $idExtractor($result));

		$result = $this->Controller->request->params['paging']['PaginatorPosts'];
		$this->assertEquals(3, $result['current']);
		$this->assertEquals(3, $result['count']);

		$this->Paginator->settings = array('findType' => 'published', 'limit' => 2);
		$result = $this->Paginator->paginate($table);
		$this->assertCount(2, $result, '2 rows should come back');
		$this->assertEquals(array(1, 2), $idExtractor($result));

		$result = $this->Controller->request->params['paging']['PaginatorPosts'];
		$this->assertEquals(2, $result['current']);
		$this->assertEquals(3, $result['count']);
		$this->assertEquals(2, $result['pageCount']);
		$this->assertTrue($result['nextPage']);
		$this->assertFalse($result['prevPage']);
	}

/**
 * test paginate() and custom find with fields array, to make sure the correct count is returned.
 *
 * @return void
 */
	public function testPaginateCustomFindFieldsArray() {
		$this->Controller->constructClasses();

		$table = TableRegistry::get('PaginatorPosts');
		$data = array('author_id' => 3, 'title' => 'Fourth Article', 'body' => 'Article Body, unpublished', 'published' => 'N');
		$table->save(new \Cake\ORM\Entity($data));

		$this->Paginator->settings = [
			'findType' => 'list',
			'conditions' => array('PaginatorPosts.published' => 'Y'),
			'limit' => 2
		];
		$results = $this->Paginator->paginate($table);

		$result = $results->toArray();
		$expected = array(
			1 => 'First Post',
			2 => 'Second Post',
		);
		$this->assertEquals($expected, $result);

		$result = $this->Controller->request->params['paging']['PaginatorPosts'];
		$this->assertEquals(2, $result['current']);
		$this->assertEquals(3, $result['count']);
		$this->assertEquals(2, $result['pageCount']);
		$this->assertTrue($result['nextPage']);
		$this->assertFalse($result['prevPage']);
	}

/**
 * test paginate() and custom finders to ensure the count + find
 * use the custom type.
 *
 * @return void
 */
	public function testPaginateCustomFindCount() {
		$this->Paginator->settings = array(
			'findType' => 'published',
			'limit' => 2
		);
		$table = $this->_getMockPosts(['find']);
		$query = $this->_getMockFindQuery();
		$table->expects($this->at(0))
			->method('find')
			->with('published', [
				'conditions' => [],
				'order' => [],
				'limit' => 2,
				'fields' => null,
				'page' => 1,
			])
			->will($this->returnValue($query));

		$table->expects($this->at(1))
			->method('find')
			->with('published', [
				'conditions' => [],
			])
			->will($this->returnValue($query));

		$this->Paginator->paginate($table);
	}

/**
 * Helper method for making mocks.
 *
 * @return Table
 */
	protected function _getMockPosts($methods = []) {
		return $this->getMock(
			'TestApp\Model\Repository\PaginatorPostsTable',
			$methods,
			[['connection' => ConnectionManager::get('test'), 'alias' => 'PaginatorPosts']]
		);
	}

/**
 * Helper method for mocking queries.
 *
 * @return Query
 */
	protected function _getMockFindQuery() {
		$query = $this->getMock('Cake\ORM\Query', ['total', 'execute'], [], '', false);

		$results = $this->getMock('Cake\ORM\ResultSet', [], [], '', false);
		$results->expects($this->any())
			->method('count')
			->will($this->returnValue(2));

		$query->expects($this->any())
			->method('execute')
			->will($this->returnValue($results));

		$query->expects($this->any())
			->method('total')
			->will($this->returnValue(2));
		return $query;
	}

}