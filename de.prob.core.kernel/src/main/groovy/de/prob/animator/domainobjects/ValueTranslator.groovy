package de.prob.animator.domainobjects

import groovy.transform.TupleConstructor

import com.google.common.base.Joiner
import java.util.LinkedHashMap;

import de.prob.prolog.term.CompoundPrologTerm;
import de.prob.prolog.term.IntegerPrologTerm;
import de.prob.prolog.term.ListPrologTerm
import de.prob.prolog.term.PrologTerm

@TupleConstructor class  Tuple {
	def first
	def second
	@Override
	public String toString() {
		return "(${first},${second})";
	}
}

class ValueTranslator {

	def makeSet(node, set) {
		if (node.getFunctor() == "empty") set
		else {
			def value = toGroovy(node.getArgument(1))
			set.add(value)
			def left = makeSet(node.getArgument(4),set)
			makeSet(node.getArgument(5),left)
		}
	}

	def makeSetFromList(ListPrologTerm list) {
		def res = new HashSet()
		for (PrologTerm t : list) {
			res.add(toGroovy(t))
		}
		res
	}

	def makeRecord(ListPrologTerm fields) {
		// field(Name1,Val)
		def res = new LinkedHashMap()
		for (PrologTerm prologTerm : fields) {
			res.put(prologTerm.getArgument(1).functor, toGroovy(prologTerm.getArgument(2)))
		}
		res
	}

	def PrologTerm toProlog(object) {
		if (object instanceof BigInteger) {
			return new CompoundPrologTerm("int", new IntegerPrologTerm(object))
		}
		if (object instanceof Tuple) {
			return new CompoundPrologTerm("','", toProlog(object.first), toProlog(object.second))
		}
		if (object instanceof Boolean) {
			return object ? new CompoundPrologTerm("pred_true") : new CompoundPrologTerm("pred_false")
		}
		if (object instanceof HashSet) {
			return new ListPrologTerm( object.collect { toProlog(it) }.toArray(new PrologTerm[object.size()]));
		}
		if (object instanceof LinkedHashMap) {
			return new CompoundPrologTerm('rec', 
				new ListPrologTerm(object.collect { new CompoundPrologTerm("field", it.key,toProlog(it.value)) }.toArray(new PrologTerm[object.size()])));
		}
		if (object instanceof ArrayList) {
			return new CompoundPrologTerm("fd",new CompoundPrologTerm(object[1]),new IntegerPrologTerm(object[0]));
		}
		if (object instanceof String) {
			return new CompoundPrologTerm("string", new CompoundPrologTerm(object));
		}
	}

	def toGroovy(PrologTerm term) {
		if (term instanceof ListPrologTerm) {
			return makeSetFromList(term);
		}
		def termFunctor = term.functor
		switch (termFunctor) {
			case "int": term.getArgument(1).getValue()
			break
			case "pred_true": true
			break
			case "pred_false": false
			break
			case "avl_set": makeSet(term.getArgument(1),new HashSet())
			break
			case ",": new Tuple(toGroovy(term.getArgument(1)), toGroovy(term.getArgument(2)))
			break
			case "fd":  [term.getArgument(2).functor,term.getArgument(1)]
			break
			case "string":  term.getArgument(1).functor
			break
			case "rec": makeRecord(term.getArgument(1))
			break
			default: throw new IllegalArgumentException("Don't know how to translate "+term)
		}
	}



	def String asString(groovy_state) {
		switch (groovy_state) {
			case HashSet: "{"+ Joiner.on(",").join(groovy_state.collect {asString(it)}) +"}"
			break
			case LinkedHashMap: "rec("+Joiner.on(",").join(groovy_state.collect {it.key+":"+asString(it.value)})+")"
			break
			case ArrayList:  asString(groovy_state.get(0))+asString(groovy_state.get(1))
			break
			default: groovy_state.toString();
		}
	}


}
