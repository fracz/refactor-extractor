<?php

/**
 * Saves entities to files in a common directory. Useful for entities that either
 * expect a lot of instance of them (posts, comments etc.) or have variable length
 * and some may be rather large (again, e.g. posts).
 *
 * For example, posts are stored as <vpid>.ini in the `vpdb/posts` folder.
 *
 * Note that the same file can be used by multiple entities. For example, both
 * the main post data and postmeta for it are stored in the same INI file.
 */
abstract class DirectoryStorage extends Storage {

    /** @var string */
    private $directory;

    protected $entityTypeName;

    protected $idColumnName;

    /** @var EntityFilter[] */
    private $filters = array();

    function __construct($directory, $entityTypeName, $idColumnName = 'ID') {
        $this->directory = $directory;
        $this->entityTypeName = $entityTypeName;
        $this->idColumnName = $idColumnName;
    }

    function save($data) {
        $this->saveEntity($data, array($this, 'notifyChangeListeners'));
    }

    function delete($restriction) {
        $fileName = $this->getEntityFilename($restriction['vp_id']);
        if (is_file($fileName)) {
            $entity = $this->loadEntity($restriction['vp_id']);
            unlink($fileName);
            $this->notifyChangeListeners($entity, $entity, 'delete');
        }
    }

    function loadAll() {
        $entityFiles = $this->getEntityFiles();
        $entities = $this->loadAllFromFiles($entityFiles);
        return $entities;
    }

    function saveAll($entities) {
        foreach ($entities as $entity) {
            $this->saveEntity($entity);
        }
    }

    public function shouldBeSaved($data) {
        return true;
    }

    public function prepareStorage() {
        @mkdir($this->directory, 0777, true);
    }

    public function getEntityFilename($id) {
        return $this->directory . '/' . $id . '.ini';
    }

    private function deserializeEntity($serializedEntity) {
        return IniSerializer::deserialize($serializedEntity);
    }

    private function serializeEntity($entity) {
        return IniSerializer::serializeFlatData($entity);
    }

    private function getEntityFiles() {
        if (!is_dir($this->directory))
            return array();
        $excludeList = array('.', '..');
        $files = scandir($this->directory);

        $directory = $this->directory;
        return array_map(function($filename) use ($directory) { return $directory . '/' . $filename; }, array_diff($files, $excludeList));
    }

    private function loadAllFromFiles($entityFiles) {
        $entities = array();
        $indexedEntities = array();

        foreach ($entityFiles as $file) {
            $entities[] = $this->deserializeEntity(file_get_contents($file));
        }

        foreach($entities as $entity) {
            $indexedEntities[$entity['vp_id']] = $entity;
        }

        return $indexedEntities;
    }

    protected function removeUnwantedColumns($entity) {
        return $entity;
    }

    protected function notifyChangeListeners($oldEntity, $newEntity, $changeType) {
        $changeInfo = $this->createChangeInfo($oldEntity, $newEntity, $changeType);
        $this->callOnChangeListeners($changeInfo);
    }

    protected abstract function createChangeInfo($oldEntity, $newEntity, $changeType);

    protected function saveEntity($data, $callback = null) {
        $id = $data['vp_id'];

        if (!$id)
            return;

        unset($data[$this->idColumnName]);
        $data = $this->removeUnwantedColumns($data);
        $data = $this->applyFilters($data);

        $filename = $this->getEntityFilename($id);
        $oldSerializedEntity = "";
        $isExistingEntity = $this->isExistingEntity($id);

        if (!$this->shouldBeSaved($data))
            return;

        if ($isExistingEntity) {
            $oldSerializedEntity = file_get_contents($filename);
        }

        $entity = $this->deserializeEntity($oldSerializedEntity);
        if (isset($entity['vp_id']))
            unset($data['vp_id']);

        $diff = array();
        foreach ($data as $key => $value) {
            if (!isset($entity[$key]) || (isset($entity[$key]) && $entity[$key] != $value)) // not present or different value
                $diff[$key] = $value;
        }


        if (count($diff) > 0) {
            $oldEntity = $entity;
            $newEntity = array_merge($entity, $diff);
            file_put_contents($filename, $this->serializeEntity($newEntity));
            if (is_callable($callback))
                call_user_func($callback, $oldEntity, $newEntity, $isExistingEntity ? $this->getEditAction($diff, $oldEntity, $newEntity) : 'create');
        }
    }

    protected function isExistingEntity($id) {
        return file_exists($this->getEntityFilename($id));
    }

    /**
     * @return string
     */
    protected function getEditAction($diff, $oldEntity, $newEntity) {
        return 'edit';
    }

    private function loadEntity($vpid) {
        $entities = $this->loadAllFromFiles(array($this->getEntityFilename($vpid)));
        return $entities[$vpid];
    }

    protected function applyFilters($data) {
        foreach ($this->filters as $filter) {
            $data = $filter->apply($data);
        }

        return $data;
    }

    protected function addFilter(EntityFilter $filter) {
        $this->filters[] = $filter;
    }
}