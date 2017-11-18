package com.alibaba.druid.sql.dialect.mysql.visitor;

import com.alibaba.druid.sql.dialect.mysql.ast.MySqlKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlBinaryExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlBooleanExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.CobarShowStatus;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlBinlogStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCommitStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateUserStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateUserStatement.UserSpecification;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDescribeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDropTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDropUser;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExecuteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlKillStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLoadDataInFileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLoadXmlStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByKey;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPrepareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlReplicateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlResetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRollbackStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectGroupBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowColumnsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowDatabasesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowWarningsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlStartTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.visitor.SQLASTVisitorAdapter;

public class MySqlASTVisitorAdapter extends SQLASTVisitorAdapter implements
		MySqlASTVisitor {

	@Override
	public boolean visit(MySqlBooleanExpr x) {
		return true;
	}

	@Override
	public void endVisit(MySqlBooleanExpr x) {

	}

	@Override
	public boolean visit(Limit x) {
		return true;
	}

	@Override
	public void endVisit(Limit x) {

	}

	@Override
	public boolean visit(MySqlTableIndex x) {
		return true;
	}

	@Override
	public void endVisit(MySqlTableIndex x) {

	}

	@Override
	public boolean visit(MySqlKey x) {
		return true;
	}

	@Override
	public void endVisit(MySqlKey x) {

	}

	@Override
	public boolean visit(MySqlPrimaryKey x) {

		return true;
	}

	@Override
	public void endVisit(MySqlPrimaryKey x) {

	}

	@Override
	public void endVisit(MySqlIntervalExpr x) {

	}

	@Override
	public boolean visit(MySqlIntervalExpr x) {

		return true;
	}

	@Override
	public void endVisit(MySqlExtractExpr x) {

	}

	@Override
	public boolean visit(MySqlExtractExpr x) {

		return true;
	}

	@Override
	public void endVisit(MySqlMatchAgainstExpr x) {

	}

	@Override
	public boolean visit(MySqlMatchAgainstExpr x) {

		return true;
	}

	@Override
	public void endVisit(MySqlBinaryExpr x) {

	}

	@Override
	public boolean visit(MySqlBinaryExpr x) {

		return true;
	}

	@Override
	public void endVisit(MySqlPrepareStatement x) {

	}

	@Override
	public boolean visit(MySqlPrepareStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlExecuteStatement x) {

	}

	@Override
	public boolean visit(MySqlExecuteStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlDeleteStatement x) {

	}

	@Override
	public boolean visit(MySqlDeleteStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlInsertStatement x) {

	}

	@Override
	public boolean visit(MySqlInsertStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlLoadDataInFileStatement x) {

	}

	@Override
	public boolean visit(MySqlLoadDataInFileStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlLoadXmlStatement x) {

	}

	@Override
	public boolean visit(MySqlLoadXmlStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlReplicateStatement x) {

	}

	@Override
	public boolean visit(MySqlReplicateStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlSelectGroupBy x) {

	}

	@Override
	public boolean visit(MySqlSelectGroupBy x) {

		return true;
	}

	@Override
	public void endVisit(MySqlStartTransactionStatement x) {

	}

	@Override
	public boolean visit(MySqlStartTransactionStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlCommitStatement x) {

	}

	@Override
	public boolean visit(MySqlCommitStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlRollbackStatement x) {

	}

	@Override
	public boolean visit(MySqlRollbackStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlShowColumnsStatement x) {

	}

	@Override
	public boolean visit(MySqlShowColumnsStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlShowTablesStatement x) {

	}

	@Override
	public boolean visit(MySqlShowTablesStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlShowDatabasesStatement x) {

	}

	@Override
	public boolean visit(MySqlShowDatabasesStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlShowWarningsStatement x) {

	}

	@Override
	public boolean visit(MySqlShowWarningsStatement x) {

		return true;
	}

	@Override
	public void endVisit(MySqlShowStatusStatement x) {

	}

	@Override
	public boolean visit(MySqlShowStatusStatement x) {

		return true;
	}

	@Override
	public void endVisit(CobarShowStatus x) {

	}

	@Override
	public boolean visit(CobarShowStatus x) {
		return true;
	}

	@Override
	public void endVisit(MySqlKillStatement x) {

	}

	@Override
	public boolean visit(MySqlKillStatement x) {
		return true;
	}

	@Override
	public void endVisit(MySqlBinlogStatement x) {

	}

	@Override
	public boolean visit(MySqlBinlogStatement x) {
		return true;
	}

	@Override
	public void endVisit(MySqlResetStatement x) {

	}

	@Override
	public boolean visit(MySqlResetStatement x) {
		return true;
	}

    @Override
    public void endVisit(MySqlCreateUserStatement x) {

    }

    @Override
    public boolean visit(MySqlCreateUserStatement x) {
        return true;
    }

    @Override
    public void endVisit(UserSpecification x) {

    }

    @Override
    public boolean visit(UserSpecification x) {
        return true;
    }

    @Override
    public void endVisit(MySqlDropUser x) {

    }

    @Override
    public boolean visit(MySqlDropUser x) {
        return true;
    }

    @Override
    public void endVisit(MySqlDropTableStatement x) {

    }

    @Override
    public boolean visit(MySqlDropTableStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlPartitionByKey x) {

    }

    @Override
    public boolean visit(MySqlPartitionByKey x) {
        return true;
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock x) {

    }

    @Override
    public boolean visit(MySqlOutFileExpr x) {
        return true;
    }

    @Override
    public void endVisit(MySqlOutFileExpr x) {

    }

    @Override
    public boolean visit(MySqlDescribeStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlDescribeStatement x) {

    }

	@Override
	public boolean visit(MySqlUpdateStatement x) {
		return true;
	}

	@Override
	public void endVisit(MySqlUpdateStatement x) {

	}

}