package de.prob.animator.domainobjects;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.prob.ProBException;
import de.prob.parser.BindingGenerator;
import de.prob.parser.ResultParserException;
import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.IntegerPrologTerm;
import de.prob.prolog.term.ListPrologTerm;
import de.prob.prolog.term.PrologTerm;

public class OpInfo {
	public final String id;
	public final String name;
	public final String src;
	public final String dest;
	public final List<String> params = new ArrayList<String>();

	Logger logger = LoggerFactory.getLogger(OpInfo.class);

	public OpInfo(final String id, final String name, final String src,
			final String dest, final List<String> params) {
		this.id = id;
		this.name = name;
		this.src = src;
		this.dest = dest;
		if (params != null) {
			for (String string : params) {
				this.params.add(string);
			}
		}
	}

	// FIXME: Implement this for OpInfo. Should it be a static method?
	public OpInfo(final CompoundPrologTerm opTerm) throws ProBException {
		// final CompoundPrologTerm opTerm = (CompoundPrologTerm) rawOpTerm;

		String id = null, src = null, dest = null;
		try {
			id = getIdFromPrologTerm(opTerm.getArgument(1));
			src = getIdFromPrologTerm(opTerm.getArgument(3));
			dest = getIdFromPrologTerm(opTerm.getArgument(4));
			ListPrologTerm lpt = BindingGenerator
					.getList(opTerm.getArgument(6));
			for (PrologTerm prologTerm : lpt) {
				params.add(prologTerm.getFunctor());
			}
		} catch (ResultParserException e) {
			logger.error("Result from Prolog was not as expected.", e);
			throw new ProBException();
		}

		this.id = id;
		this.name = PrologTerm.atomicString(opTerm.getArgument(2));
		this.src = src;
		this.dest = dest;
		// final List<PrologTerm> args = (ListPrologTerm)
		// opTerm.getArgument(5);
		// so what is params?
	}

	public static String getIdFromPrologTerm(final PrologTerm destTerm)
			throws ResultParserException {
		if (destTerm instanceof IntegerPrologTerm) {
			return BindingGenerator.getInteger(destTerm).getValue().toString();
		}
		return destTerm.getFunctor();
	}
}
