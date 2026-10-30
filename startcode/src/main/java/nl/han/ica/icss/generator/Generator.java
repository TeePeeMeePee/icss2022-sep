package nl.han.ica.icss.generator;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;

public class Generator {

    private static final String INDENT = "  ";

    public String generate(AST ast) {
        StringBuilder css = new StringBuilder();
        for (ASTNode node : ast.root.body) {
            if (node instanceof Stylerule) {
                generateStylerule(css, (Stylerule) node, 0);
            }
        }
        return css.toString();
    }

    private void generateStylerule(StringBuilder css, Stylerule stylerule, int level) {
        appendIndent(css, level);
        css.append(joinSelectors(stylerule)).append(" {\n");
        for (ASTNode node : stylerule.body) {
            if (node instanceof Declaration) {
                generateDeclaration(css, (Declaration) node, level + 1);
            }
        }
        appendIndent(css, level);
        css.append("}\n");
    }

    private void generateDeclaration(StringBuilder css, Declaration declaration, int level) {
        appendIndent(css, level);
        css.append(declaration.property.name)
                .append(": ")
                .append(toCssValue(declaration.expression))
                .append(";\n");
    }

    private String joinSelectors(Stylerule stylerule) {
        StringBuilder selectors = new StringBuilder();
        for (Selector selector : stylerule.selectors) {
            if (selectors.length() > 0) {
                selectors.append(", ");
            }
            selectors.append(selector);
        }
        return selectors.toString();
    }

    private String toCssValue(Expression expression) {
        if (expression instanceof PixelLiteral) {
            return ((PixelLiteral) expression).value + "px";
        } else if (expression instanceof PercentageLiteral) {
            return ((PercentageLiteral) expression).value + "%";
        } else if (expression instanceof ColorLiteral) {
            return ((ColorLiteral) expression).value;
        } else if (expression instanceof ScalarLiteral) {
            return String.valueOf(((ScalarLiteral) expression).value);
        }
        throw new IllegalArgumentException("Kan geen CSS maken van: " + expression.getNodeLabel()
                + ". Is de AST eerst getransformeerd?");
    }

    private void appendIndent(StringBuilder css, int level) {
        for (int i = 0; i < level; i++) {
            css.append(INDENT);
        }
    }
}
