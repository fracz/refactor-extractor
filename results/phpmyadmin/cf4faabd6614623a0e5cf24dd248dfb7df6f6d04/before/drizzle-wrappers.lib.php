<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Wrappers for Drizzle extension classes
 *
 * Drizzle extension exposes libdrizzle functions and requires user to have it in
 * mind while using them.
 * This wrapper is not complete and hides a lot of original functionality,
 * but allows for easy usage of the drizzle PHP extension.
 *
 * @package    PhpMyAdmin-DBI
 * @subpackage Drizzle
 */
if (! defined('PHPMYADMIN')) {
    exit;
}

/**
 * Workaround for crashing module
 *
 * @return void
 *
 * @todo drizzle module segfaults while freeing resources, often.
 *       This allows at least for some development
 */
function PMA_drizzleShutdownFlush()
{
    flush();
}
register_shutdown_function('PMA_drizzleShutdownFlush');

/**
 * Wrapper for Drizzle class
 *
 * @package    PhpMyAdmin-DBI
 * @subpackage Drizzle
 */
class PMA_Drizzle extends Drizzle
{
    /**
     * Fetch mode: result rows contain column names
     */
    const FETCH_ASSOC = 1;
    /**
     * Fetch mode: result rows contain only numeric indices
     */
    const FETCH_NUM = 2;
    /**
     * Fetch mode: result rows have both column names and numeric indices
     */
    const FETCH_BOTH = 3;

    /**
     * Result buffering: entire result set is buffered upon execution
     */
    const BUFFER_RESULT = 1;
    /**
     * Result buffering: buffering occurs only on row level
     */
    const BUFFER_ROW = 2;

    /**
     * Constructor
     */
    public function __construct()
    {
        parent::__construct();
    }

    /**
     * Creates a new database conection using TCP
     *
     * @param string  $host     Drizzle host
     * @param integer $port     Drizzle port
     * @param string  $user     username
     * @param string  $password password
     * @param string  $db       database name
     * @param integer $options  connection options
     *
     * @return PMA_DrizzleCon
     */
    public function addTcp($host, $port, $user, $password, $db, $options)
    {
        $dcon = parent::addTcp($host, $port, $user, $password, $db, $options);
        return $dcon instanceof DrizzleCon
            ? new PMA_DrizzleCon($dcon)
            : $dcon;
    }

    /**
     * Creates a new connection using unix domain socket
     *
     * @param string  $uds      socket
     * @param string  $user     username
     * @param string  $password password
     * @param string  $db       database name
     * @param integer $options  connection options
     *
     * @return PMA_DrizzleCon
     */
    public function addUds($uds, $user, $password, $db, $options)
    {
        $dcon = parent::addUds($uds, $user, $password, $db, $options);
        return $dcon instanceof DrizzleCon
            ? new PMA_DrizzleCon($dcon)
            : $dcon;
    }
}

/**
 * Wrapper around DrizzleCon class
 *
 * Its main task is to wrap results with PMA_DrizzleResult class
 *
 * @package    PhpMyAdmin-DBI
 * @subpackage Drizzle
 */
class PMA_DrizzleCon
{
    /**
     * Instance of DrizzleCon class
     * @var DrizzleCon
     */
    private $_dcon;

    /**
     * Result of the most recent query
     * @var PMA_DrizzleResult
     */
    private $_lastResult;

    /**
     * Constructor
     *
     * @param DrizzleCon $_dcon connection handle
     *
     * @return void
     */
    public function __construct(DrizzleCon $dcon)
    {
        $this->_dcon = $dcon;
    }

    /**
     * Executes given query. Opens database connection if not already done.
     *
     * @param string $query       query to execute
     * @param int    $bufferMode  PMA_Drizzle::BUFFER_RESULT,PMA_Drizzle::BUFFER_ROW
     * @param int    $fetchMode   PMA_Drizzle::FETCH_ASSOC, PMA_Drizzle::FETCH_NUM
     *                            or PMA_Drizzle::FETCH_BOTH
     *
     * @return PMA_DrizzleResult
     */
    public function query($query, $bufferMode = PMA_Drizzle::BUFFER_RESULT,
        $fetchMode = PMA_Drizzle::FETCH_ASSOC
    ) {
        $result = $this->_dcon->query($query);
        if ($result instanceof DrizzleResult) {
            $this->_lastResult = new PMA_DrizzleResult(
                $result, $bufferMode, $fetchMode
            );
            return $this->_lastResult;
        }
        return $result;
    }

    /**
     * Returns the number of rows affected by last query
     *
     * @return int|false
     */
    public function affectedRows()
    {
        return $this->_lastResult
            ? $this->_lastResult->affectedRows()
            : false;
    }

    /**
     * Pass calls of undefined methods to DrizzleCon object
     *
     * @param $method
     * @param $args
     *
     * @return mixed
     */
    public function __call($method, $args)
    {
        return call_user_func_array(array($this->_dcon, $method), $args);
    }

    /**
     * Returns original Drizzle connection object
     *
     * @return DrizzleCon
     */
    public function getConnectionObject()
    {
        return $this->_dcon;
    }
}

