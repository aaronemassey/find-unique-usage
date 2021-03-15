package edu.gmu.swe.intellij.uniqueusages.backend;

import com.intellij.openapi.externalSystem.service.execution.NotSupportedException;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class UniqueUsageGroup implements UsageGroup {
    String usageDisplayed;
    int count = 1;

    public UniqueUsageGroup() {
        super();
        throw new NotSupportedException("Must call parameterized ctor.");
    }

    public UniqueUsageGroup(String usageDisplayed) {
        this.usageDisplayed = usageDisplayed;
    }

    void incrementUsageCount() {
        count++;
    }


    @Nullable
    @Override
    public Icon getIcon(boolean isOpen) {
        return null;
    }

    @NotNull
    @Override
    public String getText(@Nullable UsageView view) {
//        return String.valueOf(count);
        return usageDisplayed;
    }

    @Nullable
    @Override
    public FileStatus getFileStatus() {
        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void update() {

    }

    @Override
    public void navigate(boolean requestFocus) {

    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public int compareTo(@NotNull UsageGroup o) {
        if (o instanceof UniqueUsageGroup) {
            return this.count - ((UniqueUsageGroup) o).count;
        }
        return 0;
    }
}
