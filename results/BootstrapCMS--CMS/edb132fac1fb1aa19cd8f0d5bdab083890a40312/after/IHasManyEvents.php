<?php namespace GrahamCampbell\BootstrapCMS\Models\Relations\Interfaces;

interface IHasManyEvents {

    public function events();

    public function getEvents($columns = array('*'));

    public function findEvent($id, $columns = array('*'));

    public function deleteEvents();

}