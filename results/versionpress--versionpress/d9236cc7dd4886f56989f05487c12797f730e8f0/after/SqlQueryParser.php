<?php

namespace VersionPress\Database;

use SqlParser\Components\SetOperation;
use SqlParser\Parser;
use SqlParser\Statement;
use SqlParser\Statements\DeleteStatement;
use SqlParser\Statements\InsertStatement;
use SqlParser\Statements\UpdateStatement;

class SqlQueryParser {

    /**
     * @var DbSchemaInfo
     */
    private $schema;

    /**
     * @var \wpdb
     */
    private $wpdb;

    /**
     * SqlQueryParser constructor.
     * @param DbSchemaInfo $schema
     * @param \wpdb $wpdb
     */
    public function __construct($schema, $wpdb) {
        $this->schema = $schema;
        $this->wpdb = $wpdb;
    }

    /**
     * Parses Sql query called. If not parseable, returns null
     * @param string $query
     * @return ParsedQueryData
     */
    public function parseQuery($query) {
        $parser = self::getParser($query);
        $sqlStatement = $parser->statements[0];
        if ($sqlStatement instanceof UpdateStatement) {
            return self::parseUpdateQuery($parser, $query, $this->schema, $this->wpdb);
        } elseif ($sqlStatement instanceof InsertStatement) {
            return self::parseInsertQuery($parser, $query, $this->schema);
        } elseif ($sqlStatement instanceof DeleteStatement) {
            return $this->parseDeleteQuery($parser, $query, $this->schema, $this->wpdb);
        }
        return null;

    }

    /**
     * Parses UPDATE query
     *
     * @param SqlParser\Parser $parser
     * @param DbSchemaInfo $schema
     * @param string $query
     * @param \wpdb $wpdb
     * @return ParsedQueryData
     */
    private function parseUpdateQuery($parser, $query, $schema, $wpdb) {
        $sqlStatement = $parser->statements[0];
        $table = $sqlStatement->tables[0]->table;
        $idColumn = $this->resolveIdColumn($schema, $table);
        if ($idColumn == null) {
            return null;
        }
        $parsedQueryData = new ParsedQueryData(ParsedQueryData::UPDATE_QUERY);
        $parsedQueryData->table = $table;
        $parsedQueryData->idColumnName = $idColumn;
        $parsedQueryData->entityName = $this->resolveEntityName($schema, $table);
        $selectSql = $this->getSelectQuery($parser, $idColumn);
        $where = $this->getWhereFragments($parser, $query, $sqlStatement);
        if (isset($where)) {
            $selectSql .= " WHERE " . join(' ', $where);
        }
        $parsedQueryData->sqlQuery = $selectSql;
        $parsedQueryData->ids = $wpdb->get_col($selectSql);
        $parsedQueryData->data = $this->getColumnDataToSet($sqlStatement);
        return $parsedQueryData;
    }

    /**
     * Parses INSERT query
     *
     * @param SqlParser\Parser $parser
     * @param string $query
     * @return ParsedQueryData
     */
    private function parseInsertQuery($parser, $query, $schema) {
        $sqlStatement = $parser->statements[0];
        $queryType = ParsedQueryData::INSERT_QUERY;
        $table = $sqlStatement->into->dest->table;
        $idColumn = $this->resolveIdColumn($schema, $table);
        if ($idColumn == null) {
            return null;
        }
        if (count($sqlStatement->options->options) > 0) {
            foreach (array_keys($sqlStatement->options->options) as $key) {
                $queryType .= "_" . $sqlStatement->options->options[$key];
            }
            if ($queryType == ParsedQueryData::INSERT_IGNORE_QUERY) {
                return null;
            }
        }
        if (strpos($query, 'ON DUPLICATE KEY UPDATE') !== false) {
            $queryType .= "_UPDATE";
        }
        $parsedQueryData = new ParsedQueryData($queryType);
        $parsedQueryData->data = $this->getColumnDataToSet($sqlStatement);
        $parsedQueryData->table = $table;
        $parsedQueryData->idColumnName = $idColumn;
        $parsedQueryData->entityName = $this->resolveEntityName($schema, $table);
        $selectSql = $this->getSelectQuery($parser, $idColumn);
        $parsedQueryData->sqlQuery = $selectSql;
        return $parsedQueryData;
    }

    /**
     * Parses DELETE query
     *
     * @param SqlParser\Parser $parser
     * @param string $query
     * @param DbSchemaInfo $schema
     * @param $wpdb \wpdb
     * @return ParsedQueryData
     */
    private function parseDeleteQuery($parser, $query, $schema, $wpdb) {
        $sqlStatement = $parser->statements[0];
        $table = $sqlStatement->from[0]->table;
        $idColumn = $this->resolveIdColumn($schema, $table);
        if ($idColumn == null) {
            return null;
        }
        $parsedQueryData = new ParsedQueryData(ParsedQueryData::DELETE_QUERY);
        $parsedQueryData->idColumnName = $idColumn;
        $parsedQueryData->entityName = $this->resolveEntityName($schema, $table);
        $parsedQueryData->table = $table;
        $selectSql = $this->getSelectQuery($parser, $idColumn);
        $where = $this->getWhereFragments($parser, $query, $sqlStatement);
        if (isset($where)) {
            $selectSql .= " WHERE " . join(' ', $where);
        }
        $parsedQueryData->sqlQuery = $selectSql;
        $parsedQueryData->ids = $wpdb->get_col($selectSql);

        return $parsedQueryData;
    }

