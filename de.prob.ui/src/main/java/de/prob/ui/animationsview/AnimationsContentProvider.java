package de.prob.ui.animationsview;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.prob.statespace.AnimationSelector;

/**
 * Creates a new list of Operations, merging the list of available operations
 * with the list of enabled operations. Before adding the enabled operations,
 * they are divided into groups by their operation name
 * 
 */
class AnimationsContentProvider implements IStructuredContentProvider {

	private final Logger logger = LoggerFactory
			.getLogger(AnimationsContentProvider.class);

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput,
			final Object newInput) {
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		List<Object> animations = new ArrayList<Object>();
		if (inputElement instanceof AnimationSelector) {
			AnimationSelector aS = (AnimationSelector) inputElement;
			animations.addAll(aS.getTraces());
		} else {
			logger.warn(
					"Input Element was not an Animation Selector. Class was {}",
					inputElement.getClass());
		}
		return animations.toArray();
	}
}