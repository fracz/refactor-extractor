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
namespace Cake\Test\TestCase\Model\Behavior;

use Cake\Collection\Collection;
use Cake\Event\Event;
use Cake\Model\Behavior\TranslateBehavior;
use Cake\ORM\Entity;
use Cake\ORM\TableRegistry;
use Cake\TestSuite\TestCase;

/**
 * Translate behavior test case
 */
class TranslateBehaviorTest extends TestCase {

/**
 * fixtures
 *
 * @var array
 */
	public $fixtures = [
		'core.translate',
		'core.article',
		'core.comment',
		'core.author'
	];

	public function tearDown() {
		parent::tearDown();
		TableRegistry::clear();
	}

/**
 * Returns an array with all the translations found for a set of records
 *
 * @return Collection
 */
	protected function _extractTranslations($data) {
		return (new Collection($data))->map(function($row) {
			$translations = $row->get('_translations');
			if (!$translations) {
				return [];
			}
			return array_map(function($t) {
				return $t->toArray();
			}, $translations);
		});
	}

/**
 * Tests that fields from a translated model are overriden
 *
 * @return void
 */
	public function testFindSingleLocale() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$table->locale('eng');
		$results = $table->find()->combine('title', 'body', 'id')->toArray();
		$expected = [
			1 => ['Title #1' => 'Content #1'],
			2 => ['Title #2' => 'Content #2'],
			3 => ['Title #3' => 'Content #3'],
		];
		$this->assertSame($expected, $results);
	}

/**
 * Tests that overriding fields with the translate behavior works when
 * using conditions and that all other columns are preserved
 *
 * @return void
 */
	public function testFindSingleLocaleWithConditions() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$table->locale('eng');
		$results = $table->find()
			->where(['Articles.id' => 2])
			->all();

		$this->assertCount(1, $results);
		$row = $results->first();

		$expected = [
			'id' => 2,
			'title' => 'Title #2',
			'body' => 'Content #2',
			'author_id' => 3,
			'published' => 'Y',
			'_locale' => 'eng'
		];
		$this->assertEquals($expected, $row->toArray());
	}

/**
 * Tests that translating fields work when other formatters are used
 *
 * @return void
 */
	public function testFindList() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$table->locale('eng');

		$results = $table->find('list')->toArray();
		$expected = [1 => 'Title #1', 2 => 'Title #2', 3 => 'Title #3'];
		$this->assertSame($expected, $results);
	}

/**
 * Tests that the query count return the correct results
 *
 * @return void
 */
	public function testFindCount() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$table->locale('eng');

		$this->assertEquals(3, $table->find()->count());
	}

