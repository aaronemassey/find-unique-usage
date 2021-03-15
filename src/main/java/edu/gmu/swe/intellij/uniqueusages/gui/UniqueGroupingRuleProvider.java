package edu.gmu.swe.intellij.uniqueusages.gui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.usages.UsageView;
import com.intellij.usages.UsageViewSettings;
import com.intellij.usages.rules.UsageGroupingRule;
import com.intellij.usages.rules.UsageGroupingRuleProvider;
import org.jetbrains.annotations.NotNull;

public class UniqueGroupingRuleProvider implements UsageGroupingRuleProvider {

    @NotNull
    @Override
    public UsageGroupingRule[] getActiveRules(@NotNull Project project) {
        return new UsageGroupingRule[0];
    }

    @NotNull
    @Override
    public UsageGroupingRule[] getActiveRules(@NotNull Project project, @NotNull UsageViewSettings usageViewSettings) {
        return new UsageGroupingRule[]{new UniqueGroupingRule()};
    }

    @NotNull
    @Override
    public AnAction[] createGroupingActions(@NotNull UsageView view) {
        return new AnAction[0];
    }
}
