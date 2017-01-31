package spring.reactive;


import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class ItemRepository {
	public Flux<Item> findAllItems(final int repetitions) {
		final List<Item> itemList = new ArrayList<>();
		itemList.add(new Item(1, "Mercury"));
		itemList.add(new Item(2, "Venus"));
		itemList.add(new Item(3, "Earth"));
		itemList.add(new Item(4, "Mars"));
		itemList.add(new Item(5, "Ceres"));
		itemList.add(new Item(6, "Jupiter"));
		itemList.add(new Item(7, "Saturn"));
		itemList.add(new Item(8, "Uranus"));
		itemList.add(new Item(9, "Neptune"));
		itemList.add(new Item(10, "Pluto"));

		return Flux.fromIterable(itemList).repeat(repetitions);

	}
}

class Item {

	private final int id;
	private final String name;

	public Item(final int id, final String name) {
		super();
		this.id = id;
		this.name = name;
	}


	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

}

class ComplexItem {

	private Flux<Item> items;

	public ComplexItem(Flux<Item> items) {
		this.items = items;
	}

	public Flux<Item> getItems() {
		return items;
	}
}

@Component
class BookMethodArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Book.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
		return Mono.just(new Book(exchange.getRequest().getQueryParams().getFirst("name")));
	}

}


class Book {
	private String name;
	private Integer id;

	public Book() {
	}

	public Book(String name) {
		this.name = name;
	}

	public Book(Integer id, String name) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Integer getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Book{" +
				"name='" + name + '\'' +
				", id=" + id +
				'}';
	}
}
