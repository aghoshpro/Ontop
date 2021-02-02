package it.unibz.inf.ontop.iq.optimizer.impl.lj;

import com.google.common.collect.*;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.UniqueConstraint;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OptimizationSingletons;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultNonRecursiveIQTreeTransformer;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.InjectiveVar2VarSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractJoinTransferLJTransformer extends DefaultNonRecursiveIQTreeTransformer {

    private final Supplier<VariableNullability> variableNullabilitySupplier;
    // LAZY
    private VariableNullability variableNullability;

    protected final VariableGenerator variableGenerator;
    protected final RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor;
    protected final OptimizationSingletons optimizationSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final TermFactory termFactory;
    private final SubstitutionFactory substitutionFactory;

    protected AbstractJoinTransferLJTransformer(Supplier<VariableNullability> variableNullabilitySupplier,
                                                VariableGenerator variableGenerator,
                                                RequiredExtensionalDataNodeExtractor requiredDataNodeExtractor,
                                                OptimizationSingletons optimizationSingletons) {
        this.variableNullabilitySupplier = variableNullabilitySupplier;
        this.variableGenerator = variableGenerator;
        this.requiredDataNodeExtractor = requiredDataNodeExtractor;

        this.optimizationSingletons = optimizationSingletons;
        CoreSingletons coreSingletons = optimizationSingletons.getCoreSingletons();
        this.iqFactory = coreSingletons.getIQFactory();
        this.termFactory = coreSingletons.getTermFactory();
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild) {
        IQTree transformedLeftChild = transform(leftChild);
        // Cannot reuse
        IQTree transformedRightChild = preTransformLJRightChild(rightChild);

        return furtherTransformLeftJoin(rootNode, transformedLeftChild, transformedRightChild)
                .orElseGet(() -> transformedLeftChild.equals(leftChild)
                        && transformedRightChild.equals(rightChild)
                                ? tree
                                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, transformedLeftChild, transformedRightChild))
                                        .normalizeForOptimization(variableGenerator);
    }

    /**
     * Returns empty if no optimization has been applied
     */
    protected Optional<IQTree> furtherTransformLeftJoin(LeftJoinNode rootNode, IQTree leftChild,
                                                        IQTree rightChild) {
        ImmutableSet<ExtensionalDataNode> leftDataNodes = requiredDataNodeExtractor.extractSomeRequiredNodes(leftChild, true)
                .collect(ImmutableCollectors.toSet());

        if (leftDataNodes.isEmpty())
            return Optional.empty();

        ImmutableSet<ExtensionalDataNode> rightDataNodes = extractRightUniqueDataNodes(rightChild);

        if (rightDataNodes.isEmpty())
            return Optional.empty();

        ImmutableSet<SelectedNode> selectedRightDataNodes = selectRightDataNodesToTransfer(
                leftDataNodes, rightDataNodes);

        if (selectedRightDataNodes.isEmpty())
            return Optional.empty();

        return Optional.of(transfer(rootNode, leftChild, rightChild, selectedRightDataNodes)
                .normalizeForOptimization(variableGenerator));
    }

    protected ImmutableSet<SelectedNode> selectRightDataNodesToTransfer(
            ImmutableSet<ExtensionalDataNode> leftDataNodes, ImmutableSet<ExtensionalDataNode> rightDataNodes) {

        ImmutableMultimap<RelationDefinition, ExtensionalDataNode> leftDataNodeMultimap = leftDataNodes.stream()
                .collect(ImmutableCollectors.toMultimap(
                        ExtensionalDataNode::getRelationDefinition,
                        n -> n
                ));

        return rightDataNodes.stream()
                .map(r -> selectForTransfer(r, leftDataNodeMultimap))
                .flatMap(o -> o
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .collect(ImmutableCollectors.toSet());
    }

    protected abstract Optional<SelectedNode> selectForTransfer(ExtensionalDataNode rightDataNode,
                                                                ImmutableMultimap<RelationDefinition, ExtensionalDataNode> leftMultimap);

    /**
     * Does not consider nodes co-occurring multiple times on the right. This allows to guarantee
     * that the position of node in the tree can be found again.
     *   This looks fair as such co-occurrences are likely to be eliminated by other optimizations.
     */
    private ImmutableSet<ExtensionalDataNode> extractRightUniqueDataNodes(IQTree rightChild) {
        ImmutableMultiset<ExtensionalDataNode> multiset = extractRightDataNodes(rightChild)
                .collect(ImmutableCollectors.toMultiset());

        return multiset.entrySet().stream()
                .filter(e -> e.getCount() == 1)
                .map(Multiset.Entry::getElement)
                .collect(ImmutableCollectors.toSet());
    }

    protected synchronized VariableNullability getInheritedVariableNullability() {
        if (variableNullability == null)
        variableNullability = variableNullabilitySupplier.get();

        return variableNullability;
    }


    /**
     * Matches an unique constraint whose determinants are nullable in the tree
     */
    protected Optional<ImmutableList<Integer>> matchUniqueConstraint(UniqueConstraint uniqueConstraint,
                                         ImmutableSet<ExtensionalDataNode> sameRelationLeftNodes,
                                         ImmutableMap<Integer, ? extends VariableOrGroundTerm> rightArgumentMap) {

        ImmutableList<Integer> indexes = uniqueConstraint.getDeterminants().stream()
                .map(a -> a.getIndex() - 1)
                .collect(ImmutableCollectors.toList());

        if (!rightArgumentMap.keySet().containsAll(indexes))
            return Optional.empty();

        VariableNullability variableNullability = getInheritedVariableNullability();
        if (indexes.stream().anyMatch(i ->
                Optional.of(rightArgumentMap.get(i))
                        .filter(t -> (t instanceof Variable) && variableNullability.isPossiblyNullable((Variable) t))
                        .isPresent()))
            return Optional.empty();

        return sameRelationLeftNodes.stream()
                .map(ExtensionalDataNode::getArgumentMap)
                .filter(leftArgumentMap -> leftArgumentMap.keySet().containsAll(indexes)
                                && indexes.stream().allMatch(
                        i -> leftArgumentMap.get(i).equals(rightArgumentMap.get(i))))
                .findAny()
                .map(n -> indexes);
    }


    /**
     * Can be overridden to put restrictions
     */
    protected Stream<ExtensionalDataNode> extractRightDataNodes(IQTree rightChild) {
        return requiredDataNodeExtractor.extractSomeRequiredNodes(rightChild, true);
    }

    private IQTree transfer(LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild,
                            ImmutableSet<SelectedNode> selectedNodes) {
        if (selectedNodes.isEmpty())
            throw new IllegalArgumentException("selectedNodes must not be empty");

        ImmutableSet<DataNodeAndReplacement> nodesToTransferAndReplacements = selectedNodes.stream()
                .map(n -> n.transformForTransfer(variableGenerator, iqFactory))
                .collect(ImmutableCollectors.toSet());

        IQTree newLeftChild = iqFactory.createNaryIQTree(
                iqFactory.createInnerJoinNode(),
                Stream.concat(
                        Stream.of(leftChild),
                        nodesToTransferAndReplacements.stream()
                                .map(n -> n.extensionalDataNode))
                        .collect(ImmutableCollectors.toList()));

        RenamingAndEqualities renamingAndEqualities = RenamingAndEqualities.extract(
                nodesToTransferAndReplacements.stream().map(n -> n.replacement),
                termFactory, substitutionFactory);

        IQTree tmp = replaceSelectedNodesAndRename(selectedNodes, rightChild,
                renamingAndEqualities.renamingSubstitution);

        throw new RuntimeException("TODO: continue");

    }

    /**
     * NB: nodes to be replaced by TrueNodes should be safe to do so (should have been already checked before)
     * In this context, renaming is safe to apply after.
     */
    private IQTree replaceSelectedNodesAndRename(ImmutableSet<SelectedNode> selectedNodes, IQTree rightChild,
                                                 InjectiveVar2VarSubstitution renamingSubstitution) {

        ReplaceNodeByTrueTransformer transformer = new ReplaceNodeByTrueTransformer(
                selectedNodes.stream()
                        .map(n -> n.extensionalDataNode)
                        .collect(ImmutableCollectors.toSet()),
                iqFactory);

        return rightChild.acceptTransformer(transformer)
                .applyFreshRenaming(renamingSubstitution);
    }


    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child) {
        // Recursive
        return transformUnaryNode(tree, rootNode, child, this::transform);
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode rootNode, IQTree child) {
        // Recursive
        return transformUnaryNode(tree, rootNode, child, this::transform);
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode sliceNode, IQTree child) {
        // Recursive
        return transformUnaryNode(tree, sliceNode, child, this::transform);
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child) {
        // Recursive
        return transformUnaryNode(tree, rootNode, child, this::transform);
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children) {
        // Recursive
        return transformNaryCommutativeNode(tree, rootNode, children, this::transform);
    }

    @Override
    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child) {
        return transformUnaryNode(tree, rootNode, child, this::transformBySearchingFromScratch);
    }

    protected IQTree transformUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child,
                                        Function<IQTree, IQTree> childTransformation) {
        IQTree newChild = childTransformation.apply(child);
        return newChild.equals(child)
                ? tree
                : iqFactory.createUnaryIQTree(rootNode, newChild)
                    .normalizeForOptimization(variableGenerator);
    }

    @Override
    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children) {
        return transformNaryCommutativeNode(tree, rootNode, children, this::transformBySearchingFromScratch);
    }

    protected IQTree transformNaryCommutativeNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children,
                                                  Function<IQTree, IQTree> childTransformation) {
        ImmutableList<IQTree> newChildren = children.stream()
                .map(childTransformation)
                .collect(ImmutableCollectors.toList());
        return newChildren.equals(children)
                ? tree
                : iqFactory.createNaryIQTree(rootNode, newChildren)
                    .normalizeForOptimization(variableGenerator);
    }

    @Override
    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild) {
        return transformBinaryNonCommutativeNode(tree, rootNode, leftChild, rightChild,
                this::transformBySearchingFromScratch);
    }

    protected IQTree transformBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                       IQTree leftChild, IQTree rightChild,
                                                       Function<IQTree, IQTree> childTransformation) {
        IQTree newLeftChild = childTransformation.apply(leftChild);
        IQTree newRightChild = childTransformation.apply(rightChild);
        return newLeftChild.equals(leftChild) && newRightChild.equals(rightChild)
                ? tree
                : iqFactory.createBinaryNonCommutativeIQTree(rootNode, newLeftChild, newRightChild)
                    .normalizeForOptimization(variableGenerator);
    }

    protected abstract IQTree transformBySearchingFromScratch(IQTree tree);

    /**
     * Can be overridden
     */
    protected IQTree preTransformLJRightChild(IQTree rightChild) {
        return transformBySearchingFromScratch(rightChild);
    }

    protected static class SelectedNode {

        public final ImmutableList<Integer> determinantIndexes;
        public final ExtensionalDataNode extensionalDataNode;


        public SelectedNode(ImmutableList<Integer> determinantIndexes, ExtensionalDataNode extensionalDataNode) {
            this.determinantIndexes = determinantIndexes;
            this.extensionalDataNode = extensionalDataNode;
        }

        /**
         * The determinants are preserved, while the other arguments are replaced by a fresh variable
         */
        public DataNodeAndReplacement transformForTransfer(VariableGenerator variableGenerator,
                                                           IntermediateQueryFactory iqFactory) {
            ImmutableMap<Integer, ? extends VariableOrGroundTerm> initialArgumentMap = extensionalDataNode.getArgumentMap();
            ImmutableMap<Integer, VariableOrGroundTerm> newArgumentMap = initialArgumentMap.entrySet().stream()
                    .collect(ImmutableCollectors.toMap(
                            Map.Entry::getKey,
                            e -> determinantIndexes.contains(e.getKey())
                                    ? e.getValue()
                                    : variableGenerator.generateNewVariable()
                    ));

            ImmutableMultimap<? extends VariableOrGroundTerm, Variable> replacement = initialArgumentMap.entrySet().stream()
                    .filter(e -> !determinantIndexes.contains(e.getKey()))
                    .filter(e -> !newArgumentMap.get(e.getKey()).equals(e.getValue()))
                    .collect(ImmutableCollectors.toMultimap(
                            Map.Entry::getValue,
                            e -> (Variable) newArgumentMap.get(e.getKey())
                    ));

            return new DataNodeAndReplacement(
                    iqFactory.createExtensionalDataNode(extensionalDataNode.getRelationDefinition(), newArgumentMap),
                    replacement);

        }
    }

    protected static class DataNodeAndReplacement {
        public final ExtensionalDataNode extensionalDataNode;
        // Key: replaced argument, value: the replacing variable
        public final ImmutableMultimap<? extends VariableOrGroundTerm, Variable> replacement;

        public DataNodeAndReplacement(ExtensionalDataNode extensionalDataNode,
                                      ImmutableMultimap<? extends VariableOrGroundTerm, Variable> replacement) {
            this.extensionalDataNode = extensionalDataNode;
            this.replacement = replacement;
        }
    }

    protected static class RenamingAndEqualities {
        public final InjectiveVar2VarSubstitution renamingSubstitution;
        public final ImmutableSet<ImmutableExpression> equalities;

        private RenamingAndEqualities(InjectiveVar2VarSubstitution renamingSubstitution,
                                      ImmutableSet<ImmutableExpression> equalities) {
            this.renamingSubstitution = renamingSubstitution;
            this.equalities = equalities;
        }

        public static RenamingAndEqualities extract(
                Stream<ImmutableMultimap<? extends VariableOrGroundTerm, Variable>> replacementStream,
                TermFactory termFactory, SubstitutionFactory substitutionFactory) {
            ImmutableMap<? extends VariableOrGroundTerm, Collection<Variable>> replacement = replacementStream
                    .flatMap(m -> m.entries().stream())
                    .collect(ImmutableCollectors.toMultimap()).asMap();

            ImmutableMap<Variable, Variable> renamingSubstitutionMap = replacement.entrySet().stream()
                    .filter(e -> e.getKey() instanceof Variable)
                    .collect(ImmutableCollectors.toMap(
                            e -> (Variable) e.getKey(),
                            e -> e.getValue().iterator().next()));

            Stream<ImmutableExpression> varEqualities = replacement.values().stream()
                    .filter(variables -> variables.size() > 1)
                    .map(variables -> termFactory.getStrictEquality(ImmutableList.copyOf(variables)));

            Stream<ImmutableExpression> groundTermEqualities = replacement.entrySet().stream()
                    .filter(e -> e.getKey() instanceof GroundTerm)
                    .map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue().iterator().next()));

            ImmutableSet<ImmutableExpression> equalities = Stream.concat(varEqualities, groundTermEqualities)
                    .collect(ImmutableCollectors.toSet());

            return new RenamingAndEqualities(
                    substitutionFactory.getInjectiveVar2VarSubstitution(renamingSubstitutionMap),
                    equalities);
        }
    }

    protected static class ReplaceNodeByTrueTransformer extends DefaultRecursiveIQTreeVisitingTransformer {

        private final ImmutableSet<ExtensionalDataNode> dataNodesToReplace;

        protected ReplaceNodeByTrueTransformer(ImmutableSet<ExtensionalDataNode> dataNodesToReplace,
                                               IntermediateQueryFactory iqFactory) {
            super(iqFactory);
            this.dataNodesToReplace = dataNodesToReplace;
        }

        @Override
        public IQTree transformExtensionalData(ExtensionalDataNode dataNode) {
            return dataNodesToReplace.contains(dataNode)
                    ? iqFactory.createTrueNode()
                    : dataNode;
        }
    }


}
