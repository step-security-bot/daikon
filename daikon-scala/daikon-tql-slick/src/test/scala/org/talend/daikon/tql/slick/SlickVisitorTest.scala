package org.talend.daikon.tql.slick

import org.scalatest.flatspec.AnyFlatSpec
import org.talend.daikon.tql.slick.SlickVisitor
import org.talend.tql.model.{AllFields, AndExpression, ComparisonExpression, ComparisonOperator, Expression, FieldBetweenExpression, FieldCompliesPattern, FieldContainsExpression, FieldInExpression, FieldIsEmptyExpression, FieldIsInvalidExpression, FieldIsValidExpression, FieldMatchesRegex, FieldReference, FieldWordCompliesPattern, LiteralValue, NotExpression, OrExpression, TqlElement}
import org.talend.tql.parser.Tql
import org.talend.tql.visitor.IASTVisitor
import slick.driver.H2Driver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.Stack
import scala.concurrent.Await

class TestBean(tag: Tag) extends Table[(Int, Int, String)](tag, "TEST_BEAN") {
  def id = column[Int]("ID", O.PrimaryKey)

  def int_value = column[String]("INT")

  def text = column[String]("STRING")

  def * = (id, int_value, text)
}

class SlickVisitorTest extends AnyFlatSpec {



  "A SlickVisitor" should "filter values using GreaterThan operator" in {
    val testBeans = TableQuery[TestBean]

    val db = Database.forConfig("h2mem1")
    try {
      val setup = DBIO.seq(
        testBeans.schema.create,
        testBeans += (1, 10, "String1"),
        testBeans += (2, 40, "String2"),
        testBeans += (3, 70, "String3"),
      )

      val expression = Tql.parse("int_value > 20")
      val filter = expression.accept(new SlickVisitor[TestBean])

      val setupFuture = db.run(setup)
      Await.result(setupFuture)

      val filteredResults : Query[TestBean] = testBeans.filter(filter).result
      val value : List[TestBean] = Await.result(db.run(filteredResults))
      assert(value.size === 2)
    } finally db.close
  }

}
