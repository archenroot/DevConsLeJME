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

import com.github.devconslejme.misc.GlobalManagerI;
import com.github.devconslejme.misc.MessagesI;
import com.github.devconslejme.misc.jme.MiscJmeI;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.event.AbstractCursorEvent;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.CursorListener;
import com.simsilica.lemur.event.CursorMotionEvent;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class DragParentestPanelListenerI implements CursorListener{
	public static DragParentestPanelListenerI i(){return GlobalManagerI.i().get(DragParentestPanelListenerI.class);}
	
	private boolean bDragging = false;
	private boolean bHightlightToo = true;
	private Vector3f	v3fDistToCursor;
	private Panel	pnlParentestBeingDragged;
	
	
	private Vector3f getCursorPos(AbstractCursorEvent event){
		return new Vector3f(event.getX(),event.getY(),0);
	}
	
	@Override
	public void cursorButtonEvent(CursorButtonEvent event, Spatial target,				Spatial capture) {
//		if(event.isConsumed())return;
		
		if(event.getButtonIndex()==0){
			bDragging=event.isPressed();
			if(bDragging){
				//find parentest 
				Panel pnlParentest = (Panel)capture.getUserData(getUserDataIdFor(EDrag.ApplyDragAt));
				if(pnlParentest==null)pnlParentest = MiscJmeI.i().getParentest(capture, Panel.class, true);
				
				// base dist calc
				v3fDistToCursor=pnlParentest.getWorldTranslation().subtract(getCursorPos(event));
				v3fDistToCursor.z=0; //DO NOT MESS WITH Z!!!!
			}else{
				pnlParentestBeingDragged=null;
			}
			event.setConsumed();
		}
	}
	
	public Panel getParentestBeingDragged(){
		return pnlParentestBeingDragged;
	}
	
//	private void highlightBackground(Spatial spt, boolean bHighLight){
//		if(spt instanceof Panel){
//			Panel pnl = (Panel)spt;
//			GuiComponent gc = pnl.getBackground();
//			if (gc instanceof QuadBackgroundComponent) {
//				QuadBackgroundComponent qbc = (QuadBackgroundComponent) gc;
//				ColorRGBA color = qbc.getColor().clone();
//				
//				String strId = getUserDataIdFor(EDrag.ColorHighlight);
//				ColorRGBA colorStored = (ColorRGBA)spt.getUserData(strId);
//				if(colorStored==null){
//					colorStored=color.clone();
//					spt.setUserData(strId, colorStored);
//				}
//				
//				if(bHighLight){
//					qbc.setColor(ColorI.i().neglightColor(color));
//				}else{
//					qbc.setColor(colorStored);
//				}
//			}
//		}
//	}
	
	@Override
	public void cursorEntered(CursorMotionEvent event, Spatial target,				Spatial capture) {
//		highlightBackground(target,true);
	}
	
	@Override
	public void cursorExited(CursorMotionEvent event, Spatial target,				Spatial capture) {
//		highlightBackground(target,false);
	}
	
	@Override
	public void cursorMoved(CursorMotionEvent event, Spatial target,				Spatial capture) {
		if(bDragging){ //((Panel)capture).getPreferredSize() ((Panel)capture).getSize()
			// find parentest
			Panel pnlParentest = (Panel)capture.getUserData(getUserDataIdFor(EDrag.ApplyDragAt));
			if(pnlParentest==null)pnlParentest = MiscJmeI.i().getParentest(capture, Panel.class, true);
			
			// position parentest
			Vector3f v3f = getCursorPos(event).add(v3fDistToCursor);
			v3f.z=pnlParentest.getLocalTranslation().z; //DO NOT MESS WITH Z!!!!
			pnlParentest.setLocalTranslation(v3f);
			
			pnlParentestBeingDragged = pnlParentest;
			
			event.setConsumed();
		}
	}

	public void applyAt(Panel pnl) {
		applyAt(pnl,null);
		if(isHightlightToo()){
			GuiComponent gc = pnl.getBackground();
			if (gc instanceof QuadBackgroundComponent) {
				QuadBackgroundComponent qbc = (QuadBackgroundComponent) gc;
				HoverHighlightEffectI.i().applyAt(pnl, qbc);
			}else{
				MessagesI.i().debugInfo(this, "unable to apply "+HoverHighlightEffectI.class.getSimpleName(), pnl, gc);
			}
		}
	}
	
	/**
	 * 
	 * @param pnl
	 * @param pnlApplyDragAt can be used to move a non attached/linked/parent panel
	 */
	public void applyAt(Panel pnl, Panel pnlApplyDragAt) {
		CursorEventControl.addListenersToSpatial(pnl, this);
		if(pnlApplyDragAt!=null){
			pnl.setUserData(getUserDataIdFor(EDrag.ApplyDragAt), pnlApplyDragAt);
			pnlApplyDragAt.setUserData(getUserDataIdFor(EDrag.ApplyingDragFrom), pnl);
		}
	}
	
	private static enum EDrag{
		ApplyDragAt, 
		ApplyingDragFrom, 
//		ColorHighlight,
		;
	}
	
	private String getUserDataIdFor(EDrag e){
		return DragParentestPanelListenerI.class.getName()+"/"+e;
	}

	public boolean isHightlightToo() {
		return bHightlightToo;
	}

	public void setHightlightToo(boolean bHightlightToo) {
		this.bHightlightToo = bHightlightToo;
	}
}