<?php declare(strict_types=1);
namespace Phan\Plugin;

use Phan\CodeBase;
use Phan\Config;
use Phan\Language\AST\Visitor\Element;
use Phan\Language\Context;
use Phan\Language\Element\Clazz;
use Phan\Language\Element\Func;
use Phan\Language\Element\Method;
use Phan\Plugin;
use Phan\PluginV2;
use Phan\PluginV2\AnalyzeNodeCapability;
use Phan\PluginV2\PreAnalyzeNodeCapability;
use Phan\PluginV2\AnalyzeClassCapability;
use Phan\PluginV2\AnalyzeFunctionCapability;
use Phan\PluginV2\AnalyzeMethodCapability;
use Phan\PluginV2\LegacyAnalyzeNodeCapability;
use Phan\PluginV2\LegacyPreAnalyzeNodeCapability;
use Phan\PluginV2\PluginAwareAnalysisVisitor;
use Phan\PluginV2\PluginAwarePreAnalysisVisitor;
use ast\Node;

/**
 * The root plugin that calls out each hook
 * on any plugins defined in the configuration.
 *
 * (Note: This is called almost once per each AST node being analyzed.
 * Speed is preferred over using Phan\Memoize.)
 */
final class ConfigPluginSet extends PluginV2 implements
    AnalyzeClassCapability,
    AnalyzeFunctionCapability,
    AnalyzeMethodCapability,
    LegacyAnalyzeNodeCapability,
    LegacyPreAnalyzeNodeCapability {

    /** @var Plugin[]|null - Cached plugin set for this instance. Lazily generated. */
    private $pluginSet;

    /** @var \Closure[]|null */
    private $preAnalyzeNodePluginSet;

    /** @var \Closure[]|null */
    private $analyzeNodePluginSet;

    /** @var AnalyzeClassCapability[]|null */
    private $analyzeClassPluginSet;

    /** @var AnalyzeFunctionCapability[]|null */
    private $analyzeFunctionPluginSet;

    /** @var AnalyzeMethodCapability[]|null */
    private $analyzeMethodPluginSet;

    /**
     * Call `ConfigPluginSet::instance()` instead.
     */
    private function __construct() {}

    /**
     * @return ConfigPluginSet
     * A shared single instance of this plugin
     */
    public static function instance() : ConfigPluginSet
    {
        static $instance = null;
        if ($instance === null) {
            $instance = new self;
            $instance->ensurePluginsExist();
        }
        return $instance;
    }

    /**
     * @param CodeBase $code_base
     * The code base in which the node exists
     *
     * @param Context $context
     * The context in which the node exits. This is
     * the context inside the given node rather than
     * the context outside of the given node
     *
     * @param Node $node
     * The php-ast Node being analyzed.
     *
     * @return void
     */
    public function preAnalyzeNode(
        CodeBase $code_base,
        Context $context,
        Node $node
    ) {
        $plugin_callback = $this->preAnalyzeNodePluginSet[$node->kind] ?? null;
        if ($plugin_callback !== null) {
            $plugin_callback(
                $code_base,
                $context,
                $node
            );
        }
    }

    /**
     * @param CodeBase $code_base
     * The code base in which the node exists
     *
     * @param Context $context
     * The context in which the node exits. This is
     * the context inside the given node rather than
     * the context outside of the given node
     *
     * @param Node $node
     * The php-ast Node being analyzed.
     *
     * @param Node $node
     * The parent node of the given node (if one exists).
     *
     * @return void
     */
    public function analyzeNode(
        CodeBase $code_base,
        Context $context,
        Node $node,
        Node $parent_node = null
    ) {
        $plugin_callback = $this->analyzeNodePluginSet[$node->kind] ?? null;
        if ($plugin_callback !== null) {
            $plugin_callback(
                $code_base,
                $context,
                $node,
                $parent_node
            );
        }
    }

    /**
     * @param CodeBase $code_base
     * The code base in which the class exists
     *
     * @param Clazz $class
     * A class being analyzed
     *
     * @return void
     */
    public function analyzeClass(
        CodeBase $code_base,
        Clazz $class
    ) {
        foreach ($this->analyzeClassPluginSet as $plugin) {
            $plugin->analyzeClass(
                $code_base,
                $class
            );
        }
    }

    /**
     * @param CodeBase $code_base
     * The code base in which the method exists
     *
     * @param Method $method
     * A method being analyzed
     *
     * @return void
     */
    public function analyzeMethod(
        CodeBase $code_base,
        Method $method
    ) {
        foreach ($this->analyzeMethodPluginSet as $plugin) {
            $plugin->analyzeMethod(
                $code_base,
                $method
            );
        }
    }

    /**
     * @param CodeBase $code_base
     * The code base in which the function exists
     *
     * @param Func $function
     * A function being analyzed
     *
     * @return void
     */
    public function analyzeFunction(
        CodeBase $code_base,
        Func $function
    ) {
        foreach ($this->analyzeFunctionPluginSet as $plugin) {
            $plugin->analyzeFunction(
                $code_base,
                $function
            );
        }
    }

    // Micro-optimization in tight loops: check for plugins before calling config plugin set
    public function hasPlugins() : bool {
        \assert(!\is_null($this->pluginSet));
        return \count($this->pluginSet) > 0;
    }

    public function hasAnalyzeFunctionPlugins() : bool {
        \assert(!\is_null($this->pluginSet));
        return \count($this->analyzeFunctionPluginSet) > 0;
    }

    public function hasAnalyzeMethodPlugins() : bool {
        \assert(!\is_null($this->pluginSet));
        return \count($this->analyzeMethodPluginSet) > 0;
    }

    /** @return void */
    private function ensurePluginsExist()
    {
        if (!\is_null($this->pluginSet)) {
            return;
        }
        $plugin_set = array_map(
            function (string $plugin_file_name) : PluginV2 {
                $plugin_instance =
                    require($plugin_file_name);

                \assert(!empty($plugin_instance),
                    "Plugins must return an instance of the plugin. The plugin at $plugin_file_name does not.");

                \assert($plugin_instance instanceof PluginV2,
                    "Plugins must extend \Phan\PluginV2. The plugin at $plugin_file_name does not.");

                return $plugin_instance;
            },
            Config::getValue('plugins')
        );
        $this->pluginSet = $plugin_set;

        $this->preAnalyzeNodePluginSet      = self::filterPreAnalysisPlugins($plugin_set);
        $this->analyzeNodePluginSet         = self::filterAnalysisPlugins($plugin_set);
        $this->analyzeMethodPluginSet       = self::filterByClass($plugin_set, AnalyzeMethodCapability::class);
        $this->analyzeFunctionPluginSet     = self::filterByClass($plugin_set, AnalyzeFunctionCapability::class);
        $this->analyzeClassPluginSet        = self::filterByClass($plugin_set, AnalyzeClassCapability::class);
    }

    /**
     * @return \Closure[] - [function(CodeBase $code_base, Context $context, Node $node, Node $parent_node = null): void]
     */
    private static function filterPreAnalysisPlugins(array $plugin_set) : array
    {
        $closures_for_kind = new ClosuresForKind();
        foreach ($plugin_set as $plugin) {
            if ($plugin instanceof LegacyPreAnalyzeNodeCapability) {
                if ($plugin instanceof PreAnalyzeNodeCapability) {
                    throw new \TypeError(sprintf("plugin %s should implement only one of LegacyPreAnalyzeNodeCapability and PreAnalyzeNodeCapability, not both", get_class($plugin)));
                }
                $closure = (new \ReflectionMethod($plugin, 'preAnalyzeNode'))->getClosure($plugin);
                $closures_for_kind->recordForAllKinds($closure);
            } else if ($plugin instanceof PreAnalyzeNodeCapability) {
                $plugin_analysis_class = $plugin->getPreAnalyzeNodeVisitorClassName();
                if (!is_subclass_of($plugin_analysis_class, PluginAwarePreAnalysisVisitor::class)) {
                    throw new \TypeError(sprintf("Result of %s::getAnalyzeNodeVisitorClassName must be the name of a subclass of '%s', but '%s' is not", get_class($plugin), PluginAwarePreAnalysisVisitor::class, $plugin_analysis_class));
                }
                $closure = (new \ReflectionMethod($plugin_analysis_class, 'staticInvoke'))->getClosure(null);
                $record_closure($plugin_analysis_class::get_handled_node_kinds(), $closure);
            }
        }
        return $closures_for_kind->getFlattenedClosures(static function(array $closure_list) : \Closure {
            return static function(CodeBase $code_base, Context $context, Node $node) use($closure_list) {
                foreach ($closure_list as $closure) {
                    $closure($code_base, $context, $node);
                }
            };
        });
    }

    /**
     * @return \Closure[][] - [function(CodeBase $code_base, Context $context, Node $node, Node $parent_node = null): void]
     * @var \Closure[][] $closures_for_kind
     */
    private static function filterAnalysisPlugins(array $plugin_set) : array
    {
        $closures_for_kind = new ClosuresForKind();
        foreach ($plugin_set as $plugin) {
            if ($plugin instanceof LegacyAnalyzeNodeCapability) {
                if ($plugin instanceof AnalyzeNodeCapability) {
                    throw new \TypeError(sprintf("plugin %s should implement only one of LegacyAnalyzeNodeCapability and AnalyzeNodeCapability, not both", get_class($plugin)));
                }
                $closure = (new \ReflectionMethod($plugin, 'analyzeNode'))->getClosure($plugin);
                $closures_for_kind->recordForAllKinds($closure);
            } else if ($plugin instanceof AnalyzeNodeCapability) {
                $plugin_analysis_class = $plugin->getAnalyzeNodeVisitorClassName();
                if (!is_subclass_of($plugin_analysis_class, PluginAwareAnalysisVisitor::class)) {
                    throw new \TypeError(sprintf("Result of %s::getAnalyzeNodeVisitorClassName must be the name of a subclass of '%s', but '%s' is not", get_class($plugin), PluginAwareAnalysisVisitor::class, $plugin_analysis_class));
                }
                $closure = (new \ReflectionMethod($plugin_analysis_class, 'staticInvoke'))->getClosure(null);
                $closures_for_kind->recordForKinds($plugin_analysis_class::get_handled_node_kinds(), $closure);
            }
        }
        return $closures_for_kind->getFlattenedClosures(static function(array $closure_list) : \Closure {
           return static function(CodeBase $code_base, Context $context, Node $node, Node $parent_node = null) use($closure_list) {
                foreach ($closure_list as $closure) {
                    $closure($code_base, $context, $node, $parent_node);
                }
           };
        });
    }

    private static function filterByClass(array $plugin_set, string $interface_name) : array
    {
        $result = [];
        foreach ($plugin_set as $plugin) {
            if ($plugin instanceof $interface_name) {
                $result[] = $plugin;
            }
        }
        return $result;
    }


    /**
     * @return Plugin[]
     */
    private function getPlugins() : array
    {
        return $this->pluginSet;
    }

}