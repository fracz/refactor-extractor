<?php
/**
 * @package     Joomla.UnitTest
 * @subpackage  Mail
 *
 * @copyright   Copyright (C) 2005 - 2016 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE
 */

/**
 * Test class for JMail.
 * Generated by PHPUnit on 2011-10-26 at 19:32:59.
 *
 * @package     Joomla.UnitTest
 * @subpackage  Mail
 * @since       11.1
 */
class JMailTest extends TestCase
{
	/**
	 * @var JMail
	 */
	protected $object;

	/**
	 * Sets up the fixture, for example, opens a network connection.
	 * This method is called before a test is executed.
	 *
	 * @return void
	 */
	protected function setUp()
	{
		parent::setUp();

		$this->object = new JMail;
	}

	/**
	 * Tears down the fixture, for example, closes a network connection.
	 * This method is called after a test is executed.
	 *
	 * @return void
	 */
	protected function tearDown()
	{

	}

	/**
	 * Provides test data for request format detection.
	 *
	 * @return array
	 */
	public function seedTestAdd()
	{
		// Recipient, name, method
		return array(
			array('test@example.com', 'test_name', 'AddAddress', array(array('test@example.com', 'test_name'))),
			array(array('test_1@example.com', 'test_2@example.com'), 'test_name', 'AddAddress',
				array(array('test_1@example.com', 'test_name'), array('test_2@example.com', 'test_name'))),
			array(array('test_1@example.com', 'test_2@example.com'), array('test_name1', 'test_name2'), 'AddAddress',
				array(array('test_1@example.com', 'test_name1'), array('test_2@example.com', 'test_name2'))),
			array('test@example.com', 'test_name', 'AddCC', array(array('test@example.com', 'test_name'))),
			array(array('test_1@example.com', 'test_2@example.com'), 'test_name', 'AddCC',
				array(array('test_1@example.com', 'test_name'), array('test_2@example.com', 'test_name'))),
			array(array('test_1@example.com', 'test_2@example.com'), array('test_name1', 'test_name2'), 'AddCC',
				array(array('test_1@example.com', 'test_name1'), array('test_2@example.com', 'test_name2'))),
			array('test@example.com', 'test_name', 'AddBCC', array(array('test@example.com', 'test_name'))),
			array(array('test_1@example.com', 'test_2@example.com'), 'test_name', 'AddBCC',
				array(array('test_1@example.com', 'test_name'), array('test_2@example.com', 'test_name'))),
			array(array('test_1@example.com', 'test_2@example.com'), array('test_name1', 'test_name2'), 'AddBCC',
				array(array('test_1@example.com', 'test_name1'), array('test_2@example.com', 'test_name2'))),
			array('test@example.com', 'test_name', 'AddReplyTo',
				array('test@example.com' => array('test@example.com', 'test_name'))),
			array(array('test_1@example.com', 'test_2@example.com'), 'test_name', 'AddReplyTo',
				array(
					'test_1@example.com' => array('test_1@example.com', 'test_name'),
					'test_2@example.com' => array('test_2@example.com', 'test_name')
				)
			),
			array(array('test_1@example.com', 'test_2@example.com'), array('test_name1', 'test_name2'), 'AddReplyTo',
				array(
					'test_1@example.com' => array('test_1@example.com', 'test_name1'),
					'test_2@example.com' => array('test_2@example.com', 'test_name2')
				)
			)
		);
	}

	/**
	 * Tests the add method
	 *
	 * @param   mixed   $recipient  Either a string or array of strings [email address(es)]
	 * @param   mixed   $name       Either a string or array of strings [name(s)]
	 * @param   string  $method     The parent method's name.
	 * @param   array   $expected   The expected array.
	 *
	 * @covers  JMail::add
	 * @dataProvider  seedTestAdd
	 *
	 * @return void
	 */
	public function testAdd($recipient, $name, $method, $expected)
	{
		TestReflection::invoke($this->object, 'add', $recipient, $name, $method);

		switch ($method)
		{
			case 'AddAddress':
				$type = 'to';
				break;
			case 'AddCC':
				$type = 'cc';
				break;
			case 'AddBCC':
				$type = 'bcc';
				break;
			case 'AddReplyTo':
				$type = 'ReplyTo';
				break;
		}

		$this->assertThat($expected, $this->equalTo(TestReflection::getValue($this->object, $type)));
	}

