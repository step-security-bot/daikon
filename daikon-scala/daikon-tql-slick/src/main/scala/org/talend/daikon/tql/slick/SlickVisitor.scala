package org.talend.daikon.tql.slick

import org.talend.tql.model.{AllFields, AndExpression, ComparisonExpression, ComparisonOperator, Expression, FieldBetweenExpression, FieldCompliesPattern, FieldContainsExpression, FieldInExpression, FieldIsEmptyExpression, FieldIsInvalidExpression, FieldIsValidExpression, FieldMatchesRegex, FieldReference, FieldWordCompliesPattern, LiteralValue, NotExpression, OrExpression, TqlElement}
import org.talend.tql.visitor.IASTVisitor
import slick.lifted.CanBeQueryCondition

class SlickVisitor[T] extends IASTVisitor[slick.lifted.CanBeQueryCondition[T]] {

  /**
    * Visits a {@link TqlElement}
    *
    * @param elt element to visit
    */
  override def visit(elt: TqlElement): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link ComparisonOperator}
    *
    * @param elt element to visit
    */
  override def visit(elt: ComparisonOperator): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link LiteralValue}
    *
    * @param elt element to visit
    */
  override def visit(elt: LiteralValue): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldReference}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldReference): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link Expression}
    *
    * @param elt element to visit
    */
  override def visit(elt: Expression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link AndExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: AndExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link OrExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: OrExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link ComparisonExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: ComparisonExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldInExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldInExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldIsEmptyExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldIsEmptyExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldIsValidExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldIsValidExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldIsInvalidExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldIsInvalidExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldMatchesRegex}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldMatchesRegex): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldCompliesPattern}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldCompliesPattern): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldWordCompliesPattern}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldWordCompliesPattern): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldBetweenExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldBetweenExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link NotExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: NotExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link FieldContainsExpression}
    *
    * @param elt element to visit
    */
  override def visit(elt: FieldContainsExpression): CanBeQueryCondition[T] = ???

  /**
    * Visits a {@link AllFields}
    *
    * @param allFields the element that represent all fields.
    */
  override def visit(allFields: AllFields): CanBeQueryCondition[T] = ???
}
