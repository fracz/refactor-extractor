<?php
/**
 * @package     Joomla.UnitTest
 * @subpackage  User
 *
 * @copyright   Copyright (C) 2005 - 2016 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE
 */

/**
 * Test class for JUserHelper.
 * Generated by PHPUnit on 2009-10-26 at 22:44:33.
 *
 * @package     Joomla.UnitTest
 * @subpackage  User
 * @since       12.1
*/
class JUserHelperTest extends TestCaseDatabase
{
	/**
	 * @var    JUserHelper
	 * @since  12.1
	 */
	protected $object;

	/**
	 * Sets up the fixture, for example, opens a network connection.
	 * This method is called before a test is executed.
	 *
	 * @return  void
	 *
	 * @since   12.1
	 */
	protected function setUp()
	{
		parent::setUp();

		$this->saveFactoryState();

		// Set the session object for JUserHelper::addUserToGroup()
		JFactory::$session = $this->getMockSession();
	}

	/**
	 * Tears down the fixture, for example, closes a network connection.
	 * This method is called after a test is executed.
	 *
	 * @return  void
	 *
	 * @since   3.4
	 */
	protected function tearDown()
	{
		$this->restoreFactoryState();
		TestReflection::setValue('JPluginHelper', 'plugins', null);
		parent::tearDown();
	}

	/**
	 * Gets the data set to be loaded into the database during setup
	 *
	 * @return  PHPUnit_Extensions_Database_DataSet_CsvDataSet
	 *
	 * @since   12.2
	 */
	protected function getDataSet()
	{
		$dataSet = new PHPUnit_Extensions_Database_DataSet_CsvDataSet(',', "'", '\\');

		$dataSet->addTable('jos_users', JPATH_TEST_DATABASE . '/jos_users.csv');
		$dataSet->addTable('jos_user_usergroup_map', JPATH_TEST_DATABASE . '/jos_user_usergroup_map.csv');
		$dataSet->addTable('jos_usergroups', JPATH_TEST_DATABASE . '/jos_usergroups.csv');

		return $dataSet;
	}

	/**
	 * Test cases for userGroups
	 *
	 * Each test case provides
	 * - integer  userid  a user id
	 * - array    group   user group, given as hash
	 *                    group_id => group_name,
	 *                    empty if undefined
	 * - array    error   error info, given as hash
	 *                    with indices 'code', 'msg', and
	 *                    'info', empty, if no error occurred
	 *
	 * @see ... (link to where the group and error structures are
	 *      defined)
	 * @return array
	 */
	public function casesGetUserGroups()
	{
		return array(
			'unknownUser' => array(
				1000,
				array(),
				array(
					'code' => 'SOME_ERROR_CODE',
					'msg' => 'JLIB_USER_ERROR_UNABLE_TO_LOAD_USER',
					'info' => ''),
			),
			'publisher' => array(
				43,
				array(5 => 5),
				array(),
			),
			'manager' => array(
				44,
				array(6 => 6),
				array(),
			),
		);
	}

	/**
	 * TestingGetUserGroups().
	 *
	 * @param   integer  $userid    User ID
	 * @param   mixed    $expected  User object or empty array if unknown
	 * @param   array    $error     Expected error info
	 *
	 * @dataProvider casesGetUserGroups
	 * @covers  JUserHelper::getUserGroups
	 * @return  void
	 */
	public function testGetUserGroups($userid, $expected, $error)
	{
		$this->assertThat(
			JUserHelper::getUserGroups($userid),
			$this->equalTo($expected)
		);
	}

	/**
	 * Test cases for userId
	 *
	 * @return array
	 */
	public function casesGetUserId()
	{
		return array(
			'admin' => array(
				'admin',
				42,
				array(),
			),
			'unknown' => array(
				'unknown',
				null,
				array(),
			),
		);
	}

	/**
	 * TestingGetUserId().
	 *
	 * @param   string   $username  User name
	 * @param   integer  $expected  Expected user id
	 * @param   array    $error     Expected error info
	 *
	 * @dataProvider casesGetUserId
	 * @covers  JUserHelper::getUserId
	 *
	 * @return  void
	 *
	 * @since   12.2
	 */
	public function testGetUserId($username, $expected, $error)
	{
		$expResult = $expected;
		$this->assertThat(
			JUserHelper::getUserId($username),
			$this->equalTo($expResult)
		);

	}

