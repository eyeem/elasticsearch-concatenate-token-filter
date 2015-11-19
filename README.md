# Elasticsearch concatenation token filter
Elasticsearch plugin providing the `concatenate` token filter for merging tokens in a token stream back into one single token. The Lucene code is taken from a [discussion thread](http://elasticsearch-users.115913.n3.nabble.com/Is-there-a-concatenation-filter-td3711094.html) on the Elasticsearch mailing list.

## Usage
The plugin provides a token filter of type `concatenate`. The only provided parameter is `token_separator`, that allows to set the separator to use when concatenating the tokens back together.

## Example usage

```javascript
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

```bash
> POST /test/_analyze?text=the fox jumped over the fence&analyzer=stop_concatenate
> {
    "tokens": [
      {
        "token": "this is a test",
        "start_offset": 0,
        "end_offset": 0,
        "type": "word",
        "position": 1
      }
    ]
  }
