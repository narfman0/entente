package com.blastedstudios.entente.filter;

public class AABB{
	public float x, y, w, h;
	
	public AABB(float x, float y, float w, float h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public boolean collides(AABB aabb) {
		return Math.abs(x-aabb.x) < w + aabb.w && Math.abs(y-aabb.y) < h + aabb.h;
	}

	public boolean inside(float x1, float y1){
		return Math.abs(x-x1) < w && Math.abs(y-y1) < h;
	}
}