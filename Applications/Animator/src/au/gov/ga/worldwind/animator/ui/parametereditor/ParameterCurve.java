package au.gov.ga.worldwind.animator.ui.parametereditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.JPanel;

import au.gov.ga.worldwind.animator.animation.parameter.Parameter;
import au.gov.ga.worldwind.animator.application.LAFConstants;
import au.gov.ga.worldwind.animator.ui.parametereditor.ParameterCurveModel.ParameterCurveModelListener;
import au.gov.ga.worldwind.animator.util.DaemonThreadFactory;
import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A class that draws the curve for a single parameter
 */
public class ParameterCurve extends JPanel implements ParameterCurveModelListener
{
	private static final long serialVersionUID = 20101102L;

	private static final RenderingHints RENDER_HINT = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(new DaemonThreadFactory("Parameter Curve Updater"));
	private static final int Y_PADDING = 10;
	private static final int NODE_SHAPE_SIZE = 8;
	
	/** The model backing this component */
	private ParameterCurveModel model;
	
	/** 
	 * The bounds used to limit which part of the curve to draw. Can be used to implement zooming etc.
	 * <p/>
	 * If <code>null</code>, will calculate the bounds to be the extent of the parameter
	 */
	private ParameterCurveBounds curveBounds;
	private Lock boundsLock = new ReentrantLock();
	
	/** Whether to show the axis for this curve */
	private boolean showAxis = false;
	
	/** Whether this curve has been destroyed */
	private boolean isDestroyed = false;
	
	private List<KeyNodeMarker> keyNodeMarkers = new ArrayList<KeyNodeMarker>();
	private ReadWriteLock keyNodeMarkersLock = new ReentrantReadWriteLock(true);
	
	private NodeMouseListener nodeMouseListener = new NodeMouseListener();
	
	public ParameterCurve(Parameter parameter)
	{
		this(parameter, null);
	}
	
	public ParameterCurve(Parameter parameter, ParameterCurveBounds curveBounds)
	{
		Validate.notNull(parameter, "A parameter is required");
		
		model = new DefaultParameterCurveModel(parameter, THREAD_POOL);
		model.addListener(this);
		
		this.curveBounds = curveBounds;
		
		setOpaque(true);
		setBackground(LAFConstants.getCurveEditorBackgroundColor());
		addMouseListener(nodeMouseListener);
		addMouseMotionListener(nodeMouseListener);
		addComponentListener(new ComponentListener());
	}

	/**
	 * Destroy's this curve. Once called, no further updates will take place for the curve.
	 */
	public void destroy()
	{
		model.destroy();
		this.isDestroyed = true;
	}
	
	/**
	 * Set the curve drawing bounds for this parameter curve
	 */
	public void setCurveBounds(ParameterCurveBounds curveBounds)
	{
		this.curveBounds = curveBounds;
		if (this.curveBounds == null)
		{
			calculateDefaultBounds();
		}
	}

	/**
	 * Set the frame bounds for this parameter curve. The value bounds will be left untouched.
	 */
	public void setCurveFrameBounds(int minFrame, int maxFrame)
	{
		if (curveBounds == null)
		{
			calculateDefaultBounds();
		}
		setCurveBounds(new ParameterCurveBounds(minFrame, maxFrame, curveBounds.getMinValue(), curveBounds.getMaxValue()));
	}
	
	/**
	 * Set the value bounds for this parameter curve. The frame bounds will be left untouched.
	 */
	public void setCurveValueBounds(double minValue, double maxValue)
	{
		if (curveBounds == null)
		{
			calculateDefaultBounds();
		}
		setCurveBounds(new ParameterCurveBounds(curveBounds.getMinFrame(), curveBounds.getMaxFrame(), minValue, maxValue));
	}
	
	/**
	 * Calculates the default bounds to use for the parameter.
	 * <p/>
	 * The default bounds [0, maxValue] - [lastFrame, minValue] 
	 */
	private void calculateDefaultBounds()
	{
		try
		{
			boundsLock.lock();
			curveBounds = new ParameterCurveBounds(0, model.getMaxFrame(), model.getMinValue(), model.getMaxValue());
		}
		finally
		{
			boundsLock.unlock();
		}
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		if (isDestroyed)
		{
			return;
		}
		
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHints(RENDER_HINT);

		try
		{
			boundsLock.lock();
			model.lock();
			if (curveBounds == null)
			{
				calculateDefaultBounds();
			}
			
			if (showAxis)
			{
				paintAxisLines(g2);
			}
			
			paintParameterCurve(g2);
			paintKeyFrameNodes(g2);
		}
		finally
		{
			boundsLock.unlock();
			model.unlock();
		}
	}
	
