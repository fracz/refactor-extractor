<?php namespace Lio\Forum\Replies;

use McCool\LaravelAutoPresenter\BasePresenter;
use App, Input, Str, Request;

class ReplyPresenter extends BasePresenter
{
    public function url()
    {
        // $slug = $this->resource->slug;
        // if ( ! $slug) return '';
        // return action('ForumThreadsController@getShowThread', [$slug->slug]);
    }

    public function created_ago()
    {
        return $this->resource->created_at->diffForHumans();
    }

    public function updated_ago()
    {
        return $this->resource->updated_at->diffForHumans();
    }

    public function body()
    {
        $body = $this->resource->body;
        //$body = $this->removeDoubleSpaces($body);
        $body = $this->convertMarkdown($body);
       // $body = $this->convertNewlines($body);
        $body = $this->formatGists($body);
        $body = $this->linkify($body);
        return $body;
    }

    public function viewReplyUrl()
    {
        $slug = $this->thread->slug;
        $threadUrl = action('ForumThreadsController@getShowThread', [$slug]);
        return $threadUrl . \App::make('Lio\Forum\Replies\ReplyQueryStringGenerator')->generate($this->resource);
    }

    // ------------------- //

    private function removeDoubleSpaces($content)
    {
        return str_replace('  ', '', $content);
    }

    private function convertNewlines($content)
    {
        return preg_replace("/(?<!\\n)(\\n)(?!\\n)/", "<br>", $content);
    }

    private function convertMarkdown($content)
    {
        return App::make('Lio\Markdown\HtmlMarkdownConvertor')->convertMarkdownToHtml($content);
    }



    private function formatGists($content)
    {
        return App::make('Lio\Github\GistEmbedFormatter')->format($content);
    }

    private function linkify($content)
    {
        $linkify = new \Misd\Linkify\Linkify();
        return $linkify->process($content);
    }
}