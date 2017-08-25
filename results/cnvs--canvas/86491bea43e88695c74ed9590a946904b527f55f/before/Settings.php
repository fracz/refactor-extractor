<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Settings extends Model
{
    /**
     * The database table used by the model.
     *
     * @var string
     */
    protected $table = 'settings';

    /**
     * The attributes that are mass assignable.
     *
     * @var array
     */
    protected $fillable = ['id', 'setting_name', 'setting_value'];

    /**
     * Indicates if the model should be timestamped.
     *
     * @var bool
     */
    public $timestamps = false;

    /**
     * Get the value of the Blog Title.
     *
     * return @string
     */
    public static function blogTitle()
    {
        return $blogTitle = self::where('setting_name', 'blog_title')->pluck('setting_value')->first();
    }

    /**
     * Get the value of the Blog Subtitle.
     *
     * return @string
     */
    public static function blogSubTitle()
    {
        return $blogSubTitle = self::where('setting_name', 'blog_subtitle')->pluck('setting_value')->first();
    }

    /**
     * Get the value of the Blog Description.
     *
     * return @string
     */
    public static function blogDescription()
    {
        return $blogDescription = self::where('setting_name', 'blog_description')->pluck('setting_value')->first();
    }

    /**
     * Get the value of the Blog SEO.
     *
     * return @string
     */
    public static function blogSeo()
    {
        return $blogSeo = self::where('setting_name', 'blog_seo')->pluck('setting_value')->first();
    }

    /**
     * Get the value of the Blog SEO.
     *
     * return @string
     */
    public static function blogAuthor()
    {
        return $blogAuthor = self::where('setting_name', 'blog_author')->pluck('setting_value')->first();
    }

    /**
     * Get the value of the Disqus shortname.
     *
     * return @string
     */
    public static function disqus()
    {
        return $disqusName = self::where('setting_name', 'disqus_name')->pluck('setting_value')->first();
    }

    /**
     * Get the value of the Google Analytics Tracking ID.
     *
     * return @string
     */
    public static function gaId()
    {
        return $disqusName = self::where('setting_name', 'ga_id')->pluck('setting_value')->first();
    }
}