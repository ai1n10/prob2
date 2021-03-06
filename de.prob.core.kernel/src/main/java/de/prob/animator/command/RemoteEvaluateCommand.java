package de.prob.animator.command;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import de.prob.animator.domainobjects.EvaluationResult;
import de.prob.parser.ISimplifiedROMap;
import de.prob.prolog.output.IPrologTermOutput;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

/**
 * Calculates the values of a formula. Uses the Prolog Parser, not the Java
 * Parser
 * 
 * @author bendisposto
 * 
 */
public class RemoteEvaluateCommand extends AbstractCommand {

	public enum EEvaluationStrategy {
		EXISTENTIAL("evaluate_formula", true), UNIVERSAL("evaluate_tautology",
				false);
		private final String prolog;
		private final boolean existential;

		EEvaluationStrategy(final String prolog, final boolean existential) {
			this.prolog = prolog;
			this.existential = existential;
		}

		public boolean isExistential() {
			return existential;
		}

	}

	Logger logger = LoggerFactory.getLogger(RemoteEvaluateCommand.class);

	private static final String EVALUATE_TERM_VARIABLE = "Val";
	private final String formula;
	private EvaluationResult value;

	private final EEvaluationStrategy quantifier;

	public RemoteEvaluateCommand(final String formula,
			final EEvaluationStrategy quantifier) {
		this.formula = formula;
		this.quantifier = quantifier;
	}

	public EvaluationResult getValue() {
		return value;
	}

	@Override
	public void processResult(
			final ISimplifiedROMap<String, PrologTerm> bindings) {

		PrologTerm term = bindings.get(EVALUATE_TERM_VARIABLE);

		if (term instanceof ListPrologTerm) {
			ListPrologTerm lpt = (ListPrologTerm) term;
			ArrayList<String> list = new ArrayList<String>();

			String code = lpt.get(0).getFunctor();

			for (int i = 1; i < lpt.size(); i++) {
				list.add(lpt.get(i).getArgument(1).getFunctor());
			}

			value = new EvaluationResult("", code, "", "", Joiner.on(", ")
					.join(list), "error", new ArrayList<String>(), false);
		} else {
			String v = term.getArgument(1).getFunctor();
			String solution = term.getArgument(2).getFunctor();
			String resultType = term.getArgument(3).getFunctor();
			List<String> atomicStrings = ListPrologTerm
					.atomicStrings((ListPrologTerm) term.getArgument(4));
			boolean enumerationWarnings = "true".equals(term.getArgument(5)
					.getFunctor());
			value = new EvaluationResult("", formula, v, solution, "",
					resultType, atomicStrings, enumerationWarnings);
		}

	}

	@Override
	public void writeCommand(final IPrologTermOutput pout) {
		pout.openTerm(quantifier.prolog);
		pout.printString(formula);
		pout.printVariable(EVALUATE_TERM_VARIABLE);
		pout.closeTerm();
	}
}
