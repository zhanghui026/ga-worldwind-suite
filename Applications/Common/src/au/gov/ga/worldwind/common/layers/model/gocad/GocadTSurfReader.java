package au.gov.ga.worldwind.common.layers.model.gocad;

import gov.nasa.worldwind.geom.Position;

import java.awt.Color;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.opengl.GL;

import au.gov.ga.worldwind.common.util.FastShape;

import com.sun.opengl.util.BufferUtil;

/**
 * {@link GocadReader} implementation for reading TSurf GOCAD files.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public class GocadTSurfReader implements GocadReader
{
	public final static String HEADER_REGEX = "(?i).*tsurf.*";

	private final static Pattern vertexPattern = Pattern
			.compile("P?VRTX\\s+(\\d+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+)\\s+([\\d.\\-]+).*");
	private final static Pattern trianglePattern = Pattern.compile("TRGL\\s+(\\d+)\\s+(\\d+)\\s+(\\d+).*");
	private final static Pattern colorPattern = Pattern.compile("\\*solid\\*color:.+");
	private final static Pattern namePattern = Pattern.compile("name:\\s*(.*)\\s*");

	private List<Position> positions;
	private List<Integer> triangleIds;
	private Color color;
	private Map<Integer, Integer> vertexIdMap;
	private String name;

	@Override
	public void begin()
	{
		positions = new ArrayList<Position>();
		triangleIds = new ArrayList<Integer>();
		vertexIdMap = new HashMap<Integer, Integer>();
	}

	@Override
	public void addLine(String line)
	{
		Matcher matcher;

		matcher = vertexPattern.matcher(line);
		if (matcher.matches())
		{
			int id = Integer.parseInt(matcher.group(1));
			double x = Double.parseDouble(matcher.group(2));
			double y = Double.parseDouble(matcher.group(3));
			double z = Double.parseDouble(matcher.group(4));
			Position position = Position.fromDegrees(y, x, z);

			if (vertexIdMap.containsKey(id))
			{
				throw new IllegalArgumentException("Duplicate vertex id: " + id);
			}
			vertexIdMap.put(id, positions.size());
			positions.add(position);
			return;
		}

		matcher = trianglePattern.matcher(line);
		if (matcher.matches())
		{
			int t1 = Integer.parseInt(matcher.group(1));
			int t2 = Integer.parseInt(matcher.group(2));
			int t3 = Integer.parseInt(matcher.group(3));
			triangleIds.add(t1);
			triangleIds.add(t2);
			triangleIds.add(t3);
			return;
		}

		matcher = colorPattern.matcher(line);
		if (matcher.matches())
		{
			color = GocadColor.gocadLineToColor(line);
			return;
		}

		matcher = namePattern.matcher(line);
		if (matcher.matches())
		{
			name = matcher.group(1);
			return;
		}
	}

	@Override
	public FastShape end(URL context)
	{
		IntBuffer indicesBuffer = BufferUtil.newIntBuffer(triangleIds.size());
		for (Integer i : triangleIds)
		{
			if (!vertexIdMap.containsKey(i))
			{
				throw new IllegalArgumentException("Unknown vertex id: " + i);
			}
			indicesBuffer.put(vertexIdMap.get(i));
		}
		
		if (name == null)
		{
			name = "TSurf";
		}
		
		FastShape shape = new FastShape(positions, indicesBuffer, GL.GL_TRIANGLES);
		shape.setName(name);
		shape.setLighted(true);
		shape.setCalculateNormals(true);
		if (color != null)
		{
			shape.setColor(color);
		}
		return shape;
	}
}