package org.swaggertools.core.model;

import lombok.Data;

@Data
public class Property {
	boolean required;
	String name;
	Schema schema;
}
