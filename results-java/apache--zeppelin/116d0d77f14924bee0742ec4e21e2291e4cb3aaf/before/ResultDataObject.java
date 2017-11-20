package com.nflabs.zeppelin.result;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

public class ResultDataObject extends ResultData{
	List<Object []> rows = new LinkedList<Object [] >();

	public ResultDataObject(ResultSet res) throws ResultDataException {
		super(res);
	}

	@Override
	protected void process(ColumnDef[] columnDef, Object[] row, long n) {
		rows.add(row);
	}

	public List<Object []> getRows(){
		return rows;
	}

	@Override
	protected void beforeProcess() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void afterProcess() {
		// TODO Auto-generated method stub

	}

}