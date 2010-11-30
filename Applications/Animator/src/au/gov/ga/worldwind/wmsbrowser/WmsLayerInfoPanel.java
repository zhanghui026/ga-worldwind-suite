package au.gov.ga.worldwind.wmsbrowser;

import static au.gov.ga.worldwind.common.util.message.MessageSourceAccessor.getMessage;
import static au.gov.ga.worldwind.wmsbrowser.util.message.WmsBrowserMessageConstants.*;
import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerInfoURL;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import gov.nasa.worldwindow.core.WMSLayerInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import nasa.worldwind.awt.WorldWindowGLCanvas;
import au.gov.ga.worldwind.animator.application.AnimatorConfiguration;
import au.gov.ga.worldwind.common.ui.BasicAction;
import au.gov.ga.worldwind.common.ui.FileFilters;
import au.gov.ga.worldwind.common.util.DefaultLauncher;
import au.gov.ga.worldwind.common.util.Icons;
import au.gov.ga.worldwind.common.util.Util;
import au.gov.ga.worldwind.common.util.Validate;
import au.gov.ga.worldwind.wmsbrowser.layer.MetacartaCoastlineLayer;
import au.gov.ga.worldwind.wmsbrowser.layer.MetacartaCountryBoundariesLayer;
import au.gov.ga.worldwind.wmsbrowser.layer.WmsLayerExporter;
import au.gov.ga.worldwind.wmsbrowser.layer.WmsLayerExporterImpl;

/**
 * A panel used to display WMS Layer information
 * <p/>
 * Uses the capabilities information to display layer metadata, and a flat globe view to display the layer data itself
 */
