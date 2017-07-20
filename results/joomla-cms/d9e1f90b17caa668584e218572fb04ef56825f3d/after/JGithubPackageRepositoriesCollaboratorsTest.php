<?php
/**
 * Generated by PHPUnit_SkeletonGenerator 1.2.0 on 2013-02-01 at 14:05:16.
 */
class JGithubPackageRepositoriesCollaboratorsTest extends PHPUnit_Framework_TestCase
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
     * @var JGithubPackageRepositoriesCollaborators
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
		$this->client   = $this->getMockBuilder('JGithubHttp')->setMethods(array('get', 'post', 'delete', 'patch', 'put'))->getMock();
		$this->response = $this->getMockBuilder('JHttpResponse')->getMock();

		$this->object = new JGithubPackageRepositoriesCollaborators($this->options, $this->client);
	}

    /**
     * @covers JGithubPackageRepositoriesCollaborators::getList
     */
    public function testGetList()
    {
	    $this->response->code = 200;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('get')
		    ->with('/repos/joomla/joomla-platform/collaborators')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->getList('joomla', 'joomla-platform'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }

    /**
     * @covers JGithubPackageRepositoriesCollaborators::get
     */
    public function testGet()
    {
	    $this->response->code = 204;
	    $this->response->body = true;

	    $this->client->expects($this->once())
		    ->method('get')
		    ->with('/repos/joomla/joomla-platform/collaborators/elkuku')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->get('joomla', 'joomla-platform', 'elkuku'),
		    $this->equalTo($this->response->body)
	    );
    }

	/**
	 * @covers JGithubPackageRepositoriesCollaborators::get
	 */
	public function testGetNegative()
	{
		$this->response->code = 404;
		$this->response->body = false;

		$this->client->expects($this->once())
			->method('get')
			->with('/repos/joomla/joomla-platform/collaborators/elkuku')
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->get('joomla', 'joomla-platform', 'elkuku'),
			$this->equalTo($this->response->body)
		);
	}

	/**
	 * @covers JGithubPackageRepositoriesCollaborators::get
	 *
	 * @expectedException UnexpectedValueException
	 */
	public function testGetUnexpected()
	{
		$this->response->code = 666;
		$this->response->body = null;

		$this->client->expects($this->once())
			->method('get')
			->with('/repos/joomla/joomla-platform/collaborators/elkuku')
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->get('joomla', 'joomla-platform', 'elkuku'),
			$this->equalTo($this->response->body)
		);
	}

	/**
     * @covers JGithubPackageRepositoriesCollaborators::add
     */
    public function testAdd()
    {
	    $this->response->code = 204;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('put')
		    ->with('/repos/joomla/joomla-platform/collaborators/elkuku')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->add('joomla', 'joomla-platform', 'elkuku'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }

    /**
     * @covers JGithubPackageRepositoriesCollaborators::remove
     */
    public function testRemove()
    {
	    $this->response->code = 204;
	    $this->response->body = $this->sampleString;

	    $this->client->expects($this->once())
		    ->method('delete')
		    ->with('/repos/joomla/joomla-platform/collaborators/elkuku')
		    ->will($this->returnValue($this->response));

	    $this->assertThat(
		    $this->object->remove('joomla', 'joomla-platform', 'elkuku'),
		    $this->equalTo(json_decode($this->sampleString))
	    );
    }
}