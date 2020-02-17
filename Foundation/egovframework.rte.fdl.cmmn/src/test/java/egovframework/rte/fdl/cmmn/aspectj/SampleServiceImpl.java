package egovframework.rte.fdl.cmmn.aspectj;

import java.util.Date;
import java.util.Random;

public class SampleServiceImpl implements SampleService {

	public String getOrderDescription() {
		Order existingOrder = new Order();
		existingOrder.setDescription("Order for XYZ in units");
		return "Description: " + existingOrder.getDescription();
	}

	public String getOrderStringCode() {
		Order existingOrder = new Order();
		existingOrder.setSecurityCode("XYZ");
		return "Account Code: " + existingOrder.getSecurityCode();
	}
	
	public Order createOrder(Order order) {
		Order newOrder = new Order();
		newOrder.setOrderId(new Random().nextInt());
		newOrder.setSecurityCode("XYZ");
		newOrder.setOrderStatus("INITIATED");
		newOrder.setOrderDate(new Date());
		return newOrder;
	}
	
	public Order getOrder(int id) {
		Order newOrder = new Order();
		newOrder.setOrderId(new Random().nextInt());
		newOrder.setSecurityCode("XYZ");
		newOrder.setOrderStatus("COMPLETED");
		newOrder.setOrderDate(new Date());
		newOrder.setDescription("This is the new XYZ order");
		return newOrder;
	}
	
	

}
