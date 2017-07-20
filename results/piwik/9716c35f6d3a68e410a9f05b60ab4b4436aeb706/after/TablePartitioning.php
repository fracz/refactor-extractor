<?php

abstract class Piwik_TablePartitioning
{
	protected $tableName = null;
	protected $generatedTableName = null;
	protected $timestamp = null;

	public function __construct( $tableName )
	{
		$this->tableName = $tableName;
	}

	abstract protected function generateTableName() ;


	public function setDate( $timestamp )
	{
		$this->timestamp = $timestamp;
		$this->generatedTableName = null;
	}

	public function getTableName()
	{
		// table name already processed
		if(!is_null($this->generatedTableName))
		{
			return $this->generatedTableName;
		}

		if(is_null($this->timestamp))
		{
			throw new Exception("You have to specify a timestamp for a Table Partitioning by date.");
		}

		// generate table name
		$this->generatedTableName = $this->generateTableName();

		// we make sure the table already exists
		$this->checkTableExists();
	}

	protected function checkTableExists()
	{
		$tablesAlreadyInstalled = Piwik::getTablesInstalled();

		if(!in_array($this->generatedTableName, $tablesAlreadyInstalled))
		{
			$db = Zend_Registry::get('db');
			$sql = Piwik::getTableCreateSql($this->tableName);

			$config = Zend_Registry::get('config');
			$prefixTables = $config->database->tables_prefix;
			$sql = str_replace( $prefixTables . $this->tableName, $this->generatedTableName, $sql);

			$db->query( $sql );
		}
	}

	protected function __toString()
	{
		return $this->getTableName();
	}
}

class Piwik_TablePartitioning_Monthly extends Piwik_TablePartitioning
{
	public function __construct( $tableName )
	{
		parent::__construct($tableName);
	}
	protected function generateTableName()
	{
		$config = Zend_Registry::get('config');
		$prefixTables = $config->database->tables_prefix;

		$date = date("Y_m", $this->timestamp);

		return $prefixTables . $this->tableName . "_" . $date;
	}

}
class Piwik_TablePartitioning_Daily extends Piwik_TablePartitioning
{
	public function __construct( $tableName )
	{
		parent::__construct($tableName);
	}
	protected function generateTableName()
	{
		$config = Zend_Registry::get('config');
		$prefixTables = $config->database->tables_prefix;

		$date = date("Y_m_d", $this->timestamp);

		return $prefixTables . $this->tableName . "_" . $date;
	}

}
?>