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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.github.devconslejme.gendiag.ContextMenuI.ContextMenu;
import com.github.devconslejme.gendiag.ContextMenuI.HintUpdater;
import com.github.devconslejme.misc.Annotations.Bugfix;
import com.github.devconslejme.misc.Annotations.ToDo;
import com.github.devconslejme.misc.Annotations.Workaround;
import com.github.devconslejme.misc.DetailedException;
import com.github.devconslejme.misc.GlobalManagerI;
import com.github.devconslejme.misc.JavaLangI;
import com.github.devconslejme.misc.JavaLangI.LinkedHashMapX;
import com.github.devconslejme.misc.MessagesI;
import com.github.devconslejme.misc.MethodHelp;
import com.github.devconslejme.misc.QueueI;
import com.github.devconslejme.misc.QueueI.CallableXAnon;
import com.github.devconslejme.misc.jme.ColorI;
import com.github.devconslejme.misc.jme.MiscJmeI;
import com.github.devconslejme.misc.lemur.DragParentestPanelListenerI;
import com.github.devconslejme.misc.lemur.MiscLemurI;
import com.github.devconslejme.misc.lemur.PopupHintHelpListenerI;
import com.github.devconslejme.misc.lemur.ResizablePanel;
import com.google.common.base.Function;
import com.jme3.app.Application;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.BorderLayout.Position;
import com.simsilica.lemur.component.TextEntryComponent;
import com.simsilica.lemur.core.VersionedList;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DefaultCursorListener;
import com.simsilica.lemur.event.KeyAction;
import com.simsilica.lemur.event.KeyActionListener;
import com.simsilica.lemur.focus.FocusManagerState;
import com.simsilica.lemur.list.DefaultCellRenderer;
import com.simsilica.lemur.style.ElementId;


/**
 * A text based generic dialog.
 * TODO move whatever fits at super class to there
 * 
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class SimpleGenericDialog extends AbstractGenericDialog {
	private Label	btnInfoText;
	private ListBox<OptionData>	lstbxOptions;
	private VersionedList<OptionData>	vlodOptions;
	/** a list of options can never be empty or the dialog will make no sense at all */
	private LinkedHashMapX<String,OptionData> hmOptionsRoot;
	private VersionedList<ToolAction>	vlodTools;
	private TextField	tfInput;
	private boolean	bReturnJustTheInputTextValue;
	private boolean	bRequestUserSubmitedInputValueApply;
	private KeyActionListener	kal;
	private boolean	bRequestUpdateListItems;
	private SectionIndicator sectionIndicator;
	private Function<IVisibleText, String>	funcVisibleText;
	private boolean bRequestSelectedToggleExpandedOnce;
	private Command<? super Button>	cmdOption;
	private DefaultCellRenderer<IVisibleText>	crVisibleText;
	private VersionedReference<Set<Integer>>	vrSelection;
	private boolean	bRequestUpdateOptionSelected;
	private boolean	bCloseOnChoiceMade;
	private Container	cntrInfo;
	private String	strName;
	private String	strTitle;
	private Button	btnTitleText;
	private Container	cntrTitle;
	private Button	btnMinimize;
	private Button	btnMaximizeRestore;
	private Button	btnClose;
	private ArrayList<Button>	abtnInfoSection;
	private Command<? super Button>	cmdInfoSectionTitleButtons;
	private Container	cntrDiagControls;
	private int	iDiagControlColumnInitIndex;
	private boolean	bKeepMaximized;
