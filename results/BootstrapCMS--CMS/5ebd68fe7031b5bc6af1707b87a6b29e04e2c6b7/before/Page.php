<?php

class Page extends BaseModel {

    protected $table = 'pages';

    public static $rules = array(
        'title' => 'required',
        'slug' => 'required',
        'body' => 'required',
        'show_title' => 'required',
        'show_nav' => 'required',
        'user_id' => 'required'
        );

    public static $factory = array(
        'title' => 'Page Title',
        'slug' => 'page-title',
        'body' => 'This is the page body!',
        'show_title' => true,
        'show_nav' => true,
        'icon' => '',
        'user_id' => 1
    );

    /**
     * Belongs to user.
     *
     * @return BelongsTo
     */
    public function user()
    {
        return $this->belongsTo('User');
    }

    /**
     * Get user.
     *
     * @return User
     */
    public function getUser($columns = array('*')) {
        return $this->user()->first($columns);
    }
}