public class WmsLayerInfoPanel extends JComponent
{
	private static final Sector DEFAULT_VIEW_EXTENTS = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG180, Angle.POS180);
	private static final long serialVersionUID = 20101122L;
	private static int PADDING = 10;
	
	private static final WmsLayerExporter exporter = new WmsLayerExporterImpl();
	
	/** The layer this panel is backed by */
	private WMSLayerInfo layerInfo;
	private WMSLayerCapabilities layerCapabilities;
	
	private JPanel splitPanel;
	
	private JPanel panel;
	private JTextPane infoTextPane;
	
	private JPanel buttonPanel;
	private BasicAction exportLayerAction;
	private JFileChooser fileChooser;
	
	private WorldWindowGLCanvas wwd;
	private WorldMapLayer worldMapLayer;
	private MetacartaCountryBoundariesLayer countryBoundariesLayer;
	private MetacartaCoastlineLayer coastlinesLayer;
	
	public WmsLayerInfoPanel()
	{
		setLayout(new BorderLayout());
		
		initialiseActions();
		initialiseFileChooser();
		initialiseSplitPanel();
		initialiseContainerPanel();
		initialiseInfoPanel();
		initialiseButtonPanel();
		initialiseWorldWindow();
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
				wwd.createBufferStrategy(2);
			}
		});
	}

	private void initialiseActions()
	{
		exportLayerAction = new BasicAction(getMessage(getWmsExportLayerLabelKey()), Icons.export.getIcon());
		exportLayerAction.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				promptUserToSaveLayer();
			}
		});
	}
	
	private void initialiseFileChooser()
	{
		fileChooser = new JFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileFilter(FileFilters.getXmlFilter());
	}
	
	private void initialiseSplitPanel()
	{
		splitPanel = new JPanel();
		splitPanel.setLayout(new GridLayout(2, 1));
		splitPanel.setBorder(null);
		add(splitPanel, BorderLayout.CENTER);
	}

	private void initialiseContainerPanel()
	{
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(new CompoundBorder(panel.getBorder(), new EmptyBorder(PADDING, PADDING, PADDING, PADDING)));
		panel.setBackground(Color.WHITE);
		panel.setOpaque(true);
		
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.add(panel);
		splitPanel.add(scrollPane);
	}

	private void initialiseInfoPanel()
	{
		infoTextPane = new JTextPane();
		infoTextPane.setEditorKit(new HTMLEditorKit());
		infoTextPane.setEditable(false);
		infoTextPane.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (e.getEventType() == EventType.ACTIVATED)
				{
					DefaultLauncher.openURL(e.getURL());
				}
			}
		});
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add(infoTextPane, constraints);
	}
	
	private void initialiseButtonPanel()
	{
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(new JButton(exportLayerAction));
		buttonPanel.setOpaque(false);
		buttonPanel.setVisible(false);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.weighty = 0;
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.FIRST_LINE_END;
		panel.add(buttonPanel, constraints);
	}
	
	private void initialiseWorldWindow()
	{
		wwd = new WorldWindowGLCanvas(AnimatorConfiguration.getGLCapabilities());
		wwd.setModel(new BasicModel());
		((AWTInputHandler) wwd.getInputHandler()).setSmoothViewChanges(false);
		wwd.setMinimumSize(new Dimension(1, 1));
		((OrbitView) wwd.getView()).getOrbitViewLimits().setPitchLimits(Angle.ZERO, Angle.ZERO);
		((OrbitView) wwd.getView()).getOrbitViewLimits().setHeadingLimits(Angle.ZERO, Angle.ZERO);
		wwd.getModel().setGlobe(new EarthFlat());
		wwd.getModel().getGlobe().getTessellator().setMakeTileSkirts(false);
		wwd.getModel().getGlobe().setElevationModel(new ZeroElevationModel());
		wwd.setVisible(false);
		
		splitPanel.add(wwd);
		
		worldMapLayer = new WorldMapLayer();
		countryBoundariesLayer = new MetacartaCountryBoundariesLayer();
		coastlinesLayer = new MetacartaCoastlineLayer();
	}
	
	public void setLayerInfo(WMSLayerInfo layerInfo)
	{
		if (this.layerInfo == layerInfo)
		{
			return;
		}
		
		this.layerInfo = layerInfo;
		updateLayerInfoPanel();
	}

	private synchronized void updateLayerInfoPanel()
	{
		try
		{
			if (layerInfo == null)
			{
				wwd.setVisible(false);
				infoTextPane.setText("");
				buttonPanel.setVisible(false);
				return;
			}
			
			layerCapabilities = ((WMSCapabilities)layerInfo.getCaps()).getLayerByName(layerInfo.getParams().getStringValue(AVKey.LAYER_NAMES));
			
			String infoText = new LayerInfoBuilder(layerCapabilities).getFormattedString();
			infoTextPane.setText(infoText);
			
			updateFlatGlobeViewer();
			
			buttonPanel.setVisible(true);
		}
		finally
		{
			panel.validate();
			panel.repaint();
			validate();
			repaint();
		}
	}

	
	private void updateFlatGlobeViewer()
	{
		wwd.setVisible(true);
		wwd.getModel().getLayers().clear();		
		wwd.getModel().getLayers().add(new WMSTiledImageLayer((WMSCapabilities)layerInfo.getCaps(), layerInfo.getParams()));
		wwd.getModel().getLayers().add(countryBoundariesLayer);
		wwd.getModel().getLayers().add(coastlinesLayer);
		wwd.getModel().getLayers().add(worldMapLayer);
		zoomViewToSector(layerCapabilities.getGeographicBoundingBox() == null ? DEFAULT_VIEW_EXTENTS : layerCapabilities.getGeographicBoundingBox());
		wwd.redraw();
	}
	
	private void zoomViewToSector(Sector geographicBoundingBox)
	{
		// Estimate the earth as a perfect sphere...
		double earthRadius = wwd.getModel().getGlobe().getRadius();
		
		// Compute the estimated deltas in metres (Add some padding to account for rounding errors and some buffering)
		double deltaX = earthRadius * geographicBoundingBox.getDeltaLonRadians();
		double deltaY = earthRadius * geographicBoundingBox.getDeltaLatRadians();

		// Compute the altitude required to ensure that the whole of each axis is visible in the viewport 
		double altitudeX = (deltaX/2) / Math.tan(wwd.getView().getFieldOfView().radians / 2) * 1.3;
		double altitudeY = (deltaY/2) / Math.tan(wwd.getView().getFieldOfView().radians / 2) * 1.3;
		
		LatLon centroid = geographicBoundingBox.getCentroid();
		Position pos = new Position(centroid.getLatitude(), centroid.getLongitude(), Math.max(altitudeX, altitudeY));
		
		wwd.getView().setEyePosition(pos);
	}

	private void promptUserToSaveLayer()
	{
		int response = fileChooser.showSaveDialog(getParent());
		if (response == JFileChooser.CANCEL_OPTION)
		{
			return;
		}
		
		File targetFile = fileChooser.getSelectedFile();
		if (targetFile == null)
		{
			return;
		}
		
		if (targetFile.exists())
		{
			response = JOptionPane.showConfirmDialog(getParent(), getMessage(getLayerDefinitionAlreadyExistsMessageKey(), targetFile.getName()), getMessage(getLayerDefinitionAlreadyExistsTitleKey()), JOptionPane.YES_NO_CANCEL_OPTION);
			if (response == JOptionPane.NO_OPTION)
			{
				return;
			}
			if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION)
			{
				promptUserToSaveLayer();
			}
		}
		
		exporter.exportLayer(targetFile, layerInfo);
	}
	
	/**
	 * A builder that uses an external template to create a HTML-formatted string suitable for display in a text area or similar.
	 */
	public static class LayerInfoBuilder 
	{
		private static String INFO_TEMPLATE;
		static
		{
			try
			{
				INFO_TEMPLATE = Util.readStreamToString(WmsLayerInfoPanel.class.getResourceAsStream("layerInfoTemplate.txt"));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		private static String getKeyWordsAsString(WMSLayerCapabilities capabilities)
		{
			String result = "";
			Iterator<String> keywords = capabilities.getKeywords().iterator();
			while (keywords.hasNext())
			{
				result += keywords.next();
				if (keywords.hasNext())
				{
					result += ", ";
				}
			}
			return result;
		}

		private static String getBoundingBoxAsString(WMSLayerCapabilities capabilities)
		{
			return capabilities.getGeographicBoundingBox().toString();
		}

		private static String getMetaDataUrlAsString(WMSLayerCapabilities capabilities)
		{
			return asUnorderedListOfLinks(capabilities.getMetadataURLs());
		}
		
		private static String getDataUrlAsString(WMSLayerCapabilities capabilities)
		{
			return asUnorderedListOfLinks(capabilities.getDataURLs());
		}
		
		private static String asUnorderedListOfLinks(Collection<WMSLayerInfoURL> urls)
		{
			if (urls.isEmpty())
			{
				return "";
			}
			
			String result = "";
			Iterator<WMSLayerInfoURL> iterator = urls.iterator();
			while (iterator .hasNext())
			{
				String href = iterator.next().getOnlineResource().getHref();
				String urlLink = "<a href=" + href + ">" + href + "</a>";
				if (urls.size() > 1)
				{
					urlLink = "<li>" + urlLink + "</li>";
				}
				result += urlLink;
			}
			if (urls.size() > 1)
			{
				result = "<ul>" + result + "</ul>";
			}
			return result;
		}
		
		private WMSLayerCapabilities layerCapabilities;
		private String formattedString = null;
		
		public LayerInfoBuilder(WMSLayerCapabilities layerCapabilities)
		{
			Validate.notNull(layerCapabilities, "Layer capabilities are required");
			this.layerCapabilities = layerCapabilities;
		}
		
		public String getFormattedString()
		{
			if (formattedString == null)
			{
				formattedString = buildFormattedString();
			}
			return formattedString;
		}

		private String buildFormattedString()
		{
			String result = INFO_TEMPLATE;
			result = substitute(result, "LAYER_NAME", layerCapabilities.getTitle());
			result = substituteNameValue(result, "DATA_URL", getMessage(getLayerInfoDataUrlKey()), getDataUrlAsString(layerCapabilities));
			result = substituteNameValue(result, "METADATA_URL", getMessage(getLayerInfoMetaDataUrlKey()), getMetaDataUrlAsString(layerCapabilities));
			result = substituteNameValue(result, "LAST_UPDATE", getMessage(getLayerInfoLastUpdateKey()), layerCapabilities.getLastUpdate());
			result = substituteNameValue(result, "ABSTRACT", getMessage(getLayerInfoAbstractKey()), layerCapabilities.getLayerAbstract());
			result = substituteNameValue(result, "BOUNDING_BOX", getMessage(getLayerInfoBoundingBoxKey()), getBoundingBoxAsString(layerCapabilities));
			result = substituteNameValue(result, "KEYWORDS", getMessage(getLayerInfoKeywordsKey()), getKeyWordsAsString(layerCapabilities));
			return result;
		}
		
		private static String substitute(String template, String variable, String value)
		{
			return template.replace("@" + variable + "@", Util.isBlank(value) ? getMessage(getLayerInfoDefaultStringValueKey()) : value);
		}
		
		private static String substituteNameValue(String template, String variable, String heading, String value)
		{
			String result = substitute(template, variable + "_HEADING", heading);
			result = substitute(result, variable + "_VALUE", value);
			return result;
		}
	}
}
