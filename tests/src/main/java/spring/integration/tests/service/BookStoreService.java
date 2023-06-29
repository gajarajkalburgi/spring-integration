package spring.integration.tests.service;

import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Service;
import spring.integration.tests.entity.Book;
import spring.integration.tests.entity.BookStore;

@Service
public class BookStoreService {
	/**
	 * initiate bookstore required by controller.
	 *
	 * @return BookStore
	 */
	public BookStore getBookStore() {
		Book cookingBook = Book.builder()
				.author("Giada De Laurentiis")
				.category("COOKING")
				.id(12578)
				.price(30.0)
				.reservation(true)
				.year("2005")
				.title("Everyday Italian").build();

		Book childrenBook = Book.builder()
				.author("J K. Rowling")
				.category("CHILDREN")
				.id(86465)
				.price(29.99)
				.reservation(false)
				.year("2005")
				.title("Harry Potter").build();

		Book webBook = Book.builder()
				.author("Erik T. Ray")
				.category("WEB")
				.id(99009)
				.price(39.95)
				.reservation(true)
				.year("2003")
				.title("Learning XML").build();

		List<Book> books = new LinkedList<>();
		books.add(cookingBook);
		books.add(childrenBook);
		books.add(webBook);

		BookStore bookStore = new BookStore();
		bookStore.setBooks(books);

		return bookStore;
	}
}
