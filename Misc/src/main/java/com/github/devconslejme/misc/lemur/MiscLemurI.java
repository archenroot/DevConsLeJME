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

package com.github.devconslejme.misc.lemur;

import com.github.devconslejme.misc.DetailedException;
import com.github.devconslejme.misc.GlobalInstanceManagerI;
import com.github.devconslejme.misc.jme.MiscJmeI;
import com.jme3.font.BitmapText;
import com.jme3.math.FastMath;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.TextComponent;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.grid.GridModel;

/**
 * DevSelfNote: Misc lib class should not exist. As soon coehsion is possible, do it!
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class MiscLemurI {
	public static MiscLemurI i(){return GlobalInstanceManagerI.i().get(MiscLemurI.class);}
	
	public Integer getEntryHeightPixels(ListBox lstbx){
		GridModel<Panel> gm = lstbx.getGridPanel().getModel();
		if(gm.getRowCount()==0)throw new DetailedException("list must not be empty");
		Panel pnl = gm.getCell(0, 0, null); // create a new cell
		float fHeight = pnl.getPreferredSize().getY();
		
		return (int)FastMath.ceil(fHeight);
	}
	
	public int getFontCharWidthForStyle(String strStyle){ //TODO check as it should be mono spaced font...
		// this detached label has no preferred size etc, so the line width will be as shrinked as possible
		String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		Label lbl = new Label(str,strStyle);
//		float fLineWidth = MiscJmeI.i().getBitmapTextFrom(lbl).getLineWidth();
		float fLineWidth = MiscJmeI.i().getChildRecursiveExactMatch(lbl,BitmapText.class).getLineWidth();
		int iCharWidthPixels = Math.round(fLineWidth/=str.length());
		
		return iCharWidthPixels;
	}
	
	public TextEntryComponent getTextEntryComponentFrom(TextField tf){
		return (TextEntryComponent)tf.getControl(GuiControl.class).getComponent(TextField.LAYER_TEXT);
	}
	
	public TextComponent getTextComponentFrom(Label lbl){
		return lbl.getControl(GuiControl.class).getComponent(Label.LAYER_TEXT);
	}
}
