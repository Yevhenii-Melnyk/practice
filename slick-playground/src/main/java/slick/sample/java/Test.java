package slick.sample.java;

import scala.Function1;
import scala.Tuple1;
import scala.collection.JavaConverters;
import scala.collection.mutable.MutableList;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import slick.basic.BasicActionComponent;
import slick.basic.BasicProfile;
import slick.dbio.DBIOAction;
import slick.dbio.DBIOAction$;
import slick.dbio.Effect;
import slick.dbio.NoStream;
import slick.jdbc.*;
import slick.jdbc.UnboundRelationalTableComponent.UnboundTable;
import slick.lifted.*;
import slick.relational.RelationalActionComponent;
import slick.relational.RelationalProfile;
import slick.sql.FixedSqlAction;
import slick.sql.FixedSqlStreamingAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static scala.compat.java8.FutureConverters.toJava;


public class Test {

	private static final H2Profile$ h2Profile = H2Profile$.MODULE$;
	private static final JdbcProfile.API api = h2Profile.api();
	private static final DBIOAction$ DBIO = DBIOAction$.MODULE$;

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		TableQuery<Users> tableQuery = new TableQuery<>(Users::new);
		JdbcBackend.DatabaseDef db = UnboundH2Profile.db("h2mem1");

		RelationalProfile.TableQueryExtensionMethods extQuery = getTableQueryExtensionMethods(tableQuery);
		BasicProfile.SchemaDescriptionDef schema = (BasicProfile.SchemaDescriptionDef) extQuery.schema();
		RelationalActionComponent.SchemaActionExtensionMethodsImpl methods
				= (RelationalActionComponent.SchemaActionExtensionMethodsImpl) api.schemaActionExtensionMethods(schema);
		FixedSqlAction tableCreateAction = (FixedSqlAction) methods.create();

		RelationalActionComponent.InsertActionExtensionMethodsImpl inserter = queryInsertActionExtensionMethods(tableQuery);
		FixedSqlAction<Object, NoStream, Effect.Write> insertAction = UnboundH2Profile.add(tableQuery, Tuple1.apply("COOOOL"));

		BasicActionComponent.StreamingQueryActionExtensionMethodsImpl queryMethods = streamableQueryActionExtensionMethods(tableQuery);
		FixedSqlStreamingAction result = (FixedSqlStreamingAction) queryMethods.result();

		DBIOAction dbioAction = DBIO.seq(JavaConverters.asScalaBuffer(Arrays.asList(
				tableCreateAction,
				insertAction,
				result.map(new Function1() {
					@Override
					public Function1 compose(Function1 g) {
						return this;
					}

					@Override
					public Object apply(Object v1) {
						System.out.println(v1);
						return v1;
					}

					@Override
					public Function1 andThen(Function1 g) {
						return this;
					}
				}, ExecutionContext.Implicits$.MODULE$.global())
		)));

		Future run = db.run(dbioAction);
		toJava(run).toCompletableFuture().thenRun(() -> {
			System.out.println("awesome");
		}).get();
	}

	private static BasicActionComponent.StreamingQueryActionExtensionMethodsImpl streamableQueryActionExtensionMethods(Query tableQuery) {
		return api.streamableQueryActionExtensionMethods(tableQuery);
	}

	private static RelationalActionComponent.InsertActionExtensionMethodsImpl queryInsertActionExtensionMethods(Query tableQuery) {
		return api.queryInsertActionExtensionMethods(tableQuery);
	}

	private static RelationalProfile.TableQueryExtensionMethods getTableQueryExtensionMethods(TableQuery<Users> tableQuery) {
		return new UnboundRelationalTableComponent.UnboundTableQueryExtensionMethods(tableQuery);
	}

}


class Users extends UnboundTable<Tuple1<String>> {
	private static final H2Profile$ h2Profile = H2Profile$.MODULE$;

	public Users(Tag tag) {
		super(tag, "USERS");
	}

	Rep<String> name() {
		return column("NAME", new MutableList<>(), h2Profile.api().stringColumnType());
	}

	@Override
	public ProvenShape<Tuple1<String>> $times() {
		Tuple1<Rep<String>> tuple = new Tuple1<>(name());
		System.out.println(tableProvider());
		System.out.println("~~~~~");
		JdbcTypesComponent.JdbcTypes.StringJdbcType strType = h2Profile.api().stringColumnType();
		Shape<FlatShapeLevel, Rep<String>, String, Rep<String>> strShape = Shape.repColumnShape(strType);
		Shape<FlatShapeLevel, Tuple1<Rep<String>>, Tuple1<String>, Tuple1<Rep<String>>> tuple1Shape
				= Shape.tuple1Shape(strShape);
		return ProvenShape$.MODULE$.proveShapeOf(tuple, tuple1Shape);
	}
}
