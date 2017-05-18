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

import com.jme3.math.Vector3f;



/**
 * @author Henrique Abdalla <https://github.com/AquariusPower><https://sourceforge.net/u/teike/profile/>
 */
public class CalcI {
	public static CalcI i(){return GlobalManagerI.i().get(CalcI.class);}
	
	/**
	 *     <- here ->
	 * v = ((4/3)*pi) * r^3
	 */
	double dSphereVolBaseMul = (4f/3f)*Math.PI;
	
	/**
	 * v = (4/3)*pi * r^3
	 * r^3 = v / ((4/3)*pi)
	 * r = (v / ((4/3)*pi)) ^ (1/3)
	 */
	public double radiusFromVolume(double dVolume) {
		return Math.cbrt( dVolume / dSphereVolBaseMul );
	}
	
	public double sphereVolume(double dRadius){
		return sphereVolume(dRadius,dRadius,dRadius);
	}
	/**
	 * if each is scaled will work precisely too
	 * @param dRadiusX
	 * @param dRadiusY
	 * @param dRadiusZ
	 * @return
	 */
	public double sphereVolume(double dRadiusX, double dRadiusY, double dRadiusZ){
		return (double) ( dSphereVolBaseMul *
				dRadiusX *
				dRadiusY *
				dRadiusZ
			);
	}

	/**
	 * Mesh pre-conditions:
	 * 1) The mesh containing the triangles must be centralized.
	 * 2) The vertices must be ordered in a way that results in a normal that will make the 
	 * triangle point towards the center.
	 * @return
	 */
	public float triangleSVol(
		float fAx, float fAy, float fAz,
		float fBx, float fBy, float fBz,
		float fCx, float fCy, float fCz
	){
		return (
			-(fCx*fBy*fAz)+(fBx*fCy*fAz)+(fCx*fAy*fBz)
		  -(fAx*fCy*fBz)-(fBx*fAy*fCz)+(fAx*fBy*fCz)
		)/6f;
	}
}
