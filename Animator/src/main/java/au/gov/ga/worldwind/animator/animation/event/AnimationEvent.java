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
package au.gov.ga.worldwind.animator.animation.event;


/**
 * A super-interface for events that can occur within an animation.
 * <p/>
 * An event has:
 * <ol>
 *  <li>an owner (the object that signalled the event);
 *  <li>a type (that signals the type of action that occurred to trigger the event);
 *  <li>a value (the value that was acted on in the action that triggered the event); and
 *  <li>a cause (the event that caused this event to be triggered)
 * </ol>
 * <p/>
 * Animation events support a causal chain that can be queried by listeners interested in particular event types.
 * For example, a key frame display might be interested in any event that involves key frames.
 * 
 * @author James Navin (james.navin@ga.gov.au)
 */
public interface AnimationEvent
{

	/**
	 * Gets the event that triggered this event to be fired (if any).
	 * 
	 * @return the event that triggered this event to be fired, or <code>null</code> if one does not exist.
	 */
	AnimationEvent getCause();
	
	/**
	 * Gets the <em>first</em> event in the causal chain of the given class.
	 * <p/>
	 * If this event is of the provided type, will return this event.
	 * <p/>
	 * If there is no event in the causal chain of the given class, will return <code>null</code>.
	 * 
	 * @param clazz The class of event to retrieve from the causal chain.
	 * 
	 * @return the <em>first</em> event in the causal chain of the given class, or <code>null</code> if no event
	 * of the given class exists in the causal chain.
	 */
	AnimationEvent getCauseOfClass(Class<? extends AnimationEvent> clazz);
	
	/**
	 * Returns whether an owner in the causal chain of this event is of the provided type. Useful for determining if an 
	 * event has effected a particular level of the animation (for example, if an event may have effected the Camera). 
	 */
	boolean hasOwnerInChainOfType(Class<?> clazz);
	
	/**
	 * Returns the first object in the chain of the given type.
	 * <p/>
	 * Inspects (in order):
	 * <ol>
	 * 	<li>The event itself
	 *  <li>The event owner
	 *  <li>The event value
	 *  <li>The event cause
	 * </ol>
	 * Inspects from this event down to the root cause in order.
	 */
	<T> T getObjectInChainOfType(Class<T> clazz);
	
	/**
	 * Gets the last event in the causal chain.
	 * 
	 * @return the ultimate cause in the causal chain.
	 */
	AnimationEvent getRootCause();
	
	/**
	 * @return The type of event this instance represents
	 */
	Type getType();
	
	/**
	 * @return The owner of <em>this</em> event. This is the object that signalled the event.
	 */
	Object getOwner();
	
	/**
	 * @return The value associated with this event. This is the object that was added/changed/removed to trigger the event.
	 * Will usually be <code>null</code> in all but the ultimate cause in the causal chain.
	 */
	Object getValue();
	
	/**
	 * @return Whether this event is of the provided type
	 */
	boolean isOfType(Type type);
	
	/**
	 * The valid types of events
	 */
	public static enum Type
	{
		ADD, REMOVE, CHANGE, OTHER;
	}

	
}
