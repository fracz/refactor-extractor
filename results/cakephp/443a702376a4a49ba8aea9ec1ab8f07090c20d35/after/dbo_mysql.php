<?PHP
//////////////////////////////////////////////////////////////////////////
// + $Id$
// +------------------------------------------------------------------+ //
// + Cake <https://developers.nextco.com/cake/>                       + //
// + Copyright: (c) 2005 Cake Authors/Developers                      + //
// +                                                                  + //
// + Author(s): Michal Tatarynowicz aka Pies <tatarynowicz@gmail.com> + //
// +            Larry E. Masters aka PhpNut <nut@phpnut.com>          + //
// +            Kamil Dzielinski aka Brego <brego.dk@gmail.com>       + //
// +                                                                  + //
// +------------------------------------------------------------------+ //
// + Licensed under The MIT License                                   + //
// + Redistributions of files must retain the above copyright notice. + //
// + You may not use this file except in compliance with the License. + //
// +                                                                  + //
// + You may obtain a copy of the License at:                         + //
// + License page: http://www.opensource.org/licenses/mit-license.php + //
// +------------------------------------------------------------------+ //
//////////////////////////////////////////////////////////////////////////

/**
  * MySQL layer for DBO
  *
  * @filesource
  * @author Cake Authors/Developers
  * @copyright Copyright (c) 2005, Cake Authors/Developers
  * @link https://developers.nextco.com/cake/wiki/Authors Authors/Developers
  * @package cake
  * @subpackage cake.libs
  * @since Cake v 0.2.9
  * @version $Revision$
  * @modifiedby $LastChangedBy$
  * @lastmodified $Date$
  * @license http://www.opensource.org/licenses/mit-license.php The MIT License
  */

/**
  * Include DBO.
  *
  */

uses('dbo');
/**
  * MySQL layer for DBO.
  *
  * @package cake
  * @subpackage cake.libs
  * @since Cake v 0.2.9
  *
  */
class DBO_MySQL extends DBO {

/**
  * Connects to the database using options in the given configuration array.
  *
  * @param array $config Configuration array for connecting
  * @return boolean True if the database could be connected, else false
  */
	function connect ($config) {
		if($config) {
			$this->config = $config;
			$this->_conn = mysql_pconnect($config['host'],$config['login'],$config['password']);
		}
		$this->connected = $this->_conn? true: false;

		if($this->connected)
			Return mysql_select_db($config['database'], $this->_conn);
		else
			die('Could not connect to DB.');
	}

/**
  * Disconnects from database.
  *
  * @return boolean True if the database could be disconnected, else false
  */
	function disconnect () {
		return mysql_close();
	}

/**
  * Executes given SQL statement.
  *
  * @param string $sql SQL statement
  * @return resource MySQL result resource identifier
  */
	function execute ($sql) {
		return mysql_query($sql);
	}

/**
  * Returns a row from given resultset as an array .
  *
  * @param unknown_type $res Resultset
  * @return array The fetched row as an array
  */
	function fetchRow ($res) {
		return mysql_fetch_array($res);
	}

/**
  * Returns an array of tables in the database. If there are no tables, an error is raised and the application exits.
  *
  * @return array Array of tablenames in the database
  */
	function tables () {
		$result = mysql_list_tables($this->config['database']);

		if (!$result) {
			trigger_error(ERROR_NO_TABLE_LIST, E_USER_NOTICE);
			exit;
		}
		else {
			$tables = array();
			while ($line = mysql_fetch_array($result)) {
				$tables[] = $line[0];
			}
			return $tables;
		}
	}

/**
  * Returns an array of the fields in given table name.
  *
  * @param string $table_name Name of database table to inspect
  * @return array Fields in table. Keys are name and type
  */
	function fields ($table_name) {
		$data = $this->all("DESC {$table_name}");
		$fields = false;

		foreach ($data as $item)
			$fields[] = array('name'=>$item['Field'], 'type'=>$item['Type']);

		return $fields;
	}

/**
  * Returns a quoted and escaped string of $data for use in an SQL statement.
  *
  * @param string $data String to be prepared for use in an SQL statement
  * @return string Quoted and escaped
  */
	function prepareValue ($data) {
		return "'".mysql_real_escape_string($data)."'";
	}

/**
  * Returns a formatted error message from previous database operation.
  *
  * @return string Error message with error number
  */
	function lastError () {
		return mysql_errno()? mysql_errno().': '.mysql_error(): null;
	}

/**
  * Returns number of affected rows in previous database operation. If no previous operation exists, this returns false.
  *
  * @return int Number of affected rows
  */
	function lastAffected () {
		return $this->_result? mysql_affected_rows(): false;
	}

/**
  * Returns number of rows in previous resultset. If no previous resultset exists,
  * this returns false.
  *
  * @return int Number of rows
  */
	function lastNumRows () {
		return $this->_result? @mysql_num_rows($this->_result): false;
	}

/**
  * Returns the ID generated from the previous INSERT operation.
  *
  * @param string $table Name of the database table
  * @return int
  */
	function lastInsertId() {
		return mysql_insert_id();
	}

}

?>