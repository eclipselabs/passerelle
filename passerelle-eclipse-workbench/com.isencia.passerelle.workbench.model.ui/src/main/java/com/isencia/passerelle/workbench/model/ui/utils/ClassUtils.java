package com.isencia.passerelle.workbench.model.ui.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.osgi.framework.Bundle;

/**
 * @author Dirk Jacobs
 * 
 */
public class ClassUtils {

	public static URLClassLoader getURLClassLoader(IResource resource) throws MalformedURLException {
		IJavaElement javaElement = getJavaElement(resource);
		IJavaProject javaProject = javaElement.getJavaProject();
		URL[] urls = getPasserelleClassPath(javaProject);
		return getURLClassLoader(urls);
	}

	public static URLClassLoader getURLClassLoader(URL[] urls) throws MalformedURLException {
		return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
	}


	public static URL[] getPasserelleClassPath(IJavaProject project) {
		List classPathPaths = new ArrayList();
		if (project != null) {
			classPathPaths.addAll(ClassUtils.getProjectClassPathPaths(project));
		}
		IPath[] paths = (IPath[]) classPathPaths.toArray(new IPath[classPathPaths.size()]);
		try {
			return getRawLocationsURLForResources(paths);
		} catch (MalformedURLException e) {
		}
		return null;
	}

	public static List<IPath> getProjectClassPathPaths(IJavaProject project) {
		ArrayList<IPath> pathElements = new ArrayList<IPath>();

		try {
			IClasspathEntry paths[] = project.getResolvedClasspath(true);

			if (paths != null) {
				for (int i = 0; i < paths.length; i++) {
					IClasspathEntry path = paths[i];
					if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						IPath simplePath = path.getPath();
						pathElements.add(simplePath);
					}
				}
			}

			IPath location = getProjectLocation(project.getProject());
			IPath outputPath = location.append(project.getOutputLocation().removeFirstSegments(1));

			pathElements.add(outputPath);
		} catch (JavaModelException e) {
			// ignore : project classpath could not be calculated,return empty list
		}

		return pathElements;
	}

	public static URL getRawLocationURL(IPath simplePath) throws MalformedURLException {
		File file = getRawLocationFile(simplePath);
		return file.toURL();
	}

	public static String getRawLocation(IPath simplePath) throws MalformedURLException {
		IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(simplePath);
		if (member != null) {
			IPath rawLocation = member.getRawLocation();
			if (rawLocation == null) {
				rawLocation = member.getLocation();
				if (rawLocation == null) {
					throw new RuntimeException("Could not determine physical location for " + simplePath);
				}
			}
			return rawLocation.toOSString();
		}
		return simplePath.makeAbsolute().toOSString();
	}

	public static File getRawLocationFile(IPath simplePath) {
		IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(simplePath);
		File file = null;
		if (member != null) {
			IPath rawLocation = member.getRawLocation();
			if (rawLocation == null) {
				rawLocation = member.getLocation();
				if (rawLocation == null) {
					throw new RuntimeException("Could not determine physical location for " + simplePath);
				}
			}
			file = rawLocation.toFile();
		} else {
			file = simplePath.toFile();
		}
		return file;
	}

	public static IPath getProjectLocation(IProject project) {
		if (project.getRawLocation() == null) {
			return project.getLocation();
		} else
			return project.getRawLocation();
	}

	public static URL[] getRawLocationsURLForResources(IPath[] classpaths) throws MalformedURLException {
		URL[] l = new URL[classpaths.length];
		for (int i = 0; i < classpaths.length; i++) {
			l[i] = getRawLocationURL(classpaths[i]);
		}
		return l;
	}

	public static String[] getRawLocationsForResources(IPath[] classpaths) throws MalformedURLException {
		String[] l = new String[classpaths.length];
		for (int i = 0; i < classpaths.length; i++) {
			l[i] = getRawLocation(classpaths[i]);
		}
		return l;
	}

	public static IJavaElement getJavaElement(IResource obj) {
		if (obj != null) {
			IJavaElement je = JavaCore.create((IResource) obj);
			if (je == null) {
				IProject pro = ((IResource) obj).getProject();
				je = JavaCore.create(pro);
			}
			if (je != null) {
				return je;
			}
		}
		return null;
	}

	public static IPath[] buildProjectClasspath(ILaunchConfiguration configuration, IJavaProject javaProject)
		throws CoreException, MalformedURLException {
		List classPathPaths = new ArrayList();

		if (javaProject != null) {
			// add project classpath
			classPathPaths.addAll(ClassUtils.getProjectClassPathPaths(javaProject));
		}

		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entries[i].getLocation();
				if (location != null) {
					classPathPaths.add(entries[i].getPath());
				}
			}
		}
		return (IPath[]) classPathPaths.toArray(new IPath[classPathPaths.size()]);
	}

}