	/**
	 * Tests the addRecipient method.
	 *
	 * @covers  JMail::addRecipient
	 *
	 * @return void
	 */
	public function testAddRecipient()
	{
		$recipient = 'test@example.com';
		$name = 'test_name';
		$expected = array(array('test@example.com', 'test_name'));

		$this->object->addRecipient($recipient, $name);
		$this->assertThat($expected, $this->equalTo(TestReflection::getValue($this->object, 'to')));
	}

	/**
	 * Tests the addCC method.
	 *
	 * @covers  JMail::addCc
	 *
	 * @return void
	 */
	public function testAddCc()
	{
		$recipient = 'test@example.com';
		$name = 'test_name';
		$expected = array(array('test@example.com', 'test_name'));

		$this->object->addCc($recipient, $name);
		$this->assertThat($expected, $this->equalTo(TestReflection::getValue($this->object, 'cc')));
	}

	/**
	 * Tests the addBCC method.
	 *
	 * @covers  JMail::addBcc
	 *
	 * @return void
	 */
	public function testAddBcc()
	{
		$recipient = 'test@example.com';
		$name = 'test_name';
		$expected = array(array('test@example.com', 'test_name'));

		$this->object->addBcc($recipient, $name);
		$this->assertThat($expected, $this->equalTo(TestReflection::getValue($this->object, 'bcc')));
	}

	/**
	 * Test...
	 *
	 * @return void
	 */
	public function testAddAttachment()
	{
		$attachments = array(JPATH_PLATFORM . '/joomla/mail/mail.php');
		$names = array('mail.php');

		$mail = new JMail;
		$mail->addAttachment($attachments, $names);

		$actual = $mail->GetAttachments();
		$actual_attachments = array();
		$actual_names = array();

		foreach ($actual as $attach)
		{
			array_push($actual_attachments, $attach[0]);
			array_push($actual_names, $attach[2]);
		}

		$this->assertThat($attachments, $this->equalTo($actual_attachments));
		$this->assertThat($names, $this->equalTo($actual_names));
	}

	/**
	 * Tests the addReplyTo method.
	 *
	 * @covers  JMail::addReplyTo
	 *
	 * @return void
	 */
	public function testAddReplyTo()
	{
		$recipient = 'test@example.com';
		$name = 'test_name';
		$expected = array('test@example.com' => array('test@example.com', 'test_name'));

		$this->object->addReplyTo($recipient, $name);
		$this->assertThat($expected, $this->equalTo(TestReflection::getValue($this->object, 'ReplyTo')));
	}

	/**
	 * Tests the IsHTML method.
	 *
	 * @covers  JMail::IsHTML
	 *
	 * @return void
	 */
	public function testIsHtml()
	{
		$this->object->isHtml(false);

		$this->assertThat('text/plain', $this->equalTo($this->object->ContentType));
	}

	/**
	 * Test data for testUseSMTP method
	 *
	 * @return  array
	 *
	 * @since   12.1
	 */
	public function dataUseSmtp()
	{
		return array(
			'SMTP without Authentication' => array(
				null,
				'example.com',
				null,
				null,
				false,
				null,
				array(
					'called' => 'IsSMTP',
					'return' => true
				)
			)
		);
	}

	/**
	 * Test for the JMail::useSMTP method.
	 *
	 * @param   string   $auth      SMTP Authentication
	 * @param   string   $host      SMTP Host
	 * @param   string   $user      SMTP Username
	 * @param   string   $pass      SMTP Password
	 * @param   string   $secure    Use secure methods
	 * @param   integer  $port      The SMTP port
	 * @param   string   $expected  The expected result
	 *
	 * @return  void
	 *
	 * @since   12.1
	 *
	 * @dataProvider  dataUseSMTP
	 */
	public function testUseSmtp($auth, $host, $user, $pass, $secure, $port, $expected)
	{
		$mail = $this->getMock('JMail', array('SetLanguage', 'IsSMTP', 'IsMail'));

		$mail->expects(
			$this->once()
		)
			->method($expected['called']);

		$this->assertThat(
			$mail->useSmtp($auth, $host, $user, $pass, $secure, $port),
			$this->equalTo($expected['return'])
		);
	}
}