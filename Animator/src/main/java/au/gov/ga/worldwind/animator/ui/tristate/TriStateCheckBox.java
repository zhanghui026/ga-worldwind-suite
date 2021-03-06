/*******************************************************************************
 * Copyright 2012 Geoscience Australia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package au.gov.ga.worldwind.animator.ui.tristate;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ActionMapUIResource;

import au.gov.ga.worldwind.animator.util.Icons;

/**
 * A checkbox that can have three states: checked, unchecked and partially checked.
 * <p/>
 * Parts of this class are based on the TristateCheckBox by Heinz Kabutz (http://www.javaspecialists.eu/archive/Issue145.html).
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class TriStateCheckBox extends JLabel
{
	private static final long serialVersionUID = 20100915L;
	
	private static final Icon DEFAULT_CHECKED_ICON = Icons.check.getIcon();
	private static final Icon DEFAULT_UNCHECKED_ICON = Icons.uncheck.getIcon();
	private static final Icon DEFAULT_PARTIAL_CHECKED_ICON = Icons.partialCheck.getIcon();
	
	private Icon checkedIcon = DEFAULT_CHECKED_ICON;
	private Icon uncheckedIcon = DEFAULT_UNCHECKED_ICON;
	private Icon partialCheckedIcon = DEFAULT_PARTIAL_CHECKED_ICON;
	
	/** The model that backs this component */
	private TriStateCheckBoxModel model;
	
	/**
	 * A representation of the three states a {@link TriStateCheckBox} can be in. 
	 * <p/>
	 * Encodes the state machine describing the state the checkbox moves into when selected.
	 */
	public static enum State 
	{
		CHECKED { @Override public State nextState() { return UNCHECKED; } },
		UNCHECKED{ @Override public State nextState() { return CHECKED; } },
		PARTIAL{ @Override public State nextState() { return UNCHECKED; } };
		
		public abstract State nextState();
	}
	
	@SuppressWarnings("serial")
	public TriStateCheckBox()
	{
		super.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e)
			{
				model.iterateState();
			}
		});
		ActionMap actions = new ActionMapUIResource();
		actions.put("pressed", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				model.iterateState();
			}
		});
		actions.put("released", null);
		SwingUtilities.replaceUIActionMap(this, actions);
		
		this.model = new DefaultTriStateCheckBoxModel(State.CHECKED);
		setIcon(checkedIcon);
	}

	
	
	public TriStateCheckBox(Icon checkedIcon, Icon uncheckedIcon, Icon partialCheckedIcon)
	{
		this();
		this.checkedIcon = checkedIcon;
		this.uncheckedIcon = uncheckedIcon;
		this.partialCheckedIcon = partialCheckedIcon;
	}

	@Override
	public Icon getIcon()
	{
		switch (getCurrentState())
		{
			case CHECKED: return checkedIcon;
			case UNCHECKED: return uncheckedIcon;
			case PARTIAL: return partialCheckedIcon;
		}
		return null;
	}
	
	public TriStateCheckBoxModel getModel()
	{
		return model;
	}
	
	public void setModel(TriStateCheckBoxModel model)
	{
		if (model == null)
		{
			return;
		}
		this.model = model;
	}
	
	public void setCurrentState(State state)
	{
		model.setCurrentState(state);
	}
	
	public State getCurrentState()
	{
		return model.getCurrentState();
	}
	
	public void setCheckedIcon(Icon checkedIcon)
	{
		this.checkedIcon = checkedIcon;
	}

	public void setUncheckedIcon(Icon uncheckedIcon)
	{
		this.uncheckedIcon = uncheckedIcon;
	}
	
	public void setPartialCheckedIcon(Icon partialCheckedIcon)
	{
		this.partialCheckedIcon = partialCheckedIcon;
	}
}
