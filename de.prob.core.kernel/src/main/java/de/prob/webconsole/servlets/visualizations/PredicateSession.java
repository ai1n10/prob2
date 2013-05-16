package de.prob.webconsole.servlets.visualizations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import de.prob.animator.command.ExpandFormulaCommand;
import de.prob.animator.command.InsertFormulaForVisualizationCommand;
import de.prob.animator.domainobjects.ExpandedFormula;
import de.prob.animator.domainobjects.FormulaId;
import de.prob.animator.domainobjects.IEvalElement;
import de.prob.statespace.AnimationSelector;
import de.prob.statespace.History;
import de.prob.statespace.IHistoryChangeListener;
import de.prob.statespace.StateSpace;
import de.prob.visualization.Transformer;

public class PredicateSession implements ISessionServlet,
		IHistoryChangeListener, IVisualizationServlet {

	private final IEvalElement formula;
	private final StateSpace stateSpace;
	private History currentHistory;
	private final FormulaId formulaId;
	private ExpandedFormula expanded;
	private final List<Transformer> styling = new ArrayList<Transformer>();
	private int count = 0;

	public PredicateSession(final AnimationSelector animations,
			final IEvalElement formula) {
		currentHistory = animations.getCurrentHistory();
		stateSpace = currentHistory.getStatespace();
		this.formula = formula;
		InsertFormulaForVisualizationCommand cmd = new InsertFormulaForVisualizationCommand(
				formula);
		stateSpace.execute(cmd);
		formulaId = cmd.getFormulaId();
		calculate();
		animations.registerHistoryChangeListener(this);
	}

	@Override
	public void doGet(final HttpServletRequest req,
			final HttpServletResponse resp) throws IOException {
		PrintWriter out = resp.getWriter();
		Map<String, Object> response = new HashMap<String, Object>();

		boolean getFormula = Boolean.valueOf(req.getParameter("getFormula"));

		if (getFormula) {
			response.put("data", expanded);
		}
		response.put("count", count);
		response.put("attrs", styling);

		Gson g = new Gson();

		String json = g.toJson(response);
		out.println(json);
		out.close();
	}

	@Override
	public void historyChange(final History history) {
		if (currentHistory != history) {
			currentHistory = history;
			calculate();
		}
	}

	public void calculate() {
		if (currentHistory != null && currentHistory.getS() == stateSpace) {
			ExpandFormulaCommand cmd = new ExpandFormulaCommand(formulaId,
					currentHistory.getCurrentState().getId());
			stateSpace.execute(cmd);
			expanded = cmd.getResult();
			count++;
		}
	}

	@Override
	public void apply(final Transformer styling) {
		this.styling.add(styling);
		count++;
	}

	public IEvalElement getFormula() {
		return formula;
	}
}