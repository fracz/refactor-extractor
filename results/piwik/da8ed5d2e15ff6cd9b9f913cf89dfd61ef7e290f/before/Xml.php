<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik
 * @package Piwik
 */
namespace Piwik\DataTable\Renderer;

use Exception;
use Piwik\DataTable;
use Piwik\DataTable\Map;
use Piwik\DataTable\Renderer\Php;
use Piwik\DataTable\Simple;
use Piwik\DataTable\Renderer;
use Piwik\Piwik;

/**
 * XML export of a given DataTable.
 * See the tests cases for more information about the XML format (/tests/core/DataTable/Renderer.test.php)
 * Or have a look at the API calls examples.
 *
 * Works with recursive DataTable (when a row can be associated with a subDataTable).
 *
 * @package Piwik
 * @subpackage DataTable
 */
class Xml extends Renderer
{
    /**
     * Computes the dataTable output and returns the string/binary
     *
     * @return string
     */
    function render()
    {
        $this->renderHeader();
        return '<?xml version="1.0" encoding="utf-8" ?>' . "\n" . $this->renderTable($this->table);
    }

    /**
     * Computes the exception output and returns the string/binary
     *
     * @return string
     */
    function renderException()
    {
        $this->renderHeader();

        $exceptionMessage = $this->getExceptionMessage();

        $return = '<?xml version="1.0" encoding="utf-8" ?>' . "\n" .
            "<result>\n" .
            "\t<error message=\"" . $exceptionMessage . "\" />\n" .
            "</result>";

        return $return;
    }

    /**
     * Converts the given data table to an array
     *
     * @param DataTable|DataTable/Map $table  data table to convert
     * @return array
     */
    protected function getArrayFromDataTable($table)
    {
        if (is_array($table)) {
            return $table;
        }

        $renderer = new Php();
        $renderer->setRenderSubTables($this->isRenderSubtables());
        $renderer->setSerialize(false);
        $renderer->setTable($table);
        $renderer->setHideIdSubDatableFromResponse($this->hideIdSubDatatable);
        return $renderer->flatRender();
    }

    /**
     * Computes the output for the given data table
     *
     * @param DataTable|DataTable/Map $table
     * @param bool $returnOnlyDataTableXml
     * @param string $prefixLines
     * @return array|string
     * @throws Exception
     */
    protected function renderTable($table, $returnOnlyDataTableXml = false, $prefixLines = '')
    {
        $array = $this->getArrayFromDataTable($table);
        if ($table instanceof Map) {
            $out = $this->renderDataTableArray($table, $array, $prefixLines);

            if ($returnOnlyDataTableXml) {
                return $out;
            }
            $out = "<results>\n$out</results>";
            return $out;
        }

        // integer value of ZERO is a value we want to display
        if ($array != 0 && empty($array)) {
            if ($returnOnlyDataTableXml) {
                throw new Exception("Illegal state, what xml shall we return?");
            }
            $out = "<result />";
            return $out;
        }
        if ($table instanceof Simple) {
            if (is_array($array)) {
                $out = $this->renderDataTableSimple($array);
            } else {
                $out = $array;
            }
            if ($returnOnlyDataTableXml) {
                return $out;
            }

            if (is_array($array)) {
                $out = "<result>\n" . $out . "</result>";
            } else {
                $value = self::formatValueXml($out);
                if ($value === '') {
                    $out = "<result />";
                } else {
                    $out = "<result>" . $value . "</result>";
                }
            }
            return $out;
        }

        if ($table instanceof DataTable) {
            $out = $this->renderDataTable($array);
            if ($returnOnlyDataTableXml) {
                return $out;
            }
            $out = "<result>\n$out</result>";
            return $out;
        }

        if (is_array($array)) {
            $out = $this->renderArray($array, $prefixLines . "\t");
            if ($returnOnlyDataTableXml) {
                return $out;
            }
            return "<result>\n$out</result>";
        }
    }

