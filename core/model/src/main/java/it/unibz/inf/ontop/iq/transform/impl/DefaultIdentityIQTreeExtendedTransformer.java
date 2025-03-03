package it.unibz.inf.ontop.iq.transform.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.IQTreeExtendedTransformer;

/**
 * To be extended by overloading the methods of interest.
 */
public class DefaultIdentityIQTreeExtendedTransformer<T> implements IQTreeExtendedTransformer<T> {

    @Override
    public IQTree transformIntensionalData(IntensionalDataNode dataNode, T context) {
        return dataNode;
    }

    @Override
    public IQTree transformExtensionalData(ExtensionalDataNode dataNode, T context) {
        return dataNode;
    }

    @Override
    public IQTree transformEmpty(EmptyNode node, T context) {
        return node;
    }

    @Override
    public IQTree transformTrue(TrueNode node, T context) {
        return node;
    }

    @Override
    public IQTree transformValues(ValuesNode node, T context) {
        return node;
    }

    @Override
    public IQTree transformNonStandardLeafNode(LeafIQTree leafNode, T context) {
        return leafNode;
    }

    @Override
    public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child,
                                        T context) {
        return tree;
    }

    @Override
    public IQTree transformAggregation(IQTree tree, AggregationNode aggregationNode, IQTree child,
                                       T context) {
        return tree;
    }

    @Override
    public IQTree transformFilter(IQTree tree, FilterNode rootNode, IQTree child, T context) {
        return tree;
    }

    @Override
    public IQTree transformFlatten(IQTree tree, FlattenNode rootNode, IQTree child, T context) {
        return tree;
    }

    @Override
    public IQTree transformDistinct(IQTree tree, DistinctNode distinctNode, IQTree child, T context) {
        return tree;
    }

    @Override
    public IQTree transformSlice(IQTree tree, SliceNode rootNode, IQTree child, T context) {
        return tree;
    }

    @Override
    public IQTree transformOrderBy(IQTree tree, OrderByNode rootNode, IQTree child, T context) {
        return tree;
    }

    @Override
    public IQTree transformNonStandardUnaryNode(IQTree tree, UnaryOperatorNode rootNode, IQTree child, T context) {
        return tree;
    }

    @Override
    public IQTree transformLeftJoin(IQTree tree, LeftJoinNode rootNode, IQTree leftChild, IQTree rightChild, T context) {
        return tree;
    }

    @Override
    public IQTree transformNonStandardBinaryNonCommutativeNode(IQTree tree, BinaryNonCommutativeOperatorNode rootNode,
                                                               IQTree leftChild, IQTree rightChild, T context) {
        return tree;
    }

    @Override
    public IQTree transformInnerJoin(IQTree tree, InnerJoinNode rootNode, ImmutableList<IQTree> children, T context) {
        return tree;
    }

    @Override
    public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children, T context) {
        return tree;
    }

    @Override
    public IQTree transformNonStandardNaryNode(IQTree tree, NaryOperatorNode rootNode, ImmutableList<IQTree> children, T context) {
        return tree;
    }
}
