<?php
namespace VersionPress\Database;

use VersionPress\Storages\Mirror;

/**
 * Bridge between hooks in {@see wpdb} and {@see Mirror}. Transforms WordPress data to the form suitable for Mirror.
 * Especially, it transforms WP ids to VPIDs.
 */
class WpdbMirrorBridge {

    /**
     * @var Mirror
     */
    private $mirror;

    /**
     * @var DbSchemaInfo
     */
    private $dbSchemaInfo;

    /**
     * @var \wpdb
     */
    private $database;
    /**
     * @var VpidRepository
     */
    private $vpidRepository;

    /** @var bool */
    private $disabled;

    function __construct($wpdb, Mirror $mirror, DbSchemaInfo $dbSchemaInfo, VpidRepository $vpidRepository) {
        $this->database = $wpdb;
        $this->mirror = $mirror;
        $this->dbSchemaInfo = $dbSchemaInfo;
        $this->vpidRepository = $vpidRepository;
    }

    function insert($table, $data) {
        if ($this->disabled) {
            return;
        }

        $id = $this->database->insert_id;
        $entityInfo = $this->dbSchemaInfo->getEntityInfoByPrefixedTableName($table);

        if (!$entityInfo) {
            return;
        }

        $entityName = $entityInfo->entityName;
        $data = $this->vpidRepository->replaceForeignKeysWithReferences($entityName, $data);
        $shouldBeSaved = $this->mirror->shouldBeSaved($entityName, $data);

        if (!$shouldBeSaved) {
            return;
        }

        $data = $this->vpidRepository->identifyEntity($entityName, $data, $id);
        $this->mirror->save($entityName, $data);
    }

    function update($table, $data, $where) {
        if ($this->disabled) {
            return;
        }

        $entityInfo = $this->dbSchemaInfo->getEntityInfoByPrefixedTableName($table);

        if (!$entityInfo) {
            return;
        }

        $entityName = $entityInfo->entityName;
        $data = array_merge($where, $data);

        if (!$entityInfo->usesGeneratedVpids) { // options etc.
            $data = $this->vpidRepository->replaceForeignKeysWithReferences($entityName, $data);
            $this->mirror->save($entityName, $data);
            return;
        }

        $ids = $this->detectAllAffectedIds($entityName, $data, $where);
        $data = $this->vpidRepository->replaceForeignKeysWithReferences($entityName, $data);

        foreach ($ids as $id) {
            $this->updateEntity($data, $entityName, $id);
        }
    }

    function delete($table, $where) {
        if ($this->disabled) {
            return;
        }

        $entityInfo = $this->dbSchemaInfo->getEntityInfoByPrefixedTableName($table);

        if (!$entityInfo)
            return;

        $entityName = $entityInfo->entityName;

        if (!$entityInfo->usesGeneratedVpids) {
            $this->mirror->delete($entityName, $where);
            return;
        }

        $ids = $this->detectAllAffectedIds($entityName, $where, $where);

        foreach ($ids as $id) {
            $where['vp_id'] = $this->vpidRepository->getVpidForEntity($entityName, $id);
            if (!$where['vp_id']) {
                continue; // already deleted - deleting postmeta is sometimes called twice
            }

            if ($this->dbSchemaInfo->isChildEntity($entityName) && !isset($where["vp_{$entityInfo->parentReference}"])) {
                $where = $this->fillParentId($entityName, $where, $id);
            }

            $this->vpidRepository->deleteId($entityName, $id);
            $this->mirror->delete($entityName, $where);
        }
    }

    /**
     * @param $parsedQueryData ParsedQueryData
     */
    function query($parsedQueryData) {
        if ($this->disabled) {
            return;
        }

        $entityInfo = $this->dbSchemaInfo->getEntityInfoByPrefixedTableName($parsedQueryData->table);
        $entityName = $entityInfo->entityName;

        if (!$entityInfo)
            return;
        $usesSqlFunctions = $parsedQueryData->usesSqlFunctions;

        switch ($parsedQueryData->queryType) {
            case ParsedQueryData::UPDATE_QUERY:
                $this->processUpdateQuery($parsedQueryData);
                break;
            case  ParsedQueryData::DELETE_QUERY:
                $this->processDeleteQuery($parsedQueryData, $entityInfo);
                break;
            case ParsedQueryData::INSERT_QUERY:
                if ($usesSqlFunctions) {
                    $this->processInsertQueryWithSqlFunctions($parsedQueryData, $entityName);
                } else {
                    $this->processInsertQueryWithoutSqlFunctions($parsedQueryData);
                }
                break;
            case ParsedQueryData::INSERT_UPDATE_QUERY:
                $this->processInsertUpdateQuery($parsedQueryData);
                break;
        }


    }

