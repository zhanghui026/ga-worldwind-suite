/**
 * 
 */
package au.gov.ga.worldwind.animator.animation.parameter;

import au.gov.ga.worldwind.animator.util.Validate;

/**
 * A basic implementation of the {@link ParameterValue} interface.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public class BasicParameterValue implements ParameterValue
{

	/** The owner of this value */
	private Parameter owner;
	
	/** The value of this {@link ParameterValue}*/
	private double value;
	
	/**
	 * Constructor. 
	 * <p/>
	 * Initialises the mandatory {@link #owner} and {@link #value} fields
	 */
	public BasicParameterValue(double value, Parameter owner) 
	{
		Validate.notNull(owner, "An owner is required");
		this.value = value;
		this.owner = owner;
	}
	
	@Override
	public String getRestorableState()
	{
		// TODO Implement me!
		return null;
	}

	@Override
	public void restoreState(String stateInXml)
	{
		// TODO Implement me!
	}

	@Override
	public double getValue()
	{
		return value;
	}

	@Override
	public void setValue(double value)
	{
		this.value = value;
	}

	@Override
	public Parameter getOwner()
	{
		return owner;
	}

	@Override
	public ParameterValueType getType()
	{
		return ParameterValueType.LINEAR;
	}
}
