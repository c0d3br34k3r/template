package com.catascopic.template.x;
//package com.catascopic.template.parse;
//
//import java.io.IOException;
//
//import com.catascopic.template.Scope;
//import com.catascopic.template.Values;
//import com.catascopic.template.eval.Term;
//import com.catascopic.template.eval.Tokenizer;
//import com.catascopic.template.parse.TemplateParser.BlockBuilder;
//
//class TextFileNode implements Node, Tag {
//
//	private final Term fileName;
//
//	private TextFileNode(Term text) {
//		this.fileName = text;
//	}
//
//	@Override
//	public void render(Appendable writer, Scope scope) throws IOException {
//		scope.renderTextFile(writer, Values.toString(fileName.evaluate(scope)));
//	}
//
//	@Override
//	public String toString() {
//		return "<% text " + fileName + " %>";
//	}
//
//	public static Tag parseTag(Tokenizer tokenizer) {
//		return new TextFileNode(tokenizer.parseExpression());
//	}
//
//	@Override
//	public void build(BlockBuilder builder) {
//		builder.add(this);
//	}
//
//}