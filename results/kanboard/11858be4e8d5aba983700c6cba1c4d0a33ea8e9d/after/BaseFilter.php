<?php

namespace Kanboard\Filter;

use PicoDb\Table;

/**
 * Base filter class
 *
 * @package filter
 * @author  Frederic Guillot
 */
abstract class BaseFilter
{
    /**
     * @var Table
     */
    protected $query;

    /**
     * @var mixed
     */
    protected $value;

    /**
     * BaseFilter constructor
     *
     * @access public
     * @param  mixed $value
     */
    public function __construct($value = null)
    {
        $this->value = $value;
    }

    /**
     * Get object instance
     *
     * @static
     * @access public
     * @param  mixed $value
     * @return static
     */
    public static function getInstance($value = null)
    {
        $self = new static($value);
        return $self;
    }

    /**
     * Set query
     *
     * @access public
     * @param  Table $query
     * @return \Kanboard\Core\Filter\FilterInterface
     */
    public function withQuery(Table $query)
    {
        $this->query = $query;
        return $this;
    }

    /**
     * Set the value
     *
     * @access public
     * @param  string $value
     * @return \Kanboard\Core\Filter\FilterInterface
     */
    public function withValue($value)
    {
        $this->value = $value;
        return $this;
    }

    /**
     * Parse operator in the input string
     *
     * @access protected
     * @return string
     */
    protected function parseOperator()
    {
        $operators = array(
            '<=' => 'lte',
            '>=' => 'gte',
            '<' => 'lt',
            '>' => 'gt',
        );

        foreach ($operators as $operator => $method) {
            if (strpos($this->value, $operator) === 0) {
                $this->value = substr($this->value, strlen($operator));
                return $method;
            }
        }

        return '';
    }

    /**
     * Apply a date filter
     *
     * @access protected
     * @param  string $field
     */
    protected function applyDateFilter($field)
    {
        $timestamp = strtotime($this->value);
        $method = $this->parseOperator();

        if ($method !== '') {
            $this->query->$method($field, $timestamp);
        } else {
            $this->query->gte($field, $timestamp);
            $this->query->lte($field, $timestamp + 86399);
        }
    }
}