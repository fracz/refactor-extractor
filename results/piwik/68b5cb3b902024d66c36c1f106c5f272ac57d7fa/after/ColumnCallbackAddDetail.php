<?php

/**
 * Add a new column to the table based on the value resulting
 * from a callback function with the parameter being another column's value
 *
 * For example from the "label" column we want to create a "short label" column
 * that is a shorter text.
 */
class Piwik_DataTable_Filter_ColumnCallbackAddDetail extends Piwik_DataTable_Filter
{
	private $columnToRead;
	private $functionToApply;
	private $detailToAdd;

	public function __construct( $table, $columnToRead, $detailToAdd, $functionToApply )
	{
		parent::__construct($table);
		$this->functionToApply = $functionToApply;
		$this->columnToRead = $columnToRead;
		$this->detailToAdd = $detailToAdd;
		$this->filter();
	}

	protected function filter()
	{
		foreach($this->table->getRows() as $key => $row)
		{
			$oldValue = $row->getColumn($this->columnToRead);
			$newValue = call_user_func( $this->functionToApply, $oldValue);
			$row->addDetail($this->detailToAdd, $newValue);
		}
	}
}
