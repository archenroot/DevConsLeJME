/* 
	Copyright (c) 2017, Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
	
	All rights reserved.

	Redistribution and use in source and binary forms, with or without modification, are permitted 
	provided that the following conditions are met:

	1.	Redistributions of source code must retain the above copyright notice, this list of conditions 
		and the following disclaimer.

	2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
		and the following disclaimer in the documentation and/or other materials provided with the distribution.
	
	3.	Neither the name of the copyright holder nor the names of its contributors may be used to endorse 
		or promote products derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED 
	WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
	PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
	LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
	INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN 
	IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.github.devconslejme.tests;

import com.github.devconslejme.misc.GlobalManagerI;
import com.github.devconslejme.misc.lemur.HoverHighlightEffectI;
import com.github.devconslejme.misc.lemur.ResizablePanel;
import com.github.devconslejme.projman.SimpleApplicationAndStateAbs;
import com.jme3.app.SimpleApplication;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.component.QuadBackgroundComponent;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class TestResizablePanel extends SimpleApplicationAndStateAbs {
	public static void main(String[] args) {
		TestResizablePanel tst = new TestResizablePanel();
		tst.start();
	}

	private SimpleApplication	sapp;
	
	@Override
	public void simpleInitApp() {
		com.github.devconslejme.misc.lemur.PkgCfgI.i().configure(this, getGuiNode(), getRootNode());
		initTest();
	}
	
	@Override
	public void update(float tpf) {}
	
	@Override
	public void initTest() {
		super.initTest();
		
		sapp = GlobalManagerI.i().get(SimpleApplication.class); // may not be this
		
		int i=300;
		test(new Vector3f(100,i+100,10));
		test(new Vector3f(200,i+200,20));
		test(new Vector3f(300,i+300,30));
	}

	private void test(Vector3f pos) {
		ResizablePanel rzp = new ResizablePanel(null);
		rzp.setPreferredSizeWH(new Vector3f(300,200,0));
		rzp.setLocalTranslationXY(pos); //above DevCons
		sapp.getGuiNode().attachChild(rzp); //will not use the dialog hierarchy!
		
		HoverHighlightEffectI.i().applyAt(rzp, (QuadBackgroundComponent)rzp.getResizableBorder());
		
		Button btn = new Button("drag borders to resize:"+pos);
//		btn.setBackground(new QuadBackgroundComponent(ColorRGBA.Red.clone()));//,5,5, 0.02f, false));
		rzp.setContents(btn);
	}

}
