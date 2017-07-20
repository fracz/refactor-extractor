<?php
/**
 * Generated by PHPUnit_SkeletonGenerator 1.2.0 on 2013-01-30 at 14:24:14.
 */
class JGithubPackageMarkdownTest extends \PHPUnit\Framework\TestCase
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
	 * @var JGithubPackageMarkdown
	 */
	protected $object;

	/**
	 * Sets up the fixture, for example, opens a network connection.
	 * This method is called before a test is executed.
	 */
	protected function setUp()
	{
		parent::setUp();

		$this->options  = new JRegistry;
		$this->client   = $this->getMockBuilder('JGithubHttp')->setMethods(array('get', 'post', 'delete', 'patch', 'put'))->getMock();
		$this->response = $this->getMockBuilder('JHttpResponse')->getMock();

		$this->object = new JGithubPackageMarkdown($this->options, $this->client);
	}

	/**
	 * Overrides the parent tearDown method.
	 *
	 * @return  void
	 *
	 * @see     \PHPUnit\Framework\TestCase::tearDown()
	 * @since   3.6
	 */
	protected function tearDown()
	{
		unset($this->options, $this->client, $this->response, $this->object);
		parent::tearDown();
	}

	/**
	 * @covers JGithubPackageMarkdown::render
	 */
	public function testRender()
	{
		$this->response->code = 200;
		$this->response->body = '<p>Hello world <a href="http://github.com/github/linguist/issues/1" '
			. 'class="issue-link" title="This is a simple issue">github/linguist#1</a> <strong>cool</strong>, '
			. 'and <a href="http://github.com/github/gollum/issues/1" class="issue-link" '
			. 'title="This is another issue">#1</a>!</p>';

		$text    = 'Hello world github/linguist#1 **cool**, and #1!';
		$mode    = 'gfm';
		$context = 'github/gollum';

		$data = str_replace('\\/', '/', json_encode(
				array(
					'text'    => $text,
					'mode'    => $mode,
					'context' => $context
				)
			)
		);

		$this->client->expects($this->once())
			->method('post')
			->with('/markdown', $data, 0, 0)
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->render($text, $mode, $context),
			$this->equalTo($this->response->body)
		);
	}

	/**
	 * @expectedException  InvalidArgumentException
	 */
	public function testRenderInvalidMode()
	{
		$this->assertThat(
			$this->object->render('', 'xxx', 'github/gollum'),
			$this->equalTo('')
		);
	}

	/**
	 * @expectedException  DomainException
	 */
	public function testRenderFailure()
	{
		$this->response->code = 404;
		$this->response->body = '';

		$text    = 'Hello world github/linguist#1 **cool**, and #1!';
		$mode    = 'gfm';
		$context = 'github/gollum';

		$data = str_replace('\\/', '/', json_encode(
				array(
					'text'    => $text,
					'mode'    => $mode,
					'context' => $context
				)
			)
		);

		$this->client->expects($this->once())
			->method('post')
			->with('/markdown', $data, 0, 0)
			->will($this->returnValue($this->response));

		$this->assertThat(
			$this->object->render($text, $mode, $context),
			$this->equalTo('')
		);
	}
}