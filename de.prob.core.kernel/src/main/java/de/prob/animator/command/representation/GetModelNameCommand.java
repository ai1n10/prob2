package de.prob.animator.command.representation;

import de.prob.animator.command.AbstractCommand;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.PrologTerm;

/**
 * Extracts the name of a loaded model from ProB
 * 
 * @author joy
 * 
 */
public class GetModelNameCommand extends AbstractCommand {

	private static final String NAME = "Name";
	private String name;

	@Override
	public void writeCommand(final IPrologTermOutput pto) {
		pto.openTerm("get_name").printVariable(NAME).closeTerm();

	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {
		name = PrologTerm.atomicString(bindings.get(NAME));
	}

	public String getName() {
		return name;
	}

}
