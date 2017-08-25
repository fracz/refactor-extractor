<?php

namespace Glide\API;

use Glide\Request;
use Intervention\Image\Image;
use Intervention\Image\ImageManager;

class API implements APIInterface
{
    private $manager;
    private $manipulators;

    public function __construct(ImageManager $manager, $maxImageSize = null)
    {
        $this->manager = $manager;

        $this->manipulators = [
            'adjustments' => new Manipulators\Adjustments(),
            'size' => new Manipulators\Size($maxImageSize),
            'effects' => new Manipulators\Effects(),
            'output' => new Manipulators\Output(),
        ];
    }

    public function run(Request $request, $imageSource)
    {
        foreach ($request->getParams() as $name => $value) {
            if (method_exists($this, $name) and !in_array($name, ['__construct', 'run'])) {
                $this->$name($value);
            }
        }

        $image = $this->manager->make($imageSource);

        foreach ($this->manipulators as $manipulator) {
            $image = $manipulator->run($image);
        }

        return $image;
    }

    public function bri($value)
    {
        $this->manipulators['adjustments']->setBrightness($value);
    }

    public function con($value)
    {
        $this->manipulators['adjustments']->setContrast($value);
    }

    public function gam($value)
    {
        $this->manipulators['adjustments']->setGamma($value);
    }

    public function sharp($value)
    {
        $this->manipulators['adjustments']->setSharpen($value);
    }

    public function w($value)
    {
        $this->manipulators['size']->setWidth($value);
    }

    public function h($value)
    {
        $this->manipulators['size']->setHeight($value);
    }

    public function fit($value)
    {
        $this->manipulators['size']->setFit($value);
    }

    public function crop($value)
    {
        $this->manipulators['size']->setCropPosition($value);
    }

    public function rect($value)
    {
        $this->manipulators['size']->setCropRectangle($value);
    }

    public function ori($value)
    {
        $this->manipulators['size']->setOrientation($value);
    }

    public function filt($value)
    {
        $this->manipulators['effects']->setFilter($value);
    }

    public function blur($value)
    {
        $this->manipulators['effects']->setBlur($value);
    }

    public function pixel($value)
    {
        $this->manipulators['effects']->setPixelate($value);
    }

    public function fm($value)
    {
        $this->manipulators['output']->setFormat($value);
    }

    public function q($value)
    {
        $this->manipulators['output']->setQuality($value);
    }
}