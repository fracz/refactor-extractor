<?php
/**
 * Generated by PHPUnit_SkeletonGenerator 1.2.0 on 2013-01-31 at 02:25:55.
 */
class JGithubPackagePullsCommentsTest extends PHPUnit_Framework_TestCase
{
	/**
	 * @var    JRegistry  Options for the GitHub object.
	 * @since  11.4
	 */
	protected $options;

	/**
	 * @var    JGithubHttp  Mock client object.
	 * @since  11.4
	 */
	protected $client;

	/**
	 * @var    JHttpResponse  Mock response object.
	 * @since  12.3
	 */
	protected $response;

	/**
	 * @var JGithubPackagePullsComments
	 */
	protected $object;

	/**
	 * @var    string  Sample JSON string.
	 * @since  12.3
	 */
	protected $sampleString = '{"a":1,"b":2,"c":3,"d":4,"e":5}';

	/**
	 * @var    string  Sample JSON error message.
	 * @since  12.3
	 */
	protected $errorString = '{"message": "Generic Error"}';

	/**
	 * Sets up the fixture, for example, opens a network connection.
	 * This method is called before a test is executed.
	 */
	protected function setUp()
	{
		parent::setUp();

		$this->options  = new JRegistry;
		$this->client   = $this->getMock('JGithubHttp', array('get', 'post', 'delete', 'patch', 'put'));
		$this->response = $this->getMock('JHttpResponse');

		$this->object = new JGithubPackagePullsComments($this->options, $this->client);
	}

	/**
	 * @covers JGithubPackagePullsComments::create
	 * @todo   Implement testCreate().
	 */
	public function testCreate()
	{
		$this->response->code = 201;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('post')
			->with('/repos/joomla/joomla-platform/pulls/1/comments', '{"body":"The Body","commit_id":"123abc","path":"a\/b\/c","position":456}', 0, 0)
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->create('joomla', 'joomla-platform', 1, 'The Body', '123abc', 'a/b/c', 456),
			$this->equalTo(json_decode($this->response->body))
		);
	}

	/**
	 * @covers JGithubPackagePullsComments::createReply
	 * @todo   Implement testCreateReply().
	 */
	public function testCreateReply()
	{
		$this->response->code = 201;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('post')
			->with('/repos/joomla/joomla-platform/pulls/1/comments', '{"body":"The Body","in_reply_to":456}', 0, 0)
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->createReply('joomla', 'joomla-platform', 1, 'The Body', 456),
			$this->equalTo(json_decode($this->response->body))
		);
	}

	/**
	 * @covers JGithubPackagePullsComments::delete
	 * @todo   Implement testDelete().
	 */
	public function testDelete()
	{
		$this->response->code = 204;
		$this->response->body = '';

		$this->client->expects($this->once())
			->method('delete')
			->with('/repos/joomla/joomla-platform/pulls/comments/456', 0, 0)
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->delete('joomla', 'joomla-platform', 456),
			$this->equalTo(json_decode($this->response->body))
		);
	}

	/**
	 * @covers JGithubPackagePullsComments::edit
	 * @todo   Implement testEdit().
	 */
	public function testEdit()
	{
		$this->response->code = 200;
		$this->response->body = '';

		$this->client->expects($this->once())
			->method('patch')
			->with('/repos/joomla/joomla-platform/pulls/comments/456', '{"body":"Hello"}', 0, 0)
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->edit('joomla', 'joomla-platform', 456, 'Hello'),
			$this->equalTo(json_decode($this->response->body))
		);
	}

	/**
	 * @covers JGithubPackagePullsComments::get
	 * @todo   Implement testGet().
	 */
	public function testGet()
	{
		$this->response->code = 200;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('get')
			->with('/repos/joomla/joomla-platform/pulls/comments/456', 0, 0)
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->get('joomla', 'joomla-platform', 456, 'Hello'),
			$this->equalTo(json_decode($this->response->body))
		);
	}

	/**
	 * @covers JGithubPackagePullsComments::getList
	 * @todo   Implement testGetList().
	 */
	public function testGetList()
	{
		$this->response->code = 200;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('get')
			->with('/repos/joomla/joomla-platform/pulls/456/comments', 0, 0)
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->getList('joomla', 'joomla-platform', 456),
			$this->equalTo(json_decode($this->response->body))
		);
	}
}