package jflowmap.views.timeline;

import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import jflowmap.util.piccolo.PNodes;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * @author Ilya Boyandin
 */
class Centroid extends PPath {
  private final static Font LABEL_FONT = new Font("Arial", Font.PLAIN, 8);
  private final Point2D point;
  private final double size;
  private final PText labelNode;
  private boolean isSelected;
  private final DuoTimelineView view;

  public Centroid(DuoTimelineView view, double origX, double origY, double size, Paint paint, String nodeLabel) {
    super(new Ellipse2D.Double(origX - size/2, origY - size/2, size, size));
    this.view = view;
    this.point = new Point2D.Double(origX, origY);
    this.size = size;
    this.labelNode = new PText(nodeLabel);
    addChild(labelNode);
    labelNode.setFont(LABEL_FONT);
    setStroke(null);
    updateColors();
  }

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    if (this.isSelected != selected) {
      this.isSelected = selected;
      updateColors();
    }
  }

  private void updateColors() {
    DuoTimelineStyle style = view.getStyle();
    if (isSelected) {
      setPaint(style.getMapAreaSelectedCentroidPaint());
      labelNode.setPaint(style.getMapAreaSelectedCentroidLabelPaint());
      labelNode.setTextPaint(style.getMapAreaSelectedCentroidLabelTextPaint());
    } else {
      setPaint(style.getMapAreaCentroidPaint());
      labelNode.setPaint(style.getMapAreaCentroidLabelPaint());
      labelNode.setTextPaint(style.getMapAreaCentroidLabelTextPaint());
    }
  }

  public PText getLabelNode() {
    return labelNode;
  }

//  @Override
//  public void setPaint(Paint newPaint) {
//    super.setPaint(newPaint);
//    if (labelNode != null) {
//      labelNode.setTextPaint(newPaint);
//    }
//  }

  @Override
  public void setPickable(boolean isPickable) {
    super.setPickable(isPickable);
    if (labelNode != null) {
      labelNode.setPickable(isPickable);
    }
  }

  @Override
  public boolean setBounds(double x, double y,
      double width, double height) {
    if (labelNode != null) {
//      PNodes.setPosition(labelNode, x + size*1.5, y - labelNode.getFont().getSize2D()/2.0);
      PNodes.setPosition(labelNode, x - labelNode.getWidth()/2, y + size /*- labelNode.getFont().getSize2D()/2.0*/);
    }
    return super.setBounds(x, y, width, height);
  }

  public Point2D getPoint() {
    return (Point2D) point.clone();
  }

  void updateInCamera(PCamera cam) {
    Point2D p = getPoint();
    setVisible(cam.getViewBounds().contains(p));
//    labelNode.setVisible(cam.getBounds().contains(labelNode.getFullBounds()));
    cam.viewToLocal(p);
    p.setLocation(p.getX() - size/2, p.getY() - size/2);
    PNodes.setPosition(this, p);
  }
}