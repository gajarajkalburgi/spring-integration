package spring.integration.tests.entity;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class Book {

	@XmlAttribute
	private String category;

	@XmlAttribute
	@NotNull
	private boolean reservation;

	@NotNull
	@Max(99999)
	private int id;

	@Size(max = 1024)
	private String author;

	@NotNull
	@Size(max = 1024)
	@Pattern(regexp = "[0-9a-zA-Z\\s_@#$%&]+")
	private String title;

	@Size(max = 4)
	@Pattern(regexp = "[1-9][0-9]{1,3}")
	private String year;

	@Max(100000000)
	private double price;
}