    /**
     * Renders an array as XML.
     *
     * @param array $array The array to render.
     * @param string $prefixLines The string to prefix each line in the output.
     * @return string
     */
    private function renderArray($array, $prefixLines)
    {
        $isAssociativeArray = Piwik::isAssociativeArray($array);

        // check if array contains arrays, and if not wrap the result in an extra <row> element
        // (only check if this is the root renderArray call)
        // NOTE: this is for backwards compatibility. before, array's were added to a new DataTable.
        // if the array had arrays, they were added as multiple rows, otherwise it was treated as
        // one row. removing will change API output.
        $wrapInRow = $prefixLines === "\t"
            && self::shouldWrapArrayBeforeRendering($array, $wrapSingleValues = false, $isAssociativeArray);

        // render the array
        $result = "";
        if ($wrapInRow) {
            $result .= "$prefixLines<row>\n";
            $prefixLines .= "\t";
        }
        foreach ($array as $key => $value) {
            // based on the type of array & the key, determine how this node will look
            if ($isAssociativeArray) {
                if (is_numeric($key)) {
                    $prefix = "<row key=\"$key\">";
                    $suffix = "</row>";
                    $emptyNode = "<row key=\"$key\"/>";
                } else {
                    $prefix = "<$key>";
                    $suffix = "</$key>";
                    $emptyNode = "<$key />";
                }
            } else {
                $prefix = "<row>";
                $suffix = "</row>";
                $emptyNode = "<row/>";
            }

            // render the array item
            if (is_array($value)) {
                $result .= $prefixLines . $prefix . "\n";
                $result .= $this->renderArray($value, $prefixLines . "\t");
                $result .= $prefixLines . $suffix . "\n";
            } else if ($value instanceof DataTable
                || $value instanceof Map
            ) {
                if ($value->getRowsCount() == 0) {
                    $result .= $prefixLines . $emptyNode . "\n";
                } else {
                    $result .= $prefixLines . $prefix . "\n";
                    if ($value instanceof Map) {
                        $result .= $this->renderDataTableArray($value, $this->getArrayFromDataTable($value), $prefixLines);
                    } else if ($value instanceof Simple) {
                        $result .= $this->renderDataTableSimple($this->getArrayFromDataTable($value), $prefixLines);
                    } else {
                        $result .= $this->renderDataTable($this->getArrayFromDataTable($value), $prefixLines);
                    }
                    $result .= $prefixLines . $suffix . "\n";
                }
            } else {
                $xmlValue = self::formatValueXml($value);
                if (strlen($xmlValue) != 0) {
                    $result .= $prefixLines . $prefix . $xmlValue . $suffix . "\n";
                } else {
                    $result .= $prefixLines . $emptyNode . "\n";
                }
            }
        }
        if ($wrapInRow) {
            $result .= substr($prefixLines, 0, strlen($prefixLines) - 1) . "</row>\n";
        }
        return $result;
    }

    /**
     * Computes the output for the given data table array
     *
     * @param Map $table
     * @param array $array
     * @param string $prefixLines
     * @return string
     */
    protected function renderDataTableArray($table, $array, $prefixLines = "")
    {
        // CASE 1
        //array
        //  'day1' => string '14' (length=2)
        //  'day2' => string '6' (length=1)
        $firstTable = current($array);
        if (!is_array($firstTable)) {
            $xml = '';
            $nameDescriptionAttribute = $table->getKeyName();
            foreach ($array as $valueAttribute => $value) {
                if (empty($value)) {
                    $xml .= $prefixLines . "\t<result $nameDescriptionAttribute=\"$valueAttribute\" />\n";
                } elseif ($value instanceof Map) {
                    $out = $this->renderTable($value, true);
                    //TODO somehow this code is not tested, cover this case
                    $xml .= "\t<result $nameDescriptionAttribute=\"$valueAttribute\">\n$out</result>\n";
                } else {
                    $xml .= $prefixLines . "\t<result $nameDescriptionAttribute=\"$valueAttribute\">" . self::formatValueXml($value) . "</result>\n";
                }
            }
            return $xml;
        }

        $subTables = $table->getArray();
        $firstTable = current($subTables);

        // CASE 2
        //array
        //  'day1' =>
        //    array
        //      'nb_uniq_visitors' => string '18'
        //      'nb_visits' => string '101'
        //  'day2' =>
        //    array
        //      'nb_uniq_visitors' => string '28'
        //      'nb_visits' => string '11'
        if ($firstTable instanceof Simple) {
            $xml = '';
            $nameDescriptionAttribute = $table->getKeyName();
            foreach ($array as $valueAttribute => $dataTableSimple) {
                if (count($dataTableSimple) == 0) {
                    $xml .= $prefixLines . "\t<result $nameDescriptionAttribute=\"$valueAttribute\" />\n";
                } else {
                    if (is_array($dataTableSimple)) {
                        $dataTableSimple = "\n" . $this->renderDataTableSimple($dataTableSimple, $prefixLines . "\t") . $prefixLines . "\t";
                    }
                    $xml .= $prefixLines . "\t<result $nameDescriptionAttribute=\"$valueAttribute\">" . $dataTableSimple . "</result>\n";
                }
            }
            return $xml;
        }

        // CASE 3
        //array
        //  'day1' =>
        //    array
        //      0 =>
        //        array
        //          'label' => string 'phpmyvisites'
        //          'nb_uniq_visitors' => int 11
        //          'nb_visits' => int 13
        //      1 =>
        //        array
        //          'label' => string 'phpmyvisits'
        //          'nb_uniq_visitors' => int 2
        //          'nb_visits' => int 2
        //  'day2' =>
        //    array
        //      0 =>
        //        array
        //          'label' => string 'piwik'
        //          'nb_uniq_visitors' => int 121
        //          'nb_visits' => int 130
        //      1 =>
        //        array
        //          'label' => string 'piwik bis'
        //          'nb_uniq_visitors' => int 20
        //          'nb_visits' => int 120
        if ($firstTable instanceof DataTable) {
            $xml = '';
            $nameDescriptionAttribute = $table->getKeyName();
            foreach ($array as $keyName => $arrayForSingleDate) {
                $dataTableOut = $this->renderDataTable($arrayForSingleDate, $prefixLines . "\t");
                if (empty($dataTableOut)) {
                    $xml .= $prefixLines . "\t<result $nameDescriptionAttribute=\"$keyName\" />\n";
                } else {
                    $xml .= $prefixLines . "\t<result $nameDescriptionAttribute=\"$keyName\">\n";
                    $xml .= $dataTableOut;
                    $xml .= $prefixLines . "\t</result>\n";
                }
            }
            return $xml;
        }

        if ($firstTable instanceof Map) {
            $xml = '';
            $tables = $table->getArray();
            $nameDescriptionAttribute = $table->getKeyName();
            foreach ($tables as $valueAttribute => $tableInArray) {
                $out = $this->renderTable($tableInArray, true, $prefixLines . "\t");
                $xml .= $prefixLines . "\t<result $nameDescriptionAttribute=\"$valueAttribute\">\n" . $out . $prefixLines . "\t</result>\n";
            }
            return $xml;
        }
    }

