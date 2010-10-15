package jflowmap.views.flowtimaps;

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
  private final static Font LABEL_FONT = new Font("Arial", Font.PLAIN, 9);
  private final Point2D point;
  private final double size;
  private final PText labelText;
  private boolean isSelected;
  private boolean isHighlighted;
  private final FlowtimapsView view;
  private final String nodeId;
  private final double origX;
  private final double origY;

  public Centroid(String nodeId, String nodeLabel, double origX, double origY,
      double size, Paint paint, FlowtimapsView view) {
//    super(new Ellipse2D.Double(origX - size/2, origY - size/2, size, size));
    super(new Ellipse2D.Double(origX, origY, size, size));
    this.origX = origX;
    this.origY = origY;
    this.view = view;
    this.point = new Point2D.Double(origX, origY);
    this.size = size;
    this.nodeId = nodeId;
    this.labelText = new PText(nodeLabel);
    addChild(labelText);
    labelText.setFont(LABEL_FONT);
    setStroke(null);
    updateColors();
  }

  public String getNodeId() {
    return nodeId;
  }

  public double getOrigX() {
    return origX;
  }

  public double getOrigY() {
    return origY;
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

  public boolean isHighlighted() {
    return isHighlighted;
  }

  public void setHighlighted(boolean highlighted) {
    if (this.isHighlighted != highlighted) {
      this.isHighlighted = highlighted;
      updateColors();
    }
  }

  private void updateColors() {
    FlowtimapsStyle style = view.getStyle();
    if (isHighlighted) {
      setPaint(style.getMapAreaHighlightedCentroidColor());
      labelText.setPaint(style.getMapAreaHighlightedCentroidLabelColor());
      labelText.setTextPaint(style.getMapAreaHighlightedCentroidLabelTextColor());
    } else if (isSelected) {
      setPaint(style.getMapAreaSelectedCentroidPaint());
      labelText.setPaint(style.getMapAreaSelectedCentroidLabelPaint());
      labelText.setTextPaint(style.getMapAreaSelectedCentroidLabelTextPaint());
    } else {
      setPaint(style.getMapAreaCentroidPaint());
      labelText.setPaint(style.getMapAreaCentroidLabelPaint());
      labelText.setTextPaint(style.getMapAreaCentroidLabelTextPaint());
    }
  }

  public PText getLabelNode() {
    return labelText;
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
    if (labelText != null) {
      labelText.setPickable(isPickable);
    }
  }

  @Override
  public boolean setBounds(double x, double y,
      double width, double height) {
    if (labelText != null) {
//      PNodes.setPosition(labelNode, x + size*1.5, y - labelNode.getFont().getSize2D()/2.0);
      PNodes.setPosition(labelText, x - labelText.getWidth()/2, y + size /*- labelNode.getFont().getSize2D()/2.0*/);
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