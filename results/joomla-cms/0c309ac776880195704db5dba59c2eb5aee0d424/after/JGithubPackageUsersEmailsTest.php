<?php
/**
 * Generated by PHPUnit_SkeletonGenerator 1.2.0 on 2013-01-31 at 11:14:25.
 */
class JGithubPackageUsersEmailsTest extends PHPUnit_Framework_TestCase
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
     * @var JGithubPackageUsersEmails
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

		$this->object = new JGithubPackageUsersEmails($this->options, $this->client);
	}

    /**
     * @covers JGithubPackageUsersEmails::getList
     * @todo   Implement testGetList().
     */
    public function testGetList()
    {
	    $this->response->code = 200;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('get')
		    ->with('/user/emails')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->getList(),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }

    /**
     * @covers JGithubPackageUsersEmails::add
     * @todo   Implement testAdd().
     */
    public function testAdd()
    {
	    $this->response->code = 201;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('post')
		    ->with('/user/emails')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->add('email@example.com'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }

    /**
     * @covers JGithubPackageUsersEmails::delete
     * @todo   Implement testDelete().
     */
    public function testDelete()
    {
	    $this->response->code = 204;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('delete')
		    ->with('/user/emails')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->delete('email@example.com'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }
}