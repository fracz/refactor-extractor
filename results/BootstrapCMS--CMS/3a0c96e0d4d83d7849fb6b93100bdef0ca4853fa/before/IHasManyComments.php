<?php namespace GrahamCampbell\BootstrapCMS\Models\Relations\Interfaces;

interface IHasManyComments {

    public function comments();

    public function getComments($columns = array('*'));

    public function getCommentsReversed($columns = array('*'));

    public function findComment($id, $columns = array('*'));

    public function deleteComments();

}