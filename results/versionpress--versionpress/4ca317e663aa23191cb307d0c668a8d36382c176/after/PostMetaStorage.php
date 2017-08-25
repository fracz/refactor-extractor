<?php

class PostMetaStorage extends DirectoryStorage {

    private $postMetaKey;
    private $postMetaVpId;

    private $ignoredMeta = array('_edit_lock', '_edit_last', '_pingme', '_encloseme');

    function __construct($directory) {
        parent::__construct($directory, 'post');
    }

    function save($values) {
        if(in_array($values['meta_key'], $this->ignoredMeta)) return;

        $data = $this->transformToPostField($values);

        $this->postMetaKey = $values['meta_key'];
        $this->postMetaVpId = $values['vp_id'];

        parent::save($data);
    }

    function saveAll($entities) {
        foreach ($entities as $entity) {
            $data = $this->transformToPostField($entity);
            parent::save($data);
        }
    }

    protected function createChangeInfo($oldEntity, $newEntity, $action = null) {
        $postTitle = $newEntity['post_title'];
        $postType = $newEntity['post_type'];
        $postVpId = $newEntity['vp_id'];

        return new PostMetaChangeInfo($action, $this->postMetaVpId, $postType, $postTitle, $postVpId, $this->postMetaKey);
    }

    private function transformToPostField($values) {
        $key = sprintf('%s#%s', $values['meta_key'], $values['vp_id']);
        $data = array(
            'vp_id' => $values['vp_post_id'],
            $key => $values['meta_value']
        );
        return $data;
    }
}