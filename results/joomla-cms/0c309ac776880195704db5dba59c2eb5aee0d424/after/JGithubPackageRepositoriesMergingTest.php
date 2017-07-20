<?php
/**
 * Generated by PHPUnit_SkeletonGenerator 1.2.0 on 2013-01-29 at 14:34:13.
 */
class JGithubPackageRepositoriesMergingTest extends PHPUnit_Framework_TestCase
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
	 * @var JGithubPackageRepositoriesMerging
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
	 *
	 * @since   ¿
	 *
	 * @return  void
	 */
	protected function setUp()
	{
		parent::setUp();

		$this->options  = new JRegistry;
		$this->client   = $this->getMock('JGithubHttp', array('get', 'post', 'delete', 'patch', 'put'));
		$this->response = $this->getMock('JHttpResponse');

		$this->object = new JGithubPackageRepositoriesMerging($this->options, $this->client);
	}

	/**
	 * @covers JGithubPackageRepositoriesMerging::perform
	 * @todo   Implement testPerform().
	 *
	 *     POST /repos/:owner/:repo/merges

	Input

	base
	Required string - The name of the base branch that the head will be merged into.
	head
	Required string - The head to merge. This can be a branch name or a commit SHA1.
	commit_message
	Optional string - Commit message to use for the merge commit. If omitted, a default message will be used.

	{
	"base": "master",
	"head": "cool_feature",
	"commit_message": "Shipped cool_feature!"
	}

	Successful Response (The resulting merge commit)

	Status: 201 Created
	X-RateLimit-Limit: 5000
	X-RateLimit-Remaining: 4999

	{
	"commit": {
	"sha": "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d",
	"commit": {
	"author": {
	"name": "The Octocat",
	"date": "2012-03-06T15:06:50-08:00",
	"email": "octocat@nowhere.com"
	},
	"url": "https://api.github.com/repos/octocat/Hello-World/git/commits/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d",
	"message": "Shipped cool_feature!",
	"tree": {
	"sha": "b4eecafa9be2f2006ce1b709d6857b07069b4608",
	"url": "https://api.github.com/repos/octocat/Hello-World/git/trees/b4eecafa9be2f2006ce1b709d6857b07069b4608"
	},
	"committer": {
	"name": "The Octocat",
	"date": "2012-03-06T15:06:50-08:00",
	"email": "octocat@nowhere.com"
	}
	},
	"author": {
	"gravatar_id": "7ad39074b0584bc555d0417ae3e7d974",
	"avatar_url": "https://secure.gravatar.com/avatar/7ad39074b0584bc555d0417ae3e7d974?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png",
	"url": "https://api.github.com/users/octocat",
	"id": 583231,
	"login": "octocat"
	},
	"parents": [
	{
	"sha": "553c2077f0edc3d5dc5d17262f6aa498e69d6f8e",
	"url": "https://api.github.com/repos/octocat/Hello-World/commits/553c2077f0edc3d5dc5d17262f6aa498e69d6f8e"
	},
	{
	"sha": "762941318ee16e59dabbacb1b4049eec22f0d303",
	"url": "https://api.github.com/repos/octocat/Hello-World/commits/762941318ee16e59dabbacb1b4049eec22f0d303"
	}
	],
	"url": "https://api.github.com/repos/octocat/Hello-World/commits/7fd1a60b01f91b314f59955a4e4d4e80d8edf11d",
	"committer": {
	"gravatar_id": "7ad39074b0584bc555d0417ae3e7d974",
	"avatar_url": "https://secure.gravatar.com/avatar/7ad39074b0584bc555d0417ae3e7d974?d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-140.png",
	"url": "https://api.github.com/users/octocat",
	"id": 583231,
	"login": "octocat"
	}
	}
	}

	No-op response (base already contains the head, nothing to merge)

	Status: 204 No Content
	X-RateLimit-Limit: 5000
	X-RateLimit-Remaining: 4999

	Merge conflict response

	Status: 409 Conflict
	X-RateLimit-Limit: 5000
	X-RateLimit-Remaining: 4999

	{
	"message": "Merge Conflict"
	}

	Missing base response

	Status: 404 Not Found
	X-RateLimit-Limit: 5000
	X-RateLimit-Remaining: 4999

	{
	"message": "Base does not exist"
	}

	Missing head response

	Status: 404 Not Found
	X-RateLimit-Limit: 5000
	X-RateLimit-Remaining: 4999

	{
	"message": "Head does not exist"
	}

	 */
	public function testPerform()
	{
		$this->response->code = 201;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('post')
			->with('/repos/joomla/joomla-platform/merges')
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->perform('joomla', 'joomla-platform', '123', '456', 'My Message'),
			$this->equalTo(json_decode($this->sampleString))
		);
	}

	/**
	 * @expectedException UnexpectedValueException
	 */
	public function testPerformNoOp()
	{
		$this->response->code = 204;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('post')
			->with('/repos/joomla/joomla-platform/merges')
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->perform('joomla', 'joomla-platform', '123', '456', 'My Message'),
			$this->equalTo(json_decode($this->sampleString))
		);
	}

	/**
	 * @expectedException UnexpectedValueException
	 */
	public function testPerformMissing()
	{
		$this->response->code = 404;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('post')
			->with('/repos/joomla/joomla-platform/merges')
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->perform('joomla', 'joomla-platform', '123', '456', 'My Message'),
			$this->equalTo(json_decode($this->sampleString))
		);
	}

	/**
	 * @expectedException UnexpectedValueException
	 */
	public function testPerformConflict()
	{
		$this->response->code = 409;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('post')
			->with('/repos/joomla/joomla-platform/merges')
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->perform('joomla', 'joomla-platform', '123', '456', 'My Message'),
			$this->equalTo(json_decode($this->sampleString))
		);
	}

	/**
	 * @expectedException UnexpectedValueException
	 */
	public function testPerformUnexpected()
	{
		$this->response->code = 666;
		$this->response->body = $this->sampleString;

		$this->client->expects($this->once())
			->method('post')
			->with('/repos/joomla/joomla-platform/merges')
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->perform('joomla', 'joomla-platform', '123', '456', 'My Message'),
			$this->equalTo(json_decode($this->sampleString))
		);
	}
}