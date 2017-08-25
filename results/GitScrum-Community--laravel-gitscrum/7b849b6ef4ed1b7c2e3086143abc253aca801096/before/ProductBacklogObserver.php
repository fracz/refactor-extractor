<?php
/**
 * GitScrum v0.1.
 *
 * @author  Renato Marinho <renato.marinho@s2move.com>
 * @license http://opensource.org/licenses/GPL-3.0 GPLv3
 */

namespace GitScrum\Observers;

use GitScrum\Models\ProductBacklog;
use GitScrum\Models\Organization;
use GitScrum\Classes\Helper;
use Auth;

class ProductBacklogObserver
{

    public function creating(ProductBacklog $productBacklog)
    {
        $productBacklog->user_id = Auth::user()->id;
        $productBacklog->slug = Helper::slug($productBacklog->title);
        if(isset($productBacklog->is_api))
        {
            $owner = Organization::find($productBacklog->organization_id);
            $productBacklog::$tmp = app('GithubClass')->createRepository($owner->username, $productBacklog);
        }
    }

    public function created(ProductBacklog $productBacklog)
    {
        if(isset($productBacklog->is_api))
        {
            $template = app('GithubClass')->getRepositoryTemplate($productBacklog::$tmp, $productBacklog->slug);
            $obj = ProductBacklog::where('slug', $template->slug)->first();
            $obj->update(get_object_vars($template));
            $productBacklog::$tmp = null;
        }
    }

    public function updating(ProductBacklog $productBacklog)
    {
        $oldRepos = ProductBacklog::find($productBacklog->id);
        $owner = Organization::find($productBacklog->organization_id);
        $repos = app('GithubClass')->updateRepository($owner->username, $oldRepos->title, $productBacklog);
        $productBacklog->html_url = $repos->html_url;
        $productBacklog->ssh_url = $repos->ssh_url;
        $productBacklog->clone_url = $repos->clone_url;
        $productBacklog->url = $repos->url;
    }
}