	private void paintAxisLines(Graphics2D g2)
	{
		// TODO Auto-generated method stub
		
	}
	
	private void paintParameterCurve(Graphics2D g2)
	{
		g2.setColor(Color.GREEN); // TODO: Make dynamic
		for (int frame = (int)curveBounds.getMinFrame(); frame < curveBounds.getMaxFrame(); frame++)
		{
			double x1 = getScreenX(frame);
			double x2 = getScreenX(frame + 1);
			double y1 = getScreenY(model.getValueAtFrame(frame));
			double y2 = getScreenY(model.getValueAtFrame(frame + 1));
			
			g2.draw(new Line2D.Double(x1, y1, x2, y2));
		}
	}

	private void paintKeyFrameNodes(Graphics2D g2)
	{
		try
		{
			updateKeyNodeMarkers();
			keyNodeMarkersLock.readLock().lock();
			for (KeyNodeMarker marker : keyNodeMarkers)
			{
				marker.paint(g2);
			}
		}
		finally
		{
			keyNodeMarkersLock.readLock().unlock();
		}
	}

	/**
	 * Maps the provided curve point to a screen point
	 */
	private Point2D.Double getScreenPoint(ParameterCurvePoint p)
	{
		System.out.println("Point: " + p);
		System.out.println("Canvas dimensions: " + getSize());
		System.out.println("Curve bounds: " + curveBounds);
		
		double screenX = getScreenX(p.frame);
		double screenY = getScreenY(p.value);

		System.out.println("Computed screen X: " + screenX);
		System.out.println("Computed screen Y: " + screenY);
		System.out.println();
		
		return new Point2D.Double(screenX, screenY);
	}
	
	/**
	 * Maps the provided frame number to a screen x-coordinate
	 */
	private double getScreenX(double frame)
	{
		return (double)getWidth() * (double)(frame - curveBounds.getMinFrame()) / (double)(curveBounds.getMaxFrame() - curveBounds.getMinFrame());
	}
	
	/**
	 * Maps the provided parameter value to a screen y-coordinate
	 */
	private double getScreenY(double parameterValue)
	{
		double h = (double)getHeight() - Y_PADDING;
		return h - (h * (parameterValue - curveBounds.getMinValue()) / (curveBounds.getMaxValue() - curveBounds.getMinValue())) + ((double)Y_PADDING / 2);
	}

	/**
	 * Maps the provided screen point to a curve point
	 */
	private ParameterCurvePoint getCurvePoint(Point2D.Double screenPoint)
	{
		return new ParameterCurvePoint(getCurveX(screenPoint.x), getCurveY(screenPoint.y));
	}
	
	/**
	 * Maps the provided screen x-coordinate to a curve frame coordinate
	 */
	private double getCurveX(double x)
	{
		return curveBounds.getMinFrame() + ((x / getWidth()) * (curveBounds.getMaxFrame() - curveBounds.getMinFrame()));
	}
	
	/**
	 * Maps the provided screen y-coordinate to a curve value coordinate
	 */
	private double getCurveY(double y)
	{
		double h = (double)getHeight() - Y_PADDING;
		double r = (y - (Y_PADDING/2d)) / h;
		double curveValuewindow = (curveBounds.getMaxValue() - curveBounds.getMinValue());
		return curveBounds.getMinValue() + (r * curveValuewindow);
	}
	
	public void setShowAxis(boolean showAxis)
	{
		this.showAxis = showAxis;
	}
	
	public void setModel(ParameterCurveModel model)
	{
		this.model = model;
	}

	@Override
	public void curveChanged()
	{
		repaint();
	}
	
