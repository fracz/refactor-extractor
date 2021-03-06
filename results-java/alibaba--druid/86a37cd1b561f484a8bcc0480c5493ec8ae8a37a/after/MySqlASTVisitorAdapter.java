package com.alibaba.druid.sql.dialect.mysql.visitor;

import com.alibaba.druid.sql.dialect.mysql.ast.MySqlKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlBinaryExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlBooleanExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.CobarShowStatus;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableStatement;
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
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetCharSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetNamesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionIsolationLevelStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowAuthorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowBinLogEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowBinaryLogsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCharacterSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCollationStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowColumnsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowContributorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateDatabaseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateEventStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateFunctionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateProcedureStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTriggerStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateViewStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowDatabasesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEngineStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEnginesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowErrorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowFunctionCodeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowFunctionStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowGrantsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowIndexesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowKeysStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowMasterLogsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowMasterStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowOpenTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowPluginsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowPrivilegesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcedureCodeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcedureStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcessListStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProfileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProfilesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowRelayLogEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowSlaveHostsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowSlaveStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTableStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTriggersStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowVariantsStatement;
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

    @Override
    public boolean visit(MySqlSetTransactionIsolationLevelStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSetTransactionIsolationLevelStatement x) {

    }

    @Override
    public boolean visit(MySqlSetNamesStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSetNamesStatement x) {

    }

    @Override
    public boolean visit(MySqlSetCharSetStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlSetCharSetStatement x) {

    }

    @Override
    public boolean visit(MySqlShowAuthorsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowAuthorsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowBinaryLogsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowBinaryLogsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowMasterLogsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowMasterLogsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCollationStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCollationStatement x) {

    }

    @Override
    public boolean visit(MySqlShowBinLogEventsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowBinLogEventsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCharacterSetStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCharacterSetStatement x) {

    }

    @Override
    public boolean visit(MySqlShowContributorsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowContributorsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCreateDatabaseStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateDatabaseStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCreateEventStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateEventStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCreateFunctionStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateFunctionStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCreateProcedureStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateProcedureStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCreateTableStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateTableStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCreateTriggerStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateTriggerStatement x) {

    }

    @Override
    public boolean visit(MySqlShowCreateViewStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateViewStatement x) {

    }

    @Override
    public boolean visit(MySqlShowEngineStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowEngineStatement x) {

    }

    @Override
    public boolean visit(MySqlShowEnginesStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowEnginesStatement x) {

    }

    @Override
    public boolean visit(MySqlShowErrorsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowErrorsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowEventsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowEventsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowFunctionCodeStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowFunctionCodeStatement x) {

    }

    @Override
    public boolean visit(MySqlShowFunctionStatusStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowFunctionStatusStatement x) {

    }

    @Override
    public boolean visit(MySqlShowGrantsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowGrantsStatement x) {
    }

    @Override
    public boolean visit(MySqlUserName x) {
        return true;
    }

    @Override
    public void endVisit(MySqlUserName x) {

    }

    @Override
    public boolean visit(MySqlShowIndexesStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowIndexesStatement x) {

    }

    @Override
    public boolean visit(MySqlShowKeysStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowKeysStatement x) {

    }

    @Override
    public boolean visit(MySqlShowMasterStatusStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowMasterStatusStatement x) {

    }

    @Override
    public boolean visit(MySqlShowOpenTablesStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowOpenTablesStatement x) {

    }

    @Override
    public boolean visit(MySqlShowPluginsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowPluginsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowPrivilegesStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowPrivilegesStatement x) {

    }

    @Override
    public boolean visit(MySqlShowProcedureCodeStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowProcedureCodeStatement x) {

    }

    @Override
    public boolean visit(MySqlShowProcedureStatusStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowProcedureStatusStatement x) {

    }

    @Override
    public boolean visit(MySqlShowProcessListStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowProcessListStatement x) {

    }

    @Override
    public boolean visit(MySqlShowProfileStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowProfileStatement x) {

    }

    @Override
    public boolean visit(MySqlShowProfilesStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowProfilesStatement x) {

    }

    @Override
    public boolean visit(MySqlShowRelayLogEventsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowRelayLogEventsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowSlaveHostsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowSlaveHostsStatement x) {

    }

    @Override
    public boolean visit(MySqlShowSlaveStatusStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowSlaveStatusStatement x) {

    }

    @Override
    public boolean visit(MySqlShowTableStatusStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowTableStatusStatement x) {

    }

    @Override
    public boolean visit(MySqlShowTriggersStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowTriggersStatement x) {

    }

    @Override
    public boolean visit(MySqlShowVariantsStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlShowVariantsStatement x) {

    }

    @Override
    public boolean visit(MySqlAlterTableStatement x) {
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableStatement x) {

    }
}