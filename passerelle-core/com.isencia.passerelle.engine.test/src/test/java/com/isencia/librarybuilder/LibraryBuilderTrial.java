/* Copyright 2011 - iSencia Belgium NV

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
package com.isencia.librarybuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import com.isencia.passerelle.model.util.MoMLParser;

import junit.framework.TestCase;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Const;
import ptolemy.kernel.Entity;

public class LibraryBuilderTrial extends TestCase {

//	public void testReadClassFile() throws Exception {
//		String fileName = "E:/iSencia/Soleil/workspaces/workspace-passerelle-v4/com.isencia.passerelle.engine/test/rrr.moml";
//		testFileParsing(fileName);
//	}

	private void testClassFileParsing(String fileName) throws Exception {
		MoMLParser parser = new MoMLParser();
		Entity e = (Entity) parser.parse(null, new File(fileName).toURI().toURL());
		assertNotNull("did not load an entity",e);
		assertTrue("the thing we loaded is not a class",e.isClassDefinition());
	}
	
	public void testWriteClassFile() throws Exception {
		TypedCompositeActor toplevel = new TypedCompositeActor();
		new Const(toplevel,"hello there");
		toplevel.setClassDefinition(true);

		File constFile = new File("C:/temp/const.moml");
		Writer classWriter = new FileWriter(constFile);
		
		toplevel.exportMoML(classWriter);
		classWriter.flush();
		classWriter.close();
		assertTrue("file write failed",constFile.exists());
		
		testClassFileParsing("C:/temp/const.moml");
	}

}
