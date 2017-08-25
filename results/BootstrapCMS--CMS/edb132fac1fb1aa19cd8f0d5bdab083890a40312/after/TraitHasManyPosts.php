<?php namespace GrahamCampbell\BootstrapCMS\Models\Relations\Common;

trait TraitHasManyPosts {

    public function posts() {
        return $this->hasMany('GrahamCampbell\BootstrapCMS\Models\Post');
    }

    public function getPosts($columns = array('*')) {
        return $this->posts()->get($columns);
    }

    public function findPost($id, $columns = array('*')) {
        return $this->posts()->find($id, $columns);
    }

    public function deletePosts() {
        foreach($this->getPosts(array('id')) as $post) {
            $post->delete();
        }
    }
}