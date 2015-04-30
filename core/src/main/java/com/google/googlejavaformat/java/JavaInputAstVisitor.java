/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.googlejavaformat.java;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.googlejavaformat.CloseOp;
import com.google.googlejavaformat.Doc;
import com.google.googlejavaformat.Indent;
import com.google.googlejavaformat.Input;
import com.google.googlejavaformat.Op;
import com.google.googlejavaformat.OpenOp;
import com.google.googlejavaformat.OpsBuilder;
import com.google.googlejavaformat.Output.BreakTag;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An extension of {@link OpsBuilder}, implementing a visit pattern for Eclipse AST nodes to build a
 * sequence of {@link Op}s.
 */
@SuppressWarnings({"unchecked", "rawtypes"}) // jdt uses rawtypes extensively
public final class JavaInputAstVisitor extends ASTVisitor {
  /** Direction for Annotations (usually VERTICAL). */
  enum Direction {
    VERTICAL, HORIZONTAL;

    boolean isVertical() {
      return this == VERTICAL;
    }
  }

  /** Whether to break or not. */
  enum BreakOrNot {
    YES, NO;

    boolean isYes() {
      return this == YES;
    }
  }

  /** Whether to collapse empty blocks. */
  enum CollapseEmptyOrNot {
    YES, NO;

    static CollapseEmptyOrNot valueOf(boolean b) {
      return b ? YES : NO;
    }

    boolean isYes() {
      return this == YES;
    }
  }

  /** Whether to allow trailing blank lines in blocks. */
  enum AllowTrailingBlankLine {
    YES, NO;

    static AllowTrailingBlankLine valueOf(boolean b) {
      return b ? YES : NO;
    }
  }

  /** Whether to include braces. */
  enum BracesOrNot {
    YES, NO;

    boolean isYes() {
      return this == YES;
    }
  }

  /** Whether or not to include dimensions. */
  enum DimensionsOrNot {
    YES, NO;

    boolean isYes() {
      return this == YES;
    }
  }

  /** Whether or not the declaration is Varargs. */
  enum VarArgsOrNot {
    YES, NO;

    static VarArgsOrNot valueOf(boolean b) {
      return b ? YES : NO;
    }

    boolean isYes() {
      return this == YES;
    }
  }

  /** Whether these declarations are the first in the block. */
  enum FirstDeclarationsOrNot {
    YES, NO;

    boolean isYes() {
      return this == YES;
    }
  }

  private final OpsBuilder builder;

  private static final Indent.Const ZERO = Indent.Const.ZERO;
  private final Indent.Const minusTwo;
  private final Indent.Const plusTwo;
  private final Indent.Const plusFour;
  private final Indent.Const plusEight;

  private static final ImmutableList<Op> BREAK_LIST =
      ImmutableList.of(
          OpenOp.make(ZERO, 0), Doc.Break.make(Doc.FillMode.INDEPENDENT, " ", ZERO),
          CloseOp.make());
  private static final ImmutableList<Op> FORCE_BREAK_LIST =
      ImmutableList.<Op>of(Doc.Break.makeForced());
  private static final ImmutableList<Op> EMPTY_LIST = ImmutableList.of();
  private static final Map<String, Integer> PRECEDENCE = new HashMap<>();
  private static final int MAX_LINES_FOR_ARGUMENTS = 1;
  private static final int MAX_LINES_FOR_ARRAY_INITIALIZERS = 3;
  private static final int MAX_LINES_FOR_ANNOTATION_ELEMENT_VALUE_PAIRS = 1;

  static {
    PRECEDENCE.put("*", 10);
    PRECEDENCE.put("/", 10);
    PRECEDENCE.put("%", 10);
    PRECEDENCE.put("+", 9);
    PRECEDENCE.put("-", 9);
    PRECEDENCE.put("<<", 8);
    PRECEDENCE.put(">>", 8);
    PRECEDENCE.put(">>>", 8);
    PRECEDENCE.put("<", 7);
    PRECEDENCE.put(">", 7);
    PRECEDENCE.put("<=", 7);
    PRECEDENCE.put(">=", 7);
    PRECEDENCE.put("==", 6);
    PRECEDENCE.put("!=", 6);
    PRECEDENCE.put("&", 5);
    PRECEDENCE.put("^", 4);
    PRECEDENCE.put("|", 3);
    PRECEDENCE.put("&&", 2);
    PRECEDENCE.put("||", 1);
  }

  private static final int MAX_FILLED_INFIX_LINES = 3;
  private static final int MAX_LINES_FOR_FORMAL_LIST = 1;

  /**
   * The {@code Visitor} constructor.
   * @param builder the {@link OpsBuilder}
   */
  public JavaInputAstVisitor(OpsBuilder builder, int indentMultiplier) {
    this.builder = builder;
    minusTwo = Indent.Const.make(-2, indentMultiplier);
    plusTwo = Indent.Const.make(+2, indentMultiplier);
    plusFour = Indent.Const.make(+4, indentMultiplier);
    plusEight = Indent.Const.make(+8, indentMultiplier);
  }

  /** Visitor method for a {@link CompilationUnit}. */
  @Override
  public boolean visit(CompilationUnit node) {
    boolean first = true;
    if (node.getPackage() != null) {
      visit(node.getPackage());
      builder.breakOp();
      first = false;
    }
    if (!node.imports().isEmpty()) {
      if (!first) {
        builder.blankLineWanted(true);
      }
      for (ImportDeclaration importDeclaration : (List<ImportDeclaration>) node.imports()) {
        visit(importDeclaration);
        builder.breakOp();
      }
      first = false;
    }
    for (AbstractTypeDeclaration type : (List<AbstractTypeDeclaration>) node.types()) {
      if (!first) {
        builder.blankLineWanted(true);
      }
      type.accept(this);
      builder.breakOp();
      first = false;
    }
    return false;
  }