    /**
     * Returns representation of WHERE SQL clauses found in whole query
     *
     * @param SqlParser\Parser $parser
     * @param string $sqlQuery
     * @param SqlParser\Statements\Statement $primarySqlStatement
     * @return array
     */
    private function getWhereFragments($parser, $sqlQuery, $primarySqlStatement) {
        if ($primarySqlStatement->where != null) {
            $whereFragments = $primarySqlStatement->where;
            return $whereFragments;
        } elseif ($primarySqlStatement->where == null && strpos($sqlQuery, 'WHERE') !== false) {
            if (isset($parser->statements[1])) {
                $secondarySqlSatement = $parser->statements[1];
                if ($secondarySqlSatement->where != null) {
                    $whereFragments = $secondarySqlSatement->where;
                    return $whereFragments;
                }

            }
        } elseif ($parser->errors != null && $primarySqlStatement->where == null && strpos($sqlQuery, 'WHERE') === false) {
            $whereFragments = ['1=1'];
            return $whereFragments;
        }
    }

    /**
     * Gets data which needs to be set by UPDATE statement
     *
     * Example
     * [data] =>
     *  [
     *          [ column => post_modified, value => NOW() ],
     *          [ column => another_column, value => 123 ]
     *  ]
     * @param SqlParser\Statements\Statement $sqlStatement
     * @return array
     */
    private function getColumnDataToSet($sqlStatement) {

        if ($sqlStatement instanceof UpdateStatement) {
            $dataSet = [];
            foreach ($sqlStatement->set as $set) {
                if(is_string($set->value)) {
                    $dataSet[str_replace('`', '', $set->column)] = stripslashes($set->value);
                } else {
                    $data[$columns[$i]] = $set->value;
                }

            };
            return $dataSet;
        } elseif ($sqlStatement instanceof InsertStatement) {
            $columns = $sqlStatement->into->columns;
            $dataToSet = [];
            foreach($sqlStatement->values as $sets) {
                $data = [];
                foreach ($sets->values as $i => $value) {
                    if(is_string($value)) {
                        $data[$columns[$i]] = stripslashes($value);
                    } else {
                        $data[$columns[$i]] = $value;
                    }
                }
                array_push($dataToSet, $data);
            }
            return $dataToSet;
        }

    }

    /**
     * Creates Select SQL query from query in Parser
     *
     * @param SqlParser\Parser $parser
     * @param string $idColumn
     * @return string
     */
    private function getSelectQuery($parser, $idColumn) {
        $selectQuery = "SELECT $idColumn FROM ";
        $sqlStatement = $parser->statements[0];
        if ($sqlStatement instanceof InsertStatement) {
            $selectQuery = "SELECT * FROM " . $sqlStatement->into->dest . " WHERE ";
            $dataSet = $this->getColumnDataToSet($sqlStatement);
            $whereConditions = [];
            foreach ($dataSet[0] as $key => $value) {
                array_push($whereConditions, $key . '=\'' . $value . '\'');
            }
            $selectQuery .= join(" AND ", $whereConditions);
            return $selectQuery;
        }
        if (isset($sqlStatement->from)) {
            $from = $sqlStatement->from[0];
        } else {
            $from = $sqlStatement->tables[0];
        }

        $selectQuery .= $from->expr;
        if ($from->alias != null) {
            $selectQuery .= ' AS ' . $from->alias;
        }
        if (isset($sqlStatement->join)) {
            $join = $sqlStatement->join[0];
            $selectQuery .= ' JOIN ' . $join->expr->expr;
            if ($join->expr->alias != null) {
                $selectQuery .= ' AS ' . $join->expr->alias;
            }
            if ($join->on != null) {
                $selectQuery .= ' ON ' . $join->on[0]->expr;
            }
        }
        return $selectQuery;
    }

    /**
     * If query contains some suspicious patten, we need to transform it and than create Parser for further use.
     *
     * @param $query
     * @return SqlParser\Parser
     */
    private function getParser($query) {
        $containsUsingPattern = "/(.*)(USING ?\\(([^\\)]+)\\))(.*)/"; //https://regex101.com/r/vF6dI5/1
        $isTransformed = false;
        $parser = new Parser($query);
        $transformedQuery = '';
        $sqlStatement = $parser->statements[0];
        if ($sqlStatement instanceof DeleteStatement) {
            $transformedPart = 'ON ';
            if (preg_match_all($containsUsingPattern, $query, $matches)) {
                $usingColumn = str_replace('`', '', $matches[3][0]);
                $isTransformed = true;
                $from = $sqlStatement->from[0];
                if ($from->alias != null) {
                    $transformedPart .= $from->alias . '.' . $usingColumn;
                } else {
                    $transformedPart .= $from->table . '.' . $usingColumn;
                }
                $transformedPart .= '=';
                $join = $sqlStatement->join[0];

                if ($join->expr->alias != null) {
                    $transformedPart .= $join->expr->alias . '.' . $usingColumn;
                } else {
                    $transformedPart .= $join->expr->table . '.' . $usingColumn;
                }
                $transformedQuery = preg_replace($containsUsingPattern, '$1' . $transformedPart . '$4', $query);

            }
        }
        if ($isTransformed) {
            return new Parser($transformedQuery);
        }
        return $parser;
    }

    /**
     * Returns ID column for a table
     *
     * @param DbSchemaInfo $schema
     * @param string $table
     * @return mixed
     */
    private function resolveIdColumn($schema, $table) {

        $entity = $schema->getEntityInfoByPrefixedTableName($table);
        return $entity == null ? null : $entity->idColumnName;
    }

    /**
     * Returns entity name for a table
     *
     * @param DbSchemaInfo $schema
     * @param string $table
     * @return mixed
     */
    private function resolveEntityName($schema, $table) {

        $entity = $schema->getEntityInfoByPrefixedTableName($table);
        return $entity == null ? null : $entity->entityName;
    }

}