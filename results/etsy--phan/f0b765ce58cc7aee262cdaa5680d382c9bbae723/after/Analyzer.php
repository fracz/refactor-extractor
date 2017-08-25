<?php declare(strict_types=1);
namespace Phan;

use \Phan\CodeBase;
use \Phan\Configuration;
use \Phan\Debug;
use \Phan\Language\AST\Element;
use \Phan\Language\Context;
use \Phan\Language\FQSEN;
use \Phan\Language\ParseVisitor;
use \ast\Node;

/**
 * This class is the entry point into the static analyzer.
 */
class Analyzer {
    use \Phan\Analyze\DuplicateClass;
    use \Phan\Analyze\ParentClassExists;
    use \Phan\Analyze\ParentConstructorCalled;

    public function __construct() {
    }

    /**
     * Analyze the given set of files and emit any issues
     * found to STDOUT.
     *
     * @param CodeBase $code_base
     * A code base needs to be passed in because we require
     * it to be initialized before any classes or files are
     * loaded.
     *
     * @param string[] $file_path_list
     * A list of files to scan
     *
     * @return null
     * We emit messages to STDOUT. Nothing is returned.
     *
     * @see \Phan\CodeBase
     */
    public function analyze(
        CodeBase $code_base,
        array $file_path_list
    ) {
        // This first pass parses code and populates the
        // global state we'll need for doing a second
        // analysis after.
        foreach ($file_path_list as $file_path) {
            $this->parseFile($code_base, $file_path);
        }

        // Take a pass over all classes verifying various
        // states now that we have the whole state in
        // memory
        $this->analyzeClasses($code_base);

        // Take a pass over all functions verifying
        // various states now that we have the whole
        // state in memory
        $this->analyzeFunctions($code_base);

        // Once we know what the universe looks like we
        // can scan for more complicated issues.
        foreach ($file_path_list as $file_path) {
            $this->passTwo($code_base, $file_path);
        }

        // Emit all log messages
        Log::display();
    }


    /**
     * This first pass parses code and looks for the subset
     * of issues that can be found without having to have
     * an understanding of the entire code base.
     *
     * @param CodeBase $code_base
     * The CodeBase represents state across the entire
     * code base. This is a mutable object which is
     * populated as we parse files
     *
     * @param string $file_path
     * The full path to a file we'd like to parse
     */
    public function parseFile(
        CodeBase $code_base,
        string $file_path
    ) : Context {

        // Convert the file to an Abstract Syntax Tree
        // before passing it on to the recursive version
        // of this method
        $node = \ast\parse_file(
            $file_path,
            Configuration::instance()->ast_version
        );

        $context =
            (new Context($code_base))
                ->withFile($file_path)
                ->withLineNumberStart($node->lineno ?? 0)
                ->withLineNumberEnd($node->endLineno ?? 0);

        if (empty($node)) {
            Log::err(
                Log::EUNDEF,
                "Empty or missing file  {$file_path}",
                $file_path,
                0
            );
            return $context;
        }

        return $this->parseAndGetContextForNodeInContext($node, $context);
    }

    /**
     * Parse the given node in the given context populating
     * the code base within the context as a side effect. The
     * returned context is the new context from within the
     * given node.
     *
     * @param Node $node
     * A node to parse and scan for errors
     *
     * @param Context $context
     * The context in which this node exists
     *
     * @return Context
     * The context from within the node is returned
     */
    public function parseAndGetContextForNodeInContext(
        Node $node,
        Context $context
    ) : Context {

        // Visit the given node populating the code base
        // with anything we learn and get a new context
        // indicating the state of the world within the
        // given node
        $context =
            (new Element($node))->acceptKindVisitor(
                new ParseVisitor($context)
            );

        assert(!empty($context), 'Context cannot be null');

        // Recurse into each child node
        $child_context = $context;
        foreach($node->children as $child_node) {

            // Skip any non Node children.
            if (!($child_node instanceof Node)) {
                continue;
            }

            // Step into each child node and get an
            // updated context for the node
            $child_context =
                $this->parseAndGetContextForNodeInContext(
                    $child_node,
                    $child_context
                );

            assert(!empty($context), 'Context cannot be null');
        }

        // Pass the context back up to our parent
        return $context;
    }

    /**
     * Take a pass over all classes verifying various
     * states.
     *
     * @return null
     */
    private function analyzeClasses(CodeBase $code_base) {
        foreach ($code_base->getClassMap() as $fqsen_string => $clazz) {
            self::analyzeDuplicateClass($code_base, $clazz);
            self::analyzeParentClassExists($code_base, $clazz);
            self::analyzeParentConstructorCalled($code_base, $clazz);
        }
    }

    /**
     * Take a pass over all functions verifying various
     * states.
     *
     * @return null
     */
    private function analyzeFunctions(CodeBase $code_base) {
    }

    /**
     * Once we know what the universe looks like we
     * can scan for more complicated issues.
     *
     * @param CodeBase $code_base
     * A code base needs to be passed in because we require
     * it to be initialized before any classes or files are
     * loaded.
     *
     * @param string[] $file_path_list
     * A list of files to scan
     *
     * @return null
     */
    public function passTwo(
        CodeBase $code_base,
        string $file_path
    ) {
        // TODO
    }
}