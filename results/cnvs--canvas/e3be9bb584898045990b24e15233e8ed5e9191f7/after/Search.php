<?php
namespace App\Services;

use App\Models\Post;
use App\Models\Tag;
use TeamTNT\TNTSearch\TNTSearch;

class Search
{
    protected $tnt;

    public function __construct(TNTSearch $tnt)
    {
        $this->tnt = $tnt;
        $this->tnt->loadConfig(config('services.tntsearch'));
    }

    public function posts()
    {
        $this->tnt->selectIndex("posts.index");
        $res = $this->tnt->search(request('search'), 12);
        return Post::whereIn('id', $res['ids'])->get();
    }

    public function tags()
    {
        $this->tnt->selectIndex("tags.index");
        $res = $this->tnt->search(request('search'), 12);
        return Tag::whereIn('id', $res['ids'])->get();
    }

}