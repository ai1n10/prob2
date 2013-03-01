/** 
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */
package de.bmotionstudio.core.model.control;

import de.bmotionstudio.core.model.attribute.AttributeTrackDirection;
import de.bmotionstudio.core.model.attribute.BAttributeHeight;
import de.bmotionstudio.core.model.attribute.BAttributeLabel;
import de.bmotionstudio.core.model.attribute.BAttributeSize;
import de.bmotionstudio.core.model.attribute.BAttributeWidth;

/**
 * @author Lukas Ladenberger
 * 
 */
public class Signal extends BControl {

	@Override
	protected void initAttributes() {

		BAttributeHeight aHeight = new BAttributeHeight(22);
		aHeight.setGroup(BAttributeSize.ID);
		aHeight.setShow(false);
		aHeight.setEditable(false);
		initAttribute(aHeight);

		BAttributeWidth aWidth = new BAttributeWidth(30);
		aWidth.setGroup(BAttributeSize.ID);
		aWidth.setShow(false);
		aWidth.setEditable(false);
		initAttribute(aWidth);
		
		initAttribute(new AttributeTrackDirection(AttributeTrackDirection.RIGHT));
		initAttribute(new BAttributeLabel("Signal"));

	}

}
