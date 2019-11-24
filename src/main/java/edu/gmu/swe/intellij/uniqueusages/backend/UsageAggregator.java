package edu.gmu.swe.intellij.uniqueusages.backend;

import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;

import java.util.HashMap;
import java.util.Map;

public class UsageAggregator {


    private Map<String, UniqueUsageGroup> usageToUniqueUsageGroup = new HashMap<String, UniqueUsageGroup> ();
    private Map<String, UsageInfo> usageToUsageInfo = new HashMap<String, UsageInfo> ();

    public UniqueUsageGroup getAggregateUsage(Usage usage) {
        UsageInfo usageInfo = ((UsageInfo2UsageAdapter) usage).getUsageInfo();
        String key = usageInfo.getElement().getContext().getText();

//        PsiElement currentElement = usageInfo.getElement();
//        while (!currentElement.toString().equals("PsiCodeBlock")) {
//            currentElement = currentElement.getContext();
//        }
//        PsiElement codeBlockOfUsage = currentElement; // TODO actually do something with this like AST diffing.

        usageToUniqueUsageGroup.putIfAbsent(key, new UniqueUsageGroup(key));
        usageToUsageInfo.putIfAbsent(key, usageInfo);

        return usageToUniqueUsageGroup.get(key);
    }
}
