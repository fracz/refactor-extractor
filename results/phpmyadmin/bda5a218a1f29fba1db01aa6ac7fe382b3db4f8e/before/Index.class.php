<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * holds the datasbe index class
 *
 * @version $Id$
 */

/**
 * @since phpMyAdmin 3.0.0
 *
 */
class PMA_Index
{
    /**
     * Class-wide storage container for indexes (caching, singleton)
     *
     * @var array
     */
    protected static $_registry = array();

    /**
     * @var string The name of the database
     */
    protected $_database    = '';

    /**
     * @var string The name of the table
     */
    protected $_table       = '';

    /**
     * @var string The name of the index
     */
    protected $_name        = '';

    /**
     * Columns in index
     *
     * @var array
     */
    protected $_columns     = array();

    /**
     * The index method used (BTREE, FULLTEXT, HASH, RTREE).
     *
     * @var string
     */
    protected $_type        = '';

    /**
     * Various remarks.
     *
     * @var string
     */
    protected $_remarks     = '';

    /**
     * Any comment provided for the index with a COMMENT attribute when the
     * index was created.
     *
     * @var string
     */
    protected $_comment     = '';

    /**
     * @var integer 0 if the index cannot contain duplicates, 1 if it can.
     */
    protected $_non_unique  = 0;

    /**
     * Indicates how the key is packed. NULL if it is not.
     *
     * @var string
     */
    protected $_packed      = null;

    /**
     * Constructor
     *
     * @uses    $this->set()
     * @param   array $params
     */
    public function __construct($params = array())
    {
        $this->set($params);
    }

    /**
     * returns an array with all indexes from the given table
     *
     * @uses    PMA_Index::_loadIndexes()
     * @uses    PMA_Index::$_registry
     * @param   string $table
     * @param   string $database
     * @return  array
     */
    static public function getFromTable($table, $database)
    {
        PMA_Index::_loadIndexes($table, $database);

        if (isset(PMA_Index::$_registry[$database][$table])) {
            return PMA_Index::$_registry[$database][$table];
        } else {
            return array();
        }
    }

    /**
     * return primary if set, false otherwise
     *
     * @uses    PMA_Index::_loadIndexes()
     * @uses    PMA_Index::$_registry
     * @param   string $table
     * @param   string $database
     * @return  mixed primary index or false if no one exists
     */
    static public function getPrimary($table, $database)
    {
        PMA_Index::_loadIndexes($table, $database);

        if (isset(PMA_Index::$_registry[$database][$table]['PRIMARY'])) {
            return PMA_Index::$_registry[$database][$table]['PRIMARY'];
        } else {
            return false;
        }
    }

    /**
     * Load index data for table
     *
     * @uses    PMA_Index::$_registry
     * @uses    PMA_DBI_fetch_result()
     * @uses    PMA_backquote()
     * @uses    PMA_Index
     * @uses    PMA_Index->addColumn()
     * @param   string $table
     * @param   string $database
     * @return  boolean
     */
    static protected function _loadIndexes($table, $database)
    {
        if (isset(PMA_Index::$_registry[$database][$table])) {
            return true;
        }

        $_raw_indexes = PMA_DBI_fetch_result('SHOW INDEX FROM ' . PMA_backquote($database) . '.' . PMA_backquote($table));
        foreach ($_raw_indexes as $_each_index) {
            if (! isset(PMA_Index::$_registry[$database][$table][$_each_index['Key_name']])) {
                $key = new PMA_Index($_each_index);
                PMA_Index::$_registry[$database][$table][$_each_index['Key_name']] = $key;
            } else {
                $key = PMA_Index::$_registry[$database][$table][$_each_index['Key_name']];
            }

            $key->addColumn($_each_index);
        }

        return true;
    }

    /**
     * Add column to index
     *
     * @uses    $this->_columns
     * @uses    PMA_Index_Column
     * @param   array $params column params
     */
    public function addColumn($params)
    {
        $this->_columns[$params['Column_name']] = new PMA_Index_Column($params);
    }

    /**
     * Returns true if $column indexed in this index
     *
     * @uses    $this->_columns
     * @param   string $column
     * @return  boolean
     */
    public function hasColumn($column)
    {
        return isset($this->_columns[$column]);
    }

