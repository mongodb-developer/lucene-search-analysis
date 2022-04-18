package com.mongodb.atlas.search.analysis.views.analysis;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
//import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.mongodb.atlas.search.analysis.LuceneAnalyzer;
import com.mongodb.atlas.search.analysis.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import elemental.json.Json;
import elemental.json.JsonObject;
import elemental.json.impl.JsonUtil;

@PageTitle("Analysis")
@Route(value = "analysis", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class AnalysisView extends Div {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private final static Logger logger = Logger.getLogger(AnalysisView.class.getName());
	private Select<String> analyzer = new Select<String>();
    private String selectedAnalyzer = new String("Standard");
	private Select<String> operator = new Select<String>();
	private String selectedOperator = new String("Text");
	private String selectedLanguage = new String();
	private Select<String> tokenizer = new Select<String>();
	private String selectedTokenizer = new String("edgeNGram");
	private IntegerField minGram = new IntegerField();
	private IntegerField maxGram = new IntegerField();
    private ComboBox<String> language = new ComboBox<>("Language");
    private TextField textToAnalyze = new TextField("Text to analyze");
    private TextField customAnalyzer = new TextField("Name of custom analyzer");
    private TextArea indexDefinition = new TextArea("Index definition");
    private Div output = new Div();
    private JsonObject indexDef = null;
    MemoryBuffer memoryBuffer = new MemoryBuffer();
    Upload defUpload = new Upload(memoryBuffer);
    
    private Button clear = new Button("Clear");
    private Button submit = new Button("Analyze");

    public AnalysisView() {
        addClassName("analysis-view");

        add(createTitle());
        add(createFormLayout());
        add(createButtonLayout());

        clear.addClickListener(e -> {
            clearOutput();
        });
        submit.addClickListener(e -> {
        	/*
            logger.info("Analyzer: " + selectedAnalyzer);
            if (selectedAnalyzer.equalsIgnoreCase("language")) {
            	logger.info("Language: " + selectedLanguage);
            }
        	logger.info("Text: " + textToAnalyze.getValue());
            logger.info("Operator: " + selectedOperator);
            logger.info("Tokenizer: " + selectedTokenizer);
            logger.info("MinGram: " + minGram.getValue());
            logger.info("MaxGram: " + maxGram.getValue());
            */
            LuceneAnalyzer luceneAnalyzer = new LuceneAnalyzer();
			Analyzer analyzer = null;
			String result = null;
			
			try {	
				if (null != selectedOperator && selectedOperator.equalsIgnoreCase("autocomplete")) {
					analyzer = luceneAnalyzer.buildAutocompleteAnalyzer(selectedTokenizer, minGram.getValue().toString(), maxGram.getValue().toString());
				} else {
					switch (selectedAnalyzer.toLowerCase()) {
						case "simple":
							analyzer = new SimpleAnalyzer();
							break;
			
						case "standard":
							analyzer = new StandardAnalyzer();
							break;
			
						case "whitespace":
							analyzer = new WhitespaceAnalyzer();
							break;
			
						case "keyword":
							analyzer = new KeywordAnalyzer();
							break;
							
						case "language":
							analyzer = luceneAnalyzer.getLanguageAnalyzer(selectedLanguage);
							if (null == analyzer) {
								setError(null == selectedLanguage || selectedLanguage.trim().length() == 0 ? "Language is required" : "Unsupported language " + selectedLanguage);
							}
							break;
							
						case "custom":
							analyzer = luceneAnalyzer.buildCustomAnalyzer(indexDef, customAnalyzer.getValue());
							if (null == analyzer) {
								setError("Unable to construct the custom analyzer from the index definition");
							}
							break;
					} // end switch
				} // end if selectedOperator.equalsIgnoreCase("autocomplete")
				
				if (null != analyzer) {
					String text = textToAnalyze.getValue();
					if (null == text || text.trim().length() == 0) {
						setError("Text to analyze is required");
					} else {
						result = luceneAnalyzer.printTokens(analyzer, textToAnalyze.getValue(), selectedOperator, selectedTokenizer, minGram.getValue().toString(), maxGram.getValue().toString());
						if (null != result) {
							output.getElement().setProperty("innerHTML", result);
						}
					}
				}
			} catch (Exception ex) {
				setError(ex.getLocalizedMessage());
			}
            
        });
    }
    
    private void setError(String error) {
    	output.getElement().setProperty("innerHTML", "<span style=\"color: red\">" + error + "</span>");
    }
    
    private void clearOutput() {
    	output.getElement().setProperty("innerHTML", "");
    }
    
    private Component createTitle() {
		Image img = new Image("images/mongodb-atlas.png", "MongoDB Atlas Search");
		img.setWidth("200px");
		img.getStyle().set("margin-top", "20px");
		return img;

        //return new H3("Atlas Search Analysis");
    }

    private Component createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(operator, tokenizer, minGram, maxGram, analyzer, language,
        	textToAnalyze, defUpload, indexDefinition, customAnalyzer, output);
        return formLayout;
    }

    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addClassName("button-layout");
        
        //textToAnalyze.setRequired(true);
        textToAnalyze.setRequiredIndicatorVisible(true);
        textToAnalyze.setErrorMessage("This field is required");
        textToAnalyze.addValueChangeListener(event -> {
        	clearOutput();
        });
        
        String[] analyzers = {"Standard", "Simple", "Whitespace", "Language", "Keyword", "Custom"};
        analyzer.setItems(analyzers);
        analyzer.setLabel("Analyzer");
        analyzer.setPlaceholder("Select an analyzer");
        analyzer.addValueChangeListener(event -> {
        	clearOutput();
        	selectedAnalyzer = event.getValue();
        	language.setVisible(selectedAnalyzer.equalsIgnoreCase("language"));
        	customAnalyzer.setVisible(selectedAnalyzer.equalsIgnoreCase("custom"));
        	indexDefinition.setVisible(selectedAnalyzer.equalsIgnoreCase("custom"));
        	defUpload.setVisible(selectedAnalyzer.equalsIgnoreCase("custom"));
        });
        
        String[] operators = {"Text", "Autocomplete"};
        operator.setItems(operators);
        operator.setLabel("Operator");
        operator.setPlaceholder("Select a query operator");
        operator.addValueChangeListener(event -> {
        	clearOutput();
        	selectedOperator = event.getValue();
        	tokenizer.setVisible(selectedOperator.equalsIgnoreCase("autocomplete"));
        	minGram.setVisible(selectedOperator.equalsIgnoreCase("autocomplete"));
        	maxGram.setVisible(selectedOperator.equalsIgnoreCase("autocomplete"));
    		analyzer.setVisible(!selectedOperator.equalsIgnoreCase("autocomplete"));
    		customAnalyzer.setVisible(!selectedOperator.equalsIgnoreCase("autocomplete"));
        	defUpload.setVisible(!selectedOperator.equalsIgnoreCase("autocomplete"));
        	indexDefinition.setVisible(!selectedOperator.equalsIgnoreCase("autocomplete"));
        	if (selectedOperator.equalsIgnoreCase("autocomplete")) {
        		customAnalyzer.clear();
        	}
        });
                
        String[] tokenizers = {"edgeNGram", "nGram"};
        tokenizer.setItems(tokenizers);
        tokenizer.setLabel("Tokenizer");
        tokenizer.setPlaceholder("Select an autocomplete tokenizer");
        tokenizer.addValueChangeListener(event -> {
        	clearOutput();
        	selectedTokenizer = event.getValue();
        });
        
        minGram.setLabel("MinGram");
        maxGram.setLabel("MaxGram");
        minGram.setValue(2);
        minGram.setMin(1);
        minGram.setMax(15);
        maxGram.setValue(3);
        maxGram.setMin(1);
        maxGram.setMax(15);
        minGram.setHasControls(true);
        maxGram.setHasControls(true);
        
        String[] languages = {
    		"lucene.arabic",
    		"lucene.armenian",
    		"lucene.basque",
    		"lucene.bengali",
    		"lucene.brazilian",
    		"lucene.bulgarian",
    		"lucene.catalan",
    		"lucene.cjk 1",
    		"lucene.czech",
    		"lucene.danish",
    		"lucene.dutch",
    		"lucene.english",
    		"lucene.finnish",
    		"lucene.french",
    		"lucene.galician",
    		"lucene.german",
    		"lucene.greek",
    		"lucene.hindi",
    		"lucene.hungarian",
    		"lucene.indonesian",
    		"lucene.irish",
    		"lucene.italian",
    		"lucene.japanese",
    		"lucene.korean",
    		"lucene.latvian",
    		"lucene.lithuanian",
    		"lucene.norwegian",
    		"lucene.persian",
    		"lucene.portuguese",
    		"lucene.romanian",
    		"lucene.russian",
    		"lucene.smartcn 5",
    		"lucene.sorani",
    		"lucene.spanish",
    		"lucene.swedish",
    		"lucene.thai",
    		"lucene.turkish",
    		"lucene.ukrainian"};
        language.setItems(languages);
        language.setPlaceholder("Select a language");
    	language.setVisible(false);
    	language.addValueChangeListener(event -> {
        	clearOutput();
        	selectedLanguage = event.getValue();
    	});
    	
    	customAnalyzer.setVisible(false);
    	tokenizer.setVisible(false);
    	indexDefinition.setVisible(false);
    	defUpload.setVisible(false);
    	minGram.setVisible(false);
    	maxGram.setVisible(false);
    	output.setVisible(true);
    	//output.setHeight("200px");
    	output.setWidth("100%");
    	
    	defUpload.setDropLabel(new Span("Drop index definition file here"));
    	defUpload.addSucceededListener(event -> {
        	clearOutput();
        	indexDefinition.clear();
        	
    	    // Get information about the uploaded file
    	    InputStream fileData = memoryBuffer.getInputStream();
    	    long contentLength = event.getContentLength();
    	    String mimeType = event.getMIMEType();
    	    if (contentLength <= 0) {
    	    	setError("Empty definition file");
    	    } else if (!mimeType.equals("application/json")) {
    	    	setError("Encountered " + mimeType + " file -- expecting JSON");
    	    } else {
	    	    // Do something with the file data
	    	    try {
		    	    String jsonString = new String(fileData.readAllBytes(), StandardCharsets.UTF_8);
		    	    indexDef = Json.parse(jsonString);
		    	    indexDefinition.setValue(JsonUtil.stringify(indexDef, 2));  
	    	    } catch (Exception ex) {
	    	    	setError(ex.getLocalizedMessage());
	    	    }
    	    }
    	});
    	
        indexDefinition.addValueChangeListener(event -> {
    	    indexDef = Json.parse(event.getValue());        	
        });
        
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(submit);
        buttonLayout.add(clear);
        return buttonLayout;
    }
}
