package nl.han.ica.icss.parser;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

	//Accumulator attributes:
	private AST ast;

	//Use this to keep track of the parent nodes when recursively traversing the ast
	private IHANStack<ASTNode> currentContainer;

	public ASTListener() {
		ast = new AST();
		currentContainer = new HANStack<>();
	}

	public AST getAST() {
		return ast;
	}

	@Override
	public void enterStylesheet(ICSSParser.StylesheetContext ctx) {
		currentContainer.push(new Stylesheet());
	}

	@Override
	public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
		ast.root = (Stylesheet) currentContainer.pop();
	}

	@Override
	public void enterStylerule(ICSSParser.StyleruleContext ctx) {
		currentContainer.push(new Stylerule());
	}

	@Override
	public void exitStylerule(ICSSParser.StyleruleContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterTagSelector(ICSSParser.TagSelectorContext ctx) {
		currentContainer.push(new TagSelector(ctx.getText()));
	}

	@Override
	public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterIdSelector(ICSSParser.IdSelectorContext ctx) {
		currentContainer.push(new IdSelector(ctx.getText()));
	}

	@Override
	public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterClassSelector(ICSSParser.ClassSelectorContext ctx) {
		currentContainer.push(new ClassSelector(ctx.getText()));
	}

	@Override
	public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterDeclaration(ICSSParser.DeclarationContext ctx) {
		currentContainer.push(new Declaration());
	}

	@Override
	public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterPropertyName(ICSSParser.PropertyNameContext ctx) {
		currentContainer.push(new PropertyName(ctx.getText()));
	}

	@Override
	public void exitPropertyName(ICSSParser.PropertyNameContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterColorLiteral(ICSSParser.ColorLiteralContext ctx) {
		currentContainer.push(new ColorLiteral(ctx.getText()));
	}

	@Override
	public void exitColorLiteral(ICSSParser.ColorLiteralContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
		currentContainer.push(new PixelLiteral(ctx.getText()));
	}

	@Override
	public void exitPixelLiteral(ICSSParser.PixelLiteralContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
		currentContainer.push(new PercentageLiteral(ctx.getText()));
	}

	@Override
	public void exitPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterBoolLiteral(ICSSParser.BoolLiteralContext ctx) {
		currentContainer.push(new BoolLiteral(ctx.getText()));
	}

	@Override
	public void exitBoolLiteral(ICSSParser.BoolLiteralContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		currentContainer.push(new VariableAssignment());
	}

	@Override
	public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterVariableReference(ICSSParser.VariableReferenceContext ctx) {
		currentContainer.push(new VariableReference(ctx.getText()));
	}

	@Override
	public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
		currentContainer.push(new ScalarLiteral(ctx.getText()));
	}

	@Override
	public void exitScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
		currentContainer.push(new MultiplyOperation());
	}

	@Override
	public void exitMultiplyOperation(ICSSParser.MultiplyOperationContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterAddSubtractOperation(ICSSParser.AddSubtractOperationContext ctx) {
		if (ctx.PLUS() != null) {
			currentContainer.push(new AddOperation());
		} else {
			currentContainer.push(new SubtractOperation());
		}
	}

	@Override
	public void exitAddSubtractOperation(ICSSParser.AddSubtractOperationContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterIfClause(ICSSParser.IfClauseContext ctx) {
		currentContainer.push(new IfClause());
	}

	@Override
	public void exitIfClause(ICSSParser.IfClauseContext ctx) {
		attachToParent(currentContainer.pop());
	}

	@Override
	public void enterElseClause(ICSSParser.ElseClauseContext ctx) {
		currentContainer.push(new ElseClause());
	}

	@Override
	public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
		attachToParent(currentContainer.pop());
	}

	private void attachToParent(ASTNode child) {
		currentContainer.peek().addChild(child);
	}
}
