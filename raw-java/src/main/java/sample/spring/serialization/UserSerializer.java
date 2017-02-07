package sample.spring.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.util.ReflectionUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class UserSerializer extends StdSerializer<User> {

	public UserSerializer() {
		this(User.class);
	}

	private UserSerializer(Class t) {
		super(t);
	}

	@Override
	public void serialize(User bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
		Map<String, Object> properties = beanProperties(bean);
		gen.writeStartObject();
		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			gen.writeObjectField(entry.getKey(), entry.getValue());
		}
		gen.writeEndObject();
	}

	private static Map<String, Object> beanProperties(Object bean) {
		try {
			return Arrays.stream(Introspector.getBeanInfo(bean.getClass(), Object.class).getPropertyDescriptors())
					.filter(descriptor -> Objects.nonNull(descriptor.getReadMethod()))
					.flatMap(descriptor -> {
						String name = descriptor.getName();
						Method getter = descriptor.getReadMethod();
						Object value = ReflectionUtils.invokeMethod(getter, bean);
						Property originalProperty = new Property(name, value);

						Stream<Property> constraintProperties = Stream.of(getter.getAnnotations())
								.map(anno -> new Property(name + "_constraint", annotationProperties(anno)));

						return Stream.concat(Stream.of(originalProperty), constraintProperties);
					})
					.collect(toMap(Property::getName, Property::getValue));
		} catch (Exception e) {
			return Collections.emptyMap();
		}
	}

	// Methods from Annotation.class
	private static List<String> EXCLUDED_ANNO_NAMES = Arrays.asList("toString", "equals", "hashCode", "annotationType");

	private static Map<String, Object> annotationProperties(Annotation anno) {
		try {
			Stream<Property> annoProps = Arrays.stream(Introspector.getBeanInfo(anno.getClass(), Proxy.class).getMethodDescriptors())
					.filter(descriptor -> !EXCLUDED_ANNO_NAMES.contains(descriptor.getName()))
					.map(descriptor -> {
						String name = descriptor.getName();
						Method method = descriptor.getMethod();
						Object value = ReflectionUtils.invokeMethod(method, anno);
						return new Property(name, value);
					});
			Stream<Property> type = Stream.of(new Property("type", anno.annotationType().getName()));
			return Stream.concat(type, annoProps).collect(toMap(Property::getName, Property::getValue));
		} catch (IntrospectionException e) {
			return Collections.emptyMap();
		}
	}

	private static class Property {
		private String name;
		private Object value;

		public Property(String name, Object value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public Object getValue() {
			return value;
		}
	}
}
