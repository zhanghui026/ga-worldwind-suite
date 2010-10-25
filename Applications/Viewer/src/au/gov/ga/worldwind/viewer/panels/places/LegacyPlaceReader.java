package au.gov.ga.worldwind.viewer.panels.places;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.orbit.OrbitViewInputSupport;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.w3c.dom.Element;

import au.gov.ga.worldwind.common.util.XMLUtil;

public class LegacyPlaceReader
{
	public static List<Place> readPlacesFromLegacyXML(Object source, Globe globe)
	{
		XPath xpath = XMLUtil.makeXPath();
		Element elem = XMLUtil.getElementFromSource(source);
		if (elem != null)
		{
			Element[] placeElements =
					XMLUtil.getElements(elem,
							"//object[@class=\"au.gov.ga.worldwind.viewer.panels.places.Place\"]",
							xpath);
			if (placeElements == null)
				return null;

			List<Place> places = new ArrayList<Place>();
			for (Element placeElement : placeElements)
			{
				addPlace(placeElement, places, xpath, globe);
			}
			return places;
		}
		return null;
	}

	protected static void addPlace(Element context, List<Place> places, XPath xpath, Globe globe)
	{
		Place place = new Place();
		places.add(place);
		place.setExcludeFromPlaylist(XMLUtil.getBoolean(context,
				"void[@property=\"excludeFromPlaylist\"]/boolean", place.isExcludeFromPlaylist(),
				xpath));
		place.setLabel(XMLUtil.getText(context, "void[@property=\"label\"]/string", xpath));
		Double lat = XMLUtil.getDouble(context, "void[@property=\"latitude\"]/double", xpath);
		Double lon = XMLUtil.getDouble(context, "void[@property=\"longitude\"]/double", xpath);
		LatLon latlon = lat == null || lon == null ? null : LatLon.fromDegrees(lat, lon);
		if (latlon != null)
		{
			place.setLatLon(latlon);
		}
		place.setMinZoom(XMLUtil.getDouble(context, "void[@property=\"minZoom\"]/double",
				place.getMinZoom(), xpath));
		place.setMaxZoom(XMLUtil.getDouble(context, "void[@property=\"maxZoom\"]/double",
				place.getMaxZoom(), xpath));
		place.setSaveCamera(XMLUtil.getBoolean(context, "void[@property=\"saveCamera\"]/boolean",
				place.isSaveCamera(), xpath));
		place.setVisible(XMLUtil.getBoolean(context, "void[@property=\"visible\"]/boolean",
				place.isVisible(), xpath));

		Double elevation =
				XMLUtil.getDouble(context, "void[@property=\"elevation\"]/double", xpath);
		Double heading = XMLUtil.getDouble(context, "void[@property=\"heading\"]/double", xpath);
		Double pitch = XMLUtil.getDouble(context, "void[@property=\"pitch\"]/double", xpath);
		Double zoom = XMLUtil.getDouble(context, "void[@property=\"zoom\"]/double", xpath);

		if (zoom != null && latlon != null)
		{
			if (elevation == null)
				elevation = 0d;
			if (pitch == null)
				pitch = 0d;
			if (heading == null)
				heading = 0d;

			Position center = new Position(latlon, elevation);
			Matrix transform =
					OrbitViewInputSupport.computeTransformMatrix(globe, center,
							Angle.fromDegrees(heading), Angle.fromDegrees(pitch), zoom);

			Matrix modelviewInv = transform.getInverse();
			if (modelviewInv != null)
			{
				Vec4 eyePoint = Vec4.UNIT_W.transformBy4(modelviewInv);
				Vec4 upVector = Vec4.UNIT_Y.transformBy4(modelviewInv);
				Position eyePosition = globe.computePositionFromPoint(eyePoint);
				place.setEyePosition(eyePosition);
				place.setUpVector(upVector);
			}
		}
	}
}