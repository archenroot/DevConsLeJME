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

package com.github.devconslejme;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.github.devconslejme.misc.StringI;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;

/**
 * TODO work with java.util.concurrent.DelayQueue?
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class QueueStateI extends AbstractAppState{
	private static QueueStateI instance = new QueueStateI();
	/**instance*/ public static QueueStateI i(){return instance;}
	
	ArrayList<CallableX> acxList = new ArrayList<CallableX>();
	private Application	app;
	
	public static interface CallableWeak<V> extends Callable<V>{
		/**
		 * without exception thrown, the debug will stop in the exact place where the exception
		 * happens inside a call (finally!!)
		 * 
		 * @return true if succeeded and the queue can be discarded, false if failed and it must be retried.
		 */
		@Override V call();
	}
	
	/**
	 * CallableX like in extra, plus 
	 */
	public abstract static class CallableX implements CallableWeak<Boolean>{
		// THE ORDER of these vars will be at the toString()
		private String strUId;
		private String	strName;
		private float	fDelaySeconds;
		private boolean	bPaused;
		private boolean	bLoop;
		private long lRunAtTimeMilis;
		private boolean	bUserCanKill;
		private boolean	bUserCanPause;
		
		private HashMap<String,Object> hmKeyValue = new HashMap<String,Object>();
		private boolean	bAnonymousClass;
		
		private static String strLastUId="0";
//		private boolean bDone;
		
		public CallableX(float fDelaySeconds, boolean bLoop){
			this(1,null,fDelaySeconds,bLoop);
		}
		public CallableX(String strName, float fDelaySeconds, boolean bLoop){
			this(1,strName,fDelaySeconds,bLoop);
		}
		public CallableX(int iStackAdd, String strName, float fDelaySeconds, boolean bLoop){
			strUId=strLastUId=StringI.i().getNextUniqueId(strLastUId);
			
			// auto name
			if(strName==null){ // for annonimous class
				Method m = this.getClass().getEnclosingMethod();
				if(m!=null)strName=m.getName();
				bAnonymousClass=true;
			}
			
			if(strName==null){
				strName=this.getClass().getSimpleName();
			}
			
			this.strName = strName;
			
			this.bLoop=bLoop;
			
			this.fDelaySeconds=(fDelaySeconds);
			updateRunAt();
		}
		
		public void updateRunAt() {
			this.lRunAtTimeMilis = QueueStateI.i().calcRunAt(fDelaySeconds);
		}
		
		public float getDelaySeconds() {
			return fDelaySeconds;
		}
		
		public boolean isReady(){
			return QueueStateI.i().isReady(lRunAtTimeMilis);
		}

		public boolean isLoop() {
			return bLoop;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CallableX [strUId=");
			builder.append(strUId);
			builder.append(", strName=");
			builder.append(strName);
			builder.append(", fDelaySeconds=");
			builder.append(fDelaySeconds);
			builder.append(", bPaused=");
			builder.append(bPaused);
			builder.append(", bLoop=");
			builder.append(bLoop);
			builder.append(", lRunAtTimeMilis=");
			builder.append(lRunAtTimeMilis);
			builder.append(", bUserCanKill=");
			builder.append(bUserCanKill);
			builder.append(", bUserCanPause=");
			builder.append(bUserCanPause);
			builder.append("]");
			return builder.toString();
		}

		public String getUId() {
			return strUId;
		}

		public void breakLoop() {
			bLoop=false;
		}
		public boolean isUserCanKill() {
			return bUserCanKill;
		}
		public CallableX setUserCanKill(boolean bUserCanKill) {
			this.bUserCanKill = bUserCanKill;
			return this;
		}
		public boolean isUserCanPause() {
			return bUserCanPause;
		}
		public CallableX setUserCanPause(boolean bUserCanPause) {
			this.bUserCanPause = bUserCanPause;
			return this;
		}
		public void togglePause() {
			bPaused=!bPaused;
		}
		public boolean isPaused() {
			return bPaused;
		}
		synchronized public CallableX putKeyValue(String strKey, Object objVal) {
			hmKeyValue.put(strKey,objVal);
			return this;
		}
		synchronized public <T> T getValue(String strKey) {
			return (T)hmKeyValue.get(strKey);
		}
		public String getName() {
			return strName;
		}
//		public void setName(String strName) {
//			this.strName = strName;
//		}
		
	}
	
	/**
	 * call things that must happen on the proper update moment
	 * @param cx
	 */
	public void enqueue(CallableX cx){
		synchronized(acxList){
			if(!acxList.contains(cx)){
				acxList.add(cx);
			}
		}
	}
	
	private boolean isReady(long lRunAt){
		return lRunAt <= app.getTimer().getTime();
	}
	
	private long calcRunAt(float fDelaySeconds){
		return app.getTimer().getTime() + (long)(fDelaySeconds*app.getTimer().getResolution());
	}
	
	@Override
	public void update(float tpf) {
		super.update(tpf);
		
		for(CallableX cx:acxList.toArray(new CallableX[]{})){
			
			if(cx.isReady()){
				if(cx.isPaused())continue;
				
				if(cx.call()){
//					cx.done();
					if(cx.isLoop()){
						cx.updateRunAt();
					}else{
						synchronized(acxList){
							acxList.remove(cx);
						}
					}
				}else{
					// if a loop queue fails, it will not wait and will promptly retry!
				}
			}
		}
		
//		if(iDoneCount>10){ //TODO what is a good amount to let it be cleaned considering speed/cpu usage?
//			for(CallableX cx:acxList.toArray(new CallableX[]{})){
//				if(cx.isDone())acxList.remove(cx);
//			}
//		}
	}
	
	public void configure(Application app) {
		this.app=app;
		
		if(app.getTimer().getResolution() < 1000L){
			throw new NullPointerException("timer resolution is too low");
		}
		
		app.getStateManager().attach(this);
	}
	
	public void showQueue(){
		for(CallableX cx:acxList){
			LoggingI.i().logSubEntry(cx.toString());
		}
	}

	private void killOrPauseToggle(String strUId,boolean bKill) {
		for(CallableX cx:acxList){
			if(strUId.equalsIgnoreCase( strUId.endsWith(".js") ? cx.getName() : cx.getUId() )){
				if(bKill){
					if(cx.isUserCanKill())cx.breakLoop();
				}else{
					if(cx.isUserCanPause())cx.togglePause();
				}
			}
		}
	}

	public void kill(String strUId) {
		killOrPauseToggle(strUId, true);
	}
	
	public void pauseToggle(String strUId) {
		killOrPauseToggle(strUId, false);
	}
	
}