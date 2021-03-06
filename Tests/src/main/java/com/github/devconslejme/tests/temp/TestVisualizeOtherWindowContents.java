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
package com.github.devconslejme.tests.temp;

import com.github.devconslejme.extras.OSCmd;
import com.github.devconslejme.gendiag.DialogHierarchyStateI;
import com.github.devconslejme.misc.GlobalManagerI;
import com.github.devconslejme.misc.MessagesI;
import com.github.devconslejme.misc.QueueI;
import com.github.devconslejme.misc.QueueI.CallableXAnon;
import com.github.devconslejme.misc.jme.MiscJmeI;
import com.github.devconslejme.misc.lemur.ResizablePanel;
import com.github.devconslejme.misc.lemur.SizeAndLocationI;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetNotFoundException;
import com.jme3.math.Vector3f;
import com.jme3.ui.Picture;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.style.BaseStyles;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class TestVisualizeOtherWindowContents extends SimpleApplication{
	public static void main(String[] args) {
		assert(true);
		TestVisualizeOtherWindowContents test = new TestVisualizeOtherWindowContents();
		test.start();
	}

	private OSCmd	oscmd;
	private Long	lWindowId;
	private float	fTime;
	private ResizablePanel	rzp;
	private IconComponent	ic;
	private boolean	bInitialized;
	protected TextField	tf;
	protected Picture	pic;
	private int	iTryMode=1;
	private Panel	pnl;
	private SimpleApplication	sapp;

	@Override
	public void simpleInitApp() {
		GuiGlobals.initialize(this);
		BaseStyles.loadGlassStyle();
		GuiGlobals.getInstance().getStyles().setDefaultStyle(BaseStyles.GLASS);
		com.github.devconslejme.gendiag.PkgCfgI.i().configure(this,getGuiNode(), getRootNode());
		
		initTest();
	}
	
	/**
	 * public so can be called from devcons user cmds
	 */
	public void initTest() {
		sapp = GlobalManagerI.i().get(SimpleApplication.class); //ca be this class or the devcons test one
		
		QueueI.i().enqueue(new CallableXAnon() {
			@Override
			public Boolean call() {
				oscmd = new OSCmd();
				
				init();
				
		//		ic=new IconComponent(getFileName());ic.setImageTexture(GuiGlobals.getInstance().loadTexture(getFileName(), false, false));pnl.getContents().setBackground(ic); //TODO rm
				bInitialized=true;
				
				pic = new Picture();
				
				return true;
			}
		});
	}
	
	private void init(){
		if(iTryMode==0){
			rzp = DialogHierarchyStateI.i().prepareDialogParts(this.getClass().getSimpleName(),null).getDialog();
			MiscJmeI.i().addToName(rzp, this.getClass().getSimpleName(), true);
			rzp.setContents(new Panel());
			rzp.setPreferredSizeWH(new Vector3f(100,100,Float.NaN));
			DialogHierarchyStateI.i().showDialog(rzp);
			
			if(false){
				tf = new TextField("TypeWindowIdHere");
				sapp.getGuiNode().attachChild(tf);
				tf.setLocalTranslation(new Vector3f(300,300,0));
				tf.setPreferredSize(new Vector3f(300,50,SizeAndLocationI.i().getMinSizeZ()));
			}
		}
		if(iTryMode==1){
//			Button btn = new Button("oi", BaseStyles.GLASS);
//			btn.setPreferredSize(new Vector3f(100,100,0.01f));
//			btn.setLocalTranslation(new Vector3f(100,400,0));
//			getGuiNode().attachChild(btn);
			
			pnl = new Panel(BaseStyles.GLASS);
			SizeAndLocationI.i().setPreferredSize(pnl,new Vector3f(100,100,0.01f));
			SizeAndLocationI.i().setLocalTranslationXY(pnl,new Vector3f(100,300,0));
			
			ic = new IconComponent(getFileName());
			pnl.setBackground(ic);
			
			sapp.getGuiNode().attachChild(pnl);
		}
	}
	private boolean applyTexture(){
		if(iTryMode==0){
			if(ic==null){
				try{
					ic=new IconComponent(getFileName());
				}catch(AssetNotFoundException ex){
					MessagesI.i().warnMsg(this, "file not found", getFileName(), ex);
					return false;
				}
				rzp.getContents().setBackground(ic);
			}
			ic.setImageTexture(GuiGlobals.getInstance().loadTexture(getFileName(), false, false));
			/**
			 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			 * the above line is probably causing this exception stack whenever trying to open a gendiag !!! :O
					ListSort<T>.mergeHigh(int, int, int, int) line: 855	
					ListSort<T>.mergeRuns(int) line: 476	
					ListSort<T>.mergeCollapse() line: 407	
					ListSort<T>.sort(T[], Comparator<T>) line: 233	
					GeometryList.sort() line: 158	
					RenderQueue.renderGeometryList(GeometryList, RenderManager, Camera, boolean) line: 262	
					RenderQueue.renderQueue(RenderQueue$Bucket, RenderManager, Camera, boolean) line: 302	
					RenderManager.renderViewPortQueues(ViewPort, boolean) line: 898	
					RenderManager.flushQueue(ViewPort) line: 781	
					RenderManager.renderViewPort(ViewPort, float) line: 1097	
					RenderManager.render(float, boolean) line: 1153	
					TestDevCons(SimpleApplication).update() line: 253	
					LwjglDisplay(LwjglAbstractDisplay).runLoop() line: 151	
					LwjglDisplay.runLoop() line: 193	
					LwjglDisplay(LwjglAbstractDisplay).run() line: 232	
					Thread.run() line: 745	
			 */
	//		ic.setIconScale(MiscJmeI.i().toV2f(pnl.getContents().getSize()));
			ic.setIconScale(0.25f);
		}
		if(iTryMode==1){
//			sapp.getAssetManager().deleteFromCache(new TextureKey(getFileName()));
//			ic.setImageTexture(GuiGlobals.getInstance().loadTexture(getFileName(), false, false));
			sapp.getAssetManager().clearCache();
			pnl.setBackground(new IconComponent(getFileName()));
		}
		
		return true;
	}
	
	@Override
	public void simpleUpdate(float tpf) {
		if(!bInitialized)return;
		
		if(false)try{setWindowId(Long.parseLong(tf.getText()));}catch(NumberFormatException ex){}
		
		if(getWindowId()!=null){
			fTime+=tpf;
			if(fTime>3f){
				String strCmd = "import -window "+getWindowId()+" ./bin/"+getFileName()+"";
				if(oscmd.runLinuxCmd(strCmd)){
					QueueI.i().enqueue(new CallableXAnon() {
						@Override
						public Boolean call() {
							if(!applyTexture())return false;
							return true;
						}
					});
				}else{
					setWindowId(null);
					MessagesI.i().warnMsg(this, "command failed", strCmd);
				}
				fTime=0;
			}
		}
	}

	private String getFileName() {
		return "."+TestVisualizeOtherWindowContents.class.getSimpleName()+"-TEMP.jpg";
	}

	public Long getWindowId() {
		return lWindowId;
	}

	public TestVisualizeOtherWindowContents setWindowId(Long lWindowId) {
		this.lWindowId = lWindowId;
		return this;
	}
	
}