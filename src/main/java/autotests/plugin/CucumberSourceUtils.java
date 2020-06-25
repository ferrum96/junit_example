package autotests.plugin;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import cucumber.api.event.TestSourceRead;
import gherkin.*;
import gherkin.ast.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

final class CucumberSourceUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CucumberSourceUtils.class);
    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap();
    private final Map<String, GherkinDocument> pathToAstMap = new HashMap();
    private final Map<String, Map<Integer, AstNode>> pathToNodeMap = new HashMap();

    CucumberSourceUtils() {
    }

    public void addTestSourceReadEvent(String path, TestSourceRead event) {
        this.pathToReadEventMap.put(path, event);
    }

    public Feature getFeature(String path) {
        if (!this.pathToAstMap.containsKey(path)) {
            this.parseGherkinSource(path);
        }

        return this.pathToAstMap.containsKey(path) ? ((GherkinDocument)this.pathToAstMap.get(path)).getFeature() : null;
    }

    private void parseGherkinSource(String path) {
        if (this.pathToReadEventMap.containsKey(path)) {
            Parser<GherkinDocument> parser = new Parser(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();

            try {
                GherkinDocument gherkinDocument = (GherkinDocument)parser.parse(((TestSourceRead)this.pathToReadEventMap.get(path)).source, matcher);
                this.pathToAstMap.put(path, gherkinDocument);
                Map<Integer, AstNode> nodeMap = new HashMap();
                AstNode currentParent = new AstNode(gherkinDocument.getFeature(), (AstNode)null);
                Iterator var7 = gherkinDocument.getFeature().getChildren().iterator();

                while(var7.hasNext()) {
                    ScenarioDefinition child = (ScenarioDefinition)var7.next();
                    this.processScenarioDefinition(nodeMap, child, currentParent);
                }

                this.pathToNodeMap.put(path, nodeMap);
            } catch (ParserException var9) {
                LOGGER.trace(var9.getMessage(), var9);
            }

        }
    }

    private void processScenarioDefinition(Map<Integer, AstNode> nodeMap, ScenarioDefinition child, AstNode currentParent) {
        AstNode childNode = new AstNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        child.getSteps().forEach((step) -> {
            AstNode var10000 = (AstNode)nodeMap.put(step.getLocation().getLine(), new AstNode(step, childNode));
        });
        if (child instanceof ScenarioOutline) {
            this.processScenarioOutlineExamples(nodeMap, (ScenarioOutline)child, childNode);
        }

    }

    private void processScenarioOutlineExamples(Map<Integer, AstNode> nodeMap, ScenarioOutline scenarioOutline, AstNode childNode) {
        scenarioOutline.getExamples().forEach((examples) -> {
            AstNode examplesNode = new AstNode(examples, childNode);
            TableRow headerRow = examples.getTableHeader();
            AstNode headerNode = new AstNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);
            IntStream.range(0, examples.getTableBody().size()).forEach((i) -> {
                TableRow examplesRow = (TableRow)examples.getTableBody().get(i);
                Node rowNode = new ExamplesRowWrapperNode(examplesRow, i);
                AstNode expandedScenarioNode = new AstNode(rowNode, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            });
        });
    }

    private AstNode getAstNode(String path, int line) {
        if (!this.pathToNodeMap.containsKey(path)) {
            this.parseGherkinSource(path);
        }

        return this.pathToNodeMap.containsKey(path) ? (AstNode)((Map)this.pathToNodeMap.get(path)).get(line) : null;
    }

    public ScenarioDefinition getScenarioDefinition(String path, int line) {
        return this.getScenarioDefinition(this.getAstNode(path, line));
    }

    private ScenarioDefinition getScenarioDefinition(AstNode astNode) {
        return astNode.getNode() instanceof ScenarioDefinition ? (ScenarioDefinition)astNode.getNode() : (ScenarioDefinition)astNode.getParent().getParent().getNode();
    }

    public String getKeywordFromSource(String uri, int stepLine) {
        Feature feature = this.getFeature(uri);
        if (feature != null) {
            TestSourceRead event = this.getTestSourceReadEvent(uri);
            String trimmedSourceLine = event.source.split("\n")[stepLine - 1].trim();
            GherkinDialect dialect = (new GherkinDialectProvider(feature.getLanguage())).getDefaultDialect();
            Iterator var7 = dialect.getStepKeywords().iterator();

            while(var7.hasNext()) {
                String keyword = (String)var7.next();
                if (trimmedSourceLine.startsWith(keyword)) {
                    return keyword;
                }
            }
        }

        return "";
    }

    private TestSourceRead getTestSourceReadEvent(String uri) {
        return this.pathToReadEventMap.containsKey(uri) ? (TestSourceRead)this.pathToReadEventMap.get(uri) : null;
    }

    private static class AstNode {
        private final Node node;
        private final AstNode parent;

        AstNode(Node node, AstNode parent) {
            this.node = node;
            this.parent = parent;
        }

        public Node getNode() {
            return this.node;
        }

        public AstNode getParent() {
            return this.parent;
        }
    }

    private static class ExamplesRowWrapperNode extends Node {
        private final int bodyRowIndex;

        ExamplesRowWrapperNode(Node examplesRow, int bodyRowIndex) {
            super(examplesRow.getLocation());
            this.bodyRowIndex = bodyRowIndex;
        }

        public int getBodyRowIndex() {
            return this.bodyRowIndex;
        }
    }
}

