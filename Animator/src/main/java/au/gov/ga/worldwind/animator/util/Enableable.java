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
package au.gov.ga.worldwind.animator.util;

import au.gov.ga.worldwind.animator.animation.Animation;

/**
 * A super-interface for objects that can be 'enabled' or 'disabled'.
 * <p/>
 * When making an object 'enabled' or 'disabled', this status is propagated to
 * any child {@link Enableable}s.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface Enableable
{

	/**
	 * Return whether this object is currently enabled for the current
	 * {@link Animation}.
	 * 
	 * @return whether this object is currently enabled
	 */
	boolean isEnabled();

	/**
	 * @return Whether all of this objects {@link Enableable} children are
	 *         'enabled'. If this object has no {@link Enableable} children,
	 *         should return <code>true</code>.
	 */
	boolean isAllChildrenEnabled();

	/**
	 * @return Whether any of this objects {@link Enableable} children are
	 *         'enabled'. If this object has no {@link Enableable} children,
	 *         should return <code>false</code>.
	 */
	boolean hasEnabledChildren();

	/**
	 * Set whether this object is currently enabled for the current
	 * {@link Animation}.
	 * <p/>
	 * The 'enabled' status will be propagated to child objects (if applicable)
	 * 
	 * @param enabled
	 *            Whether this object is currently enabled
	 */
	void setEnabled(boolean enabled);

	/**
	 * Add an {@link Enableable} which is codependant on this {@link Enableable}
	 * (ie, whenever this is enabled, enableable is enabled, and vice versa).
	 * Must also call {@link #connectCodependantEnableable(Enableable)} on the
	 * enableable provided (and check that the enableable has not already been
	 * added to prevent a stack overflow).
	 */
	void connectCodependantEnableable(Enableable enableable);
}