    public function set($params)
    {
        if (isset($params['Column_name'])) {
            $this->_database = $params['Column_name'];
        }
        if (isset($params['Table'])) {
            $this->_table = $params['Table'];
        }
        if (isset($params['Key_name'])) {
            $this->_name = $params['Key_name'];
        }
        if (isset($params['Index_type'])) {
            $this->_type = $params['Index_type'];
        }
        if (isset($params['Comment'])) {
            $this->_remarks = $params['Comment'];
        }
        if (isset($params['Index_comment'])) {
            $this->_comment = $params['Index_comment'];
        }
        if (isset($params['Non_unique'])) {
            $this->_non_unique = $params['Non_unique'];
        }
        if (isset($params['Packed'])) {
            $this->_packed = $params['Packed'];
        }
    }

    public function getColumnCount()
    {
        return count($this->_columns);
    }

    public function getComment()
    {
        return $this->_comment;
    }

    public function getRemarks()
    {
        return $this->_remarks;
    }

    public function getComments()
    {
        $comments = $this->getRemarks();
        if (strlen($comments)) {
            $comments .= "\n";
        }
        $comments .= $this->getComment();

        return $comments;
    }

    public function getType()
    {
        return $this->_type;
    }

    public function getPacked()
    {
        return $this->_packed;
    }

    public function isPacked($as_text = false)
    {
        if ($as_text) {
            $r = array(
                '0' => $GLOBALS['strNo'],
                '1' => $GLOBALS['strYes'],
            );
        } else {
            $r = array(
                '0' => false,
                '1' => true,
            );
        }

        if (null === $this->_packed) {
            return $r[0];
        }

        return $this->_packed;
    }

    public function getNonUnique()
    {
        return $this->_non_unique;
    }

    public function isUnique($as_text = false)
    {
        if ($as_text) {
            $r = array(
                '0' => $GLOBALS['strYes'],
                '1' => $GLOBALS['strNo'],
            );
        } else {
            $r = array(
                '0' => true,
                '1' => false,
            );
        }

        return $r[$this->_non_unique];
    }

    public function getName()
    {
        return $this->_name;
    }

    public function getColumns()
    {
        return $this->_columns;
    }

    /**
     * Show index data
     *
     * @param   string      $table          The tablename
     * @param   array       $indexes_info   Referenced info array
     * @param   array       $indexes_data   Referenced data array
     * @param   boolean     $print_mode
     * @access  public
     * @return  array       Index collection array
     * @author  Garvin Hicking (pma@supergarv.de)
     */
    static public function getView($table, $db, $print_mode = false)
    {
        $indexes = PMA_Index::getFromTable($table, $db);

        if (count($indexes) < 1) {
            return PMA_Message::warning('strNoIndex')->getDisplay();
        }

        $r = '';

        $r .= '<h2>' . $GLOBALS['strIndexes'] . ': ';
        $r .= PMA_showMySQLDocu('optimization', 'optimizing-database-structure');
        $r .= '</h2>';
        $r .= '<table>';
        $r .= '<thead>';
        $r .= '<tr>';
        if (! $print_mode) {
            $r .= '<th colspan="2">' . $GLOBALS['strAction'] . '</th>';
        }
        $r .= '<th>' . $GLOBALS['strKeyname'] . '</th>';
        $r .= '<th>' . $GLOBALS['strType'] . '</th>';
        $r .= '<th>' . $GLOBALS['strUnique'] . '</th>';
        $r .= '<th>' . $GLOBALS['strPacked'] . '</th>';
        $r .= '<th>' . $GLOBALS['strField'] . '</th>';
        $r .= '<th>' . $GLOBALS['strCardinality'] . '</th>';
        $r .= '<th>' . $GLOBALS['strCollation'] . '</th>';
        $r .= '<th>' . $GLOBALS['strNull'] . '</th>';
        $r .= '<th>' . $GLOBALS['strComment'] . '</th>';
        $r .= '</tr>';
        $r .= '</thead>';
        $r .= '<tbody>';

        $odd_row = true;
        foreach ($indexes as $index) {
            $row_span = ' rowspan="' . $index->getColumnCount() . '" ';

            $r .= '<tr class="' . ($odd_row ? 'odd' : 'even') . '">';

            if (! $print_mode) {
                $this_params = $GLOBALS['url_params'];
                $this_params['index'] = $index->getName();
                $r .= '<td ' . $row_span . '>'
                   . '    <a href="tbl_indexes.php' . PMA_generate_common_url($this_params)
                   . '">' . PMA_getIcon('b_edit.png', $GLOBALS['strEdit']) . '</a>'
                   . '</td>' . "\n";

                $this_params = $GLOBALS['url_params'];
                if ($index->getName() == 'PRIMARY') {
                    $this_params['sql_query'] = 'ALTER TABLE ' . PMA_backquote($table) . ' DROP PRIMARY KEY';
                    $this_params['zero_rows'] = $GLOBALS['strPrimaryKeyHasBeenDropped'];
                    $js_msg      = PMA_jsFormat('ALTER TABLE ' . $table . ' DROP PRIMARY KEY');
                } else {
                    $this_params['sql_query'] = 'ALTER TABLE ' . PMA_backquote($table) . ' DROP INDEX ' . PMA_backquote($index->getName());
                    $this_params['zero_rows'] = sprintf($GLOBALS['strIndexHasBeenDropped'], $index->getName());
                    $js_msg      = PMA_jsFormat('ALTER TABLE ' . $table . ' DROP INDEX ' . $index->getName());
                }

                $r .= '<td ' . $row_span . '>'
                   . '    <a href="sql.php' . PMA_generate_common_url($this_params)
                   . '" onclick="return confirmLink(this, \'' . $js_msg . '\')">'
                   . PMA_getIcon('b_drop.png', $GLOBALS['strDrop'])  . '</a>'
                   . '</td>' . "\n";
            }

            $r .= '<th ' . $row_span . '>' . htmlspecialchars($index->getName()) . '</th>';
            $r .= '<td ' . $row_span . '>' . htmlspecialchars($index->getType()) . '</td>';
            $r .= '<td ' . $row_span . '>' . $index->isUnique(true) . '</td>';
            $r .= '<td ' . $row_span . '>' . $index->isPacked(true) . '</td>';

            foreach ($index->getColumns() as $column) {
                if ($column->getSeqInIndex() > 1) {
                    $r .= '<tr class="' . ($odd_row ? 'odd' : 'even') . '">';
                }
                $r .= '<td>' . htmlspecialchars($column->getName());
                if ($column->getSubPart()) {
                    $r .= ' (' . $column->getSubPart() . ')';
                }
                $r .= '</td>';
                $r .= '<td>' . htmlspecialchars($column->getCardinality()) . '</td>';
                $r .= '<td>' . htmlspecialchars($column->getCollation()) . '</td>';
                $r .= '<td>' . htmlspecialchars($column->getNull()) . '</td>';

                if ($column->getSeqInIndex() == 1) {
                    $r .= '<td ' . $row_span . '>'
                        . htmlspecialchars($index->getComments()) . '</td>';
                }
                $r .= '</tr>';
            } // end foreach $index['Sequences']

            $odd_row = ! $odd_row;
        } // end while
        $r .= '</tbody>';
        $r .= '</table>';

        if (! $print_mode) {
            //$r .= PMA_check_indexes($ret_keys);
        }

        return $r;
    }
}

