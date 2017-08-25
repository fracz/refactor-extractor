<?php

require_once(dirname(__FILE__) . '/../../wp-load.php');

class PostSynchronizer {

    /**
     * @var EntityStorage
     */
    private $postStorage;

    /**
     * @var string
     */
    private $tableName;

    /**
     * @var wpdb
     */
    private $database;

    function __construct(EntityStorage $postStorage, wpdb $database, $tableName) {
        $this->postStorage = $postStorage;
        $this->database = $database;
        $this->tableName = $tableName;
    }

    function syncPosts() {
        $this->updatePostsInDatabase();
        $this->fixParentIds();
        $this->mirrorDatabaseToFiles();
    }

    private function updatePostsInDatabase() {
        $posts = $this->loadAllPostsFromFiles();
        $postWithoutIDs = array_map(function($post){ unset($post['ID']); return $post; }, $posts);

        foreach ($postWithoutIDs as $post) {
            $sql = $this->buildInsertWithUpdateFallbackQuery($this->tableName, $post);
            $this->database->query($sql);
        }

        $postVpIds = array_map(function($post){ return $post['vp_id'];  }, $posts);
        $sql = "DELETE FROM {$this->tableName} WHERE vp_id NOT IN (" . implode(', ', $postVpIds) . ")";
        $this->database->query($sql);
    }

    private function loadAllPostsFromFiles() {
        $posts = $this->postStorage->loadAll();
        return $posts;
    }

    private function buildInsertWithUpdateFallbackQuery($table, $data) {
        $columns = array_keys($data);
        $stringColumns = implode(', ', $columns);
        $safeValues = array_map(function($value){ return "\"$value\""; }, $data);
        $stringValues = implode(', ', $safeValues);
        $updatePairs = array_map(function($column) use ($safeValues){ return "$column = $safeValues[$column]"; }, $columns);
        $updateString = implode(', ', $updatePairs);

        $sql = "INSERT INTO {$this->tableName} ($stringColumns) VALUES ($stringValues)
                ON DUPLICATE KEY UPDATE $updateString";

        return $sql;
    }

    private function fixParentIds() {
        $sql = "SELECT ID, post_parent, vp_id, vp_parent_id FROM {$this->tableName}";
        $posts = $this->database->get_results($sql);
        $vpId_ID_map = array();
        foreach($posts as $post) {
            $vpId_ID_map[$post->vp_id] = $post->ID;
        }

        foreach($posts as $post) {
            $newParent = 0;
            if($post->vp_parent_id != 0){
                $newParent = $vpId_ID_map[$post->vp_parent_id];
            }
            if($post->post_parent != $newParent) {
                $updateSql = "UPDATE {$this->tableName} SET post_parent = $newParent WHERE ID = $post->ID";
                $this->database->query($updateSql);
            }
        }
    }

    private function mirrorDatabaseToFiles() {
        $postsInDatabase = $this->loadAllPostsFromDatabase();
        $postsInFiles = $this->loadAllPostsFromFiles();

        $getPostId = function($post){ return $post['ID']; };

        $dbPostIds = array_map($getPostId, $postsInDatabase);
        $filePostIds = array_map($getPostId, $postsInFiles);

        $deletedPostIds =  array_diff($filePostIds, $dbPostIds);

        foreach($deletedPostIds as $deletedPostId) {
            $this->postStorage->delete(array('ID' => $deletedPostId));
        }

        $this->postStorage->saveAll($postsInDatabase);
    }

    private function loadAllPostsFromDatabase() {
        $sql = "SELECT * FROM {$this->tableName}";
        return $this->database->get_results($sql, ARRAY_A);
    }
}

$storageFactory = new EntityStorageFactory(VERSIONPRESS_MIRRORING_DIR);
$postStorage = $storageFactory->getStorage('posts');

global $wpdb, $table_prefix;
$postSynchronizer = new PostSynchronizer($postStorage, $wpdb, $table_prefix . 'posts');
$postSynchronizer->syncPosts();