    /**
     * Returns all ids from DB suitable for given restriction.
     * E.g. all comment_id values where comment_post_id = 1
     * @param string $entityName
     * @param array $where
     * @return array
     */
    private function getIdsForRestriction($entityName, $where) {
        $idColumnName = $this->dbSchemaInfo->getEntityInfo($entityName)->idColumnName;
        $table = $this->dbSchemaInfo->getPrefixedTableName($entityName);

        $sql = "SELECT {$idColumnName} FROM {$table} WHERE ";
        $sql .= join(
            " AND ",
            array_map(
                function ($column) {
                    return "`$column` = %s";
                },
                array_keys($where)
            )
        );
        $ids = $this->database->get_col($this->database->prepare($sql, $where));
        return $ids;
    }

    private function updateEntity($data, $entityName, $id) {
        $vpId = $this->vpidRepository->getVpidForEntity($entityName, $id);

        $data['vp_id'] = $vpId;

        if ($this->dbSchemaInfo->isChildEntity($entityName)) {
            $entityInfo = $this->dbSchemaInfo->getEntityInfo($entityName);
            $parentVpReference = "vp_" . $entityInfo->parentReference;
            if (!isset($data[$parentVpReference])) {
                $table = $this->dbSchemaInfo->getPrefixedTableName($entityName);
                $parentTable = $this->dbSchemaInfo->getTableName($entityInfo->references[$entityInfo->parentReference]);
                $vpidTable = $this->dbSchemaInfo->getPrefixedTableName('vp_id');
                $parentVpidSql = "SELECT HEX(vpid.vp_id) FROM {$table} t JOIN {$vpidTable} vpid ON t.{$entityInfo->parentReference} = vpid.id AND `table` = '{$parentTable}' WHERE {$entityInfo->idColumnName} = $id";
                $parentVpid = $this->database->get_var($parentVpidSql);
                $data[$parentVpReference] = $parentVpid;
            }
        }

        $shouldBeSaved = $this->mirror->shouldBeSaved($entityName, $data);
        if (!$shouldBeSaved) {
            return;
        }

        $savePostmeta = !$vpId && $entityName === 'post'; // the post exists in DB for a while but until now it wasn't tracked, so we have to save its postmeta

        if (!$vpId) {
            $data = $this->vpidRepository->identifyEntity($entityName, $data, $id);
        }

        $this->mirror->save($entityName, $data);

        if (!$savePostmeta) {
            return;
        }

        $postmeta = $this->database->get_results("SELECT meta_id, meta_key, meta_value FROM {$this->database->postmeta} WHERE post_id = {$id}", ARRAY_A);
        foreach ($postmeta as $meta) {
            $meta['vp_post_id'] = $data['vp_id'];

            $meta = $this->vpidRepository->replaceForeignKeysWithReferences('postmeta', $meta);
            if (!$this->mirror->shouldBeSaved('postmeta', $meta)) {
                continue;
            }

            $meta = $this->vpidRepository->identifyEntity('postmeta', $meta, $meta['meta_id']);
            $this->mirror->save('postmeta', $meta);
        }
    }

    /**
     * Returns all database IDs matching the restriction.
     * In most cases it returns ID from $where array.
     * For meta-entities it can find the ID by key and parent entity ID, if
     * the ID is missing in the $where array.
     * For all other cases see {@link WpdbMirrorBridge::getIdsForRestriction}.
     *
     * @param $entityName
     * @param $data
     * @param $where
     * @return array List of ids
     */
    private function detectAllAffectedIds($entityName, $data, $where) {
        $idColumnName = $this->dbSchemaInfo->getEntityInfo($entityName)->idColumnName;

        if (isset($where[$idColumnName])) {
            return array($where[$idColumnName]);
        }

        return $this->getIdsForRestriction($entityName, $where);
    }

    private function fillParentId($metaEntityName, $where, $id) {
        $entityInfo = $this->dbSchemaInfo->getEntityInfo($metaEntityName);
        $parentReference = $entityInfo->parentReference;

        $parent = $entityInfo->references[$parentReference];
        $vpIdTable = $this->dbSchemaInfo->getPrefixedTableName('vp_id');
        $entityTable = $this->dbSchemaInfo->getPrefixedTableName($metaEntityName);
        $parentTable = $this->dbSchemaInfo->getTableName($parent);
        $idColumnName = $this->dbSchemaInfo->getEntityInfo($metaEntityName)->idColumnName;

        $where["vp_{$parentReference}"] = $this->database->get_var("SELECT HEX(vp_id) FROM $vpIdTable WHERE `table` = '{$parentTable}' AND ID = (SELECT {$parentReference} FROM $entityTable WHERE {$idColumnName} = $id)");
        return $where;
    }

