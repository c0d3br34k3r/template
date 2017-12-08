package au.com.codeka.carrot.expr;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.EnumSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import au.com.codeka.carrot.CarrotException;

/**
 * Tests for {@link Tokenizer}.
 */
@RunWith(JUnit4.class)
public class TokenizerTest {
	@Test
	public void testIdentifier() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("foo bar baz");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("foo");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("bar");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("baz");
	}

	@Test
	public void testParen() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("(foo) (bar)");
		assertThat(tokenizer.get(TokenType.LEFT_PARENTHESIS)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("foo");
		assertThat(tokenizer.get(TokenType.RIGHT_PARENTHESIS)).isNotNull();
		assertThat(tokenizer.get(TokenType.LEFT_PARENTHESIS)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("bar");
		assertThat(tokenizer.get(TokenType.RIGHT_PARENTHESIS)).isNotNull();
	}

	@Test
	public void testBooleanOperands() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("a && b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.LOGICAL_AND));
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a and b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.LOGICAL_AND));
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a || b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.LOGICAL_OR));
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a or b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.LOGICAL_OR));
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("not a");
		assertThat(tokenizer.get(TokenType.NOT));
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");

		try {
			tokenizer = createTokenizer("a & b");
			assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
			assertThat(tokenizer.get(TokenType.LOGICAL_AND)).isNotNull();
			fail("Expected CarrotException");
		} catch (CarrotException e) {
			assertThat(e.getMessage()).isEqualTo("???\n1: a & b\n       ^\nExpected &&");
		}

		try {
			tokenizer = createTokenizer("a | b");
			assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
			assertThat(tokenizer.get(TokenType.LOGICAL_OR)).isNotNull();
			fail("Expected CarrotException");
		} catch (CarrotException e) {
			assertThat(e.getMessage()).isEqualTo("???\n1: a | b\n       ^\nExpected ||");
		}
	}

	@Test
	public void testEquality() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("a == b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.EQUAL)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a != b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.NOT_EQUAL)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a ! b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.NOT)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a = b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.ASSIGNMENT)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");
	}

	@Test
	public void testGreaterLessThan() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("a < b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.LESS_THAN)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a > b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.GREATER_THAN)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a <= b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.LESS_THAN_OR_EQUAL)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

		tokenizer = createTokenizer("a >= b");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.GREATER_THAN_OR_EQUAL)).isNotNull();
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");

	}

	@Test
	public void testString() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("\"Hello World\" \"This's a test\" 'So is \"this\"'");
		assertThat(tokenizer.get(TokenType.STRING_LITERAL).getValue()).isEqualTo("Hello World");
		assertThat(tokenizer.get(TokenType.STRING_LITERAL).getValue())
				.isEqualTo("This's a test");
		assertThat(tokenizer.get(TokenType.STRING_LITERAL).getValue())
				.isEqualTo("So is \"this\"");
	}

	@Test
	public void testInteger() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("foo 1234 bar");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("foo");
		assertThat(tokenizer.get(TokenType.NUMBER_LITERAL).getValue()).isEqualTo(1234L);
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("bar");
	}

	@Test
	public void testDouble() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("foo 12.34 bar");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("foo");
		assertThat(tokenizer.get(TokenType.NUMBER_LITERAL).getValue()).isEqualTo(12.34);
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("bar");
	}

	@Test
	public void testUnexpectedToken() {
		try {
			Tokenizer tokenizer = createTokenizer("a + + b");
			assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
			assertThat(tokenizer.get(TokenType.PLUS).getType()).isEqualTo(TokenType.PLUS);
			assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");
			fail("Expected CarrotException");
		} catch (CarrotException e) {
			assertThat(e.getMessage()).isEqualTo(
					"???\n1: a + + b\n        ^\nExpected token of type IDENTIFIER, got PLUS");
		}

		try {
			Tokenizer tokenizer = createTokenizer("a + + b");
			assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
			assertThat(tokenizer.get(TokenType.PLUS).getType()).isEqualTo(TokenType.PLUS);
			assertThat(tokenizer.get(EnumSet.of(TokenType.IDENTIFIER, TokenType.NUMBER_LITERAL,
					TokenType.STRING_LITERAL)).getValue()).isEqualTo("b");
			fail("Expected CarrotException");
		} catch (CarrotException e) {
			assertThat(e.getMessage()).isEqualTo(
					"???\n1: a + + b\n        ^\nExpected token of type [STRING_LITERAL, NUMBER_LITERAL, IDENTIFIER], got PLUS");
		}
	}

	@Test
	public void testEndOfStream() throws CarrotException {
		Tokenizer tokenizer = createTokenizer("a =");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("a");
		assertThat(tokenizer.get(TokenType.ASSIGNMENT).getType())
				.isEqualTo(TokenType.ASSIGNMENT);

		tokenizer = createTokenizer("b <");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("b");
		assertThat(tokenizer.get(TokenType.LESS_THAN).getType()).isEqualTo(TokenType.LESS_THAN);

		tokenizer = createTokenizer("c >");
		assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("c");
		assertThat(tokenizer.get(TokenType.GREATER_THAN).getType())
				.isEqualTo(TokenType.GREATER_THAN);

		try {
			tokenizer = createTokenizer("d \"foo");
			assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("d");
			tokenizer.get(TokenType.STRING_LITERAL);
			fail("Expected CarrotException");
		} catch (CarrotException e) {
			assertThat(e.getMessage()).isEqualTo(
					"???\n1: d \"foo\n         ^\nUnexpected end-of-file waiting for \"");
		}
	}

	@Test
	public void testUnexpectedCharacter() {
		try {
			Tokenizer tokenizer = createTokenizer("foo \u263a bar");
			assertThat(tokenizer.get(TokenType.IDENTIFIER).getValue()).isEqualTo("foo");
			tokenizer.get(TokenType.IDENTIFIER);
			fail("Expected CarrotException");
		} catch (CarrotException e) {
			assertThat(e.getMessage())
					.isEqualTo("???\n1: foo \u263a bar\n        ^\nUnexpected character: \u263a");
		}
	}

	@Test
	public void testTokenEqualsToString() {
		Token t1 = Token.of(TokenType.ASSIGNMENT);
		Token t2 = Token.of(TokenType.EQUAL);
		Token t3 = new Token(TokenType.IDENTIFIER, "foo");
		Token t4 = new Token(TokenType.IDENTIFIER, "bar");
		Token t5 = new Token(TokenType.IDENTIFIER, new String("bar"));

		assertThat(t1).isNotEqualTo(t2);
		assertThat(t2).isNotEqualTo(t1);
		assertThat(t3).isNotEqualTo(new Object());
		assertThat(t4).isEqualTo(t5);
		assertThat(t3).isNotEqualTo(t4);
		assertThat(t1).isEqualTo(Token.of(TokenType.ASSIGNMENT));

		assertThat(t1.toString()).isEqualTo("ASSIGNMENT");
		assertThat(t2.toString()).isEqualTo("EQUAL");
		assertThat(t3.toString()).isEqualTo("IDENTIFIER <foo>");
		assertThat(t4.toString()).isEqualTo("IDENTIFIER <bar>");
		assertThat(t5.toString()).isEqualTo("IDENTIFIER <bar>");

		assertThat(t1.hashCode()).isNotEqualTo(t2.hashCode());
		assertThat(t4.hashCode()).isEqualTo(t5.hashCode());
	}

	private static Tokenizer createTokenizer(String str) throws CarrotException {
		return new Tokenizer(new StringReader(str));
	}
}
