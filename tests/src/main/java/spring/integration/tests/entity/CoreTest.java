package spring.integration.tests.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(name = "CORE_TEST")
@Data
@Entity
@EqualsAndHashCode(callSuper = false)
public class CoreTest {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	@Column(name = "ID")
	private Long id;

	@Column(name = "FLAG")
	private Boolean flag;

	@Column(name = "AMT")
	private Integer amt;

	@Column(name = "DESC")
	private String desc;
}
