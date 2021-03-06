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
package com.github.devconslejme.gendiag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.devconslejme.es.DialogHierarchyComp.DiagCompBean;
import com.github.devconslejme.es.DialogHierarchySystemI;
import com.github.devconslejme.gendiag.DialogHierarchyStateI.DialogVisuals;
import com.github.devconslejme.misc.GlobalManagerI;
import com.github.devconslejme.misc.HierarchySorterI.EHierarchyType;
import com.github.devconslejme.misc.QueueI;
import com.github.devconslejme.misc.QueueI.CallableXAnon;
import com.github.devconslejme.misc.jme.HWEnvironmentJmeI;
import com.github.devconslejme.misc.jme.TextStringI;
import com.github.devconslejme.misc.lemur.AbsorbClickCommandsI;
import com.github.devconslejme.misc.lemur.PopupHintHelpListenerI;
import com.github.devconslejme.misc.lemur.ResizablePanel;
import com.github.devconslejme.misc.lemur.ResizablePanel.IResizableListener;
import com.jme3.audio.Environment;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class MinimizedDialogsPanelI implements IResizableListener{
	public static MinimizedDialogsPanelI i(){return GlobalManagerI.i().get(MinimizedDialogsPanelI.class);}
	
	private ResizablePanel	minimizedDiags;
	private Container	cntrMinimized;
	private DialogHierarchyStateI	dhs;
	private DialogHierarchySystemI	sys;
	private Node	nodeToMonitor;
	protected boolean	bInitialized;
	private float fMinSize=30; //TODO auto find out the current style font height
	protected DialogVisuals	vs;
	
	public void configure(Node nodeToMonitor){
		dhs=DialogHierarchyStateI.i();
		sys=DialogHierarchySystemI.i();
		this.nodeToMonitor=nodeToMonitor;
		
		QueueI.i().enqueue(new CallableXAnon() {
			@Override
			public Boolean call() {
				if(!bInitialized)return false;
				
				boolean bShow = false;
				if(HWEnvironmentJmeI.i().getMouse().isCursorVisible()){
					if(cntrMinimized.getLayout().getChildren().size() > 0){
						bShow=true;
					}
				}
				
				if(bShow){
					if(minimizedDiags.getParent()==null)nodeToMonitor.attachChild(minimizedDiags);
				}else{
					if(minimizedDiags.getParent()!=null)minimizedDiags.removeFromParent();
				}
				
				return true;
			}
		}.enableLoopMode().setDelaySeconds(1f).setName("Show/Hide minimized dialogs' panel"));
		
		QueueI.i().enqueue(new CallableXAnon() {
			@Override
			public Boolean call() {
				vs = dhs.prepareDialogParts("Minimized dialogs panel", null);
				minimizedDiags = vs.getDialog();
				minimizedDiags.addResizableListener(MinimizedDialogsPanelI.this);
				minimizedDiags.setApplyContentsBoundingBoxSize(false);
				minimizedDiags.setLocalTranslationXY(new Vector3f(0,HWEnvironmentJmeI.i().getDisplay().getHeight(),Float.NaN));
				minimizedDiags.setPreferredSizeWH(new Vector3f(HWEnvironmentJmeI.i().getDisplay().getWidth(),fMinSize,Float.NaN));
				sys.setHierarchyComp(vs.getEntityId(), new DiagCompBean().setHierarchyType(EHierarchyType.Top));
				
				cntrMinimized=new Container();
				minimizedDiags.setContents(cntrMinimized);
				
				bInitialized=true;
				return true;
			}
		});
	}
	
	public static class ButtonMinimized extends Button{
		private AbstractGenericDialog	gendiag;

		public ButtonMinimized(String s) {
			super(s);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void minimize(AbstractGenericDialog sgd) {
		if(dhs.getHierarchyComp(sgd.getDialog()).getHierarchyParent()!=null)return;
		
		ButtonMinimized btn = new ButtonMinimized(sgd.getTitle());
		btn.gendiag = sgd;
		PopupHintHelpListenerI.i().setPopupHintHelp(btn, btn.getText());
		TextStringI.i().recursivelyApplyTextNoWrap(btn);
		//TODO rotate text 90degreess if in the vertical?
		
		addMinimized(btn, cntrMinimized.getLayout().getChildren().size());
//		cntrMinimized.addChild(btn, cntrMinimized.getLayout().getChildren().size());
//		btn.addClickCommands(new Command<Button>() {
		sgd.getDialogVisuals().setMinimizedButton(btn);
		AbsorbClickCommandsI.i().addClickCommands(btn,new Command<Button>() {
			@Override
			public void execute(Button source) {
				assert btn==source;
				restoreDialog(sgd);
			}

		});
		
		sgd.close();
	}
	
	public void restoreDialog(AbstractGenericDialog sgd){
		DialogHierarchyStateI.i().showDialog(sgd.getDialog());
		sgd.getDialogVisuals().resetMinimizedButton();
		update();
	}
	
	private void update() {
		if(minimizedDiags.getParent()==null)return;
		
//		if(HWEnvironmentJmeI.i().getMouse().isCursorVisible()){
//			if(cntrMinimized.getParent()==null)todo;
			
			//read remaining
			ArrayList<Node> children = new ArrayList<Node>(cntrMinimized.getLayout().getChildren()); //remaining
			cntrMinimized.getLayout().clearChildren();
			int i=0;for(Node child:children){
				Button btnChild = (Button)child;
				addMinimized(btnChild, i++);
				Vector3f v3fPrefSize = btnChild.getPreferredSize();
			}
			
			Vector3f v3fSize = minimizedDiags.getSize().clone();
			if(v3fSize.x<fMinSize)v3fSize.x=fMinSize;
			if(v3fSize.y<fMinSize)v3fSize.y=fMinSize;
			minimizedDiags.setPreferredSizeWH(v3fSize);
//		}else{
//			if(cntrMinimized.getParent()!=null)cntrMinimized.removeFromParent();
//		}
	}
	
	private void addMinimized(Node node,int iIndex){
		Vector3f v3fSize = cntrMinimized.getSize();
		if(v3fSize.x>=v3fSize.y){
			cntrMinimized.addChild(node, 0, iIndex);
		}else{
			cntrMinimized.addChild(node, iIndex, 0);
		}
	}

	@Override
	public void resizableUpdatedLogicalStateEvent(float tpf,			ResizablePanel rzpSource) {
	}

	@Override
	public void resizableRemovedFromParentEvent(ResizablePanel rzpSource) {	}

	@Override
	public void resizableStillResizingEvent(ResizablePanel rzpSource, Vector3f v3fNewSize) {
		update();
	}

	@Override
	public void resizableEndedResizingEvent(ResizablePanel rzpSource) { }

	public ArrayList<ResizablePanel> getAllOpenedAndMinimizedDialogs() {
		ArrayList<ResizablePanel> a = new ArrayList<ResizablePanel>();
		List<ButtonMinimized> descendantMatches = cntrMinimized.descendantMatches(ButtonMinimized.class, null);
		for(ButtonMinimized btn:descendantMatches){
			a.add(btn.gendiag.getDialog());
		}
		return a;
	}
	
}