  /** Visitor method for {@link AnnotationTypeDeclaration}s. */
  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    sync(node);
    builder.open(ZERO);
    visitAndBreakModifiers(node.modifiers(), Direction.VERTICAL);
    builder.open(ZERO);
    token("@");
    token("interface");
    builder.breakOp(" ");
    visit(node.getName());
    builder.close();
    builder.close();
    if (node.bodyDeclarations() == null) {
      builder.open(plusFour);
      token(";");
      builder.close();
    } else {
      addBodyDeclarations(node.bodyDeclarations(), BracesOrNot.YES, FirstDeclarationsOrNot.YES);
    }
    builder.guessToken(";");
    return false;
  }

  /** Visitor method for {@link AnnotationTypeMemberDeclaration}s. */
  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    sync(node);
    declareOne(
        Direction.VERTICAL,
        node.modifiers(),
        node.getType(),
        VarArgsOrNot.NO,
        ImmutableList.<Annotation>of(),
        node.getName(),
        "()",
        ImmutableList.<Dimension>of(),
        "default",
        Optional.fromNullable(node.getDefault()));
    token(";");
    return false;
  }

  /** Visitor method for {@link AnonymousClassDeclaration}s. */
  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    sync(node);
    addBodyDeclarations(node.bodyDeclarations(), BracesOrNot.YES, FirstDeclarationsOrNot.YES);
    return false;
  }

  // TODO(jdd): Get rid of instanceof?

  /** Visitor method for {@link ArrayAccess}es. */
  @Override
  public boolean visit(ArrayAccess node) {
    sync(node);
    builder.open(plusFour);
    // Collapse chains of ArrayAccess nodes.
    ArrayDeque<Expression> stack = new ArrayDeque<>();
    Expression array;
    while (true) {
      stack.addLast(node.getIndex());
      array = node.getArray();
      if (!(array instanceof ArrayAccess)) {
        break;
      }
      node = (ArrayAccess) array;
    }
    array.accept(this);
    do {
      token("[");
      builder.breakToFill();
      stack.removeLast().accept(this);
      token("]");
    } while (!stack.isEmpty());
    builder.close();
    return false;
  }

  /** Visitor method for {@link ArrayCreation}s. */
  @Override
  public boolean visit(ArrayCreation node) {
    sync(node);
    builder.open(plusFour);
    token("new");
    builder.space();
    visitArrayType(node.getType(), DimensionsOrNot.NO);
    int dimensions = node.getType().getDimensions();
    builder.open(ZERO);
    for (int i = 0; i < dimensions; i++) {
      builder.breakOp();
      token("[");
      if (i < node.dimensions().size()) {
        ((Expression) node.dimensions().get(i)).accept(this);
      }
      token("]");
    }
    builder.close();
    builder.close();
    if (node.getInitializer() != null) {
      builder.space();
      visit(node.getInitializer());
    }
    return false;
  }

  /** Visitor method for {@link ArrayInitializer}s. */
  @Override
  public boolean visit(ArrayInitializer node) {
    sync(node);
    if (node.expressions().isEmpty()) {
      tokenBreakTrailingComment("{", plusTwo);
      builder.blankLineWanted(false);
      token("}", plusTwo);
    } else {
      builder.open(plusTwo);
      tokenBreakTrailingComment("{", plusTwo);
      builder.blankLineWanted(false);
      builder.breakOp();
      builder.open(ZERO, MAX_LINES_FOR_ARRAY_INITIALIZERS);
      boolean hasTrailingComma = hasTrailingToken(builder.getInput(), node.expressions(), ",");
      boolean first = true;
      for (Expression expression : (List<Expression>) node.expressions()) {
        if (!first) {
          token(",");
          if (hasTrailingComma) {
            builder.forcedBreak();
          } else {
            builder.breakToFill(" ");
          }
        }
        expression.accept(this);
        first = false;
      }
      builder.guessToken(",");
      builder.close();
      builder.breakOp(minusTwo);
      builder.blankLineWanted(false);
      builder.close();
      token("}", plusTwo);
    }
    return false;
  }

  /** Visitor method for {@link ArrayType}s. */
  @Override
  public boolean visit(ArrayType node) {
    sync(node);
    visitArrayType(node, DimensionsOrNot.YES);
    return false;
  }

  /** Visitor method for {@link AssertStatement}s. */
  @Override
  public boolean visit(AssertStatement node) {
    sync(node);
    builder.open(ZERO);
    token("assert");
    builder.space();
    builder.open(node.getMessage() == null ? ZERO : plusFour);
    node.getExpression().accept(this);
    if (node.getMessage() != null) {
      builder.breakOp(" ");
      token(":");
      builder.space();
      node.getMessage().accept(this);
    }
    builder.close();
    builder.close();
    token(";");
    return false;
  }

  /** Visitor method for {@link Assignment}s. */
  @Override
  public boolean visit(Assignment node) {
    sync(node);
    builder.open(plusFour);
    node.getLeftHandSide().accept(this);
    builder.space();
    builder.op(node.getOperator().toString());
    builder.breakOp(" ");
    node.getRightHandSide().accept(this);
    builder.close();
    return false;
  }

  /** Visitor method for {@link Block}s. */
  @Override
  public boolean visit(Block node) {
    visitBlock(node, CollapseEmptyOrNot.YES, AllowTrailingBlankLine.NO);
    return false;
  }

  /** Visitor method for {@link BooleanLiteral}s. */
  @Override
  public boolean visit(BooleanLiteral node) {
    sync(node);
    token(node.toString());
    return false;
  }

  /** Visitor method for {@link BreakStatement}s. */
  @Override
  public boolean visit(BreakStatement node) {
    sync(node);
    builder.open(plusFour);
    token("break");
    if (node.getLabel() != null) {
      builder.breakOp(" ");
      visit(node.getLabel());
    }
    builder.close();
    token(";");
    return false;
  }

  /** Visitor method for {@link CastExpression}s. */
  @Override
  public boolean visit(CastExpression node) {
    sync(node);
    builder.open(plusFour);
    token("(");
    node.getType().accept(this);
    token(")");
    builder.breakOp(" ");
    node.getExpression().accept(this);
    builder.close();
    return false;
  }

  /** Visitor method for {@link CharacterLiteral}s. */
  @Override
  public boolean visit(CharacterLiteral node) {
    sync(node);
    token(node.getEscapedValue());
    return false;
  }

  /** Visitor method for {@link ClassInstanceCreation}s. */
  @Override
  public boolean visit(ClassInstanceCreation node) {
    sync(node);
    builder.open(ZERO);
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
      builder.breakOp();
      token(".");
    }
    token("new");
    builder.space();
    addTypeArguments(node.typeArguments(), plusFour);
    node.getType().accept(this);
    token("(");
    addArguments(node.arguments(), plusFour);
    token(")");
    builder.close();
    if (node.getAnonymousClassDeclaration() != null) {
      visit(node.getAnonymousClassDeclaration());
    }
    return false;
  }

  /** Visitor method for {@link ConditionalExpression}s. */
  @Override
  public boolean visit(ConditionalExpression node) {
    sync(node);
    builder.open(plusFour);
    node.getExpression().accept(this);
    builder.breakOp(" ");
    token("?");
    builder.space();
    node.getThenExpression().accept(this);
    builder.breakOp(" ");
    token(":");
    builder.space();
    node.getElseExpression().accept(this);
    builder.close();
    return false;
  }

  /** Visitor method for {@link ConstructorInvocation}s. */
  @Override
  public boolean visit(ConstructorInvocation node) {
    sync(node);
    addTypeArguments(node.typeArguments(), plusFour);
    token("this");
    token("(");
    addArguments(node.arguments(), plusFour);
    token(")");
    token(";");
    return false;
  }

  /** Visitor method for {@link ContinueStatement}s. */
  @Override
  public boolean visit(ContinueStatement node) {
    sync(node);
    builder.open(plusFour);
    token("continue");
    if (node.getLabel() != null) {
      builder.breakOp(" ");
      visit(node.getLabel());
    }
    token(";");
    builder.close();
    return false;
  }

  /** Visitor method for {@link CreationReference}s. */
  @Override
  public boolean visit(CreationReference node) {
    sync(node);
    builder.open(plusFour);
    node.getType().accept(this);
    builder.breakOp();
    builder.op("::");
    addTypeArguments(node.typeArguments(), plusFour);
    token("new");
    builder.close();
    return false;
  }

  /** Visitor method for {@link Dimension}s. */
  @Override
  public boolean visit(Dimension node) {
    sync(node);
    if (!node.annotations().isEmpty()) {
      builder.open(ZERO);
      visitAnnotations(node.annotations(), BreakOrNot.NO, BreakOrNot.NO);
      builder.breakToFill(" ");
      builder.close();
    }
    token("[");
    token("]");
    return false;
  }

  /** Visitor method for {@link DoStatement}s. */
  @Override
  public boolean visit(DoStatement node) {
    sync(node);
    token("do");
    builder.space();
    visitStatement(node.getBody(), CollapseEmptyOrNot.YES, AllowTrailingBlankLine.YES);
    if (node.getBody().getNodeType() == ASTNode.BLOCK) {
      builder.space();
    } else {
      builder.breakOp(" ");
    }
    token("while");
    builder.space();
    token("(");
    node.getExpression().accept(this);
    token(")");
    token(";");
    return false;
  }

  /** Visitor method for {@link EmptyStatement}s. */
  @Override
  public boolean visit(EmptyStatement node) {
    sync(node);
    token(";");
    return false;
  }

  /** Visitor method for {@link EnhancedForStatement}s. */
  @Override
  public boolean visit(EnhancedForStatement node) {
    sync(node);
    builder.open(ZERO);
    token("for");
    builder.space();
    token("(");
    builder.open(ZERO);
    visitToDeclare(
        Direction.HORIZONTAL, node.getParameter(), Optional.of(node.getExpression()), ":");
    builder.close();
    token(")");
    builder.space();
    builder.close();
    visitStatement(node.getBody(), CollapseEmptyOrNot.YES, AllowTrailingBlankLine.NO);
    return false;
  }

  /** Visitor method for {@link EnumConstantDeclaration}s. */
  @Override
  public boolean visit(EnumConstantDeclaration node) {
    sync(node);
    List<Op> breaks = visitModifiers(node.modifiers(), Direction.VERTICAL);
    if (!breaks.isEmpty()) {
      builder.open(ZERO);
      builder.addAll(breaks);
      builder.close();
    }
    visit(node.getName());
    if (node.arguments().isEmpty()) {
      builder.guessToken("(");
      builder.guessToken(")");
    } else {
      token("(");
      addArguments(node.arguments(), plusFour);
      token(")");
    }
    if (node.getAnonymousClassDeclaration() != null) {
      visit(node.getAnonymousClassDeclaration());
    }
    return false;
  }

  /** Visitor method for {@link EnumDeclaration}s. */
  @Override
  public boolean visit(EnumDeclaration node) {
    sync(node);
    builder.open(ZERO);
    visitAndBreakModifiers(node.modifiers(), Direction.VERTICAL);
    builder.open(plusFour);
    token("enum");
    builder.breakOp(" ");
    visit(node.getName());
    builder.close();
    builder.close();
    if (!node.superInterfaceTypes().isEmpty()) {
      builder.open(plusFour);
      builder.breakOp(" ");
      builder.open(plusFour);
      token("implements");
      builder.breakOp(" ");
      builder.open(ZERO);
      boolean first = true;
      for (Type superInterfaceType : (List<Type>) node.superInterfaceTypes()) {
        if (!first) {
          token(",");
          builder.breakToFill(" ");
        }
        superInterfaceType.accept(this);
        first = false;
      }
      builder.close();
      builder.close();
      builder.close();
    }
    builder.space();
    tokenBreakTrailingComment("{", plusTwo);
    if (node.enumConstants().isEmpty()) {
      builder.open(ZERO);
      builder.blankLineWanted(false);
      token("}");
      builder.close();
    } else {
      builder.open(plusTwo);
      builder.blankLineWanted(false);
      builder.forcedBreak();
      builder.open(ZERO);
      boolean first = true;
      for (EnumConstantDeclaration enumConstant :
          (List<EnumConstantDeclaration>) node.enumConstants()) {
        if (!first) {
          token(",");
          builder.forcedBreak();
        }
        visit(enumConstant);
        first = false;
      }
      if (builder.peekToken().or("").equals(",")) {
        token(",");
        builder.forcedBreak(); // The ";" goes on its own line.
      }
      builder.close();
      builder.close();
      builder.open(ZERO);
      if (node.bodyDeclarations().isEmpty()) {
        builder.guessToken(";");
      } else {
        token(";");
        builder.forcedBreak();
        addBodyDeclarations(node.bodyDeclarations(), BracesOrNot.NO, FirstDeclarationsOrNot.NO);
      }
      builder.forcedBreak();
      builder.blankLineWanted(false);
      token("}", plusTwo);
      builder.close();
    }
    builder.guessToken(";");
    return false;
  }

  /** Visitor method for {@link ExpressionMethodReference}s. */
  @Override
  public boolean visit(ExpressionMethodReference node) {
    sync(node);
    builder.open(plusFour);
    node.getExpression().accept(this);
    builder.breakOp();
    builder.op("::");
    if (!node.typeArguments().isEmpty()) {
      addTypeArguments(node.typeArguments(), plusFour);
    }
    visit(node.getName());
    builder.close();
    return false;
  }

  /** Visitor method for {@link ExpressionStatement}s. */
  @Override
  public boolean visit(ExpressionStatement node) {
    sync(node);
    node.getExpression().accept(this);
    token(";");
    return false;
  }

  /** Visitor method for {@link FieldAccess}es. */
  @Override
  public boolean visit(FieldAccess node) {
    sync(node);
    visitDot(node);
    return false;
  }

  /** Visitor method for {@link FieldDeclaration}s. */
  @Override
  public boolean visit(FieldDeclaration node) {
    sync(node);
    addDeclaration(node.modifiers(), node.getType(), node.fragments(), Direction.VERTICAL);
    builder.guessToken(";");
    return false;
  }

  /** Visitor method for {@link ForStatement}s. */
  @Override
  public boolean visit(ForStatement node) {
    sync(node);
    token("for");
    builder.space();
    token("(");
    builder.open(plusFour);
    builder.open(node.initializers().size() <= 1 ? ZERO : plusFour);
    boolean first = true;
    for (Expression initializer : (List<Expression>) node.initializers()) {
      if (!first) {
        token(",");
        builder.breakToFill(" ");
      }
      initializer.accept(this);
      first = false;
    }
    builder.close();
    token(";");
    if (node.getExpression() == null) {
      builder.breakOp("");
    } else {
      builder.breakOp(" ");
      node.getExpression().accept(this);
    }
    token(";");
    if (node.updaters().isEmpty()) {
      builder.breakOp("");
    } else {
      builder.breakOp(" ");
      builder.open(node.updaters().size() <= 1 ? ZERO : plusFour);
      boolean firstUpdater = true;
      for (Expression updater : (List<Expression>) node.updaters()) {
        if (!firstUpdater) {
          token(",");
          builder.breakToFill(" ");
        }
        updater.accept(this);
        firstUpdater = false;
      }
      builder.close();
    }
    builder.close();
    token(")");
    builder.space();
    visitStatement(node.getBody(), CollapseEmptyOrNot.YES, AllowTrailingBlankLine.NO);
    return false;
  }

  /** Visitor method for {@link IfStatement}s. */
  @Override
  public boolean visit(IfStatement node) {
    sync(node);
    // Collapse chains of else-ifs.
    List<Expression> expressions = new ArrayList<>();
    List<Statement> statements = new ArrayList<>();
    while (true) {
      expressions.add(node.getExpression());
      statements.add(node.getThenStatement());
      if (node.getElseStatement() != null
          && node.getElseStatement().getNodeType() == ASTNode.IF_STATEMENT) {
        node = (IfStatement) node.getElseStatement();
      } else {
        break;
      }
    }
    builder.open(ZERO);
    boolean first = true;
    boolean followingBlock = false;
    int expressionsN = expressions.size();
    for (int i = 0; i < expressionsN; i++) {
      if (!first) {
        if (followingBlock) {
          builder.space();
        } else {
          builder.forcedBreak();
        }
        token("else");
        builder.space();
      }
      token("if");
      builder.space();
      token("(");
      expressions.get(i).accept(this);
      token(")");
      builder.space();
      // An empty block can collapse to "{}" if there are no if/else or else clauses
      boolean onlyClause = expressionsN == 1 && node.getElseStatement() == null;
      // Trailing blank lines are permitted if this isn't the last clause
      boolean trailingClauses = i < expressionsN - 1 || node.getElseStatement() != null;
      visitStatement(
          statements.get(i),
          CollapseEmptyOrNot.valueOf(onlyClause),
          AllowTrailingBlankLine.valueOf(trailingClauses));
      followingBlock = statements.get(i).getNodeType() == ASTNode.BLOCK;
      first = false;
    }
    if (node.getElseStatement() != null) {
      if (followingBlock) {
        builder.space();
      } else {
        builder.forcedBreak();
      }
      token("else");
      builder.space();
      visitStatement(node.getElseStatement(), CollapseEmptyOrNot.NO, AllowTrailingBlankLine.NO);
    }
    builder.close();
    return false;
  }

  /** Visitor method for {@link ImportDeclaration}s. */
  @Override
  public boolean visit(ImportDeclaration node) {
    sync(node);
    token("import");
    builder.space();
    if (node.isStatic()) {
      token("static");
      builder.space();
    }
    visitName(node.getName(), BreakOrNot.NO);
    if (node.isOnDemand()) {
      token(".");
      token("*");
    }
    token(";");
    return false;
  }

  /** Visitor method for {@link InfixExpression}s. */
  @Override
  public boolean visit(InfixExpression node) {
    sync(node);
    /*
     * Collect together all operators with same precedence to clean up indentation. Eclipse's
     * extended operands help a little (to collect together the same operator), but they're applied
     * inconsistently, and don't apply to other operators of the same precedence.
     */
    List<Expression> operands = new ArrayList<>();
    List<String> operators = new ArrayList<>();
    walkInfix(PRECEDENCE.get(node.getOperator().toString()), node, operands, operators);
    builder.open(plusFour, MAX_FILLED_INFIX_LINES);
    operands.get(0).accept(this);
    int operatorsN = operators.size();
    for (int i = 0; i < operatorsN; i++) {
      builder.breakToFill(" ");
      builder.op(operators.get(i));
      builder.space();
      operands.get(i + 1).accept(this);
    }
    builder.close();
    return false;
  }

  /** Visitor method for {@link Initializer}s. */
  @Override
  public boolean visit(Initializer node) {
    sync(node);
    visitAndBreakModifiers(node.modifiers(), Direction.VERTICAL);
    visit(node.getBody());
    builder.guessToken(";");
    return false;
  }

  /** Visitor method for {@link InstanceofExpression}s. */
  @Override
  public boolean visit(InstanceofExpression node) {
    sync(node);
    builder.open(plusFour);
    node.getLeftOperand().accept(this);
    builder.breakOp(" ");
    builder.open(ZERO);
    token("instanceof");
    builder.breakOp(" ");
    node.getRightOperand().accept(this);
    builder.close();
    builder.close();
    return false;
  }

  /** Visitor method for {@link IntersectionType}s. */
  @Override
  public boolean visit(IntersectionType node) {
    sync(node);
    builder.open(plusFour);
    List<Type> types = new ArrayList<>();
    walkIntersectionTypes(types, node);
    boolean first = true;
    for (Type type : types) {
      if (!first) {
        builder.breakToFill(" ");
        token("&");
        builder.space();
      }
      type.accept(this);
      first = false;
    }
    builder.close();
    return false;
  }

  /** Visitor method for {@link LabeledStatement}s. */
  @Override
  public boolean visit(LabeledStatement node) {
    sync(node);
    builder.open(ZERO);
    visit(node.getLabel());
    token(":");
    builder.forcedBreak();
    builder.close();
    node.getBody().accept(this);
    return false;
  }

  /** Visitor method for {@link LambdaExpression}s. */
  @Override
  public boolean visit(LambdaExpression node) {
    sync(node);
    builder.open(plusFour);
    builder.open(plusFour);
    if (node.hasParentheses()) {
      token("(");
    }
    boolean first = true;
    for (ASTNode parameter : (List<ASTNode>) node.parameters()) {
      if (!first) {
        token(",");
        builder.breakOp(" ");
      }
      if (parameter.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
        visit((SingleVariableDeclaration) parameter);
      } else if (parameter.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
        visit((VariableDeclarationFragment) parameter);
      } else {
        token("<<<PARAMETER>>>");
      }
      first = false;
    }
    if (node.hasParentheses()) {
      token(")");
    }
    builder.close();
    builder.breakOp(" ");
    builder.op("->");
    builder.space();
    if (node.getBody().getNodeType() == ASTNode.BLOCK) {
      visit((Block) node.getBody());
    } else if (node.getBody() instanceof Expression) {
      node.getBody().accept(this);
    } else {
      token("<<<BODY>>>");
    }
    builder.close();
    return false;
  }

  /** Visitor method for {@link MarkerAnnotation}s. */
  @Override
  public boolean visit(MarkerAnnotation node) {
    sync(node);
    builder.open(ZERO);
    token("@");
    node.getTypeName().accept(this);
    builder.close();
    return false;
  }

  /** Visitor method for {@link MemberValuePair}s. */
  @Override
  public boolean visit(MemberValuePair node) {
    sync(node);
    builder.open(plusFour);
    visit(node.getName());
    builder.space();
    token("=");
    builder.breakOp(" ");
    node.getValue().accept(this);
    builder.close();
    return false;
  }

  /*
   * Method declarations are perhaps the most difficult part of implementing Google's Java Style
   * Guide. They have too many optional slots for a simple break-from-the-top approach to apply
   * directly, and so a slightly more complex approach is needed.
   */

  /** Visitor method for {@link MethodDeclaration}s. */
  @Override
  public boolean visit(MethodDeclaration node) {
    sync(node);
    visitAndBreakModifiers(node.modifiers(), Direction.VERTICAL);
    builder.open(ZERO);
    boolean first = true;
    if (!node.typeParameters().isEmpty()) {
      builder.breakToFill("");
      visitTypeParameters(node.typeParameters(), plusFour, BreakOrNot.NO);
      first = false;
    }
    if (!node.isConstructor()) {
      if (!first) {
        builder.breakOp(
            Doc.FillMode.INDEPENDENT, " ", ZERO,
            Optional.of(Doc.ProgressiveIndent.make(plusFour, ZERO)));
      }
      if (node.getReturnType2() == null) {
        token("void");
      } else {
        builder.open(plusFour);
        node.getReturnType2().accept(this);
        builder.close();
      }
      first = false;
    }
    // Break before method name.
    builder.breakOp(
        Doc.FillMode.INDEPENDENT, first ? "" : " ", ZERO,
        Optional.of(Doc.ProgressiveIndent.make(plusFour, plusFour)));
    visit(node.getName());
    token("(");
    BreakTag argsBreak = genSym();
    if (!node.parameters().isEmpty() || node.getReceiverType() != null) {
      // Break before args.
      builder.breakOp(
          Doc.FillMode.INDEPENDENT, "", ZERO,
          Optional.of(Doc.ProgressiveIndent.make(plusFour, ZERO)), Optional.of(argsBreak));
      visitFormals(
          Optional.fromNullable(node.getReceiverType()), node.getReceiverQualifier(),
          node.parameters());
    }
    token(")");
    extraDimensions(plusFour, node.extraDimensions());
    if (!node.thrownExceptionTypes().isEmpty()) {
      builder.breakOp(
          Doc.FillMode.INDEPENDENT, " ", Indent.If.make(argsBreak, ZERO, plusFour),
          Optional.of(Doc.ProgressiveIndent.make(ZERO, plusFour)));
      builder.open(Indent.If.make(argsBreak, ZERO, plusFour));
      visitThrowsClause(node.thrownExceptionTypes());
      builder.close();
    }
    builder.close();
    if (node.getBody() == null) {
      token(";");
    } else {
      builder.space();
      visit(node.getBody());
    }
    builder.guessToken(";");
    return false;
  }

  /** Visitor method for {@link MethodInvocation}s. */
  @Override
  public boolean visit(MethodInvocation node) {
    sync(node);
    visitDot(node);
    return false;
  }

  /** Visitor method for {@link Modifier}s. */
  @Override
  public boolean visit(Modifier node) {
    sync(node);
    token(node.toString());
    return false;
  }

  // TODO(jdd): Collapse chains of "." operators here too.

  /** Visitor method for {@link NameQualifiedType}s. */
  @Override
  public boolean visit(NameQualifiedType node) {
    sync(node);
    beforeAnnotatableType(node);
    builder.open(plusFour);
    node.getQualifier().accept(this);
    builder.breakOp();
    token(".");
    visit(node.getName());
    builder.close();
    return false;
  }

  /** Visitor method for {@link NormalAnnotation}s. */
  @Override
  public boolean visit(NormalAnnotation node) {
    sync(node);
    builder.open(plusFour);
    token("@");
    node.getTypeName().accept(this);
    token("(");
    builder.breakOp();
    builder.open(ZERO, MAX_LINES_FOR_ANNOTATION_ELEMENT_VALUE_PAIRS);
    boolean first = true;
    for (MemberValuePair value : (List<MemberValuePair>) node.values()) {
      if (!first) {
        token(",");
        builder.breakToFill(" ");
      }
      value.accept(this);
      first = false;
    }
    builder.close();
    builder.close();
    token(")");
    return false;
  }

  /** Visitor method for {@link NullLiteral}s. */
  @Override
  public boolean visit(NullLiteral node) {
    sync(node);
    token(node.toString());
    return false;
  }

  /** Visitor method for {@link NumberLiteral}s. */
  @Override
  public boolean visit(NumberLiteral node) {
    sync(node);
    token(node.getToken());
    return false;
  }

  /** Visitor method for {@link PackageDeclaration}s. */
  @Override
  public boolean visit(PackageDeclaration node) {
    sync(node);
    visitAndBreakModifiers(node.annotations(), Direction.VERTICAL);
    builder.open(plusFour);
    token("package");
    builder.space();
    visitName(node.getName(), BreakOrNot.NO);
    builder.close();
    token(";");
    return false;
  }

  /** Visitor method for {@link ParameterizedType}s. */
  @Override
  public boolean visit(ParameterizedType node) {
    sync(node);
    if (node.typeArguments().isEmpty()) {
      node.getType().accept(this);
      token("<");
      token(">");
    } else {
      builder.open(plusFour);
      node.getType().accept(this);
      token("<");
      builder.breakOp();
      builder.open(ZERO);
      boolean first = true;
      for (Type typeArgument : (List<Type>) node.typeArguments()) {
        if (!first) {
          token(",");
          builder.breakToFill(" ");
        }
        typeArgument.accept(this);
        first = false;
      }
      builder.close();
      builder.close();
      token(">");
    }
    return false;
  }

  /** Visitor method for {@link ParenthesizedExpression}s. */
  @Override
  public boolean visit(ParenthesizedExpression node) {
    sync(node);
    token("(");
    node.getExpression().accept(this);
    token(")");
    return false;
  }

  /** Visitor method for {@link PostfixExpression}s. */
  @Override
  public boolean visit(PostfixExpression node) {
    sync(node);
    node.getOperand().accept(this);
    builder.op(node.getOperator().toString());
    return false;
  }

  /** Visitor method for {@link PrefixExpression}s. */
  @Override
  public boolean visit(PrefixExpression node) {
    sync(node);
    String op = node.getOperator().toString();
    builder.op(op);
    // Keep prefixes unambiguous.
    Expression operand = node.getOperand();
    if ((op.equals("+") || op.equals("-")) && operand.getNodeType() == ASTNode.PREFIX_EXPRESSION
        && ((PrefixExpression) operand).getOperator().toString().startsWith(op)) {
      builder.space();
    }
    operand.accept(this);
    return false;
  }

  /** Visitor method for {@link PrimitiveType}s. */
  @Override
  public boolean visit(PrimitiveType node) {
    sync(node);
    beforeAnnotatableType(node);
    token(node.toString());
    return false;
  }

  /** Visitor method for {@link QualifiedName}s. */
  @Override
  public boolean visit(QualifiedName node) {
    visitQualifiedName(node, BreakOrNot.YES);
    return false;
  }

  // TODO(jdd): Can we share?

  /** Visitor method for {@link QualifiedType}s. */
  @Override
  public boolean visit(QualifiedType node) {
    sync(node);
    builder.open(plusFour);
    // Collapse chains of "." operators.
    ArrayDeque<SimpleName> stack = new ArrayDeque<>();
    Type qualifier;
    while (true) {
      stack.add(node.getName());
      qualifier = node.getQualifier();
      if (qualifier.getNodeType() != ASTNode.QUALIFIED_TYPE) {
        break;
      }
      node = (QualifiedType) qualifier;
    }
    qualifier.accept(this);
    do {
      builder.breakOp();
      token(".");
      visit(stack.removeLast());
    } while (!stack.isEmpty());
    builder.close();
    return false;
  }

  /** Visitor method for {@link ReturnStatement}s. */
  @Override
  public boolean visit(ReturnStatement node) {
    sync(node);
    token("return");
    if (node.getExpression() != null) {
      builder.space();
      node.getExpression().accept(this);
    }
    token(";");
    return false;
  }

  /** Visitor method for {@link SimpleName}s. */
  @Override
  public boolean visit(SimpleName node) {
    sync(node);
    token(node.getIdentifier());
    return false;
  }

  /** Visitor method for {@link SimpleType}s. */
  @Override
  public boolean visit(SimpleType node) {
    sync(node);
    beforeAnnotatableType(node);
    node.getName().accept(this);
    return false;
  }

  /** Visitor method for {@link SingleMemberAnnotation}s. */
  @Override
  public boolean visit(SingleMemberAnnotation node) {
    sync(node);
    Expression value = node.getValue();
    boolean isArrayInitializer = value.getNodeType() == ASTNode.ARRAY_INITIALIZER;
    builder.open(isArrayInitializer ? ZERO : plusFour);
    token("@");
    node.getTypeName().accept(this);
    token("(");
    if (!isArrayInitializer) {
      builder.breakOp();
    }
    value.accept(this);
    builder.close();
    token(")");
    return false;
  }

  /** Visitor method for {@link SingleVariableDeclaration}s. */
  @Override
  public boolean visit(SingleVariableDeclaration node) {
    visitToDeclare(Direction.VERTICAL, node, Optional.fromNullable(node.getInitializer()), "=");
    return false;
  }

  /** Visitor method for {@link StringLiteral}s. */
  @Override
  public boolean visit(StringLiteral node) {
    sync(node);
    token(node.getEscapedValue());
    return false;
  }

  /** Visitor method for {@link SuperConstructorInvocation}s. */
  @Override
  public boolean visit(SuperConstructorInvocation node) {
    sync(node);
    addTypeArguments(node.typeArguments(), plusFour);
    token("super");
    token("(");
    addArguments(node.arguments(), plusFour);
    token(")");
    token(";");
    return false;
  }

  /** Visitor method for {@link SuperFieldAccess}es. */
  @Override
  public boolean visit(SuperFieldAccess node) {
    sync(node);
    builder.open(plusFour);
    if (node.getQualifier() != null) {
      node.getQualifier().accept(this);
      builder.breakOp();
      token(".");
    }
    token("super");
    builder.breakOp();
    token(".");
    visit(node.getName());
    builder.close();
    return false;
  }

  /** Visitor method for {@link SuperMethodInvocation}s. */
  @Override
  public boolean visit(SuperMethodInvocation node) {
    sync(node);
    builder.open(ZERO);
    if (node.getQualifier() != null) {
      node.getQualifier().accept(this);
      builder.breakOp();
      token(".");
    }
    token("super");
    builder.breakOp();
    token(".");
    builder.close();
    addTypeArguments(node.typeArguments(), plusFour);
    visit(node.getName());
    token("(");
    addArguments(node.arguments(), plusFour);
    token(")");
    return false;
  }

  /** Visitor method for {@link SuperMethodReference}s. */
  @Override
  public boolean visit(SuperMethodReference node) {
    sync(node);
    builder.open(plusFour);
    if (node.getQualifier() != null) {
      builder.open(plusFour);
      node.getQualifier().accept(this);
      builder.breakOp();
      token(".");
      builder.close();
    }
    token("super");
    builder.breakOp();
    builder.op("::");
    if (!node.typeArguments().isEmpty()) {
      addTypeArguments(node.typeArguments(), plusFour);
    }
    visit(node.getName());
    builder.close();
    return false;
  }

  /** Visitor method for {@link SwitchCase}s. */
  @Override
  public boolean visit(SwitchCase node) {
    sync(node);
    if (node.isDefault()) {
      token("default", plusTwo);
      token(":");
    } else {
      token("case", plusTwo);
      builder.space();
      node.getExpression().accept(this);
      token(":");
    }
    return false;
  }

  /** Visitor method for {@link SwitchStatement}s. */
  @Override
  public boolean visit(SwitchStatement node) {
    sync(node);
    token("switch");
    builder.space();
    token("(");
    node.getExpression().accept(this);
    token(")");
    builder.space();
    tokenBreakTrailingComment("{", plusTwo);
    builder.blankLineWanted(false);
    builder.open(plusFour);
    for (ASTNode statement : (List<ASTNode>) node.statements()) {
      if (statement.getNodeType() == ASTNode.SWITCH_CASE) {
        builder.open(minusTwo);
        builder.forcedBreak();
        visit((SwitchCase) statement);
        builder.close();
      } else {
        builder.forcedBreak();
        statement.accept(this);
      }
    }
    builder.close();
    builder.forcedBreak();
    builder.blankLineWanted(false);
    token("}", plusFour);
    return false;
  }

  /** Visitor method for {@link SynchronizedStatement}s. */
  @Override
  public boolean visit(SynchronizedStatement node) {
    sync(node);
    token("synchronized");
    builder.space();
    token("(");
    builder.open(plusFour);
    builder.breakOp();
    node.getExpression().accept(this);
    builder.close();
    token(")");
    builder.space();
    visit(node.getBody());
    return false;
  }

  /** Visitor method for {@link ThisExpression}s. */
  @Override
  public boolean visit(ThisExpression node) {
    sync(node);
    if (node.getQualifier() != null) {
      builder.open(plusFour);
      node.getQualifier().accept(this);
      builder.breakOp();
      token(".");
      builder.close();
    }
    token("this");
    return false;
  }

  /** Visitor method for {@link ThrowStatement}s. */
  @Override
  public boolean visit(ThrowStatement node) {
    sync(node);
    token("throw");
    builder.space();
    node.getExpression().accept(this);
    token(";");
    return false;
  }

  /** Visitor method for {@link TryStatement}s. */
  @Override
  public boolean visit(TryStatement node) {
    sync(node);
    builder.open(ZERO);
    token("try");
    builder.space();
    if (!node.resources().isEmpty()) {
      token("(");
      builder.open(ZERO);
      boolean first = true;
      for (VariableDeclarationExpression resource :
          (List<VariableDeclarationExpression>) node.resources()) {
        if (!first) {
          token(";");
          builder.breakOp(" ");
        }
        visit(resource);
        first = false;
      }
      token(")");
      builder.close();
      builder.space();
    }
    // An empty try-with-resources body can collapse to "{}" if there are no trailing catch or 
    // finally blocks.
    boolean trailingClauses = !node.catchClauses().isEmpty() || node.getFinally() != null;
    visitBlock(
        node.getBody(),
        CollapseEmptyOrNot.valueOf(!trailingClauses),
        AllowTrailingBlankLine.valueOf(trailingClauses));
    for (int i = 0; i < node.catchClauses().size(); i++) {
      CatchClause catchClause = (CatchClause) node.catchClauses().get(i);
      trailingClauses = i < node.catchClauses().size() - 1 || node.getFinally() != null;
      visitCatchClause(catchClause, AllowTrailingBlankLine.valueOf(trailingClauses));
    }
    if (node.getFinally() != null) {
      builder.space();
      token("finally");
      builder.space();
      visitBlock(node.getFinally(), CollapseEmptyOrNot.NO, AllowTrailingBlankLine.NO);
    }
    builder.close();
    return false;
  }

  /** Visitor method for {@link TypeDeclaration}s. */
  @Override
  public boolean visit(TypeDeclaration node) {
    sync(node);
    List<Op> breaks = visitModifiers(node.modifiers(), Direction.VERTICAL);
    boolean hasSuperclassType = node.getSuperclassType() != null;
    boolean hasSuperInterfaceTypes = !node.superInterfaceTypes().isEmpty();
    builder.open(ZERO);
    builder.addAll(breaks);
    token(node.isInterface() ? "interface" : "class");
    builder.space();
    visit(node.getName());
    if (!node.typeParameters().isEmpty()) {
      visitTypeParameters(
          node.typeParameters(), hasSuperclassType || hasSuperInterfaceTypes ? plusFour : ZERO,
          BreakOrNot.YES);
    }
    if (hasSuperclassType || hasSuperInterfaceTypes) {
      builder.open(plusFour);
      builder.breakOp(" ");
      if (hasSuperclassType) {
        builder.open(plusFour);
        token("extends");
        builder.breakToFill(" ");
        node.getSuperclassType().accept(this);
        builder.close();
      }
      if (hasSuperInterfaceTypes) {
        if (hasSuperclassType) {
          builder.breakOp(" ");
        }
        builder.open(plusFour);
        token(node.isInterface() ? "extends" : "implements");
        builder.breakToFill(" ");
        boolean first = true;
        for (Type superInterfaceType : (List<Type>) node.superInterfaceTypes()) {
          if (!first) {
            token(",");
            builder.breakToFill(" ");
          }
          superInterfaceType.accept(this);
          first = false;
        }
        builder.close();
      }
      builder.close();
    }
    builder.close();
    if (node.bodyDeclarations() == null) {
      token(";");
    } else {
      addBodyDeclarations(node.bodyDeclarations(), BracesOrNot.YES, FirstDeclarationsOrNot.YES);
      builder.guessToken(";");
    }
    return false;
  }

  /** Visitor method for {@link TypeDeclarationStatement}s. */
  @Override
  public boolean visit(TypeDeclarationStatement node) {
    sync(node);
    node.getDeclaration().accept(this);
    return false;
  }

  /** Visitor method for {@link TypeLiteral}s. */
  @Override
  public boolean visit(TypeLiteral node) {
    sync(node);
    builder.open(plusFour);
    node.getType().accept(this);
    builder.breakOp();
    token(".");
    token("class");
    builder.close();
    return false;
  }

  /** Visitor method for {@link TypeMethodReference}s. */
  @Override
  public boolean visit(TypeMethodReference node) {
    sync(node);
    builder.open(plusFour);
    node.getType().accept(this);
    builder.breakOp();
    builder.op("::");
    if (!node.typeArguments().isEmpty()) {
      addTypeArguments(node.typeArguments(), plusFour);
    }
    visit(node.getName());
    builder.close();
    return false;
  }

  /** Visitor method for {@link TypeParameter}s. */
  @Override
  public boolean visit(TypeParameter node) {
    sync(node);
    builder.open(ZERO);
    visitAndBreakModifiers(node.modifiers(), Direction.HORIZONTAL);
    visit(node.getName());
    if (!node.typeBounds().isEmpty()) {
      builder.space();
      token("extends");
      builder.open(plusFour);
      builder.breakOp(" ");
      builder.open(plusFour);
      boolean first = true;
      for (Type typeBound : (List<Type>) node.typeBounds()) {
        if (!first) {
          builder.breakToFill(" ");
          token("&");
          builder.space();
        }
        typeBound.accept(this);
        first = false;
      }
      builder.close();
      builder.close();
    }
    builder.close();
    return false;
  }

  /** Visitor method for {@link UnionType}s. */
  @Override
  public boolean visit(UnionType node) {
    sync(node);
    builder.open(plusFour);
    List<Type> types = new ArrayList<>();
    walkUnionTypes(types, node);
    boolean first = true;
    for (Type type : types) {
      if (!first) {
        builder.breakToFill(" ");
        token("|");
        builder.space();
      }
      type.accept(this);
      first = false;
    }
    builder.close();
    return false;
  }

  /** Visitor method for {@link VariableDeclarationExpression}s. */
  @Override
  public boolean visit(VariableDeclarationExpression node) {
    sync(node);
    builder.open(plusFour);
    // TODO(jdd): Why no use common method?
    for (IExtendedModifier modifier : (List<IExtendedModifier>) node.modifiers()) {
      ((ASTNode) modifier).accept(this);
      builder.breakToFill(" ");
    }
    node.getType().accept(this);
    if (node.fragments().size() == 1) {
      builder.breakToFill(" ");
      visit((VariableDeclarationFragment) node.fragments().get(0));
    } else {
      // TODO(jdd): Are the indentations consistent here?
      builder.breakToFill(" ");
      builder.open(plusFour);
      boolean first = true;
      for (VariableDeclarationFragment fragment :
          (List<VariableDeclarationFragment>) node.fragments()) {
        if (!first) {
          token(",");
          builder.breakToFill(" ");
        }
        visit(fragment);
        first = false;
      }
      builder.close();
    }
    builder.close();
    return false;
  }

  /** Visitor method for {@link VariableDeclarationFragment}s. */
  @Override
  public boolean visit(VariableDeclarationFragment node) {
    sync(node);
    // TODO(jdd): Why no open-close?
    visit(node.getName());
    extraDimensions(plusFour, node.extraDimensions());
    if (node.getInitializer() != null) {
      builder.space();
      token("=");
      builder.breakToFill(" ");
      // TODO(jdd): Why this way.
      builder.open(ZERO);
      node.getInitializer().accept(this);
      builder.close();
    }
    return false;
  }

  // TODO(jdd): Worry about upper and lower bounds.

  /** Visitor method for {@link VariableDeclarationStatement}s. */
  @Override
  public boolean visit(VariableDeclarationStatement node) {
    sync(node);
    addDeclaration(
        node.modifiers(), node.getType(), node.fragments(),
        canLocalHaveHorizontalAnnotations(node.modifiers()));
    return false;
  }

  /** Visitor method for {@link WhileStatement}s. */
  @Override
  public boolean visit(WhileStatement node) {
    sync(node);
    token("while");
    builder.space();
    token("(");
    node.getExpression().accept(this);
    token(")");
    builder.space();
    visitStatement(node.getBody(), CollapseEmptyOrNot.YES, AllowTrailingBlankLine.NO);
    return false;
  }

  /** Visitor method for {@link WildcardType}s. */
  @Override
  public boolean visit(WildcardType node) {
    sync(node);
    beforeAnnotatableType(node);
    builder.open(ZERO);
    token("?");
    if (node.getBound() != null) {
      builder.open(plusFour);
      builder.space();
      token(node.isUpperBound() ? "extends" : "super");
      builder.breakOp(" ");
      node.getBound().accept(this);
      builder.close();
    }
    builder.close();
    return false;
  }

  // Helper methods.

  /** Before Visitor methods for {@link Type}. */
  private void beforeAnnotatableType(AnnotatableType node) {
    if (!node.annotations().isEmpty()) {
      builder.open(ZERO);
      for (Annotation annotation : (List<Annotation>) node.annotations()) {
        annotation.accept(this);
        builder.breakOp(" ");
      }
      builder.close();
    }
  }

  /** Helper method for {@link Annotation}s and declareOne. */
  void visitAnnotations(
      List<Annotation> annotations, BreakOrNot breakBefore, BreakOrNot breakAfter) {
    if (!annotations.isEmpty()) {
      if (breakBefore.isYes()) {
        builder.breakToFill(" ");
      }
      boolean first = true;
      for (Annotation annotation : annotations) {
        if (!first) {
          builder.breakToFill(" ");
        }
        annotation.accept(this);
        first = false;
      }
      if (breakAfter.isYes()) {
        builder.breakToFill(" ");
      }
    }
  }

  /**
   * Helper method for {@link Block}s, {@link CatchClause}s, {@link Statement}s,
   * {@link TryStatement}s, and {@link WhileStatement}s.
   */
  private void visitBlock(
      Block node,
      CollapseEmptyOrNot collapseEmptyOrNot,
      AllowTrailingBlankLine allowTrailingBlankLine) {
    sync(node);
    if (collapseEmptyOrNot.isYes() && node.statements().isEmpty()) {
      tokenBreakTrailingComment("{", plusTwo);
      builder.blankLineWanted(false);
      token("}");
    } else {
      builder.open(ZERO);
      builder.open(plusTwo);
      tokenBreakTrailingComment("{", plusTwo);
      for (Statement statement : (List<Statement>) node.statements()) {
        builder.forcedBreak();
        statement.accept(this);
      }
      builder.close();
      builder.forcedBreak();
      builder.close();
      if (allowTrailingBlankLine == AllowTrailingBlankLine.NO) {
        builder.blankLineWanted(false);
      }
      token("}", plusTwo);
    }
  }

  /**
   * Helper method for {@link DoStatement}s, {@link EnhancedForStatement}s, {@link ForStatement}s,
   * {@link IfStatement}s, and WhileStatements.
   */
  private void visitStatement(
      Statement node,
      CollapseEmptyOrNot collapseEmptyOrNot,
      AllowTrailingBlankLine allowTrailingBlank) {
    sync(node);
    switch (node.getNodeType()) {
      case ASTNode.BLOCK:
        visitBlock((Block) node, collapseEmptyOrNot, allowTrailingBlank);
        break;
      default:
        builder.space();
        // TODO(jdd): Fix.
        builder.open(plusTwo);
        builder.forcedBreak();
        node.accept(this);
        builder.close();
    }
  }

  /** Helper method for {@link ArrayCreation}s and {@link ArrayType}s. */
  private void visitArrayType(ArrayType node, DimensionsOrNot includeDimensions) {
    if (includeDimensions.isYes() && !node.dimensions().isEmpty()) {
      builder.open(plusFour);
    }
    node.getElementType().accept(this);
    if (includeDimensions.isYes()) {
      for (Dimension dimension : (List<Dimension>) node.dimensions()) {
        builder.breakToFill(dimension.annotations().isEmpty() ? "" : " ");
        visit(dimension);
      }
    }
    if (includeDimensions.isYes() && !node.dimensions().isEmpty()) {
      builder.close();
    }
  }

  /**
   * Helper methods for {@link AnnotationTypeDeclaration}s,
   * {@link AnnotationTypeMemberDeclaration}s, {@link EnumDeclaration}s, {@link Initializer}s,
   * {@link MethodDeclaration}s, {@link PackageDeclaration}s, and {@link TypeParameter}s. Output
   * combined modifiers and annotations and the trailing break.
   * @param modifiers a list of {@link IExtendedModifier}s, which can include annotations
   * @param annotationDirection direction of annotations
   */
  void visitAndBreakModifiers(List<IExtendedModifier> modifiers, Direction annotationDirection) {
    builder.addAll(visitModifiers(modifiers, annotationDirection));
  }

  /**
   * Helper method for {@link EnumConstantDeclaration}s, {@link TypeDeclaration}s, and
   * {@code visitAndBreakModifiers}. Output combined modifiers and annotations and returns the
   * trailing break.
   * @param modifiers a list of {@link IExtendedModifier}s, which can include annotations
   * @param annotationsDirection {@link Direction#VERTICAL} or {@link Direction#HORIZONTAL}
   * @return the list of {@link Doc.Break}s following the modifiers and annotations
   */
  private List<Op> visitModifiers(
      List<IExtendedModifier> modifiers, Direction annotationsDirection) {
    if (modifiers.isEmpty()) {
      return EMPTY_LIST;
    }
    builder.open(ZERO);
    boolean first = true;
    boolean lastWasAnnotation = false;
    int idx = 0;
    for (; idx < modifiers.size(); idx++) {
      IExtendedModifier modifier = modifiers.get(idx);
      if (modifier.isModifier()) {
        break;
      }
      if (!first) {
        builder.addAll(annotationsDirection.isVertical() ? FORCE_BREAK_LIST : BREAK_LIST);
      }
      ((ASTNode) modifier).accept(this);
      first = false;
      lastWasAnnotation = true;
    }
    builder.close();
    builder.open(ZERO);
    first = true;
    for (; idx < modifiers.size(); idx++) {
      IExtendedModifier modifier = modifiers.get(idx);
      if (!first) {
        builder.addAll(BREAK_LIST);
      } else if (lastWasAnnotation) {
        builder.addAll(annotationsDirection.isVertical() ? FORCE_BREAK_LIST : BREAK_LIST);
      }
      ((ASTNode) modifier).accept(this);
      first = false;
      lastWasAnnotation = false;
    }
    builder.close();
    return lastWasAnnotation && annotationsDirection.isVertical() ? FORCE_BREAK_LIST : BREAK_LIST;
  }

  /** Helper method for {@link CatchClause}s. */
  private void visitCatchClause(CatchClause node, AllowTrailingBlankLine allowTrailingBlankLine) {
    sync(node);
    builder.space();
    token("catch");
    builder.space();
    token("(");
    builder.open(plusFour);
    builder.breakOp();
    builder.open(ZERO);
    visit(node.getException());
    builder.close();
    builder.close();
    token(")");
    builder.space();
    visitBlock(node.getBody(), CollapseEmptyOrNot.NO, allowTrailingBlankLine);
  }

  /**
   * Helper method for {@link InfixExpression}s. Visit this {@link Expression} node, and its
   * children, as long as they are {@link InfixExpression} nodes of the same precedence. Accumulate
   * the operands and operators.
   * @param precedence the precedence of the operators to collect
   * @param operands the output list of {@code n + 1} operands
   * @param operators the output list of {@code n} operators
   */
  private static void walkInfix(
      int precedence, Expression expression, List<Expression> operands, List<String> operators) {
    if (expression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
      InfixExpression infixExpression = (InfixExpression) expression;
      String myOperator = infixExpression.getOperator().toString();
      if (PRECEDENCE.get(myOperator) == precedence) {
        walkInfix(precedence, infixExpression.getLeftOperand(), operands, operators);
        operators.add(myOperator);
        walkInfix(precedence, infixExpression.getRightOperand(), operands, operators);
        if (infixExpression.hasExtendedOperands()) {
          for (Expression extendedOperand : (List<Expression>) infixExpression.extendedOperands()) {
            operators.add(myOperator);
            walkInfix(precedence, extendedOperand, operands, operators);
          }
        }
      } else {
        operands.add(expression);
      }
    } else {
      operands.add(expression);
    }
  }

  // TODO(jdd): Merge with union types.

  /** Helper method for {@link IntersectionType}s. */
  private static void walkIntersectionTypes(List<Type> types, IntersectionType node) {
    for (ASTNode type : (List<ASTNode>) node.types()) {
      if (type.getNodeType() == ASTNode.INTERSECTION_TYPE) {
        walkIntersectionTypes(types, (IntersectionType) type);
      } else {
        types.add((Type) type);
      }
    }
  }

  /** Helper method for {@link MethodDeclaration}s. */
  private void visitFormals(
      Optional<Type> receiverType, SimpleName receiverQualifier,
      List<SingleVariableDeclaration> parameters) {
    if (receiverType.isPresent() || !parameters.isEmpty()) {
      builder.open(ZERO, MAX_LINES_FOR_FORMAL_LIST);
      boolean first = true;
      if (receiverType.isPresent()) {
        // TODO(jdd): Use builders.
        declareOne(
            Direction.HORIZONTAL,
            ImmutableList.<IExtendedModifier>of(),
            receiverType.get(),
            VarArgsOrNot.NO,
            ImmutableList.<Annotation>of(),
            receiverQualifier,
            "",
            ImmutableList.<Dimension>of(),
            "",
            Optional.<Expression>absent());
        first = false;
      }
      for (SingleVariableDeclaration parameter : parameters) {
        if (!first) {
          token(",");
          builder.breakToFill(" ");
        }
        // TODO(jdd): Check for "=".
        visitToDeclare(Direction.HORIZONTAL, parameter, Optional.<Expression>absent(), "=");
        first = false;
      }
      builder.close();
    }
  }

  /** Helper method for {@link MethodDeclaration}s. */
  private void visitThrowsClause(List<Type> thrownExceptionTypes) {
    token("throws");
    builder.breakToFill(" ");
    boolean first = true;
    for (Type thrownExceptionType : thrownExceptionTypes) {
      if (!first) {
        token(",");
        builder.breakToFill(" ");
      }
      thrownExceptionType.accept(this);
      first = false;
    }
  }

  /** Helper method for {@link ImportDeclaration}s, {@link Name}s, and {@link QualifiedName}s. */
  private void visitName(Name node, BreakOrNot breaks) {
    sync(node);
    if (node.isSimpleName()) {
      visit((SimpleName) node);
    } else {
      visitQualifiedName((QualifiedName) node, breaks);
    }
  }

  /**
   * Helper method for {@link EnhancedForStatement}s, {@link MethodDeclaration}s, and
   * {@link SingleVariableDeclaration}s.
   */
  private void visitToDeclare(
      Direction annotationsDirection, SingleVariableDeclaration node,
      Optional<Expression> initializer, String equals) {
    sync(node);
    declareOne(
        annotationsDirection,
        node.modifiers(),
        node.getType(),
        VarArgsOrNot.valueOf(node.isVarargs()),
        node.varargsAnnotations(),
        node.getName(),
        "",
        node.extraDimensions(),
        equals,
        initializer);
  }

  /** Helper method for {@link MethodDeclaration}s and {@link TypeDeclaration}s. */
  private void visitTypeParameters(
      List<TypeParameter> nodes, Indent plusIndent, BreakOrNot breakAfterOpen) {
    if (!nodes.isEmpty()) {
      token("<");
      builder.open(plusIndent);
      builder.open(plusFour);
      if (breakAfterOpen.isYes()) {
        builder.breakOp();
      }
      boolean first = true;
      for (TypeParameter node : nodes) {
        if (!first) {
          token(",");
          builder.breakToFill(" ");
        }
        visit(node);
        first = false;
      }
      builder.close();
      builder.close();
      token(">");
    }
  }

  /** Helper method for {@link UnionType}s. */
  private static void walkUnionTypes(List<Type> types, UnionType node) {
    for (ASTNode type : (List<ASTNode>) node.types()) {
      if (type.getNodeType() == ASTNode.UNION_TYPE) {
        walkUnionTypes(types, (UnionType) type);
      } else {
        types.add((Type) type);
      }
    }
  }

  /** Collapse chains of {@code .} operators, across multiple {@link ASTNode} types. */

  /**
   * Output a "." node.
   * @param node0 the "." node
   */
  void visitDot(Expression node0) {
    Expression node = node0;
    ArrayDeque<Expression> stack = new ArrayDeque<>();
    LOOP:
    do {
      stack.addLast(node);
      switch (node.getNodeType()) {
        case ASTNode.FIELD_ACCESS:
          node = ((FieldAccess) node).getExpression();
          break;
        case ASTNode.METHOD_INVOCATION:
          node = ((MethodInvocation) node).getExpression();
          break;
        default:
          stack.removeLast();
          break LOOP;
      }
    } while (node != null);
    if (stack.isEmpty()) {
      node.accept(this);
      return;
    }
    boolean fillNextBreak = isBaseName(Optional.fromNullable(node));
    boolean needDot = node != null;
    builder.open(plusFour);
    if (node != null) {
      node.accept(this);
    }
    Expression expression = null;
    // TODO(jdd): Stack must be non-empty!
    Optional<BreakTag> optionalTag = Optional.absent();
    while (!stack.isEmpty()) {
      expression = stack.removeLast();
      optionalTag = Optional.fromNullable(stack.isEmpty() ? genSym() : null);
      if (needDot) {
        builder.breakOp(
            fillNextBreak ? Doc.FillMode.INDEPENDENT : Doc.FillMode.UNIFIED, "", ZERO,
            Optional.of(Doc.ProgressiveIndent.make(ZERO, ZERO)), optionalTag);
        token(".");
      }
      dotExpressionUpToArgs(expression);
      if (stack.isEmpty()) {
        break;
      }
      dotExpressionArgsAndParen(expression);
      fillNextBreak = false;
      needDot = true;
    }
    builder.close();
    // TODO(jdd): Is this possible?
    if (expression != null) {
      if (optionalTag.isPresent()) {
        builder.open(Indent.If.make(optionalTag.get(), plusFour, ZERO));
      } else {
        builder.open(ZERO);
      }
      dotExpressionArgsAndParen(expression);
      builder.close();
    }
  }

  private static boolean isBaseName(Optional<Expression> optionalNode) {
    return optionalNode.isPresent()
        && (optionalNode.get().getNodeType() == ASTNode.SIMPLE_NAME
            || optionalNode.get().getNodeType() == ASTNode.QUALIFIED_NAME);
  }

  private void dotExpressionUpToArgs(Expression expression) {
    switch (expression.getNodeType()) {
      case ASTNode.FIELD_ACCESS:
        FieldAccess fieldAccess = (FieldAccess) expression;
        visit(fieldAccess.getName());
        break;
      case ASTNode.METHOD_INVOCATION:
        MethodInvocation methodInvocation = (MethodInvocation) expression;
        if (!methodInvocation.typeArguments().isEmpty()) {
          builder.open(plusFour);
          addTypeArguments(methodInvocation.typeArguments(), ZERO);
          // TODO(jdd): Should indent the name -4.
          builder.breakOp(ZERO);
          builder.close();
        }
        visit(methodInvocation.getName());
        token("(");
        break;
      default:
        throw new IllegalArgumentException("bad expression type");
    }
  }

  private void dotExpressionArgsAndParen(Expression expression) {
    switch (expression.getNodeType()) {
      case ASTNode.FIELD_ACCESS:
        break;
      case ASTNode.METHOD_INVOCATION:
        MethodInvocation methodInvocation = (MethodInvocation) expression;
        addArguments(methodInvocation.arguments(), plusFour);
        token(")");
        break;
      default:
        throw new IllegalArgumentException("bad expression type");
    }
  }

  /** Helper methods for method invocations. */

  void addTypeArguments(List<Type> typeArguments, Indent plusIndent) {
    if (!typeArguments.isEmpty()) {
      token("<");
      builder.open(plusIndent);
      boolean first = true;
      for (Type typeArgument : typeArguments) {
        if (!first) {
          token(",");
          builder.breakToFill(" ");
        }
        typeArgument.accept(this);
        first = false;
      }
      builder.close();
      token(">");
    }
  }

  /**
   * Add arguments to a method invocation, etc. The arguments indented {@code plusFour}, filled,
   * from the current indent. The arguments may be output two at a time if they seem to be arguments
   * to a map constructor, etc.
   * @param arguments the arguments
   * @param plusIndent the extra indent for the arguments
   */
  void addArguments(List<Expression> arguments, Indent plusIndent) {
    if (!arguments.isEmpty()) {
      if (argumentsArePaired(builder.getInput(), arguments)) {
        builder.open(plusIndent);
        builder.forcedBreak();
        builder.open(ZERO);
        boolean first = true;
        for (int i = 0; i < arguments.size() - 1; i += 2) {
          Expression argument0 = arguments.get(i);
          Expression argument1 = arguments.get(i + 1);
          if (!first) {
            token(",");
            builder.forcedBreak();
          }
          builder.open(plusFour);
          argument0.accept(this);
          token(",");
          builder.breakOp(" ");
          argument1.accept(this);
          builder.close();
          first = false;
        }
        builder.close();
        builder.close();
      } else {
        builder.open(plusIndent);
        builder.breakOp();
        builder.open(ZERO, MAX_LINES_FOR_ARGUMENTS);
        boolean first = true;
        for (Expression argument : arguments) {
          if (!first) {
            token(",");
            builder.breakToFill(" ");
          }
          argument.accept(this);
          first = false;
        }
        builder.close();
        builder.close();
      }
    }
  }

  private static boolean argumentsArePaired(Input input, List<Expression> arguments) {
    int n = arguments.size();
    if (n % 2 != 0 || n < 4) {
      return false;
    }
    List<Expression> firsts = new ArrayList<>();
    List<Expression> seconds = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      (i % 2 == 0 ? firsts : seconds).add(arguments.get(i));
    }
    Map<Integer, Integer> positionToColumnMap = input.getPositionToColumnMap();
    Integer firstColumn0 = positionToColumnMap.get(firsts.get(0).getStartPosition());
    if (firstColumn0 == null) {
      return false;
    }
    for (int i = 1; i < n / 2; i++) {
      Integer firstColumnI = positionToColumnMap.get(firsts.get(i).getStartPosition());
      if (!firstColumn0.equals(firstColumnI)) {
        return false;
      }
    }
    for (int i = 0; i < n / 2; i++) {
      Integer secondColumnI = positionToColumnMap.get(seconds.get(i).getStartPosition());
      if (secondColumnI == null) {
        return false;
      }
      if (!(firstColumn0 < secondColumnI)) {
        return false;
      }
    }
    return expressionsAreParallel(firsts, n / 2) && expressionsAreParallel(seconds, n / 4 + 1);
  }

  private static boolean expressionsAreParallel(List<Expression> expressions, int atLeastM) {
    Multimap<Integer, Expression> map = HashMultimap.create();
    for (Expression expression : expressions) {
      map.put(expression.getNodeType(), expression);
    }
    for (Integer nodeType : map.keys()) {
      if (map.get(nodeType).size() >= atLeastM) {
        return true;
      }
    }
    return false;
  }

  /** Visitor method for {@link QualifiedName}s. */
  private void visitQualifiedName(QualifiedName node0, BreakOrNot breaks) {
    QualifiedName node = node0;
    sync(node);
    if (breaks.isYes()) {
      builder.open(plusFour);
    }
    // Collapse chains of "." operators.
    ArrayDeque<SimpleName> stack = new ArrayDeque<>();
    Name qualifier;
    while (true) {
      stack.addLast(node.getName());
      qualifier = node.getQualifier();
      if (qualifier == null || qualifier.getNodeType() != ASTNode.QUALIFIED_NAME) {
        break;
      }
      node = (QualifiedName) qualifier;
    }
    if (qualifier != null) {
      visitName(qualifier, breaks);
      if (breaks.isYes()) {
        builder.breakToFill();
      }
      token(".");
    }
    boolean needDot = false;
    while (!stack.isEmpty()) {
      if (needDot) {
        if (breaks.isYes()) {
          builder.breakToFill();
        }
        token(".");
      }
      visit(stack.removeLast());
      needDot = true;
    }
    if (breaks.isYes()) {
      builder.close();
    }
  }

  // General helper functions.

  // TODO(jdd): Mention annotation declarations.

  /**
   * Declare one variable or variable-like thing.
   * @param annotationsDirection {@link Direction#VERTICAL} or {@link Direction#HORIZONTAL}
   * @param modifiers the {@link IExtendedModifier}s, including annotations
   * @param type the {@link Type}
   * @param isVarargs is the type varargs?
   * @param varargsAnnotations annotations on the varargs
   * @param name the name
   * @param op if non-empty, tokens to follow the name
   * @param extraDimensions the extra dimensions
   * @param equals "=" or equivalent
   * @param initializer the (optional) initializer
   */
  void declareOne(
      Direction annotationsDirection,
      List<IExtendedModifier> modifiers,
      Type type,
      VarArgsOrNot isVarargs,
      List<Annotation> varargsAnnotations,
      SimpleName name,
      String op,
      List<Dimension> extraDimensions,
      String equals,
      Optional<Expression> initializer) {
    builder.open(ZERO);
    visitAndBreakModifiers(modifiers, annotationsDirection);
    builder.open(ZERO);
    builder.open(plusFour);
    type.accept(this);
    if (isVarargs.isYes()) {
      // TODO(jdd): Should these be vertical?
      visitAnnotations(varargsAnnotations, BreakOrNot.YES, BreakOrNot.YES);
      builder.op("...");
    }
    builder.close();
    builder.breakOp(
        Doc.FillMode.INDEPENDENT, " ", ZERO,
        Optional.of(Doc.ProgressiveIndent.make(plusFour, ZERO)));
    visit(name);
    builder.op(op);
    extraDimensions(initializer.isPresent() ? plusEight : plusFour, extraDimensions);
    if (initializer.isPresent()) {
      builder.space();
      token(equals);
      if (initializer.get().getNodeType() == ASTNode.ARRAY_INITIALIZER) {
        builder.close();
        builder.open(ZERO);
        builder.space();
        initializer.get().accept(this);
      } else {
        builder.breakOp(
            Doc.FillMode.INDEPENDENT, " ", ZERO,
            Optional.of(Doc.ProgressiveIndent.make(plusFour, ZERO)));
        initializer.get().accept(this);
      }
    }
    builder.close();
    builder.close();
  }

  /**
   * Declare multiple variables or variable-like things.
   * @param annotationsDirection {@link Direction#VERTICAL} or {@link Direction#HORIZONTAL}
   * @param modifiers the {@link IExtendedModifier}s, including annotations
   * @param type the {@link Type}s
   * @param fragments the {@link VariableDeclarationFragment}s
   */
  private void declareMany(
      Direction annotationsDirection, List<IExtendedModifier> modifiers, Type type,
      List<VariableDeclarationFragment> fragments) {
    builder.open(ZERO);
    visitAndBreakModifiers(modifiers, annotationsDirection);
    builder.open(plusFour);
    type.accept(this);
    // TODO(jdd): Open another time?
    boolean first = true;
    for (VariableDeclarationFragment fragment : fragments) {
      if (!first) {
        token(",");
      }
      builder.breakOp(" ");
      builder.open(ZERO);
      visit(fragment.getName());
      Expression initializer = fragment.getInitializer();
      extraDimensions(initializer != null ? plusEight : plusFour, fragment.extraDimensions());
      if (initializer != null) {
        builder.space();
        token("=");
        if (initializer.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
          // TODO(jdd): Check on this.
          builder.close();
          builder.open(ZERO);
          builder.space();
          initializer.accept(this);
        } else {
          builder.open(plusFour);
          builder.breakOp(" ");
          initializer.accept(this);
          builder.close();
        }
      }
      builder.close();
      first = false;
    }
    builder.close();
    builder.close();
  }

  /**
   * Add a declaration.
   * @param modifiers the {@link IExtendedModifier}s, including annotations
   * @param type the {@link Type}s
   * @param fragments the {@link VariableDeclarationFragment}s
   * @param annotationsDirection {@link Direction#VERTICAL} or {@link Direction#HORIZONTAL}
   */
  void addDeclaration(
      List<IExtendedModifier> modifiers, Type type, List<VariableDeclarationFragment> fragments,
      Direction annotationsDirection) {
    if (fragments.size() == 1) {
      VariableDeclarationFragment fragment = fragments.get(0);
      declareOne(
          annotationsDirection,
          modifiers,
          type,
          VarArgsOrNot.NO,
          ImmutableList.<Annotation>of(),
          fragment.getName(),
          "",
          fragment.extraDimensions(),
          "=",
          Optional.fromNullable(fragment.getInitializer()));
    } else {
      declareMany(annotationsDirection, modifiers, type, fragments);
    }
    token(";");
  }

  // TODO(jdd): State precondition (and check callers).
  /**
   * Emit extra dimensions (if any).
   * @param plusIndent the extra indentation for the extra dimensions
   * @param extraDimensions the extra {@link Dimension}s
   */
  void extraDimensions(Indent plusIndent, List<Dimension> extraDimensions) {
    builder.open(plusIndent);
    for (Dimension extraDimension : extraDimensions) {
      builder.breakToFill(extraDimension.annotations().isEmpty() ? "" : " ");
      visit(extraDimension);
    }
    builder.close();
  }

  // TODO(jdd): Static checks?
  /**
   * Add a list of {@link BodyDeclaration}s
   * @param bodyDeclarations the {@link BodyDeclaration}s
   * @param braces whether to include braces in the output
   * @param first0 is the first {@link BodyDeclaration} the first to be output?
   */
  void addBodyDeclarations(
      List<BodyDeclaration> bodyDeclarations, BracesOrNot braces, FirstDeclarationsOrNot first0) {
    if (bodyDeclarations.isEmpty()) {
      if (braces.isYes()) {
        builder.space();
        tokenBreakTrailingComment("{", plusTwo);
        builder.blankLineWanted(false);
        builder.open(ZERO);
        token("}", plusTwo);
        builder.close();
      }
    } else {
      if (braces.isYes()) {
        builder.space();
        tokenBreakTrailingComment("{", plusTwo);
        builder.blankLineWanted(false);
        builder.open(ZERO);
      }
      builder.open(plusTwo);
      boolean first = first0.isYes();
      boolean lastOneGotBlankLineBefore = false;
      for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
        builder.forcedBreak();
        boolean thisOneGetsBlankLineBefore =
            bodyDeclaration.getNodeType() != ASTNode.FIELD_DECLARATION
                || hasJavaDoc(bodyDeclaration) || hasAnnotations(bodyDeclaration.modifiers());
        if (!first && (thisOneGetsBlankLineBefore || lastOneGotBlankLineBefore)) {
          builder.blankLineWanted(true);
        }
        bodyDeclaration.accept(this);
        first = false;
        lastOneGotBlankLineBefore = thisOneGetsBlankLineBefore;
      }
      builder.close();
      builder.forcedBreak();
      if (braces.isYes()) {
        builder.blankLineWanted(false);
        token("}", plusTwo);
        builder.close();
      }
    }
  }

  /** Does a list of {@link IExtendedModifier}s contain annotations? */
  private static boolean hasAnnotations(List<IExtendedModifier> extendedModifiers) {
    for (IExtendedModifier extendedModifier : extendedModifiers) {
      if (extendedModifier.isAnnotation()) {
        return true;
      }
    }
    return false;
  }

  // Use Eclipse token ID instead of position?
  /** Does this {@link BodyDeclaration} have JavaDoc preceding it? */
  private boolean hasJavaDoc(BodyDeclaration bodyDeclaration) {
    int position = bodyDeclaration.getStartPosition();
    Map.Entry<Integer, ? extends Input.Token> entry =
        builder.getInput().getPositionTokenMap().ceilingEntry(position);
    if (entry != null) {
      for (Input.Tok tok : entry.getValue().getToksBefore()) {
        if (tok.getText().startsWith("/**")) {
          return true;
        }
      }
    }
    return false;
  }

  private static Optional<? extends Input.Token> getNextToken(Input input, int position) {
    Map.Entry<Integer, ? extends Input.Token> ceilingEntry =
        input.getPositionTokenMap().ceilingEntry(position);
    return ceilingEntry == null
        ? Optional.<JavaInput.Token>absent()
        : Optional.of(ceilingEntry.getValue());
  }

  /**
   * Does this list of {@link ASTNode}s ends with the specified token?
   * @param input the {@link Input}
   * @param nodes list of {@link ASTNode}s
   * @return whether the list has an extra trailing comma
   */
  private static boolean hasTrailingToken(Input input, List<ASTNode> nodes, String token) {
    if (nodes.isEmpty()) {
      return false;
    }
    ASTNode lastNode = nodes.get(nodes.size() - 1);
    Optional<? extends Input.Token> nextToken =
        getNextToken(input, lastNode.getStartPosition() + lastNode.getLength());
    return nextToken.isPresent() && nextToken.get().getTok().getText().equals(token);
  }

  // TODO(jdd): Use constants for limits?

  /**
   * Can a local with a set of modifiers be declared with horizontal annotations? This is currently
   * true if there is at most one marker annotation, and no others.
   * @param modifiers the list of {@link IExtendedModifier}s
   * @return whether the local can be declared with horizontal annotations
   */
  private static Direction canLocalHaveHorizontalAnnotations(List<IExtendedModifier> modifiers) {
    int normalAnnotations = 0;
    int markerAnnotations = 0;
    int singleMemberAnnotations = 0;
    for (IExtendedModifier modifier : modifiers) {
      switch (((ASTNode) modifier).getNodeType()) {
        case ASTNode.NORMAL_ANNOTATION:
          ++normalAnnotations;
          break;
        case ASTNode.MARKER_ANNOTATION:
          ++markerAnnotations;
          break;
        case ASTNode.SINGLE_MEMBER_ANNOTATION:
          ++singleMemberAnnotations;
          break;
        default:
          break;
      }
    }
    return normalAnnotations == 0 && markerAnnotations <= 1 && singleMemberAnnotations == 0
        ? Direction.HORIZONTAL
        : Direction.VERTICAL;
  }

  // TODO(jdd): Do more?
  /**
   * Emit a {@link Doc.Token}.
   * @param token the {@link String} to wrap in a {@link Doc.Token}
   */
  final void token(String token) {
    builder.token(token, Doc.Token.RealOrImaginary.REAL, ZERO, Optional.<Indent>absent());
  }

  /**
   * Emit a {@link Doc.Token}.
   * @param token the {@link String} to wrap in a {@link Doc.Token}
   * @param plusIndentCommentsBefore extra indent for comments before this token
   */
  final void token(String token, Indent plusIndentCommentsBefore) {
    builder.token(
        token, Doc.Token.RealOrImaginary.REAL, plusIndentCommentsBefore, Optional.<Indent>absent());
  }

  /**
   * Emit a {@link Doc.Token}, and breaks and indents trailing javadoc or block comments.
   */
  final void tokenBreakTrailingComment(String token, Indent breakAndIndentTrailingComment) {
    builder.token(
        token,
        Doc.Token.RealOrImaginary.REAL,
        ZERO,
        Optional.<Indent>of(breakAndIndentTrailingComment));
  }

  /**
   * Sync to position in the input. If we've skipped outputting any tokens that were present in the
   * input tokens, output them here and complain.
   * @param node the ASTNode holding the input position
   */
  final void sync(ASTNode node) {
    builder.sync(node.getStartPosition(), true);
  }

  final BreakTag genSym() {
    return new BreakTag();
  }

  @Override
  public final String toString() {
    return MoreObjects.toStringHelper(this).add("builder", builder).toString();
  }
}