/**
 * Tests that it is possible to get all translated fields at once
 *
 * @return void
 */
	public function testFindTranslations() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$results = $table->find('translations');
		$expected = [
			[
				'eng' => ['title' => 'Title #1', 'body' => 'Content #1', 'locale' => 'eng'],
				'deu' => ['title' => 'Titel #1', 'body' => 'Inhalt #1', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #1', 'body' => 'Obsah #1', 'locale' => 'cze']
			],
			[
				'eng' => ['title' => 'Title #2', 'body' => 'Content #2', 'locale' => 'eng'],
				'deu' => ['title' => 'Titel #2', 'body' => 'Inhalt #2', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #2', 'body' => 'Obsah #2', 'locale' => 'cze']
			],
			[
				'eng' => ['title' => 'Title #3', 'body' => 'Content #3', 'locale' => 'eng'],
				'deu' => ['title' => 'Titel #3', 'body' => 'Inhalt #3', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #3', 'body' => 'Obsah #3', 'locale' => 'cze']
			]
		];

		$translations = $this->_extractTranslations($results);
		$this->assertEquals($expected, $translations->toArray());
		$expected = [
			1 => ['First Article' => 'First Article Body'],
			2 => ['Second Article' => 'Second Article Body'],
			3 => ['Third Article' => 'Third Article Body']
		];

		$grouped = $results->combine('title', 'body', 'id');
		$this->assertEquals($expected, $grouped->toArray());
	}

/**
 * Tests that it is possible to request just a few translations
 *
 * @return void
 */
	public function testFindFilteredTranslations() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$results = $table->find('translations', ['locales' => ['deu', 'cze']]);
		$expected = [
			[
				'deu' => ['title' => 'Titel #1', 'body' => 'Inhalt #1', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #1', 'body' => 'Obsah #1', 'locale' => 'cze']
			],
			[
				'deu' => ['title' => 'Titel #2', 'body' => 'Inhalt #2', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #2', 'body' => 'Obsah #2', 'locale' => 'cze']
			],
			[
				'deu' => ['title' => 'Titel #3', 'body' => 'Inhalt #3', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #3', 'body' => 'Obsah #3', 'locale' => 'cze']
			]
		];

		$translations = $this->_extractTranslations($results);
		$this->assertEquals($expected, $translations->toArray());

		$expected = [
			1 => ['First Article' => 'First Article Body'],
			2 => ['Second Article' => 'Second Article Body'],
			3 => ['Third Article' => 'Third Article Body']
		];

		$grouped = $results->combine('title', 'body', 'id');
		$this->assertEquals($expected, $grouped->toArray());
	}

/**
 * Tests that it is possible to combine find('list') and find('translations')
 *
 * @return void
 */
	public function testFindTranslationsList() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$results = $table
			->find('list', [
				'idField' => 'title',
				'valueField' => '_translations.deu.title',
				'groupField' => 'id'
			])
			->find('translations', ['locales' => ['deu']]);

		$expected = [
			1 => ['First Article' => 'Titel #1'],
			2 => ['Second Article' => 'Titel #2'],
			3 => ['Third Article' => 'Titel #3']
		];
		$this->assertEquals($expected, $results->toArray());
	}

/**
 * Tests that you can both override fields and find all translations
 *
 * @return void
 */
	public function testFindTranslationsWithFieldOverriding() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$table->locale('cze');
		$results = $table->find('translations', ['locales' => ['deu', 'cze']]);
		$expected = [
			[
				'deu' => ['title' => 'Titel #1', 'body' => 'Inhalt #1', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #1', 'body' => 'Obsah #1', 'locale' => 'cze']
			],
			[
				'deu' => ['title' => 'Titel #2', 'body' => 'Inhalt #2', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #2', 'body' => 'Obsah #2', 'locale' => 'cze']
			],
			[
				'deu' => ['title' => 'Titel #3', 'body' => 'Inhalt #3', 'locale' => 'deu'],
				'cze' => ['title' => 'Titulek #3', 'body' => 'Obsah #3', 'locale' => 'cze']
			]
		];

		$translations = $this->_extractTranslations($results);
		$this->assertEquals($expected, $translations->toArray());

		$expected = [
			1 => ['Titulek #1' => 'Obsah #1'],
			2 => ['Titulek #2' => 'Obsah #2'],
			3 => ['Titulek #3' => 'Obsah #3']
		];

		$grouped = $results->combine('title', 'body', 'id');
		$this->assertEquals($expected, $grouped->toArray());
	}

/**
 * Tests that fields can be overriden in a hasMany association
 *
 * @return void
 */
	public function testFindSingleLocaleHasMany() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$table->hasMany('Comments');
		$comments = $table->hasMany('Comments')->target();
		$comments->addBehavior('Translate', ['fields' => ['comment']]);

		$table->locale('eng');
		$comments->locale('eng');

		$results = $table->find()->contain(['Comments' => function($q) {
			return $q->select(['id', 'comment', 'article_id']);
		}]);

		$list = new Collection($results->first()->comments);
		$expected = [
			1 => 'Comment #1',
			2 => 'Comment #2',
			3 => 'Comment #3',
			4 => 'Comment #4'
		];
		$this->assertEquals($expected, $list->combine('id', 'comment')->toArray());
	}

/**
 * Test that it is possible to bring translations from hasMany relations
 *
 * @return void
 */
	public function testTranslationsHasMany() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$table->hasMany('Comments');
		$comments = $table->hasMany('Comments')->target();
		$comments->addBehavior('Translate', ['fields' => ['comment']]);

		$results = $table->find('translations')->contain([
			'Comments' => function($q) {
				return $q->find('translations')->select(['id', 'comment', 'article_id']);
			}
		]);

		$comments = $results->first()->comments;
		$expected = [
			[
				'eng' => ['comment' => 'Comment #1', 'locale' => 'eng']
			],
			[
				'eng' => ['comment' => 'Comment #2', 'locale' => 'eng']
			],
			[
				'eng' => ['comment' => 'Comment #3', 'locale' => 'eng']
			],
			[
				'eng' => ['comment' => 'Comment #4', 'locale' => 'eng'],
				'spa' => ['comment' => 'Comentario #4', 'locale' => 'spa']
			]
		];

		$translations = $this->_extractTranslations($comments);
		$this->assertEquals($expected, $translations->toArray());
	}

/**
 * Tests that it is possible to both override fields with a translation and
 * also find separately other translations
 *
 * @return void
 */
	public function testTranslationsHasManyWithOverride() {
		$table = TableRegistry::get('Articles');
		$table->addBehavior('Translate', ['fields' => ['title', 'body']]);
		$table->hasMany('Comments');
		$comments = $table->hasMany('Comments')->target();
		$comments->addBehavior('Translate', ['fields' => ['comment']]);

		$table->locale('cze');
		$comments->locale('eng');
		$results = $table->find('translations')->contain([
			'Comments' => function($q) {
				return $q->find('translations')->select(['id', 'comment', 'article_id']);
			}
		]);

		$comments = $results->first()->comments;
		$expected = [
			1 => 'Comment #1',
			2 => 'Comment #2',
			3 => 'Comment #3',
			4 => 'Comment #4'
		];
		$list = new Collection($comments);
		$this->assertEquals($expected, $list->combine('id', 'comment')->toArray());

		$expected = [
			[
				'eng' => ['comment' => 'Comment #1', 'locale' => 'eng']
			],
			[
				'eng' => ['comment' => 'Comment #2', 'locale' => 'eng']
			],
			[
				'eng' => ['comment' => 'Comment #3', 'locale' => 'eng']
			],
			[
				'eng' => ['comment' => 'Comment #4', 'locale' => 'eng'],
				'spa' => ['comment' => 'Comentario #4', 'locale' => 'spa']
			]
		];
		$translations = $this->_extractTranslations($comments);
		$this->assertEquals($expected, $translations->toArray());

		$this->assertEquals('Titulek #1', $results->first()->title);
		$this->assertEquals('Obsah #1', $results->first()->body);
	}

}