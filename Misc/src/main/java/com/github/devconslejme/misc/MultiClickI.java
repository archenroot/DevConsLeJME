/* 
	Copyright (c) 2016-2017, Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
	
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

package com.github.devconslejme.misc;

import com.github.devconslejme.misc.QueueI.CallableX;
import com.github.devconslejme.misc.QueueI.CallableXAnon;
import com.github.devconslejme.misc.jme.OriginDevice.MultiClickAxis;


/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class MultiClickI {
	public static MultiClickI i(){return GlobalManagerI.i().get(MultiClickI.class);}
	
	private boolean bDebug=false;
	
	private long lMaxDelayMilis=500;
	
	public long getMaxDelayMilis() {
		return lMaxDelayMilis;
	}
	
	/**
	 * between click to recogniize as valid multi-clicking mode
	 * @param lMaxDelayMilis
	 * @return
	 */
	public MultiClickI setMaxDelayMilis(long lMaxDelayMilis) {
		this.lMaxDelayMilis = lMaxDelayMilis;
		return this; 
	}

	public boolean isDebug() {
		return bDebug;
	}

	public MultiClickI setDebug(boolean bDebug) {
		this.bDebug = bDebug;
		return this; 
	}
	
	public static abstract class CallMultiClickUpdate extends CallableX<CallMultiClickUpdate>{
		private MultiClick mc;
		
		@Deprecated
		@Override
		public Boolean call() {
			if(mc.isTimeLimitReached())mc.bReady=true;
			if(!mc.isReady())return true;
			
			applyMultiClick(mc.getTotalClicks());
			mc.reset();
			
			return true;
		}

		public abstract void applyMultiClick(int totalClicks);
	}
	
	public static class MultiClick <SELF extends MultiClick<SELF>>{
		private int	iButtonIndex;
		
		private Long lLastClickMilis;
		private int	iClickCount;
		private boolean bReady;

		private String[]	astrHelpPerClickCountIndex;

		private int	iMaxClicks;

		private CallableX	cxUpdate;
		
		public SELF reset() {
			init();
			QueueI.i().removeLoopFromQueue(cxUpdate);
			return getThis();
		}
		private void init() {
			bReady=false;
			lLastClickMilis=null;
			iClickCount=0;
		}
		
		public MultiClick(int iButtonIndex, int iMaxClicks, CallMultiClickUpdate cxUpdate){
			assert iMaxClicks>=2 : "the max's min is two clicks, single click does not need this...";
			
			this.iButtonIndex=iButtonIndex;
			this.iMaxClicks=iMaxClicks;
			this.cxUpdate=cxUpdate.enableLoopMode().setName(this.getClass().getSimpleName()+"_UpdateLoop");
			cxUpdate.mc=this;
			
			init();
		}
		
		@SuppressWarnings("unchecked")
		public SELF getThis(){
			return (SELF)this;
		}
		
		public SELF setHelp(String string, String... astrHelpPerClickCountIndex) {
			this.astrHelpPerClickCountIndex=astrHelpPerClickCountIndex;
			return getThis();
		}
		
		public String getHelp(){
			String str="";
			int i=0;
			for(String strHelp:astrHelpPerClickCountIndex){
				str+=++i+":"+strHelp+"\n";
			}
			return str;
		}
		
		public boolean isReady(){
			return bReady;
		}
		
		/**
		 * 
		 * @param iMaxClicks min of 2
		 * @return
		 */
		@SuppressWarnings("unchecked")
		public void updateIncClicks() {
			if(bReady)return; //just skip as limit reached
			
			QueueI.i().enqueue(cxUpdate);
			
			long lMilis = System.currentTimeMillis();
			if(lLastClickMilis==null){
				lLastClickMilis=lMilis;
				iClickCount++;
			}else{
//				if( (lMilis-lLastClickMilis)>MultiClickI.i().getMaxDelayMilis() ){
				if(isTimeLimitReached()){
					bReady=true;
				}else{
					lLastClickMilis=lMilis;
					iClickCount++;
				}
				
				if(iClickCount==iMaxClicks)bReady=true;
				
				if(bReady){ 
					if(MultiClickI.i().bDebug){
						System.out.println(
								MultiClick.class.getSimpleName()+":clk="+iClickCount+","+"/mb="+iButtonIndex+"/totclks="+iMaxClicks);
					}
				}
				
			}
		}
		
		private boolean isTimeLimitReached() {
			if(lLastClickMilis==null)return false;
			return (System.currentTimeMillis()-lLastClickMilis) > MultiClickI.i().getMaxDelayMilis();
		}
		
		public int getTotalClicks(){
			return iClickCount;
		}

	}
	
	/**
	 * too complex..
	 */
	@Deprecated
	private static class _MultiClick{
		private Long lLastClickMilis=null;
		private int	iClickCount=0;
		private boolean bLock=false;
//		ArrayList<CallableX> acxClickIndex = new ArrayList<CallableX>();
		private CallableX[] acxClickIndex;
		private boolean	bDiscarded;
		private int	iButtonIndex;
		
		private _MultiClick(int iButtonIndex,CallableX... acx){
			this.iButtonIndex=iButtonIndex;
			this.acxClickIndex=acx;
			assert acxClickIndex!=null && acxClickIndex.length>0 : "requires at least two commands to call, single click does not need this...";
			
			QueueI.i().enqueue(new CallableXAnon() {
				@SuppressWarnings("unchecked")
				@Override
				public Boolean call() {
					if(isReady()){
						int i=getTotalClicks()-1;
						int iLimit=acxClickIndex.length-1;
						if(i>=iLimit)i=iLimit;
						CallableX cx = acxClickIndex[i];
						if(cx!=null){ //this allow for click count index gaps for specific multiclicks counts
							QueueI.i().enqueue(cx);
						}
						
						QueueI.i().removeLoopFromQueue(this);
						
//						MultiClick.this.bDiscarded=true;
						
						if(MultiClickI.i().bDebug)System.out.println(MultiClick.class.getSimpleName()+":clk="+i+","+getName());
						
						return true;
					}
					
					return true;
				}
			}).enableLoopMode()
				.setDelaySeconds( TimeConvertI.i().milisToSeconds(MultiClickI.i().getMaxDelayMilis()) / 3f )
				.setName(MultiClick.class.getSimpleName()+"/mb="+iButtonIndex+"/totclks="+acx.length);
		}
		
		public boolean isReady(){
			return bLock;
		}
		
		/**
		 * 
		 * @param tpf
		 * @return if ready to retrieve the multi-click count
		 */
		private boolean updateIncClicks() {
			if(bLock)return true;
			
			long lMilis = System.currentTimeMillis();
			if(lLastClickMilis==null){
				lLastClickMilis=lMilis;
				iClickCount++;
				return false;
			}else{
				if( (lMilis-lLastClickMilis)>MultiClickI.i().getMaxDelayMilis() ){
					bLock=true; 
					return true;
				}
				
				lLastClickMilis=lMilis;
				iClickCount++;
			}
			
			return false;
		}
		
		public int getTotalClicks(){
			return iClickCount;
		}

		public boolean isDiscarded() {
			return bDiscarded;
		}
	}
	
}
