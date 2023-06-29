package spring.integration.tests.entity;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@XmlRootElement(name = "bookstore")
@XmlAccessorType(XmlAccessType.FIELD)
public class BookStore {

    @XmlElement(name = "book")
    private List<Book> books;
}
