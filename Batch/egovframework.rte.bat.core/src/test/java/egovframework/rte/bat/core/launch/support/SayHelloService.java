package egovframework.rte.bat.core.launch.support;

public class SayHelloService {
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public void sayHello() {
		System.out.println("Hello " + name + "!!");
	}
}
