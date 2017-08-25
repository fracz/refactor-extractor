<?php

class TermTaxonomyStorage extends SingleFileStorage {

    protected $notSavedFields = array('vp_term_id', 'count', 'term_id');

    function __construct($file) {
        parent::__construct($file, 'term taxonomy', 'term_taxonomy_id');
    }

    protected function saveEntity($data, $callback = null) {
        $this->loadEntities();
        $termId = $this->findTermId($data);

        if ($termId === null)
            return;

        $taxonomyId = $data['vp_id'];

        if(!isset($this->entities[$termId]['taxonomies'])) {
            $this->entities[$termId]['taxonomies'] = array();
        }

        $originalTaxonomies = $this->entities[$termId]['taxonomies'];

        $isNew = !isset($originalTaxonomies[$taxonomyId]);

        $this->updateTaxonomy($termId, $taxonomyId, $data);

        if ($this->entities[$termId]['taxonomies'] != $originalTaxonomies) {
            $this->saveEntities();

            if (is_callable($callback))
                $callback($this->entities[$termId], $isNew ? 'create' : 'edit');
        }
    }

    function delete($restriction) {
        $taxonomyId = $restriction['vp_id'];

        $this->loadEntities();
        $termId = $this->findTermId($restriction);

        if($termId === null)
            return;
        $originalTerm = $this->entities[$termId];
        $originalTaxonomies = $originalTerm['taxonomies'];
        unset($this->entities[$termId]['taxonomies'][$taxonomyId]);
        if($this->entities[$termId]['taxonomies'] != $originalTaxonomies) {
            $this->saveEntities();
            $this->notifyOnChangeListeners($originalTerm, 'delete');
        }

    }

    public function shouldBeSaved($data) {
        return !(count($data) === 2 && isset($data['count'], $data[$this->idColumnName]));
    }

    private function findTermId($data) {
        $taxonomyId = $data['vp_id'];

        foreach ($this->entities as $termId => $term) {
            if (isset($term['taxonomies'][$taxonomyId])
                || (isset($data['vp_term_id']) && strval($term['vp_id']) == strval($data['vp_term_id'])))
                return $termId;
        }

        return null;
    }

    private function updateTaxonomy($termId, $taxonomyId, $data) {
        $taxonomies = & $this->entities[$termId]['taxonomies'];

        if (!isset($taxonomies[$taxonomyId]))
            $taxonomies[$taxonomyId] = array();

        foreach ($this->notSavedFields as $field)
            unset($data[$field]);

        foreach($data as $field => $value)
            $taxonomies[$taxonomyId][$field] = $value;
    }

    /**
     * @param $entity
     * @param $changeType
     * @return EntityChangeInfo
     */
    protected function createChangeInfo($entity, $changeType) {
        return new TermChangeInfo('edit', $entity['vp_id'], $entity['name']);
    }
}