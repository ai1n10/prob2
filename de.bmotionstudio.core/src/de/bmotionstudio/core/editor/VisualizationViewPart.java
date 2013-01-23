package de.bmotionstudio.core.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleRulerVisibilityAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import com.google.inject.Injector;

import de.bmotionstudio.core.editor.action.CopyAction;
import de.bmotionstudio.core.editor.action.AddObserverAction;
import de.bmotionstudio.core.editor.action.PasteAction;
import de.bmotionstudio.core.editor.part.BMSEditPartFactory;
import de.bmotionstudio.core.editor.view.library.AttributeTransferDropTargetListener;
import de.bmotionstudio.core.editor.view.outline.BMotionOutlinePage;
import de.bmotionstudio.core.model.BMotionRuler;
import de.bmotionstudio.core.model.Simulation;
import de.bmotionstudio.core.model.VisualizationView;
import de.bmotionstudio.core.model.control.BControl;
import de.bmotionstudio.core.model.control.Visualization;
import de.prob.statespace.AnimationSelector;
import de.prob.statespace.History;
import de.prob.statespace.IHistoryChangeListener;
import de.prob.webconsole.ServletContextListener;

public class VisualizationViewPart extends ViewPart implements
		CommandStackListener, PropertyChangeListener, IHistoryChangeListener {

	public static String ID = "de.bmotionstudio.core.view.VisualizationView";
	
	Injector injector = ServletContextListener.INJECTOR;
	
	private EditDomain editDomain;

	private VisualizationView visualizationView;

	private ActionRegistry actionRegistry;

	private BMotionSelectionSynchronizer selectionSynchronizer;

	private RulerComposite container;

	private GraphicalViewer graphicalViewer;

	private ScalableRootEditPart rootEditPart;

	private boolean isInitialized = false;

	private Composite parent;

	private List<String> selectionActions = new ArrayList<String>();
	private List<String> stackActions = new ArrayList<String>();
	private List<String> propertyActions = new ArrayList<String>();

	private Simulation simulation;

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {

		// // Adapter for zoom manager
		if (type == ZoomManager.class)
			return ((ScalableRootEditPart) getGraphicalViewer()
					.getRootEditPart()).getZoomManager();

		// Adapter for content outline page
		if (type == IContentOutlinePage.class) {
			return new BMotionOutlinePage(this);
		}

		if (type == ActionRegistry.class)
			return getActionRegistry();

		if (type == CommandStack.class)
			return getCommandStack();

		// Adapter for property page
		if (type == IPropertySheetPage.class) {
			BMotionPropertyPage page = new BMotionPropertyPage(
					getCommandStack(), getActionRegistry().getAction(
							ActionFactory.UNDO.getId()), getActionRegistry()
							.getAction(ActionFactory.REDO.getId()));
			page.setRootEntry(new CustomSortPropertySheetEntry(
					getCommandStack()));
			return page;
		}

		return super.getAdapter(type);

	}

	public SelectionSynchronizer getSelectionSynchronizer() {
		if (selectionSynchronizer == null)
			selectionSynchronizer = new BMotionSelectionSynchronizer();
		return selectionSynchronizer;
	}

	public GraphicalViewer getGraphicalViewer() {
		return graphicalViewer;
	}

	/**
	 * Lazily creates and returns the action registry.
	 * 
	 * @return the action registry
	 */
	public ActionRegistry getActionRegistry() {
		if (actionRegistry == null)
			actionRegistry = new ActionRegistry();
		return actionRegistry;
	}

	public EditDomain getEditDomain() {
		return editDomain;
	}

	protected CommandStack getCommandStack() {
		if (getEditDomain() != null)
			return getEditDomain().getCommandStack();
		return null;
	}

	public VisualizationView getVisualizationView() {
		return visualizationView;
	}

//	public Simulation getSimulation() {
//		return simulation;
//	}

	private void createActions() {

		ActionRegistry registry = getActionRegistry();
		IAction action;

		action = new UndoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new RedoAction(this);
		registry.registerAction(action);
		getStackActions().add(action.getId());

		action = new CopyAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new PasteAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new DeleteAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new SelectAllAction(this);
		registry.registerAction(action);

		ZoomManager manager = rootEditPart.getZoomManager();
		getActionRegistry().registerAction(new ZoomInAction(manager));
		getActionRegistry().registerAction(new ZoomOutAction(manager));

		double[] zoomLevels = new double[] { 0.25, 0.5, 0.75, 1.0, 1.5, 2.0,
				2.5, 3.0, 4.0, 5.0, 10.0, 20.0 };
		manager.setZoomLevels(zoomLevels);
		ArrayList<String> zoomContributions = new ArrayList<String>();
		zoomContributions.add(ZoomManager.FIT_ALL);
		zoomContributions.add(ZoomManager.FIT_HEIGHT);
		zoomContributions.add(ZoomManager.FIT_WIDTH);
		manager.setZoomLevelContributions(zoomContributions);

		getActionRegistry().registerAction(
				new ToggleRulerVisibilityAction(getGraphicalViewer()) {
					@Override
					public void run() {
						super.run();
						setChecked(!isChecked());
						// editor.setDirty(true);
					}
				});
		getActionRegistry().registerAction(
				new ToggleSnapToGeometryAction(getGraphicalViewer()) {
					@Override
					public void run() {
						super.run();
						setChecked(!isChecked());
						// editor.setDirty(true);
					}
				});
		getActionRegistry().registerAction(
				new ToggleGridAction(getGraphicalViewer()) {
					@Override
					public void run() {
						super.run();
						setChecked(!isChecked());
						// editor.setDirty(true);
					}
				});

		installObserverActions();
		// TODO: Reimplement me!!!
//		installSchedulerActions();

	}

	private void installObserverActions() {

		IAction action;
		ActionRegistry registry = getActionRegistry();

		IExtensionRegistry reg = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = reg
				.getExtensionPoint("de.bmotionstudio.core.observer");
		for (IExtension extension : extensionPoint.getExtensions()) {
			for (IConfigurationElement configurationElement : extension
					.getConfigurationElements()) {

				if ("observer".equals(configurationElement.getName())) {

					String observerClassName = configurationElement
							.getAttribute("class");

					action = new AddObserverAction(this);
					action.setId("de.bmotionstudio.core.observerAction."
							+ observerClassName);
					((AddObserverAction) action)
							.setClassName(observerClassName);
					registry.registerAction(action);
					getSelectionActions().add(
							"de.bmotionstudio.core.observerAction."
									+ observerClassName);

				}

			}

		}

	}

	// TODO: Reimplement me!!!
//	private void installSchedulerActions() {
//
//		IAction action;
//		ActionRegistry registry = getActionRegistry();
//
//		IExtensionRegistry reg = Platform.getExtensionRegistry();
//		IExtensionPoint extensionPoint = reg
//				.getExtensionPoint("de.bmotionstudio.gef.editor.schedulerEvent");
//		for (IExtension extension : extensionPoint.getExtensions()) {
//			for (IConfigurationElement configurationElement : extension
//					.getConfigurationElements()) {
//
//				if ("schedulerEvent".equals(configurationElement.getName())) {
//
//					String sClassName = configurationElement
//							.getAttribute("class");
//
//					action = new OpenSchedulerEventAction(this);
//					action.setId("de.bmotionstudio.gef.editor.SchedulerEventAction."
//							+ sClassName);
//					((OpenSchedulerEventAction) action)
//							.setClassName(sClassName);
//					registry.registerAction(action);
//					getSelectionActions().add(
//							"de.bmotionstudio.gef.editor.SchedulerEventAction."
//									+ sClassName);
//
//				}
//
//			}
//
//		}
//
//	}

	@Override
	public void dispose() {
		unregister();
		super.dispose();
	}

	private void unregister() {
		
		if (getCommandStack() != null)
			getCommandStack().removeCommandStackListener(this);
		if (getActionRegistry() != null)
			getActionRegistry().dispose();
		setInitialized(false);
		if (getVisualizationView() != null)
			getVisualizationView().removePropertyChangeListener(this);
		
		final AnimationSelector selector = injector
				.getInstance(AnimationSelector.class);
		selector.unregisterHistoryChangeListener(this);
//		if (getSimulation() != null)
//			getSimulation().removePropertyChangeListener(this);
		
	}

	@Override
	public void commandStackChanged(EventObject event) {
		updateActions(stackActions);
		simulation.setDirty(getCommandStack().isDirty());
	}

	/**
	 * A convenience method for updating a set of actions defined by the given
	 * List of action IDs. The actions are found by looking up the ID in the
	 * {@link #getActionRegistry() action registry}. If the corresponding action
	 * is an {@link UpdateAction}, it will have its <code>update()</code> method
	 * called.
	 * 
	 * @param actionIds
	 *            the list of IDs to update
	 */
	protected void updateActions(List<String> actionIds) {
		ActionRegistry registry = getActionRegistry();
		Iterator<String> iter = actionIds.iterator();
		while (iter.hasNext()) {
			IAction action = registry.getAction(iter.next());
			if (action instanceof UpdateAction)
				((UpdateAction) action).update();
		}
	}

	protected List<String> getStackActions() {
		return stackActions;
	}

	protected List<String> getPropertyActions() {
		return propertyActions;
	}

	protected List<String> getSelectionActions() {
		return selectionActions;
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		this.container = new RulerComposite(parent, SWT.NONE);
	}

	@Override
	public void setFocus() {
		this.container.setFocus();
	}

	public void init(Simulation simulation, VisualizationView visualizationView) {
		this.visualizationView = visualizationView;
		this.visualizationView.addPropertyChangeListener(this);
		this.simulation = simulation;
		this.simulation.addPropertyChangeListener(this);
		this.graphicalViewer = new ScrollingGraphicalViewer();
		this.graphicalViewer.createControl(this.container);
		Visualization visualization = visualizationView.getVisualization();
		this.editDomain = new EditDomain();
		this.editDomain.getCommandStack().addCommandStackListener(this);
		configureGraphicalViewer();
		hookGraphicalViewer();
		loadProperties(visualizationView);
		buildActions();
		createActions();
		createMenu(getViewSite());
		setPartName(visualizationView.getName());
		getGraphicalViewer().setContents(visualization);
		
		final AnimationSelector selector = injector
				.getInstance(AnimationSelector.class);
		selector.registerHistoryChangeListener(this);
		
		setInitialized(true);
		
	}

	protected void hookGraphicalViewer() {
		getSelectionSynchronizer().addViewer(getGraphicalViewer());
		getSite().setSelectionProvider(getGraphicalViewer());
	}

	public void configureGraphicalViewer() {

		rootEditPart = new ScalableRootEditPart();
		rootEditPart.setViewer(graphicalViewer);
		graphicalViewer.setRootEditPart(rootEditPart);
		graphicalViewer.setEditPartFactory(new BMSEditPartFactory());
		container
				.setGraphicalViewer((ScrollingGraphicalViewer) graphicalViewer);
		graphicalViewer.setEditDomain(getEditDomain());
		graphicalViewer
				.addDropTargetListener(new BControlTransferDropTargetListener(
						graphicalViewer));
		graphicalViewer.getControl().setBackground(ColorConstants.white);
		graphicalViewer
				.addDropTargetListener(new AttributeTransferDropTargetListener(
						graphicalViewer, getSite().getPart()));
		graphicalViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						updateActions(selectionActions);
					}
				});
		ContextMenuProvider provider = new BMSContextMenuProvider(
				graphicalViewer, getActionRegistry());
		graphicalViewer.setContextMenu(provider);

	}

	private void buildActions() {

		IActionBars bars = getViewSite().getActionBars();
		ActionRegistry ar = getActionRegistry();

		bars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
				ar.getAction(ActionFactory.UNDO.getId()));
		bars.setGlobalActionHandler(ActionFactory.REDO.getId(),
				ar.getAction(ActionFactory.REDO.getId()));

		bars.setGlobalActionHandler(ActionFactory.COPY.getId(),
				ar.getAction(ActionFactory.COPY.getId()));
		bars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
				ar.getAction(ActionFactory.PASTE.getId()));

		bars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
				ar.getAction(ActionFactory.DELETE.getId()));

		bars.updateActionBars();

	}

	private void createMenu(final IViewSite viewSite) {

		viewSite.getActionBars().getToolBarManager()
				.add(getActionRegistry().getAction(ActionFactory.UNDO.getId()));
		viewSite.getActionBars().getToolBarManager()
				.add(getActionRegistry().getAction(ActionFactory.REDO.getId()));
		viewSite.getActionBars().getToolBarManager()
				.add(getActionRegistry().getAction(ActionFactory.COPY.getId()));
		viewSite
				.getActionBars()
				.getToolBarManager()
				.add(getActionRegistry().getAction(ActionFactory.PASTE.getId()));
		viewSite
				.getActionBars()
				.getToolBarManager()
				.add(getActionRegistry()
						.getAction(ActionFactory.DELETE.getId()));

		viewSite.getActionBars().getToolBarManager().add(new Separator());

		viewSite.getActionBars().getToolBarManager()
				.add(getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
		viewSite
				.getActionBars()
				.getToolBarManager()
				.add(getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));

		viewSite
				.getActionBars()
				.getMenuManager()
				.add(getActionRegistry().getAction(
						GEFActionConstants.TOGGLE_GRID_VISIBILITY));
		viewSite
				.getActionBars()
				.getMenuManager()
				.add(getActionRegistry().getAction(
						GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));
		viewSite
				.getActionBars()
				.getMenuManager()
				.add(getActionRegistry().getAction(
						GEFActionConstants.TOGGLE_RULER_VISIBILITY));

		viewSite.getActionBars().updateActionBars();

		// TODO Reimplement me!
		// pageSite.getActionBars().getToolBarManager()
		// .add(new ZoomComboContributionItem(pageSite.getPage()));

	}

	protected void loadProperties(VisualizationView visualizationView) {

		// Ruler properties
		BMotionRuler ruler = visualizationView.getRuler(PositionConstants.WEST);
		RulerProvider provider = null;
		if (ruler != null)
			provider = new BMotionRulerProvider(ruler);
		getGraphicalViewer().setProperty(RulerProvider.PROPERTY_VERTICAL_RULER,
				provider);
		ruler = visualizationView.getRuler(PositionConstants.NORTH);
		provider = null;
		if (ruler != null)
			provider = new BMotionRulerProvider(ruler);
		getGraphicalViewer().setProperty(
				RulerProvider.PROPERTY_HORIZONTAL_RULER, provider);
		getGraphicalViewer().setProperty(
				RulerProvider.PROPERTY_RULER_VISIBILITY,
				visualizationView.isRulerVisible());

		getGraphicalViewer().setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED,
				visualizationView.isSnapToGeometryEnabled());
		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED,
				visualizationView.isGridEnabled());
		getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE,
				visualizationView.isGridEnabled());

		getGraphicalViewer().setProperty(
				MouseWheelHandler.KeyGenerator.getKey(SWT.NONE),
				MouseWheelZoomHandler.SINGLETON);

	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public Composite getParent() {
		return parent;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		String name = visualizationView.getName();
		boolean dirty = simulation.isDirty();

		String propertyName = evt.getPropertyName();

		if (propertyName.equals("name"))
			name = evt.getNewValue().toString();
		
		if (propertyName.equals("running"))
			dirty = Boolean.valueOf(evt.getNewValue().toString());
		
		if (propertyName.equals("dirty")
				|| propertyName.equals("name")) {

			if (dirty) {
				setPartName("*" + name);
			} else {
				setPartName(name);
			}

		}
		
	}

	@Override
	public void historyChange(History history) {
		
		if(visualizationView != null) {
			checkObserver(history);			
		}
		
	}
	
	public void checkObserver(final History history) {

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {

				for (VisualizationView visView : simulation
						.getVisualizationViews().values()) {

					Visualization visualization = visView.getVisualization();
					List<BControl> allBControls = new ArrayList<BControl>();
					allBControls.add(visualization);
					collectAllBControls(allBControls, visualization);
					for (BControl c : allBControls)
						c.checkObserver(history);

				}

			}

		});

	}

	private void collectAllBControls(List<BControl> allBControls,
			BControl control) {

		if (control.getChildren().isEmpty())
			return;

		for (BControl bcontrol : control.getChildren()) {
			allBControls.add(bcontrol);
			collectAllBControls(allBControls, bcontrol);
		}

	}

}