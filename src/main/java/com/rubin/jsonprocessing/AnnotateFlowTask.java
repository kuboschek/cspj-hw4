package com.rubin.jsonprocessing;

import java.util.concurrent.Callable;

public class AnnotateFlowTask implements Runnable {
	private Flow f;
	private String name;
	private Callable<?> generator;
	
	public AnnotateFlowTask(Flow f, String annotationName, Callable<?> generator) {
		this.f = f;
		this.name = annotationName;
		this.generator = generator;
	}

	@Override
	public void run() {
		Object value = null;
		try {
			value = generator.call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(value != null)
			f.addAnnotation(name, value);
	}
}
