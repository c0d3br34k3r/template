package au.com.codeka.carrot.tmpl;

import java.io.IOException;
import java.io.Reader;

import au.com.codeka.carrot.CarrotException;
import au.com.codeka.carrot.Configuration;
import au.com.codeka.carrot.tmpl.parse.Segment;
import au.com.codeka.carrot.tmpl.parse.Parser;

public class Document {

	public static Document parse(Reader reader, Configuration config) {
		Document root = new Document();
		Parser parser = new Parser(reader);
		parse(parser, root, config);
		return root;
	}

	/**
	 * Parse tokens into the given {@link Node}.
	 * 
	 * @throws IOException
	 * @throws CarrotException
	 */
	private static void parse(Reader parser, Node node, Configuration config)
			throws IOException, CarrotException {
		Node current = node;
		for (;;) {
			Segment token = parser.getNext();
			if (token == null) {
				// Note if there's any open blocks right now, we just assume
				// they end at the end of the file.
				break;
			}

			Node childNode;
			switch (token.getType()) {
				case COMMENT:
					continue;
				case ECHO:
					childNode = TagNode.createEcho(token, config);
					break;
				case TAG:
					TagNode tagNode = TagNode.create(token, config);
					if (tagNode.isEndBlock()) {
						return;
					} else if (current.canChain(tagNode)) {
						/*
						 * If we can chain to the given node, then instead of
						 * adding it as child, we'll chain to it instead.
						 */
						current = current.chain(tagNode);
						childNode = null;
					} else {
						childNode = tagNode;
					}
				case FIXED:
					childNode = FixedNode.create(token);
					break;
				default:
					throw new IllegalStateException("Unknown token type: " + token.getType());
			}
			if (childNode.isBlockNode()) {
				parse(parser, childNode, config);
			}
			current.add(childNode);
		}
	}
	
}
