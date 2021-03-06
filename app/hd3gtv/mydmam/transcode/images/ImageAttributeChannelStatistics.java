/*
 * This file is part of MyDMAM.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * Copyright (C) hdsdi3g for hd3g.tv 2015
 * 
*/
package hd3gtv.mydmam.transcode.images;

public class ImageAttributeChannelStatistics {
	ImageAttributeStatistics alpha;
	ImageAttributeStatistics red;
	ImageAttributeStatistics green;
	ImageAttributeStatistics blue;
	ImageAttributeStatistics cyan;
	ImageAttributeStatistics magenta;
	ImageAttributeStatistics yellow;
	ImageAttributeStatistics black;
	
	public ImageAttributeStatistics getAlpha() {
		return alpha;
	}
	
	public ImageAttributeStatistics getBlue() {
		return blue;
	}
	
	public ImageAttributeStatistics getGreen() {
		return green;
	}
	
	public ImageAttributeStatistics getRed() {
		return red;
	}
	
	public ImageAttributeStatistics getBlack() {
		return black;
	}
	
	public ImageAttributeStatistics getCyan() {
		return cyan;
	}
	
	public ImageAttributeStatistics getMagenta() {
		return magenta;
	}
	
	public ImageAttributeStatistics getYellow() {
		return yellow;
	}
}
