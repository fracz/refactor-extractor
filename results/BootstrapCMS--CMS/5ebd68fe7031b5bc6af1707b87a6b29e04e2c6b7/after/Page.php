<?php

class Page extends BaseModel {

    protected $table = 'pages';

    public static $rules = array(
        'title'      => 'required',
        'slug'       => 'required',
        'body'       => 'required',
        'show_title' => 'required',
        'show_nav'   => 'required',
        'user_id'    => 'required',
    );

    public static $factory = array(
        'title'      => 'Page Title',
        'slug'       => 'page-title',
        'body'       => 'This is the page body!',
        'show_title' => true,
        'show_nav'   => true,
        'icon'       => '',
        'user_id'    => 1, //'factory|User'
    );

    public function getNav() {
        // TODO: caching logic
        return $this->all(array('title', 'slug', 'icon', 'show_nav'))->toArray();
    }

    /**
     * Belongs to user.
     *
     * @return BelongsTo
     */
    public function user() {
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

    /**
     * Get page by slug.
     *
     * @return Page
     */
    public function findBySlug($slug, $columns = array('*')) {
        return $this->where('slug', '=', $slug)->first($columns);
    }
}