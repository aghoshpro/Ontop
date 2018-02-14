package it.unibz.inf.ontop.iq.node.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.datalog.impl.DatalogTools;
import it.unibz.inf.ontop.evaluator.TermNullabilityEvaluator;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.node.impl.ConditionSimplifier.ExpressionAndSubstitution;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.evaluator.ExpressionEvaluator;
import it.unibz.inf.ontop.iq.node.JoinOrFilterNode;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.substitution.impl.ImmutableSubstitutionTools;
import it.unibz.inf.ontop.substitution.impl.ImmutableUnificationTools;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;


public abstract class JoinOrFilterNodeImpl extends CompositeQueryNodeImpl implements JoinOrFilterNode {

    private Optional<ImmutableExpression> optionalFilterCondition;
    private final TermNullabilityEvaluator nullabilityEvaluator;
    protected final TermFactory termFactory;
    protected final TypeFactory typeFactory;
    protected final DatalogTools datalogTools;
    protected final ImmutabilityTools immutabilityTools;
    protected final SubstitutionFactory substitutionFactory;
    protected final ImmutableUnificationTools unificationTools;
    protected final ImmutableSubstitutionTools substitutionTools;
    private final ExpressionEvaluator defaultExpressionEvaluator;

    protected JoinOrFilterNodeImpl(Optional<ImmutableExpression> optionalFilterCondition,
                                   TermNullabilityEvaluator nullabilityEvaluator, TermFactory termFactory,
                                   IntermediateQueryFactory iqFactory, TypeFactory typeFactory, DatalogTools datalogTools,
                                   ImmutabilityTools immutabilityTools, SubstitutionFactory substitutionFactory,
                                   ImmutableUnificationTools unificationTools, ImmutableSubstitutionTools substitutionTools,
                                   ExpressionEvaluator defaultExpressionEvaluator) {
        super(substitutionFactory, iqFactory);
        this.optionalFilterCondition = optionalFilterCondition;
        this.nullabilityEvaluator = nullabilityEvaluator;
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.datalogTools = datalogTools;
        this.immutabilityTools = immutabilityTools;
        this.substitutionFactory = substitutionFactory;
        this.unificationTools = unificationTools;
        this.substitutionTools = substitutionTools;
        this.defaultExpressionEvaluator = defaultExpressionEvaluator;
    }

    @Override
    public Optional<ImmutableExpression> getOptionalFilterCondition() {
        return optionalFilterCondition;
    }

    protected String getOptionalFilterString() {
        if (optionalFilterCondition.isPresent()) {
            return " " + optionalFilterCondition.get().toString();
        }

        return "";
    }

    @Override
    public ImmutableSet<Variable> getLocalVariables() {
        if (optionalFilterCondition.isPresent()) {
            return optionalFilterCondition.get().getVariables();
        }
        else {
            return ImmutableSet.of();
        }
    }


    protected ExpressionEvaluator createExpressionEvaluator() {
        return defaultExpressionEvaluator.clone();
    }

    protected boolean isFilteringNullValue(Variable variable) {
        return getOptionalFilterCondition()
                .filter(e -> nullabilityEvaluator.isFilteringNullValue(e, variable))
                .isPresent();
    }

    protected TermNullabilityEvaluator getNullabilityEvaluator() {
        return nullabilityEvaluator;
    }

    @Override
    public ImmutableSet<Variable> getLocallyRequiredVariables() {
        return getLocalVariables();
    }

    @Override
    public ImmutableSet<Variable> getLocallyDefinedVariables() {
        return ImmutableSet.of();
    }

    protected Optional<ImmutableExpression> computeDownConstraint(Optional<ImmutableExpression> optionalConstraint,
                                                                  ExpressionAndSubstitution conditionSimplificationResults)
            throws UnsatisfiableConditionException {
        if (optionalConstraint.isPresent()) {
            ImmutableExpression substitutedConstraint = conditionSimplificationResults.substitution
                    .applyToBooleanExpression(optionalConstraint.get());

            ImmutableExpression combinedExpression = conditionSimplificationResults.optionalExpression
                    .flatMap(e -> immutabilityTools.foldBooleanExpressions(
                            Stream.concat(
                                    e.flattenAND().stream(),
                                    substitutedConstraint.flattenAND().stream())))
                    .orElse(substitutedConstraint);

            ExpressionEvaluator.EvaluationResult evaluationResults = createExpressionEvaluator()
                    .evaluateExpression(combinedExpression);

            if (evaluationResults.isEffectiveFalse())
                throw new UnsatisfiableConditionException();

            return evaluationResults.getOptionalExpression();
        }
        else
            return conditionSimplificationResults.optionalExpression;
    }

    protected void checkExpression(ImmutableExpression expression, ImmutableList<IQTree> children)
            throws InvalidIntermediateQueryException {

        ImmutableSet<Variable> childrenVariables = children.stream()
                .flatMap(c -> c.getVariables().stream())
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> unboundVariables = expression.getVariableStream()
                .filter(v -> !childrenVariables.contains(v))
                .collect(ImmutableCollectors.toSet());
        if (!unboundVariables.isEmpty()) {
            throw new InvalidIntermediateQueryException("Expression " + expression + " of "
                    + expression + " uses unbound variables (" + unboundVariables +  ").\n" + this);
        }
    }

}
