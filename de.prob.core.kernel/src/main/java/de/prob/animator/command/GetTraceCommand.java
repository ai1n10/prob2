/**
 * (c) 2009 Lehrstuhl fuer Softwaretechnik und Programmiersprachen, Heinrich
 * Heine Universitaet Duesseldorf This software is licenced under EPL 1.0
 * (http://www.eclipse.org/org/documents/epl-v10.html)
 * */

package de.prob.animator.command;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.prob.parser.BindingGenerator;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

public final class GetTraceCommand extends AbstractCommand {

	private static final String TRACE_VARIABLE = "Trace";
	Logger logger = LoggerFactory.getLogger(GetTraceCommand.class);

	private final static class Occurence {
		private final String text;

		private int count;

		public Occurence(final String text) {
			this.text = text;
			count = 1;
		}

		public synchronized void inc() {
			count++;
		}

		@Override
		public synchronized String toString() {
			return text + ((count > 1) ? " (" + count + " times)" : "");
		}

	}

	private List<String> trace;

	public List<String> getTrace() {
		return trace;
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		List<Occurence> res = new LinkedList<Occurence>();

		ListPrologTerm list = BindingGenerator.getList(bindings
				.get(TRACE_VARIABLE));

		Occurence current = null;
		for (PrologTerm term : list) {
			if (current == null || !current.text.equals(term.toString())) {
				current = new Occurence(term.toString());
				res.add(current);
			} else {
				current.inc();
			}
		}

		final List<String> actions = new ArrayList<String>();
		for (Occurence occurence : res) {
			actions.add(occurence.toString());
		}

		trace = actions;

	}

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm("find_shortest_trace_to_current_state")
				.printVariable(TRACE_VARIABLE).closeTerm();
	}

}
