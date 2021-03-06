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
import com.github.devconslejme.misc.GlobalManagerI;
import com.github.devconslejme.misc.jme.ColorI;
import com.github.devconslejme.misc.jme.UserDataI;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.anim.Animation;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.effect.AbstractEffect;
import com.simsilica.lemur.effect.Effect;
import com.simsilica.lemur.effect.EffectInfo;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.MouseEventControl;


// (tab indent=2 spaces)

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class HoverHighlightEffectI {
	public static HoverHighlightEffectI i(){return GlobalManagerI.i().get(HoverHighlightEffectI.class);}
	
	public static class EffectUserData{
		private QuadBackgroundComponent	qbcHighLightTarget;

		public QuadBackgroundComponent getQbcHighLightTarget() {
			return qbcHighLightTarget;
		}

		public EffectUserData setQbcHighLightTarget(QuadBackgroundComponent qbcHighLightTarget) {
			this.qbcHighLightTarget = qbcHighLightTarget;
			return this; //for beans setter
		}
	}
	
	private static enum EEffectIds{
		ChannelHighLight,
		
		EffectActivateHighLight,
		EffectDeactivateHighLight,
		
//		UserDataHighLightTarget,
		
		EffectDummy,
		;
		public String s(){ return toString(); }
	}
	
	
//	private Effect<Panel> efHighLightBkg = new AbstractEffect<Panel>("ChannelHighLight") {
	private Effect<Panel> efHighLightBkg = new AbstractEffect<Panel>(EEffectIds.ChannelHighLight.s()) {
		@Override
		public Animation create(final Panel pnlTarget, final EffectInfo effiExisting) {
			return new Animation() {
				EffectUserData eud = DetailedException.assertNotNull(UserDataI.i().getMustExistOrNull(pnlTarget, EffectUserData.class),this,pnlTarget);
				QuadBackgroundComponent qbc = eud.getQbcHighLightTarget();
				ColorRGBA colorBkp = qbc.getColor().clone();
				boolean bApplied=false;
				
				@Override	public void cancel() {
					qbc.setColor(colorBkp);
				}
				
				@Override	public boolean animate(double tpf) {
					if(!bApplied){
	//					if(existing!=null && existing.getAnimation()==this)return true;
						qbc.setColor(ColorI.i().neglightColor(colorBkp,(pnlTarget instanceof TextField)?0.1f:1f));
						bApplied=true;
					}
					return true;
				}
			};
		}
	};
	
	private static class HighlightMouseListener extends DefaultMouseListener{
		@Override
		public void mouseEntered(MouseMotionEvent event, Spatial target,				Spatial capture) {
			super.mouseEntered(event, target, capture);
			((Panel)target).runEffect(EEffectIds.EffectActivateHighLight.s());
		}
		
		@Override
		public void mouseExited(MouseMotionEvent event, Spatial target,				Spatial capture) {
			super.mouseExited(event, target, capture);
			((Panel)target).runEffect(EEffectIds.EffectDeactivateHighLight.s());
		}
	}
	private HighlightMouseListener hml = new HighlightMouseListener();
	
	private DummyEffect	efDummy;
	public void applyAt(Panel pnlToThisElement, QuadBackgroundComponent qbcTargetToBeHighlighted){
		MouseEventControl.addListenersToSpatial(pnlToThisElement, hml);
//		UserDataI.i().setUserDataPSHSafely(pnlToThisElement, EEffectIds.UserDataHighLightTarget.s(), qbcTargetToBeHighlighted);
		UserDataI.i().overwriteSafely(pnlToThisElement, new EffectUserData().setQbcHighLightTarget(qbcTargetToBeHighlighted));
//		pnl.setUserData(EEffectIds.UserDataHighLightTarget.s(), qbc);
		efDummy = setupSimpleEffect(pnlToThisElement, EEffectIds.EffectActivateHighLight, efHighLightBkg, efDummy);
		pnlToThisElement.addEffect(EEffectIds.EffectDeactivateHighLight.s(),efDummy);
	}

	/**
	 * 
	 * @param pnl
	 * @param strEffectId
	 * @param ef
	 * @param efDummy can be null initially, use a field variable
	 * @return dummy effect for re-use
	 */
	private DummyEffect setupSimpleEffect(Panel pnl, EEffectIds e, Effect ef, DummyEffect efDummy){
		if(efDummy==null)efDummy=new DummyEffect(EEffectIds.EffectDummy.s(),ef.getChannel());
		
		if(!efDummy.getChannel().equals(ef.getChannel())){
			throw new DetailedException("both should be on the same channel", efDummy, e.s(), ef, pnl, this);
		}
		
		if(e.compareTo(EEffectIds.EffectDummy)==0){
			throw new DetailedException("ids should differ", EEffectIds.EffectDummy, efDummy, e.s(), ef, pnl, this);
		}
		
		pnl.addEffect(e.s(), (Effect)ef);
		pnl.addEffect(EEffectIds.EffectDummy.s(), efDummy);
		
		return efDummy;
	}

	/**
	 * this is just to let actual effects to end themselves with their own cancel()
	 */
	private static class DummyEffect extends AbstractEffect{
		private String	strId;
		public DummyEffect(String strId, String channel){
			super(channel);
			this.strId=strId;
		}
		
		public String getId(){
			return this.strId;
		}
		
		@Override
		public Animation create(Object target, EffectInfo existing) {
			return new Animation() {
				@Override	public void cancel() {}
				@Override	public boolean animate(double tpf) {return true;}
			};
		}
	}

}
