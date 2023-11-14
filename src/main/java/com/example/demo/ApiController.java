package com.example.demo;

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    private static final Logger LOGGER = Logger.getLogger(ApiController.class.getName());
    @PostMapping(value = "/api",
                    headers = {"content-type=application/json" }, 
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> postResource(@RequestBody RequestBodyModel requestBody) {

        //Check if body & parameters are present
        if(requestBody == null 
            || requestBody.getdbQuery().isEmpty() 
            || requestBody.getdbToken().isEmpty())
        {
            return ResponseEntity.badRequest().body("Invalid Call");
        }
        
        
        // Handle the POST request and return a response
        return ResponseEntity.ok("Received (POST): " + requestBody.getdbToken());
    }
    @GetMapping("/")
	public ResponseEntity<String> message(){
        LOGGER.info("Enter Logger");
		return ResponseEntity.ok("Congrats ! your application deployed successfully in Azure Platform. !");
	}



    /**
    * Process the input string and remove detokenize_long/short 
    * and store the columns that needs to be detokenize.
    * @param columnString A string containing user input query 
    * @param columnFunctionMap A map store column name and function pair.
    * @return A processed string of user input to be concatenated and submitted to databricks.
    */ 
    private static String processColumnString(String columnString,Map<String,ProcessFunction> columnFunctionMap)
    {
        //Seperate the string by comma and store into array 
        String[] splitColumns = columnString.split(",");

        //Register patters to look for 
        Pattern p_detokenizeLong = Pattern.compile("detokenize_long\\((.*?)\\)");
        Pattern p_detokenizeShort = Pattern.compile("detokenize_short\\((.*?)\\)");
        Pattern p_AliasKeyword = Pattern.compile("\\b" + Pattern.quote("as") + "\\b", Pattern.CASE_INSENSITIVE);
        
        StringBuilder processedColumns = new StringBuilder();
        
        //Iterate through the column array and check for patterns
        for (String column : splitColumns) {
            Matcher m_detokenizeLong = p_detokenizeLong.matcher(column);
            Matcher m_detokenizeShort = p_detokenizeShort.matcher(column);
            Matcher m_AliasKeyword = p_AliasKeyword.matcher(column);

            boolean longMatched = m_detokenizeLong.find();
            boolean shortMatched = m_detokenizeShort.find();
            boolean asMatched = m_AliasKeyword.find();
            //Check if detokenize long/short is found
            if (longMatched || shortMatched) {
                String innerToken;
                String replacement;

                if (longMatched) {
                    innerToken = m_detokenizeLong.group(1);
                    replacement = m_detokenizeLong.group();
                    //Store processed columns to a map. If it has an alias, store the alias name instead
                    columnFunctionMap.put(asMatched ? column.substring(m_AliasKeyword.end()).strip() : innerToken, ProcessFunction.DETOKENIZE_L);
                } else {
                    innerToken = m_detokenizeShort.group(1);
                    replacement = m_detokenizeShort.group();
                    columnFunctionMap.put(asMatched ? column.substring(m_AliasKeyword.end()).strip() : innerToken, ProcessFunction.DETOKENIZE_S);
                }
                //replace the old string with the processed string
                column = column.replace(replacement, innerToken);
            }
            //append the new string into the container 
            processedColumns.append(column);
            //Stitch the string back together
            if (!column.equals(splitColumns[splitColumns.length - 1])) {
                processedColumns.append(",");
            }
        }

        return processedColumns.toString();
    }

    //Enum to call specific function to process
    private enum ProcessFunction {
        DETOKENIZE_L {
            @Override
            public String apply(String innerToken)
            {
                return innerToken.toUpperCase();
            }
        },
        DETOKENIZE_S{
            @Override
            public String apply(String innerToken)
            {
                
                return innerToken.toLowerCase();
            }
        },
        TOKENIZE{
            @Override
            public String apply(String innertoken)
            {
                return "1970";
            }
        };
        public abstract String apply(String innerToken);
    }


}