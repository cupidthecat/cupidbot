package net.runelite.client.plugins.cupidbot.util.mouse.engine;

import net.runelite.api.Point;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.Objects;
import java.util.Random;

public final class MouseTarget
{
	public enum Shape
	{
		POINT,
		RECTANGLE,
		POLYGON
	}

	private final Shape shape;
	private final Point center;
	private final Rectangle bounds;
	private final Polygon polygon;

	private MouseTarget(Shape shape, Point center, Rectangle bounds, Polygon polygon)
	{
		this.shape = Objects.requireNonNull(shape, "shape");
		this.center = Objects.requireNonNull(center, "center");
		this.bounds = new Rectangle(Objects.requireNonNull(bounds, "bounds"));
		this.polygon = polygon == null ? null : new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
	}

	public static MouseTarget point(Point point)
	{
		Point safe = point == null ? new Point(1, 1) : point;
		return new MouseTarget(
			Shape.POINT,
			safe,
			new Rectangle(safe.getX(), safe.getY(), 1, 1),
			null);
	}

	public static MouseTarget rectangle(Rectangle rectangle)
	{
		Rectangle safe = sanitize(rectangle);
		return new MouseTarget(
			Shape.RECTANGLE,
			new Point((int) safe.getCenterX(), (int) safe.getCenterY()),
			safe,
			null);
	}

	public static MouseTarget polygon(Polygon polygon)
	{
		if (polygon == null || polygon.npoints == 0)
		{
			return point(new Point(1, 1));
		}
		Rectangle bounds = sanitize(polygon.getBounds());
		return new MouseTarget(
			Shape.POLYGON,
			new Point((int) bounds.getCenterX(), (int) bounds.getCenterY()),
			bounds,
			polygon);
	}

	public Shape getShape()
	{
		return shape;
	}

	public Point getCenter()
	{
		return center;
	}

	public Rectangle getBounds()
	{
		return new Rectangle(bounds);
	}

	public double getEffectiveWidth()
	{
		if (shape == Shape.POINT)
		{
			return 1.0;
		}
		return Math.max(1.0, Math.min(bounds.getWidth(), bounds.getHeight()));
	}

	public boolean contains(Point point)
	{
		if (point == null)
		{
			return false;
		}
		if (shape == Shape.POINT)
		{
			return center.equals(point);
		}
		if (!bounds.contains(point.getX(), point.getY()))
		{
			return false;
		}
		return polygon == null || polygon.contains(point.getX(), point.getY());
	}

	public Point samplePoint(Random random, int edgeInset, boolean forceCenter)
	{
		if (forceCenter || shape == Shape.POINT)
		{
			return center;
		}

		Random rng = random == null ? new Random() : random;
		Rectangle safe = insetBounds(edgeInset);
		for (int i = 0; i < 16; i++)
		{
			int x = safe.x + rng.nextInt(Math.max(1, safe.width));
			int y = safe.y + rng.nextInt(Math.max(1, safe.height));
			Point candidate = new Point(x, y);
			if (contains(candidate))
			{
				return candidate;
			}
		}
		return center;
	}

	private Rectangle insetBounds(int edgeInset)
	{
		int inset = Math.max(0, Math.min(edgeInset, Math.min(bounds.width, bounds.height) / 3));
		int width = Math.max(1, bounds.width - inset * 2);
		int height = Math.max(1, bounds.height - inset * 2);
		return new Rectangle(bounds.x + inset, bounds.y + inset, width, height);
	}

	private static Rectangle sanitize(Rectangle rectangle)
	{
		if (rectangle == null)
		{
			return new Rectangle(1, 1, 1, 1);
		}
		int width = Math.max(1, rectangle.width);
		int height = Math.max(1, rectangle.height);
		return new Rectangle(rectangle.x, rectangle.y, width, height);
	}
}