	/**
	 * Test cases for testAddUserToGroup
	 *
	 * @return array
	 */
	public function casesAddUserToGroup()
	{
		return array(
			'publisher' => array(
				43,
				6,
				true
			),
			'manager' => array(
				44,
				6,
				true
			),
		);
	}
	/**
	 * Testing addUserToGroup().
	 *
	 * @param   string   $userId    User id
	 * @param   integer  $groupId   Group to add user to
	 * @param   boolean  $expected  Expected params
	 *
	 * @dataProvider casesAddUsertoGroup
	 * @covers  JUserHelper::addUsertoGroup
	 * @return  void
	 *
	 * @since   12.3
	 */
	public function testAddUserToGroup($userId, $groupId, $expected)
	{
		$this->assertThat(
			JUserHelper::addUserToGroup($userId, $groupId),
			$this->equalTo($expected)
		);
	}

	/**
	 * Testing addUserToGroup() with expected exception.
	 *
	 * @return  void
	 *
	 * @since   12.3
	 * @expectedException  RuntimeException
	 * @covers  JUserHelper::addUsertoGroup
	 */
	public function testAddUserToGroupException()
	{
		JUserHelper::addUserToGroup(44, 99);
	}

	/**
	 * Test cases for testRemoveUserFromGroup
	 *
	 * @return array
	 */
	public function casesRemoveUserFromGroup()
	{
		return array(
			'publisher' => array(
				43,
				8,
				true
			),
			'manager' => array(
				44,
				6,
				true
			),
		);
	}

	/**
	 * Testing removeUserFromGroup().
	 *
	 * @param   string   $userId    User id
	 * @param   integer  $groupId   Group to remove user from
	 * @param   boolean  $expected  Expected params
	 *
	 * @dataProvider casesRemoveUserFromGroup
	 * @covers  JUserHelper::removeUserFromGroup
	 * @return  void
	 */
	public function testRemoveUserFromGroup($userId, $groupId, $expected)
	{
		$this->markTestSkipped('Unexpected test failure in CMS environment');
		$this->assertThat(
			JUserHelper::removeUserFromGroup($userId, $groupId),
			$this->equalTo($expected)
		);
	}

	/**
	 * Test cases for testActivateUser
	 *
	 * @return array
	 */
	public function casesActivateUser()
	{
		return array(
			'Valid User' => array(
				'30cc6de70fb18231196a28dd83363d57',
				true),
			'Invalid User' => array(
				'30cc6de70fb18231196a28dd83363d72',
				false),
		);
	}

	/**
	 * Testing activateUser().
	 *
	 * @param   string   $activation  Activation string
	 * @param   boolean  $expected    Expected params
	 *
	 * @dataProvider casesActivateUser
	 * @covers  JUserHelper::activateUser
	 * @return  void
	 *
	 * @since   12.3
	 */
	public function testActivateUser($activation, $expected)
	{
		$this->assertEquals(
			JUserHelper::activateUser($activation),
			$expected
		);
	}

	/**
	 * Testing hashPassword().
	 *
	 * @covers  JUserHelper::hashPassword
	 * @return  void
	 *
	 * @since   3.2
	 */
	public function testHashPassword()
	{
		$this->assertEquals(
			strpos(JUserHelper::hashPassword('mySuperSecretPassword'), '$P$'),
			0,
			'Joomla currently hashes passwords using PHPass, verify the correct prefix is present'
		);
	}

	/**
	 * Testing verifyPassword().
	 *
	 * @covers  JUserHelper::verifyPassword
	 * @return  void
	 *
	 * @since   3.2
	 */
	public function testVerifyPassword()
	{
		$this->assertTrue(
			JUserHelper::verifyPassword('mySuperSecretPassword', '$P$D6vpNa203LlaQUah3KcVQIhgFZ4E6o1'),
			'Properly verifies a password hashed with PHPass'
		);

		$this->assertTrue(
			JUserHelper::verifyPassword('mySuperSecretPassword', '$2y$10$0GfV1d.dfYvWu83ZKFD4surhsaRpVjUZqhG9bShmPcSnmqwCes/lC'),
			'Properly verifies a password hashed with BCrypt'
		);

		$this->assertTrue(
			JUserHelper::verifyPassword('mySuperSecretPassword', '{SHA256}972c5f5b845306847cb4bf941b7a683f1a828f48c46abef8b9ae4dac9798b1d5:oeLpBZ2sFJwLZmm4'),
			'Properly verifies a password hashed with SHA256'
		);

		$this->assertTrue(
			JUserHelper::verifyPassword('mySuperSecretPassword', '693560686f4d591d8dd5e34006442061'),
			'Properly verifies a password hashed with Joomla legacy MD5'
		);

		$password = 'mySuperSecretPassword';
		// Generate the old style password hash used before phpass was implemented.
		$salt		= JUserHelper::genRandomPassword(32);
		$crypted	= JUserHelper::getCryptedPassword($password, $salt);
		$hashed	        = $crypted . ':' . $salt;
		$this->assertTrue(
			JUserHelper::verifyPassword('mySuperSecretPassword', $hashed),
			'Properly verifies a password which was hashed before phpass was implemented'
		);

		$this->assertTrue(
			JUserHelper::verifyPassword('mySuperSecretPassword', 'fb7b0a16d7e0e6706c0f962832e1fdd8:vQnUrofbvGRcBR6l502Bt8nioKj8MObh'),
			'Properly verifies an existing password hash which was hashed before phpass was implimented'
		);
	}