    /**
     * Computes the output for the given data array
     *
     * @param array $array
     * @param string $prefixLine
     * @return string
     */
    protected function renderDataTable($array, $prefixLine = "")
    {
        $out = '';
        foreach ($array as $rowId => $row) {
            if (!is_array($row)) {
                $value = self::formatValueXml($row);
                if (strlen($value) == 0) {
                    $out .= $prefixLine . "\t\t<$rowId />\n";
                } else {
                    $out .= $prefixLine . "\t\t<$rowId>" . $value . "</$rowId>\n";
                }
                continue;
            }

            // Handing case idgoal=7, creating a new array for that one
            $rowAttribute = '';
            if (($equalFound = strstr($rowId, '=')) !== false) {
                $rowAttribute = explode('=', $rowId);
                $rowAttribute = " " . $rowAttribute[0] . "='" . $rowAttribute[1] . "'";
            }
            $out .= $prefixLine . "\t<row$rowAttribute>";

            if (count($row) === 1
                && key($row) === 0
            ) {
                $value = self::formatValueXml(current($row));
                $out .= $prefixLine . $value;
            } else {
                $out .= "\n";
                foreach ($row as $name => $value) {
                    // handle the recursive dataTable case by XML outputting the recursive table
                    if (is_array($value)) {
                        $value = "\n" . $this->renderDataTable($value, $prefixLine . "\t\t");
                        $value .= $prefixLine . "\t\t";
                    } else {
                        $value = self::formatValueXml($value);
                    }
                    if (strlen($value) == 0) {
                        $out .= $prefixLine . "\t\t<$name />\n";
                    } else {
                        $out .= $prefixLine . "\t\t<$name>" . $value . "</$name>\n";
                    }
                }
                $out .= "\t";
            }
            $out .= $prefixLine . "</row>\n";
        }
        return $out;
    }

    /**
     * Computes the output for the given data array (representing a simple data table)
     *
     * @param $array
     * @param string $prefixLine
     * @return string
     */
    protected function renderDataTableSimple($array, $prefixLine = "")
    {
        $out = '';
        foreach ($array as $keyName => $value) {
            $xmlValue = self::formatValueXml($value);
            if (strlen($xmlValue) == 0) {
                $out .= $prefixLine . "\t<$keyName />\n";
            } else {
                $out .= $prefixLine . "\t<$keyName>" . $xmlValue . "</$keyName>\n";
            }
        }
        return $out;
    }

    /**
     * Sends the XML headers
     */
    protected function renderHeader()
    {
        // silent fail because otherwise it throws an exception in the unit tests
        @header('Content-Type: text/xml; charset=utf-8');
    }
}