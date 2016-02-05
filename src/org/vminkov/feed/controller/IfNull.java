package org.vminkov.feed.controller;

public class  IfNull<T> {
	private T value;

	public static void throwRE(Object value, RuntimeException re){
		if(value == null){
			throw re;
		}
	}
	
	public static <T> IfNull<T> withDefault(T value, T defaultValue){
		return new IfNull<T>((value != null) ? value : defaultValue);
	}
	
	private IfNull(T value){
		this.value = value;
	}
	
	private IfNull(T value, T defaultValue){
		this.value = (value != null) ? value : defaultValue;
	}
	
	public T get(){
		return this.value;
	}
}
