<?php

namespace VersionPress\Database;

use SqlParser\Components\SetOperation;
use SqlParser\Parser;
use SqlParser\Statement;
use SqlParser\Statements\DeleteStatement;
use SqlParser\Statements\InsertStatement;
use SqlParser\Statements\UpdateStatement;
use VersionPress\Database\DbSchemaInfo;

class SqlQueryParser {

    /**
     * Parses Sql query called. If not parseable, returns null
     * @param $query string
     * @param $schema DbSchemaInfo
     * @param \wpdb $wpdb
     * @return ParsedQueryData
     */
    public static function parseQuery($query, $schema, $wpdb) {
        $parser = self::getParser($query);
        $primaryStatement = $parser->statements[0];
        if ($primaryStatement instanceof UpdateStatement) {
            return self::parseUpdateQuery($parser, $query, $schema, $wpdb);
        } elseif ($primaryStatement instanceof InsertStatement) {
            return self::parseInsertQuery($parser, $query);
        } elseif ($primaryStatement instanceof DeleteStatement) {
            return self::parseDeleteQuery($parser, $query, $schema, $wpdb);
        }
        return null;

    }

    /**
     * Parses UPDATE query
     *
     * @param $parser Parser
     * @param $schema DbSchemaInfo
     * @param $query string
     * @param \wpdb $wpdb
     */
    private static function parseUpdateQuery($parser, $query, $schema, $wpdb) {
        $statement = $parser->statements[0];
        $table = $statement->tables[0]->table;
        $idColumn = self::getIdColumn($schema, $table);
        $result = new ParsedQueryData('UPDATE');
        $result->originalQuery = $query;
        $result->table = $table;

        $selectSql = self::getSelect($parser, $idColumn);
        $where = self::getSelectWhereClause($parser, $query, $statement);
        if (isset($where)) {
            $selectSql .= " WHERE " . join(' ', $where);
        }
        $result->query = $selectSql;
        $result->ids = $wpdb->get_col($selectSql);
        $result->data = self::getData($statement);
        $result->dirty = self::isQueryDirty($parser);
        $result->where = self::getWhere($where);
        return $result;
    }

    /**
     * Parses INSERT query
     *
     * @param $parser
     * @param $query
     * @return ParsedQueryData
     */
    private static function parseInsertQuery($parser, $query) {
        $primaryStatement = $parser->statements[0];
        $queryType = 'INSERT';
        $table = $primaryStatement->into->dest->table;
        if (count($primaryStatement->options->options) > 0) {
            foreach (array_keys($primaryStatement->options->options) as $key) {
                $queryType .= "_" . $primaryStatement->options->options[$key];
            }
        }
        $result = new ParsedQueryData($queryType);
        $result->data = self::getData($primaryStatement);
        $result->table = $table;
        $result->originalQuery = $query;
        $result->dirty = self::isQueryDirty($parser);
        return $result;
    }


    /**
     * Parses DELETE query
     *
     * @param $parser
     * @param $query
     * @param $schema
     * @param $wpdb
     * @return ParsedQueryData
     */
    private static function parseDeleteQuery($parser, $query, $schema, $wpdb) {
        $statement = $parser->statements[0];
        $table = $statement->from[0]->table;
        $idColumn = self::getIdColumn($schema, $table);
        $result = new ParsedQueryData('DELETE');
        $result->originalQuery = $query;
        $result->table = $table;
        $selectSql = self::getSelect($parser, $idColumn);
        $where = self::getSelectWhereClause($parser, $query, $statement);
        if (isset($where)) {
            $selectSql .= " WHERE " . join(' ', $where);
        }
        $result->query = $selectSql;
        $result->ids = $wpdb->get_col($selectSql);
        $result->dirty = self::isQueryDirty($parser);
        $result->where = self::getWhere($where);
        return $result;
    }

    /**
     * Returns representation of WHERE SQL clauses found in whole query
     *
     * @param $parser
     * @param $sql
     * @param $primaryStatement
     * @return array
     */
    private static function getSelectWhereClause($parser, $sql, $primaryStatement) {
        if ($primaryStatement->where != null) {
            $where = $primaryStatement->where;
            return $where;
        } elseif ($primaryStatement->where == null && strpos($sql, 'WHERE') !== false) {
            if (isset($parser->statements[1])) {
                $secondarySatement = $parser->statements[1];
                if ($secondarySatement->where != null) {
                    $where = $secondarySatement->where;
                    return $where;
                }

            }
        } elseif ($parser->errors != null && $primaryStatement->where == null && strpos($sql, 'WHERE') === false) {
            $where = ['1=1'];
            return $where;
        }
    }

