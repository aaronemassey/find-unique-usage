package edu.gmu.swe.intellij.uniqueusages.backend;

import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UsageAggregator {


    private Map<String, UniqueUsageGroup> usageToUniqueUsageGroup = new HashMap<String, UniqueUsageGroup>();
    private Map<String, UsageInfo> usageToUsageInfo = new HashMap<String, UsageInfo>();

    public UniqueUsageGroup getAggregateUsage(Usage usage) {
        UsageInfo usageInfo = ((UsageInfo2UsageAdapter) usage).getUsageInfo();
        String key = Objects.requireNonNull(Objects.requireNonNull(usageInfo.getElement()).getContext()).getText();

//        PsiElement currentElement = usageInfo.getElement();
//        while (!currentElement.toString().equals("PsiCodeBlock")) {
//            currentElement = currentElement.getContext();
//        }
//        PsiElement codeBlockOfUsage = currentElement; // TODO actually do something with this like AST diffing.

        usageToUniqueUsageGroup.putIfAbsent(key, new UniqueUsageGroup(key));
//        usageToUsageInfo.putIfAbsent(key, usageInfo);
        System.out.println("getAggregateUsage: it returns exact matches");
        usageToUniqueUsageGroup.forEach((k, v) -> System.out.println((k + ":" + v)));

        return usageToUniqueUsageGroup.get(key);
    }

    /**
     * it checks a strings if the similarity of the input with all elements in collection be
     * less than the threshold. In this way it consider it as unique string then insert it in map.
     * This distance is computed as levenshtein distance divided by the length of the longest string.
     * The resulting value is always in the interval [0.0 1.0] but it is not a metric anymore! 1 shows they are not similar at all.
     *
     * @param usage
     * @param differenceThreshold The threshold for define differenceThreshold. Values can be from zero to 1.
     *                            1 shows they are completely different. Zero means they are completely similar.
     * @return UniqueUsageGroup
     * @throws IllegalArgumentException if percentOfMatch do be in the [0 ...1].
     */

    public UniqueUsageGroup usageByStringDistance(Usage usage, double differenceThreshold) {

        if (differenceThreshold > 1 || differenceThreshold < 0) {
            throw new IllegalArgumentException("Invalid percent for matching");
        }
        NormalizedLevenshtein l = new NormalizedLevenshtein();
        final boolean[] existed = {false};
        final String[] keyFound = new String[1];

        try {
            UsageInfo usageInfo = ((UsageInfo2UsageAdapter) usage).getUsageInfo();
            String key = Objects.requireNonNull(Objects.requireNonNull(usageInfo.getElement()).getContext()).getText();
            //Checking existence in map, if usage exist it does nothing, it return the usage otherwise
            // if usage does not exist AND distanceThreshold is bigger than difference of current Key and any keys in map it put in map
            keyFound[0] = key;
            if (!usageToUniqueUsageGroup.containsKey(key)) {
                usageToUniqueUsageGroup.forEach((k, v) -> {
                    System.out.println((k + ":" + v));
                    double distance = l.distance(key, k);
                    // if it find any key in the map that distance is less than the differenceThreshold, it means this this key does exist
                    // so true the flag for avoid inserting this key
                    if (distance < differenceThreshold) {
                        existed[0] = true;
                        keyFound[0] = k;
                    }
                });
                if (!existed[0]) {
                    usageToUniqueUsageGroup.put(keyFound[0], new UniqueUsageGroup(key));
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace(); // TODO: to be fixed

        }
        return usageToUniqueUsageGroup.get(keyFound[0]);
    }

    /**
     * Example 1:  method(2),method(100), and method(65) are placed in ONE category.
     * Example 2: int rr=90; int rrr=90; System.out.println(a.method1A(rr)); System.out.println(a.method1A(rrr));
     *         System.out.println(a.method1A(new Integer(45))); are placed in ONE category
     * @param usage
     * @return
     */
    public UniqueUsageGroup usageByAbstraction(Usage usage) {
        UsageInfo usageInfo = ((UsageInfo2UsageAdapter) usage).getUsageInfo();
        String key = Objects.requireNonNull(Objects.requireNonNull(usageInfo.getElement())).getText();
        usageToUniqueUsageGroup.putIfAbsent(key, new UniqueUsageGroup(key));
        return usageToUniqueUsageGroup.get(key);
    }
}
