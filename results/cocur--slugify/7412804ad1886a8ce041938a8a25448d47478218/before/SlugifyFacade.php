<?php namespace Cocur\Slugify\Bridge\Laravel;

use Illuminate\Support\Facades\Facade;


class SlugifyFacade extends Facade {

    /**
     * Get the registered name of the component.
     *
     * @return string
     */
    protected static function getFacadeAccessor()
    {
        return 'slugify';
    }

}