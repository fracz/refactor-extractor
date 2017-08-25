<?php declare(strict_types=1);
namespace Phan\Analyze;

use \Phan\Phan;
use \Phan\Configuration;
use \Phan\Debug;
use \Phan\Language\Context;
use \ast\Node;

/**
 * Objects implementing this trait store a handle to
 * the AST node that defines them and allows us to
 * reanalyze them later on
 */
trait Analyzable {

    /**
     * @var Node
     * The AST Node defining this object. We keep a
     * reference to this so that we can come to it
     * and
     */
    private $node = null;

    /**
     * @var int
     * The depth of recursion on this analyzable
     * object
     */
    private $recursion_depth = 0;

    /**
     * @param Node $node
     * The AST Node defining this object. We keep a
     * reference to this so that we can come to it
     * and
     */
    public function setNode(Node $node) {
        // Don't waste the memory if we're in quick mode
        if (Configuration::instance()->quick_mode) {
            return;
        }

        $this->node = $node;
    }

    /**
     * @return bool
     * True if we have a node defined on this object
     */
    public function hasNode() : bool {
        return !empty($this->node);
    }

    /**
     * @return Node
     * The AST node associated with this object
     */
    public function getNode() : Node {
        return $this->node;
    }

    /**
     * @return null
     * Analyze the node associated with this object
     * in the given context
     */
    public function analyze(Context $context) : Context {
        // Don't do anything if we care about being
        // fast
        if (Configuration::instance()->quick_mode) {
            return $context;
        }

        if (!$this->hasNode()) {
            return $context;
        }

        // Don't go deeper than one level in
        if ($this->recursion_depth++ > 2) {
            return $context;
        }

        // Analyze the node in a cloned context so that we
        // don't overwrite anything
        $context = (new Phan)->analyzeNodeInContext(
            $this->getNode(),
            clone($context)
        );

        return $context;
    }
}