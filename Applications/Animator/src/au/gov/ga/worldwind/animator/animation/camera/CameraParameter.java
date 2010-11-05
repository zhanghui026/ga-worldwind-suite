package au.gov.ga.worldwind.animator.animation.camera;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import au.gov.ga.worldwind.animator.animation.Animation;
import au.gov.ga.worldwind.animator.animation.AnimationContext;
import au.gov.ga.worldwind.animator.animation.annotation.EditableParameter;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterBase;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValue;
import au.gov.ga.worldwind.animator.animation.parameter.ParameterValueFactory;
import au.gov.ga.worldwind.animator.util.message.AnimationMessageConstants;
import au.gov.ga.worldwind.common.util.message.MessageSourceAccessor;

abstract class CameraParameter extends ParameterBase
{
	private static final long serialVersionUID = 20100922L;

	protected static final String DEFAULT_PARAMETER_NAME = "Render Camera - Param";

	protected CameraParameter()
	{
		super();
	}

	public CameraParameter(String name, Animation animation)
	{
		super(name, animation);
	}

	protected View getView()
	{
		return getAnimation().getWorldWindow().getView();
	}
	
	protected void redraw()
	{
		getAnimation().getWorldWindow().redraw();
	}

	protected Position getCurrentEyePosition()
	{
		return getView().getCurrentEyePosition();
	}

	protected Position getCurrentLookatPosition()
	{
		Vec4 center = getView().getCenterPoint();
		Globe globe = getView().getGlobe();
		if (center == null || globe == null)
			return null;
		return globe.computePositionFromPoint(center);
	}

	protected void applyCameraPositions(Position eyePosition, Position lookAtPosition)
	{
		View view = getView();
		view.stopMovement();
		view.setOrientation(eyePosition, lookAtPosition);
		redraw();
	}

	/**
	 * Parameter for eye latitude
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "deg")
	static class EyeLatParameter extends CameraParameter
	{
		public EyeLatParameter(Animation animation)
		{
			super(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraEyeLatNameKey(),
					DEFAULT_PARAMETER_NAME), animation);
		}

		EyeLatParameter()
		{
			super();
		}

		@Override
		public ParameterValue getCurrentValue(AnimationContext context)
		{
			double value = getCurrentEyePosition().getLatitude().getDegrees();
			return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentEyePosition = getCurrentEyePosition();
			Position newEyePosition = Position.fromDegrees(value,
					currentEyePosition.longitude.degrees,
					currentEyePosition.elevation);
			applyCameraPositions(newEyePosition, getCurrentLookatPosition());
		}

		@Override
		protected ParameterBase createParameter(AVList context)
		{
			return new EyeLatParameter();
		}
	}

	/**
	 * Parameter for eye longitude
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "deg")
	static class EyeLonParameter extends CameraParameter
	{
		public EyeLonParameter(Animation animation)
		{
			super(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraEyeLonNameKey(),
					DEFAULT_PARAMETER_NAME), animation);
		}

		EyeLonParameter()
		{
			super();
		}

		@Override
		public ParameterValue getCurrentValue(AnimationContext context)
		{
			double value = getCurrentEyePosition().getLongitude().getDegrees();
			return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentEyePosition = getCurrentEyePosition();
			Position newEyePosition = Position.fromDegrees(currentEyePosition.latitude.degrees,
					value,
					currentEyePosition.elevation);
			applyCameraPositions(newEyePosition, getCurrentLookatPosition());

		}

		@Override
		protected ParameterBase createParameter(AVList context)
		{
			return new EyeLonParameter();
		}
	}

	/**
	 * Parameter for eye elevation
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "km")
	static class EyeElevationParameter extends CameraParameter
	{
		public EyeElevationParameter(Animation animation)
		{
			super(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraEyeZoomNameKey(),
					DEFAULT_PARAMETER_NAME), animation);
		}

		EyeElevationParameter()
		{
			super();
		}

		@Override
		public ParameterValue getCurrentValue(AnimationContext context)
		{
			double value = context.applyZoomScaling(getCurrentEyePosition().getElevation());
			return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentEyePosition = getCurrentEyePosition();
			Position newEyePosition = Position.fromDegrees(currentEyePosition.latitude.degrees,
					currentEyePosition.longitude.degrees,
					getAnimation().unapplyZoomScaling(value));
			applyCameraPositions(newEyePosition, getCurrentLookatPosition());
		}

		@Override
		protected ParameterBase createParameter(AVList context)
		{
			return new EyeElevationParameter();
		}
	}

	/**
	 * Parameter for look-at latitude
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "deg")
	static class LookatLatParameter extends CameraParameter
	{
		public LookatLatParameter(Animation animation)
		{
			super(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraLookatLatNameKey(),
					DEFAULT_PARAMETER_NAME), animation);
		}

		LookatLatParameter()
		{
			super();
		}

		@Override
		public ParameterValue getCurrentValue(AnimationContext context)
		{
			double value = getCurrentLookatPosition().getLatitude().getDegrees();
			return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentLookatPosition = getCurrentLookatPosition();
			Position newLookatPosition = Position.fromDegrees(value,
					currentLookatPosition.longitude.degrees,
					currentLookatPosition.elevation);
			applyCameraPositions(getCurrentEyePosition(), newLookatPosition);

		}

		@Override
		protected ParameterBase createParameter(AVList context)
		{
			return new LookatLatParameter();
		}
	}

	/**
	 * Parameter for look-at longitude
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "deg")
	static class LookatLonParameter extends CameraParameter
	{
		public LookatLonParameter(Animation animation)
		{
			super(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraLookatLonNameKey(),
					DEFAULT_PARAMETER_NAME), animation);
		}

		LookatLonParameter()
		{
			super();
		}

		@Override
		public ParameterValue getCurrentValue(AnimationContext context)
		{
			double value = getCurrentLookatPosition().getLongitude().getDegrees();
			return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentLookatPosition = getCurrentLookatPosition();
			Position newLookatPosition = Position.fromDegrees(currentLookatPosition.latitude.degrees,
					value,
					currentLookatPosition.elevation);
			applyCameraPositions(getCurrentEyePosition(), newLookatPosition);
		}

		@Override
		protected ParameterBase createParameter(AVList context)
		{
			return new LookatLonParameter();
		}
	}

	/**
	 * Parameter for look-at elevation
	 */
	@SuppressWarnings("serial")
	@EditableParameter(units = "km")
	static class LookatElevationParameter extends CameraParameter
	{
		public LookatElevationParameter(Animation animation)
		{
			super(MessageSourceAccessor.get().getMessage(AnimationMessageConstants.getCameraLookatZoomNameKey(),
					DEFAULT_PARAMETER_NAME), animation);
		}

		LookatElevationParameter()
		{
			super();
		}

		@Override
		public ParameterValue getCurrentValue(AnimationContext context)
		{
			double value = context.applyZoomScaling(getCurrentLookatPosition().getElevation());
			return ParameterValueFactory.createParameterValue(this, value, context.getCurrentFrame());
		}

		@Override
		protected void doApplyValue(double value)
		{
			Position currentLookatPosition = getCurrentLookatPosition();
			Position newLookatPosition = Position.fromDegrees(currentLookatPosition.latitude.degrees,
															  currentLookatPosition.longitude.degrees,
															  getAnimation().unapplyZoomScaling(value));
			applyCameraPositions(getCurrentEyePosition(), newLookatPosition);
		}

		@Override
		protected ParameterBase createParameter(AVList context)
		{
			return new LookatElevationParameter();
		}
	}
}