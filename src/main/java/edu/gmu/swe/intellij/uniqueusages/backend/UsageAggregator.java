package edu.gmu.swe.intellij.uniqueusages.backend;

import com.github.gumtreediff.matchers.Mapping;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class UsageAggregator {

    private final static AstComparator AST_COMPARATOR = new AstComparator();
    private final static double SIMILIAR_THRESHOLD = 0.5;


    private Map<String, UniqueUsageGroup> usageToUniqueUsageGroup = new HashMap<String, UniqueUsageGroup> ();
    private Map<String, UsageInfo> usageToUsageInfo = new HashMap<String, UsageInfo> ();
    private Set<String> codeBlockSet = new HashSet<>();

    private Map<UniqueUsageGroup, CodeBlockUsage> usageInfoToCodeBlockMap = new HashMap<>();

    private SortedMap<CodeBlockUsage, UniqueUsageGroup> codeBlockIntegerSortedMap = new TreeMap<>();

    public UniqueUsageGroup getAggregateUsage(Usage usage) {
        UsageInfo usageInfo = ((UsageInfo2UsageAdapter) usage).getUsageInfo();
        String key = usageInfo.getElement().getContext().getText();

        AstComparator astComparator = new AstComparator();

        PsiElement currentElement = usageInfo.getElement();
        while (currentElement != null && !currentElement.toString().equals("PsiCodeBlock")) {
            currentElement = currentElement.getContext();
        }
        PsiElement codeBlockElement = currentElement; // TODO actually do something with this like AST diffing.

        CodeBlockUsage codeBlockUsage;
        if (codeBlockElement != null) {

            codeBlockUsage = new CodeBlockUsage(codeBlockElement);
            UniqueUsageGroup similarAstKey = null;
            for (Map.Entry<UniqueUsageGroup, CodeBlockUsage> entry : usageInfoToCodeBlockMap.entrySet()) {
                if (entry.getValue().astPercentageSimilarTo(codeBlockUsage) >= SIMILIAR_THRESHOLD) {
                    similarAstKey = entry.getKey();
                    break;
                }
            }
            if (similarAstKey == null) {
                similarAstKey = new UniqueUsageGroup("AST Group");
                usageInfoToCodeBlockMap.put(similarAstKey, codeBlockUsage);
            }
            return similarAstKey;
//
//            comparableCodeBlock = new CodeBlockUsage(codeBlockOfUsage);
//            codeBlockIntegerSortedMap.putIfAbsent(comparableCodeBlock, new UniqueUsageGroup(key));
//            UniqueUsageGroup usageGroup = codeBlockIntegerSortedMap.get(comparableCodeBlock);
//            usageGroup.usageCount++;
//            if (codeBlockSet.contains(comparableCodeBlock.getCode())) {
//                return null;
//            }
//            codeBlockSet.add(comparableCodeBlock.getCode());
//            return codeBlockIntegerSortedMap.get(comparableCodeBlock);
        }
        else {
            return usageToUniqueUsageGroup.get(key);
        }
    }

    static class CodeBlockUsage {
        private PsiElement codeBlock;
        public CodeBlockUsage(PsiElement codeBlock) {
            this.codeBlock = codeBlock;
        }
        public String getCode() {
            return codeBlock.getText();
        }

        public double astPercentageSimilarTo(@NotNull CodeBlockUsage o) {
            String fakeBeginStub = "class Foo { void foo() ";
            String fakeEndStub = "\n}";
            String myFakeAST = fakeBeginStub + this.getCode() + fakeEndStub;
            String otherFakeAST = fakeBeginStub + o.getCode() + fakeEndStub;
            Diff astDiff = AST_COMPARATOR.compare(myFakeAST, otherFakeAST);

            List<Operation> differences = astDiff.getRootOperations();
            Set<Mapping> similiarities = astDiff.getMappingsComp().asSet();

            double percentageSimilar = ((double) similiarities.size()) / (differences.size() + similiarities.size());
            return percentageSimilar;
        }

        @Override
        public String toString() {
            return this.getCode();
        }
    }
}
