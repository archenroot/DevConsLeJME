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

package com.github.devconslejme.misc;

import java.util.ArrayList;

/**
 * Differently from toString(), this is intended to provide to  
 * end-user (or developer) high quality readable output.
 * 
 * TODO review/rework/join the methods, see JavaLangI.convertToKeyValueArray() too.
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class ReportI {
	public static ReportI i(){return GlobalInstanceManagerI.i().get(ReportI.class);}
	
	public static interface IReport{
		public String getReport(boolean bFull);
	}

	private boolean	bShowType=false;
	
	public ArrayList<String> prepareReportLines(String strMsg, Object... aobjCustom){
		ArrayList<String> astrReport=new ArrayList<String>();
		astrReport.add(strMsg);
		
		for(Object obj:aobjCustom){
			recursivelyAddLinesIfHasAnyArray(astrReport," ",obj);
		}
		
		return astrReport;
	}
	
	public String prepareReport(String strMsg, Object... aobjCustom){
		return String.join("\n", prepareReportLines(strMsg, aobjCustom));
	}
	
	private String formatObject(Object obj,boolean bShowType){
		if(obj==null)return ""+null;
			
		String strType="";
		if(bShowType)strType="<"+obj.getClass().getSimpleName()+">";
		
		String strValue=obj.toString();
		if(obj.getClass()==String.class)strValue="'"+strValue+"'";
		
		return strType+strValue;
	}
	
	private String prepareKey(Object objKey){
		//key can be an index or anyhthing else that must be finally shown in a simple way...
		return StringI.i().truncAndGrantOneLine(objKey.toString(), 20, "...");
	}
	
	private void recursivelyAddLinesIfHasAnyArray(ArrayList<String> astrReport, String strPrepend, Object... aobj) {
		if(aobj==null)return;
		
		for(int i=0;i<aobj.length;i++){
			Object obj = aobj[i];
			strPrepend+="["+i+"]"; //hierarchy sub-item
			
			// obj
			if(!JavaLangI.i().isSomeArrayType(obj)){
				astrReport.add(strPrepend+formatObject(obj,bShowType));
			}else{ //array
				astrReport.add(strPrepend+"Array of: "+obj.getClass().getTypeName());
				Object[][] akvobj = JavaLangI.i().convertToKeyValueArray(obj);
				for(int j=0;j<akvobj.length;j++){
					String strKey = prepareKey(akvobj[j][0]);
					Object objValue = akvobj[j][1];
					
					String strPrependWithKey=strPrepend+"["+strKey+"] ";
					if(!JavaLangI.i().isSomeArrayType(objValue)){
						astrReport.add(strPrependWithKey+formatObject(objValue,bShowType));
					}else{
						astrReport.add(strPrependWithKey+"Array of: "+obj.getClass().getTypeName());
						recursivelyAddLinesIfHasAnyArray(astrReport, strPrepend, bShowType, objValue);
					}
				}
			}
		}
		
	}

	public boolean isShowType() {
		return bShowType;
	}

	public void setShowType(boolean bShowType) {
		this.bShowType = bShowType;
	}
	
}