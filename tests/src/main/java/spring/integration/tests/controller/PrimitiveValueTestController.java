package spring.integration.tests.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrimitiveValueTestController {

	@GetMapping(value = "/primitive/boolean")
	public boolean getBoolean() {
		return true;
	}

	@GetMapping(value = "/primitive/int")
	public int getInteger() {
		return 10;
	}

	@GetMapping(value = "/primitive/long")
	public long getLong() {
		return 10L;
	}

	@GetMapping(value = "/primitive/byte")
	public byte getByte() {
		return 10;
	}

	@GetMapping(value = "/primitive/char")
	public char getChar() {
		return 'A';
	}

	@GetMapping(value = "/primitive/short")
	public short getShort() {
		return 10;
	}

	@GetMapping(value = "/primitive/float")
	public float getFloat() {
		return (float) 1.1;
	}

	@GetMapping(value = "/primitive/double")
	public double getDouble() {
		return 1.1;
	}

}
