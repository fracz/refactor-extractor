<?php

namespace App\Http\Controllers\Backend;

use App\Http\Controllers\Controller;
use App\Models\Post;
use App\Models\Tag;
use TeamTNT\TNTSearch\TNTSearch;

class SearchController extends Controller
{
    /**
     * Display search result.
     *
     * @return \Illuminate\View\View
     */
    public function index()
    {
        $posts = $this->searchPosts();
        $tags  = $this->searchTags();

        return view('backend.search.index', compact('posts', 'tags'));
    }

    public function searchPosts()
    {
        $tnt = new TNTSearch;
        $tnt->loadConfig(config('services.tntsearch'));
        $tnt->selectIndex("posts.index");
        $res = $tnt->search(request('search'), 12);
        return Post::whereIn('id', $res['ids'])->get();
    }

    public function searchTags()
    {
        $tnt = new TNTSearch;
        $tnt->loadConfig(config('services.tntsearch'));
        $tnt->selectIndex("tags.index");
        $res = $tnt->search(request('search'), 12);
        return Tag::whereIn('id', $res['ids'])->get();
    }
}