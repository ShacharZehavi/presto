/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner;

import com.facebook.presto.sql.planner.plan.AggregationNode;
import com.facebook.presto.sql.planner.plan.DistinctLimitNode;
import com.facebook.presto.sql.planner.plan.ExchangeNode;
import com.facebook.presto.sql.planner.plan.FilterNode;
import com.facebook.presto.sql.planner.plan.JoinNode;
import com.facebook.presto.sql.planner.plan.LimitNode;
import com.facebook.presto.sql.planner.plan.MarkDistinctNode;
import com.facebook.presto.sql.planner.plan.MaterializedViewWriterNode;
import com.facebook.presto.sql.planner.plan.OutputNode;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.sql.planner.plan.PlanVisitor;
import com.facebook.presto.sql.planner.plan.ProjectNode;
import com.facebook.presto.sql.planner.plan.SampleNode;
import com.facebook.presto.sql.planner.plan.SemiJoinNode;
import com.facebook.presto.sql.planner.plan.SinkNode;
import com.facebook.presto.sql.planner.plan.SortNode;
import com.facebook.presto.sql.planner.plan.TableCommitNode;
import com.facebook.presto.sql.planner.plan.TableScanNode;
import com.facebook.presto.sql.planner.plan.TableWriterNode;
import com.facebook.presto.sql.planner.plan.TopNNode;
import com.facebook.presto.sql.planner.plan.UnionNode;
import com.facebook.presto.sql.planner.plan.WindowNode;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Computes all symbols declared by a logical plan
 */
public final class SymbolExtractor
{
    private SymbolExtractor() {}

    public static Set<Symbol> extract(PlanNode node)
    {
        ImmutableSet.Builder<Symbol> builder = ImmutableSet.builder();

        node.accept(new Visitor(builder), null);

        return builder.build();
    }

    private static class Visitor
            extends PlanVisitor<Void, Void>
    {
        private final ImmutableSet.Builder<Symbol> builder;

        public Visitor(ImmutableSet.Builder<Symbol> builder)
        {
            this.builder = builder;
        }

        @Override
        public Void visitExchange(ExchangeNode node, Void context)
        {
            builder.addAll(node.getOutputSymbols());

            return null;
        }

        @Override
        public Void visitAggregation(AggregationNode node, Void context)
        {
            // visit child
            node.getSource().accept(this, context);

            builder.addAll(node.getAggregations().keySet());

            return null;
        }

        @Override
        public Void visitMarkDistinct(MarkDistinctNode node, Void context)
        {
            node.getSource().accept(this, context);

            builder.add(node.getMarkerSymbol());

            return null;
        }

        @Override
        public Void visitWindow(WindowNode node, Void context)
        {
            // visit child
            node.getSource().accept(this, context);

            builder.addAll(node.getWindowFunctions().keySet());

            return null;
        }

        @Override
        public Void visitFilter(FilterNode node, Void context)
        {
            // visit child
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitProject(ProjectNode node, Void context)
        {
            // visit child
            node.getSource().accept(this, context);

            builder.addAll(node.getOutputSymbols());

            return null;
        }

        @Override
        public Void visitTopN(TopNNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitSort(SortNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitOutput(OutputNode node, Void context)
        {
            node.getSource().accept(this, context);
            builder.addAll(node.getOutputSymbols());
            return null;
        }

        @Override
        public Void visitLimit(LimitNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitDistinctLimit(DistinctLimitNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitSample(SampleNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitTableScan(TableScanNode node, Void context)
        {
            builder.addAll(node.getAssignments().keySet());

            return null;
        }

        @Override
        public Void visitTableWriter(TableWriterNode node, Void context)
        {
            node.getSource().accept(this, context);

            builder.addAll(node.getOutputSymbols());

            return null;
        }

        @Override
        public Void visitTableCommit(TableCommitNode node, Void context)
        {
            node.getSource().accept(this, context);

            builder.addAll(node.getOutputSymbols());

            return null;
        }

        @Override
        public Void visitMaterializedViewWriter(MaterializedViewWriterNode node, Void context)
        {
            node.getSource().accept(this, context);

            builder.addAll(node.getOutputSymbols());

            return null;
        }

        @Override
        public Void visitJoin(JoinNode node, Void context)
        {
            node.getLeft().accept(this, context);
            node.getRight().accept(this, context);

            return null;
        }

        @Override
        public Void visitSemiJoin(SemiJoinNode node, Void context)
        {
            builder.add(node.getSemiJoinOutput());

            node.getSource().accept(this, context);
            node.getFilteringSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitSink(SinkNode node, Void context)
        {
            node.getSource().accept(this, context);

            return null;
        }

        @Override
        public Void visitUnion(UnionNode node, Void context)
        {
            for (PlanNode subPlanNode : node.getSources()) {
                subPlanNode.accept(this, context);
            }

            return null;
        }

        @Override
        protected Void visitPlan(PlanNode node, Void context)
        {
            throw new UnsupportedOperationException("not yet implemented: " + node.getClass().getName());
        }
    }
}