	private void updateKeyNodeMarkers()
	{
		try
		{
			keyNodeMarkersLock.writeLock().lock();
			keyNodeMarkers.clear();
			for (ParameterCurveKeyNode node : model.getKeyFrameNodes())
			{
				keyNodeMarkers.add(new KeyNodeMarker(node));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			keyNodeMarkersLock.writeLock().unlock();
		}
	}
	
	private KeyNodeMarker getKeyNodeMarker(Point p)
	{
		if (p == null)
		{
			return null;
		}
		try
		{
			keyNodeMarkersLock.readLock().lock();
			for (KeyNodeMarker keyNodeMarker : keyNodeMarkers)
			{
				if (keyNodeMarker.isInMarker(p))
				{
					return keyNodeMarker;
				}
			}
			return null;
		}
		finally
		{
			keyNodeMarkersLock.readLock().unlock();
		}
	}
	
	/**
	 * A mouse listener used to detect and process keyframe node selections and drags
	 */
	private class NodeMouseListener extends MouseAdapter
	{
		private Point lastPoint;
		
		@Override
		public void mousePressed(MouseEvent e)
		{
			updateLastPoint(e);
		}
		
		@Override
		public void mouseDragged(MouseEvent e)
		{
			try
			{
				KeyNodeMarker marker = getKeyNodeMarker(lastPoint);
				if (marker == null)
				{
					return;
				}
				marker.applyHandleMove(lastPoint, e.getPoint());
			}
			finally
			{
				updateLastPoint(e);
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e)
		{
			clearLastPoint();
		}
		
		private void updateLastPoint(MouseEvent e)
		{
			lastPoint = e.getPoint();
		}
		
		private void clearLastPoint()
		{
			lastPoint = null;
		}
	}
	
	private class ComponentListener extends ComponentAdapter
	{
		@Override
		public void componentResized(ComponentEvent e)
		{
		}
	}
	
	/**
	 * A marker used to draw key frame nodes, and to respond to mouse events etc.
	 */
	private class KeyNodeMarker 
	{
		private ParameterCurveKeyNode curveNode;
		
		private Shape inHandle;
		private Shape valueHandle;
		private Shape outHandle;

		private Line2D.Double inValueJoiner;
		private Line2D.Double valueOutJoiner;
		
		KeyNodeMarker(ParameterCurveKeyNode curveNode)
		{
			Validate.notNull(curveNode, "A node is required");
			this.curveNode = curveNode;
			
			updateMarker();
		}
		
		void updateMarker()
		{
			valueHandle = createNodeShape(curveNode.getValuePoint());
			if (curveNode.isBezier() && curveNode.getInPoint() != null)
			{
				inHandle = createNodeShape(curveNode.getInPoint());
				inValueJoiner = new Line2D.Double(getScreenPoint(curveNode.getInPoint()), getScreenPoint(curveNode.getValuePoint()));
			}
			if (curveNode.isBezier() && curveNode.getOutPoint() != null)
			{
				outHandle = createNodeShape(curveNode.getOutPoint());
				valueOutJoiner = new Line2D.Double(getScreenPoint(curveNode.getOutPoint()), getScreenPoint(curveNode.getValuePoint()));
			}
		}
		
		void paint(Graphics2D g2)
		{
			g2.setColor(LAFConstants.getCurveKeyHandleColor());
			g2.draw(valueHandle);
			
			if (inHandle != null)
			{
				g2.setColor(LAFConstants.getCurveKeyHandleColor());
				g2.draw(inHandle);
				g2.setColor(LAFConstants.getCurveHandleJoinerColor());
				g2.draw(inValueJoiner);
			}
			
			if (outHandle != null)
			{
				g2.setColor(LAFConstants.getCurveKeyHandleColor());
				g2.draw(outHandle);
				g2.setColor(LAFConstants.getCurveHandleJoinerColor());
				g2.draw(valueOutJoiner);
			}
		}
		
		/**
		 * Create a node shape around the provided curve point
		 */
		private Rectangle2D.Double createNodeShape(ParameterCurvePoint p)
		{
			double x = getScreenX(p.frame);
			double y = getScreenY(p.value);
			double offset = NODE_SHAPE_SIZE / 2d;
			return new Rectangle2D.Double(x - offset, y - offset, NODE_SHAPE_SIZE, NODE_SHAPE_SIZE);
		}
		
		/**
		 * Applies a handle move, taking the handle located at lastPoint and shifting it to the new point
		 */
		public void applyHandleMove(Point lastPoint, Point point)
		{
			// TODO: Apply delta X
			int deltaY = point.y - lastPoint.y;
			
			if (valueHandle.contains(lastPoint))
			{
				Double screenPoint = getScreenPoint(curveNode.getValuePoint());
				ParameterCurvePoint curvePoint = getCurvePoint(new Point2D.Double(screenPoint.x, screenPoint.y + deltaY));
				curveNode.applyValueChange(curvePoint);
			}
			
		}
		
		/**
		 * @return Whether the provided point lies within one of the handles of this marker
		 */
		boolean isInMarker(Point point)
		{
			return valueHandle.contains(point) || (inHandle != null && inHandle.contains(point)) || (outHandle != null && outHandle.contains(point));
		}
	}
}