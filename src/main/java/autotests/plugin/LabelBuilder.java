package autotests.plugin;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import cucumber.api.TestCase;
import gherkin.ast.Feature;
import gherkin.pickles.PickleTag;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;
import io.qameta.allure.util.ResultsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

class LabelBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelBuilder.class);
    private static final String COMPOSITE_TAG_DELIMITER = "=";
    private static final String SEVERITY = "@SEVERITY";
    private static final String ISSUE_LINK = "@ISSUE";
    private static final String TMS_LINK = "@TMSLINK";
    private static final String PLAIN_LINK = "@LINK";
    private final List<Label> scenarioLabels = new ArrayList();
    private final List<Link> scenarioLinks = new ArrayList();

    LabelBuilder(Feature feature, TestCase scenario, Deque<PickleTag> tags) {
        TagParser tagParser = new TagParser(feature, scenario);
        this.getScenarioLabels().add(ResultsUtils.createFeatureLabel(feature.getName()));
        this.getScenarioLabels().add(ResultsUtils.createStoryLabel(scenario.getName()));

        while(tags.peek() != null) {
            PickleTag tag = (PickleTag)tags.remove();
            String tagString = tag.getName();
            if (tagString.contains("=")) {
                String[] tagParts = tagString.split("=", 2);
                if (tagParts.length >= 2 && !Objects.isNull(tagParts[1]) && !tagParts[1].isEmpty()) {
                    String tagKey = tagParts[0].toUpperCase();
                    String tagValue = tagParts[1];
                    if (tagKey.startsWith("@LINK.")) {
                        this.tryHandleNamedLink(tagString);
                    } else {
                        byte var11 = -1;
                        switch(tagKey.hashCode()) {
                            case -1747008707:
                                if (tagKey.equals("@SEVERITY")) {
                                    var11 = 0;
                                }
                                break;
                            case -604956748:
                                if (tagKey.equals("@TMSLINK")) {
                                    var11 = 1;
                                }
                                break;
                            case 61442106:
                                if (tagKey.equals("@LINK")) {
                                    var11 = 3;
                                }
                                break;
                            case 1902237817:
                                if (tagKey.equals("@ISSUE")) {
                                    var11 = 2;
                                }
                        }

                        switch(var11) {
                            case 0:
                                this.getScenarioLabels().add(ResultsUtils.createSeverityLabel(tagValue.toLowerCase()));
                                break;
                            case 1:
                                this.getScenarioLinks().add(ResultsUtils.createTmsLink(tagValue));
                                break;
                            case 2:
                                this.getScenarioLinks().add(ResultsUtils.createIssueLink(tagValue));
                                break;
                            case 3:
                                this.getScenarioLinks().add(ResultsUtils.createLink((String)null, tagValue, tagValue, (String)null));
                                break;
                            default:
                                LOGGER.warn("Composite tag {} is not supported. adding it as RAW", tagKey);
                                this.getScenarioLabels().add(this.getTagLabel(tag));
                        }
                    }
                }
            } else if (tagParser.isPureSeverityTag(tag)) {
                this.getScenarioLabels().add(ResultsUtils.createSeverityLabel(tagString.substring(1)));
            } else if (!tagParser.isResultTag(tag)) {
                this.getScenarioLabels().add(this.getTagLabel(tag));
            }
        }

        this.getScenarioLabels().addAll(Arrays.asList(ResultsUtils.createHostLabel(), ResultsUtils.createThreadLabel(), ResultsUtils.createPackageLabel(feature.getName()), ResultsUtils.createSuiteLabel(feature.getName()), ResultsUtils.createTestClassLabel(scenario.getName()), ResultsUtils.createFrameworkLabel("cucumber3jvm"), ResultsUtils.createLanguageLabel("java")));
    }

    public List<Label> getScenarioLabels() {
        return this.scenarioLabels;
    }

    public List<Link> getScenarioLinks() {
        return this.scenarioLinks;
    }

    private Label getTagLabel(PickleTag tag) {
        return ResultsUtils.createTagLabel(tag.getName().substring(1));
    }

    private void tryHandleNamedLink(String tagString) {
        String namedLinkPatternString = "@LINK\\.(\\w+-?)+=(\\w+(-|_)?)+";
        Pattern namedLinkPattern = Pattern.compile("@LINK\\.(\\w+-?)+=(\\w+(-|_)?)+", 2);
        if (namedLinkPattern.matcher(tagString).matches()) {
            String type = tagString.split("=")[0].split("[.]")[1];
            String name = tagString.split("=")[1];
            this.getScenarioLinks().add(ResultsUtils.createLink((String)null, name, (String)null, type));
        } else {
            LOGGER.warn("Composite named tag {} is not matches regex {}. skipping", tagString, "@LINK\\.(\\w+-?)+=(\\w+(-|_)?)+");
        }

    }
}
