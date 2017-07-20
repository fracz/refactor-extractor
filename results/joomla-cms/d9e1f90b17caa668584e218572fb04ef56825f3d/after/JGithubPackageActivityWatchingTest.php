<?php

/**
 * Generated by PHPUnit_SkeletonGenerator 1.2.0 on 2013-01-30 at 20:06:32.
 */
class JGithubPackageActivityWatchingTest extends PHPUnit_Framework_TestCase
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
	 * @var JGithubPackageActivityWatching
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

		$this->options = new JRegistry;
		$this->client = $this->getMockBuilder('JGithubHttp')->setMethods(array('get', 'post', 'delete', 'patch', 'put'))->getMock();
		$this->response = $this->getMockBuilder('JHttpResponse')->getMock();

		$this->object = new JGithubPackageActivityWatching($this->options, $this->client);
	}

	/**
	 * @covers JGithubPackageActivityWatching::getList
	 *
	 *     GET /repos/:owner/:repo/subscribers
	 *
	 * Response
	 *
	 * Status: 200 OK
	 * Link: <https://api.github.com/resource?page=2>; rel="next",
	 * <https://api.github.com/resource?page=5>; rel="last"
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 */
	public function testGetList()
	{
		$this->response->code = 200;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
		             ->method('get')
		             ->with('/repos/joomla/joomla-platform/subscribers', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->getList('joomla', 'joomla-platform'),
			$this->equalTo(json_decode($this->response->body))
		)
		;
	}

	/**
	 * @covers JGithubPackageActivityWatching::getRepositories
	 *
	 *     GET /users/:user/subscriptions
	 *
	 * List repositories being watched by the authenticated user.
	 *
	 * GET /user/subscriptions
	 *
	 * Response
	 *
	 * Status: 200 OK
	 * Link: <https://api.github.com/resource?page=2>; rel="next",
	 * <https://api.github.com/resource?page=5>; rel="last"
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 */
	public function testGetRepositories()
	{
		$this->response->code = 200;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
		             ->method('get')
		             ->with('/user/subscriptions', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->getRepositories(),
			$this->equalTo(json_decode($this->response->body))
		)
		;
	}

	public function testGetRepositoriesUser()
	{
		$this->response->code = 200;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
		             ->method('get')
		             ->with('/users/joomla/subscriptions', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->getRepositories('joomla'),
			$this->equalTo(json_decode($this->response->body))
		)
		;
	}

	/**
	 * @covers JGithubPackageActivityWatching::getSubscription
	 *
	 *     GET /repos/:owner/:repo/subscription
	 *
	 * Response
	 *
	 * Status: 200 OK
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 */
	public function testGetSubscription()
	{
		$this->response->code = 200;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
		             ->method('get')
		             ->with('/repos/joomla/joomla-platform/subscription', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->getSubscription('joomla', 'joomla-platform'),
			$this->equalTo(json_decode($this->response->body))
		)
		;
	}

	/**
	 * @covers JGithubPackageActivityWatching::setSubscription
	 *
	 *     PUT /repos/:owner/:repo/subscription
	 *
	 * Input
	 *
	 * subscribed
	 * boolean Determines if notifications should be received from this repository.
	 * ignored
	 * boolean Determines if all notifications should be blocked from this repository.
	 *
	 * Response
	 *
	 * Status: 200 OK
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 */
	public function testSetSubscription()
	{
		$this->response->code = 200;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
		             ->method('put')
		             ->with('/repos/joomla/joomla-platform/subscription', '{"subscribed":true,"ignored":false}', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->setSubscription('joomla', 'joomla-platform', true, false),
			$this->equalTo(json_decode($this->response->body))
		)
		;
	}

	/**
	 * @covers JGithubPackageActivityWatching::deleteSubscription
	 *
	 *     DELETE /repos/:owner/:repo/subscription
	 *
	 * Response
	 *
	 * Status: 204 No Content
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 */
	public function testDeleteSubscription()
	{
		$this->response->code = 204;
		$this->response->body = '';

		$this->client->expects($this->once())
		             ->method('delete')
		             ->with('/repos/joomla/joomla-platform/subscription', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->deleteSubscription('joomla', 'joomla-platform'),
			$this->equalTo($this->response->body)
		)
		;
	}

	/**
	 * @covers JGithubPackageActivityWatching::check
	 *
	 *     GET /user/subscriptions/:owner/:repo
	 *
	 * Response if this repository is watched by you
	 *
	 * Status: 204 No Content
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 *
	 * Response if this repository is not watched by you
	 *
	 * Status: 404 Not Found
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 */
	public function testCheck()
	{
		$this->response->code = 204;
		$this->response->body = '';

		$this->client->expects($this->once())
		             ->method('get')
		             ->with('/user/subscriptions/joomla/joomla-platform', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->check('joomla', 'joomla-platform'),
			$this->equalTo(true)
		)
		;
	}

	public function testCheckFalse()
	{
		$this->response->code = 404;
		$this->response->body = '';

		$this->client->expects($this->once())
		             ->method('get')
		             ->with('/user/subscriptions/joomla/joomla-platform', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->check('joomla', 'joomla-platform'),
			$this->equalTo(false)
		)
		;
	}

	/**
	 * @expectedException UnexpectedValueException
	 */
	public function testCheckUnexpected()
	{
		$this->response->code = 666;
		$this->response->body = '';

		$this->client->expects($this->once())
		             ->method('get')
		             ->with('/user/subscriptions/joomla/joomla-platform', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->object->check('joomla', 'joomla-platform');
	}

	/**
	 * @covers JGithubPackageActivityWatching::watch
	 *
	 *     PUT /user/subscriptions/:owner/:repo
	 *
	 * Response
	 *
	 * Status: 204 No Content
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 */
	public function testWatch()
	{
		$this->response->code = 204;
		$this->response->body = '';

		$this->client->expects($this->once())
		             ->method('put')
		             ->with('/user/subscriptions/joomla/joomla-platform', '', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->watch('joomla', 'joomla-platform'),
			$this->equalTo($this->response->body)
		)
		;
	}

	/**
	 * @covers JGithubPackageActivityWatching::unwatch
	 *
	 *     DELETE /user/subscriptions/:owner/:repo
	 *
	 * Response
	 *
	 * Status: 204 No Content
	 * X-RateLimit-Limit: 5000
	 * X-RateLimit-Remaining: 4999
	 */
	public function testUnwatch()
	{
		$this->response->code = 204;
		$this->response->body = '';

		$this->client->expects($this->once())
		             ->method('delete')
		             ->with('/user/subscriptions/joomla/joomla-platform', 0, 0)
		             ->will($this->returnValue($this->response))
		;

		$this->assertThat(
			$this->object->unwatch('joomla', 'joomla-platform'),
			$this->equalTo($this->response->body)
		)
		;
	}
}