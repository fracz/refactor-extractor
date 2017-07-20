<?php
/**
 * Generated by PHPUnit_SkeletonGenerator 1.2.0 on 2013-02-01 at 01:21:58.
 */
class JGithubPackageSearchTest extends PHPUnit_Framework_TestCase
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
     * @var JGithubPackageSearch
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

		$this->object = new JGithubPackageSearch($this->options, $this->client);
	}

    /**
     * @covers JGithubPackageSearch::issues
     */
    public function testIssues()
    {
	    $this->response->code = 200;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('get')
		    ->with('/legacy/issues/search/joomla/joomla-platform/open/github')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->issues('joomla', 'joomla-platform', 'open', 'github'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }

	/**
	 * @covers JGithubPackageSearch::issues
	 *
	 * @expectedException UnexpectedValueException
	 */
	public function testIssuesInvalidState()
	{
		$this->response->code = 200;
		$this->response->body = $this->sampleString;

		$this->object->issues('joomla', 'joomla-platform', 'invalid', 'github');
	}

	/**
     * @covers JGithubPackageSearch::repositories
     */
    public function testRepositories()
    {
	    $this->response->code = 200;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('get')
		    ->with('/legacy/repos/search/joomla')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->repositories('joomla'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }

    /**
     * @covers JGithubPackageSearch::users
     */
    public function testUsers()
    {
	    $this->response->code = 200;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('get')
		    ->with('/legacy/user/search/joomla')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->users('joomla'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }

    /**
     * @covers JGithubPackageSearch::email
     */
    public function testEmail()
    {
	    $this->response->code = 200;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('get')
		    ->with('/legacy/user/email/email@joomla')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->email('email@joomla'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }

}