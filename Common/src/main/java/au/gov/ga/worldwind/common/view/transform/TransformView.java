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
package au.gov.ga.worldwind.common.view.transform;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.ViewUtil;

/**
 * View interface extension that provides the ability to customize the model
 * view and projection matrices before they are applied.
 * 
 * @author Michael de Hoog (michael.dehoog@ga.gov.au)
 */
public interface TransformView extends View
{
	/**
	 * Called by doApply function, before the transform matrices are computed.
	 * Override this function in subclasses to perform necessary calculations.
	 */
	void beforeComputeMatrices();

	/**
	 * Calculate the current viewport for this view. Default implementation gets
	 * the viewport from the OpenGL state.
	 */
	java.awt.Rectangle computeViewport(DrawContext dc);

	/**
	 * Calculate a MODELVIEW transform for this view. Default implementation
	 * uses {@link ViewUtil#computeTransformMatrix}.
	 */
	Matrix computeModelView();

	/**
	 * Return the MODELVIEW transform/matrix that was calculated before being
	 * transformed in some way. Usually this is the value calculated by
	 * {@link ViewUtil#computeTransformMatrix}, but some views can apply
	 * additional transformations (such as head tracking rotation in a HMD).
	 * This method returns the matrix before these additional transformations
	 * are applied.
	 * 
	 * @return Model view matrix that was calculated before being tranformed
	 */
	Matrix getPretransformedModelViewMatrix();

	/**
	 * Calculates a PROJECTION transform for this view. Default implementation
	 * uses {@link Matrix#fromPerspective}.
	 * 
	 * @param nearDistance
	 *            Near frustum value
	 * @param farDistance
	 *            Far frustum value
	 * @return Projection matrix
	 */
	Matrix computeProjection(double nearDistance, double farDistance);
}
