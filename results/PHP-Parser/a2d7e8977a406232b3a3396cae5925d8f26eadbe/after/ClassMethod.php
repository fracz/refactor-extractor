<?php

namespace PhpParser\Node\Stmt;

use PhpParser\Node;
use PhpParser\Error;

class ClassMethod extends Node\Stmt
{
    /** @var int Type */
    public $type;
    /** @var bool Whether to return by reference */
    public $byRef;
    /** @var string Name */
    public $name;
    /** @var Node\Param[] Parameters */
    public $params;
    /** @var Node[] Statements */
    public $stmts;

    /**
     * Constructs a class method node.
     *
     * @param string      $name       Name
     * @param array       $subNodes   Array of the following optional subnodes:
     *                                'type'   => MODIFIER_PUBLIC: Type
     *                                'byRef'  => false          : Whether to return by reference
     *                                'params' => array()        : Parameters
     *                                'stmts'  => array()        : Statements
     * @param array       $attributes Additional attributes
     */
    public function __construct($name, array $subNodes = array(), array $attributes = array()) {
        parent::__construct(null, $attributes);
        $this->type = isset($subNodes['type']) ? $subNodes['type'] : 0;
        $this->byRef = isset($subNodes['byRef'])  ? $subNodes['byRef']  : false;
        $this->name = $name;
        $this->params = isset($subNodes['params']) ? $subNodes['params'] : array();
        $this->stmts = array_key_exists('stmts', $subNodes) ? $subNodes['stmts'] : array();

        if ($this->type & Class_::MODIFIER_STATIC) {
            switch (strtolower($this->name)) {
                case '__construct':
                    throw new Error(sprintf('Constructor %s() cannot be static', $this->name));
                case '__destruct':
                    throw new Error(sprintf('Destructor %s() cannot be static', $this->name));
                case '__clone':
                    throw new Error(sprintf('Clone method %s() cannot be static', $this->name));
            }
        }
    }

    public function getSubNodeNames() {
        return array('type', 'byRef', 'name', 'params', 'stmts');
    }

    public function isPublic() {
        return ($this->type & Class_::MODIFIER_PUBLIC) !== 0 || $this->type === 0;
    }

    public function isProtected() {
        return (bool) ($this->type & Class_::MODIFIER_PROTECTED);
    }

    public function isPrivate() {
        return (bool) ($this->type & Class_::MODIFIER_PRIVATE);
    }

    public function isAbstract() {
        return (bool) ($this->type & Class_::MODIFIER_ABSTRACT);
    }

    public function isFinal() {
        return (bool) ($this->type & Class_::MODIFIER_FINAL);
    }

    public function isStatic() {
        return (bool) ($this->type & Class_::MODIFIER_STATIC);
    }
}