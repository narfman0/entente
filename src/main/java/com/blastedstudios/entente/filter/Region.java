package com.blastedstudios.entente.filter;

import com.blastedstudios.entente.filter.Extent.ExtentAABB;

public interface Region<T extends Extent> {
	boolean applies(T extent);
	
	public class RegionAABB implements Region<ExtentAABB> {
		public AABB aabb;
		public Float x, y;
		
		public boolean applies(ExtentAABB extent){
			if(aabb != null)
				return aabb.collides(extent.aabb);
			if(x != null && y != null)
				return extent.aabb.inside(x, y);
			// no region is really defined, so no filter?
			return true;

		}
	}
}
