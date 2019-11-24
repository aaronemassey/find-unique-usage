package edu.gmu.swe.intellij.uniqueusages.backend;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.jetbrains.annotations.NotNull;
import spoon.reflect.declaration.CtElement;

import java.util.*;

public class UsageAggregator {

    private final static AstComparator AST_COMPARATOR = new AstComparator();

    private Map<String, UniqueUsageGroup> usageToUniqueUsageGroup = new HashMap<String, UniqueUsageGroup> ();
    private Map<String, UsageInfo> usageToUsageInfo = new HashMap<String, UsageInfo> ();
    private Set<String> codeBlockSet = new HashSet<>();

    private SortedMap<ComparableCodeBlock, UniqueUsageGroup> codeBlockIntegerSortedMap = new TreeMap<>();

    public UniqueUsageGroup getAggregateUsage(Usage usage) {
        UsageInfo usageInfo = ((UsageInfo2UsageAdapter) usage).getUsageInfo();
        String key = usageInfo.getElement().getContext().getText();

        AstComparator astComparator = new AstComparator();

        PsiElement currentElement = usageInfo.getElement();
        while (currentElement != null && !currentElement.toString().equals("PsiCodeBlock")) {
            currentElement = currentElement.getContext();
        }
        PsiElement codeBlockOfUsage = currentElement; // TODO actually do something with this like AST diffing.

        ComparableCodeBlock comparableCodeBlock;
        if (codeBlockOfUsage != null) {
            comparableCodeBlock = new ComparableCodeBlock(codeBlockOfUsage);
            codeBlockIntegerSortedMap.putIfAbsent(comparableCodeBlock, new UniqueUsageGroup(key));
            UniqueUsageGroup usageGroup = codeBlockIntegerSortedMap.get(comparableCodeBlock);
            usageGroup.usageCount++;
            if (codeBlockSet.contains(comparableCodeBlock.getCode())) {
                return null;
            }
            codeBlockSet.add(comparableCodeBlock.getCode());
            return codeBlockIntegerSortedMap.get(comparableCodeBlock);
        }
        else {
            return usageToUniqueUsageGroup.get(key);
        }
    }

    class ComparableCodeBlock implements Comparable<ComparableCodeBlock> {
        private PsiElement codeBlock;
        public ComparableCodeBlock(PsiElement codeBlock) {
            this.codeBlock = codeBlock;
        }
        public String getCode() {
            return codeBlock.getText();
        }
        @Override
        public int compareTo(@NotNull ComparableCodeBlock o) {
            String fakeBeginStub = "class Foo { void foo() ";
            String fakeEndStub = "\n}";
            String myFakeAST = fakeBeginStub + this.getCode() + fakeEndStub;
            String otherFakeAST = fakeBeginStub + o.getCode() + fakeEndStub;
            Diff astDiff = AST_COMPARATOR.compare(myFakeAST, otherFakeAST);

            List<Operation> operations = astDiff.getRootOperations();
            double thresholdOfNotEqual = Math.max(myFakeAST.length() / otherFakeAST.length(), otherFakeAST.length() / myFakeAST.length());
            if (operations.size() > thresholdOfNotEqual * 10) {
                return myFakeAST.length() - otherFakeAST.length();
            }
            else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return this.getCode();
        }
    }
}
