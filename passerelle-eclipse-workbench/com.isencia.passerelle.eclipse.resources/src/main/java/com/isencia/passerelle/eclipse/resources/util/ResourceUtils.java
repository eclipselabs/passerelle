package com.isencia.passerelle.eclipse.resources.util;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;


/**
 * Class designed to give access to operations without dependencies on eclipse.ui, just eclipse.resources.
 * 
 * Contains a static method to get the project.
 * 
 * @author fcp94556
 *
 */
public class ResourceUtils {

	
	public static IFile getProjectFile(final String modelPath) {

		final String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();

		// We must tell the composite actor the containing project name
		String relPath = modelPath.substring(workspacePath.length());
		IFile projFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);
		if (projFile == null) {
			relPath = modelPath.substring(workspacePath.length() + 2);
			projFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);

		}

		return projFile;
	}

	/**
	 * Attempts to find the project from the top CompositeActor by using the workspace name.
	 * 
	 * @param actor
	 * @return
	 * @throws Exception
	 */
	public static IProject getProject(final NamedObj actor) throws Exception {

		// Get top level actor, which knows the project we have.
		CompositeActor comp = (CompositeActor) actor.getContainer();
		while (comp.getContainer() != null) {
			comp = (CompositeActor) comp.getContainer();
		}

		String name = comp.workspace().getName();
		IProject project = null;
		if (!name.equals("")) {
			project = (IProject) ResourcesPlugin.getWorkspace().getRoot().findMember(name);
		}
		// Olof Svensson, 2013-09-17, DAWNSCI-734
		// I have reverted the patch submitted by Matthew on 2013-08-12, original comment:
		//// A project must be set on the actor by the editor.
		//// Now that the sub-models are not being provided by a project, we know that this is
		//// an error at this point.
		//TODO: Fix the problem (if possible) in the DAWN MessageSink actor
		if (project == null) {
			project = ResourceUtils.getPasserelleProject();
			//		      throw new Exception("The workspace must be defined as the same name as the project which the moml file is contained in!");
		}

		return project;
	}

	public static IProject getPasserelleProject() throws Exception {

		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		final IProject project = root.getProject(".passerelle");
		if (!project.exists()) {
			project.create(new NullProgressMonitor());
			try {
				project.setHidden(true);
			} catch (Exception ignored) {
				// It is not hidden for the test decks
			}
		}
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}
		return project;
	}



	/**
	 * 
	 * @param parameter
	 * @return IResource
	 */
	public static IResource getResource(Settable parameter) {
		String     path;
		if (parameter instanceof StringParameter) {
			path = ((StringParameter)parameter).getExpression();
		} else {
			path = parameter.getValueAsString();
		}
		return getResource(path);
	}
	
	public static IResource getResource(String path) {
		if (path.startsWith("\"")) {
			path = path.substring(1);
		}
		if (path.endsWith("\"")) {
			path = path.substring(0,path.length()-1);
		}
		if (path==null||"".equals(path)||"\"\"".equals(path)) return null;
		final IWorkspace space = ResourcesPlugin.getWorkspace();
		return space.getRoot().findMember(path);
	}

	/**
	 * Note really to do with resources but not sure where to put this...
	 * 
	 * @param parentModel
	 * @param name
	 * @return
	 */
	public static String findUniqueActorName(CompositeEntity parentModel, String name) {
		String newName = name;
		if (parentModel == null)
			return newName;
		List entityList = parentModel.entityList();
		if (entityList == null || entityList.size() == 0)
			return newName;

		ComponentEntity entity = parentModel.getEntity(newName);
		int i = 1;
		while (entity != null) {
			newName = name + "(" + i++ + ")";
			entity = parentModel.getEntity(newName);
		}

		return newName;
	}


}
