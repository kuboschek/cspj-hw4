package com.rubin.jsonprocessing;

import java.util.concurrent.Callable;

public class AnnotateFlowTask implements Runnable {
	private Flow f;
	private String name;
	private Callable<?> generator = null;
    private Object value = null;
	
	public AnnotateFlowTask(Flow f, String annotationName, Callable<?> generator) {
		this.f = f;
		this.name = annotationName;
		this.generator = generator;
	}

	public AnnotateFlowTask(Flow f, String annotationName, Object value) {
        this.f = f;
        this.name = annotationName;
        this.value = value;
    }

	@Override
	public void run() {
        if(value != null) {
            f.addAnnotation(name, value);
            return;
        }

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
