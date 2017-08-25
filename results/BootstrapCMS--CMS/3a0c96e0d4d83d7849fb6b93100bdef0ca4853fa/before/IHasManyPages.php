<?php namespace GrahamCampbell\BootstrapCMS\Models\Relations\Interfaces;

interface IHasManyPages {

    public function pages();

    public function getPages($columns = array('*'));

    public function findPage($id, $columns = array('*'));

    public function findPageBySlug($slug, $columns = array('*'));

    public function deletePages();

}