/**
 * Wrapper around DrizzleResult.
 *
 * Allows for reading result rows as an associative array and hides complexity
 * behind buffering.
 *
 * @package    PhpMyAdmin-DBI
 * @subpackage Drizzle
 */
class PMA_DrizzleResult
{
    /**
     * Instamce of DrizzleResult class
     * @var DrizzleResult
     */
    private $dresult;
    /**
     * Fetch mode
     * @var int
     */
    private $fetchMode;
    /**
     * Buffering mode
     * @var int
     */
    private $bufferMode;

    /**
     * Cached column data
     * @var DrizzleColumn[]
     */
    private $columns = null;
    /**
     * Cached column names
     * @var string[]
     */
    private $columnNames = null;

    /**
     * Constructor
     *
     * @param DrizzleResult $dresult
     * @param int           $bufferMode
     * @param int           $fetchMode
     */
    public function __construct(DrizzleResult $dresult, $bufferMode, $fetchMode)
    {
        $this->dresult = $dresult;
        $this->bufferMode = $bufferMode;
        $this->fetchMode = $fetchMode;

        if ($this->bufferMode == PMA_Drizzle::BUFFER_RESULT) {
            $this->dresult->buffer();
        }
    }

    /**
     * Sets fetch mode
     *
     * @param int $fetchMode
     *
     * @return void
     */
    public function setFetchMode($fetchMode)
    {
        $this->fetchMode = $fetchMode;
    }

    /**
     * Reads information about columns contained in current result
     * set into {@see $columns} and {@see $columnNames} arrays
     *
     * @return void
     */
    private function _readColumns()
    {
        $this->columns = array();
        $this->columnNames = array();
        if ($this->bufferMode == PMA_Drizzle::BUFFER_RESULT) {
            while (($column = $this->dresult->columnNext()) !== null) {
                $this->columns[] = $column;
                $this->columnNames[] = $column->name();
            }
        } else {
            while (($column = $this->dresult->columnRead()) !== null) {
                $this->columns[] = $column;
                $this->columnNames[] = $column->name();
            }
        }
    }

    /**
     * Returns columns in current result
     *
     * @return DrizzleColumn[]
     */
    public function getColumns()
    {
        if (!$this->columns) {
            $this->_readColumns();
        }
        return $this->columns;
    }

    /**
     * Returns number if columns in result
     *
     * @return int
     */
    public function numColumns()
    {
        return $this->dresult->columnCount();
    }

    /**
     * Transforms result row to conform to current fetch mode
     *
     * @param mixed &$row
     * @param int   $fetchMode
     *
     * @return void
     */
    private function _transformResultRow(&$row, $fetchMode)
    {
        if (!$row) {
            return;
        }

        switch ($fetchMode) {
        case PMA_Drizzle::FETCH_ASSOC:
            $row = array_combine($this->columnNames, $row);
            break;
        case PMA_Drizzle::FETCH_BOTH:
            $length = count($row);
            for ($i = 0; $i < $length; $i++) {
                $row[$this->columnNames[$i]] = $row[$i];
            }
            break;
        default:
            break;
        }
    }

    /**
     * Fetches next for from this result set
     *
     * @param int $fetchMode fetch mode to use, if not given the default one is used
     *
     * @return array|null
     */
    public function fetchRow($fetchMode = null)
    {
        // read column names on first fetch, only buffered results
        // allow for reading it later
        if (!$this->columns) {
            $this->_readColumns();
        }
        if ($fetchMode === null) {
            $fetchMode = $this->fetchMode;
        }
        $row = null;
        switch ($this->bufferMode) {
        case PMA_Drizzle::BUFFER_RESULT:
            $row = $this->dresult->rowNext();
            break;
        case PMA_Drizzle::BUFFER_ROW:
            $row = $this->dresult->rowBuffer();
            break;
        }
        $this->_transformResultRow($row, $fetchMode);
        return $row;
    }

    /**
     * Adjusts the result pointer to an arbitrary row in buffered result
     *
     * @param $row_index
     *
     * @return bool
     */
    public function seek($row_index)
    {
        if ($this->bufferMode != PMA_Drizzle::BUFFER_RESULT) {
            trigger_error(
                __("Can't seek in an unbuffered result set"), E_USER_WARNING
            );
            return false;
        }
        // rowSeek always returns NULL (drizzle extension v.0.5, API v.7)
        if ($row_index >= 0 && $row_index < $this->dresult->rowCount()) {
            $this->dresult->rowSeek($row_index);
            return true;
        }
        return false;
    }

    /**
     * Returns the number of rows in buffered result set
     *
     * @return int|false
     */
    public function numRows()
    {
        if ($this->bufferMode != PMA_Drizzle::BUFFER_RESULT) {
            trigger_error(
                __("Can't count rows in an unbuffered result set"), E_USER_WARNING
            );
            return false;
        }
        return $this->dresult->rowCount();
    }

    /**
     * Returns the number of rows affected by query
     *
     * @return int|false
     */
    public function affectedRows()
    {
        return $this->dresult->affectedRows();
    }

    /**
     * Frees resources taken by this result
     *
     * @return void
     */
    public function free()
    {
        unset($this->columns);
        unset($this->columnNames);
        drizzle_result_free($this->dresult);
        unset($this->dresult);
    }
}