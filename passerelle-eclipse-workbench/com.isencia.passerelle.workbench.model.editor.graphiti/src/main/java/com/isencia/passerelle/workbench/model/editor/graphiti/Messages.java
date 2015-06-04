/* Copyright 2013 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.workbench.model.editor.graphiti;


import org.eclipse.osgi.util.NLS;

/**
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$
	public static String CreateDiagramWizard_DiagramNameField;
	public static String CreateDiagramWizard_DiagramTypeField;
	public static String CreateDiagramWizard_ErrorOccuredTitle;
	public static String CreateDiagramWizard_NoProjectFoundError;
	public static String CreateDiagramWizard_NoProjectFoundErrorTitle;
	public static String CreateDiagramWizard_OpeningEditorError;
	public static String CreateDiagramWizard_WizardTitle;
	public static String DiagramNameWizardPage_PageDescription;
	public static String DiagramNameWizardPage_PageTitle;
	public static String DiagramNameWizardPage_Message;
	public static String DiagramNameWizardPage_Label;
	public static String DiagramsNode_DiagramNodeTitle;
	public static String DiagramTypeWizardPage_DiagramTypeField;
	public static String DiagramTypeWizardPage_PageDescription;
	public static String DiagramTypeWizardPage_PageTitle;
  public static String FileService_ErrorMessageCause;
  public static String FileService_ErrorMessageStart;
  public static String FileService_ErrorMessageUri;
  
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
