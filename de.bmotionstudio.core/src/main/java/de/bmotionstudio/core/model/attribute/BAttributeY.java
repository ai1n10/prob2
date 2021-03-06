/** 
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

package de.bmotionstudio.core.model.attribute;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import de.bmotionstudio.core.editor.property.IntegerPropertyDescriptor;
import de.bmotionstudio.core.model.control.BControl;

public class BAttributeY extends AbstractAttribute {

	public BAttributeY(Object value) {
		super(value);
	}

	public PropertyDescriptor preparePropertyDescriptor() {
		IntegerPropertyDescriptor descriptor = new IntegerPropertyDescriptor(
				getID(), getName());
		return descriptor;
	}

	@Override
	public String validateValue(Object value, BControl control) {
		if (!(String.valueOf(value)).trim().matches("\\d*")) {
			return "Value must be a number";
		}
		if ((String.valueOf(value)).trim().length() == 0) {
			return "Value must not be empty string";
		}
		return null;
	}

	@Override
	public String getName() {
		return "Y";
	}

}
