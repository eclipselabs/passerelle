/* Copyright 2011 - iSencia Belgiums NV

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
package com.isencia.passerelle.ext;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;

import com.isencia.passerelle.util.Level;

public interface ExecutionTracer {
    
    public void trace(Actor source, String message,Level level);
    public void trace(Director source, String message,Level level);
    public void trace(Actor source, String message);
    public void trace(Director source, String message);
}
