package au.com.codeka.carrot.expr;

import java.util.List;

import com.google.common.collect.Lists;

import au.com.codeka.carrot.Scope;

class ListTerm implements Term {

	private final List<Term> items;

	ListTerm(List<Term> items) {
		this.items = items;
	}

	@Override
	public Object evaluate(Scope scope) {
		return Lists.transform(items, scope);
	}

	@Override
	public String toString() {
		return items.toString();
	}

}
