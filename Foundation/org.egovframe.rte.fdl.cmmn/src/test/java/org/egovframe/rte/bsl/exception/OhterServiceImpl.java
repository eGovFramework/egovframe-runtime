package org.egovframe.rte.bsl.exception;

import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;

public class OhterServiceImpl extends EgovAbstractServiceImpl implements HelloService {

	public String sayHello(String name) throws Exception {
		String helloStr = "Hello " + name;
		try {
			int i = 1 / 0;
			System.out.println(i);
		} catch (Exception e) {
			leaveaTrace("message.exception.test");
		}

		return helloStr;
	}

	public void deleteMethod() throws Exception {

	}

	public String insertMethod() throws Exception {
		return null;
	}

	public void updateMethod() throws Exception {
		if (true)
			throw processException("info.nodata.msg");
	}

}
