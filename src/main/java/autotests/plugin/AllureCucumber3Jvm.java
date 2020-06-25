package autotests.plugin;

import cucumber.api.*;
import cucumber.api.Result.Type;
import cucumber.api.event.*;
import cucumber.api.formatter.Formatter;
import gherkin.ast.*;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.*;
import io.qameta.allure.util.ResultsUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AllureCucumber3Jvm implements Formatter {
    private final AllureLifecycle lifecycle;
    private final Map<String, String> scenarioUuids;
    private final CucumberSourceUtils cucumberSourceUtils;
    private Feature currentFeature;
    private String currentFeatureFile;
    private TestCase currentTestCase;
    private String currentContainer;
    private boolean forbidTestCaseStatusChange;
    private final EventHandler<TestSourceRead> featureStartedHandler;
    private final EventHandler<TestCaseStarted> caseStartedHandler;
    private final EventHandler<TestCaseFinished> caseFinishedHandler;
    private final EventHandler<TestStepStarted> stepStartedHandler;
    private final EventHandler<TestStepFinished> stepFinishedHandler;
    private final EventHandler<WriteEvent> writeEventHandler;
    private final EventHandler<EmbedEvent> embedEventHandler;
    private static final String TXT_EXTENSION = ".txt";
    private static final String TEXT_PLAIN = "text/plain";

    public AllureCucumber3Jvm() {
        this(Allure.getLifecycle());
    }

    public AllureCucumber3Jvm(AllureLifecycle lifecycle) {
        this.scenarioUuids = new HashMap();
        this.cucumberSourceUtils = new CucumberSourceUtils();
        this.featureStartedHandler = this::handleFeatureStartedHandler;
        this.caseStartedHandler = this::handleTestCaseStarted;
        this.caseFinishedHandler = this::handleTestCaseFinished;
        this.stepStartedHandler = this::handleTestStepStarted;
        this.stepFinishedHandler = this::handleTestStepFinished;
        this.writeEventHandler = this::handleWriteEvent;
        this.embedEventHandler = this::handleEmbedEvent;
        this.lifecycle = lifecycle;
    }

    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this.featureStartedHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, this.caseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, this.caseFinishedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, this.stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, this.stepFinishedHandler);
        publisher.registerHandlerFor(WriteEvent.class, this.writeEventHandler);
        publisher.registerHandlerFor(EmbedEvent.class, this.embedEventHandler);
    }

    private void handleFeatureStartedHandler(TestSourceRead event) {
        this.cucumberSourceUtils.addTestSourceReadEvent(event.uri, event);
    }

    private void handleTestCaseStarted(TestCaseStarted event) {
        this.currentTestCase = event.testCase;
        this.currentFeatureFile = this.currentTestCase.getUri();
        this.currentFeature = this.cucumberSourceUtils.getFeature(this.currentFeatureFile);
        this.currentContainer = UUID.randomUUID().toString();
        this.forbidTestCaseStatusChange = false;
        Deque<PickleTag> tags = new LinkedList(this.currentTestCase.getTags());
        LabelBuilder labelBuilder = new LabelBuilder(this.currentFeature, this.currentTestCase, tags);
        String name = this.currentTestCase.getName();
        String featureName = this.currentFeature.getName();
        TestResult result = (new TestResult()).setUuid(this.getTestCaseUuid(this.currentTestCase)).setHistoryId(this.getHistoryId(this.currentTestCase)).setFullName(featureName + ": " + name).setName(name).setLabels(labelBuilder.getScenarioLabels()).setLinks(labelBuilder.getScenarioLinks());
        ScenarioDefinition scenarioDefinition = this.cucumberSourceUtils.getScenarioDefinition(this.currentFeatureFile, this.currentTestCase.getLine());
        if (scenarioDefinition instanceof ScenarioOutline) {
            result.setParameters(this.getExamplesAsParameters((ScenarioOutline)scenarioDefinition));
        }

        if (this.currentFeature.getDescription() != null && !this.currentFeature.getDescription().isEmpty()) {
            result.setDescription(this.currentFeature.getDescription());
        }

        TestResultContainer resultContainer = (new TestResultContainer()).setName(String.format("%s: %s", scenarioDefinition.getKeyword(), scenarioDefinition.getName())).setUuid(this.getTestContainerUuid()).setChildren(Collections.singletonList(this.getTestCaseUuid(this.currentTestCase)));
        this.lifecycle.scheduleTestCase(result);
        this.lifecycle.startTestContainer(this.getTestContainerUuid(), resultContainer);
        this.lifecycle.startTestCase(this.getTestCaseUuid(this.currentTestCase));
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        String uuid = this.getTestCaseUuid(event.testCase);
        Optional<StatusDetails> details = ResultsUtils.getStatusDetails(event.result.getError());
        details.ifPresent((statusDetails) -> {
            this.lifecycle.updateTestCase(uuid, (testResult) -> {
                testResult.setStatusDetails(statusDetails);
            });
        });
        this.lifecycle.stopTestCase(uuid);
        this.lifecycle.stopTestContainer(this.getTestContainerUuid());
        this.lifecycle.writeTestCase(uuid);
        this.lifecycle.writeTestContainer(this.getTestContainerUuid());
    }

    private void handleTestStepStarted(TestStepStarted event) {
        if (event.testStep instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStep = (PickleStepTestStep)event.testStep;
            String stepKeyword = (String)Optional.ofNullable(this.cucumberSourceUtils.getKeywordFromSource(this.currentFeatureFile, pickleStep.getStepLine())).orElse("UNDEFINED");
            StepResult stepResult = (new StepResult()).setName(String.format("%s %s", stepKeyword, pickleStep.getPickleStep().getText())).setStart(System.currentTimeMillis());
            this.lifecycle.startStep(this.getTestCaseUuid(this.currentTestCase), this.getStepUuid(pickleStep), stepResult);
            Stream var10000 = pickleStep.getStepArgument().stream();
            PickleTable.class.getClass();
            var10000.filter(PickleTable.class::isInstance).findFirst().ifPresent((table) -> {
                this.createDataTableAttachment((PickleTable)table);
            });
        } else if (event.testStep instanceof HookTestStep) {
            this.initHook((HookTestStep)event.testStep);
        }

    }

    private void initHook(HookTestStep hook) {
        FixtureResult hookResult = (new FixtureResult()).setName(hook.getCodeLocation()).setStart(System.currentTimeMillis());
        if (hook.getHookType() == HookType.Before) {
            this.lifecycle.startPrepareFixture(this.getTestContainerUuid(), this.getHookStepUuid(hook), hookResult);
        } else {
            this.lifecycle.startTearDownFixture(this.getTestContainerUuid(), this.getHookStepUuid(hook), hookResult);
        }

    }

    private void handleTestStepFinished(TestStepFinished event) {
        if (event.testStep instanceof HookTestStep) {
            this.handleHookStep(event);
        } else {
            this.handlePickleStep(event);
        }

    }

    private void handleWriteEvent(WriteEvent event) {
        this.lifecycle.addAttachment("Text output", "text/plain", ".txt", Objects.toString(event.text).getBytes(StandardCharsets.UTF_8));
    }

    private void handleEmbedEvent(EmbedEvent event) {
        this.lifecycle.addAttachment("Screenshot", (String)null, (String)null, new ByteArrayInputStream(event.data));
    }

    private String getTestContainerUuid() {
        return this.currentContainer;
    }

    private String getTestCaseUuid(TestCase testCase) {
        return (String)this.scenarioUuids.computeIfAbsent(this.getHistoryId(testCase), (it) -> {
            return UUID.randomUUID().toString();
        });
    }

    private String getStepUuid(PickleStepTestStep step) {
        return this.currentFeature.getName() + this.getTestCaseUuid(this.currentTestCase) + step.getPickleStep().getText() + step.getStepLine();
    }

    private String getHookStepUuid(HookTestStep step) {
        return this.currentFeature.getName() + this.getTestCaseUuid(this.currentTestCase) + step.getHookType().toString() + step.getCodeLocation();
    }

    private String getHistoryId(TestCase testCase) {
        String testCaseLocation = testCase.getUri() + ":" + testCase.getLine();
        return ResultsUtils.md5(testCaseLocation);
    }

    private Status translateTestCaseStatus(Result testCaseResult) {
        switch(testCaseResult.getStatus()) {
            case FAILED:
                return (Status)ResultsUtils.getStatus(testCaseResult.getError()).orElse(Status.FAILED);
            case PASSED:
                return Status.PASSED;
            case SKIPPED:
            case PENDING:
                return Status.SKIPPED;
            case AMBIGUOUS:
            case UNDEFINED:
            default:
                return null;
        }
    }

    private List<Parameter> getExamplesAsParameters(ScenarioOutline scenarioOutline) {
        Optional<Examples> examplesBlock = scenarioOutline.getExamples().stream().filter((example) -> {
            return example.getTableBody().stream().anyMatch((row) -> {
                return row.getLocation().getLine() == this.currentTestCase.getLine();
            });
        }).findFirst();
        if (examplesBlock.isPresent()) {
            TableRow row = (TableRow)((Examples)examplesBlock.get()).getTableBody().stream().filter((example) -> {
                return example.getLocation().getLine() == this.currentTestCase.getLine();
            }).findFirst().get();
            return (List)IntStream.range(0, ((Examples)examplesBlock.get()).getTableHeader().getCells().size()).mapToObj((index) -> {
                String name = ((TableCell)((Examples)examplesBlock.get()).getTableHeader().getCells().get(index)).getValue();
                String value = ((TableCell)row.getCells().get(index)).getValue();
                return ResultsUtils.createParameter(name, value);
            }).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private void createDataTableAttachment(PickleTable pickleTable) {
        List<PickleRow> rows = pickleTable.getRows();
        StringBuilder dataTableCsv = new StringBuilder();
        if (!rows.isEmpty()) {
            rows.forEach((dataTableRow) -> {
                dataTableCsv.append((String)dataTableRow.getCells().stream().map(PickleCell::getValue).collect(Collectors.joining("\t")));
                dataTableCsv.append('\n');
            });
            String attachmentSource = this.lifecycle.prepareAttachment("Data table", "text/tab-separated-values", "csv");
            this.lifecycle.writeAttachment(attachmentSource, new ByteArrayInputStream(dataTableCsv.toString().getBytes(StandardCharsets.UTF_8)));
        }

    }

    private void handleHookStep(TestStepFinished event) {
        HookTestStep hookStep = (HookTestStep)event.testStep;
        String uuid = this.getHookStepUuid(hookStep);
        FixtureResult fixtureResult = (new FixtureResult()).setStatus(this.translateTestCaseStatus(event.result));
        if (!Status.PASSED.equals(fixtureResult.getStatus())) {
            TestResult testResult = (new TestResult()).setStatus(this.translateTestCaseStatus(event.result));
            StatusDetails statusDetails = (StatusDetails)ResultsUtils.getStatusDetails(event.result.getError()).get();
            statusDetails.setMessage(hookStep.getHookType().name() + " is failed: " + event.result.getError().getLocalizedMessage());
            if (hookStep.getHookType() == HookType.Before) {
                TagParser tagParser = new TagParser(this.currentFeature, this.currentTestCase);
                statusDetails.setFlaky(tagParser.isFlaky()).setMuted(tagParser.isMuted()).setKnown(tagParser.isKnown());
                testResult.setStatus(Status.SKIPPED);
                this.updateTestCaseStatus(testResult.getStatus());
                this.forbidTestCaseStatusChange = true;
            } else {
                testResult.setStatus(Status.BROKEN);
                this.updateTestCaseStatus(testResult.getStatus());
            }

            fixtureResult.setStatusDetails(statusDetails);
        }

        this.lifecycle.updateFixture(uuid, (result) -> {
            result.setStatus(fixtureResult.getStatus()).setStatusDetails(fixtureResult.getStatusDetails());
        });
        this.lifecycle.stopFixture(uuid);
    }

    private void handlePickleStep(TestStepFinished event) {
        Status stepStatus = this.translateTestCaseStatus(event.result);
        StatusDetails statusDetails;
        if (event.result.getStatus() == Type.UNDEFINED) {
            this.updateTestCaseStatus(Status.PASSED);
            statusDetails = (StatusDetails)ResultsUtils.getStatusDetails(new PendingException("TODO: implement me")).orElse(new StatusDetails());
            this.lifecycle.updateTestCase(this.getTestCaseUuid(this.currentTestCase), (scenarioResult) -> {
                scenarioResult.setStatusDetails(statusDetails);
            });
        } else {
            statusDetails = (StatusDetails)ResultsUtils.getStatusDetails(event.result.getError()).orElse(new StatusDetails());
            this.updateTestCaseStatus(stepStatus);
        }

        if (!Status.PASSED.equals(stepStatus) && stepStatus != null) {
            this.forbidTestCaseStatusChange = true;
        }

        TagParser tagParser = new TagParser(this.currentFeature, this.currentTestCase);
        statusDetails.setFlaky(tagParser.isFlaky()).setMuted(tagParser.isMuted()).setKnown(tagParser.isKnown());
        this.lifecycle.updateStep(this.getStepUuid((PickleStepTestStep)event.testStep), (stepResult) -> {
            stepResult.setStatus(stepStatus).setStatusDetails(statusDetails);
        });
        this.lifecycle.stopStep(this.getStepUuid((PickleStepTestStep)event.testStep));
    }

    private void updateTestCaseStatus(Status status) {
        if (!this.forbidTestCaseStatusChange) {
            this.lifecycle.updateTestCase(this.getTestCaseUuid(this.currentTestCase), (result) -> {
                result.setStatus(status);
            });
        }

    }
}

