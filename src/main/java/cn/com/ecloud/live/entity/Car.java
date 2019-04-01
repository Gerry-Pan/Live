package cn.com.ecloud.live.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Car")
public class Car implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String name;

	public String getId() {
		return id;
	}

	public Car setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public Car setName(String name) {
		this.name = name;
		return this;
	}

}
