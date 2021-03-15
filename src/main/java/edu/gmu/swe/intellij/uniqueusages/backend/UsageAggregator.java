package edu.gmu.swe.intellij.uniqueusages.backend;

import com.github.gumtreediff.matchers.Mapping;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageView;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spoon.Launcher;
import spoon.reflect.declaration.CtClass;

import javax.swing.*;
import java.util.*;
import java.util.stream.Stream;

public class UsageAggregator {

    private final static AstComparator AST_COMPARATOR = new AstComparator();
    private final static double SIMILIAR_THRESHOLD = 0.12; // lower means combine more



    private Map<String, UniqueUsageGroup> usageToUniqueUsageGroup = new HashMap<String, UniqueUsageGroup> ();
    private Map<String, UsageInfo> usageToUsageInfo = new HashMap<String, UsageInfo> ();
    private Set<String> codeBlockSet = new HashSet<>();

    private Map<UniqueUsageGroup, CodeBlockUsage> usageInfoToCodeBlockMap = new HashMap<>();

    private SortedMap<CodeBlockUsage, UniqueUsageGroup> codeBlockIntegerSortedMap = new TreeMap<>();

    private List<AstSimilarityNode> astSimilarityList = new LinkedList<>();

    private static class AstSimilarityNode {
        private CodeBlockUsage representativeElement;
        private UniqueUsageGroup group;

        private Set<CodeBlockUsage> elements = new HashSet<>();

        public AstSimilarityNode(CodeBlockUsage representativeElement, UniqueUsageGroup group) {
            this.representativeElement = representativeElement;
            this.group = group;
            elements.add(representativeElement);
        }

        public double getSimilarityTo(CodeBlockUsage o) {
//            return this.representativeElement.compareSimilarity(o);

            Stream<Double> similarityStream =
                    elements
                            .parallelStream().map(elem -> elem.compareSimilarity(o));

            Optional<Double> lowestSimilarity = similarityStream.min(Double::compareTo);

            if (!lowestSimilarity.isPresent()) {
                throw new RuntimeException("This should never happen");
            }
            return lowestSimilarity.get();
        }

        public CodeBlockUsage getRepresentativeElement() {
            return representativeElement;
        }

        public UniqueUsageGroup getGroup() {
            return group;
        }

    }



    public synchronized UsageGroup getAggregateUsage(Usage usage) {
        UsageInfo usageInfo = ((UsageInfo2UsageAdapter) usage).getUsageInfo();

        PsiElement currentElement = usageInfo.getElement();
        while (currentElement != null && !currentElement.toString().contains("PsiMethod:")) {
            currentElement = currentElement.getContext();
        }
        PsiElement codeBlockElement = currentElement;

        CodeBlockUsage codeBlockUsage;
        if (codeBlockElement != null) {

            if (codeBlockSet.contains(codeBlockElement.getText())) {
                return null;
            }
            codeBlockSet.add(codeBlockElement.getText());

            String codeBlockElementClazzName = codeBlockElement.getContext().toString().replaceFirst(".*:", "");
            codeBlockUsage = new CodeBlockUsage(codeBlockElement, codeBlockElementClazzName);

            Optional<AstSimilarityNode> mostSimilarAstKey = Optional.empty();

            double highestSimilarityRating = 0.0;

            mostSimilarAstKey = astSimilarityList
                    .parallelStream()
                    .max(
                            (a, b) -> (int) (1000 * (a.getSimilarityTo(codeBlockUsage) - b.getSimilarityTo(codeBlockUsage)))
                    );


//            for (AstSimilarityNode astSimilarityNode: astSimilarityList) {
//                double maxSimilarityTo = astSimilarityNode.getSimilarityTo(codeBlockUsage);
//                if (maxSimilarityTo > highestSimilarityRating) {
//                    mostSimilarAstKey = Optional.of(astSimilarityNode);
//                    highestSimilarityRating = maxSimilarityTo;
//                }
//            }

            if (mostSimilarAstKey.isPresent()) {
                highestSimilarityRating = mostSimilarAstKey.get().getSimilarityTo(codeBlockUsage);
            }

            if (highestSimilarityRating > SIMILIAR_THRESHOLD) {
                System.out.println("Aggregation occurred");
                System.out.println(highestSimilarityRating);
                // Check if returning exact match because classic find usages is weird.
                if (highestSimilarityRating >= 1.0) {
                    throw new RuntimeException("This should never happen!");
                }
                mostSimilarAstKey.get().elements.add(codeBlockUsage);
                mostSimilarAstKey.get().getGroup().incrementUsageCount();
                return mostSimilarAstKey.get().getGroup();
            }
            else {
                System.out.println("new group occurred");
                System.out.println(highestSimilarityRating);
                // Create and return a codeblock usage key
                UniqueUsageGroup newAstKey = new UniqueUsageGroup("Similar Usage Group");
                astSimilarityList.add(new AstSimilarityNode(codeBlockUsage, newAstKey));
                return newAstKey;
            }
        }
        else {
            return null;
//            return usageToUniqueUsageGroup.get(key);
        }
    }

    static class _InvalidUsageGroup implements UsageGroup {
        @Nullable
        @Override
        public Icon getIcon(boolean isOpen) {
            return null;
        }

        @NotNull
        @Override
        public String getText(@Nullable UsageView view) {
            return null;
        }

        @Nullable
        @Override
        public FileStatus getFileStatus() {
            return null;
        }

        @Override
        public boolean isValid() {
            return false;
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
            return 0;
        }
    }

    static class CodeBlockUsage {
        private PsiElement codeBlock;
        private CtClass<?> ctClass;

        public CodeBlockUsage(PsiElement codeBlock, String clazzName) {
            this.codeBlock = codeBlock;

            String fakeBeginStub = String.format("class %s { ", clazzName);
            String fakeEndStub = "\n}";

            this.ctClass = Launcher.parseClass(fakeBeginStub + codeBlock.getText() + fakeEndStub);
        }
        public String getCode() {
            return codeBlock.getText();
        }

        public double compareSimilarity(@NotNull CodeBlockUsage o) {


            try {
                int myFakeASTNodeCount = this.ctClass.filterChildren(null).list().size();

                int otherFakeASTNodeCount = o.ctClass.filterChildren(null).list().size();

                Diff astDiff = AST_COMPARATOR.compare(this.ctClass, o.ctClass);
                Set<Mapping> similiarities = astDiff.getMappingsComp().asSet();

                double simarility = 2 * similiarities.size() /
                        (double) (2 * similiarities.size() + myFakeASTNodeCount + otherFakeASTNodeCount);
                return simarility;

            }
            catch (Exception e) {
                System.out.println(o.toString());
                e.printStackTrace();
                throw new RuntimeException();
            }


//            double editScriptSize = astDiff.getRootOperations().size();
//          double normalizer = Double.max(myFakeASTNodeCount, otherFakeASTNodeCount);

//            double alpha = 1 - (editScriptSize / normalizer);

//            List<Operation> differences = astDiff.getRootOperations();


//            alpha = 1 - differences.size() / Double.max(myFakeAST.length())

//            double percentageSimilar = ((double) similiarities.size()) / (differences.size() + similiarities.size());
//            return percentageSimilar;

        }
    }
}