    /**
     * Gets data which needs to be set by UPDATE statement
     *
     * Example
     *
     * [data] => Array
     *     (
     *         [0] => Array
     *               (
     *                   [column] => post_modified
     *                   [value] => NOW()
     *               )
     *     )
     * @param $statement
     * @return array
     */
    private static function getData($statement) {

        if ($statement instanceof UpdateStatement) {
            return array_map(function ($set) {
                return array('column' => $set->column, 'value' => $set->value);
            }, $statement->set);
        } elseif ($statement instanceof InsertStatement) {
            $columns = $statement->into->columns;
            $result = [];
            for ($i = 0; $i < count($statement->values); $i++) {
                $sets = $statement->values[$i];
                $data = [];
                for ($j = 0; $j < count($sets->values); $j++) {
                    array_push($data, array('column' => $columns[$j], 'value' => $sets->values[$j]));
                }
                array_push($result, $data);
            }
            return $result;
        }

    }

    /**
     * If query contains some markers (unknown functions) or more than one statement.
     * @param $parser
     * @return bool
     */
    private static function isQueryDirty($parser) {
        if (count($parser->statements) > 1) {
            return true;
        } else {
            $statement = $parser->statements[0];
            if ($statement instanceof UpdateStatement) {
                return self::containsDirtyPatterns($parser->statements[0]->set);
            } elseif ($statement instanceof InsertStatement) {
                if (isset($statement->tables)) {
                    return true;
                }
                $values = [];
                foreach ($statement->values as $dataSet) {
                    $values = array_merge($values, $dataSet->values);
                }
                return self::containsDirtyPatterns($values);
            }
        }
    }

    /**
     * Returns Sql query WHERE fragments as array
     *
     * Example
     *
     * [where] => Array
     *     (
     *          [0] => option_name LIKE '%_'
     *          [1] => option_value LIKE '%s'
     *     )
     * Filters operators  where fragments
     * @param $whereConditions
     * @return mixed
     */
    private static function getWhere($whereConditions) {
        $where = array_filter($whereConditions, function ($w) {
            if (is_object($w)) {
                return !$w->isOperator;
            } else {
                return true;
            }
        });

        $where = array_map(function ($w) {
            if (is_object($w)) {
                return $w->expr;
            } else {
                return $w;
            }
        }, $where);

        return array_values($where);
    }

    /**
     * @param array $sets
     * @return boolean
     */
    private static function containsDirtyPatterns($sets) {
        $dirtyPatterns = array(
            "/DATE_ADD[ ]?\\(.*/", //https://regex101.com/r/tS2iC6/1
            "/NOW\\(\\)/",
            "/NOW/"
        );
        foreach ($sets as $set) {
            foreach ($dirtyPatterns as $pattern) {
                if (preg_match($pattern, $set)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates Select SQL query from query in Parser
     *
     *
     *
     * @param $parser
     * @param $idColumn
     * @return string
     */
    private static function getSelect($parser, $idColumn) {
        $query = "SELECT $idColumn FROM ";
        $statement = $parser->statements[0];
        if (isset($statement->from)) {
            $from = $statement->from[0];
        } else {
            $from = $statement->tables[0];
        }
        $query .= $from->expr;
        if ($from->alias != null) {
            $query .= ' AS ' . $from->alias;
        }
        if (isset($statement->join)) {
            $join = $statement->join[0];
            $query .= ' JOIN ' . $join->expr->expr;
            if ($join->expr->alias != null) {
                $query .= ' AS ' . $join->expr->alias;
            }
            if ($join->on != null) {
                $query .= ' ON ' . $join->on[0]->expr;
            }
        }
        return $query;
    }

    /**
     * If query contains some suspicious patten, we need to transform it and than create Parser for further use.
     *
     * @param $query
     * @return Parser
     */
    private static function getParser($query) {
        $containsUsingPattern = "/(.*)(USING ?\\(([^\\)]+)\\))(.*)/"; //https://regex101.com/r/vF6dI5/1
        $isTransformed = false;
        $parser = new Parser($query);
        $transformedQuery = '';
        $primaryStatement = $parser->statements[0];
        if ($primaryStatement instanceof DeleteStatement) {
            $transformedPart = 'ON ';
            if (preg_match_all($containsUsingPattern, $query, $matches)) {
                $usingColumn = str_replace('`', '', $matches[3][0]);
                $isTransformed = true;
                $from = $primaryStatement->from[0];
                if ($from->alias != null) {
                    $transformedPart .= $from->alias . '.' . $usingColumn;
                } else {
                    $transformedPart .= $from->table . '.' . $usingColumn;
                }
                $transformedPart .= '=';
                $join = $primaryStatement->join[0];

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
     * @param $schema
     * @param $table
     * @return mixed
     */
    private static function getIdColumn($schema, $table) {
        return $schema->getEntityInfoByPrefixedTableName($table)->idColumnName;
    }


}