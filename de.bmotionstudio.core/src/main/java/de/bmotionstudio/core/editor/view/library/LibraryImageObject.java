/** 
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, 
 * Heinrich Heine Universitaet Duesseldorf
 * This software is licenced under EPL 1.0 (http://www.eclipse.org/org/documents/epl-v10.html) 
 * */

package de.bmotionstudio.core.editor.view.library;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.google.inject.Injector;

import de.prob.statespace.AnimationSelector;
import de.prob.statespace.Trace;
import de.prob.webconsole.ServletContextListener;

public class LibraryImageObject extends LibraryObject {

	private Injector injector = ServletContextListener.INJECTOR;
	private final AnimationSelector selector = injector
			.getInstance(AnimationSelector.class);
	
	public LibraryImageObject(String name, String type, Image typeImage) {
		super(name, type, typeImage);
	}

	@Override
	public void delete(LibraryPage page) {

		Trace currentTrace = selector.getCurrentTrace();
		if (currentTrace != null) {
			String imagePath = currentTrace.getModel().getModelFile()
					.getParent()
					+ "/images" + File.separator + getName();

			File file = new File(imagePath);
			if (file.exists())
				file.delete();

			try {
				IPath path = new Path(file.getPath());
				IFile targetFile = ResourcesPlugin.getWorkspace().getRoot()
						.getFileForLocation(path);
				if (targetFile == null)
					return;
				IProject project = targetFile.getProject();
				project.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public Image getPreview(LibraryPage page) {
		Trace currentTrace = selector.getCurrentTrace();
		if (currentTrace != null) {
			String imagePath = currentTrace.getModel().getModelFile()
					.getParent()
					+ "/images" + File.separator + getName();
			if (new File(imagePath).exists())
				return new Image(Display.getDefault(), imagePath);
		}
		return null;
	}

}
