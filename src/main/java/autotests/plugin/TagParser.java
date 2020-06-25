//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package autotests.plugin;

import cucumber.api.TestCase;
import gherkin.ast.Feature;
import gherkin.pickles.PickleTag;
import io.qameta.allure.SeverityLevel;

import java.util.Arrays;

class TagParser {
    private static final String FLAKY = "@FLAKY";
    private static final String KNOWN = "@KNOWN";
    private static final String MUTED = "@MUTED";
    private final Feature feature;
    private final TestCase scenario;

    TagParser(Feature feature, TestCase scenario) {
        this.feature = feature;
        this.scenario = scenario;
    }

    protected boolean isFlaky() {
        return this.getStatusDetailByTag("@FLAKY");
    }

    protected boolean isMuted() {
        return this.getStatusDetailByTag("@MUTED");
    }

    protected boolean isKnown() {
        return this.getStatusDetailByTag("@KNOWN");
    }

    protected boolean getStatusDetailByTag(String tagName) {
        return this.scenario.getTags().stream().anyMatch((tag) -> {
            return tag.getName().equalsIgnoreCase(tagName);
        }) || this.feature.getTags().stream().anyMatch((tag) -> {
            return tag.getName().equalsIgnoreCase(tagName);
        });
    }

    protected boolean isResultTag(PickleTag tag) {
        return Arrays.asList("@FLAKY", "@KNOWN", "@MUTED").contains(tag.getName().toUpperCase());
    }

    protected boolean isPureSeverityTag(PickleTag tag) {
        return Arrays.stream(SeverityLevel.values()).map(SeverityLevel::value).map((value) -> {
            return "@" + value;
        }).anyMatch((value) -> {
            return value.equalsIgnoreCase(tag.getName());
        });
    }
}
