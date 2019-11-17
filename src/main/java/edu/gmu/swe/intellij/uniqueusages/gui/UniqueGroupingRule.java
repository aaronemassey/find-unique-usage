package edu.gmu.swe.intellij.uniqueusages.gui;

import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.rules.SingleParentUsageGroupingRule;
import edu.gmu.swe.intellij.uniqueusages.backend.UniqueUsageGroup;
import edu.gmu.swe.intellij.uniqueusages.backend.UsageAggregator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UniqueGroupingRule extends SingleParentUsageGroupingRule {


    UsageAggregator usageAggregator = new UsageAggregator();

    public UniqueGroupingRule() {
        super();
    }

    @Override
    public int getRank() {
        return 0;
    }

    @Nullable
    @Override
    protected UsageGroup getParentGroupFor(@NotNull Usage usage, @NotNull UsageTarget[] targets) {
        // Below comment indicates why we implement this method over the groupUsage method.
//        /**
//         * @deprecated override {@link #getParentGroupFor(Usage, UsageTarget[])} instead
//         */
//        @Deprecated
//        @Override
//        public UsageGroup groupUsage(@NotNull Usage usage) {
//            return getParentGroupFor(usage, UsageTarget.EMPTY_ARRAY);
//        }
        return usageAggregator.getAggregateUsage(usage);
    }
}
