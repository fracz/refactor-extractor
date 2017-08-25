<?php

class Post extends BaseModel implements IHasManyComments {

    use TraitHasManyComments;

    protected $table = 'posts';

    public $rules = array(
        'title'   => 'required',
        'summary'    => 'required',
        'body'    => 'required',
        'user_id' => 'required',
    );

    public $factory = array(
        'id'      => 1,
        'title'   => 'String',
        'summary'   => 'Summary of a post.',
        'body'    => 'The body of a post.',
        'user_id' => 1,
    );

    public function getTitle() {
        return $this->title;
    }

    public function getSummary() {
        return $this->summary;
    }

    public function getBody() {
        return $this->body;
    }

    public function delete() {
        foreach($this->getComments(array('id')) as $comment) {
            $comment->delete();
        }

        return parent::delete();
    }
}