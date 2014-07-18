/*
 * Copyright (c) 2014, Sergey Parshin, quarck@gmail.com
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of developer (Sergey Parshin) nor the
 *       names of other project contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.quarck.qrckwatch;

import java.util.HashMap;

public class CommonAppsRegistry
{
	public static final int Unknown 	= 0x01;
	public static final int Calendar 	= 0x02;
	public static final int GoogleHangouts	= 0x04;
	public static final int GooglePlus	= 0x08;
	public static final int Messages	= 0x10;
	public static final int Email	= 0x20;
	public static final int GMail	= 0x40;
	public static final int Phone	= 0x80;
	public static final int Skype	= 0x100;
	public static final int VOiP	= 0x200;
	public static final int IM	= 0x400;
	public static final int Facebook	= 0x800;
	public static final int LinkedIn	= 0x1000;
	public static final int VK	= 0x2000;
	public static final int Instgram = 0x4000;
	public static final int Viber = 0x8000;
	
	private static HashMap<String, Integer> packages = new HashMap<String, Integer>();
	
	static {
		packages.put("com.google.android.calendar", Integer.valueOf(Calendar));
		packages.put("com.android.calendar", Integer.valueOf(Calendar));
		
		packages.put("com.google.android.talk", Integer.valueOf(GoogleHangouts));
		
		packages.put("com.google.android.apps.plus", Integer.valueOf(GooglePlus));
		
		packages.put("com.android.phone", Integer.valueOf(Phone));

		packages.put("com.android.mms", Integer.valueOf(Messages));
		
		packages.put("com.google.android.email", Integer.valueOf(Email));
		packages.put("com.android.email", Integer.valueOf(Email));
		packages.put("com.fsck.k9", Integer.valueOf(Email));
		packages.put("org.kman.AquaMail", Integer.valueOf(Email));
		packages.put("net.daum.android.solmail", Integer.valueOf(Email));
		
		packages.put("com.google.android.gm", Integer.valueOf(GMail));
		
		
		packages.put("com.facebook.katana", Integer.valueOf(Facebook));
		packages.put("com.facebook.orca", Integer.valueOf(Facebook));
		
		packages.put("com.viber.voip", Integer.valueOf(Viber));
		
		packages.put("com.whatsapp", Integer.valueOf(IM));
		
		packages.put("com.skype.raider", Integer.valueOf(Skype));
		
		packages.put("com.vkontakte.android", Integer.valueOf(VK));
		
		packages.put("com.csipsimple", Integer.valueOf(VOiP));
		packages.put("unibilling.sipfone", Integer.valueOf(VOiP));
		packages.put("org.sipdroid.sipua", Integer.valueOf(VOiP));
		
		packages.put("com.yahoo.mobile.client.android.im", Integer.valueOf(IM));
		packages.put("com.instagram.android", Integer.valueOf(Instgram));
		packages.put("com.bbm", Integer.valueOf(IM));
		packages.put("com.linkedin.android", Integer.valueOf(LinkedIn));
		
		packages.put("de.shapeservices.impluslite", Integer.valueOf(IM));
		packages.put("de.shapeservices.implusfull", Integer.valueOf(IM));
		packages.put("com.sec.chaton", Integer.valueOf(IM));
	};
	
	public static int getMaskBitForPackage(String packageName)
	{
		if (packages.containsKey(packageName))
			return packages.get(packageName);
		return Unknown;
	}
}
