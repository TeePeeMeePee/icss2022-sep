package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;


public class Evaluator implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public Evaluator() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        variableValues = new HANLinkedList<>();
        transformStylesheet(ast.root);
    }

    private void transformStylesheet(Stylesheet stylesheet) {
        openScope();
        ArrayList<ASTNode> newBody = new ArrayList<>();
        for (ASTNode node : stylesheet.body) {
            if (node instanceof VariableAssignment) {
                executeAssignment((VariableAssignment) node);
            } else if (node instanceof Stylerule) {
                transformStylerule((Stylerule) node);
                newBody.add(node);
            } else {
                newBody.add(node);
            }
        }
        stylesheet.body = newBody;
        closeScope();
    }

    private void transformStylerule(Stylerule stylerule) {
        openScope();
        stylerule.body = transformBody(stylerule.body);
        closeScope();
    }


    private ArrayList<ASTNode> transformBody(ArrayList<ASTNode> body) {
        ArrayList<ASTNode> newBody = new ArrayList<>();
        for (ASTNode node : body) {
            if (node instanceof Declaration) {
                Declaration declaration = (Declaration) node;
                declaration.expression = evaluate(declaration.expression);
                newBody.add(declaration);
            } else if (node instanceof VariableAssignment) {
                executeAssignment((VariableAssignment) node);
            } else if (node instanceof IfClause) {
                newBody.addAll(transformIfClause((IfClause) node));
            }
        }
        return newBody;
    }

    private ArrayList<ASTNode> transformIfClause(IfClause ifClause) {
        boolean condition = ((BoolLiteral) evaluate(ifClause.conditionalExpression)).value;

        ArrayList<ASTNode> chosenBody;
        if (condition) {
            chosenBody = ifClause.body;
        } else if (ifClause.elseClause != null) {
            chosenBody = ifClause.elseClause.body;
        } else {
            return new ArrayList<>();
        }

        openScope();
        ArrayList<ASTNode> result = transformBody(chosenBody);
        closeScope();
        return result;
    }

    private void executeAssignment(VariableAssignment assignment) {
        assignVariable(assignment.name.name, evaluate(assignment.expression));
    }

    
    private Literal evaluate(Expression expression) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof VariableReference) {
            return findVariableValue(((VariableReference) expression).name);
        } else if (expression instanceof Operation) {
            Operation operation = (Operation) expression;
            return calculate(operation, evaluate(operation.lhs), evaluate(operation.rhs));
        }
        throw new IllegalStateException("Onbekende expressie: " + expression.getNodeLabel());
    }

    private Literal calculate(Operation operation, Literal left, Literal right) {
        if (operation instanceof MultiplyOperation) {
            Literal unitSource = (left instanceof ScalarLiteral) ? right : left;
            return createLiteral(unitSource, getValue(left) * getValue(right));
        } else if (operation instanceof AddOperation) {
            return createLiteral(left, getValue(left) + getValue(right));
        } else if (operation instanceof SubtractOperation) {
            return createLiteral(left, getValue(left) - getValue(right));
        }
        throw new IllegalStateException("Onbekende operatie: " + operation.getNodeLabel());
    }

    private int getValue(Literal literal) {
        if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        } else if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value;
        } else if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        }
        throw new IllegalStateException("Hiermee kan niet gerekend worden: " + literal.getNodeLabel());
    }

    private Literal createLiteral(Literal template, int value) {
        if (template instanceof PixelLiteral) {
            return new PixelLiteral(value);
        } else if (template instanceof PercentageLiteral) {
            return new PercentageLiteral(value);
        }
        return new ScalarLiteral(value);
    }

    // Scopes

    private void openScope() {
        variableValues.addFirst(new HashMap<>());
    }

    private void closeScope() {
        variableValues.removeFirst();
    }

    private Literal findVariableValue(String name) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            Literal value = variableValues.get(i).get(name);
            if (value != null) {
                return value;
            }
        }
        throw new IllegalStateException("Variabele '" + name + "' heeft geen waarde. Is de AST gecontroleerd?");
    }

    private void assignVariable(String name, Literal value) {
        for (int i = 0; i < variableValues.getSize(); i++) {
            if (variableValues.get(i).containsKey(name)) {
                variableValues.get(i).put(name, value);
                return;
            }
        }
        variableValues.getFirst().put(name, value);
    }
}
