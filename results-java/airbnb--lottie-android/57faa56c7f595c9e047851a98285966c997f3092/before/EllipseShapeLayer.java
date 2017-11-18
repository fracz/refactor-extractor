package com.airbnb.lottie.layers;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import com.airbnb.lottie.model.CircleShape;
import com.airbnb.lottie.model.ShapeFill;
import com.airbnb.lottie.model.ShapeStroke;
import com.airbnb.lottie.model.ShapeTransform;
import com.airbnb.lottie.model.ShapeTrimPath;
import com.airbnb.lottie.animatable.Observable;

class EllipseShapeLayer extends AnimatableLayer {

    private final CircleShape circleShape;
    private final ShapeFill fill;
    private final ShapeStroke stroke;
    private final ShapeTrimPath trim;
    private final ShapeTransform transformModel;

    private CircleShapeLayer fillLayer;
    private CircleShapeLayer strokeLayer;

    EllipseShapeLayer(CircleShape circleShape, ShapeFill fill, ShapeStroke stroke,
            ShapeTrimPath trim, ShapeTransform transform, long duration, Drawable.Callback callback) {
        super(duration, callback);
        this.circleShape = circleShape;
        this.fill = fill;
        this.stroke = stroke;
        this.trim = trim;
        this.transformModel = transform;

        setBounds(transform.getCompBounds());
        setAnchorPoint(transform.getAnchor().getObservable());
        setAlpha(transform.getOpacity().getObservable());
        setPosition(transform.getPosition().getObservable());
        setTransform(transform.getScale().getObservable());
        setRotation(transform.getRotation().getObservable());

        if (fill != null) {
            fillLayer = new CircleShapeLayer(getCallback());
            fillLayer.setColor(fill.getColor().getObservable());
            fillLayer.setAlpha(fill.getOpacity().getObservable());
            fillLayer.updateCircle(
                    circleShape.getPosition().getObservable(),
                    circleShape.getSize().getObservable());
            addLayer(fillLayer);
        }

        if (stroke != null) {
            strokeLayer = new CircleShapeLayer(getCallback());
            strokeLayer.setIsStroke();
            strokeLayer.setColor(stroke.getColor().getObservable());
            strokeLayer.setAlpha(stroke.getOpacity().getObservable());
            strokeLayer.setLineWidth(stroke.getWidth().getObservable());
            strokeLayer.setDashPattern(stroke.getLineDashPattern(), stroke.getDashOffset());
            strokeLayer.setLineCapType(stroke.getCapType());
            strokeLayer.updateCircle(
                    circleShape.getPosition().getObservable(),
                    circleShape.getSize().getObservable());
            if (trim != null) {
                strokeLayer.setTrimPath(trim.getStart().getObservable(), trim.getEnd().getObservable());
            }

            addLayer(strokeLayer);
        }

        buildAnimation();
    }

    private void buildAnimation() {
        if (transformModel != null) {
            addAnimation(transformModel.createAnimation());
        }

        if (stroke != null && strokeLayer != null) {
            strokeLayer.addAnimation(stroke.createAnimation());
            strokeLayer.addAnimation(circleShape.createAnimation());
            if (trim != null) {
                strokeLayer.addAnimation(trim.createAnimation());
            }
        }

        if (fill != null && fillLayer != null) {
            fillLayer.addAnimation(fill.createAnimation());
            fillLayer.addAnimation(circleShape.createAnimation());
        }
    }

    private static final class CircleShapeLayer extends ShapeLayer {
        private static final float ELLIPSE_CONTROL_POINT_PERCENTAGE = 0.55228f;

        private final Observable.OnChangedListener circleSizeChangedListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                onCircleSizeChanged();
            }
        };

        private final Observable.OnChangedListener circlePositionListener = new Observable.OnChangedListener() {
            @Override
            public void onChanged() {
                invalidateSelf();
            }
        };

        private final Paint paint = new Paint();
        private final Path path = new Path();
        private final Observable<Path> observable = new Observable<>(path);

        private Observable<PointF> circleSize;
        private Observable<PointF> circlePosition;

        CircleShapeLayer(Drawable.Callback callback) {
            super(callback);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            setPath(observable);
        }

        void updateCircle(Observable<PointF> circlePosition, Observable<PointF> circleSize) {
            if (this.circleSize != null) {
                this.circleSize.removeChangeListener(circleSizeChangedListener);
            }
            if (this.circlePosition != null)
                this.circlePosition.removeChangeListener(circlePositionListener);
            this.circleSize = circleSize;
            this.circlePosition = circlePosition;
            circleSize.addChangeListener(circleSizeChangedListener);
            circlePosition.addChangeListener(circlePositionListener);
            onCircleSizeChanged();
        }

        private void onCircleSizeChanged() {
            float halfWidth = circleSize.getValue().x / 2f;
            float halfHeight = circleSize.getValue().y / 2f;
            setBounds(0, 0, (int) halfWidth * 2, (int) halfHeight * 2);

            float cpW = halfWidth * ELLIPSE_CONTROL_POINT_PERCENTAGE;
            float cpH = halfHeight * ELLIPSE_CONTROL_POINT_PERCENTAGE;

            path.reset();
            path.moveTo(0, -halfHeight);
            path.cubicTo(0 + cpW, -halfHeight, halfWidth, 0 - cpH, halfWidth, 0);
            path.cubicTo(halfWidth, 0 + cpH, 0 + cpW, halfHeight, 0, halfHeight);
            path.cubicTo(0 - cpW, halfHeight, -halfWidth, 0 + cpH, -halfWidth, 0);
            path.cubicTo(-halfWidth, 0 - cpH, 0 - cpW, -halfHeight, 0, -halfHeight);
            observable.setValue(path);
            onPathChanged();

            invalidateSelf();
        }
    }
}