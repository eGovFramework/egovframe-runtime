package org.egovframe.rte.fdl.cmmn.aspectj;

public interface SampleService {

	String getOrderDescription();
	String getOrderStringCode();
	Order getOrder(int id);
	Order createOrder(Order order);
	
	
}
