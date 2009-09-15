package ch.unifr.flowmap.ui;

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * @author Ilya Boyandin
 */
public class ZoomHandler extends PDragSequenceEventHandler {

    private double minScale = 0;
    private double maxScale = Double.MAX_VALUE;
    private final double wheelZoomUnit = 0.15;
    private Point2D viewZoomPoint;

    public ZoomHandler() {
    }

    public ZoomHandler(double minScale, double maxScale) {
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    public double getMaxScale() {
        return maxScale;
    }

    public void setMaxScale(double maxScale) {
        this.maxScale = maxScale;
    }

    public double getMinScale() {
        return minScale;
    }

    public void setMinScale(double minScale) {
        this.minScale = minScale;
    }

    public Point2D getViewZoomPoint() {
        return viewZoomPoint;
    }

    public boolean isZooming() {
        return viewZoomPoint != null;
    }

    public void setViewZoomPoint(Point2D viewZoomPoint) {
        this.viewZoomPoint = viewZoomPoint;
    }

    @Override
    public void mouseWheelRotated(PInputEvent aEvent) {
        final PCamera camera = aEvent.getCamera();
        final double scaleDelta = checkScaleConstraints(camera.getViewScale(),
                1.0 - aEvent.getWheelRotation() * wheelZoomUnit);

        final Point2D position;
        if (viewZoomPoint != null) {
            position = viewZoomPoint;
        } else {
            position = aEvent.getPosition();
        }
        camera
                .scaleViewAboutPoint(scaleDelta, position.getX(), position
                        .getY());
    }

    @Override
    protected void dragActivityFirstStep(PInputEvent aEvent) {
        if (aEvent.isControlDown() && aEvent.isLeftMouseButton()) {
            if (viewZoomPoint == null) {
                viewZoomPoint = aEvent.getPosition();
            }
        }
    }

    @Override
    protected void dragActivityStep(PInputEvent aEvent) {
        if (aEvent.isControlDown() && aEvent.isLeftMouseButton()) {
            if (viewZoomPoint == null) {
                viewZoomPoint = aEvent.getPosition();
            }
            final PCamera camera = aEvent.getCamera();
            final double dy = -(aEvent.getCanvasPosition().getY() - getMousePressedCanvasPoint()
                    .getY());
            final double scaleDelta = checkScaleConstraints(camera
                    .getViewScale(), 1.0 + (0.001 * dy));

            camera.scaleViewAboutPoint(scaleDelta, viewZoomPoint.getX(),
                    viewZoomPoint.getY());
        }
    }

    private double checkScaleConstraints(final double currentScale,
            double scaleDelta) {
        final double newScale = currentScale * scaleDelta;
        final double minScale = getMinScale();
        final double maxScale = getMaxScale();
        if (newScale < minScale) {
            scaleDelta = minScale / currentScale;
        }
        if ((maxScale > 0) && (newScale > maxScale)) {
            scaleDelta = maxScale / currentScale;
        }
        return scaleDelta;
    }

    @Override
    protected void dragActivityFinalStep(PInputEvent aEvent) {
        viewZoomPoint = null;
    }

}
