<?php

namespace Glide\Manipulators;

use Glide\Request;
use Intervention\Image\Image;

class Size implements ManipulatorInterface
{
    private $maxImageSize;

    public function __construct($maxImageSize = null)
    {
        $this->maxImageSize = $maxImageSize;
    }

    public function validate(Request $request, Image $image)
    {
        $errors = array_merge(
            $this->validateWidth($request->w),
            $this->validateHeight($request->h),
            $this->validateFit($request->fit),
            $this->validateCropPosition($request->crop),
            $this->validateCropRectangle($request->rect),
            $this->validateOrientation($request->or)
        );

        if ($errors) {
            return $errors;
        }

        return $this->validateSize($request, $image);
    }

    public function validateWidth($width)
    {
        if (is_null($width)) {
            return [];
        }

        if (!ctype_digit($width)) {
            return ['w' => 'Width must be a valid number.'];
        }

        if ($width <= 0) {
            return ['w' => 'Width must be greater than 0.'];
        }

        return [];
    }

    public function validateHeight($height)
    {
        if (is_null($height)) {
            return [];
        }

        if (!ctype_digit($height)) {
            return ['h' => 'Height must be a valid number.'];
        }

        if ($height <= 0) {
            return ['h' => 'Height must be greater than 0.'];
        }

        return [];
    }

    public function validateFit($fit)
    {
        if (is_null($fit)) {
            return [];
        }

        if (!in_array($fit, ['clip', 'scale', 'crop'])) {
            return ['fit' => 'Fit only accepts "clip", "scale" or "crop".'];
        }

        return [];
    }

    public function validateCropPosition($cropPosition)
    {
        if (is_null($cropPosition)) {
            return [];
        }

        if (!in_array($cropPosition, ['top-left', 'top', 'top-right', 'left', 'center', 'right', 'bottom-left', 'bottom', 'bottom-right'])) {
            return ['crop' => 'The crop position parameter only accepts "top-left", "top", "top-right", "left", "center", "right", "bottom-left", "bottom" or "bottom-right".'];
        }

        return [];
    }

    public function validateCropRectangle($cropRectangle)
    {
        if (is_null($cropRectangle)) {
            return [];
        }

        $coordinates = explode(',', $cropRectangle);

        if (count($coordinates) !== 4) {
            return ['rect' => 'Rectangle crop requires "width", "height", "x" and "y".'];
        }

        $width = $coordinates[0];
        $height = $coordinates[1];
        $x = $coordinates[2];
        $y = $coordinates[3];

        if (!ctype_digit($width)) {
            return ['rect' => 'Rectangle crop width must be a valid number.'];
        }

        if (!ctype_digit($height)) {
            return ['rect' => 'Rectangle crop height must be a valid number.'];
        }

        if (!ctype_digit($x)) {
            return ['rect' => 'Rectangle crop x must be a valid number.'];
        }

        if (!ctype_digit($y)) {
            return ['rect' => 'Rectangle crop y must be a valid number.'];
        }

        if ($width <= 0) {
            return ['rect' => 'Rectangle crop width must be greater than 0.'];
        }

        if ($height <= 0) {
            return ['rect' => 'Rectangle crop height must be greater than 0.'];
        }

        if ($x <= 0) {
            return ['rect' => 'Rectangle crop x must be greater than 0.'];
        }

        if ($y <= 0) {
            return ['rect' => 'Rectangle crop y must be greater than 0.'];
        }

        if ($width > $image->width()) {
            return ['rect' => 'Rectangle crop width cannot be larger than the source image width.'];
        }

        if ($height > $image->height()) {
            return ['rect' => 'Rectangle crop height cannot be larger than the source image height.'];
        }

        if ($x > $image->width()) {
            return ['rect' => 'Rectangle crop x cannot be larger than the source image width.'];
        }

        if ($y > $image->height()) {
            return ['rect' => 'Rectangle crop y cannot be larger than the source image height.'];
        }

        return [];
    }

    public function validateOrientation($orientation)
    {
        if (is_null($orientation)) {
            return [];
        }

        if (!in_array($orientation, ['auto', '0', '90', '180', '270'])) {
            return ['or' => 'Orientation must be set to "auto", "0", "90", "180" or "270"'];
        }

        return [];
    }

    public function validateSize(Request $request, Image $image)
    {
        if (is_null($this->maxImageSize)) {
            return [];
        }

        $originalWidth = $image->width();
        $originalHeight = $image->height();

        if ($request->rect) {
            $coordinates = explode(',', $request->rect);
            $originalWidth = $coordinates[0];
            $originalHeight = $coordinates[1];
        }

        if ($request->w and $request->h) {
            $targetImageSize = $request->w * $request->w;
        } else if ($request->w and !$request->h) {
            $targetImageSize = $request->w * ($request->w / ($originalWidth / $originalHeight));
        } else if (!$request->w and $request->h) {
            $targetImageSize = ($request->h * ($originalWidth / $originalHeight)) * $request->h;
        } else {
            $targetImageSize = $originalWidth * $originalHeight;
        }

        if ($targetImageSize > $this->maxImageSize) {
            return ['size' => 'Image exceeds the maximum allowed size of ' . $this->maxImageSize . ' pixels.'];
        }

        return [];
    }

    public function run(Request $request, Image $image)
    {
        if ($request->rect) {
            $image->crop(explode(',', $request->rect));
        }

        if ($request->w or $request->h) {
            if (is_null($request->fit) or $request->fit === 'clip') {
                $image->resize(
                    $request->w,
                    $request->h,
                    function ($constraint) {
                        $constraint->aspectRatio();
                    }
                );
            } else if ($request->fit === 'scale') {
                $image->resize(
                    $request->w,
                    $request->h
                );
            } else if ($request->fit === 'crop') {
                $image->fit(
                    $request->w,
                    $request->h,
                    function ($constraint) {
                    },
                    $request->crop ?: 'center'
                );
            }
        }

        if (in_array($request->or, [null, 'auto'])) {
            $image->orientate();
        } else if (in_array($request->or, ['90', '180', '270'])) {
            $image->rotate($request->or);
        }
    }
}