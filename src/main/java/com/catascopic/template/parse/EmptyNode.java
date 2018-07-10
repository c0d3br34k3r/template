package com.catascopic.template.parse;

import java.io.IOException;

import com.catascopic.template.Scope;

public enum EmptyNode implements Node {
	INSTANCE;

	@Override
	public void render(Appendable writer, Scope scope) throws IOException {}

}