class PMA_Index_Column
{
    /**
     * @var string The column name
     */
    protected $_name = '';

    /**
     * @var integer The column sequence number in the index, starting with 1.
     */
    protected $_seq_in_index = 1;

    /**
     * @var string How the column is sorted in the index. “A” (Ascending) or NULL (Not sorted)
     */
    protected $_collation = null;

    /**
     * The number of indexed characters if the column is only partly indexed,
     * NULL if the entire column is indexed.
     *
     * @var integer
     */
    protected $_sub_part = null;

    /**
     * Contains YES if the column may contain NULL.
     * If not, the column contains NO.
     *
     * @var string
     */
    protected $_null = '';

    /**
     * An estimate of the number of unique values in the index. This is updated
     * by running ANALYZE TABLE or myisamchk -a. Cardinality is counted based on
     * statistics stored as integers, so the value is not necessarily exact even
     * for small tables. The higher the cardinality, the greater the chance that
     * MySQL uses the index when doing joins.
     *
     * @var integer
     */
    protected $_cardinality = 0;

    public function __construct($params = array())
    {
        $this->set($params);
    }

    public function set($params)
    {
        if (isset($params['Column_name'])) {
            $this->_name = $params['Column_name'];
        }
        if (isset($params['Seq_in_index'])) {
            $this->_seq_in_index = $params['Seq_in_index'];
        }
        if (isset($params['Collation'])) {
            $this->_collation = $params['Collation'];
        }
        if (isset($params['Cardinality'])) {
            $this->_cardinality = $params['Cardinality'];
        }
        if (isset($params['Sub_part'])) {
            $this->_sub_part = $params['Sub_part'];
        }
        if (isset($params['Null'])) {
            $this->_null = $params['Null'];
        }
    }

    public function getName()
    {
        return $this->_name;
    }

    public function getCollation()
    {
        return $this->_collation;
    }

    public function getCardinality()
    {
        return $this->_cardinality;
    }

    public function getNull()
    {
        return $this->_null;
    }

    public function getSeqInIndex()
    {
        return $this->_seq_in_index;
    }

    public function getSubPart()
    {
        return $this->_sub_part;
    }
}
?>