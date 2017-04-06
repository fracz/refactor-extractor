commit 1e0ed22c55115eb09838972c9246d68e59285147
Author: Victor Berchet <victor.berchet@yahoo.com>
Date:   Mon Mar 14 18:29:56 2011 +0100

    [Config] Component refactoring

    The Config component API have changed and the extension configuration files must be updated accordingly:

    1. Array nodes must enclosed their children definition in ->children() ... ->end() calls:

    Before:

        $treeBuilder->root('zend', 'array')
            ->arrayNode('logger')
                ->scalarNode('priority')->defaultValue('INFO')->end()
                ->booleanNode('log_errors')->defaultFalse()->end()
            ->end();

    After:

        $treeBuilder->root('zend', 'array')
            ->children()
                ->arrayNode('logger')
                    ->children()
                        ->scalarNode('priority')->defaultValue('INFO')->end()
                        ->booleanNode('log_errors')->defaultFalse()->end()
                    ->end()
                ->end()
            ->end();

    2. The 'builder' method (in NodeBuilder) has been dropped in favor of an 'append' method (in ArrayNodeDefinition)

    Before:

        $treeBuilder->root('doctrine', 'array')
            ->arrayNode('dbal')
                ->builder($this->getDbalConnectionsNode())
            ->end();

    After:

        $treeBuilder->root('doctrine', 'array')
            ->children()
                ->arrayNode('dbal')
                    ->append($this->getDbalConnectionsNode())
                ->end()
            ->end();

    3. The root of a TreeBuilder is now an NodeDefinition (and most probably an ArrayNodeDefinition):

    Before:

        $root = $treeBuilder->root('doctrine', 'array');
        $this->addDbalSection($root);

        public function addDbalSection(NodeBuilder $node)
        {
            ...
        }

    After:

        $root = $treeBuilder->root('doctrine', 'array');
        $this->addDbalSection($root);

        public function addDbalSection(ArrayNodeDefinition $node)
        {
            ...
        }

    4. The NodeBuilder API has changed (this is seldom used):

    Before:

        $node = new NodeBuilder('connections', 'array');

    After:

    The recommended way is to use a tree builder:

        $treeBuilder = new TreeBuilder();
        $node = $treeBuilder->root('connections', 'array');

    An other way would be:

        $builder = new NodeBuilder();
        $node = $builder->node('connections', 'array');

    Some notes:

    - Tree root nodes should most always be array nodes, so this as been made the default:

        $treeBuilder->root('doctrine', 'array') is equivalent to $treeBuilder->root('doctrine')

    - There could be more than one ->children() ... ->end() sections. This could help with the readability:

        $treeBuilder->root('doctrine')
            ->children()
                ->scalarNode('default_connection')->end()
            ->end()
            ->fixXmlConfig('type')
            ->children()
                ->arrayNode('types')
                    ....
                ->end()
            ->end()