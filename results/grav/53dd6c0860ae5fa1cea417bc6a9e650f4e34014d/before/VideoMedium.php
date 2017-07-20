<?php
namespace Grav\Common\Page\Medium;

use Grav\Common\Config\Config;
use Grav\Common\File\CompiledYamlFile;
use Grav\Common\Grav;
use Grav\Common\GravTrait;
use Grav\Common\Data\Blueprint;
use Grav\Common\Data\Data;
use Gregwar\Image\Image as ImageFile;

class VideoMedium extends Medium
{
    /**
     * @var int
     */
    protected $width = null;

    /**
     * @var int
     */
    protected $height = null;

    public function __construct($items = array(), Blueprint $blueprint = null)
    {
        parent::__construct($items, $blueprint);

        $this->attributes['controls'] = true;
    }

    public function sourceParsedownElement($attributes, $reset)
    {
        $attributes = $this->attributes;
        $location = $this->url($reset);

        !empty($this->width) && $attributes['width'] = $this->width;
        !empty($this->height) && $attributes['height'] = $this->height;

        return [
            'name' => 'video',
            'text' => '<source src="' . $location . '">Your browser does not support the video tag.',
            'attributes' => $attributes
        ];
    }

    /**
     * Enable link for the medium object.
     *
     * @param null $width
     * @param null $height
     * @return $this
     */
    public function link($reset = true)
    {
        if ($this->mode !== 'source') {
            $this->display('source');
        }

        !empty($this->width) && $this->linkAttributes['data-width'] = $this->width;
        !empty($this->height) && $this->linkAttributes['data-height'] = $this->height;

        return parent::link($reset);
    }

    protected function resize($width = null, $height = null)
    {
        $this->attributes['width'] = $width;
        $this->attributes['height'] = $height;

        return $this;
    }
}