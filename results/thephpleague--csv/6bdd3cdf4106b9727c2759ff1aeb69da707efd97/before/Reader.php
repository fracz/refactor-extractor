<?php
/**
* This file is part of the League.csv library
*
* @license http://opensource.org/licenses/MIT
* @link https://github.com/thephpleague/csv/
* @version 9.0.0
* @package League.csv
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
declare(strict_types=1);

namespace League\Csv;

use CallbackFilterIterator;
use Iterator;
use IteratorAggregate;
use League\Csv\Exception\RuntimeException;
use LimitIterator;
use SplFileObject;

/**
 *  A class to manage extracting and filtering a CSV
 *
 * @package League.csv
 * @since  3.0.0
 *
 */
class Reader extends AbstractCsv implements IteratorAggregate
{
    /**
     * @inheritdoc
     */
    protected $stream_filter_mode = STREAM_FILTER_READ;

    /**
     * CSV Document header offset
     *
     * @var int|null
     */
    protected $header_offset;

    /**
     * CSV Document Header record
     *
     * @var string[]
     */
    protected $header = [];

    /**
     * Tell whether the header needs to be re-generated
     *
     * @var bool
     */
    protected $is_header_loaded = false;

    /**
     * Returns the record offset used as header
     *
     * If no CSV record is used this method MUST return null
     *
     * @return int|null
     */
    public function getHeaderOffset()
    {
        return $this->header_offset;
    }

    /**
     * Selects the record to be used as the CSV header
     *
     * Because of the header is represented as an array, to be valid
     * a header MUST contain only unique string value.
     *
     * @param int|null $offset the header row offset
     *
     * @return static
     */
    public function setHeaderOffset($offset): self
    {
        $this->header_offset = null;
        if (null !== $offset) {
            $this->header_offset = $this->filterInteger(
                $offset,
                0,
                'the header offset index must be a positive integer or 0'
            );
        }
        $this->resetDynamicProperties();

        return $this;
    }

    /**
     * @inheritdoc
     */
    protected function resetDynamicProperties()
    {
        return $this->is_header_loaded = false;
    }

    /**
     * Detect Delimiters occurences in the CSV
     *
     * Returns a associative array where each key represents
     * a valid delimiter and each value the number of occurences
     *
     * @param string[] $delimiters the delimiters to consider
     * @param int      $nb_rows    Detection is made using $nb_rows of the CSV
     *
     * @return array
     */
    public function fetchDelimitersOccurrence(array $delimiters, int $nb_rows = 1): array
    {
        $nb_rows = $this->filterInteger($nb_rows, 1, 'The number of rows to consider must be a valid positive integer');
        $filter_row = function ($row) {
            return is_array($row) && count($row) > 1;
        };
        $delimiters = array_unique(array_filter($delimiters, function ($value) {
            return 1 == strlen($value);
        }));
        $this->document->setFlags(SplFileObject::READ_CSV);
        $res = [];
        foreach ($delimiters as $delim) {
            $this->document->setCsvControl($delim, $this->enclosure, $this->escape);
            $iterator = new CallbackFilterIterator(new LimitIterator($this->document, 0, $nb_rows), $filter_row);
            $res[$delim] = count(iterator_to_array($iterator, false), COUNT_RECURSIVE);
        }
        arsort($res, SORT_NUMERIC);

        return $res;
    }

    /**
     * Returns a collection of selected records
     *
     * @param Statement|null $stmt
     *
     * @return RecordSet
     */
    public function select(Statement $stmt = null): RecordSet
    {
        $stmt = $stmt ?? new Statement();

        return $stmt->process($this);
    }

    /**
     * @inheritdoc
     */
    public function getIterator(): Iterator
    {
        $bom = $this->getInputBOM();
        $header = $this->getHeader();
        $this->document->setFlags(SplFileObject::READ_CSV | SplFileObject::READ_AHEAD | SplFileObject::SKIP_EMPTY);
        $this->document->setCsvControl($this->delimiter, $this->enclosure, $this->escape);
        $normalized = function ($row) {
            return is_array($row) && $row != [null];
        };
        $iterator = new CallbackFilterIterator($this->document, $normalized);
        $iterator = $this->combineHeader($iterator, $header);

        return $this->stripBOM($iterator, $bom);
    }

    /**
     * Add the CSV header if present and valid
     *
     * @param Iterator $iterator
     * @param string[] $header
     *
     * @return Iterator
     */
    protected function combineHeader(Iterator $iterator, array $header): Iterator
    {
        if (null === $this->header_offset) {
            return $iterator;
        }

        $header = $this->filterColumnNames($header);
        $header_count = count($header);
        $iterator = new CallbackFilterIterator($iterator, function (array $row, int $offset) {
            return $offset != $this->header_offset;
        });

        return new MapIterator($iterator, function (array $row) use ($header_count, $header) {
            if ($header_count != count($row)) {
                $row = array_slice(array_pad($row, $header_count, null), 0, $header_count);
            }

            return array_combine($header, $row);
        });
    }

    /**
     * Strip the BOM sequence if present
     *
     * @param Iterator $iterator
     * @param string   $bom
     *
     * @return Iterator
     */
    protected function stripBOM(Iterator $iterator, string $bom): Iterator
    {
        if ('' === $bom) {
            return $iterator;
        }

        $bom_length = mb_strlen($bom);
        return new MapIterator($iterator, function (array $row, $index) use ($bom_length) {
            if (0 != $index) {
                return $row;
            }

            return $this->removeBOM($row, $bom_length, $this->enclosure);
        });
    }

    /**
     * Strip the BOM sequence from a record
     *
     * @param string[] $row
     * @param int      $bom_length
     * @param string   $enclosure
     *
     * @return string[]
     */
    protected function removeBOM(array $row, int $bom_length, string $enclosure): array
    {
        if (0 == $bom_length) {
            return $row;
        }

        $row[0] = mb_substr($row[0], $bom_length);
        if ($enclosure == mb_substr($row[0], 0, 1) && $enclosure == mb_substr($row[0], -1, 1)) {
            $row[0] = mb_substr($row[0], 1, -1);
        }

        return $row;
    }

    /**
     * Returns the column header associate with the RecordSet
     *
     * @throws RuntimeException If no header is found
     *
     * @return string[]
     */
    public function getHeader(): array
    {
        if ($this->is_header_loaded) {
            return $this->header;
        }

        $this->is_header_loaded = true;
        if (null === $this->header_offset) {
            $this->header = [];

            return $this->header;
        }

        $this->document->setFlags(SplFileObject::READ_CSV | SplFileObject::READ_AHEAD | SplFileObject::SKIP_EMPTY);
        $this->document->setCsvControl($this->delimiter, $this->enclosure, $this->escape);
        $this->document->seek($this->header_offset);
        $header = $this->document->current();
        if (empty($header)) {
            throw new RuntimeException('The header record specified by `Reader::setHeaderOffset` does not exist or is empty');
        }

        if (0 !== $this->header_offset) {
            $this->header = $header;

            return $this->header;
        }

        $this->header = $this->removeBOM($header, mb_strlen($this->getInputBOM()), $this->enclosure);

        return $this->header;
    }
}