//@FloatLimits(min=-1f,max=1f) float f;
	private ListBox<ToolAction>	lstbxTools;
	/**
	 * for some reason, some of the buttons on the listbox will not work with the
	 * Button.addClickCommands(). To force it to work, I am using together the 
	 * CursorListener.
	 */
	@Workaround @Bugfix
	private DefaultCursorListener	curlisExtraClickCmd;
	private ContextMenu	cmIST;
	private ContextMenu	cmSubBorderSize;
	private int	iNestingStepDistance=10;
	
	private static class SectionIndicator{}
	
	public static class ToolAction implements IVisibleText{
		private String strTextKey;
		Command<Button> cmdAction;
		public ToolAction(String strTextKey, Command<Button> cmdAction) {
			super();
			this.strTextKey = strTextKey;
			this.cmdAction = cmdAction;
		}
		
		@Override
		public String getVisibleText() {
			return strTextKey;
		}
	}
	
	public static class OptionDataDummy extends OptionData{
		public OptionDataDummy(){
			String str="(TempDummy)";
			setTextKey(str+OptionDataDummy.class.getName());
			setStoredValue(str);
		}
	}
	
	public static class OptionData implements IVisibleText{
		private String strTextKey;
		private OptionData odParent;
		private Object objStoredValue;
		private boolean bExpanded;
		private LinkedHashMapX<String,OptionData> hmNestedChildrenSubOptions;
		private ArrayList<CmdCfg> acmdcfgList = new ArrayList<CmdCfg>();
		private boolean	bHasBean;
		
		public OptionData(){
			bExpanded=true;
			hmNestedChildrenSubOptions = new LinkedHashMapX<String,OptionData>();
		}
		
		protected OptionData setTextKey(String strTextKey) {
			this.strTextKey = strTextKey;
			return this; 
		}
		protected OptionData setStoredValue(Object objValue) {
			this.objStoredValue = objValue;
			return this; 
		}
		private OptionData setSectionParent(OptionData odParent) {
			assert(odParent==null || SectionIndicator.class.isInstance(odParent.getStoredValue()));
			this.odParent = odParent;
			return this; 
		}
		public String getTextKey() {
			return strTextKey;
		}
		public OptionData getSectionParent() {
			assert(odParent==null || SectionIndicator.class.isInstance(odParent.getStoredValue()));
			return odParent;
		}
		public Object getStoredValue() {
			return objStoredValue;
		}
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("OptionData [strTextKey=");
			builder.append(strTextKey);
			builder.append(", odParent=");
			builder.append(odParent==null?null:odParent.getTextKey()); //!!!!!!!!!!!!!!! CUSTOM !!!!!!!!!!!!!!
			builder.append(", objValue=");
			builder.append(objStoredValue);
			builder.append(", bExpanded=");
			builder.append(bExpanded);
			builder.append(", hmOptions=");
			builder.append(hmNestedChildrenSubOptions);
			builder.append("]");
			return builder.toString();
		}
		public boolean isExpanded() {
			return bExpanded;
		}
		private OptionData setExpanded(boolean bExpanded) {
			this.bExpanded = bExpanded;
			return this; 
		}
		public boolean toggleExpanded(){
			bExpanded=!bExpanded;
			return bExpanded;
		}
		/**
		 * 
		 * @param aodStore will be created if null
		 * @return
		 */
		public ArrayList<OptionData> getAllChildrenRecursively(ArrayList<OptionData> aodStore){
			if(aodStore==null)aodStore = new ArrayList<OptionData>();
			for(OptionData odChild:hmNestedChildrenSubOptions.values()){
				aodStore.add(odChild);
				odChild.getAllChildrenRecursively(aodStore);
//				if(odChild.hmNestedChildrenSubOptions.size()>0){
//					getAllChildrenRecursively(aodStore);
//				}
			}
			return aodStore;
		}
		@Override
		public String getVisibleText() {
			int iDepth=getNestingDepth();
			
			String str=strTextKey;
			
			if(getStoredValue() instanceof SectionIndicator){
//				str="["+(isExpanded()?"-":"+")+"] "
				str=""
					+str
					+(
//						!isExpanded()
//						? 
								" {p"+iDepth+",c"+hmNestedChildrenSubOptions.size()+",ac"+getAllChildrenRecursively(null).size()+"}"
//						: ""
					)
				;
			}
			
//			str=" "+str;
//			str=Strings.padStart(str, str.length()+iDepth, '>');
			
			return str;
		}

		public CmdCfg[] getCmdCfgList() {
			return acmdcfgList.toArray(new CmdCfg[0]);
		}

		public void addCmdCfg(CmdCfg cc) {
//			DetailedException.assertNotAlreadySet(this.cmdCfg, cmdCfg, this);
			this.acmdcfgList.add(cc);
		}

		public int getNestingDepth() {
			int iDepth=0;
			OptionData odParent=this;
			while((odParent=odParent.getSectionParent())!=null)iDepth++;
			return iDepth;
		}

		/**
		 * if this one is a getter and has a setter
		 */
		public void setHasBean() {
			bHasBean=true;
		}
		public boolean isHasBean() {
			return bHasBean;
		}
		
	}
	
	public static abstract class CmdCfg implements Command<Button>{
//		private boolean	bIsTheApplyUserCustomValueCommand;
//		public CmdCfg(boolean bIsTheApplyUserCustomValueCommand){
//			this.bIsTheApplyUserCustomValueCommand = bIsTheApplyUserCustomValueCommand;
//			
//		}
		
		private String	strText;
		private String	strHintHelp;

		public String getText() {
			return strText;
		}

		public CmdCfg setText(String strText) {
			this.strText = strText;
			return this;
		}

		public CmdCfg setHintHelp(String string) {
			this.strHintHelp=string;
			return this;
		}

		public String getHintHelp() {
			return strHintHelp;
		}

	}
	
	public SimpleGenericDialog(ResizablePanel rzpOwner) {
		super(rzpOwner);
//		configureDefaults();
	}

	public SimpleGenericDialog() {
		this(DialogHierarchyStateI.i().createDialog(SimpleGenericDialog.class.getSimpleName(), null));
		
		hmOptionsRoot = new LinkedHashMapX<String,OptionData>();
		bRequestUpdateListItems = true;
		sectionIndicator = new SectionIndicator();
		bCloseOnChoiceMade=true;
		
		// text info row
		abtnInfoSection = new ArrayList<Button>();
		/**
		 * IMPORTANT: Button works MUCH better than Label when clicking to drag for ex.
		 * as the label will require aiming at the label's text...
		 */
		btnInfoText = createInfoButton("(No Info)",null);
	}
	
	/**
	 * TODO this may be called twice as a workaround, how to avoid it?
	 * @param btn
	 */
	private void buttonClicked(Button btn){
		ButtonCell btnc = (ButtonCell)btn;
//		OptionData od = UserDataI.i().getUserDataPSH(btn, OptionData.class);
		OptionData od = btnc.od;
		if(od!=null){
			bRequestUpdateOptionSelected=true;
			lstbxOptions.getSelectionModel().setSelection(vlodOptions.indexOf(od));
			return;
		}
		
//		ToolAction ta = UserDataI.i().getUserDataPSH(btn, ToolAction.class);
		ToolAction ta = btnc.ta;
		if(ta!=null){
			return;
		}
	}
	
	/**
	 * this method is quite simple, can be called at the constructor
	 * @param strText
	 * @param strHintPopup
	 * @return
	 */
	private Button createInfoButton(String strText,String strHintPopup){
		Button btn = new Button(strText,getDialog().getStyle());
		abtnInfoSection.add(btn);
		if(strHintPopup!=null)PopupHintHelpListenerI.i().setPopupHintHelp(btn,strHintPopup);
		return btn;
	}
	
	@SuppressWarnings("unchecked")
	private void initSectionInfo(){
		ESection es=ESection.Info;
		if(getSection(es)==null){
			initSectionInfoTitle();
			
			// cfg all buttons
			for(Button btn:abtnInfoSection){
				btn.addClickCommands(cmdInfoSectionTitleButtons);
			}
			
			// info section
			cntrInfo = new Container(new BorderLayout());
			cntrInfo.addChild(cntrTitle, BorderLayout.Position.North);
			cntrInfo.addChild(btnInfoText, BorderLayout.Position.Center);
			
			setSection(es,cntrInfo);
		}
	}
	
	private void initSectionInfoTitle() {

		cmdInfoSectionTitleButtons = new Command<Button>() {
			private String	strUDKeyPosBeforeMaximize = SimpleGenericDialog.class+"/PosBeforeMaximize";
			@Override
			public void execute(Button source) {
				if(source==btnMaximizeRestore){ //toggle
					if(bKeepMaximized){							/**							 * restore							 */
						getDialog().restoreDefaultSafeSize();
						
						Vector3f v3fPosBeforeMaximize = (Vector3f)getDialog().getUserData(strUDKeyPosBeforeMaximize);
						getDialog().setLocalTranslationXY(v3fPosBeforeMaximize);
						
						bKeepMaximized=false;
					}else{							/**							 * maximize							 */
						getDialog().applyCurrentSafeSizeAsDefault();
						
						getDialog().setUserData(strUDKeyPosBeforeMaximize,getDialog().getLocalTranslation().clone());
						
						bKeepMaximized=true;
					}
				}else
				if(source==btnClose){
					getDialog().close();
				}else
				{
					MessagesI.i().warnMsg(SimpleGenericDialog.this, "cmd not supported yet", source);
				}
			}
		};
		
		strTitle="(no title)";
		
		// title row
		cntrDiagControls = new Container();
		iDiagControlColumnInitIndex=0;
		btnMinimize=appendNewDiagControl("-","Minimize");
		btnMaximizeRestore=appendNewDiagControl("M","Maximize/Restore");
		btnClose=appendNewDiagControl("X","Close");
		MiscLemurI.i().changeBackgroundColor(btnClose, ColorI.i().colorChangeCopy(ColorRGBA.Red,0f,0.25f), true); //TODO use a lemur style instead
		
		// title row put it all
		cntrTitle = new Container(new BorderLayout());
		
		initInfoSectionTitleContextMenu();
		
		btnTitleText = createInfoButton(strTitle,null);
		MiscLemurI.i().changeBackgroundColor(btnTitleText, ColorI.i().colorChangeCopy(ColorRGBA.Blue,0f,0.25f), true); //TODO use a lemur style instead
		DragParentestPanelListenerI.i().applyAt(btnTitleText);
		ContextMenuI.i().applyContextMenuAt(btnTitleText, cmIST);
		
//		cntrTitle.setPreferredSize(new Vector3f(1,1,0.1f));
		cntrTitle.addChild(btnTitleText, BorderLayout.Position.Center);
		cntrTitle.addChild(cntrDiagControls, BorderLayout.Position.East);
		
	}

	@SuppressWarnings("unchecked")
	private void initInfoSectionTitleContextMenu() {
		cmIST = new ContextMenu(getDialog());
		cmIST.addNewEntry("Restore to default/initial size", new Command<Button>() {@Override public void execute(Button source) {
			getDialog().restoreDefaultSafeSize(); }}, null);
		cmIST.addNewEntry("Update default size to current", new Command<Button>() {@Override public void execute(Button source) {
			getDialog().applyCurrentSafeSizeAsDefault(); }}, null);
		cmIST.addNewEntry("Toggle Info Visibility", 
			new Command<Button>() {@Override public void execute(Button source) {
				if(btnInfoText.getParent()!=null){
					cntrInfo.removeChild(btnInfoText);
				}else{
					cntrInfo.addChild(btnInfoText, BorderLayout.Position.Center);
				}
			}},
			new HintUpdater() {
				@Override
				public Boolean call() {
					setPopupHintHelp(btnInfoText.getParent()==null?"show":"hide"); //inverted to show next action on click
					return true;
				}
			}
		);
		
		cmSubBorderSize = cmIST.createSubMenu("global resizable border size");
		cmSubBorderSize.setSingleChoiceMode(true);
		Command<Button> cmdBorderSize = new Command<Button>() {
			@Override
			public void execute(Button source) {
				int i = Integer.parseInt(source.getText());
				ResizablePanel.setResizableBorderSizeDefault(i);
				getDialog().setResizableBorderSize(i,i);
			}
		};
		
		for(int i=1;i<=10;i++){
			cmSubBorderSize.addNewEntry(""+i, cmdBorderSize, new HintUpdater() {
				@Override
				public Boolean call() {
//					Button btn = (Button)cmSubBorderSize.getContextSource(); //TODO why?!?!? at other places I dont have to cast to Button!?!??!?!?!?!?!
//					int i = Integer.parseInt(btn.getText());
//					int i = Integer.parseInt(getContextButtonOwner().getText());
					int i = (int)getContextButtonOwner().getValue(); //TODO why?!?!? at other places I dont have to cast to Button!?!??!?!?!?!?!
					if(ResizablePanel.getResizableBorderSizeDefault()==i){
//						setPopupHintHelp("current choice");
						return true;
					}
					return false;
				}
			}).setValue(i);
		}
	}
	
	private static class CellParts{
		Button btnNesting;
		ButtonCell btnItemText;
		Panel pnlCfg;
	}
	
	private static class ButtonCell extends Button{
		public ToolAction	ta;
		public OptionData	od;
		public ButtonCell(String s, ElementId elementId, String style) {
			super(s, elementId, style);
		}
	}
	
	private class ContainerCell extends Container{
		CellParts cp;
		public ContainerCell() {
			super(new BorderLayout(), getDialog().getStyle());
		}
//		public ContainerCell(GuiLayout layout, String style) {
//			super(layout, style);
//		}
	}
	
	private void initBase(){
		curlisExtraClickCmd = new DefaultCursorListener(){
			@Override
			protected void click(CursorButtonEvent event, Spatial target, Spatial capture) {
				buttonClicked((Button)capture);
			};
		};
		
		crVisibleText = new DefaultCellRenderer<IVisibleText>(getDialog().getStyle()){
			@SuppressWarnings("unchecked")
			private Panel getView(OptionData od, boolean selected, Panel existing) {
				ContainerCell cntr=null;
        if( existing == null ) {
//        	cntr=new ContainerCell(new BorderLayout(), getDialog().getStyle());
					cntr=new ContainerCell();
					cntr.cp = new CellParts();
        	cntr.cp.btnItemText = new ButtonCell("", getElement(), getStyle());
  				CursorEventControl.addListenersToSpatial(cntr.cp.btnItemText, curlisExtraClickCmd);
        	
        	cntr.cp.btnNesting = new Button("", getDialog().getStyle());
					cntr.addChild(cntr.cp.btnNesting, Position.West);
					
        	cntr.cp.btnItemText.addClickCommands(cmdOption);
					cntr.addChild(cntr.cp.btnItemText, Position.Center);
	      } else {
	      	cntr = (ContainerCell)existing;
	      }
        
        // ALWAYS update the value!
				cntr.cp.btnItemText.od=od;
				
				// update the nesting visuals
				String strNesting = "["+(cntr.cp.btnItemText.od.isExpanded()?"-":"+")+"]";
				if(cntr.cp.btnItemText.od.hmNestedChildrenSubOptions.size()==0)strNesting=" ";
				cntr.cp.btnNesting.setText(strNesting);
				cntr.cp.btnNesting.setInsets(new Insets3f(0, 
					getNestingStepDistance()*cntr.cp.btnItemText.od.getNestingDepth(), 
					0, 0));
				
				// update the text based on the value
				cntr.cp.btnItemText.setText(valueToString(od));
				
				// CONFIGURATOR: each item may have a different kind of configurator from button text to a different visual (like a slider etc)
				if(isEnableItemConfigurator()){ 
					cntr.cp.pnlCfg = createConfigurator(cntr.cp.btnItemText.od, cntr.cp.pnlCfg); 
					cntr.addChild(cntr.cp.pnlCfg, Position.East);
				}
				
				return cntr;
			}
			
			@SuppressWarnings({ "unchecked"})
			private Panel getView(ToolAction ta, boolean selected, Panel existing) {
				ButtonCell btnItemText = null;
				
        if( existing == null ) {
        	btnItemText = new ButtonCell(valueToString(ta), getElement(), getStyle());
	      } else {
	      	btnItemText = (ButtonCell)existing;
	      	btnItemText.setText(valueToString(ta));
	      }
        
        btnItemText.ta = ta;
				btnItemText.addClickCommands(btnItemText.ta.cmdAction);
				
				return btnItemText;
			}
			
			@Override
			public Panel getView(IVisibleText value, boolean selected, Panel existing) {
				Panel pnlRet = null;
				
				if(value instanceof OptionData){
					pnlRet = getView((OptionData)value, selected, existing);
				}else
				if(value instanceof ToolAction){
					pnlRet = getView((ToolAction)value, selected, existing);
				}
				
				return pnlRet;
			}
		};
		
		funcVisibleText = new Function<IVisibleText, String>() {
			@Override
			public String apply(IVisibleText vt) {
				return vt.getVisibleText();
			}
		};
		
		crVisibleText.setTransform(funcVisibleText);
	}
	
	protected boolean isEnableItemConfigurator() {
		return false;
	}

	@SuppressWarnings("unchecked")
	protected Panel createConfigurator(OptionData od, Panel pnlCfgExisting) {
		Container cntr = new Container(getDialog().getStyle());
		
		int i=0;
		Panel pnl = createAutomaticConfigurators(od);
		if(pnl!=null){
			cntr.addChild(pnl,i++);
		}
		
		CmdCfg[] acc = od.getCmdCfgList();
		for(CmdCfg cc:acc){
//		Command<? super Button> cmd = od.getCmdCfgList();
//		if(cmd!=null){
			Button btnCfg = new Button(cc.getText(), getDialog().getStyle());
			btnCfg.addClickCommands(cc);
			if(cc.getHintHelp()!=null)PopupHintHelpListenerI.i().setPopupHintHelp(btnCfg, cc.getHintHelp());
			cntr.addChild(btnCfg, i++);
//			return btnCfg;
//		}
		}
		
//		if(acc.length>0)return cntr;
		
		if(i==0){ //nothing was added to container
			Button btn = new Button("...", getDialog().getStyle());
			PopupHintHelpListenerI.i().setPopupHintHelp(btn, "configuration not available");
			return btn;
		}
		
		return cntr;
	}
	
	private class ContainerEdit extends Container{
		public ContainerEdit() {
//			super(new BorderLayout(), getDialog().getStyle());
			super(getDialog().getStyle());
		}
		public TextField getTf() {
			return tf;
		}
		public void setTf(TextField tf) {
			this.tf = grantMinWidth(tf);
		}
		public Button getBtn() {
			return btn;
		}
		public void setBtn(Button btn) {
			this.btn = grantMinWidth(btn);
		}
		@SuppressWarnings("unchecked")
		private <T> T grantMinWidth(Panel pnl){
			float fMinWidth=50;
			pnl.updateLogicalState(0); //this will pre-calculate the required good size 
			Vector3f v3fSize = pnl.getSize().clone();
			if(v3fSize.x<fMinWidth)v3fSize.x=fMinWidth;
			pnl.setPreferredSize(v3fSize);
			return (T)pnl;
		}
		private Button btn;
		private TextField tf;
	}
	
	@SuppressWarnings("unchecked")
	private Panel createConfiguratorMethodHelp(OptionData od, MethodHelp mh){
		Method mGetter = mh.getMethod();
//		String mName = mGetter.getName();
		ContainerEdit ce = new ContainerEdit();
		if( JavaLangI.i().isBeanGetter(mGetter) ) {
			Object objVal;
			String strButtonHintHelp=null;
			try {
				Method mSetter = JavaLangI.i().getBeanSetterFor(mGetter);//mh.getConcreteObjectInstance(), m.getName());
				
				od.setHasBean(); //first thing, so if it fails below the problem will be clearly visible
				
				objVal = mGetter.invoke(mh.getConcreteObjectInstance()); //collect value from getter method
				ce.setBtn(new Button(""+objVal, getDialog().getStyle())); //show value
				ce.addChild(ce.getBtn(), 0);
				
				strButtonHintHelp="click to change value";
				ce.setTf(new TextField(ce.getBtn().getText(), ce.getBtn().getStyle()));
				
				ce.getBtn().addClickCommands(new Command<Button>(){
					@Override
					public void execute(Button source) {
//							if(source==ce.btn){ //<- redundant check..
							ce.addChild(ce.getTf(), 0); //will replace the button
							ce.getTf().setText(ce.getBtn().getText());
							GlobalManagerI.i().get(Application.class).getStateManager().getState(FocusManagerState.class)
								.setFocus(ce.getTf());
//							}
					}});
				
				ce.getTf().getActionMap().put(
					new KeyAction(KeyInput.KEY_RETURN),
					new KeyActionListener() {
						@Override public void keyAction(TextEntryComponent source, KeyAction key) {
							boolean b=JavaLangI.i().setBeanValueAt(
								mh.getConcreteObjectInstance(), 
								mSetter, 
								mGetter.getReturnType(), 
								ce.getTf().getText());
							
							if(!b){
								MessagesI.i().warnMsg(this, "failed to change value", mSetter, mGetter, ce.getTf().getText(), mh);
							}
//									ce.btn.setText(ce.tf.getText()); //TODO should actually be a possibly validated by the setter/getter
								try {
									// retrieves value possibly validated by the setter/getter
									ce.getBtn().setText(""+mGetter.invoke(mh.getConcreteObjectInstance()));
								} catch (IllegalAccessException | IllegalArgumentException| InvocationTargetException e) {
									throw new DetailedException(e, "value get should not have failed this second time", mGetter, mSetter, mh, od);
								}
								
								ce.addChild(ce.getBtn(), 0); //will replace the textfield
//								}
						}
					}
				);
				
				ce.getBtn().setColor(ColorI.i().colorChangeCopy(ColorRGBA.Green, 0.35f));
//				}else{
//					strButtonHintHelp="WARN: no command will be called to bean set the new value";
////					ce.btn.setHighlightColor(ColorRGBA.Red);
//					ce.btn.setColor(ColorI.i().colorChangeCopy(ColorRGBA.Red, 0.35f));
				
//					od.setHasBean(); //last thing, so if it fails for any reason will not set this.
//				}
				
			} catch (IllegalAccessException | IllegalArgumentException| InvocationTargetException e) {
				strButtonHintHelp="ERROR: failed to invoke the method '"+mGetter+"' to get the value";
				MessagesI.i().warnMsg(this,strButtonHintHelp,od.getCmdCfgList(), od, mGetter, e);
				ce.setBtn(new Button("(FAIL)", getDialog().getStyle()));
			}
			
			if(ce.getBtn()!=null && strButtonHintHelp!=null){
				PopupHintHelpListenerI.i().setPopupHintHelp(ce.getBtn(), strButtonHintHelp);
			}
		}
		
		return ce;
	}
	
	/**
	 * mainly for primitives, allowing sliders too
	 * TODO but.. how to set it back after changed? a holder? lemur one?
		TODO using limits annotations, allow sliders and TextField input on the very same listbox
		TODO using reflection, look for matching getters and setters (and is...) to create a new child dialog to enable such optoins
	 * @param od
	 * @return
	 */
	private Panel createAutomaticConfigurators(OptionData od) {
		if (od.getStoredValue() instanceof MethodHelp) {
			return createConfiguratorMethodHelp(od, (MethodHelp)od.getStoredValue());
		}
		
//		if(!JavaLangI.i().isCanUserTypeIt(od.getStoredValue()))return null;
//		
//		String str=od.getStoredValue().toString();
//		return new Button(str.substring(0, Math.min(10, str.length())), getDialog().getStyle());
		return null;
	}

	@Override
	protected void initContentsContainer() {
		initBase();
		
		initSectionInfo();
		initSectionOptions();
		initSectionInput();
		initSectionTools();
		
		super.initContentsContainer();
	}
	
	protected void initSectionTools() {
		ESection es=ESection.Tools;
		if(getSection(es)==null){
			vlodTools = new VersionedList<ToolAction>();
			
			lstbxTools = new ListBox<ToolAction>(vlodTools, getDialog().getStyle());
			MiscLemurI.i().createListBoxVisibleItemsUpdater(lstbxTools);
			
			lstbxTools.setCellRenderer(crVisibleText);
			
			setSection(es,lstbxTools);
		}
	}
	
	public void putToolAction(ToolAction ta){
		if(!vlodTools.contains(ta)){
			vlodTools.add(ta);
		}
	}
	
	private void initSectionInput() {
		ESection es=ESection.Input;
		if(getSection(es)==null){
			kal = new KeyActionListener() {
				@Override
				public void keyAction(TextEntryComponent source, KeyAction key) {
					switch(key.getKeyCode()){
						case KeyInput.KEY_RETURN:
						case KeyInput.KEY_NUMPADENTER:
							bRequestUserSubmitedInputValueApply=true;
							break;
					}
				}
			};
			
			tfInput = new TextField("", getDialog().getStyle());
			
			tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_NUMPADENTER),kal); 
			tfInput.getActionMap().put(new KeyAction(KeyInput.KEY_RETURN),kal);
			//tfInput.getActionMap().entrySet()
			
			setSection(es,tfInput);
		}
	}
	
	public static interface IVisibleText{
		String getVisibleText();
	}
	
	private void initSectionOptions() {
		ESection es=ESection.Options;
		if(getSection(es)==null){
			vlodOptions = new VersionedList<OptionData>();
			lstbxOptions = new ListBox<OptionData>(vlodOptions, getDialog().getStyle());
			MiscLemurI.i().createListBoxVisibleItemsUpdater(lstbxOptions);
			
			cmdOption = new Command<Button>() {
				@Override
				public void execute(Button source) {
					buttonClicked(source);
				}
			};
			
			lstbxOptions.setCellRenderer(crVisibleText);
			
			vrSelection = lstbxOptions.getSelectionModel().createReference();
			
			setSection(es,lstbxOptions);
//			lstbxOptions.setVisibleItems(10); //TODO make automatic
		}
	}

	private Button appendNewDiagControl(String strText, String strHint) {
		Button btn = createInfoButton(strText,strHint);
		MiscJmeI.i().addToName(btn, strText, true);
		cntrDiagControls.addChild(btn, iDiagControlColumnInitIndex++);
		return btn;
	}

	public void setTextInfo(String strInfo){
		btnInfoText.setText(strInfo);
	}
	
	public OptionData putSection(OptionData odParent, String strNewSectionKey){
		OptionData od = new OptionData();
//		od.setSectionParent(odParent);
//		od.setTextKey(strNewSectionKey);
		od.setStoredValue(sectionIndicator);
		
		put(odParent,strNewSectionKey,od);
//		hmOptionsRoot.put(strNewSectionKey, od);
		
		return od;
	}
	
	/**
	 * 
	 * @param strSectionParentKey if null, will be root/topmost on the hierarchy
	 * @param strTextOptionKey also is the displayed unique text per section
	 * @param objStoredValue
	 * @return 
	 */
	public OptionData putOption(OptionData odParent, String strTextOptionKey, Object objStoredValue){
		OptionData od = new OptionData();
//		od.setSectionParent(odParent);
//		od.setTextKey(strTextOptionKey);
		od.setStoredValue(objStoredValue);
		
		put(odParent,strTextOptionKey,od);
		
		return od;
	}
	
	public int remove(OptionData odToRemove) {
		return doSomethingRecursively(odToRemove, new Function<OptionData, Boolean>() {
			@Override
			public Boolean apply(OptionData odToCompare) {
				if(odToRemove.equals(odToCompare)){
					LinkedHashMapX<String, OptionData> hmOpt = hmOptionsRoot;
					if(odToCompare.getSectionParent()!=null){
						hmOpt=odToCompare.getSectionParent().hmNestedChildrenSubOptions;
					}
					
//					hmOpt.remove(odToRemove.getTextKey()); //TODO enable
					hmOpt.removeX(odToRemove.getTextKey());
//					hmOptionsRoot.containsKey(odToRemove);
					vlodOptions.remove(odToRemove);
					return true;
				}
				return false;
			}
		});
	}
	
	/**
	 * 
	 * @param odParent
	 * @param funcDoSomething
	 * @return how many matched and the apply did anything useful
	 */
	private int doSomethingRecursively(OptionData odParent, Function<OptionData,Boolean> funcDoSomething) {
		int iCount=0;
		if(funcDoSomething.apply(odParent))iCount++;
		if(odParent.getStoredValue() instanceof SectionIndicator){
			for(OptionData odChild:odParent.hmNestedChildrenSubOptions.values()){
				iCount+=doSomethingRecursively(odChild,funcDoSomething);
			}
		}
		return iCount;
	}
	
	private int setExpandedAllRecursively(OptionData odParent, boolean bExpand) {
		int iCount=0;
//		if(odParent==null){
//			for(OptionData odChild:hmOptionsRoot.values()){
//				iCount+=setExpandedAllRecursively(odChild, b);
//			}
//		}else{
			iCount+=doSomethingRecursively(odParent, new Function<OptionData, Boolean>() {
				@Override
				public Boolean apply(OptionData odToModify) {
					if(!SectionIndicator.class.isInstance(odToModify.getStoredValue()))return false;
					
					if(odToModify.isExpanded()!=bExpand){
						odToModify.setExpanded(bExpand);
						return true;
					}
					
					return false;
				}
			});
//		}
		
		return iCount;
	}