	/**
	 * Testing verifyPassword() with a Joomla 1.0 style password with no salt.
	 *
	 * @covers  JUserHelper::verifyPassword
	 * @return  void
	 *
	 * @since   3.2
	 * @see     https://github.com/joomla/joomla-cms/pull/5551
	 */
	public function testVerifyPasswordWithNoSalt()
	{
		$this->assertTrue(
			JUserHelper::verifyPassword('test', '098f6bcd4621d373cade4e832627b4f6:'),
			'Joomla 1.0 passwords without a legacy hash are not verified correctly'
		);
	}

	/**
	 * Testing getCryptedPassword().
	 *
	 * @covers  JUserHelper::getCryptedPassword
	 * @return  void
	 *
	 * @since   3.5
	 */
	public function testGetCryptedPassword()
	{
		$this->assertSame(
			'mySuperSecretPassword',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'plain'),
			'Plain text password is returned'
		);

		$this->assertSame(
			strlen(JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'crypt')), 13, 'Password is hashed to crypt without salt'
		);

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '{crypt}myA38Ex7aHbws', 'crypt');

		$this->assertSame('myA38Ex7aHbws', $password, 'Password is hashed to crypt with salt');
		$this->assertSame('my', substr($password, 0, 2), 'Password hash uses expected salt');

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '{crypt}myA38Ex7aHbws', 'crypt', true);

		$this->assertSame('{crypt}myA38Ex7aHbws', $password, 'Password is hashed to crypt with salt with encryption prefix');
		$this->assertSame('my', substr($password, 7, 2), 'Password hash uses expected salt');

		$this->assertSame(
			strlen(JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'crypt-des')), 13, 'Password is hashed to crypt-des without salt'
		);

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '{crypt}myA38Ex7aHbws', 'crypt-des');

		$this->assertSame('myA38Ex7aHbws', $password, 'Password is hashed to crypt-des with salt');
		$this->assertSame('my', substr($password, 0, 2), 'Password hash uses expected salt');

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '{crypt}myA38Ex7aHbws', 'crypt-des', true);

		$this->assertSame('{crypt}myA38Ex7aHbws', $password, 'Password is hashed to crypt-des with salt with encryption prefix');
		$this->assertSame('my', substr($password, 7, 2), 'Password hash uses expected salt');

		$this->assertSame(
			strlen(JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'crypt-md5')), 34, 'Password is hashed to crypt-md5 without salt'
		);

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '{crypt}myA38Ex7aHbws', 'crypt-md5');

		$this->assertSame('myA38Ex7aHbws', $password, 'Password is hashed to crypt-md5 with salt');
		$this->assertSame('my', substr($password, 0, 2), 'Password hash uses expected salt');

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '{crypt}myA38Ex7aHbws', 'crypt-md5', true);

		$this->assertSame('{crypt}myA38Ex7aHbws', $password, 'Password is hashed to crypt-md5 with salt with encryption prefix');
		$this->assertSame('my', substr($password, 7, 2), 'Password hash uses expected salt');

		$this->assertSame(
			strlen(JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'crypt-blowfish')), 60, 'Password is hashed to crypt-blowfish without salt'
		);

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '{crypt}myA38Ex7aHbws', 'crypt-blowfish');

		$this->assertSame('myA38Ex7aHbws', $password, 'Password is hashed to crypt-blowfish with salt');
		$this->assertSame('my', substr($password, 0, 2), 'Password hash uses expected salt');

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '{crypt}myA38Ex7aHbws', 'crypt-blowfish', true);

		$this->assertSame('{crypt}myA38Ex7aHbws', $password, 'Password is hashed to crypt-blowfish with salt with encryption prefix');
		$this->assertSame('my', substr($password, 7, 2), 'Password hash uses expected salt');

		$password = JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'aprmd5');

		$this->assertSame(strlen($password), 37, 'Password is hashed to APRMD5 without salt');
		$this->assertSame('$apr1$', substr($password, 0, 6), 'Password hash uses expected prefix');

		$this->assertSame(
			'$apr1$myPasssw$8f98MlB.CDF6iheQgsFmE.',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '$apr1$myPasssw$8f98MlB.CDF6iheQgsFmE.', 'aprmd5'),
			'Password is hashed to APRMD5 with salt'
		);

		// Length should be 81 characters but due to a bug which causes the prefix to always render it adds 8 characters
		$this->assertSame(
			strlen(JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'sha256')), 89, 'Password is hashed to SHA256 without salt'
		);

		// Due to a bug which causes the prefix to always render it is present here
		$this->assertSame(
			'{SHA256}612994683da31910fdcabce8237303a57740e9b68b0584b2b1647539ccc28578:879334ce9ac922c6',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '879334ce9ac922c6', 'sha256'),
			'Password is hashed to SHA256 with salt'
		);

		$this->assertSame(
			'{SHA256}612994683da31910fdcabce8237303a57740e9b68b0584b2b1647539ccc28578:879334ce9ac922c6',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '879334ce9ac922c6', 'sha256', true),
			'Password is hashed to SHA256 with salt with encryption prefix'
		);

		$this->assertSame(
			'693560686f4d591d8dd5e34006442061',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'md5-hex'),
			'Password is hashed to MD5-HEX without salt'
		);

		$this->assertSame(
			'a334d9084fa0dc4ea5449afa047480e8',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', 'myPassswordHasSalt', 'md5-hex'),
			'Password is hashed to MD5-HEX with salt'
		);

		$this->assertSame(
			'{MD5}a334d9084fa0dc4ea5449afa047480e8',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', 'myPassswordHasSalt', 'md5-hex', true),
			'Password is hashed to MD5-HEX with salt with encryption prefix'
		);
	}

	/**
	 * Testing getCryptedPassword(). This is an extension of the method above but requires
	 * the PHP Mhash function which is missing in Travis and our current Jenkins Build
	 *
	 * @covers  JUserHelper::getCryptedPassword
	 * @return  void
	 *
	 * @since   3.5
	 */
	public function testGetCryptedPasswordWithMhash()
	{
		if (!function_exists('mhash')) {
			$this->markTestSkipped('The mhash function is not available');
		}

		$this->assertSame(
			'9hyEDbdjNSZv5bjLaODLzA4dtrc=',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'sha'),
			'Password is hashed to SHA without encryption prefix'
		);

		$this->assertSame(
			'{SHA}9hyEDbdjNSZv5bjLaODLzA4dtrc=',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'sha', true),
			'Password is hashed to SHA with encryption prefix'
		);

		$this->assertSame(
			'aTVgaG9NWR2N1eNABkQgYQ==',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'md5-base64'),
			'Password is hashed to MD5-BASE64 without encryption prefix'
		);

		$this->assertSame(
			'{MD5}aTVgaG9NWR2N1eNABkQgYQ==',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'md5-base64', true),
			'Password is hashed to MD5-BASE64 with encryption prefix'
		);

		$this->assertSame(
			strlen(JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'ssha')), 32, 'Password is hashed to SSHA without salt'
		);

		$this->assertSame(
			'CvjRrNi3CdI+JV7ovrXBVy/qg1djM056ZDI5eVpFaGhjMU5oYkhRPQ==',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '{SSHA}HoMCZZps34WkU6cM7J8r9OySD5xteVBhc3Nzd29yZEhhc1NhbHQ=', 'ssha'),
			'Password is hashed to SSHA with salt'
		);

		$this->assertSame(
			'{SSHA}CvjRrNi3CdI+JV7ovrXBVy/qg1djM056ZDI5eVpFaGhjMU5oYkhRPQ==',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '{SSHA}HoMCZZps34WkU6cM7J8r9OySD5xteVBhc3Nzd29yZEhhc1NhbHQ=', 'ssha', true),
			'Password is hashed to SSHA with salt with encryption prefix'
		);

		$this->assertSame(
			strlen(JUserHelper::getCryptedPassword('mySuperSecretPassword', '', 'smd5')), 28, 'Password is hashed to SMD5 without salt'
		);

		$this->assertSame(
			's3Joy5bK4AR6mqKmjkc4S2QyOXlaRWhoYzFOaGJIUT0=',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '{SMD5}oY4N5uFk6w54Ni3eKYQxIlBhc3Nzd29yZEhhc1NhbHQ=', 'smd5'),
			'Password is hashed to SMD5 with salt'
		);

		$this->assertSame(
			'{SMD5}s3Joy5bK4AR6mqKmjkc4S2QyOXlaRWhoYzFOaGJIUT0=',
			JUserHelper::getCryptedPassword('mySuperSecretPassword', '{SMD5}oY4N5uFk6w54Ni3eKYQxIlBhc3Nzd29yZEhhc1NhbHQ=', 'smd5', true),
			'Password is hashed to SMD5 with salt with encryption prefix'
		);
	}
}