<?php
require_once dirname(__FILE__) . '/../../bootstrap.php';


class Elastica_TypeTest extends PHPUnit_Framework_TestCase
{
    public function setUp() {
    }

    public function tearDown() {
    }

    public function testTest() {
    	// Creates a new index 'xodoa' and a type 'user' inside this index
    	$client = new Elastica_Client();
    	$index = new Elastica_Index($client, 'xodoa');
    	$index->create(array(), true);

    	$type = new Elastica_Type($index, 'user');


    	// Adds 1 document to the index
    	$doc1 = new Elastica_Document(1,
    		array('username' => 'hans', 'test' => array('2', '3', '5'))
    	);
    	$type->addDocument($doc1);

    	// Adds a list of documents with _bulk upload to the index
    	$docs = array();
    	$docs[] = new Elastica_Document(2,
    		array('username' => 'john', 'test' => array('1', '3', '6'))
    	);
    	$docs[] = new Elastica_Document(3,
    		array('username' => 'rolf', 'test' => array('2', '3', '7'))
    	);
    	$type->addDocuments($docs);
    	$index->refresh();

    	$resultSet = $type->search('rolf');
		$this->assertEquals(1, $resultSet->count());

		// Test if source is returned
		$result = $resultSet->current();
		$this->assertEquals(3, $result->getId());
		$data = $result->getData();
		$this->assertEquals('rolf', $data['user']['username']);

    }

	public function testNoSource() {
		// Creates a new index 'xodoa' and a type 'user' inside this index
    	$client = new Elastica_Client();
    	$index = new Elastica_Index($client, 'xodoa');
    	$index->create(array(), true);

    	$type = new Elastica_Type($index, 'user');
		$type->setMapping(
			array(
				'id' => array('type' => 'integer', 'store' => 'yes'),
				'username' => array('type' => 'string', 'store' => 'no'),
			), false
		);


    	// Adds 1 document to the index
    	$doc1 = new Elastica_Document(1,
    		array('username' => 'hans', 'test' => array('2', '3', '5'))
    	);
    	$type->addDocument($doc1);

    	// Adds a list of documents with _bulk upload to the index
    	$docs = array();
    	$docs[] = new Elastica_Document(2,
    		array('username' => 'john', 'test' => array('1', '3', '6'))
    	);
    	$docs[] = new Elastica_Document(3,
    		array('username' => 'rolf', 'test' => array('2', '3', '7'))
    	);
    	$type->addDocuments($docs);

		// To update index
    	$index->refresh();

    	$resultSet = $type->search('rolf');

		$this->assertEquals(1, $resultSet->count());

		// Tests if no source is in response except id
		$result = $resultSet->current();
		$this->assertEquals(3, $result->getId());
		$this->assertEmpty($result->getData());
	}

    public function testSearchDefault() {

    }
}