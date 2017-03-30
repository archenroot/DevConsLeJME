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

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.dnd.DragStatus;
import com.simsilica.lemur.dnd.Draggable;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.CursorListener;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.StyleAttribute;
import com.simsilica.lemur.style.StyleDefaults;
import com.simsilica.lemur.style.Styles;

/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class ResizablePanel extends Panel implements Draggable {
	private BorderLayout layout;
	private Panel contents;
	private int iBorderSize = 3;
	private QuadBackgroundComponent	qbcBorder = new QuadBackgroundComponent();
	private Vector3f	v3fDragFromPrevious;
	private Vector3f	v3fMinSize = new Vector3f(20,20,0);
	private float fCornerHotSpotRange = 10;
	private boolean	bDragEvenIfOutside = false; //TODO still buggy
	
//	private ResizerDragAndDropListener	dndlCursorListener = new ResizerDragAndDropListener();
//	private class ResizerDragAndDropListener implements DragAndDropListener{
//		@Override
//		public Draggable onDragDetected(DragEvent event) {
//			if(event.getTarget() instanceof Draggable){
//				return (Draggable)event.getTarget();
//			}
//			return null;
//		}
//
//		@Override
//		public void onDragEnter(DragEvent event) {
//			if(event.getTarget()==ResizablePanel.this){
//				v3fDragFromPrevious = event.getCollision().getContactPoint();
//			}
//		}
//
//		@Override
//		public void onDragExit(DragEvent event) {
//			if(event.getTarget()==ResizablePanel.this){
//				event.getClass(); //TODO rm tmp debug breakpoint
//			}
//		}
//		
//		@Override
//		public void onDragOver(DragEvent event) {
//			if(event.getTarget()==ResizablePanel.this){
//				resizeThruDragging(event.getX(), event.getY());
//			}
//		}
//
//		@Override
//		public void onDrop(DragEvent event) {
//			if(event.getTarget()==ResizablePanel.this){
//				v3fDragFromPrevious=null;
//			}
//		}
//
//		@Override
//		public void onDragDone(DragEvent event) {
//			if(event.getTarget()==ResizablePanel.this){
//				event.getClass();//TODO rem
//			}
//		}
//	}
	
//	enum EEdge{
//		Left,
//		Right,
//		
//		Top, //THIS ORDER MATTERS!
//		TopRight, //THIS ORDER MATTERS!
//		TopLeft, //THIS ORDER MATTERS!
//		
//		Bottom, //THIS ORDER MATTERS!
//		BottomRight, //THIS ORDER MATTERS!
//		BottomLeft, //THIS ORDER MATTERS!
//		;
//	}
	
	/**
	 * !!!!!!!!!!!!!!THIS ORDER IS IMPORTANT!!!!!!!!!!!!!!
	 */
	enum EEdge{ 
		Dummy0,
		Top,         //1
		Right,       //2
		TopRight,    //3
		Left,        //4
		TopLeft,     //5
		Bottom,      //6
		Dummy7,
		BottomRight, //8
		Dummy9,
		BottomLeft,  //10
		;
	} // I said THIS ORDER!!! not disorder... :)
	EEdge eeInitialHook = null;
	private void resizeThruDragging(float fCursorX, float fCursorY){
		Vector3f v3fOldSize = new Vector3f(getPreferredSize());
		Vector3f v3fPanelCenter=v3fOldSize.divide(2f);
		
		Vector3f v3fPanelCenterOnAppScreen=getWorldTranslation().add(
			v3fPanelCenter.x,-v3fPanelCenter.y,0);
		
		Vector3f v3fNewSize = v3fOldSize.clone();
		
		//Cursor Position: NEW          Previous
		float fDeltaX = fCursorX - v3fDragFromPrevious.x; // positive to the right
		float fDeltaY = fCursorY - v3fDragFromPrevious.y; // positive downwards
		
//		if(fDeltaX!=0 || fDeltaY!=0){
//			String.class.getName(); //TODO rm tmp debug breakpoint
//		}
		
		EEdge ee=null;
		if(eeInitialHook==null){
			float fDistPanelCenterToCursorX = Math.abs(fCursorX - v3fPanelCenterOnAppScreen.x);
			float fDistPanelCenterToCursorY = Math.abs(fCursorY - v3fPanelCenterOnAppScreen.y); 
			float fMaxDistX = v3fPanelCenter.x;
			float fMaxDistY = v3fPanelCenter.y;
			float fDistToBorderX = fMaxDistX - fDistPanelCenterToCursorX;
			float fDistToBorderY = fMaxDistY - fDistPanelCenterToCursorY;
				
			EEdge eeX=null;
			if(fDistToBorderX < fCornerHotSpotRange){
				eeX = fCursorX>v3fPanelCenterOnAppScreen.x ? EEdge.Right : EEdge.Left;
			}
			
			EEdge eeY=null;
			if(fDistToBorderY < fCornerHotSpotRange){
				eeY = fCursorY>v3fPanelCenterOnAppScreen.y ? EEdge.Top : EEdge.Bottom;
			}
			
			if(eeX==null && eeY==null)throw new NullPointerException("impossible condition: cursor at no edge?");
			
			if(eeX==null){
				ee=eeY;
			}else
			if(eeY==null){
				ee=eeX;
			}else{
				ee = EEdge.values()[eeX.ordinal()+eeY.ordinal()];
			}
			
//			if(fDistToBorderX < fCornerHotSpotRange){
//				if(fCursorX>v3fPanelCenterOnAppScreen.x){
//					ee=EEdge.values()[ee.ordinal()+1]; //Right
//				}else{
//					ee=EEdge.values()[ee.ordinal()+2]; //Left
//				}
//			}
			
			eeInitialHook=ee;
		}else{
			ee=eeInitialHook;
		}
		
		// resize and move
		Vector3f v3fNewPos = getLocalTranslation().clone();
		switch(ee){
			case Left:
				fDeltaY=0;
				break;
			case Right:
				fDeltaY=0;
				break;
				
			case Top:
				fDeltaX=0;
				v3fNewSize.y+=fDeltaY;
				v3fNewPos.y+=fDeltaY;
				break;
			case TopRight:
				v3fNewSize.x+=fDeltaX;
				v3fNewSize.y+=fDeltaY;
				v3fNewPos.y+=fDeltaY;
				break;
			case TopLeft:
				v3fNewSize.x-=fDeltaX;
				v3fNewSize.y+=fDeltaY;
				v3fNewPos.x+=fDeltaX;
				v3fNewPos.y+=fDeltaY;
				break;
				
			case Bottom:
				fDeltaX=0;
				v3fNewSize.y-=fDeltaY;
				break;
			case BottomRight:
				v3fNewSize.x+=fDeltaX;
				v3fNewSize.y-=fDeltaY;
				break;
			case BottomLeft:
				v3fNewSize.x-=fDeltaX;
				v3fNewSize.y-=fDeltaY;
				v3fNewPos.x+=fDeltaX;
				break;
		}
		
		v3fDragFromPrevious.x+=fDeltaX;
		v3fDragFromPrevious.y+=fDeltaY;
		
		// constraint
		if(v3fNewSize.x<getMinSize().x)v3fNewSize.x=getMinSize().x;
		if(v3fNewSize.y<getMinSize().y)v3fNewSize.y=getMinSize().y;
		
		setPreferredSize(v3fNewSize);
		setLocalTranslation(v3fNewPos);
	}
	
	public static final String LAYER_RESIZABLE_BORDERS = "resizableBorders";
	
  public ResizablePanel( float width, float height, String style ) {
    super(false, new ElementId("resizablePanel"), style);
    
    this.layout = new BorderLayout();
    getControl(GuiControl.class).setLayout(layout);
    
    getControl(GuiControl.class).setPreferredSize(new Vector3f(width, height, 0));
    
    // Set our layers
    getControl(GuiControl.class).setLayerOrder(LAYER_INSETS, 
                                               LAYER_BORDER, 
                                               LAYER_BACKGROUND,
                                               LAYER_RESIZABLE_BORDERS);
    
    setBorder(qbcBorder);
    setBorderSize(iBorderSize);
    
    Styles styles = GuiGlobals.getInstance().getStyles();
    styles.applyStyles(this, getElementId(), style);
    
//    addControl(new DragAndDropControl(dndlCursorListener));
    CursorEventControl.addListenersToSpatial(this, dcl);
  }
	
  ResizerCursorListener dcl = new ResizerCursorListener();
  private class ResizerCursorListener implements CursorListener{
		@Override
		public void cursorButtonEvent(CursorButtonEvent event, Spatial target,				Spatial capture) {
			if(target!=ResizablePanel.this)return;
			if(event.getButtonIndex()!=0)return;
			
			if(event.isPressed()){
				v3fDragFromPrevious=new Vector3f(event.getX(),event.getY(),0);
				if(isDragEvenIfOutside()){
					event.setConsumed(); //to let dragging happens even if it is outside the Panel!
				}
			}else{
				v3fDragFromPrevious=null;
				eeInitialHook=null;
			}
		}
		
  	@Override
  	public void cursorMoved(CursorMotionEvent event, Spatial target, Spatial capture) {
  		if(v3fDragFromPrevious!=null){
  			resizeThruDragging(event.getX(),event.getY());
  		}
  	}

		@Override
		public void cursorEntered(CursorMotionEvent event, Spatial target,				Spatial capture) {
		}

		@Override
		public void cursorExited(CursorMotionEvent event, Spatial target,				Spatial capture) {
		}
  }
  
  @StyleDefaults("resizablePanel")
  public static void initializeDefaultStyles( Attributes attrs ) {
      attrs.set( "resizableBorders", new QuadBackgroundComponent(ColorRGBA.Gray), false );
  }
  
	@StyleAttribute(value="resizableBorders", lookupDefault=false)
	public void setResizableBorders( GuiComponent bg ) {        
	    getControl(GuiControl.class).setComponent(LAYER_RESIZABLE_BORDERS, bg);   
	}
	
  public GuiComponent getResizableBorders() {
    return getControl(GuiControl.class).getComponent(LAYER_RESIZABLE_BORDERS);
  }
  
	/**
	 *  Resets the child contents that will be expanded/collapsed
	 *  with the rollup.
	 */
	public void setContents( Panel p ) {
	    if( this.contents == p ) {
	        return;
	    }
	    
	    if( this.contents != null) {
	        layout.removeChild(contents);
	    }
	    
	    this.contents = p;
	    if( this.contents != null ) {
	      if( contents.getParent() == null ) {
	        layout.addChild(contents,  BorderLayout.Position.Center);
	      }
	    }
	}
	
	@Override
	public void setLocation(float x, float y) {
	}

	@Override
	public Vector2f getLocation() {
		return null;
	}

	@Override
	public void updateDragStatus(DragStatus status) {
	}

	@Override
	public void release() {
	}

	public float getCornerHotSpotRange() {
		return fCornerHotSpotRange;
	}

	public void setCornerHotSpotRange(float fCornerHotSpotRange) {
		this.fCornerHotSpotRange = fCornerHotSpotRange;
	}

	public int getBorderSize() {
		return iBorderSize;
	}

	public void setBorderSize(int i){
		this.iBorderSize=(i);
    qbcBorder.setMargin(this.iBorderSize, this.iBorderSize);
	}

	public void setMinSize(Vector3f v3f){
		this.v3fMinSize=(v3f);
	}
	
	public Vector3f getMinSize() {
		return v3fMinSize;
	}

	public boolean isDragEvenIfOutside() {
		return bDragEvenIfOutside;
	}

	public void setDragEvenIfOutside(boolean bDragEvenIfOutside) {
		this.bDragEvenIfOutside = bDragEvenIfOutside;
	}

}