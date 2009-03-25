package au.gov.ga.worldwind.layers.user;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import au.gov.ga.worldwind.util.JDoubleField;
import au.gov.ga.worldwind.util.JIntegerField;

public class UserLayerEditor extends JDialog
{
	private JTextField nameField;
	private JTextField dirField;
	private JComboBox extensionCombo;
	private JIntegerField tilesizeField;
	private JDoubleField lztsdField;
	private JCheckBox transparentCheck;
	private JColorComponent transparentColor;
	private JIntegerField fuzzField;
	private JLabel transparentLabel;
	private JLabel fuzzLabel;
	private JLabel fuzzUnit;

	private JButton okButton;
	private boolean okPressed = false;

	private final static String[] FILE_EXTENSIONS = { "JPG", "PNG", "BMP",
			"JPEG", "DDS" };

	private UserLayerEditor(Frame owner, String title,
			final UserLayerDefinition definition)
	{
		super(owner, "User layer", true);

		setLayout(new BorderLayout());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		DocumentListener dl = new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				checkFields();
			}

			public void insertUpdate(DocumentEvent e)
			{
				checkFields();
			}

			public void removeUpdate(DocumentEvent e)
			{
				checkFields();
			}
		};
		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				checkFields();
			}
		};
		ActionListener transAl = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				enableTransparent();
			}
		};

		int s = 5;
		int numberFieldWidth = 100;
		JLabel label;
		JPanel panel, panel2;
		GridBagConstraints c;
		Dimension size;

		panel = new JPanel(new GridBagLayout());
		add(panel, BorderLayout.CENTER);

		label = new JLabel("Name:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		nameField = new JTextField(definition.getName());
		nameField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(s, s, 0, s);
		panel.add(nameField, c);

		label = new JLabel("Tile directory:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		dirField = new JTextField(definition.getDirectory());
		dirField.getDocument().addDocumentListener(dl);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(dirField, c);

		JButton browse = new JButton("...");
		browse.setToolTipText("Browse");
		browse.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Select layer tileset directory");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				if (chooser.showOpenDialog(UserLayerEditor.this) == JFileChooser.APPROVE_OPTION)
				{
					dirField.setText(chooser.getSelectedFile()
							.getAbsolutePath());
				}
			}
		});
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.insets = new Insets(s, 0, 0, s);
		panel2.add(browse, c);

		label = new JLabel("File extension:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		extensionCombo = new JComboBox(FILE_EXTENSIONS);
		for (int i = 0; i < extensionCombo.getItemCount(); i++)
		{
			if (definition.getExtension().equalsIgnoreCase(
					(String) extensionCombo.getItemAt(i)))
			{
				extensionCombo.setSelectedIndex(i);
				break;
			}
		}
		extensionCombo.addActionListener(al);
		extensionCombo.addActionListener(transAl);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		panel.add(extensionCombo, c);

		label = new JLabel("Tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 3;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		tilesizeField = new JIntegerField(definition.getTilesize());
		tilesizeField.getDocument().addDocumentListener(dl);
		size = tilesizeField.getPreferredSize();
		size.width = numberFieldWidth;
		tilesizeField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(tilesizeField, c);

		label = new JLabel("px");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.insets = new Insets(s, 1, 0, s);
		panel2.add(label, c);

		label = new JLabel("Level zero tile size:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 4;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(label, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 4;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		lztsdField = new JDoubleField(definition.getLztsd());
		lztsdField.getDocument().addDocumentListener(dl);
		size = lztsdField.getPreferredSize();
		size.width = numberFieldWidth;
		lztsdField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(lztsdField, c);

		label = new JLabel("\u00B0");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.insets = new Insets(s, 1, 0, s);
		panel2.add(label, c);

		transparentCheck = new JCheckBox("Has transparent color");
		transparentCheck.setSelected(definition.isHasTransparentColor());
		transparentCheck.addActionListener(al);
		transparentCheck.addActionListener(transAl);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		panel.add(transparentCheck, c);

		transparentLabel = new JLabel("Transparent color:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 6;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(transparentLabel, c);

		Color color = definition.getTransparentColor();
		if (color == null)
			color = Color.black;
		transparentColor = new JColorComponent(color);
		transparentColor.setBorder(BorderFactory.createLineBorder(Color.black));
		transparentColor.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (transparentColor.isEnabled())
				{
					Color color = JColorChooser.showDialog(transparentColor,
							"Select transparent color", transparentColor
									.getColor());
					if (color != null)
						transparentColor.setColor(color);
				}
			}
		});
		size = transparentColor.getPreferredSize();
		size.width = 32;
		size.height = 16;
		transparentColor.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 6;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(s, s, 0, s);
		panel.add(transparentColor, c);

		fuzzLabel = new JLabel("Transparent fuzz:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 7;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(s, s, 0, 0);
		panel.add(fuzzLabel, c);

		panel2 = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 7;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(panel2, c);

		fuzzField = new JIntegerField(definition.getTransparentFuzz());
		fuzzField.getDocument().addDocumentListener(dl);
		size = fuzzField.getPreferredSize();
		size.width = numberFieldWidth;
		fuzzField.setPreferredSize(size);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(s, s, 0, 0);
		panel2.add(fuzzField, c);

		fuzzUnit = new JLabel("%");
		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.weightx = 1;
		c.insets = new Insets(s, 1, 0, s);
		panel2.add(fuzzUnit, c);


		panel = new JPanel(new BorderLayout());
		int spacing = 5;
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createEtchedBorder(), BorderFactory.createEmptyBorder(spacing,
				spacing, spacing, spacing)));
		add(panel, BorderLayout.SOUTH);

		JPanel buttonsPanel = new JPanel(new FlowLayout());
		panel.add(buttonsPanel, BorderLayout.CENTER);

		okButton = new JButton("OK");
		buttonsPanel.add(okButton);
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (checkAndUpdateDefinition(definition))
				{
					okPressed = true;
					dispose();
				}
			}
		});
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);

		JButton button = new JButton("Cancel");
		buttonsPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});

		checkFields();
		enableTransparent();
		pack();
		size = getPreferredSize();
		setSize(500, size.height);
		setLocationRelativeTo(owner);
	}

	private void checkFields()
	{
		boolean valid = true;
		valid = valid && nameField.getText().length() > 0;
		File dir = new File(dirField.getText());
		valid = valid && dir.exists() && dir.isDirectory();
		valid = valid && lztsdField.getValue() != null;
		valid = valid && tilesizeField.getValue() != null;
		if (transparentCheck.isSelected())
		{
			valid = valid && transparentColor.getColor() != null;
			valid = valid && fuzzField.getValue() != null;
		}
		okButton.setEnabled(valid);
	}

	private boolean checkAndUpdateDefinition(UserLayerDefinition definition)
	{
		definition.setName(nameField.getText());
		definition.setDirectory(dirField.getText());
		definition.setExtension((String) extensionCombo.getSelectedItem());
		definition.setTilesize(tilesizeField.getValue());
		definition.setLztsd(lztsdField.getValue());
		definition.setHasTransparentColor(transparentCheck.isSelected()
				&& allowTransparent());
		definition.setTransparentColor(transparentColor.getColor());
		definition.setTransparentFuzz(fuzzField.getValue());
		return true;
	}

	private boolean allowTransparent()
	{
		return !"DDS".equalsIgnoreCase((String) extensionCombo
				.getSelectedItem());
	}

	private void enableTransparent()
	{
		boolean allow = allowTransparent();
		transparentCheck.setEnabled(allow);
		boolean enabled = allow && transparentCheck.isSelected();
		transparentLabel.setEnabled(enabled);
		transparentColor.setEnabled(enabled);
		fuzzLabel.setEnabled(enabled);
		fuzzField.setEnabled(enabled);
		fuzzUnit.setEnabled(enabled);
	}

	public static UserLayerDefinition editDefinition(Frame owner, String title,
			UserLayerDefinition definition)
	{
		UserLayerEditor editor = new UserLayerEditor(owner, title, definition);
		editor.setVisible(true);
		if (editor.okPressed)
		{
			return definition;
		}
		return null;
	}

	private class JColorComponent extends JComponent
	{
		private Color color;

		public JColorComponent(Color color)
		{
			this.color = color;
		}

		public Color getColor()
		{
			return color;
		}

		public void setColor(Color color)
		{
			this.color = color;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			if (color != null && isEnabled())
			{
				g.setColor(color);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		}
	}
}
