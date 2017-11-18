package com.alibaba.druid.mapping.spi;

import java.util.List;

import com.alibaba.druid.mapping.MappingEngine;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.visitor.ExportParameterVisitor;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;

public interface MappingProvider {

    MappingVisitor createMappingVisitor(MappingEngine engine);

    ExportParameterVisitor createExportParameterVisitor(List<Object> parameters);

    SQLASTOutputVisitor createOutputVisitor(MappingEngine engine, Appendable out);

    SQLSelectQueryBlock explainToSelectSQLObject(MappingEngine engine, String sql);

    SQLDeleteStatement explainToDeleteSQLObject(MappingEngine engine, String sql);

    SQLUpdateStatement explainToUpdateSQLObject(MappingEngine engine, String sql);

    SQLInsertStatement explainToInsertSQLObject(MappingEngine engine, String sql);
}