<?php
/**
*
* @package testing
* @version $Id$
* @copyright (c) 2009 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

require_once 'test_framework/framework.php';
require_once '../phpBB/includes/request/deactivated_super_global.php';

class phpbb_deactivated_super_global_test extends phpbb_test_case
{
	/**
	* Checks that on write access the correct error is thrown
	*/
	public function test_write_triggers_error()
	{
		$this->setExpectedTriggerError(E_USER_ERROR);
		$obj = new phpbb_deactivated_super_global($this->getMock('phpbb_request_interface'), 'obj', phpbb_request_interface::POST);
		$obj->offsetSet(0, 0);
	}
}