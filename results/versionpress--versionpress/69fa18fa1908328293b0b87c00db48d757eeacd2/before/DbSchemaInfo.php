<?php
namespace VersionPress\Database;
use DateTime;
use Nette\Neon\Neon;
use Nette\Neon\Entity;

/**
 * Describes parts of the DB schema, specifically telling how to identify entities
 * and what are the relationships between them. The information is loaded from a *.neon file
 * which is described in `schema-readme.md`.
 */
class DbSchemaInfo {

    /**
     * Parsed NEON schema - to see what it looks like, paste the NEON into {@link http://ne-on.org/ne-on.org}).
     * Parsed in constructor.
     *
     * @var array|int|mixed|DateTime|Entity|null|string
     */
    private $schema;

    /**
     * Database tables prefix, e.g. "wp_"
     *
     * @var string
     */
    private $prefix;

    /**
     * @var array entityName => EntityInfo object. Lazily constructed, see getEntityInfo().
     */
    private $entityInfoRegistry;

    /**
     * @param string $schemaFile Path to a *.neon file to read from disk
     * @param string $prefix
     */
    function __construct($schemaFile, $prefix) {
        $neonSchema = file_get_contents($schemaFile);
        $this->schema = Neon::decode($neonSchema);
        $this->prefix = $prefix;
    }

    /**
     * Returns EntityInfo for a given entity name (e.g., "post" or "comment")
     *
     * @param $entityName
     * @return EntityInfo
     */
    public function getEntityInfo($entityName) {
        if (!isset($this->entityInfoRegistry[$entityName])) {
            $this->entityInfoRegistry[$entityName] = new EntityInfo(array($entityName => $this->schema[$entityName]));
        }

        return $this->entityInfoRegistry[$entityName];
    }


    /**
     * Gets all entities defined by the schema
     *
     * @return array
     */
    public function getAllEntityNames() {
        return array_keys($this->schema);
    }

    /**
     * For something like "post", returns "posts"
     *
     * @param $entityName
     * @return string
     */
    public function getTableName($entityName) {
        $tableName = $this->isEntity($entityName) ? $this->getEntityInfo($entityName)->tableName : $entityName;
        return $tableName;
    }

    /**
     * For something like "post", returns "wp_posts"
     *
     * @param $entityName
     * @return string
     */
    public function getPrefixedTableName($entityName) {
        return $this->prefix . $this->getTableName($entityName);
    }

    /**
     * Returns EntityInfo for a given table name (e.g., "posts" or "commentmeta")
     *
     * @param $tableName
     * @return EntityInfo
     */
    public function getEntityInfoByTableName($tableName) {
        $entityNames = $this->getAllEntityNames();
        foreach ($entityNames as $entityName) {
            $entityInfo = $this->getEntityInfo($entityName);
            if ($entityInfo->tableName === $tableName)
                return $entityInfo;
        }
        return null;
    }

    /**
     * Returns true if given name is an entity (is defined in schema).
     * Useful for prefixing VP tables.
     *
     * @param $entityOrTableName
     * @return bool
     */
    private function isEntity($entityOrTableName) {
        return in_array($entityOrTableName, $this->getAllEntityNames());
    }
}