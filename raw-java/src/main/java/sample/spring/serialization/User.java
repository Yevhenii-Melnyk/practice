package sample.spring.serialization;

import org.hibernate.validator.constraints.Length;

public class User {

	private String name;

	public User(String name) {
		this.name = name;
	}

	@Length(min = 5, max = 15)
	public String getName() {
		return name;
	}
}
