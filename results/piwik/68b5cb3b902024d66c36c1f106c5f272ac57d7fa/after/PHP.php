<?php
class Piwik_DataTable_Renderer_PHP extends Piwik_DataTable_Renderer
{
	protected $serialize;
	function __construct($table = null, $serialize = true)
	{
		parent::__construct($table);
		$this->serialize = $serialize;
	}

	function render()
	{
		if($this->table instanceof Piwik_DataTable_Simple)
		{
			$array = $this->renderSimpleTable($this->table);
		}
		else
		{
			$array = $this->renderTable($this->table);
		}

		if($this->serialize)
		{
			$array = serialize($array);
		}

		return $array;
	}

	function renderTable($table)
	{
		$array = array();

		foreach($table->getRows() as $row)
		{
			$newRow = array(
				'columns' => $row->getColumns(),
				'details' => $row->getDetails(),
				'idsubdatatable' => $row->getIdSubDataTable()
				);
			$array[] = $newRow;
		}
		return $array;
	}

	function renderSimpleTable($table)
	{
		$array = array();

		foreach($table->getRows() as $row)
		{
			$array[$row->getColumn('label')] = $row->getColumn('value');
		}
		return $array;
	}
}