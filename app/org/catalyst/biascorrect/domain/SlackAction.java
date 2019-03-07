package org.catalyst.biascorrect.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SlackAction {

    private String _name;

    private String _type;

    private String _value;

    @JsonProperty("name")
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    @JsonProperty("type")
    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    @JsonProperty("value")
    public String getValue() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public String toString() {
        return String.format("Name = %s, Type = %s, Value = %s", _name, _type, _value);
    }
}
