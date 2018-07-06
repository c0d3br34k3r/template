package com.catascopic.template.expr;

import static com.catascopic.template.expr.Symbol.COLON;
import static com.catascopic.template.expr.Symbol.COMMA;
import static com.catascopic.template.expr.Symbol.DOT;
import static com.catascopic.template.expr.Symbol.LEFT_BRACKET;
import static com.catascopic.template.expr.Symbol.LEFT_PARENTHESIS;
import static com.catascopic.template.expr.Symbol.RIGHT_BRACKET;
import static com.catascopic.template.expr.Symbol.RIGHT_CURLY_BRACKET;
import static com.catascopic.template.expr.Symbol.RIGHT_PARENTHESIS;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.catascopic.template.TemplateParseException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

class ValueParser implements TermParser {

	@Override
	public Term parse(Tokenizer tokenizer) {
		Token token = tokenizer.next();
		Term term;
		switch (token.type()) {
		case VALUE:
			term = new ValueTerm(token.value());
			break;
		case IDENTIFIER:
			term = parseIdentifier(tokenizer, token.identifier());
			break;
		case SYMBOL:
			term = parseSymbol(token.symbol(), tokenizer);
			break;
		default:
			throw new TemplateParseException("unexpected token %s", token);
		}
		for (;;) {
			if (tokenizer.tryConsume(DOT)) {
				term = new IndexTerm(term,
						new ValueTerm(tokenizer.parseIdentifier()));
			} else if (tokenizer.tryConsume(LEFT_BRACKET)) {
				term = parseIndex(tokenizer, term);
			} else {
				return term;
			}
		}
	}

	private Term parseSymbol(Symbol symbol, Tokenizer tokenizer) {
		switch (symbol) {
		case PLUS:
		case MINUS:
		case NOT:
			return new UnaryTerm(symbol.unaryOperator(), parse(tokenizer));
		case LEFT_PARENTHESIS:
			Term term = ExpressionParser.parse(tokenizer);
			tokenizer.consume(RIGHT_PARENTHESIS);
			return term;
		case LEFT_BRACKET:
			return new ListTerm(parseExpressions(tokenizer, RIGHT_BRACKET));
		case LEFT_CURLY_BRACKET:
			return new MapTerm(parseMap(tokenizer));
		default:
			throw new TemplateParseException("unexpected symbol %s", symbol);
		}
	}

	private static Term parseIdentifier(Tokenizer tokenizer,
			String identifier) {
		if (tokenizer.tryConsume(LEFT_PARENTHESIS)) {
			return new FunctionTerm(identifier,
					parseExpressions(tokenizer, RIGHT_PARENTHESIS));
		}
		return new Variable(identifier);
	}

	private static Term parseIndex(Tokenizer tokenizer, Term seq) {
		Term index;
		if (tokenizer.tryConsume(COLON)) {
			index = null;
		} else {
			index = tokenizer.parseExpression();
			if (tokenizer.tryConsume(RIGHT_BRACKET)) {
				// [i]
				return new IndexTerm(seq, index);
			}
			tokenizer.consume(COLON);
		}
		if (tokenizer.tryConsume(RIGHT_BRACKET)) {
			// [:], [i:]
			return new SliceTerm(seq, index, null, null);
		}
		Term stop;
		if (tokenizer.tryConsume(COLON)) {
			stop = null;
		} else {
			stop = tokenizer.parseExpression();
			if (tokenizer.tryConsume(RIGHT_BRACKET)) {
				// [i:j], [:j]
				return new SliceTerm(seq, index, stop, null);
			}
			tokenizer.consume(COLON);
		}
		if (tokenizer.tryConsume(RIGHT_BRACKET)) {
			// [i:j:], [i::], [::], [:j:]
			// TODO: allow this syntax?
			return new SliceTerm(seq, index, stop, null);
		}
		Term step = tokenizer.parseExpression();
		tokenizer.consume(RIGHT_BRACKET);
		// [i:j:k], [i::k], [:j:k], [::k]
		return new SliceTerm(seq, index, stop, step);
	}

	private static List<Term> parseExpressions(Tokenizer tokenizer,
			Symbol end) {
		if (tokenizer.tryConsume(end)) {
			return Collections.emptyList();
		}
		ImmutableList.Builder<Term> terms = ImmutableList.builder();
		do {
			terms.add(tokenizer.parseExpression());
		} while (tokenizer.tryConsume(COMMA));
		tokenizer.consume(end);
		return terms.build();
	}

	private static Map<String, Term> parseMap(Tokenizer tokenizer) {
		if (tokenizer.tryConsume(RIGHT_CURLY_BRACKET)) {
			return Collections.emptyMap();
		}
		ImmutableMap.Builder<String, Term> terms = ImmutableMap.builder();
		do {
			String key = parseKey(tokenizer);
			tokenizer.consume(COLON);
			Term value = tokenizer.parseExpression();
			terms.put(key, value);
		} while (tokenizer.tryConsume(COMMA));
		tokenizer.consume(RIGHT_CURLY_BRACKET);
		return terms.build();
	}

	private static String parseKey(Tokenizer tokenizer) {
		Token token = tokenizer.next();
		switch (token.type()) {
		case IDENTIFIER:
			return token.identifier();
		case VALUE:
			if (token.value() instanceof String) {
				return (String) token.value();
			}
			// fallthrough
		default:
			throw new TemplateParseException(
					"expected string literal or identifier, got %s", token);
		}
	}

}
