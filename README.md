# elasticsearch-concatenate-token-filter
Elasticsearch plugin which only provides a TokenFilter that merges tokens in a token stream back into one. Taken from http://elasticsearch-users.115913.n3.nabble.com/Is-there-a-concatenation-filter-td3711094.html

## Usage
The plugin provides a token filter of type `concatenate` which has one parameter `token_separator`. Use it in your custom analyzers to merge tokenized strings back into one single token (usually after applying stemming or other token filters).

## Example
Given the custom analyzer (see https://www.elastic.co/guide/en/elasticsearch/guide/current/custom-analyzers.html):

```java
{
  "analysis" : {
    "filter" : {
      "concatenate" : {
        "type" : "concatenate",
        "token_separator" : "_"
      },
      "custom_stop" : {
        "type": "stop",
        "stopwords": ["and", "is", "the"]
      }
    },
    "analyzer" : {
      "stop_concatenate" : {
        "filter" : [
          "custom_stop",
          "concatenate"
        ],
        "tokenizer" : "standard"
      }
    }
  }
}
```
the string:

    "the fox jumped over the fence"

would be analyzed as:

    "fox_jumped_over_fence"