//	private void setExpandedAllRecursively(OptionData od, boolean b) {
//		od.setExpanded(b);
//		if(od.getStoredValue() instanceof SectionIndicator){
//			for(OptionData odChild:od.hmNestedChildrenSubOptions.values()){
//				setExpandedAllRecursively(odChild,b);
//			}
//		}
//	}
	
	private void put(OptionData odParent, String strTextKey, OptionData od){
		od.setSectionParent(odParent);
		od.setTextKey(strTextKey);
		
		HashMap<String,OptionData> hmOpt = hmOptionsRoot;
		if(odParent!=null){
			assert(SectionIndicator.class.isInstance(odParent.getStoredValue()));
//			if(!SectionIndicator.class.isInstance(odParent.getStoredValue())){
//				throw new DetailedException("parent section not properly configured", odParent, strTextKey, od, this);
//			}
//			OptionData odParent = findSectionRecursively(hmOpt, strSectionParentKey);
//			DetailedException.assertNotNull(odParent, "the parent section must be set before being referenced/requested/used!", odParent);
			hmOpt=odParent.hmNestedChildrenSubOptions;
		}
		
		OptionData odPrevious = hmOpt.put(strTextKey, od);
		if(odPrevious!=null)MessagesI.i().warnMsg(this, "option was already set", odPrevious.toString(), od.toString());
	}
	
	public OptionData findSectionRecursively(OptionData odParent, String strSectionKey){
		assert(SectionIndicator.class.isInstance(odParent.getStoredValue()));
//		if(!SectionIndicator.class.isInstance(odParent.getStoredValue()))return null;
		
		LinkedHashMap<String,OptionData> hmOpt = odParent==null?hmOptionsRoot:odParent.hmNestedChildrenSubOptions;
		if(hmOpt==null)return null;
		
		return findSectionRecursively(hmOpt, strSectionKey);
	}
	private OptionData findSectionRecursively(HashMap<String,OptionData> hmOpt, String strSectionKey){
		OptionData odFound= hmOpt.get(strSectionKey);
		if(odFound!=null)return odFound;
		
		//look for sub-sections
		for(OptionData od:hmOpt.values()){
			if(od.getStoredValue() instanceof SectionIndicator){
				odFound = findSectionRecursively(od.hmNestedChildrenSubOptions,strSectionKey);
				if(odFound!=null)return odFound;
			}
		}
		
		return null;
	}
	
	private void recreateListItems(){
		vlodOptions.clear();
		recreateListItemsRecursively(hmOptionsRoot,0);
	}
	
	/**
	 * TODO useless right?
	 */
	private Comparator<OptionData>	sortByChildAmount = new Comparator<SimpleGenericDialog.OptionData>() {
		@Override
		public int compare(OptionData o1, OptionData o2) {
			return Integer.compare(o1.hmNestedChildrenSubOptions.size(), o2.hmNestedChildrenSubOptions.size());
		}
	};
	
	private Comparator<OptionData>	sortAtoZ = new Comparator<SimpleGenericDialog.OptionData>() {
		@Override
		public int compare(OptionData o1, OptionData o2) {
			return o1.getTextKey().compareToIgnoreCase(o2.getTextKey());
		}
	};
	
	private boolean	bSortAlphabetically=true;
	
	private void recreateListItemsRecursively(HashMap<String, OptionData> hmOpt, int iDepth){
//		ArrayList<OptionData> aod = new ArrayList<OptionData>(hmOpt.values());
		List<OptionData> aod = Arrays.asList(hmOpt.values().toArray(new OptionData[0]));
		
		if(isSortAlphabetically()){
			ArrayList<OptionData> aodChildLess = new ArrayList<OptionData>();
			ArrayList<OptionData> aodHasChildren = new ArrayList<OptionData>();
			for(OptionData od:aod){
				if(od.getStoredValue() instanceof SectionIndicator){
					aodHasChildren.add(od);
				}else{
					aodChildLess.add(od);
				}
			}
			Collections.sort(aodChildLess, sortAtoZ);
			Collections.sort(aodHasChildren, sortAtoZ);
			ArrayList<OptionData> aodAll = new ArrayList<OptionData>();
			aodAll.addAll(aodChildLess); //childless above
			aodAll.addAll(aodHasChildren);
			aod = aodAll;//.toArray(new OptionData[0]);
		}else{
			Collections.sort(aod, sortByChildAmount); //this will just put child-less above and keep insert order
		}
		
		for(OptionData od:aod){
			vlodOptions.add(od);
			if(od.getStoredValue() instanceof SectionIndicator){
				if(od.isExpanded()){
					recreateListItemsRecursively(od.hmNestedChildrenSubOptions,++iDepth);
				}
			}
		}
	}
	
	public Integer getSelectedOptionIndex(){
		Integer i = lstbxOptions.getSelectionModel().getSelection();
		if(i==null)return null;
		
		if(i>=vlodOptions.size()){
			i = vlodOptions.size()-1;
			lstbxOptions.getSelectionModel().setSelection(i);
			return i;
		}
		
		return lstbxOptions.getSelectionModel().getSelection();
	}
	
	public OptionData getSelectedOptionData(){
		Integer i = getSelectedOptionIndex();
		if(i==null){
			MessagesI.i().warnMsg(this, "nothing selected");
			return null;
		}
		return vlodOptions.get(i);
	}
	
	public String getSelectedOptionVisibleText(){
		return vlodOptions.get(getSelectedOptionIndex()).getVisibleText();
	}
	
	public Object getSelectedOptionValue(){
		int i=getSelectedOptionIndex();
		Object obj = vlodOptions.get(i).getStoredValue();
		if(obj instanceof SectionIndicator)return null;
		return obj;
	}
	
	public void requestUpdateListItems(){
		bRequestUpdateListItems=true;
	}
	
	public void update(float tpf) {
		if(bKeepMaximized){
			MiscLemurI.i().maximize(getDialog());
		}
		
		if(bRequestSelectedToggleExpandedOnce){
			OptionData od = getSelectedOptionData();
			if(od!=null)od.toggleExpanded();
			bRequestUpdateListItems = true;
			bRequestSelectedToggleExpandedOnce=false;
		}
		
		if(bRequestUpdateListItems){
			recreateListItems();
			bRequestUpdateListItems=false;
		}
		
		/**
		 * the selection seems to only change after a mouse cursor click
		 */
		if(bRequestUpdateOptionSelected){
			updateOptionSelected();
			bRequestUpdateOptionSelected=false;
		}
		
		if(bReturnJustTheInputTextValue){
			if(bRequestUserSubmitedInputValueApply){
				setChosenValue(getInputText());
				bRequestUserSubmitedInputValueApply=false;
			}
		}else{ // set as soon an option is selected
			Integer i=getSelectedOptionIndex();
			if(i!=null){
				setChosenValue(getSelectedOptionValue());
			}
		}
		
		if(isOptionSelected()){
			if(isCloseOnChoiceMade()){
				getDialog().close();
			}
		}
	}
	
	private void updateOptionSelected() {
		OptionData od = getSelectedOptionData();
		if(od==null)return;
		
		if(SectionIndicator.class.isInstance(od.getStoredValue())){
			bRequestSelectedToggleExpandedOnce=true;
		}else{
			tfInput.setText(od.getTextKey());
		}
	}

	@Override
	public Object extractSelectedOption() {
		lstbxOptions.getSelectionModel().setSelection(-1);
		return super.extractSelectedOption();
	}
	
	public String getInputText(){
		return tfInput.getText();
	}
	
	/**
	 * options text will be used to fill the input text and be returned as the value instead of the custom objects
	 * @param b
	 */
	public void setReturnJustTheInputTextValue(boolean b){
		this.bReturnJustTheInputTextValue=b;
	}
	
	public boolean isUseInputTextValue(){
		return bReturnJustTheInputTextValue;
	}
	
	@Override
	public void resizerUpdatedLogicalStateEvent(float tpf,ResizablePanel rzp) {
		update(tpf);
	}

	public int setExpandedAll(boolean bExpand){
		int iCount=0;
		for(OptionData od:hmOptionsRoot.values()){
			iCount+=setExpandedAllRecursively(od,bExpand);
		}
		return iCount;
	}
	
	protected void clearOptions(){
		hmOptionsRoot.clear();
		vlodOptions.clear();
		
		OptionDataDummy odd = new OptionDataDummy();
		put(null, odd.getTextKey(), odd); //the list cant remain empty
		
		QueueI.i().enqueue(new CallableXAnon() {
			@Override
			public Boolean call() {
				if(hmOptionsRoot.containsKey(odd.getTextKey()) || vlodOptions.contains(odd)){
					hmOptionsRoot.remove(odd.getTextKey());
					vlodOptions.remove(odd);
					return false; //look for more
				}
//				if(hmOptionsRoot.remove(odd.getTextKey())==null)return false; //retry
				return true;
			}
		}.setName("ClearDummyOption"));
	}

	public boolean isCloseOnChoiceMade() {
		return bCloseOnChoiceMade;
	}

	public void setCloseOnChoiceMade(boolean bCloseOnChoiceMade) {
		this.bCloseOnChoiceMade = bCloseOnChoiceMade;
	}

	public int getNestingStepDistance() {
		return iNestingStepDistance;
	}

	public void setNestingStepDistance(int iNestingStepDistance) {
		if(iNestingStepDistance<1)iNestingStepDistance=1;
		this.iNestingStepDistance = iNestingStepDistance;
	}

	public boolean isSortAlphabetically() {
		return bSortAlphabetically;
	}

	public void setSortAlphabetically(boolean bSortByChildAmount) {
		this.bSortAlphabetically = bSortByChildAmount;
	}
}
