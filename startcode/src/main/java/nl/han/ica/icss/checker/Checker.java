package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Checker {

    private static final Map<String, Set<ExpressionType>> ALLOWED_PROPERTY_TYPES = new HashMap<>();

    static {
        ALLOWED_PROPERTY_TYPES.put("color", EnumSet.of(ExpressionType.COLOR));
        ALLOWED_PROPERTY_TYPES.put("background-color", EnumSet.of(ExpressionType.COLOR));
        ALLOWED_PROPERTY_TYPES.put("width", EnumSet.of(ExpressionType.PIXEL, ExpressionType.PERCENTAGE));
        ALLOWED_PROPERTY_TYPES.put("height", EnumSet.of(ExpressionType.PIXEL, ExpressionType.PERCENTAGE));
    }

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        checkStylesheet(ast.root);
    }

    private void checkStylesheet(Stylesheet stylesheet) {
        openScope();
        for (ASTNode node : stylesheet.body) {
            if (node instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) node);
            } else if (node instanceof Stylerule) {
                checkStylerule((Stylerule) node);
            }
        }
        closeScope();
    }

    private void checkStylerule(Stylerule stylerule) {
        openScope();
        checkBody(stylerule.body);
        closeScope();
    }

    private void checkBody(ArrayList<ASTNode> body) {
        for (ASTNode node : body) {
            if (node instanceof Declaration) {
                checkDeclaration((Declaration) node);
            } else if (node instanceof VariableAssignment) {
                checkVariableAssignment((VariableAssignment) node);
            } else if (node instanceof IfClause) {
                checkIfClause((IfClause) node);
            }
        }
    }

    private void checkDeclaration(Declaration declaration) {
        ExpressionType type = getType(declaration.expression);
        String property = declaration.property.name;
        Set<ExpressionType> allowedTypes = ALLOWED_PROPERTY_TYPES.get(property);

        if (allowedTypes == null) {
            declaration.setError("Property '" + property + "' is niet toegestaan. "
                    + "Gebruik color, background-color, width of height.");
        } else if (type != ExpressionType.UNDEFINED && !allowedTypes.contains(type)) {
            declaration.setError("Property '" + property + "' accepteert geen waarde van het type " + type + ".");
        }
    }

    private void checkVariableAssignment(VariableAssignment assignment) {
        ExpressionType type = getType(assignment.expression);
        assignVariable(assignment.name.name, type);
    }

    private void checkIfClause(IfClause ifClause) {
        ExpressionType conditionType = getType(ifClause.conditionalExpression);
        if (conditionType != ExpressionType.BOOL && conditionType != ExpressionType.UNDEFINED) {
            ifClause.setError("De conditie van een if-statement moet een boolean zijn, maar is " + conditionType + ".");
        }

        openScope();
        checkBody(ifClause.body);
        closeScope();

        if (ifClause.elseClause != null) {
            openScope();
            checkBody(ifClause.elseClause.body);
            closeScope();
        }
    }

    private ExpressionType getType(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof VariableReference) {
            return getVariableType((VariableReference) expression);
        } else if (expression instanceof AddOperation || expression instanceof SubtractOperation) {
            return getAddOrSubtractType((Operation) expression);
        } else if (expression instanceof MultiplyOperation) {
            return getMultiplyType((MultiplyOperation) expression);
        }
        return ExpressionType.UNDEFINED;
    }

    private ExpressionType getVariableType(VariableReference reference) {
        ExpressionType type = findVariableType(reference.name);
        if (type == null) {
            reference.setError("Variabele '" + reference.name + "' is niet gedefinieerd of niet in scope.");
            return ExpressionType.UNDEFINED;
        }
        return type;
    }

    private ExpressionType getAddOrSubtractType(Operation operation) {
        ExpressionType left = getType(operation.lhs);
        ExpressionType right = getType(operation.rhs);

        if (left == ExpressionType.UNDEFINED || right == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }
        if (!isNumeric(left) || !isNumeric(right)) {
            reportInvalidOperand(operation, left, right);
            return ExpressionType.UNDEFINED;
        }
        if (left != right) {
            operation.setError("Bij optellen en aftrekken moeten beide waarden van gelijk type zijn, maar het zijn "
                    + left + " en " + right + ".");
            return ExpressionType.UNDEFINED;
        }
        return left;
    }

    private ExpressionType getMultiplyType(MultiplyOperation operation) {
        ExpressionType left = getType(operation.lhs);
        ExpressionType right = getType(operation.rhs);

        if (left == ExpressionType.UNDEFINED || right == ExpressionType.UNDEFINED) {
            return ExpressionType.UNDEFINED;
        }
        if (!isNumeric(left) || !isNumeric(right)) {
            reportInvalidOperand(operation, left, right);
            return ExpressionType.UNDEFINED;
        }
        if (left == ExpressionType.SCALAR) {
            return right;
        }
        if (right == ExpressionType.SCALAR) {
            return left;
        }
        operation.setError("Bij vermenigvuldigen moet minstens een waarde een scalar zijn, maar het zijn "
                + left + " en " + right + ".");
        return ExpressionType.UNDEFINED;
    }

    private void reportInvalidOperand(Operation operation, ExpressionType left, ExpressionType right) {
        if (left == ExpressionType.COLOR || right == ExpressionType.COLOR) {
            operation.setError("Kleuren mogen niet gebruikt worden in berekeningen.");
        } else {
            operation.setError("Alleen pixels, percentages en scalars mogen gebruikt worden in berekeningen, "
                    + "maar het zijn " + left + " en " + right + ".");
        }
    }

    private boolean isNumeric(ExpressionType type) {
        return type == ExpressionType.PIXEL
                || type == ExpressionType.PERCENTAGE
                || type == ExpressionType.SCALAR;
    }

    // Scopes

    private void openScope() {
        variableTypes.addFirst(new HashMap<>());
    }

    private void closeScope() {
        variableTypes.removeFirst();
    }

    private ExpressionType findVariableType(String name) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            ExpressionType type = variableTypes.get(i).get(name);
            if (type != null) {
                return type;
            }
        }
        return null;
    }

    private void assignVariable(String name, ExpressionType type) {
        for (int i = 0; i < variableTypes.getSize(); i++) {
            if (variableTypes.get(i).containsKey(name)) {
                variableTypes.get(i).put(name, type);
                return;
            }
        }
        variableTypes.getFirst().put(name, type);
    }
}