    /**
     * Disables all actions. Useful for deactivating VersionPress.
     */
    public function disable() {
        $this->disabled = true;
    }

    /**
     * Processes ParsedQueryData from UPDATE query and stores updated entity/entities data into Storage.
     *
     * @param $parsedQueryData ParsedQueryData
     * @param $entityName
     */
    private function processUpdateQuery($parsedQueryData) {

        foreach ($parsedQueryData->ids as $id) {
            $stringifiedId = "'" . $id . "'";
            $data = $this->database->get_results("SELECT * FROM {$parsedQueryData->table} WHERE {$parsedQueryData->idColumn} = {$stringifiedId}", ARRAY_A)[0];
            $data = $this->vpidRepository->replaceForeignKeysWithReferences($parsedQueryData->entityName, $data);
            $this->updateEntity($data, $parsedQueryData->entityName, $stringifiedId);
        }
    }

    /**
     * Process ParsedQueryData from DELETE query and deletes entity/entities data from Storage.
     * Source parsed query does not contain any special Sql functions (e.g. NOW)
     *
     * @param $parsedQueryData ParsedQueryData
     * @param $entityInfo
     */
    private function processDeleteQuery($parsedQueryData, $entityInfo) {
        if (!$entityInfo->usesGeneratedVpids) {
            foreach ($parsedQueryData->ids as $id) {
                $stringifiedId = "'" . $id . "'";
                $where[$parsedQueryData->idColumn] = $stringifiedId;
                $this->vpidRepository->deleteId($parsedQueryData->entityName, $stringifiedId);
                $this->mirror->delete($parsedQueryData->entityName, $where);
            }
            return;
        }
        foreach ($parsedQueryData->ids as $id) {
            $stringifiedId = "'" . $id . "'";
            $where['vp_id'] = $this->vpidRepository->getVpidForEntity($parsedQueryData->entityName, $id);
            if (!$where['vp_id']) {
                continue; // already deleted - deleting postmeta is sometimes called twice
            }

            if ($this->dbSchemaInfo->isChildEntity($parsedQueryData->entityName) && !isset($where["vp_{$entityInfo->parentReference}"])) {
                $where = $this->fillParentId($parsedQueryData->entityName, $where, $id);
            }

            $this->vpidRepository->deleteId($parsedQueryData->entityName, $stringifiedId);
            $this->mirror->delete($parsedQueryData->entityName, $where);
        }
    }

    /**
     * Process ParsedQueryData from INSERT query and stores affected entity into Storage.
     * Source parsed query does not contain any special Sql functions (e.g. NOW)
     *
     * @param $parsedQueryData ParsedQueryData
     */
    private function processInsertQueryWithoutSqlFunctions($parsedQueryData) {


        $id = $this->database->insert_id;
        $entitiesCount = count($parsedQueryData->data);

        for ($i = 0; $i < $entitiesCount; $i++) {
            $data = $this->vpidRepository->replaceForeignKeysWithReferences($parsedQueryData->entityName, $parsedQueryData->data[$i]);
            $shouldBeSaved = $this->mirror->shouldBeSaved($parsedQueryData->entityName, $data);

            if (!$shouldBeSaved) {
                continue;
            }
            $data = $this->vpidRepository->identifyEntity($parsedQueryData->entityName, $data, ($id - $i));
            $this->mirror->save($parsedQueryData->entityName, $data);
        }
    }

    private function processInsertQueryWithSqlFunctions($parsedQueryData, $entityName) {

    }

    /**
     * Processes ParsedQueryData from INSERT ... ON DUPLICATE UPDATE query and stores changes into Storage
     *
     * @param $parsedQueryData ParsedQueryData
     */
    private function processInsertUpdateQuery($parsedQueryData) {

        if ($parsedQueryData->ids != 0) {
            $id = $parsedQueryData->ids;
            $data = $this->vpidRepository->replaceForeignKeysWithReferences($parsedQueryData->entityName, $parsedQueryData->data[0]);
            $shouldBeSaved = $this->mirror->shouldBeSaved($parsedQueryData->entityName, $data);

            if (!$shouldBeSaved) {
                return;
            }
            $data = $this->vpidRepository->identifyEntity($parsedQueryData->entityName, $data, $id);
            $this->mirror->save($parsedQueryData->entityName, $data);
        } else {
            $data = $this->database->get_results($parsedQueryData->query, ARRAY_A)[0];
            $stringifiedId = "'" . $data[$parsedQueryData->idColumn] . "'";
            $data = $this->vpidRepository->replaceForeignKeysWithReferences($parsedQueryData->entityName, $data);
            $this->updateEntity($data, $parsedQueryData->entityName, $stringifiedId);
        }

    }


}

