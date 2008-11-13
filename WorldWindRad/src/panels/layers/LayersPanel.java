package panels.layers;

import gov.nasa.worldwind.WorldWindow;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class LayersPanel extends JPanel
{
	private StandardPanel standardPanel;
	private WorldWindow wwd;

	public LayersPanel(WorldWindow wwd)
	{
		super(new BorderLayout());
		this.wwd = wwd;
		add(createTabs());
	}

	private JTabbedPane createTabs()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Standard", createStandard());
		tabbedPane.addTab("Radiometrics", createRadiometry());
		tabbedPane.addTab("Other", createOther());
		tabbedPane.doLayout();
		return tabbedPane;
	}

	private JComponent createStandard()
	{
		standardPanel = new StandardPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(standardPanel, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}

	private JComponent createRadiometry()
	{
		RadiometryPanel rp = new RadiometryPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(rp, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}

	private JComponent createOther()
	{
		OtherPanel op = new OtherPanel(wwd);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(op, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		return scrollPane;
	}
	
	public void turnOffAtmosphere()
	{
		standardPanel.turnOffAtmosphere();
	}
	
	public void setMapPickingEnabled(boolean enabled)
	{
		standardPanel.setMapPickingEnabled(enabled);
	}
}